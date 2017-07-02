package com.wkyn.dzudi.kbuildv1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.lang.reflect.Type;
import java.util.List;

public class PlaceDetailsActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference allPlaces;
    StorageReference placeImageURL;
    String placeID;
    List<Place> placesList;
    TextView place_details_title;
    ImageView place_details_image;
    TextView place_details_description;
    private StorageReference mStorageRef;
    StorageReference placeImgRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get id of clicked Object
        Intent listIntent = getIntent();
        Bundle positionBundle = listIntent.getExtras();
        placeID = positionBundle.getString("placeID");
        place_details_title = (TextView) findViewById(R.id.place_details_title);
        place_details_image = (ImageView) findViewById(R.id.place_details_image);
        place_details_description = (TextView) findViewById(R.id.place_details_description);
        //mStorageRef = FirebaseStorage.getInstance().getReference();

        allPlaces = database.getReference("AllPlaces");
        Query query = allPlaces.orderByChild("myId").equalTo(placeID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Place> t = new GenericTypeIndicator<Place>() {};
                Place place = new Place();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    place=ds.getValue(t);
                }
                place_details_title.setText(place.getName());
                place_details_description.setText(place.getDescription());

                File storagePath = new File(Environment.getExternalStorageDirectory(), "wKyn");
                // Create direcorty if not exists
                if(!storagePath.exists()) {
                    storagePath.mkdirs();
                }
                final File localFile = new File(storagePath,place.getName());

                placeImgRef = FirebaseStorage.getInstance().getReferenceFromUrl(place.getImageUrl());
                placeImgRef.getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                //Toast.makeText(PlaceDetailsActivity.this, "Preuzeta slika", Toast.LENGTH_SHORT).show();
                                Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                place_details_image.setImageBitmap(myBitmap);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle failed download
                        // ...
                        Toast.makeText(PlaceDetailsActivity.this, "Nije preuzeta slika", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
