package edu.bu.sandboxed;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.lang.Override;

import edu.bu.sandboxed.model.APICall;
import edu.bu.sandboxed.request.ExportRequest;
import roboguice.activity.RoboFragmentActivity;


//@ContentView(R.layout.activity_main
public class MainActivity extends RoboFragmentActivity implements LoadingFragment.LoadCallback{

    private static final String TAG = MainActivity.class.getName();
    private static final String EXTRA_EXPORT_PATH = "EXTRA_EXPORT_PATH";
    private static final String DIALOG_TAG = "DIALOG_TAG";
    private SpiceManager spiceManager = new SpiceManager(OfflineSpiceService.class);

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {

            Fragment f = (APICall.isEmpty()) ? new LoadingFragment() : new FingerprintFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, f)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        spiceManager.start( this );
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
           // case R.id.action_refresh:
           //     refreshDatabase();
           //     break;
            case R.id.action_export:
                exportDatabase();
                break;
        }
        return super.onOptionsItemSelected(item);
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
}
