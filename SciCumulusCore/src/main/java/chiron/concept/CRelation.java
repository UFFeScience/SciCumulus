package chiron.concept;

import java.io.Serializable;
import java.util.List;

/**
 * This class represent the conceptual Relation of an activity of the
 * workflow. It is derived into an ERelation class for execution.
 *
 * @author Eduardo, Jonas, Vítor.
 * @since 2010-12-25
 */
public class CRelation implements Serializable {

    public Integer id = null;
    public List<String> fields;
    public Type type;
    public String name;
    public CActivity dependency;
    public String filename;

    public enum Type {

        INPUT, OUTPUT
    }
    
    public CRelation(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = Type.valueOf(type.toUpperCase());
    }

    /**
     * Creates a relation with a dependency. Needs to use a workflow class to
     * setup the correct dependency for the relation
     *
     * @param ok The relation ID obtained from the database
     * @param name The relation name from the database
     * @param wf The workflow that contains the relation
     * @param dependency The ok of the activity that this relation depends on
     */
    public CRelation(int id, String name, String type, CWorkflow wf, Integer dependency) {
        this(id, name, type);
        if (dependency != null) {
            this.dependency = wf.activities.get(dependency);
        }

    }

    public String getFieldNames() {
        String fieldNames = "";
        boolean first = true;
        for (String f : fields) {
            if (first) {
                fieldNames += f;
                first = false;
            } else {
                fieldNames += ", " + f;
            }
        }

        return fieldNames;
    }
}
