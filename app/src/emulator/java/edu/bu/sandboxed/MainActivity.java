package edu.bu.sandboxed;

import android.os.Bundle;

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
        }
    }


    @Override
    public void onLoadSuccess() {

    }

    @Override
    public void onLoadFailure() {

    }
}
