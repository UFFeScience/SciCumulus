package chiron.setup;

/**
 * Reads the XML file and create the Workflow object
 */
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import nu.xom.*;

public class Reader {

    private Element database;

    /**
     * Reade a XML file and sets up the Workflow data structure
     *
     * @param filename The XML Filename with the concetual workflow structure
     * @return The readed workflow
     * @throws ParsingException
     * @throws ValidityException
     * @throws IOException
     */
    public Workflow readXML(String filename) throws ParsingException, ValidityException, IOException {
        Builder builder = new Builder();
        File xmlFile = new File(filename);
        Document doc = builder.build(xmlFile);
        Element workflowElement = doc.getRootElement().getFirstChildElement("conceptualWorkflow");
        //workflow attributes
        String wfTag = workflowElement.getAttributeValue("tag");
        String wfdescription = workflowElement.getAttributeValue("description");
        Workflow workflow = new Workflow(wfTag, wfdescription);
        //grab the database element
        database = doc.getRootElement().getFirstChildElement("database");
        //get all activities
        Elements activityElements = workflowElement.getChildElements();
        for (int j = 0; j < activityElements.size(); j++) {
            Element element = activityElements.get(j);
            String activityName = element.getAttributeValue("tag");
            String activityType = element.getAttributeValue("type");
            String activityDescription = element.getAttributeValue("description");
            String templatedir = element.getAttributeValue("template");
            String activation = element.getAttributeValue("activation");
            String extractor = element.getAttributeValue("extractor");
            String operand = element.getAttributeValue("operand");
            
            Activity activity = new Activity(workflow.getTag(), activityName, Activity.Type.valueOf(activityType.toUpperCase()), activityDescription, templatedir, activation, extractor);
            workflow.activities.put(activityName, activity);
            HashMap<String, Relation> relations = new HashMap<String, Relation>();
            Elements elemens = element.getChildElements();
            for (int i = 0; i < elemens.size(); i++) {
                Element el = elemens.get(i);
                String elementName = el.getLocalName();
                if (elementName.equals("relation")) {
                    String name = el.getAttributeValue("name");
                    String type = el.getAttributeValue("reltype");
                    String dependency = el.getAttributeValue("dependency");

                    Relation rel = new Relation(wfTag, name, Relation.Type.valueOf(type.toUpperCase()));
                    if (dependency != null) {
                        rel.setDependency(workflow.activities.get(dependency));
                    }
                    if (rel.getType().equals(Relation.Type.OUTPUT)) {
                        activity.addOutputRelation(rel);
                    } else {
                        activity.addInputRelation(rel);
                    }
                    relations.put(name, rel);
                } else if (elementName.equals("field")){
                    String name = el.getAttributeValue("name");
                    String input = el.getAttributeValue("input");
                    String output = el.getAttributeValue("output");
                    String type = el.getAttributeValue("type");
                    String places = el.getAttributeValue("decimalplaces");
                    String operation = el.getAttributeValue("operation");
                    String instrumented = el.getAttributeValue("instrumented");
                    Field field = new Field(name);
                    field.setFtype(type);
                    if (places != null) {
                        field.setDecimalplaces((int) Integer.valueOf(places));
                    }
                    if (operation != null) {
                        field.setFileoperation(operation);
                    }
                    if (instrumented != null) {
                        field.setInstrumented(instrumented);
                    }
                    if (input != null) {
                        Relation relation = relations.get(input);
                        if (relation == null) {
                            String line = System.getProperty("line.separator");
                            String message = "The activity " + activity.getTag()
                                    + " has the following relations: " + line;
                            for (String relName : relations.keySet()) {
                                message += relName + line;
                            }
                            message += "However, the field " + field.getFname() + "says it belongs to relation " + input + line;
                            message += "Please check your XML.";
                            throw new NullPointerException(message);
                        }
                        relation.addField(field);
                    }
                    if (output != null) {
                        Relation relation = relations.get(output);
                        if (relation == null) {
                            String line = System.getProperty("line.separator");
                            String message = "The activity " + activity.getTag()
                                    + " has the following relations:" + relations.keySet() + line;
                            message += "However, the field " + field.getFname() + " says it belongs to relation " + output + ".";
                            message += " Please check your XML.";
                            throw new NullPointerException(message);
                        }
                        relation.addField(field);
                    }
                }
            }
            
            if (operand != null) {
                String[] operands = operand.split(",");
                for(String op : operands){
                    if (activity.getType().equals(Activity.Type.REDUCE)) {
                        activity.addOperand("AGREG_FIELD",op);
                        activity.checkAgregationField(op);
                    } else if (activity.getType().equals(Activity.Type.EVALUATE)) {
                        activity.addOperand("MINTERM",op);
                    }
                }
            }
        }
        return workflow;
    }

    public String getDbAttribute(String attr) {
        if (database != null) {
            return database.getAttributeValue(attr);
        } else {
            throw new NullPointerException("The method getDbAttribute must be called only after the readXML method.");
        }

    }
}