package com.ticket.helpers;

public class Config {
	// File upload url (replace the ip with your server address)
	public static final String FILE_UPLOAD_URL = "http://178.62.223.220/api/v1/new";

    public static final String INBOUND_MESSAGE = "http://95.211.228.10/Mobile/MobileBusiness.asmx/inboundMessage";
	
	// Directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME = "Android File Upload";

    // Message Types
    public static final int MESSAGE_TYPE_IMAGE    = 1;
    public static final int MESSAGE_TYPE_VIDEO    = 2;
    public static final int MESSAGE_TYPE_TEXT     = 3;
    public static final int MESSAGE_TYPE_AUDIO    = 4;
    public static final int MESSAGE_TYPE_LOCATION = 5;



        /*
     * Constants for location update parameters
     */


    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 1;

    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    final static public int LOC_UPDATE_INTERVAL = 50;

    final static public int LOC_STATUS_NO_LOC = -1;
    final static public int LOC_STATUS_LAST_LOC = 0;
    final static public int LOC_STATUS_NEW_LOC = 1;

    final static public double LOC_NEAR_AROUND_DIST = .1;
    final static public double LOC_FAR_AROUND_DIST = .5;

    public final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    public final static String LOCATION_KEY = "location-key";
    public final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";


    //The minimum distance to change updates in metters
    public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    //The minimum time between updates in milliseconds
    public static final long MIN_TIME_BW_UPDATES = 400;


    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

}
