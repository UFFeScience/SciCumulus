package chiron.cloud;

import chiron.EMachine;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import chiron.XMLReader;
import com.amazonaws.services.ec2.model.Tag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author vitor
 */
public class CloudUtils {
    
    public static final String JDBC_DRIVER = "org.postgresql.Driver";
    public static final String clusterLabelName = "Name";
    public static final String clusterNodeType = "NodeType";
    public static final String clusterHeaderName = "SC-";
    public static final String keyHeaderName = clusterHeaderName + "Key-";
    public static final String securityGroupHeaderName = clusterHeaderName + "SG-";
    public static final String instanceStateNameLabel = "instance-state-name";
    public static final List<String> runningInstanceStates = new ArrayList<String>(Arrays.asList("running","pending","shutting-down","stopping"));
    public static final String instanceTypeNameLabel = "instance-type";
    public static final String securityGroupNameLabel = "group-name";
    public static final String keyNameLabel = "key-name";
    public static final String instanceNameLabel = "tag-value";
    public static final String networkInterfaceAttachmentStatus = "network-interface.attachment.status";
    public static final String networkInterfaceStatus = "network-interface.status";
    private static final String identation = "        ";
    
    public static void printInitialMessage(){
        System.out.println("####################################################");
        System.out.println("################ SciCumulus Starter ################");
        System.out.println("####################################################");
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
    
    public static List<Filter> createFilter(List<Filter> filters, String key, List<String> values){
        Filter newFilter = new Filter(key);
        newFilter.setValues(values);
        filters.add(newFilter);
        
        return filters;
    }

//    public static boolean hasAliveInstanceFromCluster(AmazonEC2Client amazonClient, String clusterName) {
//        DescribeInstancesResult result = getAliveInstancesFromCluster(amazonClient, clusterName);
//        
//        for (Reservation reservation : result.getReservations()) {
//            int instances = reservation.getInstances().size();
//            if(instances > 0){
//                return true;
//            }
//        }
//        
//        return false;
//    }
//    
//    public static DescribeSecurityGroupsResult getAliveSecurityGroupFromACluster(AmazonEC2Client amazonClient, XMLReader configurationFile){
//        DescribeSecurityGroupsRequest iRequest = new DescribeSecurityGroupsRequest();
//        List<Filter> filter = new ArrayList<Filter>();
//
//        List<String> values = new ArrayList<String>();
//        values.add(getSecurityGroupName(configurationFile.clusterName));
//        createFilter(filter, securityGroupNameLabel, values);
//        iRequest.setFilters(filter);
//        
//        return amazonClient.describeSecurityGroups(iRequest);
//    }
//    
//    public static boolean hasAliveSecurityGroupFromCluster(AmazonEC2Client amazonClient, XMLReader configurationFile) {
//        DescribeSecurityGroupsResult result = getAliveSecurityGroupFromACluster(amazonClient, configurationFile);
//        return (result.getSecurityGroups().size()>0);
//    }
//    
//    public static DescribeKeyPairsResult getAliveKeyPairFromACluster(AmazonEC2Client amazonClient, XMLReader configurationFile){
//        DescribeKeyPairsRequest iRequest = new DescribeKeyPairsRequest();
//        List<Filter> filter = new ArrayList<Filter>();
//
//        List<String> values = new ArrayList<String>();
//        values.add(getKeyPairName(configurationFile.clusterName));
//        createFilter(filter, keyNameLabel, values);
//        iRequest.setFilters(filter);
//        
//        return amazonClient.describeKeyPairs(iRequest);
//    }
//    
//    public static boolean hasAliveKeyPairFromCluster(AmazonEC2Client amazonClient, XMLReader configurationFile) {
//        DescribeKeyPairsResult result = getAliveKeyPairFromACluster(amazonClient, configurationFile);
//        return (result.getKeyPairs().size()>0);
//    }
//    
//    public static DescribeInstancesResult getAliveInstancesFromCluster(AmazonEC2Client amazonClient, String clusterName){
//        List<Filter> filters = new ArrayList<Filter>();
//        createFilter(filters, instanceStateNameLabel, runningInstanceStates);
//        
//        ArrayList<Tag> tags = new ArrayList<Tag>();
//        tags.add(new Tag(clusterLabelName, getVirtualMachinesName(clusterName)));
//        
//        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
//        
//        return amazonClient.describeInstances(iRequest);
//    }
//    
//    public static DescribeInstancesResult getAliveInstancesFromClusterByType(AmazonEC2Client amazonClient, String clusterName, VirtualMachineType vmType){
//        List<Filter> filters = new ArrayList<Filter>();
//        createFilter(filters, instanceStateNameLabel, runningInstanceStates);
//        
//        List<String> values = new ArrayList<String>();
//        values.add(vmType.getType());
//        createFilter(filters, instanceTypeNameLabel, values);
//        
//        ArrayList<Tag> tags = new ArrayList<Tag>();
//        tags.add(new Tag(clusterLabelName, getVirtualMachinesName(clusterName)));
//        
//        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
//        
//        return amazonClient.describeInstances(iRequest);
//    }
//    
//    public static DescribeInstancesResult getDescribeMachinesFromCluster(AmazonEC2Client amazonClient, String clusterName){
//        ArrayList<Tag> tags = new ArrayList<Tag>();
//        
//        List<Filter> filters = new ArrayList<Filter>();
//        createFilter(filters, instanceStateNameLabel, runningInstanceStates);
//        
//        tags.add(new Tag(clusterLabelName, getVirtualMachinesName(clusterName)));
//        
//        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
//        
//        return amazonClient.describeInstances(iRequest);
//    }
    
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
    
    public static DescribeInstancesResult getNodesFromCluster(AmazonEC2Client amazonClient, String clusterName){
        List<Filter> filters = new ArrayList<Filter>();
        createFilter(filters, instanceStateNameLabel, runningInstanceStates);
        
        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag(clusterLabelName, getVirtualMachinesName(clusterName)));
        tags.add(new Tag(clusterNodeType, "NODE"));
        
        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
        
        return amazonClient.describeInstances(iRequest);
    }

    public static ArrayList<EMachine> getMachinesFromCluster(AmazonEC2Client amazonClient, XMLReader configurationFile) {
        int rank = 0;
        ArrayList<EMachine> machines = new ArrayList<EMachine>();
        
        DescribeInstancesResult superNode = getDescribeSuperNodeFromCluster(amazonClient, configurationFile.clusterName);
        for(Reservation reservation : superNode.getReservations()){
            for(Instance instance : reservation.getInstances()){
                VirtualMachineType vmType = CloudUtils.getVmType(configurationFile, instance.getInstanceType());
                EMachine mac = new EMachine(rank, 
                        instance.getPublicDnsName(), 
                        instance.getPublicIpAddress(),
                        instance.getPrivateIpAddress(),
                        vmType.getType(),
                        vmType.getNumberOfCores());
                machines.add(mac);
                rank++;
            }
        }
        
        DescribeInstancesResult nodes = getNodesFromCluster(amazonClient, configurationFile.clusterName);
        for(Reservation reservation : nodes.getReservations()){
            for(Instance instance : reservation.getInstances()){
                VirtualMachineType vmType = CloudUtils.getVmType(configurationFile, instance.getInstanceType());
                EMachine mac = new EMachine(rank, 
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
    
//    public static boolean hasVMType(ArrayList<VirtualMachineType> vmTypes, VirtualMachineType vmType) {
//        for(VirtualMachineType machine : vmTypes){
//            if(machine.getType().equals(vmType.getType())){
//                return true;
//            }
//        }
//        
//        return false;
//    }
//    
//    public static VirtualMachineType getVmType(ArrayList<VirtualMachineType> vmTypes, VirtualMachineType vmType) {
//        for(VirtualMachineType machine : vmTypes){
//            if(machine.getType().equals(vmType.getType())){
//                return machine;
//            }
//        }
//        
//        return null;
//    }
//    
//    public static VirtualMachineType getVmType(List<VirtualMachineType> vmTypes, String instanceType) {
//        for(VirtualMachineType type : vmTypes){
//            if(type.getType().equals(instanceType)){
//                VirtualMachineType retrievedType = new VirtualMachineType(type.getFinancialCost(), 
//                        type.getDiskSpace(), type.getRam(), type.getGflops(), type.getPlatform(), type.getNumberOfCores());
//                retrievedType.setType(instanceType);
//                return retrievedType;
//            }
//        }
//        
//        return null;
//    }
//    
//    public static String[] splitAndDiscard(String s, String regex){
//        String[] ret = s.split(regex);
//        String aux = new String("");
//        for(int i = 0; i< ret.length; i++){
//            if(!(ret[i].matches(""))){
//                aux+=ret[i]+regex;
//            }
//        }
//        ret = aux.split(regex);
//        return ret;
//    }
//
//    public static String[] splitAndDiscard(String s, String regex1, String regex2){
//        String[] ret = s.split(regex1);
//        String aux = new String("");
//        for(int i = 0; i< ret.length; i++){
//            if(!(ret[i].matches(""))){
//                if(!(ret[i].matches(regex2))){
//                    aux+=ret[i]+regex1;
//                }
//            }
//        }
//        ret = aux.split(regex1);
//        return ret;
//    }
//
//    public static boolean MachineListContains(String name, ArrayList<EMachine> machines){
//        for(int i=0; i<machines.size(); i++){
//            if(machines.get(i).name.matches(name)) return true;
//        }
//        return false;
//    }
//
//    public static boolean machineListIsAllReady(ArrayList<EMachine> machines){
//        for(int i=0; i<machines.size(); i++){
//            EMachine mac = machines.get(i);
//            if(mac.publicDNS.matches("pending")) return false;
//            if(mac.publicDNS == null) return false;
//            if(mac.publicDNS.matches("")) return false;
//        }
//        return true;
//    }
//
//    public static EMachine getMachineByName(ArrayList<EMachine> machines, String name){
//        EMachine mach=null;
//        for(int i=0; i<machines.size(); i++){
//            if(!(machines.get(i).name.matches(name)))mach = machines.get(i);
//        }
//        return mach;
//    }
//    
//    public static ArrayList<VirtualMachineType> getVirtualMachineTypesFromCluster(EnvironmentConfiguration environment) {
//        DescribeInstancesResult result = getAliveInstancesFromCluster(environment.amazonClient, environment.getClusterName());
//        ArrayList<VirtualMachineType> vmTypes = new ArrayList<VirtualMachineType>();
//       
//        for(Reservation reservation : result.getReservations()){
//            for(Instance instance : reservation.getInstances()){
//                VirtualMachineType vmType = CloudUtils.getVmType(environment.getVMTypes(), instance.getInstanceType());
//                if(!hasVMType(vmTypes, vmType)){
//                    vmType.setAmountInstantiatedVM(1);
//                    vmTypes.add(vmType);
//                }else{
//                    VirtualMachineType virtualMachine = getVmType(vmTypes, vmType);
//                    virtualMachine.addAmountInstantiatedVM();
//                }
//            }
//        }
//        
//        return vmTypes;
//    }
//    
//    public static MachineComparison compareInstanciatedMachines(ArrayList<VirtualMachineType> instanciatedMachines, List<VirtualMachineType> newConfMachines) {
//        MachineComparison comparison = new MachineComparison();
//        comparison.setEqualToInstanciatedMachines(true);
//        
//        for(VirtualMachineType newVMType : newConfMachines){
//            VirtualMachineType instVMType = getMachineType(instanciatedMachines, newVMType);
//            
//            if(instVMType==null && newVMType.getAmountInstantiatedVM()!=0){
//                comparison.setEqualToInstanciatedMachines(false);
//                
//                VirtualMachineType vmType = new VirtualMachineType(newVMType.getFinancialCost(), 
//                        newVMType.getDiskSpace(), newVMType.getRam(), newVMType.getGflops(), 
//                        newVMType.getPlatform(), newVMType.getNumberOfCores());
//                vmType.setType(newVMType.getType());
//                vmType.setAmountInstantiatedVM(newVMType.getAmountInstantiatedVM());
//                comparison.addMachinesToAllocate(newVMType);
//            }else if(instVMType!=null){
//                int diffMachines = newVMType.getAmountInstantiatedVM() - instVMType.getAmountInstantiatedVM();
//                if(diffMachines > 0){
//                    comparison.setEqualToInstanciatedMachines(false);
//                    
//                    VirtualMachineType vmType = new VirtualMachineType(newVMType.getFinancialCost(), 
//                            newVMType.getDiskSpace(), newVMType.getRam(), newVMType.getGflops(), 
//                            newVMType.getPlatform(), newVMType.getNumberOfCores());
//                    vmType.setType(newVMType.getType());
//                    vmType.setAmountInstantiatedVM(diffMachines);
//                    comparison.addMachinesToAllocate(vmType);
//                }else if(diffMachines < 0){
//                    comparison.setEqualToInstanciatedMachines(false);
//                    VirtualMachineType vmType = new VirtualMachineType(newVMType.getFinancialCost(), 
//                            newVMType.getDiskSpace(), newVMType.getRam(), newVMType.getGflops(), 
//                            newVMType.getPlatform(), newVMType.getNumberOfCores());
//                    vmType.setType(newVMType.getType());
//                    vmType.setAmountInstantiatedVM(Math.abs(diffMachines));
//                    comparison.addMachinesToDeallocate(vmType);
//                }
//            }
//        }
//        
//        for(VirtualMachineType instVMType : instanciatedMachines){
//            VirtualMachineType newVMType = getMachineType(newConfMachines, instVMType);
//            if(newVMType == null){
//                comparison.setEqualToInstanciatedMachines(false);
//                
//                VirtualMachineType vmType = new VirtualMachineType(instVMType.getFinancialCost(), 
//                        instVMType.getDiskSpace(), instVMType.getRam(), instVMType.getGflops(), 
//                        instVMType.getPlatform(), instVMType.getNumberOfCores());
//                vmType.setType(instVMType.getType());
//                vmType.setAmountInstantiatedVM(instVMType.getAmountInstantiatedVM());
//                comparison.addMachinesToDeallocate(instVMType);
//            }
//        }
//        
//        return comparison;
//    }
//    
//    private static VirtualMachineType getMachineType(List<VirtualMachineType> machinesType, VirtualMachineType vmType){
//        for(VirtualMachineType iVMType : machinesType){
//            if(iVMType.getType().equals(vmType.getType())){
//                return iVMType;
//            }
//        }
//        
//        return null;
//    }
    
    public static String getVirtualMachinesName(String clusterName){
        return CloudUtils.clusterHeaderName + clusterName;
    }
    
//    public static String getKeyPairName(String clusterName){
//        return CloudUtils.keyHeaderName + clusterName;
//    }
//    
//    public static String getSecurityGroupName(String clusterName){
//        return CloudUtils.securityGroupHeaderName + clusterName;
//    }
//
//    public static List<String> getSecurityGroupList(String clusterName) {
//        List<String> securityGroups = new ArrayList<String>();
//        securityGroups.add(CloudUtils.securityGroupHeaderName + clusterName);
//        return securityGroups;
//    }
    
    public static DescribeInstancesResult getDescribeSuperNodeFromCluster(AmazonEC2Client amazonClient, String clusterName){
        ArrayList<Tag> tags = new ArrayList<Tag>();
        
        List<Filter> filters = new ArrayList<Filter>();
        createFilter(filters, instanceStateNameLabel, runningInstanceStates);
        
        tags.add(new Tag(clusterLabelName, getVirtualMachinesName(clusterName)));
        tags.add(new Tag(clusterNodeType, "SUPERNODE"));
        
        DescribeInstancesRequest iRequest = getDescribeInstancesRequest(tags, filters);
        
        return amazonClient.describeInstances(iRequest);
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
    
    protected static Filter getFilterFromTag(String tag, String value) {
            Filter filter = new Filter();
            filter.setName("tag:" + tag);
            filter.setValues(Collections.singletonList(value));
            return filter;
    }
}
