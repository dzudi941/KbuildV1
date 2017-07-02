package com.wkyn.dzudi.kbuildv1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class FriendDetailsActivity extends AppCompatActivity {

    TextView friend_email_view, friend_name_view;
    ImageView friend_image_view;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        friend_email_view = (TextView) findViewById(R.id.friend_details_email);
        friend_name_view = (TextView) findViewById(R.id.friend_details_name);
        friend_image_view = (ImageView) findViewById(R.id.friend_details_image);

        Intent listIntent = getIntent();
        Bundle positionBundle = listIntent.getExtras();
        String friendEmail = positionBundle.getString("friendEmail");

        /*Task<UserRecord> task = FirebaseAuth.getInstance().getUserByEmail(friendEmail)
                .addOnSuccessListener(userRecord -> {
                    // See the UserRecord reference doc for the contents of userRecord.
                    System.out.println("Successfully fetched user data: " + userRecord.getEmail());
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error fetching user data: " + e.getMessage());
                });*/
        friend_email_view.setText(friendEmail);
        Bitmap friendImge = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/wKyn/"+friendEmail);
        Bitmap resizedFriendBitmap = Bitmap.createScaledBitmap(friendImge, 736, 800, false);
        friend_image_view.setImageBitmap(resizedFriendBitmap);

    }

}
