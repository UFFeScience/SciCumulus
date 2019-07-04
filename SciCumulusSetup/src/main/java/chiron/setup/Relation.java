package chiron.setup;

import java.util.ArrayList;

public class Relation {

    private String workflowTag;
    private String rname;
    private String rtype;
    private String dependencyUp;
    private Type type;
    private ArrayList<Field> fields;
    private Activity dependency = null;
    private int id;
    private int relid;
    private int actid;
    private int dependencyDb;

    public Relation(String workflowTag, String name, Type type) {
        this.workflowTag = workflowTag;
        this.rname = name;
        this.type = type;
        this.fields = new ArrayList<Field>();
    }

    public Relation(String workflowTag, String rtype, String rname, String dependencyUp) {
        this.workflowTag = workflowTag;
        this.rtype = rtype;
        this.rname = rname;
        this.dependencyUp = dependencyUp;
        this.fields = new ArrayList<Field>();
    }

    public Relation(String workflowTag, int relid, int actid, String rtype, String rname, int dependencyDb) {
        this.workflowTag = workflowTag;
        this.relid = relid;
        this.actid = actid;
        this.rtype = rtype;
        this.rname = rname;
        this.dependencyDb = dependencyDb;
        this.fields = new ArrayList<Field>();
    }

    public enum Type {

        INPUT, OUTPUT;
    }

    public String getRname() {
        return rname.toLowerCase();
    }

    public String getRname2() {
        return this.rname;
    }

    public void setRname(String rname) {
        this.rname = rname;
    }

    public String getRtype() {
        return rtype;
    }

    public void setRtype(String rtype) {
        this.rtype = rtype;
    }

    public String getDependencyUp() {
        return dependencyUp;
    }

    public void setDependencyUp(String dependencyUp) {
        this.dependencyUp = dependencyUp;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ArrayList<Field> getFields() {
        return fields;
    }

    public void setFields(ArrayList<Field> fields) {
        this.fields = fields;
    }

    public Activity getDependency() {
        return dependency;
    }

    public void setDependency(Activity dependency) {
        this.dependency = dependency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRelid() {
        return relid;
    }

    public void setRelid(int relid) {
        this.relid = relid;
    }

    public int getActid() {
        return actid;
    }

    public void setActid(int actid) {
        this.actid = actid;
    }

    public int getDependencyDb() {
        return dependencyDb;
    }

    public void setDependencyDb(int dependencyDb) {
        this.dependencyDb = dependencyDb;
    }

    public void addField(Field field) {
        if (!fields.contains(field)) {
            fields.add(field);
        }
    }

    public String getSQLCreateStatement() {
        String separator = System.getProperty("line.separator");
        String statement = "CREATE TABLE \"" + workflowTag + "\".\"" + this.getRname() + "\" ( " + separator;
        statement += "ewkfid integer NOT NULL," + separator;
        if ((this.getDependencyUp() == null) && (this.getRtype().equalsIgnoreCase("input"))) {
            statement += "ik integer NOT NULL DEFAULT nextval(('" + this.getRname().toLowerCase() + "_seq'::text)::regclass)," + separator;
        } else {
            if (this.getRtype().equalsIgnoreCase("input")) {
                statement += "ik integer NOT NULL," + separator;
            }
        }
        if (this.getRtype().equalsIgnoreCase("output")) {
            statement += "ik integer NOT NULL," + separator + "ok integer UNIQUE NOT NULL DEFAULT nextval(('" + this.getRname().toLowerCase() + "_seq'::text)::regclass)," + separator;
        }

        for (int i = 0; i < fields.size(); i++) {
            if (i != 0) {
                statement += "," + separator;
            }
            statement += fields.get(i).getFname() + " " + fields.get(i).getFtypetoSQL();
        }
        statement += separator + ")";
        return statement;
    }

    private String basicInputCreateStatement() {
        String separator = System.getProperty("line.separator");
        String statement = "CREATE TABLE \"" + workflowTag + "\"." + this.getRname() + " ( " + separator;
        statement += "ewkfid integer NOT NULL," + separator;
        statement += "ik integer NOT NULL," + separator;

        for (int i = 0; i < fields.size(); i++) {
            if (i != 0) {
                statement += "," + separator;
            }
            statement += fields.get(i).getFname() + " " + fields.get(i).getFtypetoSQL();
        }
        return statement;
    }

    private String workflowInputCreateStatement() {
        String separator = System.getProperty("line.separator");
        String statement = "CREATE TABLE \"" + workflowTag + "\"." + this.getRname() + " ( " + separator;
        statement += "ewkfid integer NOT NULL," + separator;
        statement += "ik integer DEFAULT nextval(('" + workflowTag + "." + this.getRname().toLowerCase() + "_seq'::text)::regclass) NOT NULL," + separator;

        for (int i = 0; i < fields.size(); i++) {
            if (i != 0) {
                statement += "," + separator;
            }
            statement += fields.get(i).getFname() + " " + fields.get(i).getFtypetoSQL();
        }
        return statement;
    }

    private String outputCreateStatement() {
        String separator = System.getProperty("line.separator");
        String statement = "CREATE TABLE \"" + workflowTag + "\"." + this.getRname() + " ( " + separator;
        statement += "ewkfid integer NOT NULL," + separator;
        statement += "ik integer NOT NULL," + separator
                + "ok integer DEFAULT nextval(('" + workflowTag + "." + this.getRname().toLowerCase() + "_seq'::text)::regclass) NOT NULL," + separator;
        for (int i = 0; i < fields.size(); i++) {
            if (i != 0) {
                statement += "," + separator;
            }
            statement += fields.get(i).getFname() + " " + fields.get(i).getFtypetoSQL();
        }
        return statement;
    }

    public String getSQLStatement() {
        String separator = System.getProperty("line.separator");
        String statement = this.workflowInputCreateStatement();
        statement += "," + separator + "CONSTRAINT " + this.getRname() + "_pkey PRIMARY KEY (ewkfid, ik)";
        statement += separator + ")";
        return statement;
    }

    public String getInputFKSQLStatement(Relation r) {
        String separator = System.getProperty("line.separator");
        String statement = this.basicInputCreateStatement();
        statement += "," + separator + "CONSTRAINT " + this.getRname() + "_pkey PRIMARY KEY (ewkfid, ik)";
        statement += "," + separator + "CONSTRAINT " + this.getRname() + "_fk FOREIGN KEY (ewkfid, ik) REFERENCES \"" + workflowTag + "\"." + r.getRname() + " (ewkfid, ok) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE";
        statement += separator + ")";
        return statement;
    }

    public String getOutputFKSQLStatement(Relation r) {
        String separator = System.getProperty("line.separator");
        String statement = this.outputCreateStatement();
        statement += "," + separator + "CONSTRAINT " + this.getRname() + "_pkey PRIMARY KEY (ewkfid, ok)";
        statement += "," + separator + "CONSTRAINT " + this.getRname() + "_fk FOREIGN KEY (ewkfid, ik) REFERENCES \"" + workflowTag + "\"." + r.getRname() + " (ewkfid, ik) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE";
        statement += separator + ")";
        return statement;
    }

    public String getSQLSequence() {
        String separator = System.getProperty("line.separator");
        String statement = separator + "CREATE SEQUENCE \"" + workflowTag + "\"." + this.getRname().toLowerCase() + "_seq" + separator + "INCREMENT BY 1" + separator + "NO MAXVALUE" + separator + "MINVALUE 0" + separator + "START 0" + separator + "CACHE 1;" + separator;;
        return statement;
    }

    public String startSequence() {
        String statement = "SELECT pg_catalog.nextval('" + workflowTag + "." + this.getRname().toLowerCase() + "_seq')";
        return statement;
    }

    public String getSQLUpdate() {
        String separator = System.getProperty("line.separator");
        String statement = "";
        if (this.type.equals(Type.OUTPUT)) {
            statement = "ALTER TABLE \"" + workflowTag + "\"." + this.getRname()
                    + " CLUSTER ON " + this.getRname() + "_pkey;" + separator
                    + "CREATE UNIQUE INDEX " + this.getRname() + "_index ON \""
                    + workflowTag + "\"." + this.getRname() + " USING btree (ik, ok);" + separator
                    + "CREATE UNIQUE INDEX " + this.getRname() + "_key_index ON \""
                    + workflowTag + "\"." + this.getRname() + " USING btree (ok);";
        } else {
            statement = "ALTER TABLE \"" + workflowTag + "\"." + this.getRname()
                    + " CLUSTER ON " + this.getRname() + "_pkey;" + separator
                    + "CREATE UNIQUE INDEX " + this.getRname() + "_key_index ON \""
                    + workflowTag + "\"." + this.getRname() + " USING btree (ik);";
        }
        return statement;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.rtype != null ? this.rtype.hashCode() : 0);
        hash = 47 * hash + (this.rname != null ? this.rname.hashCode() : 0);
        hash = 47 * hash + (this.dependencyUp != null ? this.dependencyUp.hashCode() : 0);
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
        final Relation other = (Relation) obj;
        if ((this.rtype == null) ? (other.rtype != null) : !this.rtype.equalsIgnoreCase(other.rtype)) {
            return false;
        }
        if ((this.rname == null) ? (other.rname != null) : !this.rname.equalsIgnoreCase(other.rname)) {
            return false;
        }
        if ((this.dependencyUp == null) ? (other.dependencyUp != null) : !this.dependencyUp.equalsIgnoreCase(other.dependencyUp)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (this.dependencyUp != null) {
            return "\n\t\tRelation{" + "rname=" + rname + ", rtype=" + rtype + ", dependencyUp=" + dependencyUp + ", fields=" + fields + '}';
        } else {
            return "\n\t\tRelation{" + "rname=" + rname + ", rtype=" + rtype + ", fields=" + fields + '}';
        }
    }
}