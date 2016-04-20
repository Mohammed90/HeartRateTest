package com.alejandro_castilla.heartratetest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    private SyncDataService syncDataService;
    private TextView heartRateTextView;
    private Intent syncServiceIntent;

    /* Messenger for this activity */

    private final Messenger mainActivityMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SyncDataService.HEART_RATE_DATA:
                    heartRateTextView.setText(msg.getData().getString("heartratestring"));
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        heartRateTextView = (TextView) findViewById(R.id.heartratetext);
        syncServiceIntent = new Intent(MainActivity.this, SyncDataService.class);
        syncServiceIntent.putExtra("mainactivitymessenger", mainActivityMessenger);
        startService(syncServiceIntent);
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        stopService(syncServiceIntent);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopService(syncServiceIntent);
        super.onDestroy();
    }
}
