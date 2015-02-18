package edu.bu.ktwz.sandboxed.request;

import android.content.Context;

import com.octo.android.robospice.request.SpiceRequest;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.List;

import edu.bu.ktwz.sandboxed.fingerprint.APIScanner;
import edu.bu.ktwz.sandboxed.fingerprint.ServiceScanner;

/**
 * Created by wil on 2/16/15.
 */
public class ServiceFingerprintRequest extends SpiceRequest<Hashtable> {
    private final WeakReference<Context> contextReference;
    private List<String> classes;
    public ServiceFingerprintRequest(Context context){
        super(Hashtable.class);
        this.contextReference = new WeakReference<Context>(context);
    }
    @Override
    public Hashtable<String, String> loadDataFromNetwork() throws Exception {
        ServiceScanner services = new ServiceScanner(contextReference.get());
        services.setScanListener(new APIScanner.ScanProgressListener() {
            @Override
            public void onClassScanned(String className) {
                publishProgress();
            }
        });

        services.scan();

        return services.getResults();
    }

}
