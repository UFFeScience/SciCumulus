package main;

/**
 *
 * @author Vítor
 */
import adaptability.VirtualMachineType;
import environment.EnvironmentType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import workflow.Activity;

public class XMLReader {
    
    public String filePath;
    
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
    public List<VirtualMachineType> vmTypes = new ArrayList<VirtualMachineType>();
    
//    constraint
    public String wkfExecTag = new String();
    public double maxTime;
    public double maxFinancialCost;
    public int maxVMAmount;
    public double totalRAM;
    public double totaldisk;
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
    public String dbPath = new String();
    
//    query
    public String SQL;
    
//    execution workflow
    public String wkfExpDir = new String();
    public String wkfTag = new String();
    public List<Activity> wkfActs = new ArrayList<Activity>();
    public String absolutePath;

    public XMLReader(String filePath) throws ParserConfigurationException, SAXException, IOException {
        this.absolutePath = System.getProperty("user.dir");
        this.filePath = filePath;
        
        File file = new File(filePath);
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
            updateConceptualWorkflow(xml);
            updateExecutionWorkflow(xml);
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
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

    public List<VirtualMachineType> getVmTypes() {
        return vmTypes;
    }

    public double getAlfa1() {
        return alfa1;
    }

    public double getAlfa2() {
        return alfa2;
    }

    public double getAlfa3() {
        return alfa3;
    }

    public void setAlfa1(double alfa1) {
        this.alfa1 = alfa1;
    }

    public void setAlfa2(double alfa2) {
        this.alfa2 = alfa2;
    }

    public void setAlfa3(double alfa3) {
        this.alfa3 = alfa3;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setWorkflowDir(String workflowDir) {
        this.workflowDir = workflowDir;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCompressedWorkspace(String compressedWorkspace) {
        this.compressedWorkspace = compressedWorkspace;
    }

    public void setUser(String user) {
        this.macUser = user;
    }

    public void setPassword(String password) {
        this.macPassword = password;
    }

    public void setCompressedDir(String compressedDir) {
        this.compressedDir = compressedDir;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getWorkflowDir() {
        return workflowDir;
    }

    public String getImage() {
        return image;
    }

    public String getCompressedWorkspace() {
        return compressedWorkspace;
    }

    public double getMaxTime() {
        return maxTime;
    }

    public double getMaxFinancialCost() {
        return maxFinancialCost;
    }

    public int getMaxVMAmount() {
        return maxVMAmount;
    }

    public double getTotalRAM() {
        return totalRAM;
    }

    public double getTotalDisk() {
        return totaldisk;
    }

    public void setMaxTime(double maxTime) {
        this.maxTime = maxTime;
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

    public String getSQL() {
        return SQL;
    }

    public void setSQL(String SQL) {
        this.SQL = SQL;
    }

    public String getWkfExpDir() {
        return wkfExpDir;
    }

    public void setWkfExpDir(String wkfExpDir) {
        this.wkfExpDir = wkfExpDir;
    }

    public String getWkfTag() {
        return wkfTag;
    }

    public void setWkfTag(String wkfTag) {
        this.wkfTag = wkfTag;
    }

    public String getWkfExecTag() {
        return wkfExecTag;
    }

    public void setWkfExecTag(String wkfExecTag) {
        this.wkfExecTag = wkfExecTag;
    }

    public List<Activity> getWkfActs() {
        return wkfActs;
    }

    public void setWkfActs(List<Activity> wkfActs) {
        this.wkfActs = wkfActs;
    }

    public void setMaxFinancialCost(double maxFinancialCost) {
        this.maxFinancialCost = maxFinancialCost;
    }

    public void setMaxVMAmount(int maxVMAmount) {
        this.maxVMAmount = maxVMAmount;
    }

    public void setTotalRAM(double totalRAM) {
        this.totalRAM = totalRAM;
    }

    public void setTotaldisk(double totaldisk) {
        this.totaldisk = totaldisk;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getCompressedDir() {
        return compressedDir;
    }

    XMLReader() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void updateCredentials(Document doc) {
        //  Informacoes de seguranca
        NodeList nodeLst = doc.getElementsByTagName("credentials");
        Element element = (Element) nodeLst.item(0);
        if(element!=null){
            awsAccessKeyId = element.getAttribute("access_key");
            awsSecretAccessKey = element.getAttribute("secret_access_key");
        }
    }

    private void updateEnvironment(Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("environment");
        Element element = (Element) nodeLst.item(0);
        if(element!=null){
            if(element.getAttribute("verbose").toLowerCase().equals("true")){
                verbose = true;
            }
            
            String envType = element.getAttribute("type");
            if(!envType.isEmpty()){
                environmentType = EnvironmentType.valueOf(envType.toUpperCase());
            }
            
            clusterName = element.getAttribute("cluster_name");
        }
    }

    private void updateBinary(Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("binary");
        Element element = (Element) nodeLst.item(0);
        if(element!=null){
            binaryDirectory = element.getAttribute("directory");
            SCSetup = element.getAttribute("conceptual_version");
            SCCore = element.getAttribute("execution_version");
        }
    }

    private void updateMachineTypes(Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("machineTypes");
        Element element = (Element) nodeLst.item(0);
        if(element!=null){
            image = element.getAttribute("image");
            macUser = element.getAttribute("user");
            macPassword = element.getAttribute("password");

            nodeLst = doc.getElementsByTagName("vm");
            for (int i = 0; i < nodeLst.getLength(); i++) {
                element = (Element) nodeLst.item(i);
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

    private void updateWorkspace(Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("workspace");
        Element element = (Element) nodeLst.item(0);
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

    private void updateConstraints(Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("constraint");
        Element element = (Element) nodeLst.item(0);
        if(element!=null){
            wkfExecTag = element.getAttribute("workflow_exectag");
            maxTime = Double.valueOf(element.getAttribute("max_time"));
            maxFinancialCost = Double.valueOf(element.getAttribute("max_financial_cost"));
            maxVMAmount = Integer.valueOf(element.getAttribute("max_vm_amount"));
            totalRAM = Double.valueOf(element.getAttribute("total_ram"));
            totaldisk = Double.valueOf(element.getAttribute("total_disk"));
            alfa1 = Double.valueOf(element.getAttribute("alfa1"));
            alfa2 = Double.valueOf(element.getAttribute("alfa2"));
            alfa3 = Double.valueOf(element.getAttribute("alfa3"));
            cores = Integer.parseInt(element.getAttribute("cores"));
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
            dbPath = element.getAttribute("path");
        }
    }

    private void updateQuery(Document doc) {
        NodeList nodeLst = doc.getElementsByTagName("query");
        Element element = (Element) nodeLst.item(0);
        if(element!=null){
            SQL = element.getAttribute("sql");
        }
    }

    private void updateConceptualWorkflow(Document doc) {
        Element wkfElement = (Element) doc.getElementsByTagName("conceptualWorkflow").item(0);
        
        if (wkfElement!=null) {
            wkfExpDir = wkfElement.getAttribute("expdir");
            wkfTag = wkfElement.getAttribute("tag");

            NodeList nodeList = wkfElement.getElementsByTagName("activity");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                Activity act = new Activity(element.getAttribute("tag"));
                act.setOrder(i);
                wkfActs.add(act);
            }
        }
    }
    
    private void updateExecutionWorkflow(Document doc) {
        Element wkfElement = (Element) doc.getElementsByTagName("executionWorkflow").item(0);
        
        if (wkfElement!=null) {
            wkfTag = wkfElement.getAttribute("tag");
        }
    }
    
    public List<Activity> getActs() {
        return wkfActs;
    }

    public String getWorkflowTag() {
        return wkfTag;
    }

    public EnvironmentType getEnvironmentType() {
        return environmentType;
    }

    public void setEnvironmentType(EnvironmentType environmentType) {
        this.environmentType = environmentType;
    }

    public String getBinaryDirectory() {
        return binaryDirectory;
    }

    public void setBinaryDirectory(String binaryDirectory) {
        this.binaryDirectory = binaryDirectory;
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
}
