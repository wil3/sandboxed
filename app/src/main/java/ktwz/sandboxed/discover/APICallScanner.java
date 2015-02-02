package ktwz.sandboxed.discover;

import android.content.Context;
import android.util.Log;

//import org.apache.log4j.Logger;
//import org.apache.log4j.spi.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ktwz.sandboxed.model.APICall;

/**
 * Scan the Android API framework. Collect values from static variables and all possible method calls.
 *
 * Created by wil on 1/29/15.
 */

//TODO not everything has a constructor like Sensor.class, how to get instance of this?
public class APICallScanner {
    private static final String TAG = APICallScanner.class.getName();
  //  private final Logger log = Logger.getLogger(Discover.class);
    private Context context;

    private List<String> classList;
    public APICallScanner(Context context, List<String> classList){
        this.context = context;
        this.classList = classList;
    }

    public void scan(){

        for (String className : classList) { //These are the full class names
       // for (int i=0; i<5; i++){
       //     String className = classList.get(i);

            try {
                Class clazz = Class.forName(className);

                processClassMembers(clazz, className);
              //  processMethods(clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private void processMethods(Class clazz){
       // Constructor constructor = getInstance(clazz);
        Object instance = getInstance(clazz);
        if (instance == null) {
            Log.e(TAG, "Can not initialize constructor for class " + clazz.getName());
            return;
        }

        Method[] methods = clazz.getMethods(); //all public methods
        for (int i=0; i<methods.length; i++){
            String methodName = methods[i].getName();
            Class returnType = methods[i].getReturnType();
            Class [] parameterTypes = methods[i].getParameterTypes();

            try {
                //Its a primitive type and no parameters
                if (isWrapperType(returnType) && parameterTypes.length == 0){
                    Object o = methods[i].invoke(instance);
                    String value = o.toString();

                } else {
                    //recurse
                }
            } catch (IllegalAccessException e) {
                Log.e(TAG, e.getMessage());
            } catch (InvocationTargetException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private Object getInstance(Class clazz)  {

        Constructor[] c = clazz.getDeclaredConstructors();
        Constructor constructor = null;
        Object newInstance = null;
        try {
            for (int i = 0; i < c.length; i++) {
                Class[] parameterTypes = c[i].getParameterTypes();

                if (parameterTypes.length == 0) { //Empty constructor, easy just use this
                    constructor = c[i];

                    newInstance = c[i].newInstance();
                    break;
                } else {
                    //Try some tricky stuff, if context we can do that, else fuzz?
                }
            }

        } catch (InstantiationException e){
            Log.e(TAG, e.getMessage());
        } catch (IllegalAccessException e){
            Log.e(TAG, e.getMessage());
        } catch (InvocationTargetException e){
            Log.e(TAG, e.getMessage());
        }
        return newInstance;
    }

    private void processClassMembers(Class clazz, String className){
        Field[] fields = clazz.getDeclaredFields();

        //String classNameLog = className + "\n";
        //  fis.write(classNameLog.getBytes());

        // String fields = "\t" + Arrays.toString(fieldsToStringArray(f)) + "\n";
        // fis.write(fields.getBytes());
        //  fis.write("\t" + f.toString());


        for (int j=0; j<fields.length; j++){
            //TODO should we do anything about illegal accessed properties?
            String fieldName = fields[j].getName();
            String fullName = className + "." + fieldName;

            try {

                Field field = clazz.getDeclaredField(fieldName);
                if (field == null) {
                    continue;
                }

                Object obj = field.get(null);

                if (isWrapperType(obj.getClass())) {

                    String l = className + "." + fieldName + "=" + obj.toString() + "\n";
                    // fis.write(l.getBytes());

                    APICall call = new APICall(fullName,obj.toString());
                    call.save();


                } else {
                    //TODO its an object so recurse
                }


            } catch (RuntimeException e){
                String message = (e.getMessage() == null)? "Error for " + fullName : e.getMessage();
                Log.e(TAG,message);
            } catch (IllegalAccessException e) {
                Log.e(TAG, fullName + ":" + e.getMessage() );
            } catch (NoSuchFieldException e) {
                Log.e(TAG, fullName + ":" + e.getMessage());
            }
        }

    }

    private String [] fieldsToStringArray(Field [] fields){
        String [] s = new String [fields.length];
        for (int i=0; i<s.length; i++){
            s [i] = fields[i].getName();
        }
        return s;
    }


    private Constructor findAvailableConstructor(Constructor[] constructors){
        Constructor constructor = null;
        for (int i=0; i<constructors.length; i++){
            Class[] parameterTypes = constructors[i].getParameterTypes();
        }
        return constructor;
    }


    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    public static boolean isWrapperType(Class<?> clazz)
    {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypes()
    {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }
}
