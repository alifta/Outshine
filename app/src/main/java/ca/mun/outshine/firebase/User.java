package ca.mun.outshine.firebase;

public class User {

    public String name;
    public String nickName;
    public String email;
    public String signupLocation;
    public String signupIp;
    public String signupTime;
    public String authVal;
    public int statusOnline;
    public int privacyStatus;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String email) {
        this.email = email;
    }

    public User(String name, String nickName, String email, String signupLocation, String signupIp,
                String signupTime, String authVal, int statusOnline, int privacyStatus) {
        this.name = name;
        this.nickName = nickName;
        this.email = email;
        this.signupLocation = signupLocation;
        this.signupIp = signupIp;
        this.signupTime = signupTime;
        this.authVal = authVal;
        this.statusOnline = statusOnline;
        this.privacyStatus = privacyStatus;
    }

}
