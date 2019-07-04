package chiron.setup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import vs.database.M_DB;
import vs.database.M_Query;

/**
 *
 * @author Matheus Costa
 */
public class DBReader {

    private Workflow workflow;
    private ArrayList<Activity> activities;
    private ArrayList<Relation> relations;
    private ArrayList<Field> fields;
    private String connection, wkftag;
    private M_DB db;

    //CONSTRUCTOR
    public DBReader(String server, String port, String name, String username, String password, String wkftag) throws SQLException {
        this.connection = "jdbc:postgresql://" + server + ":" + port + "/" + name + "?chartset=UTF8";
        this.db = new M_DB(M_DB.DRIVER_POSTGRESQL, connection, username, password, true);
        this.activities = new ArrayList<Activity>();
        this.relations = new ArrayList<Relation>();
        this.fields = new ArrayList<Field>();
        this.wkftag = wkftag;

        this.retrieveWorkflow(this.wkftag);
        
        if(this.workflow!=null){
            this.retrieveActivities(this.workflow.getId());
            for (Activity a : this.activities) {
                this.retrieveRelations(a.getActid());
            }
            this.retrieveDependencies();
            for (Relation r : this.relations) {
                this.retrieveFields(r.getRelid());
            }
            this.buildWorkflow();
        }
    }

    //GETTER AND SETTER
    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public ArrayList<Activity> getActivities() {
        return activities;
    }

    public void setActivities(ArrayList<Activity> activities) {
        this.activities = activities;
    }

    public ArrayList<Relation> getRelations() {
        return relations;
    }

    public void setRelations(ArrayList<Relation> relations) {
        this.relations = relations;
    }

    public ArrayList<Field> getFields() {
        return fields;
    }

    public void setFields(ArrayList<Field> fields) {
        this.fields = fields;
    }

    public String getWkftag() {
        return wkftag;
    }

    public void setWkftag(String wkftag) {
        this.wkftag = wkftag;
    }

    public M_DB getDb() {
        return db;
    }

    public void setDb(M_DB db) {
        this.db = db;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    //RETRIEVE DATA FROM DATABASE
    public void retrieveWorkflow(String tag) throws SQLException {
        String q = "SELECT * FROM public.cworkflow WHERE tag = ?";
        M_Query query = db.prepQuery(q);
        query.setParString(1, tag);
        ResultSet r = query.openQuery();
        while (r.next()) {
            this.workflow = new Workflow(r.getInt("wkfid"), r.getString("tag"), r.getString("description"));
            this.workflow.setIndexRel(this.setIdRelations());
            this.workflow.setIndexAct(this.setIdActivities());
        }
    }

    public void retrieveActivities(int wkfid) throws SQLException {
        String q = "SELECT * FROM public.cactivity WHERE wkfid = ?";
        M_Query query = db.prepQuery(q);
        query.setParInt(1, wkfid);
        ResultSet r = query.openQuery();
        while (r.next()) {
            Activity a = new Activity(workflow.getTag(), r.getInt("actid"), r.getInt("wkfid"), r.getString("tag"), r.getString("atype"), r.getString("description"), r.getString("templatedir"), r.getString("activation"), r.getString("extractor"), r.getString("constrained"));
            this.activities.add(a);
        }
    }

    public void retrieveRelations(int actid) throws SQLException {
        String q = "SELECT * FROM public.crelation WHERE actid = ?";
        M_Query query = db.prepQuery(q);
        query.setParInt(1, actid);
        ResultSet r = query.openQuery();
        while (r.next()) {
            Relation rel = new Relation(workflow.getTag(), r.getInt("relid"), r.getInt("actid"), r.getString("rtype"), r.getString("rname"), r.getInt("dependency"));
            this.relations.add(rel);
        }
    }

    public void retrieveDependencies() {
        for (Relation r : this.relations) {
            if (r.getDependencyDb() != 0) {
                for (Activity a : this.activities) {
                    if (a.getActid() == r.getDependencyDb()) {
                        r.setDependencyUp(a.getTag());
                    }
                }
            }
        }
    }

    public void retrieveFields(int relid) throws SQLException {
        String q = "SELECT * FROM public.cfield WHERE relid = ?";
        M_Query query = db.prepQuery(q);
        query.setParInt(1, relid);
        ResultSet r = query.openQuery();
        while (r.next()) {
            Field f = new Field(r.getInt("relid"), r.getInt("decimalplaces"), r.getString("fname"), r.getString("ftype"), r.getString("fileoperation"), r.getString("instrumented"));
            this.fields.add(f);
        }
    }

    public int setIdActivities() throws SQLException {
        int index = 0;
        String q = "SELECT * FROM public.cactivity";
        M_Query query = db.prepQuery(q);
        ResultSet r = query.openQuery();
        while (r.next()) {
            int i = r.getInt("actid");
            if (i > index) {
                index = i;
            }
        }
        return index;
    }

    public int setIdRelations() throws SQLException {
        int index = 0;
        String q = "SELECT * FROM public.crelation";
        M_Query query = db.prepQuery(q);
        ResultSet r = query.openQuery();
        while (r.next()) {
            int i = r.getInt("relid");
            if (i > index) {
                index = i;
            }
        }
        return index;
    }

    //LINK THE DATA STRUCTS
    public void buildWorkflow() {
        for (Activity a : this.activities) {
            if (a.getWkfid() == this.workflow.getId()) {
                this.workflow.addActivity(a);
            }
            for (Relation r : this.relations) {
                if (r.getActid() == a.getActid()) {
                    a.addRelation(r);
                }
                for (Field f : this.fields) {
                    if (f.getRelid() == r.getRelid()) {
                        r.addField(f);
                    }
                }
            }
        }
        this.linkRelations();
    }

    public void linkRelations(){
        for(Activity a : this.getActivities()){
            for(Relation r : a.getRelations()){
                if(r.getRtype().equalsIgnoreCase("input")){
                    a.addInputRelation(r);
                }
                if(r.getRtype().equalsIgnoreCase("output")){
                    a.addOutputRelation(r);
                }
            }
        }
    }
}