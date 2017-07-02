package com.wkyn.dzudi.kbuildv1;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class LocationTrackerActivity extends AppCompatActivity {

    Button btnStopService;
    ToggleButton service_on_off;
    TextView txtMsg;
    Intent intentMyService;
    ComponentName service;
    BroadcastReceiver receiver;
    public static final String BROADCAST_ACTION = "MyCurrentAction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_tracker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        service_on_off = (ToggleButton) findViewById(R.id.location_tracker_service_on_off);
        txtMsg= (TextView) findViewById(R.id.location_tracker_text_output);
        // initiate the service
        intentMyService= new Intent(this, LocationService.class);

        service_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    service= startService(intentMyService);
                    txtMsg.setText("Location Tracker Started: Location Service");
                }
                else
                {
                    try{
                        stopService(new Intent(intentMyService) );
                        txtMsg.setText("After stoping Service: \n" + service.getClassName());
                        btnStopService.setText("Finished");
                        btnStopService.setClickable(false);
                    } catch(Exception e) {
                        Log.e("MYGPS", e.getMessage() );
                    }
                }
            }
        });
        txtMsg.setText("Click on button to start service:");
        // register & define filter for local listener
        IntentFilter mainFilter= new IntentFilter(BROADCAST_ACTION);

        //registerReceiver(mReceiver, mainFilter);
        /*btnStopService= (Button) findViewById(R.id.location_tracker_stop_service);

        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                try{
                    stopService(new Intent(intentMyService) );
                    txtMsg.setText("After stopingService: \n" + service.getClassName());
                    btnStopService.setText("Finished");
                    btnStopService.setClickable(false);
                } catch(Exception e) {
                    Log.e("MYGPS", e.getMessage() );
                }
            }
        });*/
    }
    /*final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
           Double a =  intent.getDoubleExtra("Latitude", -1);
            //txtMsg.setText(a);
            Toast.makeText(LocationTrackerActivity.this,"latitiude: " + a, Toast.LENGTH_SHORT).show();
        }
    };*/
   /* @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            stopService(intentMyService);
            unregisterReceiver(receiver);
        } catch(Exception e) {
            Log.e("MAIN-DESTROY>>>", e.getMessage() );
        }
        Log.e("MAIN-DESTROY>>>", "Adios");
    }// onDestroy*/

}
