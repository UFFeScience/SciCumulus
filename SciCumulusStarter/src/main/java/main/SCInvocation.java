package main;

import adaptability.Dimensioner;
import adaptability.MachineComparison;
import adaptability.VirtualMachineType;
import environment.AmazonMachine;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import enumeration.ConceptualWkfOperation;
import environment.AmazonProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/* 
    SciCumulus command line:
    {operation} {configuration_file}

    Operation types:
    -ic --> to create a cluster
    -dc --> to delete a cluster
    -icw --> to insert a conceptual workflow in provenance database
    -ucw --> to update a conceptual workflow in provenance database
    -dcw --> to delete a conceptual workflow in provenance database
    -sew --> to submit an execution workflow to control node
    -rew --> to run an execution workflow
    -dew --> to delete an execution workflow
    -mew --> to monitor execution workflow progress
    -q --> to query provenance database
*/

/**
 *
 * @author Daniel, Vítor
 */
public class SCInvocation {

    static int numberOfProcesses;
    static final int AUTHORIZED_PORT = 22000, PROTOCOL_SWITCH_LIMIT = 131072;
    static final String lineSeparator = System.getProperty("line.separator");

    static void generatePoolControlFile(List<AmazonMachine> machines) throws IOException {
        //This method creates a file of the type machine.conf on the fileName location
        File outputFile = new File("machines.conf");
        outputFile.createNewFile();
        BufferedWriter outputBuffer = new BufferedWriter(new FileWriter(outputFile));
        String line = "";

        line = "# Number of Processes\n";
        outputBuffer.write(line);

        numberOfProcesses = machines.size();
        line = numberOfProcesses + lineSeparator;
        outputBuffer.write(line);

        line = "# Protocol switch limit" + lineSeparator;
        outputBuffer.write(line);

        line = PROTOCOL_SWITCH_LIMIT + lineSeparator;
        outputBuffer.write(line);
        
        line = "# Entry in the form of machinename@port@rank" + lineSeparator;
        outputBuffer.write(line);
        
        outputBuffer.flush();
        int port = 22000;
        for (AmazonMachine mac : machines) {
            line = mac.getPublicDNS() + "@" + port + "@" + String.valueOf(mac.getRank()) + lineSeparator;
            port += 2;
            outputBuffer.write(line);
            outputBuffer.flush();
        }

        outputBuffer.close();
    }

    public static void executeSciCumulusSetup(AmazonEC2Client amazonClient, ConceptualWkfOperation operation, XMLReader confFile) {
        try {
            SCUtils.printFirstLevel("Performing operation in conceptual workflow ...");
            AmazonMachine controlInstance = SCUtils.getControlInstancesFromCluster(amazonClient, confFile);
            if(controlInstance != null){
                boolean connected;
                Connection conn = new Connection(controlInstance.publicDNS, 22);
                boolean sshConnection = tryToConnect(conn, 0);
                if(sshConnection){
                    connected = conn.authenticateWithPassword(confFile.macUser, confFile.macPassword);

                    if (connected) {
                        // Copy conceptual workflow to virtual machine
                        SCUtils.executeCommand(conn, "rm -r " + confFile.filePath);
                        SCUtils.sendDataBySCP(conn, confFile.filePath, "~");

                        if(operation == ConceptualWkfOperation.DELETE ||
                                operation == ConceptualWkfOperation.INSERT){
                            Session sess = SCUtils.executeCommand(conn, "java -jar " + confFile.binaryDirectory + "/" + confFile.SCSetup + " " 
                                + SCUtils.getArgumentForConceptualWkfOper(operation) + " " + confFile.filePath);

                            SCUtils.printSecondLevel("Executing SciCumulus Setup");
                            SCUtils.printSecondLevel(operation.toString() + " conceptual workflow");
                            SCUtils.printSecondLevel("java -jar " + confFile.binaryDirectory + "/" + confFile.SCSetup + " " 
                                    + SCUtils.getArgumentForConceptualWkfOper(operation) + " " + confFile.filePath, confFile.verbose);
                            SCUtils.printSessionResult(sess);
                        }else if(operation == ConceptualWkfOperation.UPDATE){
                            operation = ConceptualWkfOperation.DELETE;
                            Session sess = SCUtils.executeCommand(conn, "java -jar " + confFile.binaryDirectory + "/" + confFile.SCSetup + " " 
                                    + SCUtils.getArgumentForConceptualWkfOper(operation) + " " + confFile.filePath);

                            SCUtils.printSecondLevel("Executing SciCumulus Setup");
                            SCUtils.printSecondLevel("Inserting conceptual workflow");
                            SCUtils.printSecondLevel("java -jar " + confFile.binaryDirectory + "/" + confFile.SCSetup + " " 
                                    + SCUtils.getArgumentForConceptualWkfOper(operation) + " " + confFile.filePath, confFile.verbose);
                            SCUtils.printSessionResult(sess);

                            SCUtils.printSecondLevel("-----------------------------------------");
                            SCUtils.printSecondLevel("Deleting conceptual workflow");

                            operation = ConceptualWkfOperation.INSERT;
                            sess = SCUtils.executeCommand(conn, "java -jar " + confFile.binaryDirectory + "/" + confFile.SCSetup + " " 
                                    + SCUtils.getArgumentForConceptualWkfOper(operation) + " " + confFile.filePath);

                            SCUtils.printSecondLevel("java -jar " + confFile.binaryDirectory + "/" + confFile.SCSetup + " " 
                                    + SCUtils.getArgumentForConceptualWkfOper(operation) + " " + confFile.filePath, confFile.verbose);
                            SCUtils.printSessionResult(sess);
                        }
                    }
                }else{
                    System.out.println("SSH connection is not available for instance " + controlInstance.publicDNS);
                }
            }else{
                SCUtils.printSecondLevel("Control instance was not found!");
            }
        } catch (Exception ex) {
            SCUtils.printSecondLevel("Problems to insert conceptual workflow ...");
            ex.printStackTrace();
        }
    }
    
    public static void executeSciCumulusCoreAdaptively(AmazonProvider amazon, AmazonEC2Client amazonClient, XMLReader configurationFile) throws IOException, InterruptedException, SQLException {
        if(SCUtils.hasAliveInstanceFromCluster(amazonClient, configurationFile.clusterName)){
            SCUtils.checkSuperNode(amazon, amazonClient, configurationFile);
            SCInvocation.executeAdaptiveAlgorithm(amazon, amazonClient, configurationFile, true);

            long startTime;
            long endTime;
            long threshold = 60000;

            boolean hasExecutionWorkflow = SCProvenance.hasExecutionWorkflow(configurationFile.wkfExecTag);
            while(!hasExecutionWorkflow){
                Thread.sleep(5000);
                hasExecutionWorkflow = SCProvenance.hasExecutionWorkflow(configurationFile.wkfExecTag);
            }

            boolean wkfHasFinished = SCProvenance.workflowHasFinished(configurationFile.wkfExecTag);
            while(!wkfHasFinished){
                startTime = System.currentTimeMillis();
                endTime = System.currentTimeMillis();
                while(endTime - startTime < threshold){
                    endTime = System.currentTimeMillis();
                }

                wkfHasFinished = SCProvenance.workflowHasFinished(configurationFile.wkfExecTag);

                if(!wkfHasFinished){
                    SCInvocation.executeAdaptiveAlgorithm(amazon, amazonClient, configurationFile, false);
                    wkfHasFinished = SCProvenance.workflowHasFinished(configurationFile.wkfExecTag);
                }
            }
            
            System.out.println("Workflow " + configurationFile.wkfExecTag + " was completely executed using SciCumulus Core");
        }else{
            SCUtils.printFirstLevel("You did not create a cluster with name " + configurationFile.clusterName + "!");
        }
    }
    
    static void executeAdaptiveAlgorithm(AmazonProvider amazon, AmazonEC2Client amazonClient, XMLReader confFile, boolean firstTime) throws IOException, InterruptedException, SQLException {
        SCUtils.printOperation("Runtime Analysis using an Adaptive Algorithm");
        SCUtils.printFirstLevel("Executing Adaptive Algorithm...", confFile.verbose);
        ArrayList<VirtualMachineType> instanciatedMachines = SCUtils.getVirtualMachineTypesFromCluster(amazonClient, confFile);

        AmazonMachine controlNode = SCUtils.getControlInstancesFromCluster(amazonClient, confFile);
        Dimensioner dim = new Dimensioner(confFile, controlNode);
        List<VirtualMachineType> newConfMachines = dim.calculateVMDimensioning();

//        if(!newConfMachines.isEmpty()){
//            SCUtils.printFirstLevel("Next dimensioning configuration...", confFile.verbose);
//        }
        
        for (VirtualMachineType x : newConfMachines) {
            if(confFile.verbose){
                if(x.getType().equals("t1.micro")){
                    x.setAmountInstantiatedVM(2);
                }else{
                    x.setAmountInstantiatedVM(0);
                }
            }
//            SCUtils.printSecondLevel(x.toString(), confFile.verbose);
        }

        MachineComparison comparison = SCUtils.compareInstanciatedMachines(instanciatedMachines, newConfMachines);
        if(!comparison.getMachinesToDeallocate().isEmpty() || !comparison.getMachinesToAllocate().isEmpty()){
            amazon.allocateVirtualMachines(amazonClient, confFile, comparison.getMachinesToAllocate());
            SCInvocation.abortSciCumulusCore(amazon, amazonClient, confFile);
            amazon.deallocateVirtualMachines(amazonClient, confFile, comparison.getMachinesToDeallocate());

            if(!SCUtils.hasAliveInstanceFromCluster(amazonClient, confFile.clusterName)){
                DeleteKeyPairRequest kpRequest = new DeleteKeyPairRequest(SCUtils.getKeyPairName(confFile.clusterName));
                amazonClient.deleteKeyPair(kpRequest);

                DeleteSecurityGroupRequest scRequest = new DeleteSecurityGroupRequest(SCUtils.getSecurityGroupName(confFile.clusterName));
                amazonClient.deleteSecurityGroup(scRequest);
            } else{
                SCUtils.checkSuperNode(amazon, amazonClient, confFile);
            }

            invokeSciCumulusCore(amazonClient, confFile);
        }else if(firstTime){
            SCUtils.checkSuperNode(amazon, amazonClient, confFile);
            SCInvocation.abortSciCumulusCore(amazon, amazonClient, confFile);
            invokeSciCumulusCore(amazonClient, confFile);
        }
    }
    
    private static void invokeSciCumulusCore(AmazonEC2Client amazonClient, XMLReader confFile) throws IOException, InterruptedException {
        SCUtils.printFirstLevel("Invoking SciCumulus Core...");
        boolean connected;
        ArrayList<AmazonMachine> machines = SCUtils.getCoreInstancesFromCluster(amazonClient, confFile);
        generatePoolControlFile(machines);

        for(AmazonMachine machine : machines){
            // Configurate the environment for a VM 
            Connection vmConn = new Connection(machine.getPublicDNS(), 22);
            
            boolean openConnection = tryToConnect(vmConn, 0);
            if(openConnection){
                connected = vmConn.authenticateWithPassword(confFile.macUser, confFile.macPassword);
                if (connected) {
                    SCUtils.printSecondLevel("Configurating machine with public DNS " + machine.getPublicDNS());
                    if(confFile.upload){
                        // Copy compressed workspace to the virtual machine
                        SCUtils.sendDataBySCP(vmConn, confFile.compressedWorkspace, confFile.binaryDirectory);
                        // remotely unzip the compressed workspace
                        SCUtils.executeCommand(vmConn, "unzip -o " + confFile.binaryDirectory + "/" + confFile.compressedWorkspace);
                        // Remotely remove a mount point
        //                    SCUtils.executeCommand(conn, "rm -r " + confFile.workflowDir);
                        SCUtils.executeCommand(vmConn, "rm -r output_mac_*");
                        // Create the remote mount poit directory
                        SCUtils.executeCommand(vmConn, "mkdir " + confFile.workflowDir);
                        SCUtils.sleep(5000);

                        // mount the bucket to the directory
    //                    SCUtils.executeCommand(masterConn, 
    //                        "fusermount -u " + confFile.getWorkflowDir() + " " +
    //                                confFile.getBucketName());
//                        System.out.println("export AWSSECRETACCESSKEY=" + confFile.awsSecretAccessKey +
//                                ";export AWSACCESSKEYID=" + confFile.awsAccessKeyId + 
//                                ";s3fs " + confFile.bucketName + " " + confFile.workflowDir);
                        SCUtils.executeCommand(vmConn, "export AWSSECRETACCESSKEY=" + confFile.awsSecretAccessKey +
                                ";export AWSACCESSKEYID=" + confFile.awsAccessKeyId + 
                                ";s3fs " + confFile.bucketName + " " + confFile.workflowDir);

                        SCUtils.sleep(10000);
                        SCUtils.executeCommand(vmConn, "chmod 777 -R " + confFile.workflowDir);
                    }
                    SCUtils.sendDataBySCP(vmConn, "machines.conf", confFile.workflowDir);
                    // transfer workflow definitions 
                    SCUtils.sendDataBySCP(vmConn, confFile.filePath, confFile.workflowDir);
                    SCUtils.executeCommand(vmConn, "cp -rf " + confFile.compressedDir + "/* " + confFile.workflowDir);
                }else{
                    SCUtils.printSecondLevel("Problems to authenticate connection with super node");
                }
            }else{
                SCUtils.printSecondLevel("Super node does not have permission to connect via SSH");
                SCUtils.printSecondLevel(machine.getPublicDNS() + " is unavailable");
                System.exit(-1);
            }
        }
        
        AmazonMachine machine = machines.get(0);
        Connection masterConn = new Connection(machine.getPublicDNS(), 22);
        SCUtils.printSecondLevel("Super node - Public DNS: " + machine.getPublicDNS(), confFile.verbose);
        
        Thread.sleep(10000);

        boolean openConnection = tryToConnect(masterConn, 0);
        if(openConnection){
            connected = masterConn.authenticateWithPassword(confFile.macUser, confFile.macPassword);
            if (connected) {
                //VITOR-Adicionar sync de S3FS
//                Thread.sleep(60000);
                String[] splits = confFile.filePath.split("/");
                SCUtils.printSecondLevel("java -jar " + confFile.binaryDirectory + "/" + confFile.SCCore + 
                        " " + confFile.workflowDir + "/" + splits[splits.length-1] +
                        " >> output_mac_" + machine.getRank() + ".txt &", confFile.verbose);

                SCUtils.executeCommand(masterConn, "java -jar " + confFile.binaryDirectory + "/" + confFile.SCCore + 
                        " " + confFile.workflowDir + "/" + splits[splits.length-1] +
                        " >> output_mac_" + machine.getRank() + ".txt &");
            }else{
                SCUtils.printSecondLevel("Problems to authenticate connection with super node");
            }
        }else{
            SCUtils.printSecondLevel("Super node does not have permission to connect via SSH");
        }
    }

    static void deleteExecutionWorkflow(XMLReader configurationFile) {
        try {
            SCProvenance.deleteExecutionWorkflow(configurationFile.getWorkflowTag(), configurationFile.getWkfExecTag());
            SCUtils.printSecondLevel("Execution workflow " + configurationFile.getWkfExecTag() + " was removed correctly");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    static void runQuery(XMLReader configurationFile) throws SQLException, IOException {
        SCProvenance.runQuery(configurationFile);
    }

    static void monitorExecutionWorkflow(XMLReader configurationFile) throws SQLException, IOException, ParserConfigurationException, SAXException {
        String SQL = "SELECT a.actid, a.wkfid, w.tagexec, a.status, a.starttime, a.endtime "
                + "FROM eactivity as a, eworkflow as w "
                + "WHERE w.ewkfid = a.wkfid AND w.tagexec=\'" + configurationFile.getWkfExecTag() + "\';";
        configurationFile.SQL = SQL;
        SCProvenance.monitorExecution(configurationFile);
    }
    
    public static void abortSciCumulusCore(AmazonProvider amazon, AmazonEC2Client amazonClient, XMLReader configurationFile) throws IOException, InterruptedException {
        SCUtils.printFirstLevel("Aborting SciCumulus Core instances...", configurationFile.verbose);
        boolean connected;
        ArrayList<AmazonMachine> machines = SCUtils.getCoreInstancesFromCluster(amazonClient, configurationFile);
        
        for(AmazonMachine machine : machines){
            if(!machine.publicDNS.equals(configurationFile.dbServer)){
                Connection conn = new Connection(machine.publicDNS, 22);
                boolean openConnection = tryToConnect(conn, 0);
                
                if(openConnection){
                    connected = conn.authenticateWithPassword(configurationFile.macUser, configurationFile.macPassword);

                    if (connected) {
                        SCUtils.executeRemoteCommand(conn, "killall java");
//                        SCUtils.executeRemoteCommand(conn, 
//                                "fusermount -u " + configurationFile.getWorkflowDir() + " " +
//                                        configurationFile.getBucketName());
//                        SCUtils.executeRemoteCommand(conn, "rm -rf " + configurationFile.getWorkflowDir());
                    }
                }
            }
        }
    }

    private static boolean tryToConnect(Connection conn, int tries) throws InterruptedException {
        try {
            conn.connect();
            return true;
        } catch (IOException ex) {
            if(tries < 100){
                tries++;
                Thread.sleep(3000);
                return tryToConnect(conn, tries);
            }else{
                return false;
            }
        }
    }

    public static void submitAdaptiveDaemon(AmazonProvider amazon, AmazonEC2Client amazonClient, XMLReader confFile) {
        try {
            AmazonMachine controlInstance = SCUtils.getControlInstancesFromCluster(amazonClient, confFile);
            if(controlInstance != null){
                SCUtils.printFirstLevel("Invoking SciCumulus Core in control node " + controlInstance.publicDNS + " ...");
                boolean connected;
                Connection conn = new Connection(controlInstance.publicDNS, 22);
                boolean sshConnection = tryToConnect(conn, 0);
                if(sshConnection){
                    connected = conn.authenticateWithPassword(confFile.macUser, confFile.macPassword);
                    if (connected) {
                        SCUtils.sendDataBySCP(conn, confFile.compressedWorkspace, ".");
                        SCUtils.sendDataBySCP(conn, confFile.filePath, ".");
                        System.out.println("java -jar " + confFile.binaryDirectory + "/SciCumulusStarter.jar -rew " + confFile.filePath + " >> output_mac_control.txt &");
                        SCUtils.executeCommand(conn, "java -jar " + confFile.binaryDirectory + "/SciCumulusStarter.jar -rew " + confFile.filePath + " >> output_mac_control.txt &");
                    }
                }else{
                    System.out.println("SSH connection is not available for control node " + controlInstance.publicDNS);
                }
            }else{
                SCUtils.printSecondLevel("Control instance was not found!");
            }
        } catch (Exception ex) {
            Logger.getLogger(SCInvocation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void abortMountedFoldersInCoreInstances(AmazonEC2Client amazonClient, XMLReader configurationFile) throws InterruptedException, IOException {
        SCUtils.printFirstLevel("Aborting mounted folders in cluster " + configurationFile.getClusterName() + " ...", configurationFile.verbose);
        boolean connected;
        ArrayList<AmazonMachine> machines = SCUtils.getCoreInstancesFromCluster(amazonClient, configurationFile);
        
        for(AmazonMachine machine : machines){
            if(!machine.publicDNS.equals(configurationFile.dbServer)){
                SCUtils.printSecondLevel("Aborting mounted folder from machine " + machine.publicDNS);
                Connection conn = new Connection(machine.publicDNS, 22);
                boolean openConnection = tryToConnect(conn, 0);
                
                if(openConnection){
                    connected = conn.authenticateWithPassword(configurationFile.macUser, configurationFile.macPassword);

                    if (connected) {
                        SCUtils.executeRemoteCommand(conn, 
                                "fusermount -u " + configurationFile.getWorkflowDir() + " " +
                                        configurationFile.getBucketName());
                        SCUtils.sleep(5000);
                        SCUtils.executeRemoteCommand(conn, "rm -rf " + configurationFile.getWorkflowDir());
                    }
                }
            }
        }
    }
}
