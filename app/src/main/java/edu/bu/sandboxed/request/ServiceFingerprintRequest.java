package edu.bu.sandboxed.request;

import android.content.Context;

import com.octo.android.robospice.request.SpiceRequest;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.List;

import edu.bu.sandboxed.fingerprint.APIScanner;
import edu.bu.sandboxed.fingerprint.ServiceScanner;

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
            public void onAfterClassScanned(String className) {
                publishProgress();
            }

            @Override
            public void onBeforeClassScanned(String className) {

            }

            @Override
            public void onBeforeMethodInvoked(String className, String name) {

            }

            @Override
            public void onAfterMethodInvoked(String className, String name) {

            }

            @Override
            public void onValueReturned(String className, String name, String value) {

            }
        });

        services.scan();

        return services.getResults();
    }

}
