package edu.bu.sandboxed;

import android.util.Log;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;

import roboguice.util.temp.Ln;

/**
 * Created by wil on 2/26/15.
 */
public class MySpiceManager extends SpiceManager {
    public MySpiceManager(Class<? extends SpiceService> spiceServiceClass) {
        super(spiceServiceClass);
        Ln.getConfig().setLoggingLevel(Log.ERROR);
    }
}
