package environment;

import adaptability.VirtualMachineType;
import main.XMLReader;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import enumeration.NodeType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import main.SCProvenance;
import main.SCUtils;
import vs.database.M_DB;

/**
 *
 * @author Daniel, Vítor
 */
public class AmazonProvider {

    String accesskey = new String();
    String secretaccesskey = new String();
    static Random rand = new Random();
    KeyPair keyPair;
    private String ReservationId;

    public AmazonProvider(String key, String secretkey) {
        accesskey = key;
        secretaccesskey = secretkey;
    }

    public AmazonEC2Client startProvider() throws FileNotFoundException, IOException, InterruptedException {
        String credentialsFile = "credentials.sc";
        FileWriter writer = new FileWriter(new File(credentialsFile), true);
        PrintWriter saida = new PrintWriter(writer);
        saida.println("secretKey=" + secretaccesskey);
        saida.println("accessKey=" + accesskey);
        saida.close();
        writer.close();

        File f = new File(credentialsFile);
        FileInputStream fis = new FileInputStream(f);

        AWSCredentials credentials = new PropertiesCredentials(fis);
        AmazonEC2Client ec2 = new AmazonEC2Client(credentials);
        SCUtils.deleteFile(credentialsFile,".");

        return ec2;
    }

    public void createCluster(AmazonEC2Client amazonClient, XMLReader configurationFile, String absolutePath) throws Exception {
        boolean hasAliveInstance = SCUtils.hasAliveInstanceFromCluster(amazonClient, configurationFile.clusterName);
        boolean hasSecurityGroup = SCUtils.hasAliveSecurityGroupFromCluster(amazonClient, configurationFile);
        boolean hasKeyPairGroup = SCUtils.hasAliveKeyPairFromCluster(amazonClient, configurationFile);

        if(!hasAliveInstance && !hasSecurityGroup && !hasKeyPairGroup){
//            SCUtils.printFirstLevel("Calculating Dimensioning...");
//            /*
//             * Cria o objeto de dimensionamento que sera usado a partir daqui
//             */
//            Dimensioner dim = new Dimensioner(configurationFile.getDbName(), configurationFile.getDbServer(), 
//                    configurationFile.getDbUser(), configurationFile.getDbPassword(), configurationFile.getMaxTime(), 
//                    configurationFile.getMaxFinancialCost(), configurationFile.getMaxVMAmount(), configurationFile.getTotalRAM(), 
//                    configurationFile.getTotaldisk(), configurationFile.getAlfa1(), configurationFile.getAlfa2(), configurationFile.getAlfa3());
//
//            /*
//             * Retorna o cojunto de maquinas a ser usado e a quantidade de cada maquina a ser instanciada
//             */
//            List<VirtualMachineType> vms = dim.calculateVMDimensioning(configurationFile.vmTypes, configurationFile.wkfTag, 
//                    configurationFile.wkfExecTag, configurationFile.getFirstWorkflowActivity().tag, absolutePath);
//            
//            SCUtils.printFirstLevel("Next dimensioning configuration...");
//            for (VirtualMachineType x : vms) {
//                if(configurationFile.verbose){
//                    if(x.getType().equals("t1.micro")){
//                        x.setAmountInstantiatedVM(1);
//                    }else{
//                        x.setAmountInstantiatedVM(0);
//                    }
//                }
//                SCUtils.printSecondLevel(x.toString());
//            }
//
//            if(configurationFile.vmTypes.isEmpty()){
//                VirtualMachineType vmType = new VirtualMachineType(1.2, 1680, 23, 93.856, "64", 1);
//                vmType.setType("t1.micro");
//                vmType.setAmountInstantiatedVM(1);
//                configurationFile.vmTypes.add(vmType);
//            }
//            
            configurationFile.vmTypes.get(0).setAmountInstantiatedVM(1);

            SCUtils.printFirstLevel("SciCumulus is communicating with Amazon...");
            createSecurityGroup(amazonClient, configurationFile.clusterName);
            createKeyPair(amazonClient, configurationFile.clusterName);

            SCUtils.printFirstLevel("Creating pool of virtual machines...");
            List<AmazonMachine> machines = startVirtualMachines(configurationFile.vmTypes, 
                    configurationFile.clusterName, configurationFile.image, amazonClient);

            for (AmazonMachine mac : machines) {
                SCUtils.printSecondLevel(mac.toString());
            }
            SCUtils.printFirstLevel("Amount of Virtual machines = " + machines.size());
        }else{
            SCUtils.printSecondLevel("There is an alive instance with this cluster name!");
        }
    }

    public List<AmazonMachine> startVirtualMachines(List<VirtualMachineType> vmTypes, 
            String clusterName, String ami, AmazonEC2Client amazon) throws InterruptedException {
        List<RunInstancesResult> results = new ArrayList<RunInstancesResult>();
        RunInstancesRequest rir;
        RunInstancesResult result = null;
        List<Instance> resultInstance;
        List<AmazonMachine> machines = new ArrayList<AmazonMachine>();
        List<String> resources = new LinkedList<String>();
        List<String> currentResource = new LinkedList<String>();
        List<String> instanceIds = new LinkedList<String>();
        DescribeInstancesResult describeInstancesRequest;
        List<Reservation> reservations;
        List<Tag> tags = new LinkedList<Tag>();
        CreateTagsRequest ctr;
        Set instances = new HashSet();
        Tag nameTag;
        String createdInstanceId = new String();
        StartInstancesRequest startIR;
        int rank = 0;
        int i, k = 0;
        int controlType = 0;
        VirtualMachineType vmType;

        for (VirtualMachineType vmTypeLoop : vmTypes) {
            if (vmTypeLoop.getAmountInstantiatedVM() > 0) {
                rir = new RunInstancesRequest(ami, vmTypeLoop.getAmountInstantiatedVM(), vmTypeLoop.getAmountInstantiatedVM());
                rir.setInstanceType(vmTypeLoop.getType());
                rir.setKeyName(SCUtils.getKeyPairName(clusterName));
                rir.setSecurityGroups(SCUtils.getSecurityGroupList(clusterName));
                result = amazon.runInstances(rir);
                results.add(result);
            }
        }

        /**
         * Aguardando as VMs estarem no ar
         */
        SCUtils.printSecondLevel("SciCumulus is waiting virtual machines to start...");
        SCUtils.sleep(30000);
        SCUtils.printSecondLevel("All virtual machines are instantiated!");

        boolean hasControlInstances = SCUtils.hasControlInstanceFromCluster(amazon, clusterName);
        boolean hasCoreInstances = SCUtils.hasCoreInstanceFromCluster(amazon, clusterName);
        boolean first = true;
        nameTag = new Tag(SCUtils.clusterLabelName, SCUtils.getVirtualMachinesName(clusterName));
        for (RunInstancesResult res : results) {
            resultInstance = res.getReservation().getInstances();
            
            Tag nodeTypeTag;
            for (Instance ins : resultInstance) {
                currentResource = new LinkedList<String>();
                createdInstanceId = ins.getInstanceId();
                SCUtils.printSecondLevel("New virtual machine has been created: " + ins.getInstanceId()); //print the instance ID
                resources.add(createdInstanceId);
                currentResource.add(createdInstanceId);
                instanceIds.add(createdInstanceId);
                
                tags = new LinkedList<Tag>();
                tags.add(nameTag);
                if(!hasControlInstances && first){
                    nodeTypeTag = new Tag(SCUtils.clusterNodeType, NodeType.CONTROL.toString());
                    first = false;
                }else if(!hasCoreInstances && first){
                    nodeTypeTag = new Tag(SCUtils.clusterNodeType, NodeType.SUPERNODE.toString());
                    first = false;
                }else{
                    nodeTypeTag = new Tag(SCUtils.clusterNodeType, NodeType.NODE.toString());
                }
                tags.add(nodeTypeTag);
                ctr = new CreateTagsRequest(currentResource, tags);
                amazon.createTags(ctr);
            }

            ReservationId = res.getReservation().getReservationId();
            describeInstancesRequest = amazon.describeInstances();
            reservations = describeInstancesRequest.getReservations();
            
            for (Reservation reservation : reservations) {
                instances.addAll(reservation.getInstances());
                if (reservation.getReservationId().equals(this.ReservationId)) {
                    k = reservation.getInstances().size();
                    vmType = vmTypes.get(controlType);
                    while (vmType.getAmountInstantiatedVM() < 1) {
                        controlType++;
                        vmType = vmTypes.get(controlType);
                    }
                    
                    for (i = 0; i < k; i++) {
                        machines.add(new AmazonMachine(rank, reservation.getInstances().get(i).getPublicDnsName(), 
                                reservation.getInstances().get(0).getPublicIpAddress(), 
                                reservation.getInstances().get(0).getPrivateIpAddress(), 
                                vmType.getType(), vmType.getNumberOfCores()));
                        rank++;
                        controlType++;
                    }
                }
            }

            startIR = new StartInstancesRequest(instanceIds);
            amazon.startInstances(startIR);
        }
        
        return machines;
    }

    public String createKeyPair(AmazonEC2Client amazon, String clusterName) throws IOException {
        String keyName = SCUtils.getKeyPairName(clusterName);
        
        CreateKeyPairRequest newKeyRequest = new CreateKeyPairRequest();
        newKeyRequest.setKeyName(keyName);
        CreateKeyPairResult keyresult = amazon.createKeyPair(newKeyRequest);

        keyPair = keyresult.getKeyPair();
        SCUtils.printSecondLevel("The key SciCumulus created is = " + keyPair.getKeyName());
//        System.out.println("The key SciCumulus created is = "
//                + keyPair.getKeyName() + "\nIts fingerprint is="
//                + keyPair.getKeyFingerprint() + "\nIts material is= \n"
//                + keyPair.getKeyMaterial());

        File distFile = new File(keyName + ".pem");
        BufferedReader bufferedReader = new BufferedReader(new StringReader(keyPair.getKeyMaterial()));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(distFile));
        char buf[] = new char[1024];
        int len;
        while ((len = bufferedReader.read(buf)) != -1) {
            bufferedWriter.write(buf, 0, len);
        }
        bufferedWriter.flush();
        bufferedReader.close();
        bufferedWriter.close();
        return keyName;

    }

    public void verifyExistingMachines(AmazonEC2Client amazon) {
        SCUtils.printFirstLevel("#5 Describing Current Instances");
        DescribeInstancesResult describeInstancesRequest = amazon.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        Set<Instance> instances = new HashSet<Instance>();
        // add all instances to a Set.
        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }

        SCUtils.printFirstLevel("You have " + instances.size() + " Amazon EC2 instance(s).");
        for (Instance ins : instances) {
            // instance id
            String instanceId = ins.getInstanceId();
            // instance state
            InstanceState is = ins.getState();
            SCUtils.printSecondLevel(instanceId + " " + is.getName() + " " + ins.getPublicDnsName());
        }

    }

    public String createSecurityGroup(AmazonEC2Client amazon, String clusterName) {
        String newGroupName = SCUtils.getSecurityGroupName(clusterName); //name of the group
        CreateSecurityGroupRequest r1 = new CreateSecurityGroupRequest(newGroupName, "SciCumulus temporal group");
        amazon.createSecurityGroup(r1);
        AuthorizeSecurityGroupIngressRequest r2 = new AuthorizeSecurityGroupIngressRequest();
        r2.setGroupName(newGroupName);

        /**
         * *********** http****************
         */
        IpPermission permission = new IpPermission();
        permission.setIpProtocol("tcp");
        permission.setFromPort(80);
        permission.setToPort(80);
        List<String> ipRanges = new ArrayList<String>();
        ipRanges.add("0.0.0.0/0");
        permission.setIpRanges(ipRanges);

        /**
         * *********** SSH*********************
         */
        IpPermission permission1 = new IpPermission();
        permission1.setIpProtocol("tcp");
        permission1.setFromPort(22);
        permission1.setToPort(22);
        List<String> ipRanges1 = new ArrayList<String>();
        ipRanges1.add("0.0.0.0/0");
        permission1.setIpRanges(ipRanges1);

        /**
         * *********** https*********************
         */
        IpPermission permission2 = new IpPermission();
        permission2.setIpProtocol("tcp");
        permission2.setFromPort(443);
        permission2.setToPort(443);
        List<String> ipRanges2 = new ArrayList<String>();
        ipRanges2.add("0.0.0.0/0");
        permission2.setIpRanges(ipRanges2);

        /**
         * ***********mensagens tcp*********************
         */
        IpPermission permission3 = new IpPermission();
        permission3.setIpProtocol("tcp");
        permission3.setFromPort(0);
        permission3.setToPort(65535);
        List<String> ipRanges3 = new ArrayList<String>();
        ipRanges3.add("0.0.0.0/0");
        permission3.setIpRanges(ipRanges3);

        /**
         * ********************Adicionando as regras********************
         */
        List<IpPermission> permissions = new ArrayList<IpPermission>();
        permissions.add(permission);
        permissions.add(permission1);
        permissions.add(permission2);
        permissions.add(permission3);
        r2.setIpPermissions(permissions);

        amazon.authorizeSecurityGroupIngress(r2);
        return newGroupName;
    }

    public void deleteCluster(AmazonEC2Client amazonClient, XMLReader configurationFile) throws InterruptedException {
        LinkedList<String> instanceIds = new LinkedList<String>();
        
//        to get instance ids
        {
            DescribeInstancesResult result = SCUtils.getAliveInstancesFromCluster(amazonClient, configurationFile.clusterName);
            SCUtils.printFirstLevel("Removing instances from cluster " + configurationFile.clusterName + " ...");
            List<Reservation> reservations = result.getReservations();
            if(!reservations.isEmpty()){
                for (Reservation reservation : reservations) {
                    List<Instance> instances = reservation.getInstances();
                    for (Instance instance : instances) {
                        instanceIds.add(instance.getInstanceId());
                        SCUtils.printSecondLevel(instance.getInstanceId() + " ...");
                    }
                }
            }else{
                SCUtils.printSecondLevel("There is not any instance in this cluster!");
            }
        }
        
        if(!instanceIds.isEmpty()){
            TerminateInstancesRequest terminateInstances = new TerminateInstancesRequest(instanceIds);
            amazonClient.terminateInstances(terminateInstances);
        }
        
//        to check instances after termination
        while(SCUtils.hasFinishedInstancesFromCluster(amazonClient, configurationFile.clusterName)){
            SCUtils.sleep();
        }
        
        DeleteKeyPairRequest kpRequest = new DeleteKeyPairRequest(SCUtils.getKeyPairName(configurationFile.clusterName));
        amazonClient.deleteKeyPair(kpRequest);

        DeleteSecurityGroupRequest scRequest = new DeleteSecurityGroupRequest(SCUtils.getSecurityGroupName(configurationFile.clusterName));
        try{
            amazonClient.deleteSecurityGroup(scRequest);
        }catch(Exception ex){
            SCUtils.printSecondLevel("This security group was already removed!");
        }
    }

    public void startDatabaseConnection(XMLReader config) {
        String connection = "jdbc:postgresql://" + config.dbServer + ":" + config.dbPort + "/" + config.dbName + "?chartset=UTF8";
        SCProvenance.db = new M_DB(M_DB.DRIVER_POSTGRESQL, connection, config.dbUser, config.dbPassword, true);
    }

    public void shutDownProvider(AmazonEC2Client amazon) {
        amazon.shutdown();
    }

    public void allocateVirtualMachines(AmazonEC2Client amazonClient, XMLReader configurationFile, ArrayList<VirtualMachineType> machinesToAllocate) throws InterruptedException {
        if(!machinesToAllocate.isEmpty()){
            boolean hasAliveInstance = SCUtils.hasAliveInstanceFromCluster(amazonClient, configurationFile.clusterName);

            if(hasAliveInstance){
                SCUtils.printFirstLevel("Allocating new virtual machines in cluster " + configurationFile.clusterName + "...", configurationFile.verbose);
                startVirtualMachines(machinesToAllocate, configurationFile.clusterName, configurationFile.image, amazonClient);
            }
        }
    }

    public void deallocateVirtualMachines(AmazonEC2Client amazonClient, XMLReader configurationFile, ArrayList<VirtualMachineType> machinesToDeallocate) {
        if(!machinesToDeallocate.isEmpty()){
            boolean hasAliveInstance = SCUtils.hasAliveInstanceFromCluster(amazonClient, configurationFile.clusterName);

            if(hasAliveInstance){
                SCUtils.printFirstLevel("Deallocating virtual machines in cluster " + configurationFile.clusterName + "...", configurationFile.verbose);
                finishVirtualMachines(machinesToDeallocate, configurationFile, configurationFile.image, amazonClient);
            }
        }
    }

    private void finishVirtualMachines(ArrayList<VirtualMachineType> machinesToDeallocate, XMLReader configurationFile, String image, AmazonEC2Client amazonClient) {
        LinkedList<String> instanceIds = new LinkedList<String>();
        
//        to get instance ids
        for(VirtualMachineType vmType : machinesToDeallocate){
            int numberOfDeallocations = vmType.getAmountInstantiatedVM();
            
            DescribeInstancesResult result = SCUtils.getAliveInstancesFromClusterByType(amazonClient, configurationFile, vmType);
            SCUtils.printFirstLevel("Removing " + vmType.getAmountInstantiatedVM() 
                    + " instances of type " + vmType.getType() 
                    + " from cluster " + configurationFile.clusterName + "...");
            List<Reservation> reservations = result.getReservations();
            
            if(!reservations.isEmpty()){
                for (Reservation reservation : reservations) {
                    List<Instance> instances = reservation.getInstances();
                    for (Instance instance : instances) {
                        if(numberOfDeallocations > 0){
                            instanceIds.add(instance.getInstanceId());
                            SCUtils.printSecondLevel(instance.getInstanceId() + " ...");
                            numberOfDeallocations--;
                        }
                    }
                }
                
                TerminateInstancesRequest terminateInstances = new TerminateInstancesRequest(instanceIds);
                amazonClient.terminateInstances(terminateInstances);
            }else{
                SCUtils.printSecondLevel("There is not any instance in this cluster!");
            }
        }
    }
}
