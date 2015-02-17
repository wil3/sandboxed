package edu.bu.ktwz.sandboxed.request;

import android.content.Context;

import com.octo.android.robospice.request.SpiceRequest;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.List;

import edu.bu.ktwz.sandboxed.fingerprint.APICallScanner;

/**
 * Created by wil on 2/16/15.
 */
public class APIFingerprintRequest extends SpiceRequest<Hashtable> {
    private final WeakReference<Context> contextReference;
    private List<String> classes;
    public APIFingerprintRequest(Context context, List<String> classes){
        super(Hashtable.class);
        this.contextReference = new WeakReference<Context>(context);
        this.classes = classes;
    }
    @Override
    public Hashtable<String, String> loadDataFromNetwork() throws Exception {
        APICallScanner generalAPIScan = new APICallScanner(contextReference.get());
        generalAPIScan.setScanListener(new APICallScanner.ScanProgressListener() {
            @Override
            public void onClassScanned(String className) {
                publishProgress();
            }
        });
        generalAPIScan.fullScan(classes);

        return generalAPIScan.getResults();
    }

}
