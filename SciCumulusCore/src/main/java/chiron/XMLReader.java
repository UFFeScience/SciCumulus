package chiron;

import chiron.cloud.VirtualMachineType;
import chiron.concept.CWorkflow;
import chiron.environment.EnvironmentType;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import nu.xom.Builder;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import vs.database.M_DB;

/**
 * Reads the execution XML for input configuration
 *
 * @author Eduardo, Vítor, Jonas
 * @since 2011-01-13
 */
public class XMLReader {

    public String configurationFileName;
    private CWorkflow conceptualWorkflow;
    private EWorkflow executionWorkflow;
    
    //    credentials
    public String awsAccessKeyId = new String();
    public String awsSecretAccessKey = new String();
    
//    environment
    public boolean verbose = false;
    public EnvironmentType environmentType = null;
    public String clusterName = new String();
    
//    binary
    public String binaryDirectory = new String();
    public String SCSetup = new String();
    public String SCCore = new String();
    
//    machine
    public String image = new String();
    public String macUser = new String();
    public String macPassword = new String();
    
//    vm
    public ArrayList<VirtualMachineType> vmTypes = new ArrayList<VirtualMachineType>();
    
//    constraint
    public String wkfExecTag = new String();
    public double maxTime;
    public double maxFinancialCost;
    public int maxVMAmount;
    public double totalRAM;
    public double totalDisk;
    public double alfa1, alfa2, alfa3;
    public int cores;
    
//    workspace
    public boolean upload = true;
    public String bucketName = new String();
    public String workflowDir = new String();
    public String compressedWorkspace = new String();
    public String compressedDir = new String();
    
//    database
    public String dbName = new String();
    public String dbPort = new String();
    public String dbServer = new String();
    public String dbUser = new String();
    public String dbPassword = new String();    
        
//    query
    public String SQL;
    
//    execution workflow
    private int maximumFailures;
    private boolean userInteraction;
    private boolean redundancy;
    private Double minReliability;

    public XMLReader(String configurationFile) {
        this.configurationFileName = configurationFile;
    }

    /**
     * Método que realiza a leitura do arquivo de configuração no formato XML
     *
     * @param chiron
     * @return
     * @throws ParsingException
     * @throws ValidityException
     * @throws IOException
     * @throws SQLException
     */
    public void readXMLConfiguration() throws ParsingException, ValidityException, IOException, SQLException, ParserConfigurationException, SAXException {
        File file = new File(configurationFileName);
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document xml = db.parse(file);
        xml.getDocumentElement().normalize();
        
        if (xml.getDocumentElement().getNodeName().matches("SciCumulus")) {
            updateCredentials(xml);            
            updateEnvironment(xml);            
            updateBinary(xml);
            updateMachineTypes(xml);
            updateConstraints(xml);
            updateWorkspace(xml);
            updateDatabase(xml);
            updateQuery(xml);
            updateExecutionWorkflow(xml);
        }
    }

    public CWorkflow getConceptualWorkflow() {
        return conceptualWorkflow;
    }

    public EWorkflow getExecutionWorkflow() {
        return executionWorkflow;
    }
    
    private void updateCredentials(org.w3c.dom.Document doc) {
        //  Informacoes de seguranca
        NodeList nodeLst = doc.getElementsByTagName("credentials");
        org.w3c.dom.Element element = (org.w3c.dom.Element) nodeLst.item(0);
        if(element!=null){
            awsAccessKeyId = element.getAttribute("access_key");
            awsSecretAccessKey = element.getAttribute("secret_access_key");
        }
    }

    private void updateEnvironment(org.w3c.dom.Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("environment");
        org.w3c.dom.Element element = (org.w3c.dom.Element) nodeLst.item(0);
        if(element!=null){
            if(element.getAttribute("verbose").toLowerCase().equals("true")){
                verbose = true;
            }
            environmentType = EnvironmentType.valueOf(element.getAttribute("type").toUpperCase());
            clusterName = element.getAttribute("cluster_name");
        }
    }

    private void updateBinary(org.w3c.dom.Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("binary");
        org.w3c.dom.Element element = (org.w3c.dom.Element) nodeLst.item(0);
        if(element!=null){
            binaryDirectory = element.getAttribute("directory");
            SCSetup = element.getAttribute("conceptual_version");
            SCCore = element.getAttribute("execution_version");
        }
    }

    private void updateMachineTypes(org.w3c.dom.Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("machineTypes");
        org.w3c.dom.Element element = (org.w3c.dom.Element) nodeLst.item(0);
        if(element!=null){
            image = element.getAttribute("image");
            macUser = element.getAttribute("user");
            macPassword = element.getAttribute("password");

            nodeLst = doc.getElementsByTagName("vm");
            for (int i = 0; i < nodeLst.getLength(); i++) {
                element = (org.w3c.dom.Element) nodeLst.item(i);
                if(element!=null){
    //            disk_space --> disk space in MB
                    VirtualMachineType vmType = new VirtualMachineType(Double.valueOf(element.getAttribute("financial_cost")), 
                            Double.valueOf(element.getAttribute("disk_space")), 
                            Integer.valueOf(element.getAttribute("ram")), 
                            Double.valueOf(element.getAttribute("gflops")), 
                            element.getAttribute("platform"), 
                            Integer.valueOf(element.getAttribute("cores")));
                    vmType.setType(element.getAttribute("type"));
                    vmTypes.add(vmType);
                }
            }
        }
    }

    private void updateWorkspace(org.w3c.dom.Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("workspace");
        org.w3c.dom.Element element = (org.w3c.dom.Element) nodeLst.item(0);
        if(element!=null){
            if(element.getAttribute("upload").toLowerCase().equals("false")){
                upload = false;
            }
            bucketName = element.getAttribute("bucket_name");
            workflowDir = element.getAttribute("workflow_dir");
            compressedWorkspace = element.getAttribute("compressed_workspace");
            compressedDir = element.getAttribute("compressed_dir");
        }
    }

    private void updateConstraints(org.w3c.dom.Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("constraint");
        org.w3c.dom.Element element = (org.w3c.dom.Element) nodeLst.item(0);
        if(element!=null){
            wkfExecTag = element.getAttribute("workflow_exectag").toLowerCase();
            String value = element.getAttribute("max_time");
            if(!value.isEmpty()){
                maxTime = Double.valueOf(value);
            }
            
            value = element.getAttribute("max_financial_cost");
            if(!value.isEmpty()){
                maxFinancialCost = Double.valueOf(value);
            }
            
            value = element.getAttribute("max_vm_amount");
            if(!value.isEmpty()){
                maxVMAmount = Integer.valueOf(value);
            }
            
            value = element.getAttribute("total_ram");
            if(!value.isEmpty()){
                totalRAM = Double.valueOf(value);
            }
            
            value = element.getAttribute("total_disk");
            if(!value.isEmpty()){
                totalDisk = Double.valueOf(value);
            }
            
            value = element.getAttribute("alfa1");
            if(!value.isEmpty()){
                alfa1 = Double.valueOf(value);
            }
            
            value = element.getAttribute("alfa2");
            if(!value.isEmpty()){
                alfa2 = Double.valueOf(value);
            }
            
            value = element.getAttribute("alfa3");
            if(!value.isEmpty()){
                alfa3 = Double.valueOf(value);
            }
            
            String nCores = element.getAttribute("cores");
            if(!nCores.isEmpty()){
                cores = Integer.parseInt(nCores);
            }else{
                cores = Runtime.getRuntime().availableProcessors();
            }
        }
    }
    
    private void updateDatabase(Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("database");
        Element element = (Element) nodeLst.item(0);
        if(element!=null){
            dbName = element.getAttribute("name");
            dbPort = element.getAttribute("port");
            dbServer = element.getAttribute("server");
            dbUser = element.getAttribute("username");
            dbPassword = element.getAttribute("password");
            
            //Database connection configuration
            String connection = "jdbc:postgresql://" + dbServer + ":" + dbPort + "/" + dbName + "?chartset=UTF8";
            EProvenance.db = new M_DB(M_DB.DRIVER_POSTGRESQL, connection, dbUser, dbPassword, true);
            EProvenanceQueue.queue = new EProvenanceQueue();
            EProvenanceQueue.queue.db = new M_DB(M_DB.DRIVER_POSTGRESQL, connection, dbUser, dbPassword, true);
            EProvenanceQueue.queue.start();
        }
    }

    private void updateQuery(Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("query");
        Element element = (Element) nodeLst.item(0);
        if(element!=null){
            SQL = element.getAttribute("sql");
        }
    }
    
    private void updateExecutionWorkflow(Document doc) throws SQLException, ValidityException {
        //Obtain CWorkflow information from XML
        Element elementWorkflow = (Element) doc.getElementsByTagName("executionWorkflow").item(0);
        Element elementWorkspace = (Element) doc.getElementsByTagName("workspace").item(0);
        
        CWorkflow wf = new CWorkflow();
        wf.tag = elementWorkflow.getAttribute("tag").toLowerCase();
        //retrive remaining meta-data from database
        EProvenance.matchCWorkflow(wf);
        //so here we have the conceptual workflow in the variable wf with all the activities and relations stuff

        String wfDir = elementWorkspace.getAttribute("workflow_dir");
        String expDir = ChironUtils.checkDir(elementWorkflow.getAttribute("expdir").replace(ChironUtils.workflowTag, wfDir));
        
        //        failure_handling
        String maxFailures = elementWorkflow.getAttribute("max_failure");
        maximumFailures = 1;
        if(maxFailures!=null){
            maximumFailures = Integer.parseInt(maxFailures);
        }
        
        String userInter = elementWorkflow.getAttribute("user_interaction");
        userInteraction = false;
        if(userInter != null && userInter.toLowerCase().equals("true")){
            userInteraction = true;
        }
        
        String red = elementWorkflow.getAttribute("redundancy");
        redundancy = false;
        if(red != null && red.toLowerCase().equals("true")){
            redundancy = true;
        }
        
        String reliability = elementWorkflow.getAttribute("reliability");
        minReliability = 1.0;
        if(reliability!=null){
            minReliability = Double.valueOf(reliability);
        }
        
        //derive the conceptual workflow to an executable workflow
        EWorkflow eWf = wf.derive(wfDir, expDir, wkfExecTag, maximumFailures, userInteraction, minReliability, redundancy);
        EProvenance.eworkflow = eWf;
        
        String execmodel = elementWorkflow.getAttribute("execmodel");
        String verbose = elementWorkflow.getAttribute("verbose");

        if (execmodel != null) {
            eWf.model = EWorkflow.ExecModel.valueOf(execmodel);
        }
        if (verbose != null) {
            ChironUtils.verbose = Boolean.parseBoolean(verbose);
        }
        
        //insert the input relations data into database relations
        NodeList relations = elementWorkflow.getElementsByTagName("relation");
        for(int r=0; r<relations.getLength(); r++){
            Element relation = (Element) relations.item(r);
            String relationName = relation.getAttribute("name").toLowerCase();
            if (eWf.checkInputRelation(relationName)) {
                String filename = relation.getAttribute("filename");
                eWf.getInputRelation(relationName).filename = filename;
            } else {
                throw new ValidityException("The input relation named " + relationName + " is not defined in the concept of workflow " + wf.tag + ". Please check your workflow. Possible input relations are: " + wf.relations.toString());
            }
        }
        
        this.conceptualWorkflow = wf;
        this.executionWorkflow = eWf;
        EProvenance.workflow = wf;
    }

    public String getMacUser() {
        return macUser;
    }

    public void setMacUser(String macUser) {
        this.macUser = macUser;
    }

    public String getMacPassword() {
        return macPassword;
    }

    public void setMacPassword(String macPassword) {
        this.macPassword = macPassword;
    }

    public double getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(double maxTime) {
        this.maxTime = maxTime;
    }

    public double getMaxFinancialCost() {
        return maxFinancialCost;
    }

    public void setMaxFinancialCost(double maxFinancialCost) {
        this.maxFinancialCost = maxFinancialCost;
    }

    public int getMaxVMAmount() {
        return maxVMAmount;
    }

    public void setMaxVMAmount(int maxVMAmount) {
        this.maxVMAmount = maxVMAmount;
    }

    public double getTotalRAM() {
        return totalRAM;
    }

    public void setTotalRAM(double totalRAM) {
        this.totalRAM = totalRAM;
    }

    public double getTotaldisk() {
        return totalDisk;
    }

    public void setTotaldisk(double totaldisk) {
        this.totalDisk = totaldisk;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbPort() {
        return dbPort;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbServer() {
        return dbServer;
    }

    public void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public double getAlfa1() {
        return alfa1;
    }

    public void setAlfa1(double alfa1) {
        this.alfa1 = alfa1;
    }

    public double getAlfa2() {
        return alfa2;
    }

    public void setAlfa2(double alfa2) {
        this.alfa2 = alfa2;
    }

    public double getAlfa3() {
        return alfa3;
    }

    public void setAlfa3(double alfa3) {
        this.alfa3 = alfa3;
    }

    public String getConfigurationFileName() {
        return configurationFileName;
    }

    public void setConfigurationFileName(String configurationFileName) {
        this.configurationFileName = configurationFileName;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public EnvironmentType getEnvironmentType() {
        return environmentType;
    }

    public void setEnvironmentType(EnvironmentType environmentType) {
        this.environmentType = environmentType;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getBinaryDirectory() {
        return binaryDirectory;
    }

    public void setBinaryDirectory(String binaryDirectory) {
        this.binaryDirectory = binaryDirectory;
    }

    public String getSCSetup() {
        return SCSetup;
    }

    public void setSCSetup(String SCSetup) {
        this.SCSetup = SCSetup;
    }

    public String getSCCore() {
        return SCCore;
    }

    public void setSCCore(String SCCore) {
        this.SCCore = SCCore;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<VirtualMachineType> getVmTypes() {
        return vmTypes;
    }

    public void setVmTypes(ArrayList<VirtualMachineType> vmTypes) {
        this.vmTypes = vmTypes;
    }

    public String getWkfExecTag() {
        return wkfExecTag;
    }

    public void setWkfExecTag(String wkfExecTag) {
        this.wkfExecTag = wkfExecTag;
    }

    public double getTotalDisk() {
        return totalDisk;
    }

    public void setTotalDisk(double totalDisk) {
        this.totalDisk = totalDisk;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getWorkflowDir() {
        return workflowDir;
    }

    public void setWorkflowDir(String workflowDir) {
        this.workflowDir = workflowDir;
    }

    public String getCompressedWorkspace() {
        return compressedWorkspace;
    }

    public void setCompressedWorkspace(String compressedWorkspace) {
        this.compressedWorkspace = compressedWorkspace;
    }

    public String getCompressedDir() {
        return compressedDir;
    }

    public void setCompressedDir(String compressedDir) {
        this.compressedDir = compressedDir;
    }

    public String getSQL() {
        return SQL;
    }

    public void setSQL(String SQL) {
        this.SQL = SQL;
    }

    public Double getReliability() {
        return minReliability;
    }
    
    
}
