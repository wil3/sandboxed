package ktwz.sandboxed.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import ktwz.sandboxed.model.APICall;

/**
 * Created by wil on 1/30/15.
 */
public class DataSource {

    private SQLiteDatabase db;
    private DbHelper dbHelper;


    public DataSource(Context context){
        this.dbHelper = new DbHelper(context);
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }


    public void addAPICall(APICall apiCall){
        ContentValues values = new ContentValues();
        values.put(Contract.APICallEntry.COLUMN_NAME_PACKAGE, apiCall.packageName);
        values.put(Contract.APICallEntry.COLUMN_NAME_CLASS, apiCall.accessName);
        values.put(Contract.APICallEntry.COLUMN_NAME_ACCESS, apiCall.accessName);

        db.insert(Contract.APICallEntry.TABLE_NAME, null, values);

    }

}
