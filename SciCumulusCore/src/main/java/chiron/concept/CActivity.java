package chiron.concept;

import chiron.EActivation;
import chiron.EActivity;
import chiron.EProvenance;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Jonas, Vítor.
 */
public abstract class CActivity {

    public int id;
    public String tag;
    public String activation;
    public String extractor;
    public String templateDir;
    public Operator type;
    public boolean constrained = false;

    public static CActivity newInstance(Operator operator) {

        switch (operator) {
            case MAP:
                return new Map();
            case SPLIT_MAP:
                return new SplitMap();
            case REDUCE:
                return new Reduce();
            case FILTER:
                return new Filter();
            case SR_QUERY:
                /**
                 * For now, The MRQuery Class fully satisfies the SRQuery Operator 
                 * as a more general case. However, it may be necessary to create 
                 * a separate class later.
                 */
                return new MRQuery();
            case MR_QUERY:
                return new MRQuery();
        }

        return null;

    }

    protected CActivity(Operator type) {
        this.type = type;
    }

    public abstract void addInput(CRelation relations);

    public abstract void addOutput(CRelation relations);

    public abstract List<CRelation> getInputRelations();

    public abstract List<CRelation> getOutputRelations();

    public abstract void generateActivations(EActivity act, String wfDir, String expDir) throws Exception;
    
    public abstract EActivation createPipelineActivation(String expDir, String wfDir, EActivity act, EActivity dep, EActivation activation);
        
    public void checkDependencies(CRelation input) throws SQLException {
        CActivity dependency = input.dependency;
        if (dependency != null) {
            List<CRelation> outputs = dependency.getOutputRelations();
            if (outputs.size() == 1) {
                CRelation dependencyOutput = outputs.get(0);
                EProvenance.propagateData(dependencyOutput, input);
            } else {
                throw new UnsupportedOperationException("Dependencies with multiple outputs is still not supported. Activity " + dependency.tag + "has more than one output Relation.");
            }
        }
    }
    
    protected EActivation createActivation(EActivity act, String folder) {
        EActivation newActivation = new EActivation();
        newActivation.activityID = act.id;
        newActivation.workspace = folder;
        newActivation.templateDir = this.templateDir;
        newActivation.commandLine = this.activation;
        newActivation.extractor = this.extractor;
        newActivation.constrained = this.constrained;

        return newActivation;
    }
}
