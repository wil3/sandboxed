package edu.bu.ktwz.sandboxed.request;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.octo.android.robospice.request.SpiceRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import edu.bu.ktwz.sandboxed.R;
import edu.bu.ktwz.sandboxed.fingerprint.AndroidFrameworkFileIO;
import edu.bu.ktwz.sandboxed.model.APICall;

/**
 * Created by wil on 2/17/15.
 */
public class ExportRequest extends SpiceRequest<String> {

    private final WeakReference<Context> contextReference;

    public ExportRequest(Context context){
        super(String.class);
        this.contextReference = new WeakReference<Context>(context);

    }

    @Override
    public String loadDataFromNetwork() throws Exception {

        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            throw new Exception("Not mounted");
        }

        //TODO add some sort of loading thing
        FileOutputStream fos = null;
        File file = new AndroidFrameworkFileIO(contextReference.get()).getExportFile();

        try {
            fos = new FileOutputStream(file);

            Cursor cursor = APICall.fetchResultCursor();
            cursor.moveToFirst();
            while(cursor.isAfterLast() == false){
                String callName = cursor.getString(cursor.getColumnIndexOrThrow("class"));
                String value = cursor.getString(cursor.getColumnIndexOrThrow("value"));

                String line = callName + "=" + value + "\n";
                fos.write(line.getBytes());

                cursor.moveToNext();
            }
            cursor.close();

            return file.getAbsolutePath();

        } finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
