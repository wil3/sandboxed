package edu.bu.ktwz.sandboxed;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.bu.ktwz.sandboxed.R;
import edu.bu.ktwz.sandboxed.fingerprint.OfflineSpiceService;
import edu.bu.ktwz.sandboxed.request.APIFingerprintRequest;
import edu.bu.ktwz.sandboxed.request.AndroidFrameworkClassListRequest;
import edu.bu.ktwz.sandboxed.request.PostScanRequest;
import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoadingFragment extends RoboFragment {

    public interface LoadCallback {
        public void onLoadSuccess();
        public void onLoadFailure();
    }
    private SpiceManager spiceManager = new SpiceManager(OfflineSpiceService.class);


    private long startTime;
    private final Hashtable<String, String> fingerprints = new Hashtable<String, String>();
    private int numberTasks=0;

    private LoadCallback callback;

    @InjectView(R.id.textMessage) private TextView messageText;
    @InjectView(R.id.progressBar) private ProgressBar progressBar;
    @InjectView(R.id.textMessageDetails) private TextView messageDetails;

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
        progressBar.setMax(100);
    }
    @Override
    public void onStart() {
        super.onStart();
        startTime = System.currentTimeMillis();

        spiceManager.start(getActivity());
        spiceManager.execute(new AndroidFrameworkClassListRequest(getActivity().getApplicationContext()),
                new AndroidFrameworkClassListResultListener());
    }

    @Override
    public void onStop() {
        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }
        super.onStop();
    }


    private class AndroidFrameworkClassListResultListener implements RequestListener<List> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
        }

        @Override
        public void onRequestSuccess(List classes) {
           // progressBar.setProgress(10);
            progressBar.setMax(classes.size());
            beginScan(classes);

        }

        private void beginScan(List classes){
            int groups = 1;
            int size = (int) Math.ceil(((double)classes.size())/((double)groups));
            for (int i=0; i< groups; i++){
                int start = i*size;
                int end = Math.min(start + size, classes.size());
                spiceManager.execute(new APIFingerprintRequest(getActivity().getApplicationContext(), classes.subList(start, end)),
                        new FingerprintScanResultListener());
                numberTasks++;
            }
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
            numberTasks--;
            if (numberTasks == 0){
              //  messageText.setText(R.string.message_fingerprint_finish);
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
            if (message != null){
                details += " " + message;
            }
            messageDetails.setText(details);
            callback.onLoadSuccess();
        }
    }
}
