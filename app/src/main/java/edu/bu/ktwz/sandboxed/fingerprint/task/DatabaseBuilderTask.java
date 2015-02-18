package edu.bu.ktwz.sandboxed.fingerprint.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import edu.bu.ktwz.sandboxed.R;
import edu.bu.ktwz.sandboxed.fingerprint.APIScanner;
import edu.bu.ktwz.sandboxed.fingerprint.AndroidFrameworkFileIO;
import edu.bu.ktwz.sandboxed.fingerprint.ServiceScanner;

/**
 * Build the database in a separate thread
 * Created by wil on 2/1/15.
 */

//TODO may want this in a service if it takes to long
public class DatabaseBuilderTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = DatabaseBuilderTask.class.getName();

    private final WeakReference<Context> contextReference;
    private DatabaseBuildTaskCallback callback;
    String preloadedClassFilename;
    String frameworkFilename;
    String frameworkPath;
    public DatabaseBuilderTask(Context context, DatabaseBuildTaskCallback callback){
        super();
        contextReference = new WeakReference<Context>(context);

        preloadedClassFilename = context.getString(R.string.filename_preloaded_classes);
        frameworkFilename = context.getString(R.string.filename_android_framework);
        frameworkPath = context.getString(R.string.path_android_framework);

        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {

        APIScanner.NATIVE_COUNT=0;
        Looper.prepare(); //Prevent RTE cant create handler without calling looper.prepare


        long start = System.currentTimeMillis();

        AndroidFrameworkFileIO d = new AndroidFrameworkFileIO(contextReference.get());

        //TODO optimize, read from file and insert straight to db

        d.loadClassListIntoDatabase(frameworkPath, preloadedClassFilename);


        new ServiceScanner(contextReference.get()).scan();


        //Copy the framework into our working directory
//        d.copy(frameworkPath, frameworkFilename);

        //Extract the file containing all the classes
//        d.extract(frameworkFilename, preloadedClassFilename);

//        List<String> classList = d.loadPreloadedClassList(preloadedClassFilename);

//        APICallScanner scanner = new APICallScanner(context, classList);
//        scanner.scan();

        long end = System.currentTimeMillis();
        long millis  = end - start;

        String lapse = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
        Log.d(TAG, "Scan lapse time " + lapse);
        Log.d(TAG, "Native count= " + APIScanner.NATIVE_COUNT);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        callback.onDatabaseBuildSuccess();
    }

    public interface DatabaseBuildTaskCallback {
        public void onDatabaseBuildSuccess();
        public void onDatabaseBuildFailure();

    }
}
