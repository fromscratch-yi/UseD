package com.example.yufroms.used;

public class CommonSub {
  // 月を文字列で表示
  public String getMonthName(int month) {
    String[] month_name = {
        "Jan", "Feb", "Mar", "Apr",
        "May", "Jun", "Jul", "Aug",
        "Sep", "Oct", "Nov", "Dec"
    };
    String res;
    if ( (0 < month) && (month < 13) ) {
      res = month_name[month - 1];
    } else {
      res = String.valueOf(month);
    }
    return res;
  }
}