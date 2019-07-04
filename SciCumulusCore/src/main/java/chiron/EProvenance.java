package chiron;

import chiron.concept.*;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import vs.database.M_DB;
import vs.database.M_Query;

/**
 * Provenance data storage class. Stores and retrieves data from the Chiron's
 * provenance database.
 *
 * @author Vítor, Eduardo, Jonas
 * @since 2010-12-25
 */
public class EProvenance {

    static public M_DB db = null;
    public static CWorkflow workflow = null;
    public static EWorkflow eworkflow = null;
    public static String workflowIDField = "EWKFID";

    static void storeWorkflow(M_DB db, EWorkflow workflow) throws SQLException {
        if (db == null) {
            return;
        }
        String SQL = "select public.f_workflow(?,?,?,?,?,?,?,?,?);";

        M_Query q = db.prepQuery(SQL);
        q.setParInt(1, workflow.wkfId);
        q.setParString(2, workflow.tag);
        q.setParString(3, workflow.exeTag);
        q.setParString(4, workflow.expDir);
        q.setParString(5, workflow.wfDir);
        q.setParInt(6, workflow.maximumFailures);
        q.setParString(7, workflow.userInteraction ? "T" : "F");
        q.setParDouble(8, workflow.reliability);
        q.setParString(9, workflow.redundancy ? "T" : "F");
        ResultSet rs = q.openQuery();
        if (rs.next()) {
            workflow.wkfId = rs.getInt(1);
        }
        rs.close();
    }
    
    static void storeMachine(M_DB db, EMachine machine) throws SQLException {
        if (db == null) {
            return;
        }
        String SQL = "SELECT machineid "
                + "FROM emachine "
                + "WHERE hostname = ? "
                + "AND address = ?;";
        M_Query q = db.prepQuery(SQL);
        q.setParString(1, machine.publicDNS);
        q.setParString(2, machine.publicIP);
        ResultSet rs = q.openQuery();
        if (rs.next()) {
            machine.ID = rs.getInt(1);
        }
        rs.close();
        
        SQL = "select public.f_emachine(?,?,?,?);";
        q = db.prepQuery(SQL);
        q.setParInt(1, machine.ID);
        q.setParString(2, machine.publicDNS);
        q.setParString(3, machine.publicIP);
        q.setParDouble(4, machine.MflopsPerSecond);
        rs = q.openQuery();
        if (rs.next()) {
            machine.ID = rs.getInt(1);
        }
        rs.close();
    }

    public static void storeActivity(EActivity act) throws SQLException {
        if (db == null) {
            return;
        }
        String SQL = "select public.f_activity(?,?,?,?,?,?,?);";

        M_Query q = db.prepQuery(SQL);
        q.setParInt(1, act.id);
        q.setParInt(2, act.workflow.wkfId);
        q.setParString(3, act.tag);
        q.setParString(4, act.status.toString());
        q.setParDate(5, act.startTime);
        q.setParDate(6, act.endTime);
        q.setParInt(7, act.cactid);
        ResultSet rs = q.openQuery();
        if (rs.next()) {
            act.id = rs.getInt(1);
        }
    }

    private static void storeFile(EFile file, EActivation activation) throws SQLException {
        if (!Chiron.mainNode) {
            return;
        }
        String SQL = "select public.f_file(?,?,?,?,?,?,?,?,?,?,?);";

        File f = new File(file.getPath());
        if (f.exists()) {
            file.fileSize = (int) f.length();
            file.fileData = new Date(f.lastModified());
        }

        M_Query q = db.prepQuery(SQL);
        q.setParInt(1, file.fileID);
        q.setParInt(2, activation.activityID);
        q.setParInt(3, activation.id);
        q.setParString(4, file.template ? "T" : "F");
        q.setParString(5, file.instrumented ? "T" : "F");
        q.setParString(6, file.getFileDir());
        q.setParString(7, file.getFileName());
        q.setParInt(8, file.fileSize);
        q.setParDate(9, file.fileData);
        q.setParString(10, file.fileOper.toString());
        q.setParString(11, file.fieldName);

        ResultSet rs = q.openQuery();
        if (rs.next()) {
            file.fileID = (rs.getInt(1));
        }
    }

    public static EActivation callActivationFunction(EActivation activation) throws SQLException{
        String SQL = "select public.f_activation(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        M_Query q = db.prepQuery(SQL);
        q.setParInt(1, activation.id);
        q.setParInt(2, activation.activityID);
        q.setParInt(3, activation.machineID);
        q.setParInt(4, activation.exitStatus);
        q.setParString(5, activation.commandLine);
        q.setParString(6, activation.workspace);
        q.setParInt(7, activation.failureTries);
        q.setParString(8, activation.stdErr);
        q.setParString(9, activation.stdOut);
        q.setParDate(10, activation.startTime);
        q.setParDate(11, activation.endTime);
        q.setParString(12, activation.status.toString());
        q.setParString(13, activation.extractor);
        if (activation.constrained) {
            q.setParString(14, "T");
        } else {
            q.setParString(14, "F");
        }
        q.setParString(15, activation.templateDir);
        ResultSet rs;
        try {
            rs = q.openQuery();

        } catch (SQLException ex) {
            if (ex.getMessage().contains("UTF")) {
                /*
                 * UTF problems for output files: since the output depends on
                 * the scientific application it is hard to control which
                 * characters they write in their output files. Besides,
                 * searching for problems in files would degrades Chiron's
                 * perforamce the solution so far is to point the path to the
                 * file and try transaction again
                 */
                activation.stdErr = activation.workspace + "EErr.txt";
                activation.stdOut = activation.workspace + "EResult.txt";
                q.setParString(8, activation.stdErr);
                q.setParString(9, activation.stdOut);
                rs = q.openQuery();
            } else {
                throw ex;
            }
        }
        if (rs.next()) {
            activation.id = (rs.getInt(1));
        }
        q.closeQuery();
        
        return activation;
    }

    public static void storeActivation(EActivation activation) throws SQLException, IOException {
        if (db == null) {
            return;
        }
        
        storeRelations(activation);
        activation = callActivationFunction(activation);
        
        for (EFile file : activation.files) {
            storeFile(file, activation);
        }

        storeKeySpace(activation, activation.inputRelation, "INPUT");
        storeKeySpace(activation, activation.outputRelation, "OUTPUT");
    }
    
    public static void storeRelations(EActivation activation) throws SQLException, IOException{
        if (activation.pipelinedFrom != null) {
            //if the activation was pipelined from another one, it needs to retrieve
            //the generated OK for the last activation
            String okSelect = "SELECT ok FROM \"" + workflow.tag + "\"." + activation.pipelinedFrom.outputRelation.name
                    + " " + getWhereClause(eworkflow.wkfId, activation.pipelinedFrom.outputRelation.getFirstKey());
            M_Query okSelection = db.prepQuery(okSelect);
            ResultSet okrs = okSelection.openQuery();
            int newKey;
            if (okrs.next()) {
                newKey = okrs.getInt("ok");
            } else {
                throw new NullPointerException("No output inserted in previous activity, could not pipeline");
            }
            activation.inputRelation.resetKey(activation.inputRelation.getFirstKey(), newKey);
            activation.inputRelation.values = activation.pipelinedFrom.outputRelation.values;

            if (!activation.inputRelation.isEmpty()) {
                storeInputRelation(activation.inputRelation, activation);
            }
        }
        
        if (!activation.outputRelation.isEmpty()) {
            storeOutputRelation(activation.outputRelation, activation.inputRelation.getFirstKey());
        }
    }

    /**
     * Stores the KeySpace of an EActivation. This is important because the
     * generateActivations() method in CActivity first creates the activations
     * and stores them in the database. Later, those activations are retrieved
     * from the database. It is important to know which data each activation
     * will consume from their input Relation.
     *
     * @param activation The activation related to the keyspace
     * @param relation The relation that the KeySpace refers to
     * @param type A String saying if the relatioin is and INPUT or an OUTPUT
     * @throws SQLException
     */
    private static void storeKeySpace(EActivation activation, ERelation relation, String type) throws SQLException {
        String sql = "SELECT public.f_ekeyspace(?,?,?,?,?,?)";
        M_Query q = db.prepQuery(sql);
        q.setParInt(1, activation.id);
        q.setParInt(2, activation.activityID);
        q.setParString(3, relation.name);

        Integer inputFirstKey = relation.getFirstKey();
        if (inputFirstKey != null) {
            q.setParInt(4, inputFirstKey);
        } else {
            q.setParInt(4, null);
        }
        Integer inputLastKey = relation.getLastKey();
        if (inputLastKey != null) {
            q.setParInt(5, inputLastKey);
        } else {
            q.setParInt(5, null);
        }
        q.setParString(6, type);
        q.openQuery();
    }

//    failure_handling
    static protected EActivation loadReadyActivation(M_DB db, EActivity act) throws SQLException {
        if (db == null) {
            return null;
        }
        
        EActivation activation = null;
        String sql = "select taskid from public.eactivation where status = ? and actid = ? and failure_tries < ? order by taskid limit 1";
        M_Query q = db.prepQuery(sql);
        q.setParString(1, EActivity.StatusType.READY.toString());
        q.setParInt(2, (int) act.id);
        q.setParInt(3, act.workflow.maximumFailures);
        ResultSet rs = q.openQuery();

        if (rs.next()) {
//            failure_handling
            int activationid = rs.getInt("taskid");
            activation = loadActivation(act, activationid);
            String sqlUpd = "update public.eactivation set status = ?, starttime = ? where actid = ? and taskid = ?";
            M_Query qryUpd = db.prepQuery(sqlUpd);
            qryUpd.setParString(1, EActivity.StatusType.RUNNING.toString());
            qryUpd.setParDate(2, new Date());
            qryUpd.setParInt(3, (int) activation.activityID);
            qryUpd.setParInt(4, (int) activation.id);
            qryUpd.executeUpdate();
        }           
        
        return activation;
    }
    
    static protected void updateRunningActivations(M_DB db, EActivity act) throws SQLException{
        EActivation activation = null;
        String sql = "select taskid from public.eactivation where status = ? and actid = ? order by taskid";
        M_Query q = db.prepQuery(sql);
        q.setParString(1, EActivity.StatusType.RUNNING.toString());
        q.setParInt(2, (int) act.id);
        ResultSet rs = q.openQuery();

        while(rs.next()) {
            int activationid = rs.getInt("taskid");
            activation = loadActivation(act, activationid);
            String sqlUpd = "update public.eactivation set status = ? where actid = ? and taskid = ?";
            M_Query qryUpd = db.prepQuery(sqlUpd);
            qryUpd.setParString(1, EActivity.StatusType.READY.toString());
            qryUpd.setParInt(2, (int) activation.activityID);
            qryUpd.setParInt(3, (int) activation.id);
            qryUpd.executeUpdate();
        }
    }

    /**
     * Load an activation of a given EActivity act with a given activationId.
     *
     * @param db
     * @param act
     * @param activationid
     * @return
     * @throws SQLException
     */
    static private EActivation loadActivation(EActivity act, int activationid) throws SQLException {
        EActivation activation = null;
        //get data from eactivation table:
        String sql = "select taskid, commandline, workspace, extractor, failure_tries, constrained, templatedir from public.eactivation where actid = ? and taskid = ?";
        M_Query q = db.prepQuery(sql);
        q.setParInt(1, act.id);
        q.setParInt(2, activationid);
        ResultSet rs = q.openQuery();
        if (rs.next()) {
            //sets the values
            activation = new EActivation();
            activation.id = rs.getInt("taskid");
            activation.activityID = act.id;
            activation.commandLine = rs.getString("commandline");
            activation.workspace = rs.getString("workspace");
            activation.extractor = rs.getString("extractor");
            activation.failureTries = rs.getInt("failure_tries");
            if (rs.getString("constrained").equals("T")) {
                activation.constrained = true;
            }
            activation.templateDir = rs.getString("templatedir");
            
            //gets file information:
            String fsql = "SELECT fileid, ftemplate, finstrumented, fdir, fname, fsize, foper, fieldname FROM public.efile WHERE actid = ? and taskid = ?";
            M_Query fq = db.prepQuery(fsql);
            fq.setParInt(1, act.id);
            fq.setParInt(2, activationid);
            ResultSet frs = fq.openQuery();
            while (frs.next()) {
                boolean inst = false;
                if (frs.getString("finstrumented").equals("T")) {
                    inst = true;
                }
                EFile newFile = new EFile(inst, frs.getString("fname"), EFile.Operation.valueOf(frs.getString("foper")));
                if (frs.getString("ftemplate").equals("T")) {
                    newFile.template = true;
                }
                newFile.fileID = frs.getInt("fileid");
                newFile.setFileDir(frs.getString("fdir"));
                String filename = frs.getString("fname");
                if (filename != null) {
                    newFile.setFileName(frs.getString("fname"));
                }
                newFile.fileOper = EFile.Operation.valueOf(frs.getString("foper"));
                newFile.fieldName = frs.getString("fieldname");
                newFile.fileSize = frs.getInt("fsize");
                activation.files.add(newFile);
            }
            //gets keyspace data
            String ksql = "SELECT * FROM public.ekeyspace WHERE actid = ? AND taskid = ?";
            M_Query kq = db.prepQuery(ksql);
            kq.setParInt(1, act.id);
            kq.setParInt(2, activationid);
            ResultSet krs = kq.openQuery();
            while (krs.next()) {
                if (krs.getString("relationtype").equals("INPUT")) {
                    String relationName = krs.getString("relationname");
                    Integer firstKey = new Integer(krs.getInt("iik"));

                    ResultSet relation = loadParameterSpace(relationName, firstKey);
                    List<String> fields = retrieveFields(relationName);

                    TreeMap<Integer, String[]> relMap = new TreeMap<Integer, String[]>();

                    while (relation.next()) {
                        Integer k = new Integer(relation.getInt("ik"));
                        String[] values = new String[fields.size()];
                        for (int i = 0; i < fields.size(); i++) {
                            String f = fields.get(i);
                            Object o = relation.getObject(f);
                            if (o.getClass().equals(Double.class)) {
                                int decimalPlaces = EProvenance.getDecimalPlaces(f, act.operation.getInputRelations().get(0));
                                String d = ChironUtils.formatFloat((Double) o, decimalPlaces);
                                values[i] = d;
                            } else {
                                values[i] = String.valueOf(relation.getObject(f));
                            }
                        }
                        relMap.put(k, values);
                    }
                    String[] fieldArray = new String[fields.size()];
                    for (int i = 0; i < fields.size(); i++) {
                        fieldArray[i] = fields.get(i);
                    }
                    ERelation inputRelation = new ERelation(relationName, fieldArray, relMap);
                    activation.inputRelation = inputRelation;
                    relation.close();
                } else {
                    String relationName = krs.getString("relationname");
                    ERelation outputRelation = new ERelation(relationName, retrieveFields(relationName));
                    activation.outputRelation = outputRelation;
                }
            }
            krs.close();
            frs.close();
            rs.close();
        }
        activation.activator = CActivation.newInstance(act);
        return activation;
    }

//    failure_handling
    public static boolean checkIfAllActivationsFinished(M_DB db, EActivity act) {
        try {
            String sqlUpd = "update public.eactivation set status = ? where actid = ? and failure_tries >= ?";
            M_Query qryUpd = db.prepQuery(sqlUpd);
            qryUpd.setParString(1, EActivity.StatusType.FINISHED_WITH_ERROR.toString());
            qryUpd.setParInt(2, (int) act.id);
            qryUpd.setParInt(3, act.workflow.maximumFailures);
            qryUpd.executeUpdate();
            
            int activations = 0;
            {
                String sql = "SELECT count(*) FROM public.eactivation WHERE actid = ?";
                M_Query q = db.prepQuery(sql);
                q.setParInt(1, (int) act.id);
                ResultSet rs = q.openQuery();
                if (rs.next()) {
                    activations = rs.getInt(1);
                    rs.close();
                }
            }
            
//            user interaction
            if(act.workflow.userInteraction){
                String sql = "SELECT count(*) FROM public.eactivation WHERE (status = ?) AND actid = ?";
                M_Query q = db.prepQuery(sql);
                q.setParString(1, EActivity.StatusType.FINISHED_WITH_ERROR.toString());
                q.setParInt(2, (int) act.id);
                ResultSet rs = q.openQuery();
                
                if (rs.next()) {
                    int counter = rs.getInt(1);
                    if(counter > 0){
                        System.out.println("Activity " + act.tag + " with ID=" + act.id + " presents some errors in your activations.\n" 
                                + "Please check the provenance database.");
                        ChironUtils.closeSystemWithError();
                    }
                    rs.close();
                }
            }
            
            {
                boolean result = false;
//                user interaction
                String sql = "SELECT count(*) FROM public.eactivation WHERE actid = ? AND (status = ?";
                if(act.workflow.userInteraction){
                    sql += ")";
                }else{
                    sql += " OR status = ?)";
                }
                
                M_Query q = db.prepQuery(sql);
                q.setParInt(1, (int) act.id);
                q.setParString(2, EActivity.StatusType.FINISHED.toString());
                
//                user interaction
                if(!act.workflow.userInteraction){
                    q.setParString(3, EActivity.StatusType.FINISHED_WITH_ERROR.toString());
                }
                
                ResultSet rs = q.openQuery();
                
                if (rs.next()) {
                    int counter = rs.getInt(1);
                    if (counter == activations) {
                        result = true;
                    }
                    rs.close();
                }
                return result;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * This method checks wether a EWorkflow with a given tag and tagexec exists
     * in the database.
     *
     * @param tag The workflow tag in the database/XML
     * @param tagexec The tagexec value in the database/XML
     * @return If the workflow exists, returns its wkfId, else returns -1
     * @throws SQLException
     */
    public static int matchEWorkflow(String tag, String tagexec) throws SQLException {
        String SQL = "SELECT ewkfid FROM public.eworkflow WHERE tag=? and tagexec=?";
        M_Query q = db.prepQuery(SQL);
        q.setParString(1, tag);
        q.setParString(2, tagexec);
        ResultSet rs = q.openQuery();
        int wkfId = -1;
        if (rs.next()) {
            wkfId = rs.getInt(1);
        }
        return wkfId;
    }

    /**
     * Retrieves the conceptual workflow from the database based on its tag.
     *
     * @param db
     * @param tag
     * @return
     * @throws SQLException
     */
    public static void matchCWorkflow(CWorkflow wf) throws SQLException {
        String SQL = "SELECT wkfid FROM public.cworkflow WHERE tag=?";
        M_Query q = db.prepQuery(SQL);
        q.setParString(1, wf.tag);
        ResultSet rs = q.openQuery();
        if (rs.next()) {
            int wkfId = rs.getInt(1);
            SQL = "SELECT actid, tag, atype, activation, extractor, templatedir FROM public.cactivity WHERE wkfid=? ORDER BY actid";
            q = db.prepQuery(SQL);
            q.setParInt(1, wkfId);
            ResultSet as = q.openQuery();
            while (as.next()) {
                Operator operator = Operator.valueOf(as.getString("atype").trim().toUpperCase());
                CActivity activity = CActivity.newInstance(operator);
                activity.id = as.getInt("actid");
                activity.tag = as.getString("tag");
                activity.activation = as.getString("activation");
                activity.extractor = as.getString("extractor");
                activity.templateDir = as.getString("templatedir");

                String query = "SELECT relid, rtype, rname, dependency FROM public.crelation WHERE actid=? ORDER BY relid";
                M_Query qq = db.prepQuery(query);
                qq.setParInt(1, activity.id);
                ResultSet relSet = qq.openQuery();
                while (relSet.next()) {
                    int relid = relSet.getInt("relid");
                    String rtype = relSet.getString("rtype");
                    String rname = relSet.getString("rname");
                    int dep = relSet.getInt("dependency");
                    CRelation relation = new CRelation(relid, rname, rtype, wf, dep);
                    relation.fields = retrieveFields(rname);
                    wf.relations.put(rname, relation);
                    if (relation.type == CRelation.Type.INPUT) {
                        activity.addInput(relation);
                    } else {
                        activity.addOutput(relation);
                    }
                }
                wf.activities.put(activity.id, activity);
            }

        } else {
            throw new SQLException("There is no Workflow in the database with the tag " + wf.tag + ".");
        }
    }

    public static void matchActivities(M_DB db, EWorkflow workflow) throws SQLException {
        //Load the workflow's activities:
        String SQL = "select actid, tag, status, starttime, endtime, (select count(*) from public.eactivation t where eactivity.actid = t.actid) as activations from public.eactivity where wkfId = ? ";
        M_Query q = db.prepQuery(SQL);
        q.setParInt(1, workflow.wkfId);
        ResultSet actRs = q.openQuery();

        while (actRs.next()) {
            int actID = actRs.getInt("actid");
            String tag = actRs.getString("tag");
            String status = actRs.getString("status");
            Date start = actRs.getDate("starttime");
            Date end = actRs.getDate("endtime");
            int numActivations = actRs.getInt("activations");
            EActivity act = workflow.getActivity(tag);
            if (act != null) {
                act.id = actID;
                act.status = EActivity.StatusType.valueOf(status);
                act.startTime = start;
                act.endTime = end;
                act.numActivations = numActivations;
            }
        }
        actRs.close();
    }

    /**
     * Insert the data from the input CSV into the database relation. Must also
     * sets the keyspace in the relation class so the activations can be
     * generated properly. Uses COPY
     *
     * @param relation
     */
    static void insertRelationData(CRelation relation, String expdir) throws SQLException, FileNotFoundException, IOException, InterruptedException {
        /**
         * TO DO: For now we are storing the KeySpace as a contiguous space,
         * getting the first and the last inserted IKs and understanding that
         * everything between that space belogs to this executing. For multi
         * tenancy approaches it will not work and needs to be changed.
         */
        CopyManager copyManager = new CopyManager((BaseConnection) db.getConn());
        
        ChironUtils.writeFileWithWorkflowID(expdir, relation.filename, workflowIDField, eworkflow.wkfId);
        
        FileReader fR = new FileReader(expdir + "Temp_" + relation.filename);
        String sql = "COPY \"" + workflow.tag + "\"." + relation.name + "(";
        relation.fields = EProvenance.retrieveFields(relation.name);
        sql += workflowIDField;
        for (String field : relation.fields) {
            sql += "," + field;
        }
        sql += ") FROM STDIN WITH CSV DELIMITER AS ';' HEADER";
        copyManager.copyIn(sql, fR);
        
        ChironUtils.deleteFile("Temp_" + relation.filename, expdir);
    }

    /**
     * For a given Relation, retrieves its fields from the database.
     *
     * @param relationName
     * @return A List containing the fields of the relation.
     * @throws SQLException
     */
    public static List<String> retrieveFields(String relationName) throws SQLException {
        String q = "SELECT column_name FROM information_schema.columns WHERE table_name = ? ORDER BY ordinal_position";

        M_Query query = db.prepQuery(q);
        query.setParString(1, relationName);
        ResultSet r = query.openQuery();
        List result = new ArrayList<String>();
        while (r.next()) {
            String column = r.getString("column_name");
            if (!column.equals("ik") && !column.equals("ok")&& !column.equals("ewkfid")) {
                result.add(column.toUpperCase());
            }

        }
        return result;
    }
    
    public static String getWhereClause(int wkfID) {
        String where = "WHERE ewkfid = " + wkfID;
        return where;
    }
    
    public static String getWhereClause(int wkfID, int ik) {
        String where = "WHERE ewkfid = " + wkfID + " and ik = " + ik;
        return where;
    }

    private static ResultSet loadParameterSpace(String relationName) throws SQLException {
        String sql = "SELECT * FROM \"" + workflow.tag + "\"." + relationName + " " + getWhereClause(eworkflow.wkfId);
        M_Query query = db.prepQuery(sql);
        ResultSet rs = query.openQuery();
        return rs;
    }
    
    public static ResultSet loadParameterSpace(String relationName, int ik) throws SQLException {
        String sql = "SELECT * FROM \"" + workflow.tag + "\"." +  relationName + " " + getWhereClause(eworkflow.wkfId, ik);
        M_Query query = db.prepQuery(sql);
        ResultSet rs = query.openQuery();
        return rs;
    }

    public static ResultSet loadOrderedParameterSpace(String relationName, ArrayList<String> fields) throws SQLException {
        String sql = "SELECT * FROM \"" + workflow.tag + "\"." +  relationName + " " + getWhereClause(eworkflow.wkfId) + getOrderBy(fields);
        M_Query query = db.prepQuery(sql);
        ResultSet rs = query.openQuery();
        return rs;
    }
    
    public static String getOrderBy(ArrayList<String> fields){
        String orderBy = "";
        boolean first = true;
        for(String field : fields){
            if(!first){
                orderBy += ", ";
            }else{
                orderBy += " ORDER BY ";
                first = false;
            }
            
            orderBy += field;
        }
        
        return orderBy;
    }
    
    public static ResultSet loadOrderedParameterSpace(String relationName, int ik, ArrayList<String> fields) throws SQLException {
        String sql = "SELECT * FROM \"" + workflow.tag + "\"." + relationName + " " + getWhereClause(eworkflow.wkfId, ik) + getOrderBy(fields);
        M_Query query = db.prepQuery(sql);
        ResultSet rs = query.openQuery();
        return rs;
    }

    public static int getDecimalPlaces(String fieldName, CRelation inputRelation) throws SQLException {
        int places = 0;
        String sql = "SELECT decimalplaces FROM public.cfield WHERE relid=? AND fname=?";
        M_Query q = db.prepQuery(sql);
        q.setParInt(1, inputRelation.id);
        q.setParString(2, fieldName.toLowerCase());
        ResultSet rs = q.openQuery();
        if (rs.next()) {
            places = rs.getInt("decimalplaces");
        }
        return places;
    }

    public static List<EFile> getFileFields(CRelation inputRelation) throws SQLException {
        String sql = "SELECT fname, fileoperation, instrumented FROM public.cfield WHERE relid=? AND ftype='file'";

        M_Query q = db.prepQuery(sql);
        q.setParInt(1, inputRelation.id);
        ResultSet rs = q.openQuery();
        List<EFile> fileFields = new ArrayList<EFile>();
        while (rs.next()) {
            String fname = rs.getString("fname");
            String op = rs.getString("fileoperation");
            String instrumented = rs.getString("instrumented");
            boolean ins = false;
            if (instrumented != null && instrumented.equalsIgnoreCase("true")) {
                ins = true;
            }
            if (op == null) {
                op = "MOVE";
            }
            EFile file = new EFile(ins, fname.toUpperCase(), EFile.Operation.valueOf(op));
            fileFields.add(file);
        }
        return fileFields;
    }

    public static void storeOutputRelation(ERelation outputRel, int inputKey) throws SQLException, IOException {
        CopyManager copyManager = new CopyManager((BaseConnection) db.getConn());
        String sql = "COPY \"" + workflow.tag + "\"." + outputRel.name + "(" + workflowIDField + ", ik";
        for (int i = 0; i < outputRel.fields.length; i++) {
            sql += ", " + outputRel.fields[i];
        }
        sql += ") FROM STDIN WITH CSV DELIMITER AS ';'";
        InputStream is = new ByteArrayInputStream(outputRel.getCSV(inputKey,eworkflow.wkfId).getBytes());
        copyManager.copyIn(sql, is);
    }

    public static void storeInputRelation(ERelation relation, EActivation activation) throws SQLException {
        int ik = relation.getFirstKey();
        String select = "SELECT ik FROM \"" + workflow.tag + "\"." + relation.name + " " + getWhereClause(eworkflow.wkfId, ik);
        M_Query selection = db.prepQuery(select);
        int ewkfid = eworkflow.wkfId;

        ResultSet rs = selection.openQuery();
        if (!rs.next()) {
            
            String sql = "INSERT INTO \"" + workflow.tag + "\"." + relation.name + " (ewkfid, ik";
            for (int i = 0; i < relation.fields.length; i++) {
                sql += ", " + relation.fields[i];
            }
            sql += ") VALUES ";
            //Integer newKey = activation.pipelinedFrom.outputRelation.getFirstKey();
            //activation.inputRelation.resetKey(activation.outputRelation.getFirstKey(), newKey);
            for (Integer key : relation.values.keySet()) {
                sql += "(" + ewkfid + ", " + key;
                String[] tuple = relation.getTupleArray(key);

                for (String value : tuple) {
                    try {
                        Double.parseDouble(value);
                        sql += ", " + value;
                    } catch (NumberFormatException ex) {
                        sql += ", '" + value + "'";
                    }
                }
                sql += ")";
            }

            M_Query q = db.prepQuery(sql);
            q.executeUpdate();
        }
    }

    public static ResultSet loadParameterSpace(CRelation inputRelation) throws SQLException {
        return loadParameterSpace(inputRelation.name);
    }

    public static ResultSet loadOrderedParameterSpace(CRelation inputRelation, ArrayList<String> fields) throws SQLException {
        return loadOrderedParameterSpace(inputRelation.name, fields);
    }

    /**
     * Feeds the input relation of an activity using the output data from
     * previous activity. Thus this method selects the fields of the dependency
     * output and insert it into the input of the activity that now gonna be
     * able to execute.
     *
     * @param dependencyOutput
     * @param input
     * @throws SQLException
     */
    public static void propagateData(CRelation dependencyOutput, CRelation input) throws SQLException {
        String sql = "INSERT INTO \"" + workflow.tag + "\"." + input.name + " (ewkfid, ik, " + input.getFieldNames() + ")"
                + " SELECT ewkfid, ok AS ik, " + input.getFieldNames() + " FROM \"" + workflow.tag + "\"." + dependencyOutput.name + " " + getWhereClause(eworkflow.wkfId);
        M_Query q = db.prepQuery(sql);
        q.executeUpdate();
    }

    public static void executeSQLActivation(String SQL, List<CRelation> input, CRelation output) throws SQLException {
        if (checkForSqlInjection(SQL)) {
            throw new SQLException("The clause \"" + SQL + "\" ss suspicious of SQL injection. Activation aborted.");
        } else {
//            for (CRelation inputRelation : input) {
//                SQL = SQL.replace(inputRelation.name, inputRelation.name);
//            }
            int ewkfid = eworkflow.wkfId;
            String newQuery = SQL.replace("%=EWKFID%", String.valueOf(ewkfid));
            newQuery = newQuery.replace("%=KEY%", "IK");
            
            String sortFields = "";
            String[] splits = newQuery.toUpperCase().split("SELECT");
            if(splits.length == 2){
                splits = splits[1].split("FROM");
                if(splits.length == 2){
                    splits = splits[0].split(",");
            
                    String prefix = "";
                    for(String field : splits){
                        if(!sortFields.isEmpty()){
                            prefix = ", ";
                        }
                        
                        String strField = field.trim();
                        String[] tsplit = strField.split("\\.");
                        if(tsplit.length == 2){
                            strField = tsplit[1].trim();
                        }
                        
                        String[] fsplit = strField.split(" AS ");
                        if(fsplit.length == 2){
                            strField = fsplit[1].trim();
                        }
                        
                        sortFields += prefix + strField;
                    }
                }
            }
            
            if(sortFields.isEmpty()){
                sortFields = "ewkfid, ik, " + output.getFieldNames();
            }

            String insertQuery = "INSERT INTO \"" + workflow.tag + "\"." + output.name + " (" + sortFields + ") " + newQuery;
            M_Query q = db.prepQuery(insertQuery);
            q.executeUpdate();
        }
    }

    /**
     * Checks the sql string to avoid SQL Injections TO DO: Need to be improved
     * for smarter scans to detect injections.
     *
     * @param sql
     * @return
     */
    private static boolean checkForSqlInjection(String sql) {
        if (sql.contains("INSERT") || sql.contains("insert") || sql.contains("drop") || sql.contains("DROP") || sql.contains("ALTER") || sql.contains("alter")) {
            return true;
        } else {
            return false;
        }
    }

    public static ArrayList<String> getTextOperand(int id) throws SQLException {
        ArrayList<String> operands = new ArrayList<String>();
        
        String sql = "SELECT textvalue FROM public.coperand WHERE actid=?";
        M_Query q = db.prepQuery(sql);
        q.setParInt(1, id);
        ResultSet rs = q.openQuery();
        while (rs.next()) {
            operands.add(rs.getString("textvalue"));
        }
        
        return operands;
    }

    public static Double getNumericOperand(int id) throws SQLException {
        String sql = "SELECT numericvalue FROM public.coperand WHERE actid=?";
        M_Query q = db.prepQuery(sql);
        q.setParInt(1, id);
        ResultSet rs = q.openQuery();
        if (rs.next()) {
            return rs.getDouble("numericvalue");
        } else {
            throw new SQLException("There is no numeric-type operand defined for activity with id=" + String.valueOf(id));
        }
    }
    
    //    failure_handling
    public static void evaluateRunningActivations(EBody body) {
        try {
            String sql = "select ea.tag, round(avg(cast (extract(epoch from (a.endtime - a.starttime)) AS numeric)), 3) as AverageTime, "
                    + "round(stddev(cast (extract(epoch from (a.endtime - a.starttime)) AS numeric)), 3) as StandardDeviation " 
                    + "from public.eworkflow ew, public.eactivity ea, public.eactivation a " 
                    + "where ea.actid = a.actid " 
                    + "and a.status = ? " 
                    + "and ew.ewkfid = ea.wkfid " 
                    + "and ew.tag = ? " 
                    + "group by ea.tag";
            
            M_Query q = db.prepQuery(sql);
            q.setParString(1, EActivity.StatusType.FINISHED.toString());
            q.setParString(2, eworkflow.tag);
            ResultSet rs = q.openQuery();
            while (rs.next()) {
                String actTag = rs.getString(1);
                float average = rs.getFloat(2);
                float standardDeviation = rs.getFloat(3);
                float walltime = (float) (average + (2.33 * standardDeviation));
                
                EvaluateRunningActivationsByAvgAndStdDev(body, actTag, walltime);
            }
            
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void EvaluateRunningActivationsByAvgAndStdDev(EBody body, String actTag, float threshold) {
        try {
            Date currentTime = new Date();
            String sql = "SELECT a.taskid, a.failure_tries, round(cast (extract(epoch from ('"
                    + currentTime.toString()
                    + "' - a.starttime)) AS numeric), 3) "
                    + "FROM public.eworkflow ew, public.eactivity ea, public.eactivation a "
                    + "where ea.actid = a.actid " 
                    + "and a.status = ? " 
                    + "and ew.ewkfid = ea.wkfid " 
                    + "and ew.ewkfid = ? "
                    + "and ea.tag = ? "
                    + "and a.failure_tries < ?" 
                    + "order by a.taskid";
            
            M_Query q = db.prepQuery(sql);
            q.setParString(1, EActivity.StatusType.RUNNING.toString());
            q.setParInt(2, body.eWorkflow.wkfId);
            q.setParString(3, actTag);
            q.setParInt(4, body.eWorkflow.maximumFailures);
            ResultSet rs = q.openQuery();
            while (rs.next()) {
                int taskID = rs.getInt(1);
                int failures = rs.getInt(2);
                float elapsedTime = rs.getFloat(3);   
                if(elapsedTime > threshold){
                    String sqlUpd = "update public.eactivation set status = ?, failure_tries = ? where actid = ? and taskid = ?";
                    M_Query qryUpd = db.prepQuery(sqlUpd);
                    
                    String status = EActivity.StatusType.FINISHED_WITH_ERROR.toString();
                    failures++;
                    if(failures < body.eWorkflow.maximumFailures){
                        status = EActivity.StatusType.READY.toString();
                    }
                    
                    qryUpd.setParString(1, status);
                    qryUpd.setParInt(2, failures);
                    qryUpd.setParInt(3, body.eWorkflow.getActivity(actTag).id);
                    qryUpd.setParInt(4, taskID);
                    qryUpd.executeUpdate();
                }
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }
}
