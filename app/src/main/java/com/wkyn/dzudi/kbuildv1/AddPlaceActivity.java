package com.wkyn.dzudi.kbuildv1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.Query;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.List;

public class AddPlaceActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference typeOfObjectRef, allPlaces, AllPlacesRef, userRankRef;
    Spinner typeOfObjectDropDown;
    Button takePicture, upload_object;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView object_image;
    private StorageReference mStorageRef;
    String object_name="NN", object_desc, longitude, latitude, type_of_object, object_image_url="";
    Bitmap imageBitmap=null;
    EditText object_name_view, object_description_view, longitude_view, latitude_view;
    List<Place> placesList;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        user = FirebaseAuth.getInstance().getCurrentUser();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.add_object_map);
        mapFrag.getMapAsync(this);

        typeOfObjectDropDown = (Spinner) findViewById(R.id.add_object_object_type);
        typeOfObjectRef = database.getReference("TypeOfObject");
        typeOfObjectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                };
                List arrayOfObjectTypes = dataSnapshot.getValue(t);
                arrayOfObjectTypes.remove(0);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(AddPlaceActivity.this, android.R.layout.simple_spinner_dropdown_item, arrayOfObjectTypes);
                typeOfObjectDropDown.setAdapter(dataAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        takePicture = (Button) findViewById(R.id.add_object_take_picture);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePictureIntent.resolveActivity(getPackageManager())!=null){
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
        allPlaces = database.getReference("AllPlaces");
        mStorageRef = FirebaseStorage.getInstance().getReference();


        upload_object = (Button) findViewById(R.id.add_object_save_button);
        upload_object.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                object_name_view = (EditText) findViewById(R.id.add_object_object_name);
                object_name = object_name_view.getText().toString() != "" ? object_name_view.getText().toString() : "NN";
                object_description_view = (EditText) findViewById(R.id.add_object_object_desc);
                object_desc = object_description_view.getText().toString();
                longitude_view = (EditText) findViewById(R.id.add_object_longitude);
                longitude = longitude_view.getText().toString();
                latitude_view = (EditText) findViewById(R.id.add_object_latitude);
                latitude = latitude_view.getText().toString();
                typeOfObjectDropDown = (Spinner) findViewById(R.id.add_object_object_type);
                type_of_object = typeOfObjectDropDown.getSelectedItem().toString();
                if (imageBitmap != null)
                {
                    Toast.makeText(AddPlaceActivity.this, "Place added successful!", Toast.LENGTH_SHORT).show();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] dataBitmap = baos.toByteArray();

                    StorageReference stRef = mStorageRef.child(object_name);
                    stRef.putBytes(dataBitmap)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    object_image_url = downloadUrl.toString();
                                    Place place = new Place(object_name);
                                    place.setDescription(object_desc);
                                    place.setLatitude(latitude);
                                    place.setLongitude(longitude);
                                    place.setType(type_of_object);
                                    place.setImageUrl(object_image_url);
                                    //place.setID(placesList.size());
                                    //placesList.add(place);
                                    //Gson gson = new Gson();
                                    //String placesJson = gson.toJson(placesList);
                                    //allPlaces.setValue(placesJson);

                                    DatabaseReference onePlaceRef;
                                    onePlaceRef=allPlaces.push();
                                    place.setMyId(onePlaceRef.getKey());
                                    onePlaceRef.setValue(place);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                    userRankRef = database.getReference("UserRank");
                    Query query = userRankRef.orderByChild("email").equalTo(user.getEmail().toString());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                GenericTypeIndicator<UserRankModel> t = new GenericTypeIndicator<UserRankModel>() {};
                                UserRankModel urm=new UserRankModel();
                                String key="123";
                                for (DataSnapshot ds : dataSnapshot.getChildren()){
                                    key = ds.getKey();
                                    urm = ds.getValue(t);
                                }
                                urm.incrementRank();
                                userRankRef.child(key).setValue(urm);
                            }
                            else {
                                UserRankModel urm=new UserRankModel(user.getEmail().toString(), user.getDisplayName());
                                urm.incrementRank();
                                userRankRef.push().setValue(urm);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });


    }
    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            object_image = (ImageView) findViewById(R.id.add_object_object_image);
            object_image.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                    String lon = Double.toString(latLng.longitude);
                    String lat = Double.toString(latLng.latitude);
                    longitude_view = (EditText) findViewById(R.id.add_object_longitude);
                    longitude_view.setText(lon);
                    latitude_view = (EditText) findViewById(R.id.add_object_latitude);
                    latitude_view.setText(lat);
            }
        });
    }
}
