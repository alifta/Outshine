package ca.mun.outshine.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.Map;

import ca.mun.outshine.R;

@IgnoreExtraProperties
public class Challenge {

    private String id;
    private String name;
    private String type;
    private String creator;
    private String privacy;
    // private Date time_created;
    private @ServerTimestamp
    Date time_created;
    private Date time_starts;
    private Date time_ends;
    private Map<String, Object> competitors;

    @Exclude private int imageResource;

    public Challenge() {
    }

    public Challenge(String name, String type, String creator, String privacy, Date time_starts
            , Date time_ends, Map<String, Object> competitors) {
        this.id = "";
        this.name = name;
        this.type = type;
        this.creator = creator;
        this.privacy = privacy;
        this.time_starts = time_starts;
        this.time_ends = time_ends;
        this.competitors = competitors;
        setImageResource(type);
    }

    @Exclude public int getImageResource() {
        return imageResource;
    }

    @Exclude private void setImageResource(String type) {
        switch (type) {
            case "steps":
                this.imageResource = R.drawable.ic_steps;
                break;
            case "distance":
                this.imageResource = R.drawable.ic_distance;
                break;
            case "calories":
                this.imageResource = R.drawable.ic_calories;
                break;
//            case "active":
//                this.imageResource = R.drawable.ic_active;
//                break;
            default:
                this.imageResource = R.drawable.ic_steps;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public Date getTime_created() {
        return time_created;
    }

    public void setTime_created(Date time_created) {
        this.time_created = time_created;
    }

    public Date getTime_starts() {
        return time_starts;
    }

    public void setTime_starts(Date time_starts) {
        this.time_starts = time_starts;
    }

    public Date getTime_ends() {
        return time_ends;
    }

    public void setTime_ends(Date time_ends) {
        this.time_ends = time_ends;
    }

    public Map<String, Object> getCompetitors() {
        return competitors;
    }

    public void setCompetitors(Map<String, Object> competitors) {
        this.competitors = competitors;
    }

    @Exclude public void addCompetitor(String competitor_id, Map<String, Object> info) {
        this.competitors.put(competitor_id, info);
    }
}