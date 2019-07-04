package main;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vs.database.M_DB;
import vs.database.M_Query;
import main.SCUtils;

/**
 *
 * @author vitor
 */
public class SCProvenance {
    
    static public M_DB db = null;
    
    public static void deleteExecutionWorkflow(String workflowTag, String workflowExecTag) throws SQLException {
        Integer wkfID = getExecutionWorkflowID(workflowExecTag);
        
        ArrayList<String> relations = getRelationFromExecutionWorkflow(workflowExecTag);
        for(String relation : relations){
            deleteRelation(workflowTag, relation, wkfID);
        }
        
        String SQL = "DELETE FROM eworkflow WHERE tagexec=?";
        M_Query q = db.prepQuery(SQL);
        q.setParString(1, workflowExecTag);
        q.executeUpdate();
        q.closeQuery();
    }
    
    public static Integer getExecutionWorkflowID(String workflowExecTag) throws SQLException{
        String SQL = "select ewkfid from eworkflow where tagexec = ?";
        M_Query q = db.prepQuery(SQL);
        q.setParString(1, workflowExecTag);
        ResultSet rs = q.openQuery();
        Integer wkfID = null;
        if (rs.next()) {
            wkfID = rs.getInt("ewkfid");
        }
        rs.close();
        
        return wkfID;
    } 
    
    public static ArrayList<String> getRelationFromExecutionWorkflow(String workflowExecTag) throws SQLException{
        String SQL = "select distinct relationname \n" +
                "from eworkflow as ew, eactivity as ea, ekeyspace as ek \n" +
                "where ew.ewkfid = ea.wkfid \n" +
                "and ea.actid = ek.actid "
                + "and ew.tagexec = ?;";

        M_Query q = db.prepQuery(SQL);
        q.setParString(1, workflowExecTag);
        ResultSet rs = q.openQuery();
        ArrayList<String> relations = new ArrayList<String>();
        while (rs.next()) {
            relations.add(rs.getString("relationname"));
        }
        rs.close();
        
        return relations;
    } 
    
    public static ArrayList<String> getFirstActivities(String workflowTag) throws SQLException{
        String SQL = "SELECT a.tag as tag\n" +
                "FROM (cactivity a JOIN crelation r\n" +
                "ON a.actid = r.actid) JOIN cworkflow w\n" +
                "ON a.wkfid = w.wkfid\n" +
                "WHERE w.tag = ?\n" +
                "AND r.rtype = 'INPUT'\n" +
                "AND r.dependency IS NULL;";

        M_Query q = db.prepQuery(SQL);
        q.setParString(1, workflowTag);
        ResultSet rs = q.openQuery();
        ArrayList<String> activities = new ArrayList<String>();
        while (rs.next()) {
            activities.add(rs.getString("tag"));
        }
        rs.close();
        
        return activities;
    } 

    private static void deleteRelation(String workflowTag, String relation, Integer wkfID) throws SQLException {
        String SQL = "DELETE FROM \"" + workflowTag + "\".\"" + relation + "\" WHERE ewkfid=?";
        M_Query q = db.prepQuery(SQL);
        q.setParInt(1, wkfID);
        q.executeUpdate();
        q.closeQuery();
    }

    public static void runQuery(XMLReader conf) throws SQLException, IOException {
        Connection conn = new Connection(conf.dbServer, 22);
        conn.connect();
        boolean connected = conn.authenticateWithPassword(conf.dbUser, conf.dbPassword);
        
        if(connected){
            if(conf.SQL.endsWith(";")){
                conf.SQL = conf.SQL.substring(0, conf.SQL.length()-1);
            }
            
            String filePath = "SCQueryResult.csv";
            if(conf.dbPath!=null && !conf.dbPath.isEmpty()){
                filePath = conf.dbPath + "/" + filePath;
            }
            
            Session sess = conn.openSession();
            String command = "rm " + filePath;
            sess.execCommand(command);
            
            String query = "COPY (" + conf.SQL + ") TO \'" + filePath + "\' DELIMITER AS \';\' CSV HEADER;";
            
            sess = conn.openSession();
            command = "psql -d " + conf.dbName + " -c \"" + query + "\"";
            sess.execCommand(command);
            
            SCUtils.getDataBySCP(conn, filePath, ".");
            SCUtils.printFirstLevel("The result from your specified query was written in file SCQueryResult.csv");
        }
    }
    
    public static void monitorExecution(XMLReader conf) throws SQLException, IOException {
        Connection conn = new Connection(conf.dbServer, 22);
        conn.connect();
        boolean connected = conn.authenticateWithPassword(conf.dbUser, conf.dbPassword);
        
        if(connected){
            if(conf.SQL.endsWith(";")){
                conf.SQL = conf.SQL.substring(0, conf.SQL.length()-1);
            }
            
            String filePath = "SCMonitorWorkflow.csv";
            if(conf.dbPath!=null && !conf.dbPath.isEmpty()){
                filePath = conf.dbPath + "/" + filePath;
            }
            
            Session sess = conn.openSession();
            String command = "rm " + filePath;
            sess.execCommand(command);
            
            String query = "COPY (" + conf.SQL + ") TO \'" + filePath + "\' DELIMITER AS \';\' CSV HEADER;";
            
            sess = conn.openSession();
            command = "psql -d " + conf.dbName + " -c \"" + query + "\"";
            sess.execCommand(command);
            
            SCUtils.getDataBySCP(conn, filePath, ".");
            SCUtils.printFirstLevel("Analysis about execution progress of workflow " + 
                    conf.getWkfExecTag() + " was written in file SCMonitorWorkflow.csv");
        }
    }

    public static boolean workflowHasFinished(String wkfExecTag) throws SQLException {
        String SQL = "SELECT COUNT(a.actid) counter\n" +
                    "FROM eactivity a JOIN eworkflow w\n" +
                    "ON a.wkfid = w.ewkfid\n" +
                    "WHERE w.tagexec = ?\n" +
                    "AND STATUS <> ?;";

        M_Query q = db.prepQuery(SQL);
        q.setParString(1, wkfExecTag);
        q.setParString(2, "FINISHED");
        ResultSet rs = q.openQuery();
        
        int unfinishedActivities = -1;
        if (rs.next()) {
            unfinishedActivities = rs.getInt("counter");
        }
        rs.close();
        
        boolean finished = false;
        if(unfinishedActivities == 0){
            finished = true;
        }
        
        return finished;
    }

    static boolean hasExecutionWorkflow(String wkfExecTag) throws SQLException {
        String SQL = "SELECT COUNT(ewkfid) counter\n" +
                    "FROM eworkflow\n" +
                    "WHERE tagexec = ?;";

        M_Query q = db.prepQuery(SQL);
        q.setParString(1, wkfExecTag);
        ResultSet rs = q.openQuery();
        
        int workflows = -1;
        if (rs.next()) {
            workflows = rs.getInt("counter");
        }
        rs.close();
        
        boolean hasWorkflow = false;
        if(workflows != 0){
            hasWorkflow = true;
        }
        
        return hasWorkflow;
    }

    public static double getWorkflowElapsedTime(String workflowExecTag) throws SQLException {
        String SQL = "select ( select round(cast (extract(epoch from (max(a.endtime)-min(a.starttime))) AS numeric),1) as elapsedtime\n" +
                    "from eworkflow w, eactivity c, eactivation a\n" +
                    "where c.actid = a.actid \n" +
                    "and ew.ewkfid = w.ewkfid\n" +
                    "and w.ewkfid = c.wkfid ) as time\n" +
                    "from eworkflow as ew \n" +
                    "where ew.tagexec = ?;";

        M_Query q = db.prepQuery(SQL);
        q.setParString(1, workflowExecTag);
        ResultSet rs = q.openQuery();
        
        double elapsedTime = 0.0;
        if (rs.next()) {
            elapsedTime = rs.getDouble("time");
        }
        rs.close();
        
        return elapsedTime;
    }

    public static List<String> getRunningActivities(String workflowExecTag) throws SQLException {
        String SQL = "select distinct ea.tag as eatag \n" +
                "from eworkflow as ew, eactivity as ea \n" +
                "where ew.ewkfid = ea.wkfid \n" +
                "and ew.tagexec = ?;";

        M_Query q = db.prepQuery(SQL);
        q.setParString(1, workflowExecTag);
        ResultSet rs = q.openQuery();
        ArrayList<String> activities = new ArrayList<String>();
        while (rs.next()) {
            activities.add(rs.getString("eatag"));
        }
        rs.close();
        
        return activities;
    }
}
