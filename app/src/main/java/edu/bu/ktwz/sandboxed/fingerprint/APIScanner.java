package edu.bu.ktwz.sandboxed.fingerprint;

import android.content.Context;
import android.util.Log;

//import org.apache.log4j.Logger;
//import org.apache.log4j.spi.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Scan the Android API framework. Collect values from static variables and all possible method calls.
 *
 * Created by wil on 1/29/15.
 */

//TODO not everything has a constructor like Sensor.class, how to get instance of this?
public class APIScanner {

    public interface ScanProgressListener {
        public void onClassScanned(String className);
    }

    private final Hashtable<String,String> fingerprints = new Hashtable<String, String>();

    private ScanProgressListener listener;
    public static int NATIVE_COUNT = 0;

    private static final String TAG = APIScanner.class.getName();
  //  private final Logger log = Logger.getLogger(Discover.class);
    private Context context;

   // private Hashtable<String, String> api;

    public APIScanner(Context context){
        this(context, new Hashtable<String, String>());
    }
    public APIScanner(Context context, Hashtable<String, String> api) {
        this.context = context;
     //   this.api = api;
    }

    public void fullScan(List<String> classList, int offset, int count) {
        for (int i=offset; i<offset + count; i++){
            fullScan(classList.get(i));
        }
    }
    public void fullScan(List<String> classList){

        for (String className : classList) { //These are the full class names
            if (listener != null){
                listener.onClassScanned(className);
            }
            fullScan(className);
        }
    }

    public void setScanListener(ScanProgressListener listener){
        this.listener = listener;
    }
    /**
     * Scan and insert into database
     *
     * @param className
     */
    public void fullScan(String className){

        try {
            Class clazz = Class.forName(className);
            processClassMembers(clazz);
            processMethods(clazz, null,null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param clazz
     * @param instance Instance to use for method calls, if null, one is attempted
     */
    public void processMethods(Class clazz, Object instance, List<String> recurseStack){
       // if (!shouldAcceptClass(clazz)) return;

        if (recurseStack == null){
            recurseStack = new ArrayList<String>();
        }

        if (instance == null ) {
            instance = getInstance(clazz);
            if (instance == null) {
                //  Log.e(TAG, "Can not initialize constructor for class " + clazz.getName());
                return;
            }
        }

        Method[] methods = clazz.getDeclaredMethods();//getMethods(); //all public methods
        for (int i=0; i<methods.length; i++){
            methods[i].setAccessible(true);


            String methodName = methods[i].getName();

            if (recurseStack.contains(methodName)){
                continue;
            }

            if(clazz.getSimpleName().equals("MediaExtractor") && methodName.equals("getCachedDuration")){
                continue;
            }

            Class returnType = methods[i].getReturnType();
            Class [] parameterTypes = methods[i].getParameterTypes();
            String fullName = clazz.getName() + "." + methodName + "()";
//            Log.d(TAG, returnType.getSimpleName() + ":" + fullName);


            try {
                //Its a primitive type and no parameters
                if (parameterTypes.length == 0 && shouldAcceptMethod(methodName) && !hasVoidReturnType(returnType)) {


                    int modifiers = methods[i].getModifiers();
                    boolean isNative = Modifier.isNative(modifiers);

                    boolean set = false;

                    if (set && isNative) {
                        NATIVE_COUNT++;
//                        Log.d(TAG, "Skipping...native");
                        //  continue;
                    }

                    if (Modifier.isNative(methods[i].getModifiers())){
                        //
                        Log.d(TAG, "Native " + fullName);
                        NATIVE_COUNT++;
                        continue;
                    }

                    if (isSimpleReturnType(returnType)) {
                        Object o = methods[i].invoke(instance);

                        String value = (o == null) ? "null" : o.toString();

                        write(fullName, value);


//                         Log.d(TAG, fullName + "=" + value);
                        set = true;
                    } else if (returnType.isArray()){

                        Object[] o = (Object [] )methods[i].invoke(instance);
                        for (int x =0; x<o.length; x++){

                            if (isSimpleReturnType(o[x].getClass())) {

                                String value = (o[x] == null) ? "null" : o[x].toString();

                                write(fullName, value);
//                                Log.d(TAG, fullName + "=" + value);

                                set = true;
                            } else {
                                /*
                                Log.d(TAG, "RECURSIVE " + o[x].getClass().getName());
                                recurseStack.add(methodName);
                                processClassMembers( o[x].getClass());
                                processMethods( o[x].getClass(),  o[x], recurseStack);
                                */
                            }

                        }

                    } else if (returnType == List.class) {
                        Object o = methods[i].invoke(instance);
                        if (o != null) {
                            //recurse
                            for (Object lo : (List) o) {
                                if (isSimpleReturnType(lo.getClass())) {
                                    //primative, save
                                    //  Log.d(TAG, fullName + "=" + lo);

                                    String value = (o == null) ? "null" : o.toString();

                                   write(fullName, value);
                                    set = true;
//                                    Log.d(TAG, fullName + "=" + value);

                                } else {
                                    /*
                                    Log.d(TAG, "RECURSIVE " + lo.getClass().getName());
                                    recurseStack.add(methodName);
                                    processClassMembers(lo.getClass());
                                    processMethods(lo.getClass(), lo, recurseStack);
                                    */
                                }
                            }
                        }
                    } else if (shouldRecurse(clazz, returnType) &&
                            !methods[i].getName().contains("Instance")) { //Prevent loops
                        //An object
                        Object o = methods[i].invoke(instance);

                        if (o != null) {
//                            Log.d(TAG, "RECURSIVE " + o.getClass().getName());
                            recurseStack.add(methodName);
                            processClassMembers(o.getClass());
                            processMethods(o.getClass(), o, recurseStack);
                        }
                    }


                }
            } catch (IllegalAccessException e) {
                String message = (e.getMessage() == null)? "Error for " + clazz.getName() : e.getMessage();
//                Log.e(TAG, message);
            } catch (InvocationTargetException e) {
                String message = (e.getMessage() == null)? "Error for " + clazz.getName() : e.getMessage();
//                Log.e(TAG, message);
            } catch (RuntimeException e){
                String message = (e.getMessage() == null)? "Error for " + clazz.getName() : e.getMessage();
//                Log.e(TAG, message);
            }
        }
    }

    private static Object lock = new Object();

    private void write(String key, String value){
     //   APICall call = new APICall(key, value);
    //    call.save();
        synchronized(lock)
        {
            fingerprints.put(key, value);
        }
    }

    public Hashtable<String,String> getResults(){
        return fingerprints;
    }
    private boolean shouldRecurse(Class clazz, Class returnType){
        boolean should = true;
        Class[] interfaces = clazz.getInterfaces();
        for (int i=0; i<interfaces.length; i++){
            if (returnType == interfaces[i]){
                should = false;
                break;
            }
        }
        return  returnType != clazz && should;
    }

    private boolean shouldAcceptClass(Class clazz){
        return !clazz.getSimpleName().equals("MediaExtractor");
    }
    /**
     * Need to add this because some methods are very desctructive! lol
     * in particular
     * @param methodName
     * @return
     */
    private boolean shouldAcceptMethod(String methodName){
        return methodName.startsWith("get") || methodName.startsWith("is");
    }
    private Object getInstance(Class clazz)  {

        Constructor[] c = clazz.getDeclaredConstructors();
        Constructor constructor = null;
        Object newInstance = null;
        try {
            for (int i = 0; i < c.length; i++) {
                Class[] parameterTypes = c[i].getParameterTypes();

                if (parameterTypes.length == 1 && parameterTypes[0].getName().equals(Context.class.getName())) {
                    newInstance = c[i].newInstance(context);
                    break;
                } else if (parameterTypes.length == 0) { //Empty constructor, easy just use this
                    newInstance = c[i].newInstance();
                    break;
                } else {
                    //Try some tricky stuff, if context we can do that, else fuzz?
                }
            }

        } catch (InstantiationException e){
            String message = (e.getMessage() == null)? "Error for " + clazz.getName() : clazz.getName()  + ":" + e.getMessage();
            //Log.e(TAG, message);
        } catch (IllegalAccessException e){
            String message = (e.getMessage() == null)? "Error for " + clazz.getName() : clazz.getName()  + ":" + e.getMessage();
           //Log.e(TAG, message);
        } catch (InvocationTargetException e) {
            String message = (e.getMessage() == null) ? "Error for " + clazz.getName() : clazz.getName() + ":" + e.getMessage();
            //Log.e(TAG, message);
        } catch (IllegalStateException e){

        } catch (RuntimeException e){

        }

        return newInstance;
    }

    public void processClassMembers(Class clazz){
        Field[] fields = clazz.getDeclaredFields();

        for (int j=0; j<fields.length; j++){
            //TODO should we do anything about illegal accessed properties?
            fields[j].setAccessible(true);
            String fieldName = fields[j].getName();
            String fullName = clazz.getName() + "." + fieldName;
// Log.d(TAG, fullName);

            try {

                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                if (field == null) {
                    continue;
                }

                Object obj = field.get(null);

                if (isSimpleReturnType(obj.getClass())) {

                    write(fullName, obj.toString());

                } else {
                    //TODO its an object so recurse
                }


            } catch (RuntimeException e){
                String message = (e.getMessage() == null)? "Error for " + fullName : fullName + ":" + e.getMessage();
             //   Log.e(TAG,message);
            } catch (IllegalAccessException e) {
                String message = (e.getMessage() == null)? "Error for " + fullName : fullName + ":" + e.getMessage();
             //   Log.e(TAG, message );
            } catch (NoSuchFieldException e) {
                String message = (e.getMessage() == null)? "Error for " + fullName : fullName + ":" + e.getMessage();
            //    Log.e(TAG, message);
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
        ret.add(String.class);
        return ret;
    }

    private boolean hasVoidReturnType(Class c){
        return c.equals(void.class) || c.equals(Void.class);
    }

    private boolean isSimpleReturnType(Class c){
        if (c.isPrimitive() && !hasVoidReturnType(c)) {
            return true;
        } else if (c == Byte.class
                || c == Short.class
                || c == Integer.class
                || c == Long.class
                || c == Float.class
                || c == Double.class
                || c == Boolean.class
                || c == Character.class
                || c == String.class) {
            return true;
        } else {
            return false;
        }
    }
}
