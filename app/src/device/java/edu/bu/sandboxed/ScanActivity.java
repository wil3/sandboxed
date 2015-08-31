package edu.bu.sandboxed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.octo.android.robospice.SpiceManager;

import edu.bu.sandboxed.model.APICall;
import edu.bu.sandboxed.model.CachedAPICall;
import roboguice.activity.RoboFragmentActivity;

public class ScanActivity extends RoboFragmentActivity implements LoadingFragment.LoadCallback {

    private static final String TAG_SCAN = "SCAN_FRAGMENT";
    public static String EXTRA_FORCE = "EXTRA_FORCE";
    private SpiceManager spiceManager = new SpiceManager(OfflineSpiceService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        APICall apiCall = APICall.getLastInserted();

        boolean forceScan = getIntent().getBooleanExtra(EXTRA_FORCE, false);

     //   if (savedInstanceState == null){
            if (apiCall == null || forceScan) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new LoadingFragment(),TAG_SCAN)
                        .commit();
            } else {
               showResults();
            }
      //  }
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }
    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onLoadSuccess() {
        showResults();
    }

    @Override
    public void onLoadFailure() {

    }

    private void showResults(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onShowResultsClick(View view){
        showResults();
    }
    public void onRescanClick(View view){

        Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_SCAN);
        if (f != null) {
            getSupportFragmentManager().beginTransaction()
                    .detach(f)
                    .attach(f)
                    .commit();
        }
    }
}
