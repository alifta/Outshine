package ca.mun.outshine.firebase;

public class LoginLogs {

    public String userId;
    public String location;
    public String ip;
    public String logTime;

    public LoginLogs() {
    }

    public LoginLogs(String userId, String location, String ip, String logTime) {
        this.userId = userId;
        this.location = location;
        this.ip = ip;
        this.logTime = logTime;
    }

}
