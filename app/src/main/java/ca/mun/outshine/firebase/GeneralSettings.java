package ca.mun.outshine.firebase;

public class GeneralSettings {

    public String serviceUrl;
    public String serviceUrlDate;
    public String credits;
    public String creditsDate;
    public String about;
    public String aboutDate;

    public GeneralSettings() {

    }

    public GeneralSettings(String serviceUrl, String serviceUrlDate, String credits,
                           String creditsDate, String about, String aboutDate) {
        this.serviceUrl = serviceUrl;
        this.serviceUrlDate = serviceUrlDate;
        this.credits = credits;
        this.creditsDate = creditsDate;
        this.about = about;
        this.aboutDate = aboutDate;
    }

}
