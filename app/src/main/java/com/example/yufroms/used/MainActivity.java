package com.example.yufroms.used;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
  private SQLiteDatabase db = null;
  private Cursor cursor = null;
  private int year = 0;
  private int month = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final TextView dateDisp = (TextView)findViewById(R.id.date);
    final TextView totalDisp = (TextView)findViewById(R.id.total);
    final TextView spendDisp = (TextView)findViewById(R.id.spend);
    final TextView wasteDisp = (TextView)findViewById(R.id.waste);
    final TextView investmentDisp = (TextView)findViewById(R.id.investment);
    final TextView spendRate = (TextView)findViewById(R.id.spend_rate);
    final TextView wasteRate = (TextView)findViewById(R.id.waste_rate);
    final TextView investmentRate = (TextView)findViewById(R.id.investment_rate);
    final ImageButton addBtn = (ImageButton) findViewById(R.id.add_btn);
    final ImageButton hBtn = (ImageButton) findViewById(R.id.h_btn);
    final FrameLayout grahpArea = (FrameLayout) findViewById(R.id.grahp_area);
    final Calendar calendar = Calendar.getInstance();
    final UsedDbOpenHelper helper = new UsedDbOpenHelper(this);
    final NavigationView navigationView = (NavigationView)findViewById(R.id.navigation);
    final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.my_drawer);

    // 共通クラス
    CommonSub sub = new CommonSub();

    year = calendar.get(Calendar.YEAR);
    month = calendar.get(Calendar.MONTH) + 1;

    // 月を文字列に変換
    String month_name = sub.getMonthName(month);

    // 現在月を表示
    String dateStr = String.valueOf(year) + "." + month_name;
    dateDisp.setText(dateStr);

    int spend = 0;
    int waste = 0;
    int investment = 0;

    /**
     * DBから登録情報を取得し、表示する
     */
    try {
      // DB接続
      db = helper.getWritableDatabase();
      // 現在までの現在月の支出合計を取得
      cursor = db.rawQuery("SELECT kind, SUM(amount) FROM used_tbl " +
          "WHERE year = ? AND month = ? GROUP BY kind", new String[]{String.valueOf(year), String.valueOf(month)});
      boolean isEof = cursor.moveToFirst();
      while (isEof) {
        // 消費合計
        if (cursor.getInt(0) == 0) {
          spend = cursor.getInt(1);
        }
        // 浪費合計
        else if (cursor.getInt(0) == 1) {
          waste = cursor.getInt(1);
        }
        // 投資合計
        else if (cursor.getInt(0) == 2) {
          investment = cursor.getInt(1);
        }
        isEof = cursor.moveToNext();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    // 合計金額
    int total = spend + waste + investment;

    if (total > 0) {
      // 割合を計算
      BigDecimal sp = new BigDecimal((double)spend / (double)total);
      BigDecimal ws = new BigDecimal((double)waste / (double)total);
      BigDecimal iv = new BigDecimal((double)investment / (double)total);

      // 割合を%に変換
      int spend_rate = (int)(sp.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100);
      int waste_rate = (int)(ws.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100);
      int investment_rate = (int)(iv.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100);

      // データをディスプレイへ表示
      totalDisp.setText("¥ " + String.format("%,d", total) + " -");
      spendDisp.setText("¥ " + String.format("%,d", spend) + " -");
      wasteDisp.setText("¥ " + String.format("%,d", waste) + " -");
      investmentDisp.setText("¥ " + String.format("%,d", investment) + " -");
      spendRate.setText("(" + spend_rate + "%)");
      wasteRate.setText("(" + waste_rate + "%)");
      investmentRate.setText("(" + investment_rate + "%)");
    }

    /**
     * 追加ボタンクリック時のイベント
     */
    addBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, AddActivity.class);
        startActivity(intent);
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
      }
    });
    /**
     * ナビゲーションボタンクリック時のイベント
     */
    hBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDrawerLayout.openDrawer(Gravity.LEFT);
      }
    });
    /**
     * グラフクリック時のイベント
     */
    grahpArea.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, MonHistoryActivity.class);
        intent.putExtra("YEAR", year);
        intent.putExtra("MONTH", month);
        startActivity(intent);
      }
    });
    /**
     * サイドナビ
     */
    navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
      @Override
      public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()){
          case R.id.delete_db:
            // 確認ダイアログの作成
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("警告");
            alertDialog.setMessage("データを全て削除します。\n本当によろしいですか？");

            // Yesボタン押下イベント
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                try {
                  db.execSQL("DELETE FROM used_tbl;");
                } catch (SQLException e) {
                  Log.e("ERROR", e.toString());
                }
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
              }
            });
            // Noボタン押下イベント
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
              }
            });
            alertDialog.create().show();
            break;
          default:
            break;
        }

        return false;
      }
    });
  }
  /**
   * AndroidのBackボタンを無効
   */
  @Override
  public void onBackPressed() {
  }
}


