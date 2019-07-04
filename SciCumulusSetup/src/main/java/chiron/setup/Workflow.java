package chiron.setup;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Workflow {

    private int id;
    private int indexRel;
    private int indexAct;
    private String tag;
    private String description;
    private ArrayList<Activity> activitiesUp;
    public LinkedHashMap<String, Activity> activities;

    public Workflow(String tag, String description) {
        this.tag = tag;
        this.description = description;
        activities = new LinkedHashMap<String, Activity>();
        this.activitiesUp = new ArrayList<Activity>();
    }

    public Workflow(int id, String tag, String description) {
        this.id = id;
        this.tag = tag;
        this.description = description;
        this.activitiesUp = new ArrayList<Activity>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndexRel() {
        return indexRel;
    }

    public void setIndexRel(int indexRel) {
        this.indexRel = indexRel;
    }

    public int getIndexAct() {
        return indexAct;
    }

    public void setIndexAct(int indexAct) {
        this.indexAct = indexAct;
    }

    public String getTag() {
        return tag.toLowerCase();
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Activity> getActivitiesUp() {
        return activitiesUp;
    }

    public void setActivitiesUp(ArrayList<Activity> activitiesUp) {
        this.activitiesUp = activitiesUp;
    }

    public ArrayList<Activity> getActivities() {
        return activitiesUp;
    }

    public void setActivities(ArrayList<Activity> activitiesUp) {
        this.activitiesUp = activitiesUp;
    }

    public void addActivity(Activity a) {
        if (!this.activitiesUp.contains(a)) {
            this.activitiesUp.add(a);
        }
    }

    @Override
    public String toString() {
        return "\n\nWorkflow{" + "tag=" + tag + ", description=" + description + ", activities=" + activitiesUp + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + (this.description != null ? this.description.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Workflow other = (Workflow) obj;
        if ((this.description == null) ? (other.description != null) : !this.description.equalsIgnoreCase(other.description)) {
            return false;
        }
        return true;
    }
}