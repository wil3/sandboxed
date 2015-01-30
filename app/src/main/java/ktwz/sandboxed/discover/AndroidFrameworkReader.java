package ktwz.sandboxed.discover;

import android.content.Context;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by wil on 1/29/15.
 */
public class AndroidFrameworkReader {

    private static final String TAG = AndroidFrameworkReader.class.getName();
    private static String ROOT_PACKAGE = "android";
    private List<String> preloadedClasses = new ArrayList<String>();
    Context context;

    public AndroidFrameworkReader(Context context){
        this.context = context;
    }

    public void copy(String source, String target){

        if (new File("framework.zip").exists())
            return;

    File file = new File(source);

        InputStream input = null;
        OutputStream output = null;
    try {
        input = new BufferedInputStream(new FileInputStream(file));
        //output = new BufferedOutputStream(new FileOutputStream("framework.zip"));
        FileOutputStream fos = context.openFileOutput(target, Context.MODE_PRIVATE);

        IOUtils.copy(input, fos);

    } catch (Exception e){
        Log.d(TAG, e.getMessage());
    } finally {
        if (input != null){
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (output != null){
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    }

    public List<String> loadPreloadedClassList(String preloadedClassFile){
        FileInputStream fin = null;
        try {
            fin = context.openFileInput(preloadedClassFile);
            preloadedClasses.addAll(IOUtils.readLines(fin));
            cleanOutComments();
        } catch(Exception e){
            e.printStackTrace();

        } finally {
            if (fin != null){
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return preloadedClasses;
    }

    private void cleanOutComments(){
        Iterator<String> it = preloadedClasses.iterator();
        while (it.hasNext()){
            String line = it.next();
            if (line.startsWith("#")){
                it.remove();
            }
        }
    }

    public void extract (String zipFile, String fileToExtract){
        FileOutputStream out = null;
        FileInputStream fin = null;
        ZipInputStream zin = null;
        try {
            out = context.openFileOutput(fileToExtract, Context.MODE_PRIVATE);
            fin = context.openFileInput(zipFile);
            BufferedInputStream bin = new BufferedInputStream(fin);
            zin = new ZipInputStream(bin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                if (ze.getName().equals(fileToExtract)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zin.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fin != null){
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (zin != null){
                try {
                    zin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
