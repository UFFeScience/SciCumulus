package chiron;

import chiron.concept.CActivity;
import chiron.concept.CRelation;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * The class EActivity represents an execution instance of an activity of
 * the workflow. It basically stores provenance data for the activity being
 * executed.
 *
 * @author Eduardo, Vítor, Pedro, Jonas
 * @since 2011-01-04
 */
public class EActivity implements Serializable {

    protected EWorkflow workflow;
    public CActivity operation;
    public Integer id = null;
    public String tag;
    public StatusType status = StatusType.BLOCKED;
    public java.util.Date startTime = null;
    public java.util.Date endTime = null;
    public ArrayList<CRelation> relations = new ArrayList<CRelation>();
    public EActivity pipeline = null;
    public boolean constrained = false;
    public int numActivations = 0;
    public Integer cactid;

    public enum StatusType {
        BLOCKED, READY, RUNNING, PIPELINED, FINISHED, FINISHED_WITH_ERROR, ABORT
    }

    public EActivity(EWorkflow workflow) {
        this.workflow = workflow;
    }

    public boolean isConstrained() {
        return this.constrained;
    }

    public void setConstrainedTrue() {
        this.constrained = true;
    }
    
    public String getWorkflowDir() {
        return workflow.wfDir;
    }
    
    public String getExperimentDir() {
        return workflow.expDir;
    }

    public boolean hasFinishedDependentActivities() {
        for(CRelation relation : relations){
            CActivity dependency = relation.dependency;
            if(dependency!=null){
                EActivity actDependent = workflow.getActivity(dependency.tag);

                if (actDependent.status != EActivity.StatusType.FINISHED) {
                    return false;
                }
            }
        }
        
        return true;
    }
}
