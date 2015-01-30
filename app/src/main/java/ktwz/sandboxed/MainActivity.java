package ktwz.sandboxed;

import android.os.Bundle;
import android.util.Log;

import java.util.List;

import ktwz.sandboxed.discover.APICallScanner;
import ktwz.sandboxed.discover.AndroidFrameworkReader;
import ktwz.sandboxed.model.APICall;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;


//@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {

    private static final String TAG = MainActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new APICallsFragment())
                    .commit();
        }

        if (APICall.isEmpty()){
            Log.d(TAG,"Database is emtpy, needs to be initiallized");
            buildDatabase();
        }

        Log.d(TAG, "DONE!");
    }

    private void buildDatabase(){
        AndroidFrameworkReader d = new AndroidFrameworkReader(getApplicationContext());
        d.copy("/system/framework/framework.jar", "framework.zip");
        d.extract("framework.zip", "preloaded-classes");

        //TODO optimize, read from file and insert straight to db
        List<String> classList = d.loadPreloadedClassList("preloaded-classes");

        APICallScanner scanner = new APICallScanner(getApplicationContext(), classList);
        scanner.scan();
    }

}
