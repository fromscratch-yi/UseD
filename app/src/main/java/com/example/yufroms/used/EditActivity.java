package com.example.yufroms.used;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity {
  private int id, amount, kind, year, month, day,yearBefo, monthBefo, mYear, mMonth, mDay, kindVal, pushedID;
  private String remarks, dateVal, remarksVal, amountVal, dateString;
  private long ret;
  private SQLiteDatabase db;
  private Cursor cursor = null;
  private InputMethodManager inputMethodManager;
  private LinearLayout mainLayout;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit);
    final UsedDbOpenHelper helper = new UsedDbOpenHelper(this);
    final TextView dateInput = (TextView) findViewById(R.id.input_date);
    final EditText amountInput = (EditText) findViewById(R.id.input_amount);
    final Spinner kindInput = (Spinner) findViewById(R.id.select_kind);
    final EditText remarksInput = (EditText) findViewById(R.id.input_remarks);
    final LinearLayout amountLayout = (LinearLayout) findViewById(R.id.amount_edit);

    Intent intent = getIntent();
    id = intent.getIntExtra("ID", 0);
    yearBefo = intent.getIntExtra("YEAR", 0);
    monthBefo = intent.getIntExtra("MONTH", 0);
    pushedID = intent.getIntExtra("PUSHED", 0);
    try {
      // DB接続
      db = helper.getWritableDatabase();
      cursor = db.rawQuery("SELECT year, month, day, amount, kind, remarks FROM used_tbl " +
          "WHERE id = ?", new String[]{String.valueOf(id)});
      boolean isEof = cursor.moveToFirst();
      while (isEof) {
        year = cursor.getInt(0);
        month = cursor.getInt(1);
        day = cursor.getInt(2);
        amount = cursor.getInt(3);
        kind = cursor.getInt(4);
        remarks = cursor.getString(5);
        isEof = cursor.moveToNext();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    dateInput.setText(year + "/" + String.format("%02d", month) + "/" + String.format("%02d", day));
    amountInput.setText(String.valueOf(amount));
    kindInput.setSelection(kind);
    remarksInput.setText(remarks);

    // キーボード制御用
    inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    mainLayout = (LinearLayout)findViewById(R.id.add_screen);

    mYear = year;
    mMonth = month;
    mDay = day;

    /**
     * 日付入力フォームをクリック時のイベント
     */
    dateInput.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        DatePickerDialog datePickerDialog = new DatePickerDialog(EditActivity.this, android.R.style.Theme_Holo_Dialog,
            new DatePickerDialog.OnDateSetListener() {
              @Override
              public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
                dateInput.setText(dateString);
              }
            }, mYear, mMonth, mDay);
        datePickerDialog.show();
      }
    });

    /**
     * 金額入力フォームをクリック時のイベント
     */
    amountLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v){
        amountInput.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(amountInput, 0);
      }
    });

    /**
     * バックボタンをクリック時のイベント
     */
    ImageButton backBtn = (ImageButton) findViewById(R.id.back_btn);
    backBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 確認ダイアログの作成
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditActivity.this);
        alertDialog.setTitle("確認");
        alertDialog.setMessage("入力データは保存されません。\nよろしいですか？");

        // Yesボタン押下イベント
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(EditActivity.this, MonHistoryActivity.class);
            intent.putExtra("YEAR", yearBefo);
            intent.putExtra("MONTH", monthBefo);
            intent.putExtra("PUSHED", pushedID);
            startActivity(intent);
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
          }
        });

        // Noボタン押下イベント
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        });
        alertDialog.create().show();
      }
    });
    /**
     * ゴミ箱ボタンをクリック時のイベント
     */
    ImageButton dustBtn = (ImageButton) findViewById(R.id.dust_btn);
    dustBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 確認ダイアログの作成
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditActivity.this);
        alertDialog.setTitle("注意");
        alertDialog.setMessage("削除したデータは復元できません。\nよろしいですか？");

        // Yesボタン押下イベント
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            UsedDbOpenHelper helper = new UsedDbOpenHelper(EditActivity.this);
            db = helper.getReadableDatabase();
            db.delete("used_tbl", "id = " + id, null);
            Intent intent = new Intent(EditActivity.this, MonHistoryActivity.class);
            intent.putExtra("YEAR", yearBefo);
            intent.putExtra("MONTH", monthBefo);
            intent.putExtra("PUSHED", pushedID);
            startActivity(intent);
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
          }
        });

        // Noボタン押下イベント
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        });
        alertDialog.create().show();
      }
    });

    /**
     * チェックボタンをクリック時のイベント(DB登録)
     */
    ImageButton checkBtn = (ImageButton) findViewById(R.id.check_btn);
    checkBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dateVal = dateInput.getText().toString();
        amountVal = amountInput.getText().toString();
        kindVal = kindInput.getSelectedItemPosition();
        remarksVal = remarksInput.getText().toString();
        if (dateVal.length() == 0 ) {
          Toast.makeText(EditActivity.this, "日付を入力してください。", Toast.LENGTH_SHORT).show();
          return;
        } else if (amountVal.length() == 0) {
          Toast.makeText(EditActivity.this, "金額を入力してください。", Toast.LENGTH_SHORT).show();
          return;
        }
        // 確認ダイアログの作成
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditActivity.this);
        alertDialog.setTitle("確認");
        alertDialog.setMessage("入力データを保存しますか?");

        // Yesボタン押下イベント
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            year = Integer.parseInt(dateVal.substring(0, 4));
            month = Integer.parseInt(dateVal.substring(5, 7));
            day = Integer.parseInt(dateVal.substring(8, 10));
            amount = Integer.parseInt(amountVal);
            UsedDbOpenHelper helper = new UsedDbOpenHelper(EditActivity.this);
            db = helper.getReadableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("year", year);
            cv.put("month", month);
            cv.put("day", day);
            cv.put("amount", amount);
            cv.put("kind", kindVal);
            if (remarksVal.length() != 0) {
              cv.put("remarks", remarksVal);
            }
            ret = db.update("used_tbl", cv, "id = ?", new String[]{String.valueOf(id)});
            if (ret<0) {
              Log.e("Error", "データ追加に失敗");
            }
            db.close();
            Intent intent = new Intent(EditActivity.this, MonHistoryActivity.class);
            intent.putExtra("YEAR", yearBefo);
            intent.putExtra("MONTH", monthBefo);
            intent.putExtra("PUSHED", pushedID);
            startActivity(intent);
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
          }
        });

        // Noボタン押下イベント
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        });
        alertDialog.create().show();
      }
    });

  }
  /**
   * EditText編集時に背景をタップしたらキーボードを閉じるようにするタッチイベントの処理
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    //キーボードを隠す
    inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    //背景にフォーカスを移す
    mainLayout.requestFocus();
    return false;
  }
  /**
   * AndroidのBackボタンを無効
   */
  @Override
  public void onBackPressed() {
  }
}