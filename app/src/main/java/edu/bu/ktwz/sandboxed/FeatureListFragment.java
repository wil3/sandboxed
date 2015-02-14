package edu.bu.ktwz.sandboxed;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;
import edu.bu.ktwz.sandboxed.model.DetectionMethod;
import edu.bu.ktwz.sandboxed.model.Param;

@Deprecated
public class FeatureListFragment extends RoboFragment {

    private static final String TAG = FeatureListFragment.class.getName();

    private ArrayAdapter<DetectionMethod> adapter;
    private ArrayList<DetectionMethod> detectionMethods = new ArrayList<DetectionMethod>();

    @InjectView(R.id.list_features) private ListView featureList;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FeatureListFragment.
     */
    public static FeatureListFragment newInstance() {
        FeatureListFragment fragment = new FeatureListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public FeatureListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feature_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ListFeatureAdapter(getActivity().getApplicationContext(), R.layout.item_list_feature, detectionMethods);
        featureList.setAdapter(adapter);

        detect();
    }

    private void detect(){
        DataProcessor processor = new DataProcessor();

        List<DetectionMethod> detectors = processor.process(getActivity().getApplicationContext(), R.raw.static_signatures);
        Detector d = new Detector(detectors);
        d.run();

        detectionMethods.addAll(d.detectors);
        adapter.notifyDataSetChanged();


/*
        for (DetectionMethod detection : detectors) {
            String className = detection.getHandlerClass();


            try {
                Class clazz = Class.forName(className);
                Constructor constructor = clazz.getConstructor(getConstructorParams(detection));
                SandboxDetection sd = (SandboxDetection)constructor.newInstance(getConstructorValues(detection));

                detection.setDetected(sd.inSandbox());

                detectionMethods.add(detection);
                adapter.notifyDataSetChanged();

            } catch (java.lang.InstantiationException e) {
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
        }*/
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
            //    objects[i] = params.get(i).getVal();
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

  /*
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
*/


    public class ListFeatureAdapter extends ArrayAdapter<DetectionMethod> {
        private ItemHolder holder = null;
        private final int viewResourceId;
        private Context context;
        private final List<DetectionMethod> items;

        public ListFeatureAdapter(Context context, int viewResourceId, List<DetectionMethod> items) {
            super(context, viewResourceId, items);
            this.viewResourceId = viewResourceId;
            this.context = context;
            this.items = items;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            final DetectionMethod detectionMethod = items.get(position);
            LayoutInflater inflater = LayoutInflater.from(context);

            if (row == null){
                row = inflater.inflate(viewResourceId, parent, false);
                holder = new ItemHolder();
                holder.textLabel = (TextView)row.findViewById(R.id.textLabel);
                holder.textValue = (TextView)row.findViewById(R.id.textValue);
                row.setTag(holder);
            } else {
                holder = (ItemHolder)row.getTag();
            }

            holder.textLabel.setText(detectionMethod.getName());
            holder.textValue.setText(detectionMethod.getStringResult());

            return row;
        }
    }

    private static class ItemHolder {
        TextView textLabel;
        TextView textValue;
    }

}
