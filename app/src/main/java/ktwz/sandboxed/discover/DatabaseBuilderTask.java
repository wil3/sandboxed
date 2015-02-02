package ktwz.sandboxed.discover;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import ktwz.sandboxed.R;

/**
 * Build the database in a separate thread
 * Created by wil on 2/1/15.
 */

//TODO may want this in a service if it takes to long
public class DatabaseBuilderTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private DatabaseBuildTaskCallback callback;
    public DatabaseBuilderTask(Context context, DatabaseBuildTaskCallback callback){
        super();
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {

        AndroidFrameworkFileIO d = new AndroidFrameworkFileIO(context);

        String preloadedClassFilename = context.getString(R.string.filename_preloaded_classes);
        String frameworkFilename = context.getString(R.string.filename_android_framework);
        String frameworkPath = context.getString(R.string.path_android_framework);

        //TODO optimize, read from file and insert straight to db

        //Copy the framework into our working directory
        d.copy(frameworkPath, frameworkFilename);

        //Extract the file containing all the classes
        d.extract(frameworkFilename, preloadedClassFilename);

        List<String> classList = d.loadPreloadedClassList(preloadedClassFilename);

        APICallScanner scanner = new APICallScanner(context, classList);
        scanner.scan();

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
