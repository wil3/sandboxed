package edu.bu.ktwz.sandboxed.fingerprint;

import android.app.Application;
import android.content.Context;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;


/**
 * Created by wil on 2/16/15.
 */
public class OfflineSpiceService extends SpiceService{

        @Override
        public CacheManager createCacheManager( Application application ) {
            return new CacheManager();
        }
        @Override
        protected NetworkStateChecker getNetworkStateChecker() {
            return new NetworkStateChecker() {
                @Override
                public boolean isNetworkAvailable( Context context ) {
                    return true;
                }
                @Override
                public void checkPermissions( Context context ) {
                    // do nothing
                }
            };
        }

}
