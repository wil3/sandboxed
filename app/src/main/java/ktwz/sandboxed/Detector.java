package ktwz.sandboxed;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import ktwz.sandboxed.detect.SandboxDetection;
import ktwz.sandboxed.model.DetectionMethod;
import ktwz.sandboxed.model.Param;

/**
 * This will run all sandbox detections
 * Created by wil on 1/28/15.
 */
public class Detector {
    private static final String TAG = Detector.class.getName();

    List<DetectionMethod> detectors;

    public Detector(List<DetectionMethod> detectors){
        this.detectors = detectors;
    }

    public void run(){
        for (DetectionMethod detection : detectors) {
            String className = detection.getHandlerClass();


            try {
                Class clazz = Class.forName(className);
                Constructor constructor = clazz.getConstructor(getConstructorParams(detection));
                SandboxDetection sd = (SandboxDetection)constructor.newInstance(getConstructorValues(detection));

                detection.setDetected(sd.inSandbox());
                detection.setStringResult(sd.getFoundString());


            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch(NoSuchMethodException e) {
                Log.e(TAG, "Incorrect number of parameters, check parameters for handler.");
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Class not found.");
            }
        }
    }

    private Class [] getConstructorParams(DetectionMethod detection){
        List<Param> params = detection.getParams();
        Class [] classes = new Class[params.size()];

        for (int i=0; i<params.size(); i++){
            try {

                classes[i] = Class.forName(params.get(i).getType());
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Class not found.");
            }
        }

        return classes;
    }

    private Object[] getConstructorValues(DetectionMethod detection){
        List<Param> params = detection.getParams();
        Object[] objects = new Object[params.size()];
        for (int i=0; i<params.size(); i++){
                objects[i] = params.get(i).getVal();
        }
        return objects;
    }

    Object getFieldValue(String path) throws  ClassNotFoundException, NoSuchFieldException, IllegalAccessException{
        int lastDot = path.lastIndexOf(".");
        String className = path.substring(0, lastDot);
        String fieldName = path.substring(lastDot + 1);
        Class myClass = Class.forName(className);
        Field myField = myClass.getDeclaredField(fieldName);
        return myField.get(null);
    }
}
