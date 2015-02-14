package edu.bu.ktwz.sandboxed;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by wil on 1/25/15.
 */
@Deprecated
public class StaticDataLoader {


    /**
     * Load resource by its raw resource ID
     *
     * @param resourceId
     * @param context
     * @return
     */
    public String loadStringFromResource( Context context, int resourceId){

        InputStream is = context.getResources().openRawResource(resourceId);
        StringBuilder sb = new StringBuilder();
        if (is != null){
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
                String line;
                while ((line = br.readLine()) != null){
                    sb.append(line);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     *
     * @param resourcePath Fully qualified path to the resources starting at the root
     * @return
     */
    public String loadStringFromResource(String resourcePath){

        InputStream is = getClass().getResourceAsStream(resourcePath);
        StringBuilder sb = new StringBuilder();
        if (is != null){
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
                String line;
                while ((line = br.readLine()) != null){
                    sb.append(line);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
