package ktwz.sandboxed.scanners;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by wil on 2/9/15.
 */
public class ServiceScanner {

    public static final String SERVICE_SUFFIX = "_SERVICE";
    private Context context;
    public ServiceScanner(Context context){
        this.context = context;
    }
    public void scan(){

        Field[] fields = Context.class.getDeclaredFields();
        for (int j=0; j<fields.length; j++) {
            String fieldName = fields[j].getName();
            if (fieldName.endsWith(SERVICE_SUFFIX)){
                createService(fieldName);
            }
        }
    }

    private  void createService(String serviceName){
         try {
             Object service = context.getSystemService(serviceName);
             Class clazz = service.getClass();
         } catch(RuntimeException e){
             if (e.getMessage() != null) {
                 Log.d("Service", e.getMessage());
             }
         }

    }
}
