package idv.ksl.location;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOG_TAG = MainActivity.class.getName();

    private GoogleApiClient mGoogleApiClient = null;
    private AddressResultReceiver mResultReceiver = null;

    // Internal List of Geofence objects. In a real app, these might be provided by an API based on
    // locations within the user's proximity.
    List<Geofence> mGeofenceList;
    GeofencingRequest mGeofenceRequest;

    // These will store hard-coded geofences in this sample app.
    private SimpleGeofence mAndroidBuildingGeofence;
    private SimpleGeofence mYerbaBuenaGeofence;

    private PendingIntent mGeofenceRequestIntent;

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String address = resultData.getString(Constants.RESULT_DATA_KEY);
            Toast.makeText(MainActivity.this, address, Toast.LENGTH_LONG).show();
            Log.i(LOG_TAG, "address = " + address);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultReceiver = new AddressResultReceiver(new Handler());

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        }

        mGeofenceList = new ArrayList<>();
        createGeofences();
        mGeofenceRequest = getGeofencingRequest();
    }

    /**
     * In this sample, the geofences are predetermined and are hard-coded here. A real app might
     * dynamically create geofences based on the user's location.
     */
    public void createGeofences() {
        // Create internal "flattened" objects containing the geofence data.
        mAndroidBuildingGeofence = new SimpleGeofence(
                Constants.ANDROID_BUILDING_ID,                // geofenceId.
                Constants.ANDROID_BUILDING_LATITUDE,
                Constants.ANDROID_BUILDING_LONGITUDE,
                Constants.ANDROID_BUILDING_RADIUS_METERS,
                Constants.GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );

        mGeofenceList.add(mAndroidBuildingGeofence.toGeofence());

//        mYerbaBuenaGeofence = new SimpleGeofence(
//                Constants.YERBA_BUENA_ID,                // geofenceId.
//                Constants.YERBA_BUENA_LATITUDE,
//                Constants.YERBA_BUENA_LONGITUDE,
//                Constants.YERBA_BUENA_RADIUS_METERS,
//                Constants.GEOFENCE_EXPIRATION_TIME,
//                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
//        );

        // Store these flat versions in SharedPreferences and add them to the geofence list.
//        mGeofenceList.add(mYerbaBuenaGeofence.toGeofence());
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
//        checkLocationSetting();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdate();

            // Get the PendingIntent for the geofence monitoring request.
            // Send a request to add the current geofences.
            mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mGeofenceRequest, mGeofenceRequestIntent);
            Toast.makeText(this, getString(R.string.start_geofence_service), Toast.LENGTH_SHORT).show();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 0);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(LOG_TAG, "Connection suspended.");

        if (null != mGeofenceRequestIntent) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, mGeofenceRequestIntent);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(LOG_TAG, "Connection failed.");
        GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdate();
            } else {
                // Permission was denied or request was cancelled
            }
        }
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return mLocationRequest;
    }

    protected void checkLocationSetting() {
        LocationRequest mLocationRequest = createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, 0);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    protected void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest mLocationRequest = createLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.i(LOG_TAG, "startLocationUpdate.");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LOG_TAG, String.format("onActivityResult. %d %d", requestCode, resultCode));
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView textView = (TextView)findViewById(R.id.output_text_view);
        if (location != null) {
            startIntentService(location);
            textView.append(String.format("\n%f, %f", location.getLatitude(), location.getLongitude()));
            Log.i(LOG_TAG, String.valueOf(location.getLatitude()));
            Log.i(LOG_TAG, String.valueOf(location.getLongitude()));
        } else {
            textView.append("\nCan not get location.");
            Log.e(LOG_TAG, "Can not get last location.");
        }
    }

    protected void startIntentService(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionsIntentService when a geofence
     * transition occurs.
     */
    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
}
