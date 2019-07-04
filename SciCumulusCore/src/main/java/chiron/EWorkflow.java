package chiron;

import chiron.concept.CActivity;
import chiron.concept.CRelation;
import chiron.concept.Operator;
import chiron.environment.EnvironmentConfiguration;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import mpi.MPI;

/**
 * The EWorkflow is responsible to store the execution data of the workflow and
 * to control the execution following a given execution model.
 *
 * @author Eduardo, Vítor
 * @since 2010-12-25
 */
public class EWorkflow implements Serializable {

    public enum ExecModel {

        STA_FAF, DYN_FAF, STA_FTF, DYN_FTF
    }
    public ExecModel model = ExecModel.DYN_FAF;
    public String tag;
    public Integer wkfId = null;
    public String exeTag;
    public String expDir;
    public String wfDir;
    public ArrayList<EActivity> activities = new ArrayList<EActivity>();
    public ArrayList<EActivity> runnableActivities = new ArrayList<EActivity>();
    public ArrayList<CRelation> inputRelations = new ArrayList<CRelation>();
    //    failure_handling
    public int maximumFailures;
    public boolean userInteraction;
    public boolean redundancy;
    public Double reliability;
    //    environment configuration
    public EnvironmentConfiguration environment;

    /**
     * Construtor de um workflow
     */
    public EWorkflow() {
    }

    /**
     * Realiza a interação de execução de cada atividade do workflow
     *
     * @return
     */
    private void addPipelineActivations(ArrayList<EActivation> resultList, EActivity activity, EActivation activation) throws SQLException, IOException {
        EActivity dep = activity.pipeline;
        EActivity act = activity;
        while (dep != null) {
            //activity already executed, 
            EActivation depActivation = dep.operation.createPipelineActivation(expDir, wfDir, act, dep, activation);
            EProvenance.storeActivation(depActivation);
            resultList.add(depActivation);
            depActivation.pipelinedFrom = activation;
            act = dep;
            dep = dep.pipeline;
            activation = depActivation;
        }
    }
    
    public void initMPI(EBody body, String[] args) {
        MPI.Init(args);
        System.out.println("finish mpi");
        body.MPI_size = MPI.COMM_WORLD.Size();
        body.MPI_rank = MPI.COMM_WORLD.Rank();
    }

    public EActivation[] iterateExecution(EBody body) throws Exception {
        ArrayList<EActivation> resultList = new ArrayList<EActivation>();
        boolean returnWait = false;

        int i = 0;
        while ((runnableActivities.size() > 0) && (i < runnableActivities.size())) {
            EActivity act = runnableActivities.get(i);
            if (act.status == EActivity.StatusType.READY) {
                act.startTime = new Date();
                act.operation.generateActivations(act, this.wfDir, this.expDir);
                
//                if(environment.type == EnvironmentType.CLOUD_AMAZON){
//                    executeAdaptiveAlgorithm(act, body);
//                }else{
//                    ArrayList<EMachine> machines = ChironUtils.getMachines("/Users/vitor/Documents/Repository/SciCumulus/SciCumulus-Vitor/workflows/workflow_1/mpj_2.conf", environment.configurationFile.cores);
//                    ChironInvocation.generatePoolControlFile(environment.configurationFile, machines);
//                    String[] argsFromSuperNode = null;
//                    int rank = 0;
//                    for(EMachine mac : machines){
//                        if(rank==0){
//                            argsFromSuperNode = ChironUtils.getFullArgumentsForLocalWithMPI(environment.configurationFile, mac);
//                        }else{
//                            ChironInvocation.executeLocalSciCumulusCore(mac, environment.configurationFile);
//                        }
//                        rank++;
//                    }
//                    
//                    MPI.Finalize();
//                    initMPI(body, argsFromSuperNode);
//                    ChironInvocation.executeSciCumulusCore(environment, argsFromSuperNode);
//                    System.exit(0);
//                }
                
                EActivity pipe = act.pipeline;
                while (pipe != null) {
                    pipe.status = EActivity.StatusType.PIPELINED;
                    pipe.startTime = new Date();
                    EProvenance.storeActivity(pipe);
                    pipe = pipe.pipeline;
                }
            }
            
            if (act.status == EActivity.StatusType.RUNNING) {
                EActivation item = EProvenance.loadReadyActivation(EProvenance.db, act);
                if (item != null) {
                    int k = 1, n = act.numActivations / (Chiron.MPI_size * environment.configurationFile.cores);
                    resultList.add(item);
                    addPipelineActivations(resultList, act, item);
                    if ((act.workflow.model == EWorkflow.ExecModel.STA_FAF) || (act.workflow.model == EWorkflow.ExecModel.STA_FTF)) {
                        while ((item != null) && (k < n)) {
                            item = EProvenance.loadReadyActivation(EProvenance.db, act);
                            if (item != null) {
                                resultList.add(item);
                                if (act.workflow.model == EWorkflow.ExecModel.STA_FTF) {
                                    addPipelineActivations(resultList, act, item);
                                }
                                k++;
                            }
                        }
                    }
                } else if (EProvenance.checkIfAllActivationsFinished(EProvenance.db, act)) {
                    act.status = EActivity.StatusType.FINISHED;
                    act.endTime = new Date();
                } else {
                    returnWait = true;
                }
            }
            if (act.status == EActivity.StatusType.FINISHED) {
                EProvenance.storeActivity(act);
                EActivity dep = act.pipeline;
                while (dep != null) {
                    dep.status = EActivity.StatusType.FINISHED;
                    dep.endTime = new Date();
                    EProvenance.storeActivity(dep);
                    dep = dep.pipeline;
                }
                runnableActivities.remove(act);
                evaluateDependencies();
                if (resultList.isEmpty()) {
                    i = 0;
                    continue;
                }
            }
            if (resultList.size() > 0) {
                break;
            }
            i++;
        }

        if ((resultList.isEmpty()) && returnWait) {
            resultList.add(EActivation.WAIT_ACTIVATION);
        }
        if (resultList.size() > 0) {
            EActivation[] result = new EActivation[resultList.size()];
            for (int j = 0; j < resultList.size(); j++) {
                result[j] = resultList.get(j);
            }
            return result;
        }
        return null;
    }

    /**
     * Obtém a atividade a partir de sua tag
     *
     * @param actTag
     * @return
     */
    public EActivity getActivity(String actTag) {
        for (int i = 0; i < activities.size(); i++) {
            EActivity activity = activities.get(i);
            if (activity.tag.equals(actTag)) {
                return activity;
            }
        }
        return null;
    }

    public CRelation getInputRelation(String name) {
        for (CRelation rel : this.inputRelations) {
            if (rel.name.equals(name)) {
                return rel;
            }
        }
        return null;
    }

    /**
     * Calcula a dependência entre as atividades do workflow
     */
    @SuppressWarnings("CallToThreadDumpStack")
    public void evaluateDependencies() throws SQLException, FileNotFoundException, IOException, InterruptedException {
        for (int i = 0; i < activities.size(); i++) {
            EActivity activity = activities.get(i);
            if (activity.status == EActivity.StatusType.BLOCKED) {
                EActivity.StatusType newStatus = EActivity.StatusType.READY;
                
                boolean finished = activity.hasFinishedDependentActivities();
                if(!finished){
                    newStatus = EActivity.StatusType.BLOCKED;
                    break;
                } else {
                    for(CRelation relation : activity.relations){
                        //the relation can be an input relation, must store input data into database and set activity to READY
                        if (relation.type.equals(CRelation.Type.INPUT) && relation.filename != null) {
                            EProvenance.insertRelationData(relation, this.expDir);
                            newStatus = EActivity.StatusType.READY;
                        }
                    }
                }
                
                activity.status = newStatus;
            }
        }

        for (int i = activities.size() - 1; i >= 0; i--) {
            EActivity activity = activities.get(i);
            if ((activity.status == EActivity.StatusType.READY) || (activity.status == EActivity.StatusType.RUNNING)) {
                if (runnableActivities.indexOf(activity) < 0) {
                    runnableActivities.add(activity);
                }
            }
        }
    }

    public void checkPipeline() {
        if ((model != ExecModel.DYN_FTF) && (model != ExecModel.STA_FTF)) {
            return;
        }
        for (int i = activities.size() - 1; i >= 0; i--) {
            EActivity activity = activities.get(i);
            if (activity.operation.type != Operator.MAP) {
                continue;
            }
            CRelation relation = activity.relations.get(0);
            if (relation.dependency != null) {
                CActivity dependency = relation.dependency;
                EActivity actDependent = this.getActivity(dependency.tag);
                if (actDependent.operation.type == Operator.MAP) {
                    actDependent.pipeline = activity;
                }
            }
        }
    }

    public boolean checkInputRelation(String relationName) {
        for (CRelation r : this.inputRelations) {
            if (r.name.equals(relationName)) {
                return true;
            }
        }
        return false;
    }
}
