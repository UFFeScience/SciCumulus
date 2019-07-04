package chiron.environment;

import chiron.ChironInvocation;
import chiron.EMachine;
import chiron.XMLReader;
import chiron.cloud.AmazonProvider;
import chiron.cloud.VirtualMachineType;
import com.amazonaws.services.ec2.AmazonEC2Client;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author vitor
 */
public class EnvironmentConfiguration {
    
    public String absolutePath;
    public EnvironmentType type;
    public boolean verbose;
    public XMLReader configurationFile;
    
//    Amazon
    public AmazonProvider amazon;
    public AmazonEC2Client amazonClient;
    public Thread listener;
    
    public EnvironmentConfiguration(XMLReader configurationFile) throws IOException, FileNotFoundException, InterruptedException{
        this.absolutePath = System.getProperty("user.dir");
        this.type = configurationFile.environmentType;
        this.verbose = configurationFile.verbose;
        this.configurationFile = configurationFile;
        
        if(this.type == EnvironmentType.CLOUD_AMAZON){
            amazon = new AmazonProvider(configurationFile.awsAccessKeyId, configurationFile.awsSecretAccessKey);
            amazonClient = amazon.startProvider();
        }
    }

//    public void deallocateVirtualMachines(ArrayList<VirtualMachineType> machinesToDeallocate, EMachine superNode) {
//        amazon.deallocateVirtualMachines(this, machinesToDeallocate, superNode);
//    }
//
//    public void allocateVirtualMachines(ArrayList<VirtualMachineType> machinesToAllocate) throws InterruptedException {
//        amazon.allocateVirtualMachines(this, machinesToAllocate);
//    }
//
//    public void abortChironInstances(EMachine superNode) throws IOException, InterruptedException {
//        ChironInvocation.abortSciCumulusCore(this, superNode);
//    }

    public String getClusterName() {
        return configurationFile.clusterName;
    }

    public ArrayList<VirtualMachineType> getVMTypes() {
        return configurationFile.vmTypes;
    }
    
    public String getMacUser(){
        return configurationFile.macUser;
    }
    
    public String getMacPassword(){
        return configurationFile.macPassword;
    }

    public String getImage() {
        return configurationFile.image;
    }

    public String getBucketName() {
        return configurationFile.bucketName;
    }

    public String getWorkflowDir() {
        return configurationFile.workflowDir;
    }

    public String getDBServer() {
        return configurationFile.dbServer;
    }

    public String getDBPort() {
        return configurationFile.dbPort;
    }

    public String getDBName() {
        return configurationFile.dbName;
    }
    
    public String getDBUser(){
        return configurationFile.dbUser;
    }
    
    public String getDBPassword(){
        return configurationFile.dbPassword;
    }

    public String getBinaryDirectory() {
        return configurationFile.binaryDirectory;
    }

    public String getSCCore() {
        return configurationFile.SCCore;
    }

    public String getConfigurationFileName() {
        return configurationFile.configurationFileName;
    }
    
}
