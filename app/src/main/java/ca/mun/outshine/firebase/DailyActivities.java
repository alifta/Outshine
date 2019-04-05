package ca.mun.outshine.firebase;

public class DailyActivities {

    public String userId;
    public String timeUploaded;
    public String startTime;
    public String endTime;
    public String steps;
    public String calories;
    public String distance;
    public String currentLocation;
    public String currentIp;

    public DailyActivities() {

    }

    public DailyActivities(String userId, String timeUploaded, String startTime, String endTime,
                           String steps, String calories, String distance, String currentLocation,
                           String currentIp) {
        this.userId = userId;
        this.timeUploaded = timeUploaded;
        this.startTime = startTime;
        this.endTime = endTime;
        this.steps = steps;
        this.calories = calories;
        this.distance = distance;
        this.currentLocation = currentLocation;
        this.currentIp = currentIp;
    }

}
