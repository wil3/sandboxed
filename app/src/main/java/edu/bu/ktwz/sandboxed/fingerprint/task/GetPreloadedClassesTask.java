package edu.bu.ktwz.sandboxed.fingerprint.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.bu.ktwz.sandboxed.R;
import edu.bu.ktwz.sandboxed.fingerprint.AndroidFrameworkFileIO;

/**
 * Build the database in a separate thread
 * Created by wil on 2/1/15.
 */

//TODO may want this in a service if it takes to long
public class GetPreloadedClassesTask extends AsyncTask<Void, Void, List<String>> {
    private static final String TAG =GetPreloadedClassesTask.class.getName();
    private final WeakReference<Context> contextReference;
    private GetPreloadedClassesTaskCallback callback;
    String preloadedClassFilename;
    String frameworkPath;
    public GetPreloadedClassesTask(Context context, GetPreloadedClassesTaskCallback callback){
        super();
        contextReference = new WeakReference<Context>(context);
        this.callback = callback;
        preloadedClassFilename = context.getString(R.string.filename_preloaded_classes);
        frameworkPath = context.getString(R.string.path_android_framework);

    }

    @Override
    protected List<String> doInBackground(Void... params) {

        //Looper.prepare(); //Prevent RTE cant create handler without calling looper.prepare

        long start = System.currentTimeMillis();

        AndroidFrameworkFileIO d = new AndroidFrameworkFileIO(contextReference.get());
        List<String> classes = d.loadClassListIntoMemory(frameworkPath, preloadedClassFilename);

        long end = System.currentTimeMillis();
        long millis  = end - start;

        String lapse = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
        Log.d(TAG, "Scan lapse time " + lapse);
        return classes;
    }

    @Override
    protected void onPostExecute(List<String> classes) {
        super.onPostExecute(classes);
        if (callback != null) {
            callback.onGetPreloadedClassesSuccess(classes);
        }
    }

    public interface GetPreloadedClassesTaskCallback {
        public void onGetPreloadedClassesSuccess(List<String> classes);
        public void onGetPreloadedClassesFailure();

    }
}
