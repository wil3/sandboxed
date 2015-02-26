package edu.bu.ktwz.sandboxed;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import edu.bu.ktwz.sandboxed.fingerprint.task.DatabaseBuilderTask;
import edu.bu.ktwz.sandboxed.model.APICall;
import edu.bu.ktwz.sandboxed.fingerprint.task.SerializedFingerprintTask;
import edu.bu.ktwz.sandboxed.fingerprint.task.GetPreloadedClassesTask;
import edu.bu.ktwz.sandboxed.fingerprint.task.ServiceScannerTask;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;


public class FingerprintFragment extends RoboFragment implements DatabaseBuilderTask.DatabaseBuildTaskCallback,
        GetPreloadedClassesTask.GetPreloadedClassesTaskCallback,
        ServiceScannerTask.ScannerTaskCallback
       //, FingerprintTask.FingerprintTaskCallback
{

    private static final String TAG = FingerprintFragment.class.getName();



    @InjectView(R.id.input_search)  private EditText filterText;
    @InjectView(R.id.list_apicalls) private ListView apiList;

    private APICallCursorAdapter adapter;
    private ProgressDialog ringProgressDialog;
    public FingerprintFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_apicalls, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Cursor c = APICall.fetchResultCursor();
        adapter = new APICallCursorAdapter(getActivity().getApplicationContext(),c);
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return APICall.fetchResultCursor(constraint.toString());
            }
        });
        apiList.setAdapter(adapter);

        filterText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                adapter.getFilter().filter(cs);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) { }

            @Override
            public void afterTextChanged(Editable arg0) { }
        });
    }
    /*
    @Override
    public void onStart(){
        super.onStart();

        //Go here so we know the activity is created so we can use the context for the dialog
        String flavor = getResources().getString(R.string.flavor);
        if (flavor.equals(SandboxedApplication.FLAVOR_EMULATOR) ||
                flavor.equals(SandboxedApplication.FLAVOR_REMOTE)) {
            //Always run, dont use a database for these two flavors
            launchRingDialog();
            GetPreloadedClassesTask task = new GetPreloadedClassesTask(getActivity().getApplicationContext(), this);
            task.execute();

        } else {
            if (APICall.isEmpty()){
                Log.d(TAG, "Database is empty, needs to be initialized");
                launchRingDialog();
                //  DatabaseBuilderTask task = new DatabaseBuilderTask(getActivity().getApplicationContext(), this);
                //  task.execute();
                GetPreloadedClassesTask task = new GetPreloadedClassesTask(getActivity().getApplicationContext(), this);
                task.execute();
            } else {

            }
        }
    }*/
    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDatabaseBuildSuccess() {
        if (ringProgressDialog != null) {
            ringProgressDialog.dismiss();
        }
        Cursor cursor = APICall.fetchResultCursor();
        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();

    }


    @Override
    public void onDatabaseBuildFailure() {
        if (ringProgressDialog != null) {
            ringProgressDialog.dismiss();
        }
    }



    private void launchRingDialog() {
        ringProgressDialog = ProgressDialog.show(getActivity(), getString(R.string.message_database_title), getString(R.string.message_database_message), true);
        ringProgressDialog.setCancelable(false);
    }

    private int numberTasks=0;
    private long startTime =0;

    @Override
    public void onGetPreloadedClassesSuccess(List<String> classes) {
        int cores = Runtime.getRuntime().availableProcessors();
        Log.d(TAG, "Scanning " + classes.size() + " classes.");
        Log.d(TAG, "Number of cores = " + cores);
        startTime = System.currentTimeMillis();

/*
        int groups = 2;
        int size = (int) Math.ceil(((double)classes.size())/((double)groups));
        for (int i=0; i< groups; i++){
            int start = i*size;
            int end = Math.min(start + size, classes.size());
            FingerprintTask fingerprintTask = new FingerprintTask(getActivity().getApplicationContext(), this, "id="+i);
            Log.d(TAG, "Task processing " + (end-start) + " calls.");
            fingerprintTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, classes.subList(start, end)); //inclusize, exclusive
            numberTasks++;
        }
*/

        /*
        FingerprintTask fingerprintTask = new FingerprintTask(getActivity().getApplicationContext(), this, "id=0");
        fingerprintTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, classes); //inclusize, exclusive
        numberTasks++;

        ServiceScannerTask serviceTask = new ServiceScannerTask(getActivity().getApplicationContext(), this);
        serviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        numberTasks++;
*/

        SerializedFingerprintTask task = new SerializedFingerprintTask(getActivity().getApplicationContext(), this);
        task.execute(classes);
      //  numberTasks++;

        Log.d(TAG, "Launched " + numberTasks + " tasks.");
    }

    @Override
    public void onGetPreloadedClassesFailure() {

    }

    @Override
    public void onScannerSuccess(String id) {
       // Log.d(TAG, "Task complete " + id);
        //numberTasks--;
        //if (numberTasks == 0){
            postScan();
        //}

    }


    private void postScan(){

        if (ringProgressDialog != null) {
            ringProgressDialog.dismiss();
        }
        Cursor cursor = APICall.fetchResultCursor();
        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();


    }
    /*
    @Override
    public void onFingerprintSuccess() {

    }
*/
    @Override
    public void onScannerFailure() {

    }

    class APICallCursorAdapter extends CursorAdapter {
        public APICallCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_list_apicall, parent, false);
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView tvCall = (TextView)view.findViewById(R.id.textCall);
            TextView tvValue = (TextView)view.findViewById(R.id.textValue);
            TextView tvClassName = (TextView)view.findViewById(R.id.textPackage);
            //TODO shoudnt activeandroid be able to extract into the model?
            //String packageName = cursor.getString(cursor.getColumnIndexOrThrow("package"));
            String callName = cursor.getString(cursor.getColumnIndexOrThrow("class"));
            //String accessName = cursor.getString(cursor.getColumnIndexOrThrow("access"));
            String value = cursor.getString(cursor.getColumnIndexOrThrow("value"));

            tvClassName.setText(APICall.getPackageName(callName));
            tvCall.setText(APICall.getSimpleClassName(callName));
            tvValue.setText(value);
        }
    }
}
