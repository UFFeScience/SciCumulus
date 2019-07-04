package chiron.concept;

import chiron.EActivation;
import chiron.EActivity;
import chiron.EProvenance;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jonasdias
 */
class MRQuery extends CActivity {

    List<CRelation> input;
    CRelation output;

    public MRQuery() {
        this(Operator.SR_QUERY);
        input = new ArrayList<CRelation>();
    }

    private MRQuery(Operator type) {
        super(type);
    }

    @Override
    public void addInput(CRelation relations) {
        input.add(relations);
    }

    @Override
    public void addOutput(CRelation relations) {
        output = relations;
    }

    @Override
    public List<CRelation> getInputRelations() {
        return input;
    }

    @Override
    public List<CRelation> getOutputRelations() {
        List ret = new ArrayList();
        ret.add(output);
        return ret;
    }

    @Override
    public void generateActivations(EActivity act, String wfDir, String expDir) throws Exception {
        if (input.isEmpty()) {
            //something is wrong
            throw new NullPointerException("The input relations for activity" + act.tag + " are not available in the list of relations.");
        } else {
            for(CRelation inputRelation : this.getInputRelations()) {
                checkDependencies(inputRelation);
            }
            EProvenance.executeSQLActivation(this.activation, input, output);
            act.status = EActivity.StatusType.FINISHED;
            act.endTime = new Date();
        }
    }

    @Override
    public EActivation createPipelineActivation(String expDir, String wfDir, EActivity act, EActivity dep, EActivation activation) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
