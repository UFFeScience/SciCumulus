package br.uff.scicumulus.core.workflow;

import java.util.List;

public class Workflow {
    private final String id;
    private final List<Activity> activities;

    public Workflow(String id, List<Activity> activities) {
        this.id = id;
        this.activities = activities;
    }

    public String getId() {
        return id;
    }

    public List<Activity> getActivities() {
        return activities;
    }
}
