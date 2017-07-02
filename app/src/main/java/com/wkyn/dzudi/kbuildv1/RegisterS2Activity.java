package com.wkyn.dzudi.kbuildv1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class RegisterS2Activity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Button saveReg;
    Button takePicture;
    TextView userNameText;
    TextView userAboutText;
    String userName, userEmail, userImageRef;
    String userAbout;
    FirebaseUser user;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView userImage;
    private StorageReference mStorageRef;
    DatabaseReference userRankRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_s2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intUserEmail = getIntent();
        Bundle bunUserEmail = intUserEmail.getExtras();
        userEmail = bunUserEmail.getString("userEmail");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        Toast.makeText(RegisterS2Activity.this, user.getEmail(), Toast.LENGTH_SHORT).show();
        userRankRef = database.getReference("UserRank");
        saveReg = (Button) findViewById(R.id.button_registers2);
        saveReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userNameText = (TextView) findViewById(R.id.name_registers2);
                userAboutText = (TextView) findViewById(R.id.about_registers2);
                userName = userNameText.getText().toString();
                userAbout = userAboutText.getText().toString();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(userName)
                        .build();
                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    UserRankModel urm = new UserRankModel(userEmail, userName);
                                    userRankRef.push().setValue(urm);
                                    Toast.makeText(RegisterS2Activity.this, "Successfully saved!", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(RegisterS2Activity.this, MainActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        });
            }
        });
        takePicture = (Button) findViewById(R.id.take_picture_registers2);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePictureIntent.resolveActivity(getPackageManager())!=null){
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }
    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            userImage = (ImageView) findViewById(R.id.image_registers2);
            userImage.setImageBitmap(imageBitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataBitmap = baos.toByteArray();

            StorageReference stRef = mStorageRef.child(user.getEmail());
            stRef.putBytes(dataBitmap)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                           /* Toast.makeText(RegisterS2Activity.this, downloadUrl.toString(), Toast.LENGTH_SHORT).show();*/
                          // Intent i = new Intent(RegisterS2Activity.this, M)
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
            File storagePath = new File(Environment.getExternalStorageDirectory(), "wKyn");
            // Create direcorty if not exists
            if(!storagePath.exists()) {
                storagePath.mkdirs();
            }
            final File localFile = new File(storagePath,userEmail);
            if (localFile.exists ()) localFile.delete ();
            try {
                FileOutputStream out = new FileOutputStream(localFile);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
