package ca.mun.outshine.service;

public class Constants {

    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 30 * 1000;
    public static final int CONFIDENCE = 70;

    // wifi service
    public static final String KEY_WIFI = "wifi_list";
    public static final String ACTION_WIFI = "service.WifiService";


    // health service
    // receiving extra
    public static final String HEALTH_LOG = "log";
    public static final String HEALTH_DAY = "day";
    public static final String HEALTH_NOW = "now";
    public static final String HEALTH_TYPE = "type";
    // broadcasting extra
    public static final String HEALTH_RESULT_CODE = "result_code";
    public static final String HEALTH_RESULT_TYPE = "result_type";
    public static final String HEALTH_RESULT_VALUE = "result_value";
    public static final String HEALTH_TYPE_STEPS = "steps";
    public static final String HEALTH_TYPE_DISTANCE = "distance";
    public static final String HEALTH_TYPE_CALORIES = "calories";
    public static final String HEALTH_TYPE_ACTIVE_TIME = "active_time";
    // action
    public static final String HEALTH_ACTION = "service.HealthService";

}
