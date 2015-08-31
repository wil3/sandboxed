package edu.bu.sandboxed.request;

import android.content.Context;
import android.os.Build;

import com.octo.android.robospice.request.SpiceRequest;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.bu.sandboxed.R;
import edu.bu.sandboxed.fingerprint.AndroidFrameworkFileIO;

/**
 * Created by wil on 2/16/15.
 *
 * https://source.android.com/devices/tech/dalvik/configure.html#preloaded_classes_list
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

        //This changed with ART
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return d.loadPreloadedClassList(contextReference.get().getString(R.string.path_preloaded_classes));
        } else {
            return d.loadClassListIntoMemory(frameworkPath, preloadedClassFilename);
        }
    }
}
