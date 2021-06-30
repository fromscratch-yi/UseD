package com.example.yufroms.used;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.R.attr.id;

public class AddActivity extends AppCompatActivity {
  private int mYear, mMonth, mDay, kindVal, year, month, day, amount;
  private long id;
  private String dateVal, remarksVal, amountVal, dateString;
  private SQLiteDatabase db;
  private InputMethodManager inputMethodManager;
  private LinearLayout mainLayout;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add);
    //日付入力フォーム
    final TextView dateInput = (TextView) findViewById(R.id.input_date);
    //金額入力フォーム
    final EditText amountInput = (EditText) findViewById(R.id.input_amount);
    //種別選択フォーム
    final Spinner kindInput = (Spinner) findViewById(R.id.select_kind);
    //備考入力フォーム
    final EditText remarksInput = (EditText) findViewById(R.id.input_remarks);
    //金額入力レイアウト
    final LinearLayout amountLayout = (LinearLayout) findViewById(R.id.amount_edit);
    //Calendarクラス
    final Calendar calendar = Calendar.getInstance();
    //Dateクラス
    final Date date = new Date();
    //日付フォーマット
    final SimpleDateFormat simpleDateFormatDay = new SimpleDateFormat("yyyy/MM/dd");

    // キーボード制御用
    inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    mainLayout = (LinearLayout)findViewById(R.id.add_screen);

    // 初期値は現在日付
    mYear = calendar.get(Calendar.YEAR);
    mMonth = calendar.get(Calendar.MONTH);
    mDay = calendar.get(Calendar.DAY_OF_MONTH);

    /**
     * 日付入力フォームをクリック時のイベント
     */
    dateInput.setText(simpleDateFormatDay.format(date));
    dateInput.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddActivity.this, android.R.style.Theme_Holo_Dialog,
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddActivity.this);
        alertDialog.setTitle("確認");
        alertDialog.setMessage("入力データは保存されません。\nよろしいですか？");

        // Yesボタン押下イベント
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(AddActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
          }
        });

        // Noボタン押下イベント
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {}
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
          Toast.makeText(AddActivity.this, "日付を入力してください。", Toast.LENGTH_SHORT).show();
          return;
        } else if (amountVal.length() == 0) {
          Toast.makeText(AddActivity.this, "金額を入力してください。", Toast.LENGTH_SHORT).show();
          return;
        }
        // 確認ダイアログの作成
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddActivity.this);
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
            UsedDbOpenHelper helper = new UsedDbOpenHelper(AddActivity.this);
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
            id = db.insert("used_tbl", null, cv);
            if (id<0) {
              Log.e("Error", "データ追加に失敗");
            }
            db.close();
            Intent intent = new Intent(AddActivity.this, MainActivity.class);
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