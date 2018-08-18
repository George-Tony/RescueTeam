package com.webcrs.rescueteam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity{


    private static final String TAG = MainActivity.class.getSimpleName();
    private long UPDATE_INTERVAL = 30 * 60   * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private static final int REQUEST_CHECK_SETTINGS = 100;
    // location last updated time
    private String mLastUpdateTime;
    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;


    @BindView(R.id.getLocationGPS)
    Button getLocationGPS;

    @BindView(R.id.currentLocationtext)
    EditText currentLocationtext;

    @BindView(R.id.goingTo)
    EditText goingTo;

    @BindView(R.id.submitLocationUpdate)
    Button submitLocationUpdate;

    @BindView(R.id.toogleLocationTrack)
    Switch toogleLocationTrack;

    @BindView(R.id.fifteenMins)
    RadioButton fifteenMins;

    @BindView(R.id.thirtyMins)
    RadioButton thirtyMins;

    @BindView(R.id.oneHour)
    RadioButton oneHour;

    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;



    String userID;
    SessionManager sessionManager;

    private Realm mRealm;

    RecyclerView  recyclerView;

    HelpNeededRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        submitLocationUpdate = findViewById(R.id.submitLocationUpdate);
        getLocationGPS = findViewById(R.id.getLocationGPS);
        recyclerView = (RecyclerView) findViewById(R.id.recylerView);
        sessionManager = new SessionManager(getApplicationContext());
        userID= sessionManager.getuserID();

        if(userID == null){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        // initialize the necessary libraries
        init();
        // restore the values from saved instance state
        restoreValuesFromBundle(savedInstanceState);

        Realm.init(getApplicationContext());
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        mRealm = Realm.getInstance(config);

        if(toogleLocationTrack.isChecked()){
            startLocationUpdates();
        }

        //mRealm = Realm.getInstance(getApplicationContext());
        toogleLocationTrack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    startLocationUpdates();
                }else{
                    stopLocationUpdates();
                }
            }
        });


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                if(checkedId == fifteenMins.getId()){
                    UPDATE_INTERVAL = 15 * 60 * 1000;
                }else if(checkedId == thirtyMins.getId()){
                    UPDATE_INTERVAL = 30 * 60 * 1000;
                }else if(checkedId == oneHour.getId()){
                    UPDATE_INTERVAL = 60 * 60 * 1000;
                }

            }
        });


        List<HelpNeededListModel> helpNeededListModels = new ArrayList<>();
        mAdapter = new HelpNeededRecyclerViewAdapter(helpNeededListModels, new ClickListener() {
            @Override public void onPositionClicked(int position) {
                // callback performed on click
            }

        }, getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

    }



    public interface ClickListener {

        void onPositionClicked(int position);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());



                updateLocation();
            }
        };

      /*  mRequestingLocationUpdates = false;*/

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Restoring values from saved instance state
     */
    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            /*if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }*/

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
            }
        }

        updateLocation();



    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    /**
     * Update the UI displaying the location data
     * and toggling the buttons
     */
    private void updateLocation() {
        if (mCurrentLocation != null) {

            //currentLocationtext.setText("Latitude" + mCurrentLocation.getLatitude() + "Longitude" + mCurrentLocation.getLongitude());

            currentLocationtext.setText(""+getAddressLine(getApplicationContext()));
            String currentLocation =  currentLocationtext.getText().toString();
            String goingToLocation =  goingTo.getText().toString();

            if(isNetworkConnected()){

                /*write update directly to server code here*/
                Log.e("Location: ", ""+getAddressLine(getApplicationContext())/*+"\n"+
                    getLocality(getApplicationContext())+"\n"+getPostalCode(getApplicationContext())+
                    "\n"+getCountryName(getApplicationContext())*/);
                Toast.makeText(this, "Location: "+getAddressLine(getApplicationContext()), Toast.LENGTH_SHORT).show();

            }else {
                // saving the location locally and will update the location once network comes
                mRealm.beginTransaction();
                UpdateLocationModel updateLocationModel = mRealm.createObject(UpdateLocationModel.class);
                updateLocationModel.setLatitude(mCurrentLocation.getLatitude());
                updateLocationModel.setLatitude(mCurrentLocation.getLongitude());
                updateLocationModel.setAccuracy(mCurrentLocation.getAccuracy());
                updateLocationModel.setDateTime(getDate(mCurrentLocation.getTime()));
                updateLocationModel.setTxtAddress(getAddressLine(getApplicationContext()));
                updateLocationModel.setUserID("123");
                mRealm.commitTransaction();

            }

        }


    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


    private String getDate(long time) {
        /*Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);*/
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String date = dateFormat.format(time);
        return date;
    }

    @OnClick(R.id.getLocationGPS)
    public void startLocationButtonClick() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }


    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /*private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }*/

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        if(toogleLocationTrack.isChecked()) {
                            updateLocation();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                        if(toogleLocationTrack.isChecked()) {
                            updateLocation();
                        }
                    }
                });
    }

    // How many Geocoder should return our GPSTracker
    int geocoderMaxResults = 1;

    /**
     * Get list of address by latitude and longitude
     * @return null or List<Address>
     */
    public List<Address> getGeocoderAddress(Context context) {
        if (mCurrentLocation != null) {

            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);

            try {
                /**
                 * Geocoder.getFromLocation - Returns an array of Addresses
                 * that are known to describe the area immediately surrounding the given latitude and longitude.
                 */
                List<Address> addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude(), this.geocoderMaxResults);

                return addresses;
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e(TAG, "Impossible to connect to Geocoder", e);
            }
        }

        return null;
    }

    /**
     * Try to get AddressLine
     * @return null or addressLine
     */
    public String getAddressLine(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);

            return addressLine;
        } else {
            return null;
        }
    }

    /**
     * Try to get Locality
     * @return null or locality
     */
    public String getLocality(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String locality = address.getLocality();

            return locality;
        }
        else {
            return null;
        }
    }

    /**
     * Try to get Postal Code
     * @return null or postalCode
     */
    public String getPostalCode(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String postalCode = address.getPostalCode();

            return postalCode;
        } else {
            return null;
        }
    }

    /**
     * Try to get CountryName
     * @return null or postalCode
     */
    public String getCountryName(Context context) {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String countryName = address.getCountryName();

            return countryName;
        } else {
            return null;
        }
    }



}
