package idv.ksl.location;

import com.google.android.gms.location.Geofence;

/**
 * Created by ksl.startup on 9/8/16.
 */
public final class Constants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    public static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;

    // Geofence parameters for the Android building on Google's main campus in Mountain View.
    public static final String ANDROID_BUILDING_ID = "ANDROID_BUILDING_ID";
    public static final double ANDROID_BUILDING_LATITUDE = 25.0235198;
    public static final double ANDROID_BUILDING_LONGITUDE = 121.5486976;
    public static final float ANDROID_BUILDING_RADIUS_METERS = 60.0f;

    // Geofence parameters for the Yerba Buena Gardens near the Moscone Center in San Francisco.
    public static final String YERBA_BUENA_ID = "YERBA_BUENA_ID";
    public static final double YERBA_BUENA_LATITUDE = 37.784886;
    public static final double YERBA_BUENA_LONGITUDE = -122.402671;
    public static final float YERBA_BUENA_RADIUS_METERS = 72.0f;
}