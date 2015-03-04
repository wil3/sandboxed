package edu.bu.sandboxed.request;

import android.content.Context;

import com.octo.android.robospice.request.SpiceRequest;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import edu.bu.sandboxed.R;
import edu.bu.sandboxed.SandboxedApplication;
import edu.bu.sandboxed.fingerprint.AndroidFrameworkFileIO;
import edu.bu.sandboxed.net.C2;

/**
 * Created by wil on 2/16/15.
 */
public class PostScanRequest extends SpiceRequest<String> {
    private final WeakReference<Context> contextReference;
    private final Hashtable<String, String> fingerprints;
    public PostScanRequest(Context context, Hashtable<String, String> fingerprints){
        super(String.class);
        this.contextReference = new WeakReference<Context>(context);
        this.fingerprints = fingerprints;
    }

    @Override
    public String loadDataFromNetwork() throws Exception {

        String response = null;
        AndroidFrameworkFileIO io = new AndroidFrameworkFileIO(contextReference.get());

        String flavor = contextReference.get().getResources().getString(R.string.flavor);

        //Has a command and control
        if (flavor.equals(SandboxedApplication.FLAVOR_REMOTE)) {

             io.exportHashToFile(fingerprints);
            //Send to C2
            C2 c2 = new C2(contextReference.get());
            String c2Response = c2.uploadFingerprint();
            return contextReference.get().getString(R.string.response_c2, c2Response);
        } else if (flavor.equals(SandboxedApplication.FLAVOR_EMULATOR)){
            String filePath = io.exportHashToFile(fingerprints);
            return contextReference.get().getString(R.string.message_export_finish, filePath);

        } else {
            //Load memory into database
            io.loadHashtableIntoDatabase(fingerprints);
        }



        return response;
    }
}
