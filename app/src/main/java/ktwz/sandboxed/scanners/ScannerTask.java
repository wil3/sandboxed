package ktwz.sandboxed.scanners;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;

import java.util.List;

import ktwz.sandboxed.R;

/**
 * Build the database in a separate thread
 * Created by wil on 2/1/15.
 */

//TODO may want this in a service if it takes to long
public class ScannerTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private DatabaseBuildTaskCallback callback;
    public ScannerTask(Context context, DatabaseBuildTaskCallback callback){
        super();
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {

        Looper.prepare(); //Prevent RTE cant create handler without calling looper.prepare

        AndroidFrameworkFileIO d = new AndroidFrameworkFileIO(context);

        String preloadedClassFilename = context.getString(R.string.filename_preloaded_classes);

        List<String> classList = d.loadPreloadedClassList(preloadedClassFilename);

        APICallScanner scanner = new APICallScanner(context);
        scanner.scan(classList);

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
