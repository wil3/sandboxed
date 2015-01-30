package ktwz.sandboxed;


import android.content.Context;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import flexjson.JSONDeserializer;
import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import ktwz.sandboxed.model.DetectionMethod;

/**
 * Created by wil on 1/25/15.
 */
public class DataProcessor {

    public List<DetectionMethod> process(Context context, int resource){
        StaticDataLoader loader = new StaticDataLoader();
        String data = loader.loadStringFromResource(context, resource);

        return new JSONDeserializer<List<DetectionMethod>>()
                .use("values", DetectionMethod.class)
              //  .use("params", ArrayList.class)
              //  .use("params.values", Param.class)
                //Need to force deseralize to Object
                .use("values.params.values.val",  new ObjectFactory() {
                    @Override
                    public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
                        return value;
                    }
                })
                .deserialize(data, ArrayList.class);



    }
}
