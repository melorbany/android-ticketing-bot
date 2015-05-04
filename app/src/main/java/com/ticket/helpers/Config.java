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
}
