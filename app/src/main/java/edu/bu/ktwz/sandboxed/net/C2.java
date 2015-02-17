package edu.bu.ktwz.sandboxed.net;

import android.content.Context;
import android.util.Log;

import java.io.File;

import edu.bu.ktwz.sandboxed.fingerprint.AndroidFrameworkFileIO;

/**
 * Created by wil on 2/11/15.
 */
public class C2 {

    private static final String TAG = C2.class.getName();
    private Context context;

    public C2(Context context){
        this.context = context;
    }

    public String uploadFingerprint(){
        //First compress then send
        AndroidFrameworkFileIO io = new AndroidFrameworkFileIO(context);

        File export = io.getExportFile();

        File output = new File(export.getParent(), makeZipName(export));

        io.compress(export, output );

        FileTransfer ft = new FileTransfer(context);
        try {
            return ft.upload(output);
        } catch (FileTransferException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    private String makeZipName(File file){
        int lastDot = file.getName().lastIndexOf(".");
        String fileName = file.getName().substring(0, lastDot);
        return fileName + ".zip";
    }
}
