package chiron.concept;

import chiron.ChironUtils;
import chiron.EActivity;
import chiron.EWorkflow;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author jonasdias, Vítor.
 */
public class CWorkflow {

    public int id;
    public String tag;
    
    /**
     * The map of activities that compose the workflow mapped by their ids
     */
    public TreeMap<Integer, CActivity> activities;
    /**
     * The relations associated with the workflow mapped by their name
     */
    public HashMap<String, CRelation> relations;

    public CWorkflow() {
        activities = new TreeMap<Integer, CActivity>();
        relations = new HashMap<String, CRelation>();
    }

    //    failure_handling
//    reliability
    @SuppressWarnings("CallToThreadDumpStack")
    public EWorkflow derive(String wfDir, String expDir, String exeTag, int maximumIterations, boolean userInteraction, double reliability, boolean redundancy) {
        EWorkflow eWorkflow = new EWorkflow();
        eWorkflow.tag = this.tag;
        eWorkflow.wfDir = wfDir;
        eWorkflow.expDir = expDir;
        eWorkflow.exeTag = exeTag;
        eWorkflow.maximumFailures = maximumIterations;
        eWorkflow.userInteraction = userInteraction;
        eWorkflow.reliability = Double.parseDouble(ChironUtils.formatFloat(reliability, 2));
        eWorkflow.redundancy = redundancy;

        for (Integer key : activities.keySet()) {
            CActivity cAct = activities.get(key);
            EActivity eAct = new EActivity(eWorkflow);
            eAct.cactid = cAct.id;
            eAct.operation = cAct;
            eAct.tag = cAct.tag;
            eAct.status = EActivity.StatusType.BLOCKED;
            for (CRelation relation : cAct.getInputRelations()) {
                if (relation.dependency == null) {
                    eWorkflow.inputRelations.add(relation);
                }
                eAct.relations.add(relation);
            }
            for (CRelation relation : cAct.getOutputRelations()) {
                eAct.relations.add(relation);
            }
            eWorkflow.activities.add(eAct);
        }
        return eWorkflow;
    }

    public CActivity getActivity(int activityId) {
        return activities.get(activityId);
    }
}
