package chiron.cloud;

import chiron.ChironUtils;
import chiron.EMachine;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
        ChironUtils.deleteFile(credentialsFile,".");
        
        return ec2;
    }

//    public List<EMachine> startVirtualMachines(List<VirtualMachineType> vmTypes, String clusterName, String ami, AmazonEC2Client amazon) throws InterruptedException {
//        List<RunInstancesResult> results = new ArrayList<RunInstancesResult>();
//        RunInstancesRequest rir;
//        RunInstancesResult result = null;
//        List<Instance> resultInstance;
//        List<EMachine> machines = new ArrayList<EMachine>();
//        List<String> resources = new LinkedList<String>();
//        List<String> instanceIds = new LinkedList<String>();
//        DescribeInstancesResult describeInstancesRequest;
//        List<Reservation> reservations;
//        List<Tag> tags = new LinkedList<Tag>();
//        CreateTagsRequest ctr;
//        Set instances = new HashSet();
//        String createdInstanceId = new String();
//        StartInstancesRequest startIR;
//        int rank = 0;
//        int i, k = 0;
//        int controlType = 0;
//        VirtualMachineType vmType;
//
//        for (VirtualMachineType vmTypeLoop : vmTypes) {
//            if (vmTypeLoop.getAmountInstantiatedVM() > 0) {
//                rir = new RunInstancesRequest(ami, vmTypeLoop.getAmountInstantiatedVM(), vmTypeLoop.getAmountInstantiatedVM());
//                rir.setInstanceType(vmTypeLoop.getType());
//                rir.setKeyName(CloudUtils.getKeyPairName(clusterName));
//                rir.setSecurityGroups(CloudUtils.getSecurityGroupList(clusterName));
//                result = amazon.runInstances(rir);
//                results.add(result);
//            }
//        }
//
//        /**
//         * Aguardando as VMs estarem no ar
//         */
//        CloudUtils.printSecondLevel("SciCumulus is waiting virtual machines to start...");
//        ChironUtils.sleep(120000);
//        CloudUtils.printSecondLevel("All virtual machines are instantiated!");
//        
//        Tag nameTag = new Tag(CloudUtils.clusterLabelName, CloudUtils.getVirtualMachinesName(clusterName));
//        Tag nodeTypeTag = new Tag(CloudUtils.clusterNodeType, "NODE");
//
//        for (RunInstancesResult res : results) {
//            resultInstance = res.getReservation().getInstances();
//
//            for (Instance ins : resultInstance) {
//                createdInstanceId = ins.getInstanceId();
//                CloudUtils.printSecondLevel("New virtual machine has been created: " + ins.getInstanceId()); //print the instance ID
//                resources.add(createdInstanceId);
//                instanceIds.add(createdInstanceId);
//                tags.add(nameTag);
//                tags.add(nodeTypeTag);
//            }
//
//            ReservationId = res.getReservation().getReservationId();
//            describeInstancesRequest = amazon.describeInstances();
//            reservations = describeInstancesRequest.getReservations();
//            
//            for (Reservation reservation : reservations) {
//                instances.addAll(reservation.getInstances());
//                if (reservation.getReservationId().equals(this.ReservationId)) {
//                    k = reservation.getInstances().size();
//                    vmType = vmTypes.get(controlType);
//                    while (vmType.getAmountInstantiatedVM() < 1) {
//                        controlType++;
//                        vmType = vmTypes.get(controlType);
//                    }
//                    
//                    for (i = 0; i < k; i++) {
//                        machines.add(new EMachine(rank, reservation.getInstances().get(i).getPublicDnsName(), 
//                                reservation.getInstances().get(0).getPublicIpAddress(), 
//                                reservation.getInstances().get(0).getPrivateIpAddress(), 
//                                vmType.getType(), vmType.getNumberOfCores()));
//                        rank++;
//                        controlType++;
//                    }
//                }
//            }
//
//            ctr = new CreateTagsRequest(resources, tags);
//            amazon.createTags(ctr);
//            startIR = new StartInstancesRequest(instanceIds);
//
//            amazon.startInstances(startIR);
//        }
//        
//        return machines;
//    }
//
//    public String createKeyPair(AmazonEC2Client amazon, String clusterName) throws IOException {
//        String keyName = CloudUtils.getKeyPairName(clusterName);
//        
//        CreateKeyPairRequest newKeyRequest = new CreateKeyPairRequest();
//        newKeyRequest.setKeyName(keyName);
//        CreateKeyPairResult keyresult = amazon.createKeyPair(newKeyRequest);
//
//        keyPair = keyresult.getKeyPair();
//        CloudUtils.printSecondLevel("The key SciCumulus created is = " + keyPair.getKeyName());
////        System.out.println("The key SciCumulus created is = "
////                + keyPair.getKeyName() + "\nIts fingerprint is="
////                + keyPair.getKeyFingerprint() + "\nIts material is= \n"
////                + keyPair.getKeyMaterial());
//
//        File distFile = new File(keyName + ".pem");
//        BufferedReader bufferedReader = new BufferedReader(new StringReader(keyPair.getKeyMaterial()));
//        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(distFile));
//        char buf[] = new char[1024];
//        int len;
//        while ((len = bufferedReader.read(buf)) != -1) {
//            bufferedWriter.write(buf, 0, len);
//        }
//        bufferedWriter.flush();
//        bufferedReader.close();
//        bufferedWriter.close();
//        return keyName;
//
//    }
//
//    public void verifyExistingMachines(AmazonEC2Client amazon) {
//        CloudUtils.printFirstLevel("#5 Describing Current Instances");
//        DescribeInstancesResult describeInstancesRequest = amazon.describeInstances();
//        List<Reservation> reservations = describeInstancesRequest.getReservations();
//        Set<Instance> instances = new HashSet<Instance>();
//        // add all instances to a Set.
//        for (Reservation reservation : reservations) {
//            instances.addAll(reservation.getInstances());
//        }
//
//        CloudUtils.printFirstLevel("You have " + instances.size() + " Amazon EC2 instance(s).");
//        for (Instance ins : instances) {
//            // instance id
//            String instanceId = ins.getInstanceId();
//            // instance state
//            InstanceState is = ins.getState();
//            CloudUtils.printSecondLevel(instanceId + " " + is.getName() + " " + ins.getPublicDnsName());
//        }
//
//    }
//
//    public String createSecurityGroup(AmazonEC2Client amazon, String clusterName) {
//        String newGroupName = CloudUtils.getSecurityGroupName(clusterName); //name of the group
//        CreateSecurityGroupRequest r1 = new CreateSecurityGroupRequest(newGroupName, "SciCumulus temporal group");
//        amazon.createSecurityGroup(r1);
//        AuthorizeSecurityGroupIngressRequest r2 = new AuthorizeSecurityGroupIngressRequest();
//        r2.setGroupName(newGroupName);
//
//        /**
//         * *********** http****************
//         */
//        IpPermission permission = new IpPermission();
//        permission.setIpProtocol("tcp");
//        permission.setFromPort(80);
//        permission.setToPort(80);
//        List<String> ipRanges = new ArrayList<String>();
//        ipRanges.add("0.0.0.0/0");
//        permission.setIpRanges(ipRanges);
//
//        /**
//         * *********** SSH*********************
//         */
//        IpPermission permission1 = new IpPermission();
//        permission1.setIpProtocol("tcp");
//        permission1.setFromPort(22);
//        permission1.setToPort(22);
//        List<String> ipRanges1 = new ArrayList<String>();
//        ipRanges1.add("0.0.0.0/0");
//        permission1.setIpRanges(ipRanges1);
//
//        /**
//         * *********** https*********************
//         */
//        IpPermission permission2 = new IpPermission();
//        permission2.setIpProtocol("tcp");
//        permission2.setFromPort(443);
//        permission2.setToPort(443);
//        List<String> ipRanges2 = new ArrayList<String>();
//        ipRanges2.add("0.0.0.0/0");
//        permission2.setIpRanges(ipRanges2);
//
//        /**
//         * ***********mensagens tcp*********************
//         */
//        IpPermission permission3 = new IpPermission();
//        permission3.setIpProtocol("tcp");
//        permission3.setFromPort(0);
//        permission3.setToPort(65535);
//        List<String> ipRanges3 = new ArrayList<String>();
//        ipRanges3.add("0.0.0.0/0");
//        permission3.setIpRanges(ipRanges3);
//
//        /**
//         * ********************Adicionando as regras********************
//         */
//        List<IpPermission> permissions = new ArrayList<IpPermission>();
//        permissions.add(permission);
//        permissions.add(permission1);
//        permissions.add(permission2);
//        permissions.add(permission3);
//        r2.setIpPermissions(permissions);
//
//        amazon.authorizeSecurityGroupIngress(r2);
//        return newGroupName;
//    }

    public void shutDownProvider(AmazonEC2Client amazon) {
        amazon.shutdown();
    }

//    public void allocateVirtualMachines(EnvironmentConfiguration environment, ArrayList<VirtualMachineType> machinesToAllocate) throws InterruptedException {
//        if(!machinesToAllocate.isEmpty()){
////            to check if node is supernode
//            boolean hasAliveInstance = CloudUtils.hasAliveInstanceFromCluster(environment.amazonClient, environment.getClusterName());
//
//            if(hasAliveInstance){
//                CloudUtils.printFirstLevel("Allocating new virtual machines in cluster " + environment.getClusterName() + "...");
//                startVirtualMachines(machinesToAllocate, environment.getClusterName(), environment.getImage(), environment.amazonClient);
//            }
//        }
//    }
//
//    public void deallocateVirtualMachines(EnvironmentConfiguration environment, ArrayList<VirtualMachineType> machinesToDeallocate, EMachine superNode) {
//        if(!machinesToDeallocate.isEmpty()){
//            boolean hasAliveInstance = CloudUtils.hasAliveInstanceFromCluster(environment.amazonClient, environment.getClusterName());
//
//            if(hasAliveInstance){
//                CloudUtils.printSecondLevel("Deallocating virtual machines in cluster " + environment.getClusterName() + "...");
//                finishVirtualMachines(machinesToDeallocate, environment.getClusterName(), environment.getImage(), environment.amazonClient, superNode);
//            }
//        }
//    }
//
//    private void finishVirtualMachines(ArrayList<VirtualMachineType> machinesToDeallocate, String clusterName, String image, AmazonEC2Client amazonClient, EMachine superNode) {
//        LinkedList<String> instanceIds = new LinkedList<String>();
//        
////        to get instance ids
//        for(VirtualMachineType vmType : machinesToDeallocate){
//            int numberOfDeallocations = vmType.getAmountInstantiatedVM();
//            
//            DescribeInstancesResult result = CloudUtils.getAliveInstancesFromClusterByType(amazonClient, clusterName, vmType);
//            CloudUtils.printSecondLevel("Removing " + vmType.getAmountInstantiatedVM() 
//                    + " instances of type " + vmType.getType() 
//                    + " from cluster " + clusterName + "...");
//            List<Reservation> reservations = result.getReservations();
//            
//            if(!reservations.isEmpty()){
//                for (Reservation reservation : reservations) {
//                    List<Instance> instances = reservation.getInstances();
//                    for (Instance instance : instances) {
//                        if(numberOfDeallocations > 0 && !instance.getPublicDnsName().equals(superNode.publicDNS)){
//                            instanceIds.add(instance.getInstanceId());
//                            CloudUtils.printSecondLevel(instance.getInstanceId() + " ...");
//                            numberOfDeallocations--;
//                        }
//                    }
//                }
//                
//                TerminateInstancesRequest terminateInstances = new TerminateInstancesRequest(instanceIds);
//                amazonClient.terminateInstances(terminateInstances);
//            }else{
//                CloudUtils.printSecondLevel("There is not any instance in this cluster!");
//            }
//        }
//    }
}
