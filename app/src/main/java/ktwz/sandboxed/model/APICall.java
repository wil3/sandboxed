package ktwz.sandboxed.model;

import android.database.Cursor;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.Cache;
import com.activeandroid.util.SQLiteUtils;

/**
 * Defines a single API call.
 *
 * Additionally, uses ActiveAndroid to model a SQL entry
 *
 * Created by wil on 1/30/15.
 */
@Table(name="APICalls")
public class APICall extends Model{

    //@Column(name="package")
   // public String packageName;

    @Column(name="class")
    public String className;

    //@Column(name="access")
    //public String accessName; //could get field or method

    @Column(name="value")
    public String val;

    public APICall(){
        super();
    }
    public APICall(String className, String val) {
        super();
       // this.packageName = packageName;
        this.className = className;
       // this.accessName = accessName;
        this.val = val;
    }


    public static String getPackageName(String className){
        int lastDot = className.lastIndexOf(".");
        return className.substring(0, lastDot);

    }
    public static String getSimpleClassName(String className){
        int lastDot = className.lastIndexOf(".");
        return className.substring(lastDot + 1, className.length());
    }

    public static Cursor fetchResultCursor() {
        String tableName = Cache.getTableInfo(APICall.class).getTableName();
        // Query all items without any conditions
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id").
                from(APICall.class).orderBy("class, value ASC").toSql();
        // Execute query on the underlying ActiveAndroid SQLite database
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }
    public static Cursor fetchResultCursor(String filter) {
        String tableName = Cache.getTableInfo(APICall.class).getTableName();
        //TODO make sure activeandroid doesnt have support for like or create pull request if it doesnt exist
        // Execute query on the underlying ActiveAndroid SQLite database
        String query = "SELECT " + tableName + ".*, " + tableName + ".Id as _id  FROM " + tableName + " WHERE class LIKE '%" + filter + "%' OR value LIKE '%" + filter + "%' ORDER BY class, value ASC";

        Cursor resultCursor = Cache.openDatabase().rawQuery(query, null);
        return resultCursor;
    }

    public static boolean isEmpty(){
        return new Select().from(APICall.class).limit(1).execute().isEmpty();
    }



}
