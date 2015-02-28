package edu.bu.ktwz.sandboxed;

import android.os.Bundle;

import edu.bu.ktwz.sandboxed.fingerprint.task.SerializedFingerprint2Task;
import roboguice.activity.RoboFragmentActivity;


//@ContentView(R.layout.activity_main)

public class MainActivity extends RoboFragmentActivity implements LoadingFragment.LoadCallback{

    private static final String TAG = MainActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new LoadingFragment())
                    .commit();

       //     SerializedFingerprint2Task task = new SerializedFingerprint2Task(getApplicationContext(), null);
         //   task.execute();
        }
    }


    @Override
    public void onLoadSuccess() {

    }

    @Override
    public void onLoadFailure() {

    }
}
