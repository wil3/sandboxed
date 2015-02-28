package edu.bu.ktwz.sandboxed.request;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import com.octo.android.robospice.request.SpiceRequest;

import org.apache.commons.io.IOUtils;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.bu.ktwz.sandboxed.R;

/**
 * Created by wil on 2/27/15.
 */
public class PingRequest extends SpiceRequest<String> {

    private final WeakReference<Context> contextReference;

    public PingRequest(Context context) {
        super(String.class);
        this.contextReference = new WeakReference<Context>(context);

    }
    @Override
    public String loadDataFromNetwork() throws Exception {
// With Uri.Builder class we can build our url is a safe manner
        String domain = contextReference.get().getString(R.string.url_c2);
        String fqdn = contextReference.get().getString(R.string.url_c2_ping,domain);
        Uri.Builder uriBuilder = Uri.parse(
                fqdn).buildUpon();

        String url = uriBuilder.build().toString();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url)
                .openConnection();
        String result = IOUtils.toString(urlConnection.getInputStream());
        urlConnection.disconnect();
        return result;
    }
}
