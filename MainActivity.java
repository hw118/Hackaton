package capgemini.infrateam.hackaton;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private static int REGISTRATION = 123;
    private static int SELECTION = 456;
    Meeting myMeeting;
    MeetMeSettings mySettings;
    MeetingDB myDB = new MeetingDB(this);
    TextView myMeetingName;
    private int nrMeetings = 0;
    MenuItem selectMeetings;
    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myMeetingName = (TextView) findViewById(R.id.main_name);
        Button btNwApp = (Button) findViewById(R.id.new_appointment);
        btNwApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newAppointment();
            }
        });
        drawScreen();
    }

    @Override
    protected void onResume() {
        setMenu();
        super.onResume();
    }

    private void newAppointment() {
        if (myDB!=null) myDB.close();
        startActivityForResult(new Intent(getApplicationContext(), NewAppointment.class), SELECTION);
    }
    private void startRegistration() {
        if (myDB!=null) myDB.close();
        startActivityForResult(new Intent(getApplicationContext(), Registration.class), REGISTRATION);
    }

    private ArrayList<Meeting> meetings;

    private void selectMeeting() {
        if (myDB == null) {
            myDB = new MeetingDB(this);
        }
        meetings = myDB.getMeetings();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.slcthdr);
        builder.setAdapter(new SelectAdapter(meetings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myDB.setLast(meetings.get(which).getId());
                drawScreen();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    class Holder {
        TextView txt;
        int id;
    }



    class SelectAdapter extends BaseAdapter {

        private ArrayList<Meeting> meetings;

        SelectAdapter(ArrayList<Meeting> mm) {
            meetings = mm;
        }
        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            if (meetings == null) return 0;
            return meetings.size();
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
            if (meetings == null || position<0 || position >=meetings.size()) return null;
            return meetings.get(position);
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
                convertView = inflater.inflate(R.layout.rij, parent, false);
                holder = new Holder();
                holder.txt = (TextView) convertView.findViewById(R.id.rij_id);
            } else {
                holder = (Holder) convertView.getTag();
            }

            holder.id = meetings.get(position).getId();
            holder.txt.setText(meetings.get(position).getName());
            convertView.setTag(holder);
            return convertView;
        }
    }

    private void drawScreen() {
        myDB.startTransaction(false);
        mySettings = myDB.getSettings();
        if (mySettings.lastMeeting >= 0) {
            myMeeting = myDB.getMeeting(mySettings.lastMeeting);
        }
        nrMeetings = myDB.countMeetings();
        if (selectMeetings!=null) selectMeetings.setEnabled(nrMeetings>1);

        myDB.commit();
        myDB.close();
        if (myMeeting==null) {
            startRegistration();
        } else {
            Log.d(TAG,"myMeeting.name='"+myMeeting.getName()+"'");
            myMeetingName.setText(myMeeting.getName());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"onActivityResult:"+resultCode+" requestCode="+requestCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == REGISTRATION) {
                Log.d(TAG, "REGISTRATION");
                if (myDB==null) {
                    myDB = new MeetingDB(this);
                }
                myDB.startTransaction(false);
                drawScreen();
                myDB.commit();
                myDB.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        selectMeetings = menu.findItem(R.id.mnu_selectmeeting);
        setMenu();
        return true;
    }

    private void setMenu() {
        if (selectMeetings!=null) selectMeetings.setEnabled(nrMeetings>1);
    }

    @Override
    public void onStop() {
        if (myDB!=null) {
            myDB.close();
        }
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnu_nmeeting: startRegistration();
                return true;
            case R.id.mnu_selectmeeting: selectMeeting();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

}
