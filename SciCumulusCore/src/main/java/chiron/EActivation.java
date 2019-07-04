package chiron;

import chiron.EActivity.StatusType;
import chiron.concept.CActivation;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The EActivation class represents the minimal unit of data necessary
 * to run an instance of an EActivity of the EWorkflow. It stores its own
 * input and output data, the references to its files and general provenance
 * data. Since this is the class that is distributed over MPI, it should be
 * as small as possible.
 *
 * @author Jonas, Eduardo, Vítor
 * @since 2010-12-25
 *
 *
 */
public class EActivation implements Serializable {

    public static EActivation WAIT_ACTIVATION = new EActivation();
    public EActivation pipelinedFrom = null;
    public Integer id = null;
    public Integer activityID = null;
    public Integer machineID = null;
    public Integer exitStatus = null;
    public String commandLine = null;
    public String extractor = null;
    public String workspace = null;
    public String templateDir = null;
    public String stdErr = null;
    public String stdOut = null;
    public Date startTime = null;
    public Date endTime = null;
    public StatusType status = StatusType.READY;
    public List<EFile> files = new ArrayList<EFile>();
    public ERelation inputRelation = null;
    public ERelation outputRelation = null;
    public boolean constrained = false;
    public CActivation activator;
    public int failureTries = 0;
    public boolean failure = false;

    private void preparePipeline() {
        if (pipelinedFrom != null) {
            activator.pipelineData(this);
        }
    }

    public void executeActivation() {
        startTime = new Date();
        this.preparePipeline();
        
        try{
            activator.instrument(this);
            activator.execute(this);
            activator.extract(this);
            
            status = EActivity.StatusType.FINISHED;
            endTime = new Date();
            failureTries = 0;
        } catch(Exception ex){
            stdErr += ex.getMessage();
            status = EActivity.StatusType.READY;
            failureTries++;
            startTime = null;
        }
    }
    
    public boolean isConstrained() {
        return constrained;
    }
    
    public boolean hasFile(String fieldName) {
        for(EFile file : files) {
            if(file.fieldName.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }
    
    public EFile getFile(String fieldName) {
        for(EFile file : files) {
            if(file.fieldName.equalsIgnoreCase(fieldName)) {
                return file;
            }
        }
        return null;
    }
}
