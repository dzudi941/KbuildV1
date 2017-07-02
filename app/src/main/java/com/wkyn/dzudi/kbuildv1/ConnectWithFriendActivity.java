package com.wkyn.dzudi.kbuildv1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import android.os.Message;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ConnectWithFriendActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;
    static final int REQUEST_ENABLE_BT =1;
    Button paired_devices, search_for_devices, listen_connection;
    ArrayList<String> pairedDevicesStr;
    ListView devices_list;
    KBluetoothService kbs;
    Button test_send;
    FirebaseUser user;
    String mConnectedDeviceName;
    //DatabaseReference currentLocationRef;
    DatabaseReference friendsListRef, friendRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    ListView friends_list;
    //CurrentLocationModel clm;
    FriendsModel fm;
    String keyToFriends = "123";
    ArrayList<String> myFriends;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_with_friend);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pairedDevicesStr = new ArrayList<String>();
        kbs=new KBluetoothService(ConnectWithFriendActivity.this, mHandler);
        test_send = (Button) findViewById(R.id.test_uuid);
        user = FirebaseAuth.getInstance().getCurrentUser();
        test_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = user.getEmail(); //"Cao ja sam lijen u komunikacijuuu";
                byte[] send = message.getBytes();
                kbs.write(send);
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
        {
            finish();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableBlInt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlInt, REQUEST_ENABLE_BT);
        }
        devices_list = (ListView) findViewById(R.id.connect_with_friend_devices_list);
        /*paired_devices = (Button) findViewById(R.id.connect_with_friend_paired_devices);
        paired_devices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {*/
                pairedDevicesStr=selectServer();
                devices_list.setAdapter(new ArrayAdapter<String>(ConnectWithFriendActivity.this, android.R.layout.simple_list_item_1, pairedDevicesStr));
           /* }
        });*/

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        search_for_devices = (Button) findViewById(R.id.connect_with_friend_search_devices);
        search_for_devices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesStr.clear();
                bluetoothAdapter.startDiscovery();
            }
        });
        devices_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(parent.getItemAtPosition(position).toString());
               /* ConnectThread ct = new ConnectThread(device);
                ct.start();*/
               kbs.connect(device, true);



            }
        });
        listen_connection = (Button) findViewById(R.id.connect_with_friend_listen_connections);
        listen_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AcceptThread at = new AcceptThread();
                //new Thread(new AcceptThread()).start();
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
                /*AcceptThread at = new AcceptThread();
                at.start();*/

                kbs.start();
            }
        });
        //clm = new CurrentLocationModel();
        fm = new FriendsModel(user.getEmail());
        friends_list = (ListView) findViewById(R.id.connect_friend_list_of_friends);
        //currentLocationRef = database.getReference("CurrentLocationOfUsers");
        friendsListRef = database.getReference("myFriendsList");
        Query query = friendsListRef.orderByChild("email").equalTo(user.getEmail());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    GenericTypeIndicator<FriendsModel> t = new GenericTypeIndicator<FriendsModel>() {};


                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        keyToFriends = ds.getKey();
                        fm = ds.getValue(t);
                        fm.updateFriendsList(ds.getValue(t).getFriends());
                    }
                    myFriends = fm.getFriends();
                    friends_list.setAdapter(new ArrayAdapter<String>(ConnectWithFriendActivity.this, android.R.layout.simple_list_item_1, myFriends));

                }
                else{
                    friendRef=friendsListRef.push();
                    keyToFriends=friendRef.getKey();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(ConnectWithFriendActivity.this, device.getName(), Toast.LENGTH_SHORT).show();
                pairedDevicesStr.add(device.getAddress());
                devices_list.setAdapter(new ArrayAdapter<String>(ConnectWithFriendActivity.this, android.R.layout.simple_list_item_1, pairedDevicesStr));
            }
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(requestCode==REQUEST_ENABLE_BT && resultCode== Activity.RESULT_OK) {
             BluetoothAdapter BT = BluetoothAdapter.getDefaultAdapter();
             String address = BT.getAddress(); String name = BT.getName();
             String toastText = name + " : " + address;
             Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
         }
         else{
             Toast.makeText(ConnectWithFriendActivity.this, requestCode, Toast.LENGTH_SHORT).show();
         }
    }
    private ArrayList<String> selectServer(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<String> pairedDevicesString = new ArrayList<String>();
        if (pairedDevices.size() > 0){
            for (BluetoothDevice device : pairedDevices){
                pairedDevicesString.add(/*device.getName() + " address: " + */device.getAddress()/* + "   Paired"*/);
            }
        }
        return pairedDevicesString;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ConnectWithFriendActivity activity = ConnectWithFriendActivity.this;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case KBluetoothService.STATE_CONNECTED:
                            Toast.makeText(ConnectWithFriendActivity.this, "Status: connected! ", Toast.LENGTH_SHORT).show();
                            String message = user.getEmail(); //"Cao ja sam lijen u komunikacijuuu";
                            byte[] send = message.getBytes();
                            kbs.write(send);
                            break;
                        case KBluetoothService.STATE_CONNECTING:
                            Toast.makeText(ConnectWithFriendActivity.this, "Status: connecting! ", Toast.LENGTH_SHORT).show();
                            break;
                        case KBluetoothService.STATE_LISTEN:
                            Toast.makeText(ConnectWithFriendActivity.this, "Status: listen! ", Toast.LENGTH_SHORT).show();
                        case KBluetoothService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Toast.makeText(activity, "I send my email: "+ writeMessage, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    Toast.makeText(activity, "I receive: "+ readMessage, Toast.LENGTH_SHORT).show();
                    if(fm.addFriend(readMessage)){
                        Toast.makeText(activity, "You are now friend with "+ readMessage, Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(activity, readMessage+" is already your friend", Toast.LENGTH_LONG).show();
                    }
                    friendsListRef.child(keyToFriends).setValue(fm);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

}
