package edu.bu.sandboxed.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.bu.sandboxed.R;
import edu.bu.sandboxed.fingerprint.APIScanner;
import edu.bu.sandboxed.model.APICall;
import edu.bu.sandboxed.model.CachedAPICall;
import edu.bu.sandboxed.model.FatalAPICall;

/**
 * Perform a full scan locally
 *
 * Created by wil on 2/16/15.
 */
public class APIFingerprintRequest extends SpiceRequest<Hashtable> {
    private final WeakReference<Context> contextReference;
    private List<String> classes;
    private final String TAG = APIFingerprintRequest.class.getName();
    private SharedPreferences sharedPreferences;
    private boolean shouldStep = false;

    public APIFingerprintRequest(Context context, List<String> classes){
        super(Hashtable.class);
        this.contextReference = new WeakReference<Context>(context);
        this.classes = classes;
        this.sharedPreferences = this.contextReference.get().getSharedPreferences(contextReference.get().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    /**
     * Add the method call that caused a crash to the list of excludes
     * @param name
     */
    private void addToExcludes(String name){

    }

    private CachedAPICall getLastSuccess(){
        CachedAPICall last = CachedAPICall.getLastInserted();
        if (last != null){
            if (last.status == APICall.STATUS_UKNOWN) {
                last.status = APICall.STATUS_CRASH;
                last.save();
                return last;
            }
        }
        return null;
    }

    private List<String> getExcludeList2(){
        List<CachedAPICall> apiCalls = CachedAPICall.getCrashableCalls();
        List<String> methods = new ArrayList<String>();
        for (CachedAPICall apiCall : apiCalls) {
            methods.add(apiCall.methodName);
        }
        return methods;
    }
    private List<String> getExcludeList(){
        List<String> methods = new ArrayList<String>();

        List<FatalAPICall> apiCalls = FatalAPICall.getAll();
        if (apiCalls != null) {
            for (FatalAPICall apiCall : apiCalls) {
                methods.add(apiCall.methodName);
            }
        }
        for (String s : methods) {
            Log.d(TAG, "Exclude " +s);
        }
        return methods;
    }



    //TODO Need to make this so we can more easily recover from crash
    @Override
    public Hashtable<String, String> loadDataFromNetwork() throws Exception {

        final String key  = contextReference.get().getString(R.string.last_classname);


        final String cachedClassName = sharedPreferences.getString(key, null);
        //if (sharedPreferences != null){
        //if (lastSuccess != null){
        if (cachedClassName != null){
            Log.w(TAG, "Found checkpoint " + cachedClassName + ". Restoring to checkpoint and moving cautiously...");
            int indexOfLastSuccess = classes.indexOf(cachedClassName);
            //classes = classes.subList(indexOfLastSuccess, classes.size()-1);

            //TODO Fix this back because publish listener doesnt even look at this valud
            //for (int i=0; i<indexOfLastSuccess; i++) {
            //    publishProgress();
            //}

            //Look at the cache, can we identity the point of crash, add to the list of
            //methods to exclude then reset state
            if (!CachedAPICall.isEmpty()){
                //CachedAPICall lastSuccess = getLastSuccess();
                CachedAPICall failedCall = CachedAPICall.getLastInserted();
                Log.d(TAG, "Found crashed method " + failedCall.methodName);
                FatalAPICall fatalAPICall = new FatalAPICall(failedCall.className, failedCall.methodName);
                fatalAPICall.save();
                CachedAPICall.clear();


                //shouldStep = false;
            } else {
                Log.d(TAG, "Cache is empty");
            }
        }


        final SharedPreferences.Editor editor = sharedPreferences.edit();

        APIScanner generalAPIScan = new APIScanner(contextReference.get());

        generalAPIScan.setExcludes(getExcludeList());
        generalAPIScan.setScanListener(new APIScanner.ScanProgressListener() {
            @Override
            public void onBeforeClassScanned(String className) {
                //TODO only commit if already seen the last checkpoint
                editor.putString(key, className);
                editor.commit();
                if (className.equals(cachedClassName) && CachedAPICall.isEmpty()) {
                    shouldStep = true;
                }
            }
            @Override
            public void onAfterClassScanned(String className) {
                publishProgress();


                if (className.equals(cachedClassName)) { //The first one will be the one we previously cached
                    shouldStep = false;
                    //Clear the cache so we can start over
                    CachedAPICall.clear();
                    Log.d(TAG, "Clearing the cache");
                }
            }
            @Override
            public void onBeforeMethodInvoked(String className, String method) {
                //Save to db
                //editor.putString(key, name);
                // editor.commit();
                Log.d(TAG, "INVOKE " + method);
                if (shouldStep) {
                    CachedAPICall api = new CachedAPICall(className, method, null);
                    api.save();
                    Log.d(TAG, "Caching record " + method);
                }
            }

            @Override
            public void onAfterMethodInvoked(String className, String name) {
                //Update as ok
               /* APICall api = APICall.getByMethodName(name);
                if (api != null) {
                    api.status = APICall.STATUS_OK;
                    api.save();
                } else {
                    Log.d(TAG, "Could not find record for " + name);
                }*/
            }


            @Override
            public void onValueReturned(String className, String name, String value) {
                /*
                //If on device just skip and update key saved before with this value
                List<APICall> apis = APICall.getByMethodName(name);
                if (apis.size() == 1 && apis.get(0).status == APICall.STATUS_UKNOWN) {
                    apis.get(0).setValue(value);
                    apis.get(0).status = APICall.STATUS_OK;
                    apis.get(0).save();
                    Log.d(TAG, "Setting value " + value + " for " + name);
                } else  { //Create and add since we can have multiple
                    APICall newapi = new APICall(className, name, value);
                    newapi.status = APICall.STATUS_OK;
                    newapi.save();

                    //Log.d(TAG, "Could not find " + name + " to set value");
                }
                */
            }
        });
        generalAPIScan.scanClasses(classes);
        editor.remove(key); //remove everythin was ok

        for (String s : getExcludeList()){
            Log.d(TAG, "Crash on " + s);
        }

        return generalAPIScan.getResults();
    }

}
