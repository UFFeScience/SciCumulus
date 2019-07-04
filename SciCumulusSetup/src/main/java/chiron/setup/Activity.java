package chiron.setup;

import java.util.ArrayList;
import nu.xom.ValidityException;

public class Activity {

    private String workflowTag;
    private String tag;
    private String atype;
    private String templatedir;
    private String constrained;
    private int id;
    private int actid;
    private int wkfid;
    private ArrayList<Relation> relations;
    private Type type;
    private String description;
    private String template;
    private String activation;
    private String extractor;
    private ArrayList<Relation> input;
    private ArrayList<Relation> output;
    private ArrayList<Operand> operands;

    public Activity(String workflowTag, String tag, Type type, String description, String template, String activation, String extractor) {
        this.workflowTag = workflowTag;
        this.tag = tag;
        this.type = type;
        this.description = description;
        this.template = template;
        this.activation = activation;
        this.extractor = extractor;
        this.input = new ArrayList<Relation>();
        this.output = new ArrayList<Relation>();
        this.operands = new ArrayList<Operand>();
    }

    public Activity(String workflowTag, String tag, String atype, String description, String templatedir, String activation, String extractor, String constrained) {
        this.workflowTag = workflowTag;
        this.input = new ArrayList<Relation>();
        this.output = new ArrayList<Relation>();
        this.tag = tag;
        this.atype = atype;
        this.description = description;
        if (templatedir == null) {
            this.templatedir = "";
        } else {
            this.templatedir = templatedir;
        }
        this.activation = activation;
        if (extractor == null) {
            this.extractor = "";
        } else {
            this.extractor = extractor;
        }
        if (constrained == null) {
            this.constrained = "";
        } else {
            this.constrained = constrained;
        }
        this.relations = new ArrayList<Relation>();
        this.operands = new ArrayList<Operand>();
    }

    public Activity(String workflowTag, int actid, int wkfid, String tag, String atype, String description, String templatedir, String activation, String extractor, String constrained) {
        this.workflowTag = workflowTag;
        this.input = new ArrayList<Relation>();
        this.output = new ArrayList<Relation>();
        this.actid = actid;
        this.wkfid = wkfid;
        this.tag = tag;
        this.atype = atype;
        this.description = description;
        if (templatedir == null) {
            this.templatedir = "";
        } else {
            this.templatedir = templatedir;
        }
        this.activation = activation;
        if (extractor == null) {
            this.extractor = "";
        } else {
            this.extractor = extractor;
        }
        if (constrained == null) {
            this.constrained = "";
        } else {
            this.constrained = constrained;
        }
        if (constrained != null && constrained.equalsIgnoreCase("t")) {
            this.constrained = "true";
        }
        if (constrained != null && constrained.equalsIgnoreCase("f")) {
            this.constrained = "false";
        }
        this.relations = new ArrayList<Relation>();
    }

    public enum Type {

        MAP, SPLIT_MAP, REDUCE, SR_QUERY, MR_QUERY, FILTER, SLICED_MAP, EVALUATE;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getAtype() {
        return atype;
    }

    public void setAtype(String atype) {
        this.atype = atype;
    }

    public String getTemplatedir() {
        return templatedir;
    }

    public String getTemplatedirSQL() {
        String s = null;
        if (this.templatedir != null && this.templatedir.equalsIgnoreCase("")) {
            return s;
        } else {
            return this.templatedir;
        }
    }

    public void setTemplatedir(String templatedir) {
        this.templatedir = templatedir;
    }

    public String getConstrained() {
        return constrained;
    }

    public String getConstrainedSQL() {
        String s = null;
        if (this.constrained != null && this.constrained.equalsIgnoreCase("true")) {
            s = "T";
        }
        if (this.constrained != null && this.constrained.equalsIgnoreCase("false")) {
            s = "F";
        }
        if (this.constrained != null && this.constrained.equalsIgnoreCase("")) {
            return s;
        }
        return s;
    }

    public void setConstrained(String constrained) {
        this.constrained = constrained;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActid() {
        return actid;
    }

    public void setActid(int actid) {
        this.actid = actid;
    }

    public int getWkfid() {
        return wkfid;
    }

    public void setWkfid(int wkfid) {
        this.wkfid = wkfid;
    }

    public ArrayList<Relation> getRelations() {
        return relations;
    }

    public void setRelations(ArrayList<Relation> relations) {
        this.relations = relations;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getActivation() {
        return activation;
    }

    public void setActivation(String activation) {
        this.activation = activation;
    }

    public String getExtractor() {
        return extractor;
    }

    public String getExtractorSQL() {
        String s = null;
        if (this.extractor != null && this.extractor.equalsIgnoreCase("")) {
            return s;
        } else {
            return this.extractor;
        }
    }

    public void setExtractor(String extractor) {
        this.extractor = extractor;
    }

    public ArrayList<Relation> getInput() {
        return input;
    }

    public void setInput(ArrayList<Relation> input) {
        this.input = input;
    }

    public ArrayList<Relation> getOutput() {
        return output;
    }

    public void setOutput(ArrayList<Relation> output) {
        this.output = output;
    }

    public void addOperand(String agreg_field, String textValue) {
        Operand op = new Operand(agreg_field, textValue);
        operands.add(op);
    }

    public ArrayList<Operand> getOperands() {
        return operands;
    }

    public void checkAgregationField(String operandTextValue) throws ValidityException {
        String agregField = operandTextValue;
        boolean contains = false;
        for (Relation r : input) {
            for (Field f : r.getFields()) {
                if (f.getFname().equalsIgnoreCase(agregField)) {
                    contains = true;
                }
            }
        }
        if (!contains) {
            throw new ValidityException("The aggregation field " + agregField + " for the Reduce"
                    + "Operation is not available in any input relation of activity " + this.tag + ". Please check your XML.");
        }
    }

    public void addInputRelation(Relation r) {
        input.add(r);
    }

    public void addRelation(Relation r) {
        if (!this.relations.contains(r)) {
            this.relations.add(r);
        }
    }

    public void addOutputRelation(Relation r) {
        this.output.add(r);
    }

    public String getDropStatement() {
        String statement = "";
        String line = System.getProperty("line.separator");
        for (Relation i : input) {
            statement += "DROP TABLE \"" + workflowTag + "\".\"" + i.getRname() + "\" CASCADE;" + line;
            if (i.getDependencyUp() == null) {
                statement += "DROP SEQUENCE \"" + workflowTag + "\".\"" + i.getRname().toLowerCase() + "_seq\";" + line;
            }
        }
        for (Relation o : output) {
            statement += "DROP TABLE \"" + workflowTag + "\".\"" + o.getRname() + "\" CASCADE;" + line;
            statement += "DROP SEQUENCE \"" + workflowTag + "\".\"" + o.getRname().toLowerCase() + "_seq\";" + line;
        }
        return statement;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 23 * hash + (this.templatedir != null ? this.templatedir.hashCode() : 0);
        hash = 23 * hash + (this.activation != null ? this.activation.hashCode() : 0);
        hash = 23 * hash + (this.extractor != null ? this.extractor.hashCode() : 0);
        hash = 23 * hash + (this.constrained != null ? this.constrained.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Activity other = (Activity) obj;
        if ((this.description == null) ? (other.description != null) : !this.description.equalsIgnoreCase(other.description)) {
            return false;
        }
        if ((this.templatedir == null) ? (other.templatedir != null) : !this.templatedir.equalsIgnoreCase(other.templatedir)) {
            return false;
        }
        if ((this.activation == null) ? (other.activation != null) : !this.activation.equalsIgnoreCase(other.activation)) {
            return false;
        }
        if ((this.extractor == null) ? (other.extractor != null) : !this.extractor.equalsIgnoreCase(other.extractor)) {
            return false;
        }
        if ((this.constrained == null) ? (other.constrained != null) : !this.constrained.equalsIgnoreCase(other.constrained)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "\n\tActivity{" + "tag=" + tag + ", atype=" + atype + ", templatedir=" + templatedir + ", constrained=" + constrained + ", description=" + description + ", activation=" + activation + ", extractor=" + extractor + ", relations=" + relations + '}';
    }
}