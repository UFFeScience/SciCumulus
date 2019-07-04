package chiron.setup;

import java.io.File;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

/**
 *
 * @author Matheus Costa
 */
public class XMLUpdateFinder {

    public DBReader dbReader;
    public Workflow workflow;
    public WorkflowUpdater updater;

    public void workflowUpdate(String xmlfile) {
        //READ XML FILE
        try {
            Builder builder = new Builder();
            File xmlFile = new File(xmlfile);
            Document doc = builder.build(xmlFile);

            Element Workflow = doc.getRootElement().getFirstChildElement("conceptualWorkflow");
            Element dataBase = doc.getRootElement().getFirstChildElement("database");

            this.workflow = new Workflow(Workflow.getAttributeValue("tag"), Workflow.getAttributeValue("description"));

            Elements activities = Workflow.getChildElements();
            for (int i = 0; i < activities.size(); i++) {
                Element activity = activities.get(i);

                Activity act = new Activity(workflow.getTag(), activity.getAttributeValue("tag"), activity.getAttributeValue("type"), activity.getAttributeValue("description"), activity.getAttributeValue("templatedir"), activity.getAttributeValue("activation"), activity.getAttributeValue("extractor"), activity.getAttributeValue("constrained"));
                this.workflow.addActivity(act);
                Elements activityElements = activity.getChildElements();

                for (int j = 0; j < activityElements.size(); j++) {
                    Element element = activityElements.get(j);
                    String elementName = element.getLocalName();

                    if (elementName.equalsIgnoreCase("relation")) {
                        if (element.getAttributeValue("reltype").equalsIgnoreCase("input")) {
                            String name = element.getAttributeValue("name");
                            String type = element.getAttributeValue("reltype");
                            String dependency = element.getAttributeValue("dependency");
                            Relation relation = new Relation(workflow.getTag(), type, name, dependency);
                            act.addRelation(relation);
                            act.addInputRelation(relation);
                        }
                        if (element.getAttributeValue("reltype").equalsIgnoreCase("output")) {
                            String name = element.getAttributeValue("name");
                            String type = element.getAttributeValue("reltype");
                            String dependency = element.getAttributeValue("dependency");
                            Relation relation = new Relation(workflow.getTag(), type, name, dependency);
                            act.addRelation(relation);
                            act.addOutputRelation(relation);
                        }
                    }
                }

                for (int j = 0; j < activityElements.size(); j++) {
                    Element element = activityElements.get(j);
                    String elementName = element.getLocalName();
                    if (elementName.equalsIgnoreCase("field")) {
                        int decimalPlaces = 0;
                        if (element.getAttributeValue("decimalplaces") != null) {
                            decimalPlaces = Integer.parseInt(element.getAttributeValue("decimalplaces"));
                        }
                        if (element.getAttributeValue("input") != null) {
                            Field field = new Field(decimalPlaces, element.getAttributeValue("name"), element.getAttributeValue("type"), element.getAttributeValue("operation"), element.getAttributeValue("instrumented"));
                            for (Relation r : act.getRelations()) {
                                if (r.getRname().equalsIgnoreCase(element.getAttributeValue("input"))) {
                                    r.addField(field);
                                }
                            }
                        }
                        if (element.getAttributeValue("output") != null) {
                            Field field = new Field(decimalPlaces, element.getAttributeValue("name"), element.getAttributeValue("type"), element.getAttributeValue("operation"), element.getAttributeValue("instrumented"));
                            for (Relation r : act.getRelations()) {
                                if (r.getRname().equalsIgnoreCase(element.getAttributeValue("output"))) {
                                    r.addField(field);
                                }
                            }
                        }
                    }
                }
            }

            //READ THE DATABASE
            this.dbReader = new DBReader(dataBase.getAttributeValue("server"), dataBase.getAttributeValue("port"), dataBase.getAttributeValue("name"), dataBase.getAttributeValue("username"), dataBase.getAttributeValue("password"), this.workflow.getTag());

            System.out.println("\n\n===========================XML Workflow===========================");
            System.out.println(this.workflow.toString());
            System.out.println("\n\n===========================Database Workflow===========================");
            System.out.println(this.dbReader.getWorkflow().toString());

            //UPDATE THE WORKFLOW
            this.updater = new WorkflowUpdater(this.workflow, this.dbReader.getWorkflow());
            this.updater.setDb(this.dbReader.getDb());
            this.updater.workflowUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}