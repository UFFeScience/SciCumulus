package chiron;

import ch.ethz.ssh2.Connection;
import chiron.cloud.CloudUtils;
import chiron.environment.EnvironmentConfiguration;
import com.amazonaws.services.ec2.AmazonEC2Client;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Daniel, Vítor
 */
public class ChironInvocation {

    static int numberOfProcesses;
    static final int AUTHORIZED_PORT = 22000, PROTOCOL_SWITCH_LIMIT = 131072;
    static final String lineSeparator = System.getProperty("line.separator");

    public static void generatePoolControlFile(XMLReader confFile, List<EMachine> machines) throws IOException {
        //This method creates a file of the type machine.conf on the fileName location
        File outputFile = new File(confFile.workflowDir + "/machines.conf");
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
        for (EMachine mac : machines) {
            line = mac.publicDNS + "@" + port + "@" + String.valueOf(mac.rank) + lineSeparator;
            port += 2;
            outputBuffer.write(line);
            outputBuffer.flush();
        }

        outputBuffer.close();
    }
    
    public static void executeRemoteSciCumulusCore(AmazonEC2Client amazonClient, XMLReader confFile, EMachine machine) throws IOException, InterruptedException {
        CloudUtils.printFirstLevel("Executing remote SciCumulus Core...");
        boolean connected;
        
        Connection conn = new Connection(machine.publicDNS, 22);
        boolean openConnection = tryToConnect(conn, 0);
        while(!openConnection){
            openConnection = tryToConnect(conn, 0);
        }
        
        connected = conn.authenticateWithPassword(confFile.macUser, confFile.macPassword);

        try {
            if (connected) {
//                ChironUtils.executeRemoteCommand(conn, "killall java");
//                ChironUtils.executeRemoteCommand(conn, "fusermount -u " + 
//                        confFile.getWorkflowDir() + " " + confFile.getBucketName());
//                ChironUtils.executeRemoteCommand(conn, "rm -r " + confFile.workflowDir);
//                ChironUtils.sleep(10000);
                    // Create the remote mount poit directory
                ChironUtils.executeRemoteCommand(conn, "mkdir " + confFile.workflowDir);
                
                CloudUtils.printSecondLevel(machine.publicDNS);
                ChironUtils.sleep(5000);
//                CloudUtils.printSecondLevel("export AWSSECRETACCESSKEY=" + confFile.awsSecretAccessKey +
//                                ";export AWSACCESSKEYID=" + confFile.awsAccessKeyId + 
//                                ";s3fs " + confFile.bucketName + " " + confFile.workflowDir);
                ChironUtils.executeRemoteCommand(conn, "export AWSSECRETACCESSKEY=" + confFile.awsSecretAccessKey +
                                ";export AWSACCESSKEYID=" + confFile.awsAccessKeyId + 
                                ";s3fs " + confFile.bucketName + " " + confFile.workflowDir);

                ChironUtils.sleep(5000);
                ChironUtils.executeRemoteCommand(conn, "chmod 777 -R " + confFile.workflowDir);

                // To execute workflow 
                System.out.println("java -jar " + confFile.binaryDirectory + "/" + confFile.SCCore + " " + 
                        confFile.configurationFileName + " " +
                        machine.rank + " " + 
                        machine.cores + 
                        " >> output_mac_" + machine.rank + ".txt &");

                ChironUtils.executeRemoteCommand(conn, "java -jar " + confFile.binaryDirectory + "/" + confFile.SCCore + " " + 
                        confFile.configurationFileName + " " +
                        machine.rank + " " + 
                        machine.cores + 
                        " >> output_mac_" + machine.rank + ".txt &");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void abortSciCumulusCore(EnvironmentConfiguration environment, EMachine superNode) throws IOException, InterruptedException {
        CloudUtils.printFirstLevel("Aborting SciCumulus Core instances...");
        boolean connected;
        ArrayList<EMachine> machines = CloudUtils.getMachinesFromCluster(environment.amazonClient, environment.configurationFile);
        
        for(EMachine machine : machines){
            if(!machine.publicDNS.equals(superNode.publicDNS)){
                Connection conn = new Connection(machine.publicDNS, 22);
                conn.connect();
                connected = conn.authenticateWithPassword(environment.getMacUser(), environment.getMacPassword());

                if (connected) {
//                    ChironUtils.executeRemoteCommand(conn, "fusermount -u " + 
//                            environment.getWorkflowDir() + " " + environment.getBucketName());
//                    ChironUtils.executeRemoteCommand(conn, "rm -rf " + environment.getWorkflowDir());

                    ChironUtils.executeRemoteCommand(conn, "killall java");
                }
            }
        }
    }

    private static boolean tryToConnect(Connection conn, int tries) throws InterruptedException {
        try {
            conn.connect();
            return true;
        } catch (IOException ex) {
            if(tries < 10){
                tries++;
                Thread.sleep(3000);
                return tryToConnect(conn, tries);
            }else{
                return false;
            }
        }
    }
}
