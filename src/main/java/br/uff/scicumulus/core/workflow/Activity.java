package br.uff.scicumulus.core.workflow;

public class Activity {
    private final String id;
    private final String command;

    public Activity(String id, String command) {
        this.id = id;
        this.command = command;
    }

    public String getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }
}
