package edu.bu.ktwz.sandboxed;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.bu.ktwz.sandboxed.fingerprint.AndroidFrameworkFileIO;
import edu.bu.ktwz.sandboxed.fingerprint.task.ScannerTask;
import edu.bu.ktwz.sandboxed.model.APICall;
import roboguice.activity.RoboActivity;
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
