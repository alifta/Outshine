package ca.mun.outshine.firebase;

public class ChallengesEntries {

    public String challengeId;
    public String challengerId;
    public String timeAdded;
    public String timeUploaded;
    public String totalPoints;

    public ChallengesEntries() {

    }

    public ChallengesEntries(String challengeId, String challengerId, String timeAdded,
                             String timeUploaded, String totalPoints) {
        this.challengeId = challengeId;
        this.challengerId = challengerId;
        this.timeAdded = timeAdded;
        this.timeUploaded = timeUploaded;
        this.totalPoints = totalPoints;
    }

}
