package ca.mun.outshine.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    private String id;
    private String email;
    private String name;
    private String photo_url;
    private String role;
    private String privacy;
    private String sex;
    private String date_of_birth;
    private String height;
    private String weight;
    private String goal_steps;
    private String goal_distance;
    private String goal_calories;
    private String goal_active_time;
    private String goal_floor;
    private String goal_sleep;
    private String signup_ip;
    private String signup_location;
    private @ServerTimestamp
    Date signup_time;

    private Map<String, Object> participate;

    public User() {
    }

    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.photo_url = "";
        this.role = "user"; // can be set as admin
        this.privacy = "public";
        this.sex = "";
        this.date_of_birth = "";
        this.height = "";
        this.weight = "";
        this.goal_steps = "10000";
        this.goal_distance = "5000";
        this.goal_calories = "2000";
        this.goal_active_time = "40";
        this.goal_floor = "10";
        this.goal_sleep = "8";
        this.signup_ip = "";
        this.signup_location = "";
        this.participate = new HashMap<>();
    }

    public User(String id, String email, String name, String photo_url, String role, String privacy
            , String sex, String date_of_birth, String height, String weight, String goal_steps
            , String goal_distance, String goal_calories, String goal_active_time, String goal_floor
            , String goal_sleep, String signup_ip, String signup_location, Map<String, Object> participate) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.photo_url = photo_url;
        this.role = role;
        this.privacy = privacy;
        this.sex = sex;
        this.date_of_birth = date_of_birth;
        this.height = height;
        this.weight = weight;
        this.goal_steps = goal_steps;
        this.goal_distance = goal_distance;
        this.goal_calories = goal_calories;
        this.goal_active_time = goal_active_time;
        this.goal_floor = goal_floor;
        this.goal_sleep = goal_sleep;
        this.signup_ip = signup_ip;
        this.signup_location = signup_location;
        this.participate = participate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getGoal_steps() {
        return goal_steps;
    }

    public void setGoal_steps(String goal_steps) {
        this.goal_steps = goal_steps;
    }

    public String getGoal_distance() {
        return goal_distance;
    }

    public void setGoal_distance(String goal_distance) {
        this.goal_distance = goal_distance;
    }

    public String getGoal_calories() {
        return goal_calories;
    }

    public void setGoal_calories(String goal_calories) {
        this.goal_calories = goal_calories;
    }

    public String getGoal_active_time() {
        return goal_active_time;
    }

    public void setGoal_active_time(String goal_active_time) {
        this.goal_active_time = goal_active_time;
    }

    public String getGoal_floor() {
        return goal_floor;
    }

    public void setGoal_floor(String goal_floor) {
        this.goal_floor = goal_floor;
    }

    public String getGoal_sleep() {
        return goal_sleep;
    }

    public void setGoal_sleep(String goal_sleep) {
        this.goal_sleep = goal_sleep;
    }

    public String getSignup_ip() {
        return signup_ip;
    }

    public void setSignup_ip(String signup_ip) {
        this.signup_ip = signup_ip;
    }

    public String getSignup_location() {
        return signup_location;
    }

    public void setSignup_location(String signup_location) {
        this.signup_location = signup_location;
    }

    public Date getSignup_time() {
        return signup_time;
    }

    public void setSignup_time(Date signup_time) {
        this.signup_time = signup_time;
    }

    public Map<String, Object> getParticipate() {
        return participate;
    }

    public void setParticipate(Map<String, Object> participate) {
        this.participate = participate;
    }
}
