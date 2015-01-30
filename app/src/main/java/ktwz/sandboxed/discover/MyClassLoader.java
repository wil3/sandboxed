package ktwz.sandboxed.discover;

import android.util.Log;

/**
 * Created by wil on 1/29/15.
 */
public class MyClassLoader  extends  ClassLoader {


    public void getPackage(){
        Package p = getPackage("android");
        Log.d("MyClassLoader", p.toString());
    }
}
