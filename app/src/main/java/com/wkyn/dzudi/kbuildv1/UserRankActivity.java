package com.wkyn.dzudi.kbuildv1;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class UserRankActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference userRankRef;
    ArrayList<UserRankModel> urmList;
    ListView user_rank_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_rank);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        user_rank_list = (ListView) findViewById(R.id.user_rank_list);
        urmList = new ArrayList<UserRankModel>();
        userRankRef = database.getReference("UserRank");
        Query query = userRankRef.orderByChild("rank");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<UserRankModel> t = new GenericTypeIndicator<UserRankModel>(){};
                UserRankModel urm = new UserRankModel();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    urm=ds.getValue(t);
                    urmList.add(urm);
                }
                Collections.reverse(urmList);
                user_rank_list.setAdapter(new ArrayAdapter<UserRankModel>(UserRankActivity.this, android.R.layout.simple_list_item_1, urmList));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
