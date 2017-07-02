package com.wkyn.dzudi.kbuildv1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dzudi on 6/23/2017.
 */

public class LocationService extends Service
{
    public static final String BROADCAST_ACTION = "MyCurrentAction";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;

    Intent intent;
    int counter = 0;

    @Override
    public void onCreate()
    {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }



    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }



    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(listener);
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }




    public class MyLocationListener implements LocationListener
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference currentLocationRef, allPlaces;
        String clmJson;
        CurrentLocationModel clm;
        ArrayList<Place> placesList, showedPlacesList;

        public MyLocationListener(){
            super();
            placesList = new ArrayList<Place>();
            showedPlacesList = new ArrayList<Place>();
            allPlaces = database.getReference("AllPlaces");
            allPlaces.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
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
        }
        public void onLocationChanged(final Location loc)
        {
            //Toast.makeText( getApplicationContext(), "Latitude: " + loc.getLatitude(), Toast.LENGTH_SHORT ).show();
            if(isBetterLocation(loc, previousBestLocation)) {
                //loc.getLatitude();
                //loc.getLongitude();
                //Toast.makeText( getApplicationContext(), "Longitude: " + loc.getLongitude(), Toast.LENGTH_SHORT ).show();
                intent.putExtra("Latitude", loc.getLatitude());
                intent.putExtra("Longitude", loc.getLongitude());
                intent.putExtra("Provider", loc.getProvider());
                sendBroadcast(intent);
                clm = new CurrentLocationModel(user.getEmail(), loc.getLatitude(), loc.getLongitude());
                currentLocationRef = database.getReference("CurrentLocationOfUsers");
                Query query = currentLocationRef.orderByChild("email").equalTo(user.getEmail());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            GenericTypeIndicator<CurrentLocationModel> t = new GenericTypeIndicator<CurrentLocationModel>() {};
                            String key = "123";
                            for(DataSnapshot ds : dataSnapshot.getChildren())
                            {
                                key = ds.getKey();
                                clm = ds.getValue(t);
                            }
                            //Toast.makeText( getApplicationContext(), clm.getEmail(), Toast.LENGTH_SHORT ).show();
                            clm.setLatitude(loc.getLatitude());
                            clm.setLongitude(loc.getLongitude());
                            currentLocationRef.child(key).setValue(clm);
                        }
                        else {
                            //clm.addFriend("1m@a.com");
                            currentLocationRef.push().setValue(clm);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //Toast.makeText( getApplicationContext(), query.toString(), Toast.LENGTH_SHORT ).show();

                //currentLocationRef.push().setValue(clmJson);
                notificationNearObject(loc.getLatitude(), loc.getLongitude());

            }
        }

        public void onProviderDisabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
        }


        public void onProviderEnabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras)
        {

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
        public void notificationNearObject(double lat, double lon){
            int i=1;
            for(Place place : placesList){
                double placeLatitude = Double.parseDouble(place.getLatitude());
                double placeLongitude = Double.parseDouble(place.getLongitude());
                double distance=distanceMeasure(placeLatitude, placeLongitude, lat,lon);
                if(distance > 500.0 && showedPlacesList.contains(place)){
                    showedPlacesList.remove(place);
                }
                if (!showedPlacesList.contains(place)){
                    //Toast.makeText( getApplicationContext(), "Rastojanje: "+distanceMeasure(placeLatitude, placeLongitude, lat,lon)+" Ime Objekta: "+place.getName(), Toast.LENGTH_SHORT ).show();
                    if(distance <= 500.0){
                        NotificationCompat.Builder mBuilder =
                                (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                                        .setSmallIcon(R.mipmap.k1)
                                        .setContentTitle("A place in your area")
                                        .setContentText(place.getName() + "    type: " + place.getType());
                        Intent resultIntent = new Intent(getApplicationContext(), PlaceDetailsActivity.class);
                        resultIntent.putExtra("placeID", place.getMyId());
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                        stackBuilder.addParentStack(PlaceDetailsActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(i, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(resultPendingIntent);
                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(i, mBuilder.build());
                        i++;
                        showedPlacesList.add(place);
                    }
                }
            }
        }
    }
}