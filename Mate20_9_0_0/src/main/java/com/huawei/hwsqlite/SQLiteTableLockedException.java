package com.huawei.hwsqlite;

public class SQLiteTableLockedException extends SQLiteException {
    public SQLiteTableLockedException(String error) {
        super(error);
    }
}
