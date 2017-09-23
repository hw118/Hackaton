package capgemini.infrateam.hackaton;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by harriewijnans on 23/09/2017.
 */

public class NewAppointment extends AppCompatActivity {
    private static final String TAG = "NewAppointment";
    private static String THEURL = "http://ec2-54-202-189-196.us-west-2.compute.amazonaws.com/data/data.php";
    JSONArray jsonArray = null;
    LayoutInflater inflater;
    ListView listView;
    TextView tvError;
    MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newappointment);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listView = (ListView) findViewById(R.id.lv_contact);
        tvError = (TextView) findViewById(R.id.na_error);
        myAdapter = new MyAdapter();
        listView.setAdapter(myAdapter);
        getData(THEURL);
    }

    private void getData(String u) {
        AsyncTask<String, String, String> getTask = new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                String response = "";
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection)
                            url.openConnection();
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line = "";
                    while ((line = reader.readLine())!=null) {
                        response += line + "\n";
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                return response;
            }

            protected void onPostExecute(String result) {
                try {
                    jsonArray = new JSONArray(result);
                    tvError.setText("");
                    updateScreen();
                } catch (Exception e) {
                    tvError.setText(result);
                    Log.e(TAG, e.getMessage());
                }
            }
        };
        getTask.execute(u);
    }

    private void updateScreen() {
        myAdapter.notifyDataSetChanged();
    }

    private class MyAdapter extends BaseAdapter {

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            if (jsonArray==null) return 0;
            return jsonArray.length();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public Object getItem(int position) {
            if (jsonArray==null) return null;
            try {
                return jsonArray.get(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return 0;
        }

        class Holder  {
            TextView tv_key;
            TextView tv_val;
        }
        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position    The position of the item within the adapter's data set of the item whose view
         *                    we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         *                    is non-null and of an appropriate type before using. If it is not possible to convert
         *                    this view to display the correct data, this method can create a new view.
         *                    Heterogeneous lists can specify their number of view types, so that this View is
         *                    always of the right type (see {@link #getViewTypeCount()} and
         *                    {@link #getItemViewType(int)}).
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView==null) {
                convertView = inflater.inflate(R.layout.app_rij, parent, false);
                holder = new Holder();
                holder.tv_key = (TextView) convertView.findViewById(R.id.r_key);
                holder.tv_val = (TextView) convertView.findViewById(R.id.r_val);
            } else {
                holder = (Holder) convertView.getTag();
            }
            try {
                holder.tv_key.setText(jsonArray.getJSONObject(position).getString("key"));
                holder.tv_val.setText(jsonArray.getJSONObject(position).getString("val"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }
}
