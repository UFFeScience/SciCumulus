package chiron.setup;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import vs.database.M_DB;
import vs.database.M_Query;

/**
 *
 * @author Matheus Costa
 */
public class WorkflowUpdater {

    private Workflow xml, dataBase;
    private ArrayList<Activity> sameActivities, updateActivities, newActivities;
    private ArrayList<Relation> xmlRelations, dataBaseRelations, sameRelations, updateRelations, newRelations;
    private ArrayList<Field> xmlFields, sameFields, updateFields, newFields, deleteFields;
    private M_DB db;
    private M_Query query;
    private int indexAct, indexRel;
    private Scanner scanner;

    //CONSTRUCTOR
    public WorkflowUpdater(Workflow xml, Workflow dataBase) {
        this.xml = xml;
        this.dataBase = dataBase;
        this.scanner = new Scanner(System.in);
        this.indexRel = dataBase.getIndexRel();
        this.indexAct = dataBase.getIndexAct();
        this.sameActivities = new ArrayList<Activity>();
        this.updateActivities = new ArrayList<Activity>();
        this.newActivities = new ArrayList<Activity>();

        this.xmlRelations = new ArrayList<Relation>();
        this.dataBaseRelations = new ArrayList<Relation>();
        this.sameRelations = new ArrayList<Relation>();
        this.updateRelations = new ArrayList<Relation>();
        this.newRelations = new ArrayList<Relation>();

        this.xmlFields = new ArrayList<Field>();
        this.sameFields = new ArrayList<Field>();
        this.updateFields = new ArrayList<Field>();
        this.newFields = new ArrayList<Field>();
        this.deleteFields = new ArrayList<Field>();
    }

    //GETTER AND SETTER
    public Workflow getXml() {
        return xml;
    }

    public void setXml(Workflow xml) {
        this.xml = xml;
    }

    public Workflow getDataBase() {
        return dataBase;
    }

    public void setDataBase(Workflow dataBase) {
        this.dataBase = dataBase;
    }

    public M_DB getDb() {
        return db;
    }

    public void setDb(M_DB db) {
        this.db = db;
    }

    public M_Query getQuery() {
        return query;
    }

    public void setQuery(M_Query query) {
        this.query = query;
    }

    public int getIndexAct() {
        return indexAct;
    }

    public void setIndexAct(int indexAct) {
        this.indexAct = indexAct;
    }

    public int getIndexRel() {
        return indexRel;
    }

    public void setIndexRel(int indexRel) {
        this.indexRel = indexRel;
    }

    //SEARCH FOR ALTERATIONS
    public boolean workflowCheck() {
        if (this.xml.equals(this.dataBase)) {
            return true;
        } else {
            return false;
        }
    }

    public void sameActivitiesCheck() {
        for (Activity a : xml.getActivities()) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    if (a.equals(act)) {
                        if (!sameActivities.contains(a)) {
                            sameActivities.add(a);
                        }
                    }
                }
            }
        }
    }

    public void updateActivitiesCheck() {
        for (Activity a : xml.getActivities()) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    if (!sameActivities.contains(a)) {
                        updateActivities.add(a);
                    }
                }
            }
        }
    }

    public void newActivitiesCheck() {
        for (Activity a : xml.getActivities()) {
            if (!sameActivities.contains(a)) {
                if (!updateActivities.contains(a)) {
                    newActivities.add(a);
                }
            }
        }
    }

    public void checkSameRelations() {
        for (Activity a : sameActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                if (r.equals(rel)) {
                                    sameRelations.add(r);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Activity a : updateActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                if (r.equals(rel)) {
                                    sameRelations.add(r);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkUpdateRelations() {
        for (Activity a : dataBase.getActivities()) {
            for (Activity act : sameActivities) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                if (!r.equals(rel)) {
                                    updateRelations.add(rel);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Activity a : dataBase.getActivities()) {
            for (Activity act : updateActivities) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                if (!r.equals(rel)) {
                                    updateRelations.add(rel);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkNewRelations() {
        ArrayList<Relation> same = new ArrayList<Relation>();
        for (Activity a : dataBase.getActivities()) {
            for (Activity act : sameActivities) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                if (r.equals(rel)) {
                                    same.add(rel);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Activity a : dataBase.getActivities()) {
            for (Activity act : updateActivities) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                if (r.equals(rel)) {
                                    same.add(rel);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Activity a : sameActivities) {
            for (Relation r : a.getRelations()) {
                if (!same.contains(r)) {
                    if (!updateRelations.contains(r)) {
                        newRelations.add(r);
                    }
                }
            }
        }
        for (Activity a : updateActivities) {
            for (Relation r : a.getRelations()) {
                if (!same.contains(r)) {
                    if (!updateRelations.contains(r)) {
                        newRelations.add(r);
                    }
                }
            }
        }
    }

    public void checkSameFields() {
        for (Activity a : sameActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                for (Field f : r.getFields()) {
                                    for (Field fi : rel.getFields()) {
                                        if (f.getFname().equalsIgnoreCase(fi.getFname())) {
                                            if (f.equals(fi)) {
                                                if (!sameFields.contains(f)) {
                                                    sameFields.add(f);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Activity a : updateActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                for (Field f : r.getFields()) {
                                    for (Field fi : rel.getFields()) {
                                        if (f.getFname().equalsIgnoreCase(fi.getFname())) {
                                            if (f.equals(fi)) {
                                                if (!sameFields.contains(f)) {
                                                    sameFields.add(f);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkUpdateFields() {
        for (Activity a : sameActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                for (Field f : r.getFields()) {
                                    for (Field fi : rel.getFields()) {
                                        if (f.getFname().equalsIgnoreCase(fi.getFname())) {
                                            if (f.equals(fi)) {
                                                continue;
                                            } else {
                                                if (!updateFields.contains(f)) {
                                                    updateFields.add(f);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Activity a : updateActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                for (Field f : r.getFields()) {
                                    for (Field fi : rel.getFields()) {
                                        if (f.getFname().equalsIgnoreCase(fi.getFname())) {
                                            if (f.equals(fi)) {
                                                continue;
                                            } else {
                                                if (!updateFields.contains(f)) {
                                                    updateFields.add(f);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkNewFields() {
        for (Activity a : sameActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                for (Field f : r.getFields()) {
                                    if (!sameFields.contains(f)) {
                                        if (!updateFields.contains(f)) {
                                            if (!newFields.contains(f)) {
                                                newFields.add(f);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Activity a : updateActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                for (Field f : r.getFields()) {
                                    if (!sameFields.contains(f)) {
                                        if (!updateFields.contains(f)) {
                                            if (!newFields.contains(f)) {
                                                newFields.add(f);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkDeleteFields() {
        for (Activity a : sameActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                for (Field fi : rel.getFields()) {
                                    if (!sameFields.contains(fi)) {
                                        if (!updateFields.contains(fi)) {
                                            if (!newFields.contains(fi)) {
                                                this.deleteFields.add(fi);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (Field f : updateFields) {
                for (Field fi : deleteFields) {
                    if (f.getFname().equalsIgnoreCase(fi.getFname()) && f.getRelid() == fi.getRelid()) {
                        fi.setFname("DELETE");
                    }
                }
            }
            for (int i = 0; i < deleteFields.size(); i++) {
                if (deleteFields.get(i).getFname().equalsIgnoreCase("delete")) {
                    deleteFields.remove(i);
                }
            }
        }
        for (Activity a : updateActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                for (Field fi : rel.getFields()) {
                                    if (!sameFields.contains(fi)) {
                                        if (!updateFields.contains(fi)) {
                                            if (!newFields.contains(fi)) {
                                                this.deleteFields.add(fi);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //OTHERS
    public void setActivitiesIds() {
        for (Activity a : sameActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    a.setActid(act.getActid());
                }
            }
        }
        for (Activity a : updateActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    a.setActid(act.getActid());
                }
            }
        }
        for (Activity a : newActivities) {
            a.setWkfid(dataBase.getId());
        }
    }

    public void setRelationsIds() {
        for (Activity a : sameActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                r.setRelid(rel.getRelid());
                            }
                        }
                        r.setActid(act.getActid());
                    }
                }
            }
        }
        for (Activity a : updateActivities) {
            for (Activity act : dataBase.getActivities()) {
                if (a.getTag().equalsIgnoreCase(act.getTag())) {
                    for (Relation r : a.getRelations()) {
                        for (Relation rel : act.getRelations()) {
                            if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                r.setRelid(rel.getRelid());
                            }
                        }
                        r.setActid(act.getActid());
                    }
                }
            }
        }
        for (Relation r : newRelations) {
            r.setRelid(indexRel + 1);
            indexRel = indexRel + 1;
        }
    }

    public void setFieldsIds() {
        for (Activity a : xml.getActivities()) {
            for (Relation r : a.getRelations()) {
                if (r.getRtype().equalsIgnoreCase("input")) {
                    for (Field f : r.getFields()) {
                        f.setRelid(r.getRelid());
                    }
                }
                if (r.getRtype().equalsIgnoreCase("output")) {
                    for (Field f : r.getFields()) {
                        f.setRelid(r.getRelid());
                    }
                }
            }
        }
    }

    public void setXmlRelations() {
        for (Activity a : this.xml.getActivities()) {
            for (Relation r : a.getRelations()) {
                if (!this.xmlRelations.contains(r)) {
                    this.xmlRelations.add(r);
                }
            }
        }
    }

    public void setDataBaseRelations() {
        for (Activity a : this.dataBase.getActivities()) {
            for (Relation r : a.getRelations()) {
                if (!this.dataBaseRelations.contains(r)) {
                    this.dataBaseRelations.add(r);
                }
            }
        }
    }

    public void setDependencyRelations() {
        for (Relation r : this.xmlRelations) {
            for (Activity a : this.xml.getActivities()) {
                if (r.getDependencyUp() != null && r.getRtype().equalsIgnoreCase("input")) {
                    if (r.getDependencyUp().equalsIgnoreCase(a.getTag())) {
                        r.setDependencyDb(a.getActid());
                    }
                }
            }
        }
    }

    public void setDependencyRelationsUpdated() {
        for (Relation r : this.xmlRelations) {
            for (Activity a : this.xml.getActivities()) {
                if (r.getDependencyUp() != null && r.getRtype().equalsIgnoreCase("input")) {
                    if (r.getDependencyUp().equalsIgnoreCase(a.getTag())) {
                        r.setDependencyDb(a.getActid());
                    }
                }
            }
        }
    }

    //EXECUTE UPDATE
    public void workflowUpdate() throws SQLException {
        String q, wish = "n", wish2 = "n", wish3 = "n", wish4 = "n";

        //UPDATE WORKFLOW
        if (workflowCheck() == true) {
        } else {
            System.out.println("The workflows tags are different, do you want update?(y/n)");
            wish = scanner.nextLine();
            if (wish.equalsIgnoreCase("y")) {
                q = "UPDATE public.cworkflow SET description=? WHERE tag=?";
                query = db.prepQuery(q);
                query.setParString(1, xml.getDescription());
                query.setParString(2, xml.getTag());
                query.executeUpdate();
            }
        }

        //UPDATE ACTIVITIES
        this.sameActivitiesCheck();
        this.updateActivitiesCheck();
        this.newActivitiesCheck();
        if (!newActivities.isEmpty() || !updateActivities.isEmpty()) {
            System.out.println("There are modifications on Activities, do you want to update?(y/n)");
            wish2 = scanner.nextLine();
        }
        this.setActivitiesIds();
        this.setDataBaseRelations();
        this.setXmlRelations();
        if (wish2.equalsIgnoreCase("y")) {
            for (Activity a : newActivities) {
                q = "INSERT INTO public.cactivity(actid, wkfid, tag, atype, description, templatedir, activation, extractor, constrained)VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                query = db.prepQuery(q);
                query.setParInt(1, indexAct + 1);
                a.setActid(indexAct + 1);
                query.setParInt(2, a.getWkfid());
                query.setParString(3, a.getTag());
                query.setParString(4, a.getAtype());
                query.setParString(5, a.getDescription());
                query.setParString(6, a.getTemplatedirSQL());
                query.setParString(7, a.getActivation());
                query.setParString(8, a.getExtractorSQL());
                query.setParString(9, a.getConstrainedSQL());
                query.executeUpdate();
                this.setDependencyRelations();
                indexAct = indexAct + 1;
                this.setDataBaseRelations();
                for (Relation rela : this.dataBaseRelations) {
                    for (Relation r : this.dataBaseRelations) {
                        for (Relation rel : a.getRelations()) {
                            if (rel.getRtype().equalsIgnoreCase("input") && r.getRname().equalsIgnoreCase(rel.getRname())) {
                                while (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                    System.out.println("INPUT RELATION:\n");
                                    System.out.println("A Relation with this name: " + r.getRname() + " already exists.");
                                    rel.setRname(this.scanner.nextLine());
                                }
                            }
                            rel.setActid(a.getActid());
                        }
                    }
                }
                for (Relation rela : this.dataBaseRelations) {
                    for (Relation r : this.dataBaseRelations) {
                        for (Relation rel : a.getRelations()) {
                            if (rel.getRtype().equalsIgnoreCase("output") && r.getRname().equalsIgnoreCase(rel.getRname())) {
                                while (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                    System.out.println("INPUT RELATION:\n");
                                    System.out.println("A Relation with this name: " + r.getRname() + " already exists.");
                                    rel.setRname(this.scanner.nextLine());
                                }
                            }
                            rel.setActid(a.getActid());
                        }
                    }
                }
                for (Relation r : a.getRelations()) {
                    r.setRelid(indexRel + 1);
                    indexRel = indexRel + 1;
                }
                this.setDependencyRelations();
                for (Relation r : a.getRelations()) {
                    if (r.getDependencyUp() == null) {
                        q = "INSERT INTO public.crelation(relid, actid, rtype, rname) VALUES (?, ?, ?, ?)";
                        query = db.prepQuery(q);
                        query.setParInt(1, r.getRelid());
                        query.setParInt(2, r.getActid());
                        query.setParString(3, r.getRtype().toUpperCase());
                        query.setParString(4, r.getRname());
                        query.executeUpdate();
                    } else {
                        this.setDependencyRelationsUpdated();
                        q = "INSERT INTO public.crelation(relid, actid, rtype, rname, dependency) VALUES (?, ?, ?, ?, ?)";
                        query = db.prepQuery(q);
                        query.setParInt(1, r.getRelid());
                        query.setParInt(2, r.getActid());
                        query.setParString(3, r.getRtype().toUpperCase());
                        query.setParString(4, r.getRname());
                        query.setParInt(5, r.getDependencyDb());
                        query.executeUpdate();
                    }
                }
                for (Relation r : a.getRelations()) {
                    for (Field f : r.getFields()) {
                        q = "INSERT INTO public.cfield(fname, relid, ftype, decimalplaces, fileoperation, instrumented) VALUES (?, ?, ?, ?, ?, ?)";
                        query = db.prepQuery(q);
                        query.setParString(1, f.getFname());
                        query.setParInt(2, r.getRelid());
                        f.setRelid(r.getRelid());
                        query.setParString(3, f.getFtype());
                        query.setParInt(4, f.getDecimalplacesUp());
                        query.setParString(5, f.getFileoperationSQL());
                        query.setParString(6, f.getInstrumentedSQL());
                        query.executeUpdate();
                    }
                }
            }
            this.setDependencyRelations();
            for (Activity a : updateActivities) {
                q = "UPDATE public.cactivity SET atype=?, description=?, templatedir=?, activation=?, extractor=?, constrained=? WHERE tag=?";
                query = db.prepQuery(q);
                query.setParString(1, a.getAtype());
                query.setParString(2, a.getDescription());
                query.setParString(3, a.getTemplatedirSQL());
                query.setParString(4, a.getActivation());
                query.setParString(5, a.getExtractorSQL());
                query.setParString(6, a.getConstrainedSQL());
                query.setParString(7, a.getTag());
                query.executeUpdate();
            }
        }

        //UPDATE RELATIONS
        this.setActivitiesIds();
        this.checkSameRelations();
        this.checkUpdateRelations();
        this.checkNewRelations();
        if (!updateRelations.isEmpty() || !newRelations.isEmpty()) {
            System.out.println("There are modifications on Relations, do you want to update?(y/n)");
            wish3 = scanner.nextLine();
        }
        this.setRelationsIds();
        this.setXmlRelations();
        this.setDependencyRelationsUpdated();
        if (wish3.equalsIgnoreCase("y")) {
            for (Relation r : newRelations) {
                for (Relation rel : this.dataBaseRelations) {
                    if (r.getRtype().equalsIgnoreCase(rel.getRtype())) {
                        while (r.getRname().equals(rel.getRname())) {
                            System.out.println("\nA relation with this name already exists: " + rel.getRname());
                            r.setRname(scanner.nextLine());
                        }
                    }
                }
                if (r.getDependencyUp() != null) {
                    q = "INSERT INTO public.crelation(relid, actid, rtype, rname, dependency) VALUES (?, ?, ?, ?, ?)";
                    query = db.prepQuery(q);
                    query.setParInt(1, r.getRelid());
                    query.setParInt(2, r.getActid());
                    query.setParString(3, r.getRtype().toUpperCase());
                    query.setParString(4, r.getRname());
                    query.setParInt(5, r.getDependencyDb());
                    query.executeUpdate();
                } else {
                    q = "INSERT INTO public.crelation(relid, actid, rtype, rname) VALUES (?, ?, ?, ?)";
                    query = db.prepQuery(q);
                    query.setParInt(1, r.getRelid());
                    query.setParInt(2, r.getActid());
                    query.setParString(3, r.getRtype().toUpperCase());
                    query.setParString(4, r.getRname());
                    query.executeUpdate();
                }
                for (Field f : r.getFields()) {
                    q = "INSERT INTO public.cfield(fname, relid, ftype, decimalplaces, fileoperation, instrumented) VALUES (?, ?, ?, ?, ?, ?)";
                    query = db.prepQuery(q);
                    query.setParString(1, f.getFname());
                    query.setParInt(2, r.getRelid());
                    f.setRelid(r.getRelid());
                    query.setParString(3, f.getFtype());
                    query.setParInt(4, f.getDecimalplacesUp());
                    query.setParString(5, f.getFileoperationSQL());
                    query.setParString(6, f.getInstrumentedSQL());
                    query.executeUpdate();
                }
            }
            for (Relation r : updateRelations) {
                q = "UPDATE public.crelation SET relid=?, actid=?, rtype=?, rname=?, dependency=? WHERE relid=?";
                query = db.prepQuery(q);
                query.setParInt(1, r.getRelid());
                query.setParInt(2, r.getActid());
                query.setParString(3, r.getRtype().toUpperCase());
                query.setParString(4, r.getRname());
                query.setParInt(5, r.getDependencyDb());
                query.setParInt(6, r.getRelid());
                query.executeUpdate();
            }
        }

        if (wish2.equalsIgnoreCase("y")) {
            for (Activity a : this.newActivities) {
                for (Relation r : a.getRelations()) {
                    if ((r.getRtype().equalsIgnoreCase("input")) && (r.getDependencyUp() == null)) {
                        q = "CREATE SEQUENCE \"" + xml.getTag() + "\"." + r.getRname().toLowerCase() + "_seq";
                        query = db.prepQuery(q);
                        query.executeUpdate();
                    }
                    if ((r.getRtype().equalsIgnoreCase("output")) && (r.getDependencyUp() == null)) {
                        q = "CREATE SEQUENCE \"" + xml.getTag() + "\"." + r.getRname().toLowerCase() + "_seq";
                        query = db.prepQuery(q);
                        query.executeUpdate();
                    }
                }
            }
        }

        if (wish3.equalsIgnoreCase("y")) {
            for (Relation r : this.newRelations) {
                if ((r.getRtype().equalsIgnoreCase("input")) && (r.getDependencyUp() == null)) {
                    q = "CREATE SEQUENCE \"" + xml.getTag() + "\"." + r.getRname().toLowerCase() + "_seq";
                    query = db.prepQuery(q);
                    query.executeUpdate();
                }
                if ((r.getRtype().equalsIgnoreCase("output")) && (r.getDependencyUp() == null)) {
                    q = "CREATE SEQUENCE \"" + xml.getTag() + "\"." + r.getRname().toLowerCase() + "_seq";
                    query = db.prepQuery(q);
                    query.executeUpdate();
                }
            }
        }

        if (wish2.equalsIgnoreCase("y")) {
            for (Activity a : this.newActivities) {
                for (Relation r : a.getRelations()) {
                    q = r.getSQLCreateStatement();
                    query = db.prepQuery(q);
                    query.executeUpdate();
                    q = "GRANT ALL ON SCHEMA \"" + xml.getTag() + "\" TO public;";
                    query = db.prepQuery(q);
                    query.executeUpdate();
                }
            }
        }

        if (wish3.equalsIgnoreCase("y")) {
            this.setDataBaseRelations();
            for (Relation rela : this.dataBaseRelations) {
                for (Relation r : this.newRelations) {
                    for (Relation rel : this.dataBaseRelations) {
                        if (r.getRname().equalsIgnoreCase(rel.getRname())) {
                            while (r.getRname().equalsIgnoreCase(rel.getRname())) {
                                r.setRname(scanner.nextLine());
                            }
                        }
                    }
                }
            }
            for (Relation r : this.newRelations) {
                q = r.getSQLCreateStatement();
                query = db.prepQuery(q);
                query.executeUpdate();
                q = "GRANT ALL ON SCHEMA \"" + xml.getTag() + "\" TO public;";
                query = db.prepQuery(q);
                query.executeUpdate();
            }
        }

        if (wish2.equalsIgnoreCase("y")) {
            for (Activity a : this.newActivities) {
                for (Relation r : a.getRelations()) {
                    if (r.getRtype().equalsIgnoreCase("input")) {
                        if (r.getDependencyUp() == null) {
                            q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + r.getRname() + "\" ADD CONSTRAINT " + r.getRname() + "_pkey PRIMARY KEY (ewkfid, ik)";
                            query = db.prepQuery(q);
                            query.executeUpdate();
                        } else {
                            q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + r.getRname() + "\" ADD CONSTRAINT " + r.getRname() + "_pkey PRIMARY KEY (ewkfid, ik)";
                            query = db.prepQuery(q);
                            query.executeUpdate();
                            for (Activity act : xml.getActivities()) {
                                if (r.getDependencyUp().equalsIgnoreCase(act.getTag())) {
                                    for (Relation rel : act.getRelations()) {
                                        if (rel.getRtype().equalsIgnoreCase("output")) {
                                            q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + r.getRname() + "\" ADD CONSTRAINT \"" + r.getRname() + "_fk\" FOREIGN KEY (ewkfid, ik) REFERENCES \"" + rel.getRname() + "\" (ewkfid, ok) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE";
                                            query = db.prepQuery(q);
                                            query.executeUpdate();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (r.getRtype().equalsIgnoreCase("output")) {
                        q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + r.getRname() + "\" ADD CONSTRAINT " + r.getRname() + "_pkey PRIMARY KEY (ewkfid, ok)";
                        query = db.prepQuery(q);
                        query.executeUpdate();
                        for (Relation rel : a.getRelations()) {
                            if (rel.getRtype().equalsIgnoreCase("input")) {
                                q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + r.getRname() + "\" ADD CONSTRAINT \"" + r.getRname() + "_fk\" FOREIGN KEY (ewkfid, ik) REFERENCES \"" + rel.getRname() + "\" (ewkfid, ik) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE";
                                query = db.prepQuery(q);
                                query.executeUpdate();
                            }
                        }
                    }
                }
            }
        }

        if (wish3.equalsIgnoreCase("y")) {
            for (Relation r : this.newRelations) {
                if (r.getRtype().equalsIgnoreCase("input")) {
                    if (r.getDependencyUp() == null) {
                        q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + r.getRname() + "\" ADD CONSTRAINT " + r.getRname() + "_pkey PRIMARY KEY (ewkfid, ik)";
                        query = db.prepQuery(q);
                        query.executeUpdate();
                    } else {
                        q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + r.getRname() + "\" ADD CONSTRAINT " + r.getRname() + "_pkey PRIMARY KEY (ewkfid, ik)";
                        query = db.prepQuery(q);
                        query.executeUpdate();
                        for (Activity act : xml.getActivities()) {
                            if (r.getDependencyUp().equalsIgnoreCase(act.getTag())) {
                                for (Relation rel : act.getRelations()) {
                                    if (rel.getRtype().equalsIgnoreCase("output")) {
                                        q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + r.getRname() + "\" ADD CONSTRAINT \"" + r.getRname() + "_fk\" FOREIGN KEY (ewkfid, ik) REFERENCES \"" + xml.getTag() + "\".\"" + rel.getRname() + "\" (ewkfid, ok) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE NOT VALID";
                                        query = db.prepQuery(q);
                                        query.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
                if (r.getRtype().equalsIgnoreCase("output")) {
                    q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + r.getRname() + "\" ADD CONSTRAINT " + r.getRname() + "_pkey PRIMARY KEY (ewkfid, ok)";
                    query = db.prepQuery(q);
                    query.executeUpdate();
                    for (Activity a : xml.getActivities()) {
                        if (a.getActid() == r.getActid()) {
                            for (Relation rel : a.getRelations()) {
                                if (rel.getRtype().equalsIgnoreCase("input")) {
                                    q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + r.getRname() + "\" ADD CONSTRAINT \"" + r.getRname() + "_fk\" FOREIGN KEY (ewkfid, ik) REFERENCES \"" + xml.getTag() + "\".\"" + rel.getRname() + "\" (ewkfid, ik) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE";
                                    query = db.prepQuery(q);
                                    query.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
            for (Relation r : this.updateRelations) {
                for (Activity act : xml.getActivities()) {
                    if (r.getDependencyUp().equalsIgnoreCase(act.getTag())) {
                        for (Relation rel : act.getRelations()) {
                            if (rel.getRtype().equalsIgnoreCase("output")) {
                                q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + r.getRname() + "\" DROP CONSTRAINT \"" + r.getRname() + "_fk\"";
                                query = db.prepQuery(q);
                                query.executeUpdate();
                                q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + r.getRname() + "\" ADD CONSTRAINT \"" + r.getRname() + "_fk\" FOREIGN KEY (ewkfid, ik) REFERENCES \"" + xml.getTag() + "\".\"" + rel.getRname() + "\" (ewkfid, ik) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE NOT VALID";
                                query = db.prepQuery(q);
                                query.executeUpdate();
                            }
                        }
                    }
                }
            }
            for (Relation r : newRelations) {
                if (r.getRtype().equalsIgnoreCase("output")) {
                    for (Activity a : xml.getActivities()) {
                        if (a.getActid() == r.getActid()) {
                            for (Activity act : xml.getActivities()) {
                                for (Relation rel : act.getRelations()) {
                                    if ((rel.getRtype().equalsIgnoreCase("input")) && (rel.getDependencyUp() != null)) {
                                        if (rel.getDependencyUp().equalsIgnoreCase(a.getTag())) {
                                            q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + rel.getRname() + "\" DROP CONSTRAINT \"" + rel.getRname() + "_fk\"";
                                            query = db.prepQuery(q);
                                            query.executeUpdate();
                                            q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + rel.getRname() + "\" ADD CONSTRAINT \"" + rel.getRname() + "_fk\" FOREIGN KEY (ewkfid, ik) REFERENCES \"" + xml.getTag() + "\".\"" + r.getRname() + "\" (ewkfid, ok) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE NOT VALID";
                                            query = db.prepQuery(q);
                                            query.executeUpdate();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (wish3.equalsIgnoreCase("y")) {
                for (Relation r : newRelations) {
                    if (r.getRtype().equalsIgnoreCase("input")) {
                        for (Activity a : this.xml.getActivities()) {
                            for (Relation rel : a.getInput()) {
                                if (rel.getRname().equalsIgnoreCase(r.getRname())) {
                                    for (Relation rela : a.getOutput()) {
                                        q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + rela.getRname() + "\" DROP CONSTRAINT \"" + rela.getRname() + "_fk\"";
                                        query = db.prepQuery(q);
                                        query.executeUpdate();
                                        q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + rela.getRname() + "\" ADD CONSTRAINT \"" + rela.getRname() + "_fk\" FOREIGN KEY (ewkfid, ik) REFERENCES \"" + xml.getTag() + "\".\"" + r.getRname() + "\" (ewkfid, ik) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE NOT VALID";
                                        query = db.prepQuery(q);
                                        query.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //UPDATE FIELDS
        this.setFieldsIds();
        this.checkSameFields();
        this.checkUpdateFields();
        this.checkNewFields();
        this.checkDeleteFields();
        if (!updateFields.isEmpty() || !newFields.isEmpty() || !deleteFields.isEmpty()) {
            System.out.println("There are modifications on Fields, do you want to update?(y/n)");
            wish4 = scanner.nextLine();
        }
        if (wish4.equalsIgnoreCase("y")) {
            for (Field f : newFields) {
                q = "INSERT INTO public.cfield(fname, relid, ftype, decimalplaces, fileoperation, instrumented) VALUES (?, ?, ?, ?, ?, ?)";
                query = db.prepQuery(q);
                query.setParString(1, f.getFname());
                query.setParInt(2, f.getRelid());
                query.setParString(3, f.getFtype());
                query.setParInt(4, f.getDecimalplacesUp());
                query.setParString(5, f.getFileoperationSQL());
                query.setParString(6, f.getInstrumentedSQL());
                query.executeUpdate();
                String rname = null;
                for (Relation r : this.xmlRelations) {
                    if (f.getRelid() == r.getRelid()) {
                        rname = r.getRname();
                    }
                }
                q = "ALTER TABLE \"" + xml.getTag() + "\".\"" + rname + "\" ADD COLUMN " + f.getFname() + " " + f.getFtypetoSQL();
                query = db.prepQuery(q);
                query.executeUpdate();
            }
            for (Field f : updateFields) {
                q = "UPDATE public.cfield SET fname=?, relid=?, ftype=?, decimalplaces=?, fileoperation=?, instrumented=? WHERE fname=? AND relid=?";
                query = db.prepQuery(q);
                query.setParString(1, f.getFname());
                query.setParInt(2, f.getRelid());
                query.setParString(3, f.getFtype());
                query.setParInt(4, f.getDecimalplacesUp());
                query.setParString(5, f.getFileoperationSQL());
                query.setParString(6, f.getInstrumentedSQL());
                query.setParString(7, f.getFname().toUpperCase());
                query.setParInt(8, f.getRelid());
                query.executeUpdate();
            }
            for (Field f : deleteFields) {
                q = "DELETE FROM public.cfield WHERE fname=? AND relid=?";
                query = db.prepQuery(q);
                query.setParString(1, f.getFname());
                query.setParInt(2, f.getRelid());
                query.executeUpdate();
                for (Relation r : this.xmlRelations) {
                    if (r.getRelid() == f.getRelid()) {
                        q = "ALTER TABLE \"" + xml.getTag() + "\"." + r.getRname() + " DROP COLUMN " + f.getFname();
                        query = db.prepQuery(q);
                        query.executeUpdate();
                    }
                }
            }
        }
        if ((wish.equalsIgnoreCase("n")) && (wish2.equalsIgnoreCase("n")) && (wish3.equalsIgnoreCase("n")) && (wish4.equalsIgnoreCase("n"))) {
            System.out.println("NOTHING TO UPDATE");
        }
    }
}