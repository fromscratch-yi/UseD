package com.example.yufroms.used;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yufroms on 2017/06/08.
 */

public class UsedDbOpenHelper extends SQLiteOpenHelper {
  public UsedDbOpenHelper(Context context) {
    super(context, "UsedDB", null, 1);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(
        "CREATE TABLE used_tbl(" +
            "id INTEGER PRIMARY KEY, " +  // id(主キー)
            "year INTEGER, " +              // 年
            "month INTEGER, " +             // 月
            "day INTEGER, " +               // 日
            "amount INTEGER, " +          // 金額
            "kind INTEGER," +             // 種類(// 0:消費, 1:浪費, 2:投資)
            "remarks TEXT" +              // 備考
            ");");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }
}
