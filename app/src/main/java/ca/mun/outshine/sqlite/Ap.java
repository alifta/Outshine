package ca.mun.outshine.sqlite;

public class Ap {

    // Table Name
    public static final String TABLE_NAME = "ap";

    // Table column
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SSID = "ssid";
    public static final String COLUMN_BSSID = "bssid";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_SSID + " TEXT,"
                    + COLUMN_BSSID + " TEXT"
                    + ")";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private int id;
    private String ssid;
    private String bssid;

    public Ap() {
    }

    public Ap(int id, String ssid, String bssid) {
        this.id = id;
        this.ssid = ssid;
        this.bssid = bssid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
