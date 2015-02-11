package ktwz.sandboxed.fingerprint;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ktwz.sandboxed.R;
import ktwz.sandboxed.model.APICall;

/**
 * Handles the file IO for obtaining the class list for available API.
 *
 * Android has the framework.jar which is the client interface to access Android through the API.
 * We extract the preloaded_classes file which is a text file including every class available
 * to the client. This allows us to automatically scan the API for a given device.
 *
 * Created by wil on 1/29/15.
 */
public class AndroidFrameworkFileIO {

    private static final String TAG = AndroidFrameworkFileIO.class.getName();

    private List<String> preloadedClasses = new ArrayList<String>();
    private Context context;

    public AndroidFrameworkFileIO(Context context){
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

    /**
     * Given the location to the preloaded class file, load into a List
     * @param preloadedClassFile
     * @return
     */
    public List<String> loadPreloadedClassList(String preloadedClassFile){
        FileInputStream fin = null;
        try {
            fin = context.openFileInput(preloadedClassFile);
            preloadedClasses.addAll(IOUtils.readLines(fin));
            removeComments();
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

    private void removeComments(){
        Iterator<String> it = preloadedClasses.iterator();
        while (it.hasNext()){
            String line = it.next();
            if (line.startsWith("#")){
                it.remove();
            }
        }
    }

    /**
     * Extract file from zip
     *
     * @param zipFile
     * @param fileToExtract
     */
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


    public void compress(File inputFile, File outputFile){
        FileInputStream in = null;
        ZipOutputStream out = null;
        try {
            // input file
            in = new FileInputStream(inputFile);

            // out put file
            out = new ZipOutputStream(new FileOutputStream(outputFile));

            // name the file inside the zip  file
            out.putNextEntry(new ZipEntry(inputFile.getName()));

            // buffer size
            byte[] b = new byte[1024];
            int count;

            while ((count = in.read(b)) > 0) {
                out.write(b, 0, count);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null){
                try {in.close();} catch(Exception e){}
            }
            if (out != null){
                try {out.close();} catch(Exception e){}
            }
        }

    }

    public void exportHashToFile(Hashtable<String,String> apis){


        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            //TODO throws
            // Toast.makeText(this, "External SD card not mounted", Toast.LENGTH_LONG).show();
            return;
        }

        //TODO add some sort of loading thing
        FileOutputStream fos = null;
        File file = getExportFile();

        try {
            fos = new FileOutputStream(file);

            Enumeration<String> enumeration = apis.keys();
            while(enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                String value = apis.get(key);
                String line = key + "=" + value + "\n";
                fos.write(line.getBytes());
            }

        } catch (IOException e){
            Log.e(TAG, e.getMessage());
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



    public File getExportFile() {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + File.separator + context.getString(R.string.app_name));
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String version = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo( context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //+ "-" + System.currentTimeMillis() +
        String filename =  context.getString(R.string.app_name) + "_" + version + "__" + Build.FINGERPRINT.replaceAll("/","__").replaceAll(":", "+") + ".txt";
        return new File(dir, filename);
    }

    public List<String> loadClassListIntoMemory(String pathToJar, String jarFilename){
        JarFile jarFile = null;

        try {
            jarFile = new JarFile(pathToJar);

            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (entry.getName().equals(jarFilename)) {
                    JarEntry fileEntry = jarFile.getJarEntry(entry.getName());
                    return processPreloadedClassFileIntoMemory(jarFile, fileEntry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (jarFile != null){
                try {
                    jarFile.close();
                } catch (IOException e) {}
            }
        }
        return null;
    }

    private List<String> processPreloadedClassFileIntoMemory( JarFile jarFile, JarEntry fileEntry) throws IOException {
//
        List<String> calls = new ArrayList<String>();

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader in = null;

        try {
            is = jarFile.getInputStream(fileEntry);
            isr = new InputStreamReader(is);
            in = new BufferedReader(isr);


            while (in.ready()) {
                String line = in.readLine();
                if (line.startsWith("#")) {
                    continue;
                }

                calls.add(line);
            }


        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }

        }
        return calls;

    }

    public void loadHashtableIntoDatabase(Hashtable<String,String> apis){


        Enumeration<String> enumeration = apis.keys();
        while(enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            String value = apis.get(key);
            APICall call = new APICall(key, value);
            call.save();
        }
    }
    public void loadClassListIntoDatabase(String pathToJar, String jarFilename){
        JarFile jarFile = null;

        try {
            jarFile = new JarFile(pathToJar);

            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                    if (entry.getName().equals(jarFilename)) {
                        JarEntry fileEntry = jarFile.getJarEntry(entry.getName());
                        processPreloadedClassFile(jarFile, fileEntry);
                        break;
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (jarFile != null){
                try {
                    jarFile.close();
                } catch (IOException e) {}
            }
        }

    }

    private void processPreloadedClassFile( JarFile jarFile, JarEntry fileEntry) throws IOException {
        APICallScanner scanner = new APICallScanner(context);
//
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader in = null;

        try {
            is = jarFile.getInputStream(fileEntry);
            isr = new InputStreamReader(is);
            in = new BufferedReader(isr);

            while (in.ready()) {
                String line = in.readLine();
                if (line.startsWith("#")) {
                    continue;
                }

                scanner.fullScan(line);

            }


        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }

        }

    }

}
