package ktwz.sandboxed.scanners;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import ktwz.sandboxed.R;

/**
 * Build the database in a separate thread
 * Created by wil on 2/1/15.
 */

//TODO may want this in a service if it takes to long
public class DatabaseBuilderTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = DatabaseBuilderTask.class.getName();

    private Context context;
    private DatabaseBuildTaskCallback callback;
    public DatabaseBuilderTask(Context context, DatabaseBuildTaskCallback callback){
        super();
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {



        long start = System.currentTimeMillis();

        AndroidFrameworkFileIO d = new AndroidFrameworkFileIO(context);

        String preloadedClassFilename = context.getString(R.string.filename_preloaded_classes);
        String frameworkFilename = context.getString(R.string.filename_android_framework);
        String frameworkPath = context.getString(R.string.path_android_framework);

        //TODO optimize, read from file and insert straight to db

        d.loadClassListIntoDatabase(frameworkPath, preloadedClassFilename);
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
