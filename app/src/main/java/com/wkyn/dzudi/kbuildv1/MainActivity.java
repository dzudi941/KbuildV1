package com.wkyn.dzudi.kbuildv1;

        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.pm.PackageManager;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.location.Location;
        import android.os.Bundle;
        import android.os.Environment;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.support.design.widget.FloatingActionButton;
        import android.support.design.widget.Snackbar;
        import android.support.v4.app.ActivityCompat;
        import android.text.Editable;
        import android.text.TextWatcher;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.support.design.widget.NavigationView;
        import android.support.v4.view.GravityCompat;
        import android.support.v4.widget.DrawerLayout;
        import android.support.v7.app.ActionBarDrawerToggle;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.ViewGroup;
        import android.view.Window;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.CompoundButton;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.ListView;
        import android.widget.RelativeLayout;
        import android.widget.Spinner;
        import android.widget.TextView;
        import android.widget.Toast;
        import android.widget.ToggleButton;
        import android.widget.ViewFlipper;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.location.LocationListener;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;
        import com.google.android.gms.tasks.OnFailureListener;
        import com.google.android.gms.tasks.OnSuccessListener;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.auth.UserInfo;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.GenericTypeIndicator;
        import com.google.firebase.database.Query;
        import com.google.firebase.database.ValueEventListener;
        import com.google.firebase.storage.FileDownloadTask;
        import com.google.firebase.storage.FirebaseStorage;
        import com.google.firebase.storage.StorageReference;
        import com.google.gson.Gson;
        import com.google.gson.reflect.TypeToken;

        import java.io.File;
        import java.lang.reflect.Array;
        import java.lang.reflect.Type;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference typeOfObjectRef;
    StorageReference myFriendsRef;
    DatabaseReference allPlaces;
    Spinner typeOfObjectDropDown;
    Button list_map_button;

    String list_map_button_text = "Map View";
    Integer list_map_button_switch = 0;
    ViewFlipper place_for_content;
    Integer current_contet_shown = 0;
    ListView places_list_view;
    List<Place> placesList;
    private HashMap<Marker, Integer> markerPlaceIdMap;
    private GoogleMap map;
    List<Place> newPlacesList;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //DatabaseReference currentLocationRef;
    DatabaseReference friendsListRef, usersRankListRef, currentLocationOfUsersRef;
    FirebaseUser user;
    File storagePath;
    //ArrayList<CurrentLocationModel> arrayCurrentLM;
    boolean friends_showed = true;
    ArrayList<Marker> myFriendsMarkers;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Double myLatitude=0.0, myLongitude=0.0;
    LinearLayout box_search;
    boolean box_search_visibility=false;
    EditText search_radius, search_name, search_desc;
    List<Place> searchPlacesList;
    CharSequence srch_radius="",srch_name="",srch_desc="";
    ToggleButton search_box_show_hide;
    ArrayList<UserRankModel> urmList;
    private LocationRequest mLocationRequest;
    private static final long POLLING_FREQ = 1000 * 30;
    private static final long FASTEST_UPDATE_FREQ = 1000 * 5;
    Marker MyLocMarkerFusedLocApi;
    ImageView loggedUserImage;
    TextView loggedUserName, loggedUserEmail;

    public static final String BROADCAST_ACTION = "MyCurrentAction";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();

        } else {
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);


            //arrayCurrentLM = new ArrayList<CurrentLocationModel>();
            myFriendsMarkers = new ArrayList<Marker>();
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(MainActivity.this)
                        .addOnConnectionFailedListener(MainActivity.this)
                        .addApi(LocationServices.API)
                        .build();
            }
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(POLLING_FREQ);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_FREQ);
            MyLocMarkerFusedLocApi = null;
            newPlacesList = new ArrayList<Place>();
            searchPlacesFunc();
            urmList = new ArrayList<UserRankModel>();


            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            //if user logged in
            //ifUserLoggedInAddOptionsInMenu();

            //Toast.makeText(MainActivity.this, "NIJe ulogovan", Toast.LENGTH_SHORT).show();
            //FirebaseAuth.getInstance().signOut();
            mAuth = FirebaseAuth.getInstance();
            mAuthListener = new FirebaseAuth.AuthStateListener() {

                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // User is signed in
                        Log.d("A", "onAuthStateChanged: singed_in:" + user.getUid());
                        //Toast.makeText(MainActivity.this, "sd se uloguvajac", Toast.LENGTH_SHORT).show();

                    } else {
                        //User is singed out
                        Log.d("A", "onAuthStateChanged: singed_out");
                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
            };

            places_list_view = (ListView) findViewById(R.id.places_list_view);
            //add all type of places from database to drop down menu
            typeOfObjectDropDown = (Spinner) findViewById(R.id.type_of_object);
            typeOfObjectRef = database.getReference("TypeOfObject");
            typeOfObjectRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                    };
                    List arrayOfObjectTypes = dataSnapshot.getValue(t);
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, arrayOfObjectTypes);
                    typeOfObjectDropDown.setAdapter(dataAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //listen switch map-list button
            place_for_content = (ViewFlipper) findViewById(R.id.place_for_content);
            list_map_button = (Button) findViewById(R.id.list_map_switch);
            list_map_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (list_map_button_switch == 0) {
                        list_map_button_switch = 1;
                        list_map_button_text = "List View";
                        if (current_contet_shown == 0) {
                            place_for_content.setDisplayedChild(1);
                            current_contet_shown = 1;
                        }
                    } else {
                        list_map_button_switch = 0;
                        list_map_button_text = "Map View";
                        if (current_contet_shown == 1) {
                            place_for_content.setDisplayedChild(0);
                            current_contet_shown = 0;
                        }
                    }
                    list_map_button.setText(list_map_button_text);
                }
            });

            placesList = new ArrayList<Place>();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if(user != null){
            loggedUserName = (TextView) findViewById(R.id.loggedUserNameView);
            loggedUserEmail = (TextView) findViewById(R.id.loggedUserEmailView);
            loggedUserImage = (ImageView) findViewById(R.id.loggedUserImageView);
            loggedUserName.setText(user.getDisplayName());
            loggedUserEmail.setText(user.getEmail());
            Bitmap myImage = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/wKyn/"+user.getEmail());
            Bitmap resizedMyImage = Bitmap.createScaledBitmap(myImage, 50, 50, false);
            loggedUserImage.setImageBitmap(resizedMyImage);
        }
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_places) {
            place_for_content.setDisplayedChild(current_contet_shown);
            //current_contet_shown = 0;
            // Handle the camera action
            //Intent i = new Intent(this, DatabaseActivity.class);
            //startActivity(i);
        } else if(id == R.id.nav_add_place){
            Intent i = new Intent(MainActivity.this, AddPlaceActivity.class);
            startActivity(i);
        } else if(id == R.id.nav_location_tracking){
            Intent i = new Intent(MainActivity.this, LocationTrackerActivity.class);
            startActivity(i);
            /*LocationService ls = new LocationService();*/
        } else if(id == R.id.nav_connect_with_friend){
            Intent i = new Intent(MainActivity.this, ConnectWithFriendActivity.class);
            startActivity(i);
        } else if(id == R.id.nav_show_hide_friends){
            if (friends_showed){
                for(Marker myFriendMarker : myFriendsMarkers){
                    myFriendMarker.setVisible(false);
                }
                friends_showed=false;
            }
            else
            {
                for(Marker myFriendMarker : myFriendsMarkers){
                    myFriendMarker.setVisible(true);
                }
                friends_showed=true;
            }
        }else if(id == R.id.nav_user_rank){
            Intent i = new Intent(MainActivity.this, UserRankActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        googleMap.setMyLocationEnabled(true);

        addPlacesOnMap();



        //set on place on map on click listener
        /*googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent (MainActivity.this, PlaceDetailsActivity.class);
                int i = markerPlaceIdMap.get(marker);
                //Toast.makeText(MainActivity.this, i, Toast.LENGTH_SHORT).show();
                Place plc = newPlacesList.get(i);
                intent.putExtra("placeID", plc.getID());
                startActivity(intent);
                return false;
            }
        });*/


        //update my current location

        final BroadcastReceiver myLocationReceiver = new BroadcastReceiver() {
            Marker MyLocMarker=null;
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Double latitude =  intent.getDoubleExtra("Latitude", -1);
                Double longitude =  intent.getDoubleExtra("Longitude", -1);

                LatLng loc = new LatLng(latitude, longitude);
                MarkerOptions myLocOptions = new MarkerOptions();
                myLocOptions.position(loc);
                //markerOptios.icon(BitmapDescriptorFactory.fromResource(R.drawable.myplace));
                myLocOptions.title("You are here!");
                if (MyLocMarker != null)
                    MyLocMarker.remove();
                MyLocMarker = map.addMarker(myLocOptions);
                MyLocMarker.showInfoWindow();
                MyLocMarker.setTag("MYLOCATIONMARKER");
            }
        };
        IntentFilter mainFilter= new IntentFilter(BROADCAST_ACTION);
        registerReceiver(myLocationReceiver, mainFilter);

        addFriendsOnMap();

    }
    /*public void ifUserLoggedInAddOptionsInMenu()
    {
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        /*for (UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if (user.getProviderId().equals("firebase")) {
                Toast.makeText(MainActivity.this, "Korisnik je ulogovan", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(MainActivity.this, "Korisnik NIje ulogovan", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public  void onStop() {
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    public void addFriendsOnMap(){

        usersRankListRef=database.getReference("UserRank");
        Query queryUsers=usersRankListRef.orderByChild("email");
        queryUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<UserRankModel> t = new GenericTypeIndicator<UserRankModel>(){};
                UserRankModel urm = new UserRankModel();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    urm=ds.getValue(t);
                    urmList.add(urm);
                }
                currentLocationOfUsersRef = database.getReference("CurrentLocationOfUsers");
                friendsListRef = database.getReference("myFriendsList");
                Query query = friendsListRef.orderByChild("email").equalTo(user.getEmail());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            GenericTypeIndicator<FriendsModel> t = new GenericTypeIndicator<FriendsModel>() {};
                            String key = "123";
                            FriendsModel fm = new FriendsModel();
                            for(DataSnapshot ds : dataSnapshot.getChildren())
                            {
                                key = ds.getKey();
                                fm = ds.getValue(t);
                            }
                            ArrayList<String> myFriends = fm.getFriends();
                            storagePath = new File(Environment.getExternalStorageDirectory(), "wKyn");
                            // Create direcorty if not exists
                            if(!storagePath.exists()) {
                                storagePath.mkdirs();
                            }
                            for (int i=0; i < myFriends.size(); i++) {
                                final Query query1 = currentLocationOfUsersRef.orderByChild("email").equalTo(myFriends.get(i));

                                final File localFile = new File(storagePath, myFriends.get(i));

                                myFriendsRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://knv1-f595a.appspot.com/").child(myFriends.get(i));

                                myFriendsRef.getFile(localFile)
                                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        GenericTypeIndicator<CurrentLocationModel> t = new GenericTypeIndicator<CurrentLocationModel>() {};
                                                        CurrentLocationModel clm2 = new CurrentLocationModel();
                                                        for(DataSnapshot ds : dataSnapshot.getChildren())
                                                        {
                                                            clm2 = ds.getValue(t);
                                                        }
                                                        LatLng loc = new LatLng(clm2.getLatitude(), clm2.getLongitude());
                                                        MarkerOptions markerOptios = new MarkerOptions();
                                                        markerOptios.position(loc);
                                                        Bitmap friendImge = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                        Bitmap resizedFriendBitmap = Bitmap.createScaledBitmap(friendImge, 60, 80, false);

                                                        markerOptios.icon(BitmapDescriptorFactory.fromBitmap(resizedFriendBitmap));
                                                        //markerOptios.title(clm2.getEmail());
                                                        Marker marker = map.addMarker(markerOptios);
                                                        marker.setTag(clm2.getEmail());
                                                        myFriendsMarkers.add(marker);
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {

                                    }
                                });
                            }
                            for(UserRankModel urm1 : urmList){
                                if(!myFriends.contains(urm1.getEmail()) && !urm1.getEmail().equals(user.getEmail())){
                                    final String userName=urm1.getName();
                                    final Query query1 = currentLocationOfUsersRef.orderByChild("email").equalTo(urm1.getEmail());
                                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            GenericTypeIndicator<CurrentLocationModel> t = new GenericTypeIndicator<CurrentLocationModel>() {};
                                            CurrentLocationModel clm2 = new CurrentLocationModel();
                                            for(DataSnapshot ds : dataSnapshot.getChildren())
                                            {
                                                clm2 = ds.getValue(t);
                                            }
                                            LatLng loc = new LatLng(clm2.getLatitude(), clm2.getLongitude());
                                            MarkerOptions markerOptios = new MarkerOptions();
                                            markerOptios.position(loc);

                                            //Toast.makeText(MainActivity.this, userName, Toast.LENGTH_SHORT).show();
                                            markerOptios.icon(BitmapDescriptorFactory.fromResource(R.mipmap.k1));
                                            markerOptios.title(userName);
                                            Marker marker = map.addMarker(markerOptios);
                                            marker.setTag("USER");
                                            myFriendsMarkers.add(marker);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getTag() != null && marker.getTag() != "USER" && marker.getTag() != "MYLOCATIONMARKER")
                {
                    //Toast.makeText(MainActivity.this, "PRijateeeljj "+marker.getTag(), Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, FriendDetailsActivity.class);
                    i.putExtra("friendEmail", marker.getTag().toString());
                    startActivity(i);
                }
                return false;
            }
        });

    }
    public void addPlacesOnMap(){
        //Retrieve all places from database
        allPlaces = database.getReference("AllPlaces");
        allPlaces.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //GenericTypeIndicator<String> t = new GenericTypeIndicator<String>() {};
               // String stringOfPlaces = dataSnapshot.getValue(t);
                //Type type = new TypeToken<List<Place>>() {}.getType();
                //placesList = new Gson().fromJson(stringOfPlaces, type);
                GenericTypeIndicator<Place> t = new GenericTypeIndicator<Place>() {};

                Place place2 = new Place("");
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    place2=ds.getValue(t);
                    placesList.add(place2);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //Retrieve all places from database END

        //listen type of object drop down change
        typeOfObjectDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                //Toast.makeText(MainActivity.this, "Promenjeno "+position+ " id: "+ id + " "+ parentView.getItemAtPosition(position), Toast.LENGTH_SHORT).show();

                String TypeName = parentView.getItemAtPosition(position).toString();
                if (TypeName.equals("All") ) {
                    newPlacesList = placesList;
                    //places_list_view.setAdapter(new ArrayAdapter<Place>(MainActivity.this, android.R.layout.simple_list_item_1, placesList));
                }
                else {
                    newPlacesList = new ArrayList<Place>();
                    //newPlacesList.clear();
                    for (Place plc : placesList) {
                        if (plc.getType().equals(TypeName)) {
                            newPlacesList.add(plc);
                        }
                    }

                }
                searchListenerFunc();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
                Toast.makeText(MainActivity.this, "NISTAA", Toast.LENGTH_SHORT).show();
            }

        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                    if(marker.getTag() != "MYLOCATIONMARKER" && marker.getTag() != "USER") {
                        Intent intent = new Intent(MainActivity.this, PlaceDetailsActivity.class);
                        int i = markerPlaceIdMap.get(marker);
                        Place plc = newPlacesList.get(i);
                        intent.putExtra("placeID", plc.getMyId());
                        startActivity(intent);
                    }
            }
        });
        //set on place in list on click listener
        places_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place plc = (Place)parent.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, PlaceDetailsActivity.class);
                intent.putExtra("placeID", plc.getMyId());
                startActivity(intent);
            }
        });
    }
    public void searchPlacesFunc(){
        box_search = (LinearLayout) findViewById(R.id.search_box_layout);
        search_radius = (EditText) findViewById(R.id.search_places_radius);
        search_name = (EditText) findViewById(R.id.search_places_name);
        search_desc = (EditText) findViewById(R.id.search_places_description);
        search_box_show_hide = (ToggleButton) findViewById(R.id.search_box_show_hide);
        search_box_show_hide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    box_search.setVisibility(View.VISIBLE);
                }
                else {
                    box_search.setVisibility(View.GONE);
                    search_radius.setText("");
                    search_name.setText("");
                    search_desc.setText("");
                }
            }
        });


        search_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                srch_name=s;
                searchListenerFunc();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        search_radius.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                srch_radius=s;
                searchListenerFunc();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        search_desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                srch_desc=s;
                searchListenerFunc();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }
    public double distanceMeasure(double lat1, double lon1, double lat2, double lon2){
        double R = 6378.137;
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000; // meters
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            //Toast.makeText(MainActivity.this, String.valueOf(mLastLocation.getLatitude()), Toast.LENGTH_SHORT).show();
            myLatitude = mLastLocation.getLatitude();
            myLongitude = mLastLocation.getLongitude();
            //Toast.makeText(MainActivity.this, "Latitude: "+myLatitude+" Longitude: "+myLongitude, Toast.LENGTH_SHORT).show();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        // Determine whether new location is better than current best
        // estimate
        //Toast.makeText(MainActivity.this, "Latitude: "+location.getLatitude()+" Longitude: "+location.getLongitude(), Toast.LENGTH_SHORT).show();
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions myLocOptions = new MarkerOptions();
        myLocOptions.position(loc);
        //markerOptios.icon(BitmapDescriptorFactory.fromResource(R.drawable.myplace));
        myLocOptions.title("You are here! FusedLocApi");
        if (MyLocMarkerFusedLocApi != null)
            MyLocMarkerFusedLocApi.remove();
        MyLocMarkerFusedLocApi = map.addMarker(myLocOptions);
        //MyLocMarkerFusedLocApi.showInfoWindow();
        MyLocMarkerFusedLocApi.setTag("MYLOCATIONMARKER");
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    public void setPlacesListMap(List<Place> p){
        places_list_view.setAdapter(new ArrayAdapter<Place>(MainActivity.this, android.R.layout.simple_list_item_1, p));
        map.clear();
        markerPlaceIdMap = new HashMap<Marker, Integer>((int)((double)p.size()*1.2));
        for (int i=0; i < p.size(); i++){
            //Toast.makeText(MainActivity.this, i, Toast.LENGTH_SHORT).show();
            Place place = p.get(i);
            String lat = place.getLatitude();
            String lon = place.getLongitude();
            LatLng loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
            MarkerOptions markerOptios = new MarkerOptions();
            markerOptios.position(loc);
            //markerOptios.icon(BitmapDescriptorFactory.fromResource(R.drawable.myplace));
            markerOptios.title(place.getName());
            Marker marker = map.addMarker(markerOptios);
            markerPlaceIdMap.put(marker, i);
        }
    }
    public void searchListenerFunc(){
        searchPlacesList = new ArrayList<Place>();
        for(Place plc : newPlacesList){
            if(plc.getName().toLowerCase().matches(".*"+srch_name.toString().toLowerCase()+".*") &&
                    plc.getDescription().toLowerCase().matches(".*"+srch_desc.toString().toLowerCase()+".*") &&
                    (srch_radius.toString().equals("") || srch_radius.toString().matches("[0-9]+"))
                    )
                if(srch_radius.toString().equals("") || distanceMeasure(myLatitude, myLongitude, Double.parseDouble(plc.getLatitude()),
                        Double.parseDouble(plc.getLongitude()))<=Double.parseDouble(srch_radius.toString())){
                    //Toast.makeText(MainActivity.this, plc.name, Toast.LENGTH_SHORT).show();
                    searchPlacesList.add(plc);
                }
            setPlacesListMap(searchPlacesList);
        }
    }
}
