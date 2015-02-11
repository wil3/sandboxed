package ktwz.sandboxed.fingerprint;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by wil on 2/9/15.
 */
public class ServiceScanner {

    public static final String SERVICE_SUFFIX = "_SERVICE";
    private Context context;
    APICallScanner scanner;
    public ServiceScanner(Context context){
        this.context = context;
    }

    public void scan(){
        scanner = new APICallScanner(context);

        Field[] fields = Context.class.getDeclaredFields();
        for (int j=0; j<fields.length; j++) {
            String fieldName = fields[j].getName();
            Log.d("ServiceScan", fieldName);
            if (fieldName.endsWith(SERVICE_SUFFIX)){
                try {
                    String value = (String)fields[j].get(null);
                    createService(value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private  void createService(String serviceName){
         try {
             Object service = context.getSystemService(serviceName);
             Class clazz = service.getClass();

             scanner.processClassMembers(clazz);
             scanner.processMethods(clazz, service, null);
         } catch(RuntimeException e){
             if (e.getMessage() != null) {
                 Log.d("Service", e.getMessage());
             }
         }

    }
}
