package ktwz.sandboxed.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wil on 1/30/15.
 */
public class DbHelper extends SQLiteOpenHelper {



    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sandbox.db";


    private static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
    private static final String COLUMNS_START = " (";
    private static final String COLUMNS_END = " )";
    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_REAL = " REAL";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String CONSTRAINT_PRIMARY_KEY = " INTEGER PRIMARY KEY";
    private static final String CONSTRAINT_UNIQUE = " UNIQUE";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_API_CALLS =
            CREATE_TABLE + Contract.APICallEntry.TABLE_NAME + COLUMNS_START +
                    Contract.APICallEntry._ID + CONSTRAINT_PRIMARY_KEY + COMMA_SEP +
                    Contract.APICallEntry.COLUMN_NAME_PACKAGE + TYPE_TEXT + COMMA_SEP +
                    Contract.APICallEntry.COLUMN_NAME_CLASS + TYPE_TEXT + COMMA_SEP +
                    Contract.APICallEntry.COLUMN_NAME_ACCESS + TYPE_TEXT +

                    COLUMNS_END;

    private static final String SQL_DROP_WIDGET =
            DROP_TABLE + Contract.APICallEntry.TABLE_NAME;


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_API_CALLS);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        onCreate(db);
    }
    private void dropTables(SQLiteDatabase db){
        db.execSQL(SQL_DROP_WIDGET);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
