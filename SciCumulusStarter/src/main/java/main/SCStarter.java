package main;

import com.amazonaws.services.ec2.AmazonEC2Client;
import enumeration.ConceptualWkfOperation;
import enumeration.OperationType;
import environment.AmazonProvider;

//http://docs.aws.amazon.com/cli/latest/reference/ec2/describe-instances.html

/**
 *
 * @author Daniel
 */
public class SCStarter {

    private static String absolutePath;
    private static AmazonProvider amazon;
    private static AmazonEC2Client amazonClient;
    private static XMLReader configurationFile = null;

    private static OperationType getOperation(String operation, String[] args) {
        if(args.length == 2){
            switch(operation){
                case "cc":
                    return OperationType.CREATE_CLUSTER;
                case "create_cluster":
                    return OperationType.CREATE_CLUSTER;
                case "dc":
                    return OperationType.DELETE_CLUSTER;
                case "delete_cluster":
                    return OperationType.DELETE_CLUSTER;
                case "icw":
                    return OperationType.INSERT_CONCEPTUAL_WORKFLOW;
                case "insert_conceptual_workflow":
                    return OperationType.INSERT_CONCEPTUAL_WORKFLOW;
                case "ucw":
                    return OperationType.UPDATE_CONCEPTUAL_WORKFLOW;
                case "update_conceptual_workflow":
                    return OperationType.UPDATE_CONCEPTUAL_WORKFLOW;
                case "dcw":
                    return OperationType.DELETE_CONCEPTUAL_WORKFLOW;
                case "delete_conceptual_workflow":
                    return OperationType.DELETE_CONCEPTUAL_WORKFLOW;
                case "sew":
                    return OperationType.SUBMIT_EXECUTION_WORKFLOW;
                case "submit_execution_workflow":
                    return OperationType.SUBMIT_EXECUTION_WORKFLOW;
                case "rew":
                    return OperationType.RUN_EXECUTION_WORKFLOW;
                case "run_execution_workflow":
                    return OperationType.RUN_EXECUTION_WORKFLOW;
                case "dew":
                    return OperationType.DELETE_EXECUTION_WORKFLOW;
                case "delete_execution_workflow":
                    return OperationType.DELETE_EXECUTION_WORKFLOW;
                case "q":
                    return OperationType.RUN_QUERY;
                case "run_query":
                    return OperationType.RUN_QUERY;
                case "mew":
                    return OperationType.MONITOR_EXECUTION_WORKFLOW;
                case "monitor_execution_workflow":
                    return OperationType.MONITOR_EXECUTION_WORKFLOW;
                case "amf":
                    return OperationType.ABORT_MOUNTED_FOLDERS;
                case "abort_mounted_folders":
                    return OperationType.ABORT_MOUNTED_FOLDERS;            
                case "d":
                    return OperationType.DEBUG;
            }
        }
        
        return OperationType.UNKNOWN;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        SCUtils.printInitialMessage();
        
        if(args.length == 2){
            String operation = args[0].replaceAll("-", "");
            OperationType operationType = getOperation(operation, args);

            if(operationType != OperationType.UNKNOWN){
                absolutePath = System.getProperty("user.dir");
                configurationFile = new XMLReader(args[1]);
                amazon = new AmazonProvider(configurationFile.awsAccessKeyId, configurationFile.awsSecretAccessKey);
                amazonClient = amazon.startProvider();

                if(operationType != OperationType.CREATE_CLUSTER 
                        && operationType != OperationType.DELETE_CLUSTER){
                    amazon.startDatabaseConnection(configurationFile);
                }
            }

            if(operationType == OperationType.CREATE_CLUSTER){
                SCUtils.printOperation("Create Cluster");
                amazon.createCluster(amazonClient, configurationFile, absolutePath);

            }else if(operationType == OperationType.DELETE_CLUSTER){
                SCUtils.printOperation("Delete Cluster");
                amazon.deleteCluster(amazonClient, configurationFile);

            }else if(operationType == OperationType.INSERT_CONCEPTUAL_WORKFLOW){
                SCUtils.printOperation("Insert Conceptual Workflow");
                SCInvocation.executeSciCumulusSetup(amazonClient, ConceptualWkfOperation.INSERT, configurationFile);

            }else if(operationType == OperationType.UPDATE_CONCEPTUAL_WORKFLOW){
                SCUtils.printOperation("Update Conceptual Workflow");
                SCInvocation.executeSciCumulusSetup(amazonClient, ConceptualWkfOperation.UPDATE, configurationFile);

            }else if(operationType == OperationType.DELETE_CONCEPTUAL_WORKFLOW){
                SCUtils.printOperation("Delete Conceptual Workflow");
                SCInvocation.executeSciCumulusSetup(amazonClient, ConceptualWkfOperation.DELETE, configurationFile);

            }else if(operationType == OperationType.SUBMIT_EXECUTION_WORKFLOW){
                SCUtils.printOperation("Submit Execution Workflow");
                SCInvocation.submitAdaptiveDaemon(amazon, amazonClient, configurationFile);

            }else if(operationType == OperationType.RUN_EXECUTION_WORKFLOW){
                SCUtils.printOperation("Run Execution Workflow");
                SCInvocation.executeSciCumulusCoreAdaptively(amazon, amazonClient, configurationFile);

            }else if(operationType == OperationType.DELETE_EXECUTION_WORKFLOW){
                SCUtils.printOperation("Delete Execution Workflow");
                SCInvocation.deleteExecutionWorkflow(configurationFile);

            }else if(operationType == OperationType.RUN_QUERY){
                SCUtils.printOperation("Run query");
                SCInvocation.runQuery(configurationFile);

            }else if(operationType == OperationType.MONITOR_EXECUTION_WORKFLOW){
                SCUtils.printOperation("Monitor workflow execution");
                SCInvocation.monitorExecutionWorkflow(configurationFile);
            }else if(operationType == OperationType.ABORT_MOUNTED_FOLDERS){
                SCUtils.printOperation("Abort Mount Folders in Core Instances");
                SCInvocation.abortMountedFoldersInCoreInstances(amazonClient, configurationFile);
            }else{
                System.out.println("You did not specify operation or configuration file correctly!");
            }

            if(amazon!=null){
                amazon.shutDownProvider(amazonClient);
            }
        }else{
            System.out.println("You did not specify arguments correctly! (operation + configuration file)");
        }
    }
}