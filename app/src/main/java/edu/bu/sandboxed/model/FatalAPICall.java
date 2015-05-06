package edu.bu.sandboxed.model;

import android.database.Cursor;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * Defines a single API call.
 *
 * Additionally, uses ActiveAndroid to model a SQL entry
 *
 * Created by wil on 1/30/15.
 */
@Table(name="FatalAPICalls")
public class FatalAPICall extends Model{

    @Column(name="class")
    public String className;


    @Column(name="method", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE) // If there is a list or array, multiple values can be set for th4e same method
    public String methodName; //This is the full method name include class

    public FatalAPICall(){
        super();
    }
    public FatalAPICall(String className, String methodName) {
        super();
        this.className = className;
        this.methodName = methodName;

    }


    public static String getPackageName(String methodName){
        int lastDot = methodName.lastIndexOf(".");
        return methodName.substring(0, lastDot);

    }
    public static String getSimpleClassName(String methodName){
        int lastDot = methodName.lastIndexOf(".");
        return methodName.substring(lastDot + 1, methodName.length());
    }

    public static Cursor fetchResultCursor() {
        String tableName = Cache.getTableInfo(FatalAPICall.class).getTableName();
        // Query all items without any conditions
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id").
                from(FatalAPICall.class).orderBy("class, value ASC").toSql();
        // Execute query on the underlying ActiveAndroid SQLite database
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }
    public static Cursor fetchResultCursor(String filter) {
        String tableName = Cache.getTableInfo(FatalAPICall.class).getTableName();
        //TODO make sure activeandroid doesnt have support for like or create pull request if it doesnt exist
        // Execute query on the underlying ActiveAndroid SQLite database
        String query = "SELECT " + tableName + ".*, " + tableName + ".Id as _id  FROM " + tableName +
                " WHERE method LIKE '%" + filter + "%' OR class LIKE '%" + filter + "%' OR value LIKE '%" + filter + "%' ORDER BY class, value ASC";

        Cursor resultCursor = Cache.openDatabase().rawQuery(query, null);
        return resultCursor;
    }

    public static boolean isEmpty(){
        return new Select().from(FatalAPICall.class).limit(1).execute().isEmpty();
    }


    public static FatalAPICall getLastInserted(){
        List<FatalAPICall> apis = new Select().from(FatalAPICall.class).orderBy("Id DESC").limit(1).execute();

        if (apis.isEmpty()) {
            return null;
        } else {
            return apis.get(0);
        }
    }

    public static List<FatalAPICall> getByMethodName(String methodName){
        return  new Select().from(FatalAPICall.class).where("method = ?", methodName).execute();

    }
    public static List<FatalAPICall> getAll(){
        return  new Select().from(FatalAPICall.class).execute();

    }


}
