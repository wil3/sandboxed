package edu.bu.ktwz.sandboxed.detect;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Perform REGEX on var of static variables
 *
 * Created by wil on 1/25/15.
 */
@Deprecated
public class StaticVariableRegExDetection implements SandboxDetection {
    private static final String TAG = StaticVariableRegExDetection.class.getName();

    private String var;
    private String regex;
    private String value;

    public StaticVariableRegExDetection(String var, String regex){
        this.var = var;
        this.regex = regex;

        try {
            this.value = getFieldValue(var).toString();
            String s;
        } catch (IllegalAccessException e){
            Log.e(TAG, "Illegal access");
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Field not found");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class not found.");
        }
    }
    @Override
    public boolean inSandbox() {
        boolean inSandbox = false;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(value);
        inSandbox = m.matches();
        return inSandbox;
    }
    @Override
    public String getFoundString(){
        return value;
    }



    Object getFieldValue(String path) throws  ClassNotFoundException, NoSuchFieldException, IllegalAccessException{
        int lastDot = path.lastIndexOf(".");
        String className = path.substring(0, lastDot);
        String fieldName = path.substring(lastDot + 1);
        Class myClass = Class.forName(className);
        Field myField = myClass.getDeclaredField(fieldName);
        return myField.get(null);
    }


}
