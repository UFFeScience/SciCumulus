package main;

import adaptability.MachineComparison;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import enumeration.ConceptualWkfOperation;
import environment.AmazonMachine;
import adaptability.VirtualMachineType;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteTagsRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.Tag;
import enumeration.NodeType;
import environment.AmazonProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vitor
 */
public class SCUtils {
    
    public static final String JDBC_DRIVER = "org.postgresql.Driver";
    public static final String clusterLabelName = "Name";
    public static final String clusterNodeType = "NodeType";
    public static final String clusterHeaderName = "SC-";
    public static final String keyHeaderName = clusterHeaderName + "Key-";
    public static final String securityGroupHeaderName = clusterHeaderName + "SG-";
    public static final String instanceStateNameLabel = "instance-state-name";
    public static final List<String> runningInstanceStates = new ArrayList<String>(Arrays.asList("running"));
    public static final List<String> instanceNotFinishedStates = new ArrayList<String>(Arrays.asList("running","shutting-down","pending","stopping"));
    public static final String instanceTypeNameLabel = "instance-type";
    public static final String securityGroupNameLabel = "group-name";
    public static final String keyNameLabel = "key-name";
    public static final String networkInterfaceAttachmentStatus = "network-interface.attachment.status";
    public static final String networkInterfaceStatus = "network-interface.status";
    private static final int sleepTime = 5000;
    private static final String identation = "        ";
    
    public static void printInitialMessage(){
        System.out.println("##########################################################");
        System.out.println("################### SciCumulus Starter ###################");
        System.out.println("##########################################################");
    }
    
    public static void printOperation(String text) {
        System.out.println("------------------ " + text + " ------------------");
    }
    
    public static void printFirstLevel(String text){
        System.out.println(text);
    }
    
    public static void printSecondLevel(String text){
        System.out.println(identation + text);
    }
    
    public static void printOperation(String text, boolean verbose) {
        if(verbose){
            System.out.println("------------------ " + text + " ------------------");
        }
    }
    
    public static void printFirstLevel(String text, boolean verbose){
        if(verbose){
            System.out.println(text);
        }
    }
    
    public static void printSecondLevel(String text, boolean verbose){
        if(verbose){
            System.out.println(identation + text);
        }
    }
    
    public static List<Filter> createFilter(List<Filter> filters, String key, List<String> values){
        Filter newFilter = new Filter(key);
        newFilter.setValues(values);
        filters.add(newFilter);
        
        return filters;
    }
    
    public static DescribeSecurityGroupsResult getAliveSecurityGroupFromACluster(AmazonEC2Client amazonClient, XMLReader configurationFile){
        DescribeSecurityGroupsRequest iRequest = new DescribeSecurityGroupsRequest();
        List<Filter> filter = new ArrayList<Filter>();

        List<String> values = new ArrayList<String>();
        values.add(getSecurityGroupName(configurationFile.clusterName));
        createFilter(filter, securityGroupNameLabel, values);
        iRequest.setFilters(filter);
        
        return amazonClient.describeSecurityGroups(iRequest);
    }
    
    public static boolean hasAliveSecurityGroupFromCluster(AmazonEC2Client amazonClient, XMLReader configurationFile) {
        DescribeSecurityGroupsResult result = getAliveSecurityGroupFromACluster(amazonClient, configurationFile);
        return (result.getSecurityGroups().size()>0);
    }
    
    public static DescribeKeyPairsResult getAliveKeyPairFromACluster(AmazonEC2Client amazonClient, XMLReader configurationFile){
        DescribeKeyPairsRequest iRequest = new DescribeKeyPairsRequest();
        List<Filter> filter = new ArrayList<Filter>();

        List<String> values = new ArrayList<String>();
        values.add(getKeyPairName(configurationFile.clusterName));
        createFilter(filter, keyNameLabel, values);
        iRequest.setFilters(filter);
        
        return amazonClient.describeKeyPairs(iRequest);
    }
    
    public static boolean hasAliveKeyPairFromCluster(AmazonEC2Client amazonClient, XMLReader configurationFile) {
        DescribeKeyPairsResult result = getAliveKeyPairFromACluster(amazonClient, configurationFile);
        return (result.getKeyPairs().size()>0);
    }

    public static void sleep() {
        try {
            Thread.sleep(SCUtils.sleepTime);
        } catch (InterruptedException ex) {
            Logger.getLogger(SCUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            Logger.getLogger(SCUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean hasAliveInstanceFromCluster(AmazonEC2Client amazonClient, String clusterName) {
        DescribeInstancesResult result = getAliveInstancesFromCluster(amazonClient, clusterName);
        for (Reservation reservation : result.getReservations()) {
            int instances = reservation.getInstances().size();
            if(instances > 0){
                return true;
            }
        }
        return false;
    }
    
    protected static DescribeInstancesRequest getDescribeInstancesRequest(ArrayList<Tag> tags, List<Filter> filters) {
            DescribeInstancesRequest request = new DescribeInstancesRequest();
            
            for(Tag tag : tags){
                Filter filter = getFilterFromTag(tag.getKey(), tag.getValue());
                filters.add(filter);
            }
            request.setFilters(filters);
            
            return request;
    }
    
    public static DescribeInstancesResult getAliveInstancesFromCluster(AmazonEC2Client amazonClient, String clusterName){
        List<Filter> filters = new ArrayList<Filter>();
        createFilter(filters, instanceStateNameLabel, runningInstanceStates);
        
        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag(clusterLabelName, getVirtualMachinesName(clusterName)));
        
        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
        return amazonClient.describeInstances(iRequest);
    }
    
    public static DescribeInstancesResult getAliveInstancesFromClusterByType(AmazonEC2Client amazonClient, XMLReader configurationFile, VirtualMachineType vmType){
        List<Filter> filters = new ArrayList<Filter>();
        createFilter(filters, instanceStateNameLabel, runningInstanceStates);
        
        List<String> values = new ArrayList<String>();
        values.add(vmType.getType());
        createFilter(filters, instanceTypeNameLabel, values);
        
        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag(clusterLabelName, getVirtualMachinesName(configurationFile.clusterName)));
        
        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
        
        return amazonClient.describeInstances(iRequest);
    }
    
    public static DescribeInstancesResult getDescribeSuperNodesFromCluster(AmazonEC2Client amazonClient, String clusterName){
        ArrayList<Tag> tags = new ArrayList<Tag>();
        
        List<Filter> filters = new ArrayList<Filter>();
        createFilter(filters, instanceStateNameLabel, runningInstanceStates);
        
        tags.add(new Tag(clusterLabelName, getVirtualMachinesName(clusterName)));
        tags.add(new Tag(clusterNodeType, NodeType.SUPERNODE.toString()));
        
        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
        
        return amazonClient.describeInstances(iRequest);
    }
    
    public static DescribeInstancesResult getDescribeNodesFromCluster(AmazonEC2Client amazonClient, String clusterName){
        List<Filter> filters = new ArrayList<Filter>();
        createFilter(filters, instanceStateNameLabel, runningInstanceStates);
        
        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag(clusterLabelName, getVirtualMachinesName(clusterName)));
        tags.add(new Tag(clusterNodeType, NodeType.NODE.toString()));
        
        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
        
        return amazonClient.describeInstances(iRequest);
    }
    
    public static DescribeInstancesResult getDescribeControlFromCluster(AmazonEC2Client amazonClient, String clusterName){
        List<Filter> filters = new ArrayList<Filter>();
        createFilter(filters, instanceStateNameLabel, runningInstanceStates);
        
        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag(clusterLabelName, getVirtualMachinesName(clusterName)));
        tags.add(new Tag(clusterNodeType, NodeType.CONTROL.toString()));
        
        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
        
        return amazonClient.describeInstances(iRequest);
    }
    
    public static boolean hasCoreInstanceFromCluster(AmazonEC2Client amazonClient, String clusterName) {
        DescribeInstancesResult result = getDescribeSuperNodesFromCluster(amazonClient, clusterName);
        for (Reservation reservation : result.getReservations()) {
            int instances = reservation.getInstances().size();
            if(instances > 0){
                return true;
            }
        }
        
        result = getDescribeNodesFromCluster(amazonClient, clusterName);
        for (Reservation reservation : result.getReservations()) {
            int instances = reservation.getInstances().size();
            if(instances > 0){
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean hasControlInstanceFromCluster(AmazonEC2Client amazonClient, String clusterName) {
        DescribeInstancesResult result = getDescribeControlFromCluster(amazonClient, clusterName);
        for (Reservation reservation : result.getReservations()) {
            int instances = reservation.getInstances().size();
            if(instances > 0){
                return true;
            }
        }
        
        return false;
    }

    public static ArrayList<AmazonMachine> getCoreInstancesFromCluster(AmazonEC2Client amazonClient, XMLReader configurationFile) {
        int rank = 0;
        ArrayList<AmazonMachine> machines = new ArrayList<AmazonMachine>();
        
        DescribeInstancesResult superNode = getDescribeSuperNodesFromCluster(amazonClient, configurationFile.clusterName);
        for(Reservation reservation : superNode.getReservations()){
            for(Instance instance : reservation.getInstances()){
                VirtualMachineType vmType = SCUtils.getVmType(configurationFile, instance.getInstanceType());
                AmazonMachine mac = new AmazonMachine(rank, 
                        instance.getPublicDnsName(), 
                        instance.getPublicIpAddress(),
                        instance.getPrivateIpAddress(),
                        vmType.getType(),
                        vmType.getNumberOfCores());
                machines.add(mac);
                rank++;
            }
        }
        
        DescribeInstancesResult nodes = getDescribeNodesFromCluster(amazonClient, configurationFile.clusterName);
        for(Reservation reservation : nodes.getReservations()){
            for(Instance instance : reservation.getInstances()){
                VirtualMachineType vmType = SCUtils.getVmType(configurationFile, instance.getInstanceType());
                AmazonMachine mac = new AmazonMachine(rank, 
                        instance.getPublicDnsName(), 
                        instance.getPublicIpAddress(),
                        instance.getPrivateIpAddress(),
                        vmType.getType(),
                        vmType.getNumberOfCores());
                machines.add(mac);
                rank++;
            }
        }
        
        return machines;
    }
    
    public static AmazonMachine getControlInstancesFromCluster(AmazonEC2Client amazonClient, XMLReader configurationFile) {
        DescribeInstancesResult controls = getDescribeControlFromCluster(amazonClient, configurationFile.clusterName);
        for(Reservation reservation : controls.getReservations()){
            for(Instance instance : reservation.getInstances()){
                AmazonMachine mac = new AmazonMachine(instance.getPublicDnsName(), 
                        instance.getPublicIpAddress(),
                        instance.getPrivateIpAddress());
                return mac;
            }
        }
        
        return null;
    }
    
    public static ArrayList<VirtualMachineType> getVirtualMachineTypesFromCluster(AmazonEC2Client amazonClient, XMLReader configurationFile) {
        ArrayList<VirtualMachineType> vmTypes = new ArrayList<VirtualMachineType>();
        
        DescribeInstancesResult superNodes = getDescribeSuperNodesFromCluster(amazonClient, configurationFile.clusterName);
        for(Reservation reservation : superNodes.getReservations()){
            for(Instance instance : reservation.getInstances()){
                VirtualMachineType vmType = SCUtils.getVmType(configurationFile, instance.getInstanceType());
                if(!hasVMType(vmTypes, vmType)){
                    vmType.setAmountInstantiatedVM(1);
                    vmTypes.add(vmType);
                }else{
                    VirtualMachineType virtualMachine = getVmType(vmTypes, vmType);
                    virtualMachine.addAmountInstantiatedVM();
                }
            }
        }
        
        DescribeInstancesResult nodes = getDescribeNodesFromCluster(amazonClient, configurationFile.clusterName);
        for(Reservation reservation : nodes.getReservations()){
            for(Instance instance : reservation.getInstances()){
                VirtualMachineType vmType = SCUtils.getVmType(configurationFile, instance.getInstanceType());
                if(!hasVMType(vmTypes, vmType)){
                    vmType.setAmountInstantiatedVM(1);
                    vmTypes.add(vmType);
                }else{
                    VirtualMachineType virtualMachine = getVmType(vmTypes, vmType);
                    virtualMachine.addAmountInstantiatedVM();
                }
            }
        }
        
        return vmTypes;
    }
    
    static void checkSuperNode(AmazonProvider amazon, AmazonEC2Client amazonClient, XMLReader config) {
        boolean hasSuperNode = hasSuperNodeFromCluster(amazonClient, config.clusterName);
        if(!hasSuperNode){
            updateAliveMachineToBeSuperNode(amazonClient, config);
        }
    }
    
    public static void updateAliveMachineToBeSuperNode(AmazonEC2Client amazonClient, XMLReader configurationFile) {
        DescribeInstancesResult result = getDescribeNodesFromCluster(amazonClient, configurationFile.clusterName);
        
        for(Reservation reservation : result.getReservations()){
            for(Instance instance : reservation.getInstances()){
                List<Tag> tags = instance.getTags();
                for(Tag tag : tags){
                    if(tag.getKey().equals(clusterNodeType)){
                        DeleteTagsRequest dtr = new DeleteTagsRequest();
                        dtr.withResources(instance.getInstanceId());
                        dtr.withTags(new Tag(clusterNodeType));
                        amazonClient.deleteTags(dtr);
                        
                        CreateTagsRequest ctr = new CreateTagsRequest();
                        ctr.withResources(instance.getInstanceId());
                        Tag nodeTypeTag = new Tag(clusterNodeType, "SUPERNODE");
                        ctr.withTags(nodeTypeTag);
                        amazonClient.createTags(ctr);
                        return;
                    }
                }
            }
        }
    }

    private static boolean hasSuperNodeFromCluster(AmazonEC2Client amazonClient, String clusterName) {
        DescribeInstancesResult diResult = getDescribeSuperNodesFromCluster(amazonClient, clusterName);
        
        List<Reservation> reservations = diResult.getReservations();
        if(!reservations.isEmpty()){
            for (Reservation reservation : reservations) {
                List<Instance> instances = reservation.getInstances();
                if(!instances.isEmpty()){
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public static DescribeInstancesResult getNotFinishedInstancesFromCluster(AmazonEC2Client amazonClient, String clusterName){
        List<Filter> filters = new ArrayList<Filter>();
        createFilter(filters, instanceStateNameLabel, instanceNotFinishedStates);
        
        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag(clusterLabelName, getVirtualMachinesName(clusterName)));
        
        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
        
        return amazonClient.describeInstances(iRequest);
    }

    public static boolean hasFinishedInstancesFromCluster(AmazonEC2Client amazonClient, String clusterName) {
        DescribeInstancesResult result = getNotFinishedInstancesFromCluster(amazonClient, clusterName);
        for (Reservation reservation : result.getReservations()) {
            int instances = reservation.getInstances().size();
            if(instances > 0){
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasVMType(ArrayList<VirtualMachineType> vmTypes, VirtualMachineType vmType) {
        for(VirtualMachineType machine : vmTypes){
            if(machine.getType().equals(vmType.getType())){
                return true;
            }
        }
        
        return false;
    }
    
    private static VirtualMachineType getVmType(ArrayList<VirtualMachineType> vmTypes, VirtualMachineType vmType) {
        for(VirtualMachineType machine : vmTypes){
            if(machine.getType().equals(vmType.getType())){
                return machine;
            }
        }
        
        return null;
    }
    
    private static VirtualMachineType getVmType(XMLReader configurationFile, String instanceType) {
        for(VirtualMachineType type : configurationFile.vmTypes){
            if(type.getType().equals(instanceType)){
                VirtualMachineType retrievedType = new VirtualMachineType(type.getFinancialCost(), 
                        type.getDiskSpace(), type.getRam(), type.getGflops(), type.getPlatform(), type.getNumberOfCores());
                retrievedType.setType(instanceType);
                return retrievedType;
            }
        }
        
        return null;
    }
    
    public static String getArgumentForConceptualWkfOper(ConceptualWkfOperation operation){
        if(operation == ConceptualWkfOperation.INSERT){
            return "-i";
        }else if(operation == ConceptualWkfOperation.DELETE){
            return "-d";
        }
        
        return null;
    }
    
    public static String[] splitAndDiscard(String s, String regex){
        String[] ret = s.split(regex);
        String aux = new String("");
        for(int i = 0; i< ret.length; i++){
            if(!(ret[i].matches(""))){
                aux+=ret[i]+regex;
            }
        }
        ret = aux.split(regex);
        return ret;
    }

    public static String[] splitAndDiscard(String s, String regex1, String regex2){
        String[] ret = s.split(regex1);
        String aux = new String("");
        for(int i = 0; i< ret.length; i++){
            if(!(ret[i].matches(""))){
                if(!(ret[i].matches(regex2))){
                    aux+=ret[i]+regex1;
                }
            }
        }
        ret = aux.split(regex1);
        return ret;
    }

    public static boolean MachineListContains(String name, ArrayList<AmazonMachine> machines){
        for(int i=0; i<machines.size(); i++){
            if(machines.get(i).getName().matches(name)) return true;
        }
        return false;
    }

    public static boolean machineListIsAllReady(ArrayList<AmazonMachine> machines){
        for(int i=0; i<machines.size(); i++){
            AmazonMachine mac = machines.get(i);
            if(mac.getPublicDNS().matches("pending")) return false;
            if(mac.getPublicDNS() == null) return false;
            if(mac.getPublicDNS().matches("")) return false;
        }
        return true;
    }

    public static AmazonMachine getMachineByName(ArrayList<AmazonMachine> machines, String name){
        AmazonMachine mach=null;
        for(int i=0; i<machines.size(); i++){
            if(!(machines.get(i).getName().matches(name)))mach = machines.get(i);
        }
        return mach;
    }

    public static void printStringArray(String[] array){
        for(int i=0; i<array.length; i++){
            System.out.print(array[i]+ " ");
        }
        System.out.println("");
    }

    public static boolean folderContains(String dirName, String fileName) {

        File folder = new File(dirName);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].getName().matches(fileName)) return true;
        }
        return false;
    }
    
    public static void deleteFile(String fileName, String fileDir) throws IOException, InterruptedException {
        //Código que deleta um arquivo após a execução da tarefa.
        File file = new File(fileDir + "/" + fileName);
        file.delete();
    }
    
    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }
    
    public static boolean isMacOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("mac") >= 0);
    }
    
    public static Session executeCommand(Connection conn, String commandLine) throws IOException{
        Session sess = conn.openSession();
        sess.execCommand(commandLine);
        return sess;
    }
    
    public static void printSessionResult(Session sess){
        printSecondLevel("Output:");
        printInputStream(sess.getStdout());
        printSecondLevel("Error:");
        printInputStream(sess.getStderr());
    }
    
    public static void printInputStream(InputStream inputStream){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                printSecondLevel(inputLine);
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(SCUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void sendDataBySCP(Connection conn, String fileName, String remoteMountPoint) throws IOException {
        SCPClient scp = new SCPClient(conn);
        scp.put(fileName, remoteMountPoint);
    }
    
    public static void getDataBySCP(Connection conn, String filePath, String localPoint) throws IOException {
        SCPClient scp = new SCPClient(conn);
        scp.get(filePath, localPoint);
    }
    
    public static MachineComparison compareInstanciatedMachines(ArrayList<VirtualMachineType> instanciatedMachines, 
            List<VirtualMachineType> newConfMachines) {
        
        int totalVM = 0;
        for(VirtualMachineType newMac : newConfMachines){
            totalVM += newMac.getAmountInstantiatedVM();
        }
        
        MachineComparison comparison = new MachineComparison();
        
        if(totalVM!=0){
            comparison.setEqualToInstanciatedMachines(true);

            for(VirtualMachineType newVMType : newConfMachines){
                VirtualMachineType instVMType = getMachineType(instanciatedMachines, newVMType);

                if(instVMType==null && newVMType.getAmountInstantiatedVM()!=0){
                    comparison.setEqualToInstanciatedMachines(false);

                    VirtualMachineType vmType = new VirtualMachineType(newVMType.getFinancialCost(), 
                            newVMType.getDiskSpace(), newVMType.getRam(), newVMType.getGflops(), 
                            newVMType.getPlatform(), newVMType.getNumberOfCores());
                    vmType.setType(newVMType.getType());
                    vmType.setAmountInstantiatedVM(newVMType.getAmountInstantiatedVM());
                    comparison.addMachinesToAllocate(newVMType);
                }else if(instVMType!=null){
                    int diffMachines = newVMType.getAmountInstantiatedVM() - instVMType.getAmountInstantiatedVM();
                    if(diffMachines > 0){
                        comparison.setEqualToInstanciatedMachines(false);

                        VirtualMachineType vmType = new VirtualMachineType(newVMType.getFinancialCost(), 
                                newVMType.getDiskSpace(), newVMType.getRam(), newVMType.getGflops(), 
                                newVMType.getPlatform(), newVMType.getNumberOfCores());
                        vmType.setType(newVMType.getType());
                        vmType.setAmountInstantiatedVM(diffMachines);
                        comparison.addMachinesToAllocate(vmType);
                    }else if(diffMachines < 0){
                        comparison.setEqualToInstanciatedMachines(false);
                        VirtualMachineType vmType = new VirtualMachineType(newVMType.getFinancialCost(), 
                                newVMType.getDiskSpace(), newVMType.getRam(), newVMType.getGflops(), 
                                newVMType.getPlatform(), newVMType.getNumberOfCores());
                        vmType.setType(newVMType.getType());
                        vmType.setAmountInstantiatedVM(Math.abs(diffMachines));
                        comparison.addMachinesToDeallocate(vmType);
                    }
                }
            }

            for(VirtualMachineType instVMType : instanciatedMachines){
                VirtualMachineType newVMType = getMachineType(newConfMachines, instVMType);
                if(newVMType == null){
                    comparison.setEqualToInstanciatedMachines(false);

                    VirtualMachineType vmType = new VirtualMachineType(instVMType.getFinancialCost(), 
                            instVMType.getDiskSpace(), instVMType.getRam(), instVMType.getGflops(), 
                            instVMType.getPlatform(), instVMType.getNumberOfCores());
                    vmType.setType(instVMType.getType());
                    vmType.setAmountInstantiatedVM(instVMType.getAmountInstantiatedVM());
                    comparison.addMachinesToDeallocate(instVMType);
                }
            }
        }
        
        return comparison;
    }
    
    private static VirtualMachineType getMachineType(List<VirtualMachineType> machinesType, VirtualMachineType vmType){
        for(VirtualMachineType iVMType : machinesType){
            if(iVMType.getType().equals(vmType.getType())){
                return iVMType;
            }
        }
        
        return null;
    }
    
    public static String getVirtualMachinesName(String clusterName){
        return SCUtils.clusterHeaderName + clusterName;
    }
    
    public static String getKeyPairName(String clusterName){
        return SCUtils.keyHeaderName + clusterName;
    }
    
    public static String getSecurityGroupName(String clusterName){
        return SCUtils.securityGroupHeaderName + clusterName;
    }

    public static List<String> getSecurityGroupList(String clusterName) {
        List<String> securityGroups = new ArrayList<String>();
        securityGroups.add(SCUtils.securityGroupHeaderName + clusterName);
        return securityGroups;
    }
    
    protected static Filter getFilterFromTag(String tag, String value) {
            Filter filter = new Filter();
            filter.setName("tag:" + tag);
            filter.setValues(Collections.singletonList(value));
            return filter;
    }
    
    public static VirtualMachineType getVmType(List<VirtualMachineType> vmTypes, String instanceType) {
        for(VirtualMachineType type : vmTypes){
            if(type.getType().equals(instanceType)){
                VirtualMachineType retrievedType = new VirtualMachineType(type.getFinancialCost(), 
                        type.getDiskSpace(), type.getRam(), type.getGflops(), type.getPlatform(), type.getNumberOfCores());
                retrievedType.setType(instanceType);
                return retrievedType;
            }
        }
        
        return null;
    }
    
    public static void executeRemoteCommand(Connection conn, String commandLine) throws IOException{
        Session sess = conn.openSession();
        sess.execCommand(commandLine);
    }
    
//    public static DescribeInstancesResult getInstanceVM(AmazonEC2Client amazonClient, String machinePublicDNS){
//        List<Filter> filters = new ArrayList<Filter>();
//        createFilter(filters, instanceStateNameLabel, runningInstanceStates);
//        
//        ArrayList<Tag> tags = new ArrayList<Tag>();
//        
//        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
//        
//        return amazonClient.describeInstances(iRequest);
//    }
//    
//    public static boolean machineHasConnection(AmazonEC2Client amazonClient, AmazonMachine machine) {
//        DescribeInstancesResult result = getInstanceVM(amazonClient, machine.publicDNS);
//        
//        for(Reservation reservation : result.getReservations()){
//            for(Instance instance : reservation.getInstances()){
//                SCUtils.printSecondLevel(instance.getPublicDnsName());
//                return true;
//            }
//        }
//        
//        return false;
//    }
    
    public static int runCommand(String cmd, String dir) throws IOException, InterruptedException {
        Runtime run = Runtime.getRuntime();
        int result = 0;
        String command[] = null;
        if (SCUtils.isWindows()) {
            String cmdWin[] = {"cmd.exe", "/c", cmd};
            command = cmdWin;
        } else {
            String cmdLinux = cmd;
            if (cmd.contains(">")) {
                cmdLinux = cmd.replace(">", ">>");
            }
            String cmdLin[] = {"/bin/bash", "-c", cmdLinux};
            command = cmdLin;
        }
        
        Process pr = null;
        if (dir == null) {
            pr = run.exec(command);
        } else {
            pr = run.exec(command, null, new File(dir));
        }
        
        pr.waitFor();
        result = pr.exitValue();
        pr.destroy();
        
        return result;
    }
}
