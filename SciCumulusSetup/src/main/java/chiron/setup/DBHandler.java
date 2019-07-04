package chiron.setup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import vs.database.M_DB;
import vs.database.M_Query;

/**
 *
 * @author jonasdias
 */
public class DBHandler {

    private M_DB db;

    public DBHandler(String server, String port, String name, String username, String password) {
        String connection = "jdbc:postgresql://" + server + ":" + port + "/" + name + "?chartset=UTF8";
        this.db = new M_DB(M_DB.DRIVER_POSTGRESQL, connection, username, password, true);
    }

    public M_DB getDb() {
        return db;
    }

    public void setDb(M_DB db) {
        this.db = db;
    }

    private boolean workflowExists(Workflow w) throws SQLException {
        String sql = "SELECT * FROM public.cworkflow WHERE tag=?";
        M_Query q = db.prepQuery(sql);
        q.setParString(1, w.getTag());
        ResultSet rs = this.openQuery(q);
        if (rs.next()) {
            return true;
        } else {
            return false;
        }
    }

    private List<Relation> existingRelations(Map<String, Activity> activities) throws SQLException {
        List<Relation> existingRelations = new ArrayList<Relation>();
        List<String> tables = this.retrieveTables();
        for (Activity a : activities.values()) {
            for (Relation r : a.getInput()) {
                if (tables.contains(r.getRname())) {
                    existingRelations.add(r);
                }
            }
            for (Relation r : a.getOutput()) {
                if (tables.contains(r.getRname())) {
                    existingRelations.add(r);
                }
            }
        }
        return existingRelations;
    }

    void commitWorkflow(Workflow w) throws SQLException {
//        Create schema
        {
            String sql = "CREATE SCHEMA \"" + w.getTag() + "\"";
            M_Query q = db.prepQuery(sql);
            this.executeUpdate(q);
        }
        
        //checks the existence of the workflow
        if (this.workflowExists(w)) {
            throw new SQLException("A workflow with the tag " + w.getTag() + " is already stored in the database. "
                    + "Please choose a different tag or remove/update the old workflow.");
        } else {
            //check if there are relations in the database with the same name as some of the current relations
            List<Relation> existingRelations = this.existingRelations(w.activities);
            if (existingRelations.size() > 0) {
                throw new SQLException("The following Relation names are already taked in the database, "
                        + "you need to choose different names for your relations: " + existingRelations);
            } else {
                //Inserting the Workflow
                String sql = "SELECT public.f_cworkflow(?,?,?)";
                M_Query q = db.prepQuery(sql);
                q.setParInt(1, 0);
                q.setParString(2, w.getTag());
                q.setParString(3, w.getDescription());
                ResultSet rs = this.openQuery(q);
                if (rs.next()) {
                    w.setId(rs.getInt(1));
                } else {
                    throw new SQLException("Unable to retrive workflow ID from database.");
                }
                //add the atcivities of the workflow
                for (Activity a : w.activities.values()) {
                    sql = "SELECT public.f_cactivity(?,?,?,?,?,?,?,?)";
                    q = db.prepQuery(sql);
                    q.setParInt(1, 0);
                    q.setParInt(2, w.getId());
                    q.setParString(3, a.getTag());
                    q.setParString(4, a.getType().toString());
                    q.setParString(5, a.getDescription());
                    q.setParString(6, a.getActivation());
                    q.setParString(7, a.getExtractor());
                    q.setParString(8, a.getTemplate());
                    rs = this.openQuery(q);
                    if (rs.next()) {
                        a.setId(rs.getInt(1));
                    } else {
                        throw new SQLException("Unable to retrive Activity ID from database.");
                    }
                    // add the relations
                    for (Relation r : a.getInput()) {
                        this.insertRelation(r, a);
                    }
                    for (Relation r : a.getOutput()) {
                        this.insertRelation(r, a);
                    }
                    
                    if(!a.getOperands().isEmpty()){
                        this.insertOperands(a);
                    }
                }
            }
        }
    }

    private void insertRelation(Relation relation, Activity activity) throws SQLException {
        String sql = "SELECT public.f_crelation(?,?,?,?)";
        M_Query q = db.prepQuery(sql);
        q.setParInt(1, activity.getId());
        q.setParString(2, relation.getType().toString());
        q.setParString(3, relation.getRname());
        if (relation.getDependency() != null) {
            q.setParInt(4, relation.getDependency().getId());
        } else {
            q.setParInt(4, null);
        }
        ResultSet rs = this.openQuery(q);
        if (rs.next()) {
            relation.setId(rs.getInt(1));
        } else {
            throw new SQLException("Unable to retrive Relation ID from database.");
        }
        //writes the relation
        if (relation.getDependency() != null) {
            sql = relation.getInputFKSQLStatement(relation.getDependency().getOutput().get(0));
        } else if (relation.getType().equals(Relation.Type.OUTPUT)) {
            q = db.prepQuery(relation.getSQLSequence());
            this.executeUpdate(q);
            q = db.prepQuery(relation.startSequence());
            this.openQuery(q);
            sql = relation.getOutputFKSQLStatement(activity.getInput().get(0));
        } else {
            q = db.prepQuery(relation.getSQLSequence());
            this.executeUpdate(q);
            q = db.prepQuery(relation.startSequence());
            this.openQuery(q);
            sql = relation.getSQLStatement();
        }
        q = db.prepQuery(sql);
        this.executeUpdate(q);
        //adds the index
        q = db.prepQuery(relation.getSQLUpdate());
        this.executeUpdate(q);
        this.insertFields(relation);
    }

    private void insertFields(Relation relation) throws SQLException {
        for (Field f : relation.getFields()) {
            String sql = "INSERT INTO public.cfield(fname, relid, ftype, decimalplaces, fileoperation, instrumented) VALUES(?,?,?,?,?,?)";
            M_Query q = db.prepQuery(sql);
            q.setParString(1, f.getFname());
            q.setParInt(2, relation.getId());
            q.setParString(3, f.getFtype());

            if (f.getDecimalplaces() != -1) {
                q.setParInt(4, f.getDecimalplaces());
            } else {
                q.setParInt(4, null);
            }
            q.setParString(5, f.getFileoperation());
            q.setParString(6, f.getInstrumented());
            this.executeUpdate(q);
        }
    }

    private void insertOperands(Activity a) throws SQLException {
        for(Operand op : a.getOperands()){
            String sql = "INSERT INTO public.coperand(actid, oname, numericvalue, textvalue) VALUES(?,?,?,?)";
            M_Query q = db.prepQuery(sql);
            q.setParInt(1, a.getId());
            q.setParString(2, op.name);
            q.setParDouble(3, (double) op.numericValue);
            q.setParString(4, op.textValue);
            this.executeUpdate(q);
        }
    }

    void updateWorkflow(String xmlFile) {
        XMLUpdateFinder u = new XMLUpdateFinder();
        u.workflowUpdate(xmlFile);
    }

    void deleteWorkflow(Workflow w, String server, String port, String name, String username, String password) throws SQLException {
        String wkftag = w.getTag();
        DBReader r = new DBReader(server, port, name, username, password, wkftag);
        Workflow workflow = r.getWorkflow();
        
        if(workflow!=null){
            String deleteMachines = "DELETE FROM emachine\n" +
                            "WHERE machineid in (\n" +
                                "	SELECT DISTINCT ac.machineid\n" +
                                "    FROM eactivation ac JOIN eactivity a\n" +
                                "    ON ac.actid = a.actid\n" +
                                "    JOIN eworkflow w\n" +
                                "    ON a.wkfid = w.ewkfid\n" +
                                "    WHERE w.tag = ?\n" +
                            ")";
            M_Query q = db.prepQuery(deleteMachines);
            q.setParString(1, w.getTag());
            q.executeUpdate();
            
            for (Activity a : r.getActivities()) {
                String sql = a.getDropStatement();
                q = db.prepQuery(sql);
                this.executeUpdate(q);
            }
            String dropWorkflow = "DELETE FROM public.cworkflow WHERE tag=?";
            q = db.prepQuery(dropWorkflow);
            q.setParString(1, w.getTag());
            this.executeUpdate(q);

            //        Drop schema
            String sql = "DROP SCHEMA \"" + w.getTag() + "\"";
            q = db.prepQuery(sql);
            this.executeUpdate(q);
        }else{
            System.out.println("Provenance database does not have conceptual workflow with tag " + wkftag);
        }
    }

    /**
     * For a given database, it retrieve the tables available for querying.
     *
     * @return The list of available tables
     * @throws SQLException
     */
    public List<String> retrieveTables() throws SQLException {
        String q = "SELECT relname FROM pg_stat_user_tables WHERE schemaname='public'";
        M_Query query = db.prepQuery(q);
        ResultSet r = this.openQuery(query);
        List result = new ArrayList<String>();
        while (r.next()) {
            result.add(r.getString("relname"));
        }
        return result;
    }

    private void executeUpdate(M_Query q) {
        try {
            q.executeUpdate();
        } catch (SQLException ex) {
            String sql = q.getSt().toString();
            System.err.println("SQL Exception: " + ex.getMessage());
            System.err.println("SQL Statement: " + sql);
        }
    }

    private ResultSet openQuery(M_Query q) {
        ResultSet rs = null;
        try {
            rs = q.openQuery();
            return rs;
        } catch (SQLException ex) {
            String sql = q.getSt().toString();
            System.err.println("SQL Exception: " + ex.getMessage());
            System.err.println("SQL Statement: " + sql);
        }
        return rs;
    }
}