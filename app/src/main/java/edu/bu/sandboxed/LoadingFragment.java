package edu.bu.sandboxed;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.bu.sandboxed.model.APICall;
import edu.bu.sandboxed.model.CachedAPICall;
import edu.bu.sandboxed.request.APIFingerprintRequest;
import edu.bu.sandboxed.request.AndroidFrameworkClassListRequest;
import edu.bu.sandboxed.request.PingRequest;
import edu.bu.sandboxed.request.PostScanRequest;
import edu.bu.sandboxed.request.SendFingerprintToRemoteServerRequest;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 */
public class LoadingFragment extends RoboFragment {

    private static final String TAG = LoadingFragment.class.getName();

    public interface LoadCallback {
        public void onLoadSuccess();
        public void onLoadFailure();
    }
    private SpiceManager spiceManager = new MySpiceManager(OfflineSpiceService.class);
   // private SpiceManager spiceManagerOnline = new SpiceManager(
   //         UncachedSpiceService.class);

    private long startTime;
    private final Hashtable<String, String> fingerprints = new Hashtable<String, String>();
    private int numberTasks=0;

    private LoadCallback callback;

    @InjectView(R.id.textMessage) private TextView messageText;
    @InjectView(R.id.progressBar) private ProgressBar progressBar;
    @InjectView(R.id.textMessageDetails) private TextView messageDetails;

   // @InjectView(R.id.btn_rescan) private Button rescanButton;
  //  @InjectView(R.id.btn_show) private Button showButton;
    public LoadingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try {
            callback = (LoadCallback)activity;
        }  catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LoadCallback");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_no_display, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedState){
        super.onViewCreated(view, savedState);
    }
    @Override
    public void onStart() {
        super.onStart();
        startTime = System.currentTimeMillis();

        spiceManager.start(getActivity());
        //spiceManagerOnline.start(getActivity());

        new Delete().from(APICall.class).execute();
        
        String flavor = getString(R.string.flavor);
        if (flavor.equals(SandboxedApplication.FLAVOR_REMOTE)) {
 //           spiceManagerOnline.execute(new PingRequest(getActivity().getApplicationContext()),
 //                   new PingListener());
 //           spiceManagerOnline.execute(new SimpleFingerprintRequest(getActivity().getApplicationContext()), new SimpleScanResultListener());

        } else {
            spiceManager.execute(new AndroidFrameworkClassListRequest(getActivity().getApplicationContext()),
                    new AndroidFrameworkClassListResultListener());
        }

        boolean hasCache = CachedAPICall.isEmpty();

        if (!hasCache){
            messageText.setText(R.string.message_resume_scan);
        }
    }

    @Override
    public void onStop() {
        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }
       // if (spiceManagerOnline.isStarted()) {
       //     spiceManagerOnline.shouldStop();
       // }
        super.onStop();
    }


    private class SimpleScanResultListener implements RequestListener<Integer> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.e(TAG, spiceException.getMessage());
        }

        @Override
        public void onRequestSuccess(Integer message) {
            
            Log.d(TAG, "Response from C2:  " + message);

        }
    }

    /**
     * Results from the app first pinging the outside world
     */
    private class PingListener implements RequestListener<String> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            messageDetails.setText(R.string.message_ping_failed);
        }

        @Override
        public void onRequestSuccess(String message) {

            Log.d(TAG, "Received " + message + " from C2");
            String ok = getString(R.string.response_ping_ok);
            if (message.equals(ok)){
                
                if (!getResources().getBoolean(R.bool.simple_scan)) {
                    spiceManager.execute(new AndroidFrameworkClassListRequest(getActivity().getApplicationContext()),
                        new AndroidFrameworkClassListResultListener());
                } else {
                    //spiceManagerOnline.execute(new SendFingerprintToRemoteServerRequest(getActivity().getApplicationContext()), new SimpleScanResultListener());
                }
            } else {
                messageDetails.setText(R.string.message_ping_failed);
            }
        }
    }

    private class AndroidFrameworkClassListResultListener implements RequestListener<List> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            messageDetails.setText(R.string.message_fingerprint_error);
        }

        @Override
        public void onRequestSuccess(List classes) {
            if (classes == null || classes.isEmpty()){
                Log.e(TAG, "Could not obtain classes in Android framework!");
                onRequestFailure(null);
                return;
            }
            Log.d(TAG, "Size " + classes.size());
            progressBar.setMax(classes.size());
            beginScan(classes);

        }

        private void beginScan(List classes){

            int groups = getResources().getInteger(R.integer.num_threads);
            int size = (int) Math.ceil(((double)classes.size())/((double)groups));
            for (int i=0; i< groups; i++){
                int start = i*size;
                int end = Math.min(start + size, classes.size());
                spiceManager.execute(new APIFingerprintRequest(getActivity().getApplicationContext(), classes.subList(start, end)),
                        new FingerprintScanResultListener());
                numberTasks++;
            }

// UNCOMMENT TO ENABLE SERVICE SCANNING!
// Requires multiple permimissions in manifest
//            spiceManager.execute(new ServiceFingerprintRequest(getActivity().getApplicationContext()),
//                   new FingerprintScanResultListener());
//            numberTasks++;

        }
    }



    private class FingerprintScanResultListener implements RequestListener<Hashtable>, RequestProgressListener {

        public FingerprintScanResultListener(){
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
        }
        @Override
        public void onRequestSuccess(Hashtable fingerprint) {
            fingerprints.putAll(fingerprint);
            numberTasks--; //This is for if things are done in parrellel
            if (numberTasks == 0){
                spiceManager.execute(new PostScanRequest(getActivity().getApplicationContext(), fingerprints),
                        new PostScanResultListener());

            }
        }

        @Override
        public void onRequestProgressUpdate(RequestProgress progress) {


            progressBar.setProgress(progressBar.getProgress()+1);

        }
    }


    private class PostScanResultListener implements RequestListener<String> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {

        }

        @Override
        public void onRequestSuccess(String message) {

            long lapse = System.currentTimeMillis() - startTime;

            String details = "";
            String timeDetails = getString(R.string.message_lapse,
                    TimeUnit.MILLISECONDS.toMinutes(lapse),
                    TimeUnit.MILLISECONDS.toSeconds(lapse) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(lapse)));

            details+= timeDetails;

            String countDetails = getString(R.string.message_fingerprint_count, fingerprints.size());

            details+= "\n" + countDetails;

            if (message != null){
                details += "\n" + message;
            }
            messageDetails.setText(details);

           // showButton.setEnabled(true);
          //  rescanButton.setEnabled(true);
            //Allow to stay visibile for a second
            Handler handlerTimer = new Handler();

            handlerTimer.postDelayed(new Runnable(){
                public void run() {
                    callback.onLoadSuccess();
                }}, 1000);

        }
    }
}
