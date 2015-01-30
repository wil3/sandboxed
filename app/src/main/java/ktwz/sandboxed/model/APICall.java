package ktwz.sandboxed.model;

import android.database.Cursor;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.Cache;

/**
 * Created by wil on 1/30/15.
 */
@Table(name="APICalls")
public class APICall extends Model{

    @Column(name="package")
    public String packageName;

    @Column(name="class")
    public String className;

    @Column(name="access")
    public String accessName; //could get field or method

    @Column(name="val")
    public String val;

    public APICall(){
        super();
    }
    public APICall(String packageName, String className, String accessName, String val) {
        super();
        this.packageName = packageName;
        this.className = className;
        this.accessName = accessName;
        this.val = val;
    }

    // Return cursor for result set for all todo items
    public static Cursor fetchResultCursor() {
        String tableName = Cache.getTableInfo(APICall.class).getTableName();
        // Query all items without any conditions
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id").
                from(APICall.class).toSql();
        // Execute query on the underlying ActiveAndroid SQLite database
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }

    public static boolean isEmpty(){
        return new Select().from(APICall.class).limit(1).execute().isEmpty();
    }


}
