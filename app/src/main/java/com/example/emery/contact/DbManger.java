package com.example.emery.contact;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

/**
 * Created by emery on 2017/4/23.
 */

public enum DbManger {

    DB_CONTACTS("contacts.db");
    private String path;

    private DbManger(String path) {
        this.path = path;
    }

    public SQLiteDatabase getPath() {
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(Environment
                .getExternalStorageDirectory().getAbsolutePath() + DB_CONTACTS.getPath(), null);
        return sqLiteDatabase;
    }
}
