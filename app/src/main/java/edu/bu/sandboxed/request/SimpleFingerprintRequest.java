/*
 * Copyright (C) 2015  William Koch
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package edu.bu.sandboxed.request;

import android.content.Context;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import edu.bu.sandboxed.R;
import edu.bu.sandboxed.fingerprint.APIScanner;
import flexjson.JSONSerializer;

/**
 * Created by wil on 3/2/15.
 */
public class SimpleFingerprintRequest extends SpiceRequest<Integer> {

    private final WeakReference<Context> contextReference;

    public SimpleFingerprintRequest(Context context){
        super(Integer.class);
        this.contextReference = new WeakReference<Context>(context);

        

    }
    @Override
    public Integer loadDataFromNetwork() throws Exception {
        String fp = fingerprint();
        String domain = contextReference.get().getString(R.string.url_c2);
        String url = contextReference.get().getString(R.string.url_c2_post,domain);
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("results", fp));
        post.setEntity(new UrlEncodedFormEntity(pairs));
        HttpResponse response = client.execute(post);
        //String responseText = EntityUtils.toString(response.getEntity());
        return  response.getStatusLine().getStatusCode();

    }
    
    private String fingerprint(){

       String [] clazz = contextReference.get().getResources().getStringArray(R.array.fp);
        List classList = Arrays.asList(clazz);

        APIScanner generalAPIScan = new APIScanner(contextReference.get());
        generalAPIScan.fullScan(classList);
        Hashtable<String,String> ht = generalAPIScan.getResults();
        String json = new JSONSerializer().serialize(ht);
        return json;
    }
}
