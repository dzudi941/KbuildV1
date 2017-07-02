package com.wkyn.dzudi.kbuildv1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    Button registerButton;
    //FirebaseDatabase database = FirebaseDatabase.getInstance();
    //DatabaseReference userRankRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        /*mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("A", "onAuthStateChanged: singed_in:" + user.getUid());
                } else {
                    //User is singed out
                    Log.d("A", "onAuthStateChanged: singed_out");
                }
            }
        };*/
        /*FirebaseUser user = mAuth.getCurrentUser();
        Toast.makeText(RegisterActivity.this, user.getDisplayName(), Toast.LENGTH_SHORT).show();*/
        registerButton = (Button) findViewById(R.id.button_register);
        registerButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                TextView userEmailText = (TextView) findViewById(R.id.email_register);
                TextView userPasswordText = (TextView) findViewById(R.id.password_register);

                final String userEmail = userEmailText.getText().toString();
                String userPassword = userPasswordText.getText().toString();


                mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    Intent i = new Intent(RegisterActivity.this, RegisterS2Activity.class);
                                    i.putExtra("userEmail", userEmail);
                                    startActivity(i);
                                    finish();
                                }else {
                                    Toast.makeText(RegisterActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public  void onStop() {
        super.onStop();
        /*if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }*/
    }
}
