package chiron.cloud;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import chiron.ChironUtils;
import chiron.XMLReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import vs.database.M_DB;
import vs.database.M_Query;

/**
 *
 * @author vitor
 */
public class CloudProvenance {
    
    static public M_DB db = null;
    
    public static void deleteExecutionWorkflow(String workflowExecTag) throws SQLException {
        Integer wkfID = getExecutionWorkflowID(workflowExecTag);
        
        ArrayList<String> relations = getRelationFromExecutionWorkflow(workflowExecTag);
        for(String relation : relations){
            deleteRelation(relation, wkfID);
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

    private static void deleteRelation(String relation, Integer wkfID) throws SQLException {
        String SQL = "DELETE FROM " + relation + " WHERE ewkfid=?";
        M_Query q = db.prepQuery(SQL);
        q.setParInt(1, wkfID);
        q.executeUpdate();
        q.closeQuery();
    }

    public static void monitorExecutionWorkflow(XMLReader conf) throws SQLException, IOException {
        Connection conn = new Connection(conf.dbServer, 22);
        conn.connect();
        boolean connected = conn.authenticateWithPassword(conf.dbUser, conf.dbPassword);
        
        if(connected){
            String SQL = conf.SQL;
            if(SQL.endsWith(";")){
                SQL = SQL.substring(0, SQL.length()-1);
            }
            String filePath = "/var/lib/pgsql/SCQueryResult.csv";
            
            Session sess = conn.openSession();
            String command = "rm " + filePath;
            sess.execCommand(command);
            
            String query = "COPY (" + SQL + ") TO \'" + filePath + "\' DELIMITER AS \';\' CSV HEADER;";
            
            sess = conn.openSession();
            command = "psql -d " + conf.dbName + " -c \"" + query + "\"";
            sess.execCommand(command);
            
            ChironUtils.getDataBySCP(conn, filePath, ".");
        }
    }
    
    public static void runQuery(XMLReader conf, String SQL) throws SQLException, IOException {
        Connection conn = new Connection(conf.dbServer, 22);
        conn.connect();
        boolean connected = conn.authenticateWithPassword(conf.dbUser, conf.dbPassword);
        
        if(connected){
            if(SQL.endsWith(";")){
                SQL = SQL.substring(0, SQL.length()-1);
            }
            String filePath = "/var/lib/pgsql/SCMonitorWorkflow.csv";
            
            Session sess = conn.openSession();
            String command = "rm " + filePath;
            sess.execCommand(command);
            
            String query = "COPY (" + SQL + ") TO \'" + filePath + "\' DELIMITER AS \';\' CSV HEADER;";
            
            sess = conn.openSession();
            command = "psql -d " + conf.dbName + " -c \"" + query + "\"";
            sess.execCommand(command);
            
            ChironUtils.getDataBySCP(conn, filePath, ".");
        }
    }
}
