package edu.bu.ktwz.sandboxed.fingerprint.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.bu.ktwz.sandboxed.R;
import edu.bu.ktwz.sandboxed.SandboxedApplication;
import edu.bu.ktwz.sandboxed.fingerprint.APICallScanner;
import edu.bu.ktwz.sandboxed.fingerprint.AndroidFrameworkFileIO;
import edu.bu.ktwz.sandboxed.fingerprint.ServiceScanner;
import edu.bu.ktwz.sandboxed.net.C2;

/**
 * Build the database in a separate thread
 * Created by wil on 2/1/15.
 */

//TODO may want this in a service if it takes to long
public class SerializedFingerprintTask extends AsyncTask<List<String> , Void, Void> {
    private static final String TAG = SerializedFingerprintTask.class.getName();

    private final WeakReference<Context> contextReference;
    private ServiceScannerTask.ScannerTaskCallback callback;
    int count;
    String id;
   // List<String> classes;
    public SerializedFingerprintTask(Context context, ServiceScannerTask.ScannerTaskCallback callback){
        super();
      //  this.classes = classes;
        this.count = count;
        this.callback = callback;
        this.id = id;
        contextReference = new WeakReference<Context>(context);
    }

    @Override
    protected Void doInBackground(List<String> ... params) {

        List<String> classes = params[0];

        Hashtable<String, String> fingerprints = new Hashtable<String, String>();

        Looper.prepare(); //Prevent RTE cant create handler without calling looper.prepare

        long start = System.currentTimeMillis();


        APICallScanner generalAPIScan = new APICallScanner(contextReference.get());
        generalAPIScan.fullScan(classes);
        fingerprints.putAll(generalAPIScan.getResults());


        long endTimeGenericScan =  System.currentTimeMillis() ;

        Log.d(TAG, "+ " + getFormatedLapseTime(endTimeGenericScan- start) + " generic fingerprint");


        Hashtable<String, String> services = new ServiceScanner(contextReference.get()).scan();
        fingerprints.putAll(services);


        long endTimeServiceFingerprint = System.currentTimeMillis() ;

        Log.d(TAG, "+ " + getFormatedLapseTime(endTimeServiceFingerprint- endTimeGenericScan) + " service fingerprint");


        //Export to file so it can be zipped and sent
        AndroidFrameworkFileIO io = new AndroidFrameworkFileIO(contextReference.get());

        String flavor = contextReference.get().getResources().getString(R.string.flavor);

        //Has a command and control
        if (flavor.equals(SandboxedApplication.FLAVOR_REMOTE)) {

            io.exportHashToFile(fingerprints);
            long endTimeFileExport = System.currentTimeMillis();

            Log.d(TAG, "+ " + getFormatedLapseTime(endTimeFileExport - endTimeServiceFingerprint) + " file export");

            //Send to C2
            C2 c2 = new C2(contextReference.get());
            c2.uploadFingerprint();

        } else if (flavor.equals(SandboxedApplication.FLAVOR_EMULATOR)){
            io.exportHashToFile(fingerprints);
            long endTimeFileExport = System.currentTimeMillis();
            Log.d(TAG, "+ " + getFormatedLapseTime(endTimeFileExport - endTimeServiceFingerprint) + " file export");

        } else {
            Log.d(TAG, "Loading " + fingerprints.size() + " into database");
            //Load memory into database
            io.loadHashtableIntoDatabase(fingerprints);

            long endTimeDatabaseLoad = System.currentTimeMillis() ;
            Log.d(TAG, "+ " + getFormatedLapseTime(endTimeDatabaseLoad- endTimeServiceFingerprint) + " database load");

        }

        long lapse = System.currentTimeMillis() - start;
        Log.d(TAG, "Total time lapsed " + getFormatedLapseTime(lapse));

        return null;
    }

    private String getFormatedLapseTime(long millis){
        String lapse = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
        return lapse;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (callback != null) {
            callback.onScannerSuccess(id);
        }
    }
/*
    public interface FingerprintTaskCallback {
        public void onFingerprintSuccess();
        public void onDatabaseBuildFailure();

    }
    */
}
