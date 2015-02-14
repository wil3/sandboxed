package edu.bu.ktwz.sandboxed.fingerprint;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * Build the database in a separate thread
 * Created by wil on 2/1/15.
 */

//TODO may want this in a service if it takes to long
public class ServiceScannerTask extends AsyncTask<Void, Void, Void> {

    private final WeakReference<Context> contextReference;
    private ScannerTaskCallback callback;
    public ServiceScannerTask(Context context, ScannerTaskCallback callback){
        super();
        contextReference = new WeakReference<Context>(context);
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {

       // Looper.prepare(); //Prevent RTE cant create handler without calling looper.prepare

        new ServiceScanner(contextReference.get()).scan();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (callback != null) {
            callback.onScannerSuccess(ServiceScannerTask.class.getSimpleName());
        }
    }

    public interface ScannerTaskCallback {
        public void onScannerSuccess(String id);
        public void onScannerFailure();

    }
}
