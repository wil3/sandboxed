package edu.bu.sandboxed.model;

import android.database.Cursor;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.Cache;

import java.util.List;

/**
 * Defines a single API call.
 *
 * Additionally, uses ActiveAndroid to model a SQL entry
 *
 * Created by wil on 1/30/15.
 */
@Table(name="APICalls")
public class APICall extends Model{

    public static final int STATUS_CRASH = 0;
    public static final int STATUS_OK = 1;
    public static final int STATUS_UKNOWN = 2;

    //TODO need unique column, hash of key,value so we dont get dups...or see if there is a constraight for both
    //@Column(name="package")
   // public String packageName;

    //The moment it was access
    @Column(name="access")
    public int accessIndex;


    //This is the tag for the method/value pair
    @Column(name="tag", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String tag;

    @Column(name="class")
    public String className;


    @Column(name="method") // If there is a list or array, multiple values can be set for th4e same method
    public String methodName; //This is the full method name include class

    //@Column(name="access")
    //public String accessName; //could get field or method

    @Column(name="value")
    public String value;

    @Column(name="status" )
    public int status = STATUS_UKNOWN;

    public APICall(){
        super();
    }
    public APICall(String className, String methodName, String value) {
        super();
       // this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;
       // this.accessName = accessName;
        this.value = value;

        this.tag = methodName + value;
    }

    public void setValue(String value){
        this.value = value;
        this.tag = methodName + value;
    }


    public static String getClassName(String methodName){
        int lastDot = methodName.lastIndexOf(".");
        return methodName.substring(0, lastDot);

    }

    public static String getPackageName(String methodName){
        String className = getClassName(methodName);
        int lastDot = className.lastIndexOf(".");
        return className.substring(0, lastDot);
    }

    /**
     * Get just the invoked name, not the full path
     * @param methodName
     * @return
     */
    public static String getSimpleInvokedName(String methodName){
        int lastDot = methodName.lastIndexOf(".");
        return methodName.substring(lastDot + 1, methodName.length());
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
        String query = "SELECT " + tableName + ".*, " + tableName + ".Id as _id  FROM " + tableName +
                " WHERE method LIKE '%" + filter + "%' OR class LIKE '%" + filter + "%' OR value LIKE '%" + filter + "%' ORDER BY class, value ASC";

        Cursor resultCursor = Cache.openDatabase().rawQuery(query, null);
        return resultCursor;
    }

    public static boolean isEmpty(){
        return new Select().from(APICall.class).limit(1).execute().isEmpty();
    }


    public static APICall getLastInserted(){
        List<APICall> apis = new Select().from(APICall.class).orderBy("Id DESC").limit(1).execute();

        if (apis.isEmpty()) {
            return null;
        } else {
            return apis.get(0);
        }
    }

    public static List<APICall> getByMethodName(String methodName){
        return  new Select().from(APICall.class).where("method = ?", methodName).execute();

    }

    public static List<APICall> getCrashableCalls(){
        return new Select().from(APICall.class).where("status = ?", STATUS_CRASH).execute();
    }



}
