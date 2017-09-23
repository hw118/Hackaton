package capgemini.infrateam.hackaton;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by harriewijnans on 22/09/2017.
 */

public class Registration extends AppCompatActivity {
    private EditText et_name;
    MeetingDB myDB = new MeetingDB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_layout);
        et_name = (EditText) findViewById(R.id.myname);
        Button bt = (Button) findViewById(R.id.connect);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myName = et_name.getText().toString();
                if (myName.isEmpty()) return;
                // do make connection

                Meeting mm = new Meeting();
                myDB.startTransaction();
                mm.setName(myName);
                mm.setId(myDB.setMeeting(mm));
                myDB.setLast(mm.getId());
                myDB.commit();
                myDB.close();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

}
