package com.example.yufroms.used;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class MonHistoryActivity extends AppCompatActivity {
  private CommonSub sub;
  private int year, month, year_tmp, month_tmp, week, maxDays, pushedId, intentId,
      currentYear, currentMonth, currentDay, textViewIdVal,cnt, todayFlag;
  private ArrayList<Integer> id = new ArrayList<Integer>();
  private ArrayList<Integer> amount = new ArrayList<Integer>();
  private ArrayList<Integer> kind = new ArrayList<Integer>();
  private ArrayList<String> remarks = new ArrayList<String>();
  private boolean currentFlag = false;
  private String month_name, dateStr, kindStr, remarksStr, textViewId;
  private SQLiteDatabase db;
  private int[] DayinMonth = {31,28,31,30,31,30,31,31,30,31,30,31};
  private TextView[] textDayArray;
  private ImageButton nextMon;
  private TextView comment, spendingList, dateDisp, tmpText;
  private LinearLayout commentLayout,spendingLayout, scrollLayout;
  private Cursor cursor = null;
  private UsedDbOpenHelper helper;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mon_history);

    helper = new UsedDbOpenHelper(this);
    final ImageButton backBtn = (ImageButton) findViewById(R.id.back_btn);
    final ImageButton backMon = (ImageButton) findViewById(R.id.left_arrow);
    final Calendar calendar = Calendar.getInstance();
    nextMon = (ImageButton) findViewById(R.id.right_arrow);
    commentLayout = (LinearLayout) findViewById(R.id.commentLayout);
    comment = (TextView) findViewById(R.id.comment);
    spendingLayout = (LinearLayout) findViewById(R.id.spending_layout);
    spendingList = (TextView) findViewById(R.id.spending_list);
    scrollLayout = (LinearLayout) findViewById(R.id.scroll_area);

    textDayArray = new TextView[42];
    for ( cnt = 0; cnt < 42; cnt++) {
      textViewId = "textView" + String.valueOf(cnt + 8);
      textViewIdVal = getResources().getIdentifier(textViewId, "id", "com.example.yufroms.used");
      textDayArray[cnt] = (TextView) findViewById(textViewIdVal);
    }

    Intent intent = getIntent();
    year = intent.getIntExtra("YEAR", 0);
    month = intent.getIntExtra("MONTH", 0);
    intentId = intent.getIntExtra("PUSHED", 0);
    currentYear = calendar.get(Calendar.YEAR);
    currentMonth = calendar.get(Calendar.MONTH) + 1;
    currentDay = calendar.get(Calendar.DATE);

    this.refleshCalendar();
    if (intentId > 0) {
      tmpText = (TextView) findViewById(intentId);
      pushedId = intentId;
      showSpendingList();
    }
    for ( cnt = 0; cnt < 42; cnt++) {
      textDayArray[cnt].setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v) {
          if (tmpText != null) {
            tmpText.setBackgroundResource(R.drawable.edit_line);
            if (year == currentYear && month == currentMonth) {
              textDayArray[todayFlag].setBackgroundResource(R.drawable.editline_today);
            }
          }
          tmpText = (TextView) findViewById(v.getId());
          pushedId = v.getId();
          showSpendingList();
        }
      });
    }

    /**
     * 戻るボタンクリック時のイベント
     */
    backBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MonHistoryActivity.this, MainActivity.class);
        startActivity(intent);
      }
    });

    /**
     * 右矢印ボタン押下(次の月)
     */
    nextMon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!currentFlag) {
          if (month == 12) {
            year++;
            month = 1;
          } else {
            month++;
          }
          refleshCalendar();
          commentLayout.setVisibility(View.VISIBLE);
          spendingLayout.setVisibility(View.GONE);
          if (tmpText != null) {
            tmpText.setBackgroundResource(R.drawable.edit_line);
          }
          comment.setText("※Tap the date on the calendar.");
        }
      }
    });
    /**
     * 左矢印ボタン押下(前の月)
     */
    backMon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (month == 1) {
          year--;
          month = 12;
        } else {
          month--;
        }
        refleshCalendar();
        commentLayout.setVisibility(View.VISIBLE);
        spendingLayout.setVisibility(View.GONE);
        if (tmpText != null) {
          tmpText.setBackgroundResource(R.drawable.edit_line);
        }
        comment.setText("※Tap the date on the calendar.");
      }
    });
  }
  public void refleshCalendar() {
    if (year == currentYear && month == currentMonth) {
      nextMon.setVisibility(View.INVISIBLE);
      currentFlag = true;
    } else {
      nextMon.setVisibility(View.VISIBLE);
      currentFlag = false;
    }

    sub = new CommonSub();
    // 月を文字列に変換
    month_name = sub.getMonthName(month);

    // 現在月を表示
    dateDisp = (TextView) findViewById(R.id.date);
    dateStr = String.valueOf(year) + "." + month_name;
    dateDisp.setText(dateStr);

    // 曜日を計算
    if (month == 1 || month == 2) {
      year_tmp = year - 1;
      month_tmp = month + 12;
    } else {
      year_tmp = year;
      month_tmp = month;
    }
    week = (((((year_tmp + (year_tmp / 4)) - (year_tmp / 100)) +
        (year_tmp / 400)) + ((month_tmp * 13 + 8) / 5)) + 1) % 7;

    maxDays = DayinMonth[month - 1];

    for (int i = 0; i < 41; i++) {
      textDayArray[i].setText("");
    }
    for (int i = 0; i < maxDays; i++) {
      textDayArray[week + i].setText(Integer.toString(i + 1));
      if (year == currentYear && month == currentMonth && (i + 1) == currentDay) {
        textDayArray[week + i].setBackgroundResource(R.drawable.editline_today);
        todayFlag = week + i;
      } else {
        textDayArray[week + i].setBackgroundResource(R.drawable.edit_line);
      }
    }
  }
  public void showSpendingList() {
    if (tmpText.getText().length() == 0) {
      commentLayout.setVisibility(View.VISIBLE);
      spendingLayout.setVisibility(View.GONE);
      comment.setText("※Tap the date on the calendar.");
    } else {
      tmpText.setBackgroundResource(R.drawable.editline_pushued);
    }
    String pushedDay = tmpText.getText().toString();
    int dataCnt = 0;
    id.clear();
    amount.clear();
    kind.clear();
    remarks.clear();
    scrollLayout.removeAllViews();
    if (pushedDay.length() != 0) {
      try {
        // DB接続
        db = helper.getWritableDatabase();
        cursor = db.rawQuery("SELECT id, amount, kind, remarks FROM used_tbl " +
            "WHERE year = ? AND month = ? AND day = ?", new String[]{String.valueOf(year), String.valueOf(month), pushedDay});
        boolean isEof = cursor.moveToFirst();
        while (isEof) {
          dataCnt++;
          id.add(cursor.getInt(0));
          amount.add(cursor.getInt(1));
          kind.add(cursor.getInt(2));
          remarks.add(cursor.getString(3));
          isEof = cursor.moveToNext();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
      if (dataCnt == 0) {
        commentLayout.setVisibility(View.VISIBLE);
        spendingLayout.setVisibility(View.GONE);
        comment.setText("※" + year + "." + month + "." + pushedDay + " is not registered.");
      } else {
        commentLayout.setVisibility(View.GONE);
        spendingLayout.setVisibility(View.VISIBLE);
        spendingList.setText("Spending List (" + year + "." + month + "." + pushedDay + ")");
        for (int cnt = 0; cnt < dataCnt; cnt++) {
          View view = getLayoutInflater().inflate(R.layout.spending_list, null);
          scrollLayout.addView(view);
          TextView spend_data = (TextView) view.findViewById(R.id.spend_data);
          TextView kind_data = (TextView) view.findViewById(R.id.kind_data);
          TextView remarks_data = (TextView) view.findViewById(R.id.remarks_data);
          TextView edit_btn = (TextView) view.findViewById(R.id.edit_btn);
          switch (kind.get(cnt)){
            case 0:
              kindStr = "Spend";
              break;
            case 1:
              kindStr = "Waste";
              break;
            case 2:
              kindStr = "Investment";
              break;
            default:
              break;
          }
          remarksStr = remarks.get(cnt);
          if (remarksStr == null) {
            remarksStr = "-";
          } else if (remarksStr.length() > 4) {
            remarksStr = remarksStr.substring(0, 4) + "...";
          }
          spend_data.setText("¥ " + String.format("%,d", amount.get(cnt)) + " -");
          kind_data.setText(kindStr);
          remarks_data.setText(remarksStr);
          edit_btn.setId(id.get(cnt));
          edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent intent = new Intent(MonHistoryActivity.this, EditActivity.class);
              intent.putExtra("ID", v.getId());
              intent.putExtra("YEAR", year);
              intent.putExtra("MONTH", month);
              intent.putExtra("PUSHED", pushedId);
              startActivity(intent);
              overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
            }
          });
        }
      }
    }
  }
  /**
   * AndroidのBackボタンを無効
   */
  @Override
  public void onBackPressed() {
  }
}
