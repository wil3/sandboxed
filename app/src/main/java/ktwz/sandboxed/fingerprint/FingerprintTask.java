package ktwz.sandboxed.fingerprint;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Build the database in a separate thread
 * Created by wil on 2/1/15.
 */

//TODO may want this in a service if it takes to long
public class FingerprintTask extends AsyncTask<List<String> , Void, Void> {
    private static final String TAG = FingerprintTask.class.getName();

    private final WeakReference<Context> contextReference;
    private ServiceScannerTask.ScannerTaskCallback callback;
    int count;
    String id;
   // List<String> classes;
    public FingerprintTask(Context context,  ServiceScannerTask.ScannerTaskCallback callback, String id){
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
 //       Looper.prepare(); //Prevent RTE cant create handler without calling looper.prepare

        long start = System.currentTimeMillis();


        APICallScanner generalAPIScan = new APICallScanner(contextReference.get());
        generalAPIScan.fullScan(classes);



        long end = System.currentTimeMillis();
        long millis  = end - start;

        String lapse = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
        //Log.d(TAG, "Scan lapse time " + lapse);

        return null;
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
