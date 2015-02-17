package edu.bu.ktwz.sandboxed.request;

import android.content.Context;

import com.octo.android.robospice.request.SpiceRequest;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.bu.ktwz.sandboxed.R;
import edu.bu.ktwz.sandboxed.fingerprint.AndroidFrameworkFileIO;

/**
 * Created by wil on 2/16/15.
 */
public class AndroidFrameworkClassListRequest extends SpiceRequest<List> {
    private final WeakReference<Context> contextReference;
    private final String preloadedClassFilename;
    private final String frameworkPath;
    public AndroidFrameworkClassListRequest( Context context){
        super( List.class);

        this.contextReference = new WeakReference<Context>(context);
        this.preloadedClassFilename = context.getString(R.string.filename_preloaded_classes);
        this.frameworkPath = context.getString(R.string.path_android_framework);

    }

    @Override
    public  List<String> loadDataFromNetwork() throws Exception {
        AndroidFrameworkFileIO d = new AndroidFrameworkFileIO(contextReference.get());
        return d.loadClassListIntoMemory(frameworkPath, preloadedClassFilename);
    }
}
