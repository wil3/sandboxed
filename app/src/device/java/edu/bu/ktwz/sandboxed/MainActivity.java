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
import java.lang.Override;

import edu.bu.ktwz.sandboxed.LoadingFragment;
import edu.bu.ktwz.sandboxed.fingerprint.AndroidFrameworkFileIO;
import edu.bu.ktwz.sandboxed.fingerprint.task.ScannerTask;
import edu.bu.ktwz.sandboxed.model.APICall;
import roboguice.activity.RoboActivity;


//@ContentView(R.layout.activity_main)

public class MainActivity extends RoboActivity implements LoadingFragment.LoadCallback{

    private static final String TAG = MainActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {

            Fragment f = (APICall.isEmpty()) ? new LoadingFragment() : new FingerprintFragment()

            getFragmentManager().beginTransaction()
                    .add(R.id.container, f)
                    .commit();
        }
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
                refreshDatabase();
                break;
            case R.id.action_export:
                exportDatabase();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshDatabase(){
        ScannerTask task = new ScannerTask(getApplicationContext(), new ScannerTask.DatabaseBuildTaskCallback(){

            @Override
            public void onDatabaseBuildSuccess() {

            }

            @Override
            public void onDatabaseBuildFailure() {

            }
        });
        task.execute();
    }

    private void exportDatabase(){

        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Toast.makeText(this, "External SD card not mounted", Toast.LENGTH_LONG).show();
            return;
        }

        //TODO add some sort of loading thing
        FileOutputStream fos = null;
        File file = new AndroidFrameworkFileIO(this).getExportFile();

        try {
            fos = new FileOutputStream(file);

            Cursor cursor = APICall.fetchResultCursor();
            cursor.moveToFirst();
            while(cursor.isAfterLast() == false){
                String callName = cursor.getString(cursor.getColumnIndexOrThrow("class"));
                String value = cursor.getString(cursor.getColumnIndexOrThrow("value"));

                String line = callName + "=" + value + "\n";
                fos.write(line.getBytes());

                cursor.moveToNext();
            }
            cursor.close();

            String path = getResources().getString(R.string.message_export_finish);
            String notification = String.format(path,file.getAbsolutePath());
            Toast.makeText(getApplicationContext(),notification, Toast.LENGTH_LONG  ).show();

        } catch (IOException e){
            Log.e(TAG, e.getMessage());
            Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_LONG  ).show();

        } finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }


    @Override
    public void onLoadSuccess() {
        getFragmentManager().beginTransaction().replace(R.id.container,new FingerprintFragment())

    }

    @Override
    public void onLoadFailure() {

    }
}
