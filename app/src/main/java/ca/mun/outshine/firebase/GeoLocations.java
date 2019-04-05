package ca.mun.outshine.firebase;

public class GeoLocations {

    public String location;
    public String time;
    public String timeUploaded;
    public String currentIP;

    public GeoLocations() {
    }

    public GeoLocations(String location, String time, String timeUploaded, String currentIP) {
        this.location = location;
        this.time = time;
        this.timeUploaded = timeUploaded;
        this.currentIP = currentIP;
    }

}
