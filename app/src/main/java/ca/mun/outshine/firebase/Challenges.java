package ca.mun.outshine.firebase;

public class Challenges {

    public String creator;
    public String challengeName;
    public String competitors;
    public String unit;
    public String timeCreated;
    public String timeStarts;
    public String timieEnds;
    public int privacyStatus;

    public Challenges() {

    }

    public Challenges(String creator, String challengeName, String competitors, String unit,
                      String timeCreated, String timeStarts, String timieEnds, int privacyStatus) {
        this.creator = creator;
        this.challengeName = challengeName;
        this.competitors = competitors;
        this.unit = unit;
        this.timeCreated = timeCreated;
        this.timeStarts = timeStarts;
        this.timieEnds = timieEnds;
        this.privacyStatus = privacyStatus;
    }

}
