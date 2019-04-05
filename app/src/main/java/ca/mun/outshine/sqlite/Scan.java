package ca.mun.outshine.sqlite;

public class Scan {

    public static final String TABLE_NAME = "scan";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SSID = "ssid";
    public static final String COLUMN_BSSID = "bssid";
    public static final String COLUMN_LATITUDE = "lan";
    public static final String COLUMN_LONGITUDE = "lon";
    public static final String COLUMN_CONNECT = "connect"; // connect = 1 and observe = 0
    public static final String COLUMN_TIMESTAMP = "time";

    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_SSID + " TEXT,"
            + COLUMN_BSSID + " TEXT,"
            + COLUMN_LATITUDE + " TEXT,"
            + COLUMN_LONGITUDE + " TEXT,"
            + COLUMN_CONNECT + " INTEGER,"
            + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private int id;
    private int user_id;
    private String ssid;
    private String bssid;
    private String lan;
    private String lon;
    private int connect;
    private String timestamp;

    public Scan() {
    }

    public Scan(int id, String ssid, String bssid,
                String lan, String lon, int connect, String timestamp) {
        this.id = id;
        this.ssid = ssid;
        this.bssid = bssid;
        this.lan = lan;
        this.lon = lon;
        this.connect = connect;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getLan() {
        return lan;
    }

    public void setLan(String lan) {
        this.lan = lan;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int isConnect() {
        return connect;
    }

    public void setConnect(int connect) {
        this.connect = connect;
    }
}
