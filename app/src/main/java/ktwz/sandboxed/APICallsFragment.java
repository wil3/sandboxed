package ktwz.sandboxed;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

import ktwz.sandboxed.model.APICall;
import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;


public class APICallsFragment extends RoboFragment {


    @InjectView(R.id.input_search)  private EditText filterText;
    @InjectView(R.id.list_apicalls) private ListView apiList;

    public APICallsFragment() {
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
        return inflater.inflate(R.layout.fragment_apicalls, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Cursor c = APICall.fetchResultCursor();
        final APICallCursorAdapter adapter = new APICallCursorAdapter(getActivity().getApplicationContext(),c);
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return APICall.fetchResultCursor(constraint.toString());
            }
        });
        apiList.setAdapter(adapter);

        filterText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                adapter.getFilter().filter(cs);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
    }



    class APICallCursorAdapter extends CursorAdapter {
        public APICallCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_list_apicall, parent, false);
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView tvCall = (TextView)view.findViewById(R.id.textCall);
            TextView tvValue = (TextView)view.findViewById(R.id.textValue);
            TextView tvClassName = (TextView)view.findViewById(R.id.textPackage);
            //TODO shoudnt activeandroid be able to extract into the model?
            //String packageName = cursor.getString(cursor.getColumnIndexOrThrow("package"));
            String callName = cursor.getString(cursor.getColumnIndexOrThrow("class"));
            //String accessName = cursor.getString(cursor.getColumnIndexOrThrow("access"));
            String value = cursor.getString(cursor.getColumnIndexOrThrow("value"));

            tvClassName.setText(APICall.getPackageName(callName));
            tvCall.setText(APICall.getSimpleClassName(callName));
            tvValue.setText(value);
        }
    }
}