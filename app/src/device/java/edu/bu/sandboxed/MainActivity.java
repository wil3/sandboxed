package edu.bu.sandboxed;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.lang.Override;
import java.util.ArrayList;
import java.util.List;

import edu.bu.sandboxed.model.APICall;
import edu.bu.sandboxed.model.FatalAPICall;
import edu.bu.sandboxed.request.ExportRequest;
import roboguice.activity.RoboFragmentActivity;


//@ContentView(R.layout.activity_main
public class MainActivity extends RoboFragmentActivity implements LoadingFragment.LoadCallback{

    private static final String TAG = MainActivity.class.getName();
    private static final String TAG_LOAD = "FRAGMENT_LOAD";
    private static final String EXTRA_EXPORT_PATH = "EXTRA_EXPORT_PATH";
    private static final String EXTRA_INVOKED = "EXTRA_INVOKED";
    private static final String DIALOG_TAG = "DIALOG_TAG";
    private static final String DIALOG_FATAL_TAG = "DIALOG_FATAL_TAG";

    private SpiceManager spiceManager = new SpiceManager(OfflineSpiceService.class);

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
           checkAndScan();
        }
    }

    private void checkAndScan(){
        APICall apiCall = APICall.getLastInserted();
        getSupportFragmentManager().findFragmentByTag(TAG_LOAD);
        Fragment f = (apiCall == null || apiCall.status == APICall.STATUS_UKNOWN) ? new LoadingFragment() : new FingerprintFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new FingerprintFragment())
                .commit();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.action_refresh:
               Intent intent =  new Intent(this, ScanActivity.class);
                intent.putExtra(ScanActivity.EXTRA_FORCE, true);
                        startActivity(intent);
                finish();
           //     refreshDatabase();
                break;
            case R.id.action_export:
                exportDatabase();
                break;
           // case R.id.action_fatal:
           //     showFatalCalls();
           // case R.id.action_delete:
               // new Delete().from(APICall.class).execute();
               // checkAndScan();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFatalCalls(){
        List<String> invokes = getExcludeList();
        String invokeString = "";
        for (String s : invokes) {
            invokeString += s + "\n";
        }

        DialogFragment df = (DialogFragment) getFragmentManager().findFragmentByTag(DIALOG_FATAL_TAG);
        if (df == null){
            df = FatalDialogFragment.newInstance(invokeString);
        }
        df.show(getFragmentManager(), DIALOG_FATAL_TAG);
    }


    private List<String> getExcludeList(){
        List<String> methods = new ArrayList<String>();

        List<FatalAPICall> apiCalls = FatalAPICall.getAll();
        if (apiCalls != null) {
            for (FatalAPICall apiCall : apiCalls) {
                methods.add(apiCall.methodName);
            }
        }

        return methods;
    }
    private void exportDatabase(){
        spiceManager.execute(new ExportRequest(getApplicationContext()), new ExportResultListener());
    }


    private class ExportResultListener implements RequestListener<String> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(MainActivity.this, "External SD card not mounted", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(String message) {

            if (message == null){ //Something bad happened
                return;
            }
            String path = getResources().getString(R.string.message_export_finish);
            String notification = String.format(path,message);

            DialogFragment df = (DialogFragment) getFragmentManager().findFragmentByTag(DIALOG_TAG);
            if (df == null){
                df = ConfirmDialogFragment.newInstance(notification);
            }
            df.show(getFragmentManager(), DIALOG_TAG);
        }
    }

            @Override
    public void onLoadSuccess() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container,new FingerprintFragment()).commit();

    }

    @Override
    public void onLoadFailure() {

    }

    public static class ConfirmDialogFragment extends DialogFragment {

        public static DialogFragment newInstance(String path){
            Bundle b = new Bundle();
            b.putString(EXTRA_EXPORT_PATH, path);
            DialogFragment df = new ConfirmDialogFragment();
            df.setArguments(b);
            return df;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String path = getArguments().getString(EXTRA_EXPORT_PATH);

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(path)
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    });

            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public static class FatalDialogFragment extends DialogFragment {

        public static DialogFragment newInstance(String invoked){
            Bundle b = new Bundle();
            b.putString(EXTRA_INVOKED, invoked);

            DialogFragment df = new FatalDialogFragment();
            df.setArguments(b);
            return df;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String invoked = getArguments().getString(EXTRA_INVOKED);

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Fatal Calls")
                    .setMessage(invoked)
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });

            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
