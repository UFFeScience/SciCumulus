package chiron.concept;

import chiron.*;
import java.io.File;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * TO DO: Reduce Operator still needs to be implemented.
 *
 * @author jonasdias, vitor
 */
class Reduce extends CActivity {

    CRelation input;
    CRelation output;
    ArrayList<String> aggregationFields;
    
    static class ReduceActivation extends CActivation implements Serializable {

        public ReduceActivation(String wfDir, String expDir) {
            super(wfDir, expDir);
        }

        @Override
        @SuppressWarnings("CallToThreadDumpStack")
        public void instrument(EActivation activation) throws Exception {
            activation.stdErr = "";
            activation.stdOut = "";
            try {
                ChironUtils.createDirectory(activation.workspace);

                activation.templateDir = processTags(activation.templateDir, wfDir, expDir, activation.inputRelation.getFirst());
                if (activation.templateDir != null && !activation.templateDir.equals("")) {
                    ChironUtils.copyTemplateFiles(activation.templateDir, activation.workspace);
                    
                    File[] oFiles = new File(activation.templateDir).listFiles();
                    for(File file : oFiles){
                        if(file.getName().charAt(0) != '.'){
                            String instrumentFile = activation.workspace + "/" + file.getName();
                            File iFile = new File(instrumentFile);
                            processTags(iFile, wfDir, expDir, activation.inputRelation.getFirst());
                        }
                    }
                }

                manipulateFile(activation.files, activation.workspace);
                activation.commandLine = processTags(activation.commandLine, wfDir, expDir, activation.inputRelation.getFirst());
                if (activation.extractor != null) {
                    activation.extractor = processTags(activation.extractor, wfDir, expDir, activation.inputRelation.getFirst());
                }
            } catch (Exception ex) {
                activation.stdErr += ex.getStackTrace();
                ex.printStackTrace();
            }
        }

        @Override
        @SuppressWarnings("CallToThreadDumpStack")
        public void execute(EActivation activation) throws Exception {
            String workspace = activation.workspace;
            String command = activation.commandLine + " > " + workspace + ChironUtils.resultFile + " 2> " + workspace + ChironUtils.errorFile;
            try {
                activation.exitStatus = ChironUtils.runCommand(command, workspace);
                activation.stdErr += ChironUtils.ReadFile(workspace + ChironUtils.errorFile);
                activation.stdOut += ChironUtils.ReadFile(workspace + ChironUtils.resultFile);
            } catch (Exception ex) {
                activation.stdErr += ex.getStackTrace();
                ex.printStackTrace();
            }
        }

        @Override
        @SuppressWarnings("CallToThreadDumpStack")
        public void extract(EActivation activation) throws Exception {
            try {
                Scanner s = this.runExtractor(activation);

                String line = s.nextLine();
                String fields[] = line.split(ChironUtils.relationSeparator);
                line = s.nextLine();
                String values[] = line.split(ChironUtils.relationSeparator);
                if (fields.length != values.length) {
                    throw new Exception("The number of extracted values do not "
                            + "correspond to the number of extracted fields. "
                            + "Check the ERelation.txt extracted file.");
                }
                //extractedTuple stores the values extracted to ERelation.txt
                HashMap<String, String> extractedTuple = new HashMap<String, String>();
                for (int i = 0; i < fields.length; i++) {
                    extractedTuple.put(fields[i], values[i]);
                }

                //outputTuple stores the tuple of the output Relation
                LinkedHashMap<String, String> outputTuple = new LinkedHashMap<String, String>();
                for (String field : activation.outputRelation.fields) {
                    if (extractedTuple.containsKey(field)) {
                        outputTuple.put(field, extractedTuple.get(field));
                    } else if (activation.inputRelation.getFirst().containsKey(field.toLowerCase())) {
                        outputTuple.put(field, activation.inputRelation.getFirst().get(field.toLowerCase()));
                    } else {
                        throw new Exception("The value for the " + field + " is missing.");
                    }
                }
                for (EFile file : activation.files) {
                    if (outputTuple.containsKey(file.fieldName.toUpperCase())) {
                        if (file.getFileName() == null) {
                            file.setFileName(outputTuple.get(file.fieldName.toUpperCase()));
                        }
                        file.setFileDir(activation.workspace);
                        outputTuple.put(file.fieldName.toUpperCase(), file.getPath());
                    }//else{
                    //  outputTuple.put(file.fieldName, file.getPath());
                    //}
                }
                activation.outputRelation = new ERelation(activation.outputRelation.name, activation.inputRelation.getFirstKey(), outputTuple);
            } catch (Exception ex) {
                activation.stdErr += "Extractor Error:" + ex.getStackTrace();
                ex.printStackTrace();
            }
        }

        @Override
        public void pipelineData(EActivation activation) {
            HashMap<String, String> pipedOutput = activation.pipelinedFrom.outputRelation.getFirst();
            Integer k = activation.pipelinedFrom.outputRelation.getFirstKey();
            Integer newKey = k;
            HashMap<String, String> inputTuple = new HashMap<String, String>();
            for (String field : activation.inputRelation.fields) {
                inputTuple.put(field, pipedOutput.get(field));
                EFile f = activation.getFile(field);
                if (f != null) {
                    f.setFileName(pipedOutput.get(field));
                }
            }
            activation.inputRelation = new ERelation(activation.inputRelation.name, newKey, inputTuple);
        }
    }

    public Reduce() {
        this(Operator.REDUCE);
    }

    private Reduce(Operator type) {
        super(type);
    }

    @Override
    public void addInput(CRelation relations) {
        input = relations;
    }

    @Override
    public void addOutput(CRelation relations) {
        output = relations;
    }

    @Override
    public List<CRelation> getInputRelations() {
        List<CRelation> ret = new ArrayList<CRelation>();
        ret.add(input);
        return ret;
    }

    @Override
    public List<CRelation> getOutputRelations() {
        List<CRelation> ret = new ArrayList<CRelation>();
        ret.add(output);
        return ret;
    }

    @Override
    public void generateActivations(EActivity act, String wfDir, String expDir) throws Exception {
        if (input == null) {
            //something is wrong
            throw new NullPointerException("The input relation for activity" + act.tag + " is not available in the list of relations.");
        } else {
            checkDependencies(input);

            String folderActivity = expDir + this.tag + ChironUtils.SEPARATOR;
            ChironUtils.createDirectory(folderActivity);
            aggregationFields = EProvenance.getTextOperand(this.id);
            
            ResultSet rs = EProvenance.loadOrderedParameterSpace(input, aggregationFields);
            int numActivations = 0;

            ArrayList<String> lastValues = new ArrayList<String>();
            ERelation inputRelation = new ERelation(input.name, input.fields);
            List<EFile> activationFiles = new ArrayList<EFile>();
            boolean first = true;
            while (rs.next()) {
                
                ArrayList<String> values = new ArrayList<String>();
                for(String af : aggregationFields){
                    values.add(String.valueOf(rs.getObject(af)));
                }
                
                if (first) {
                    lastValues = new ArrayList<String>();
                    for(String v : values){
                        lastValues.add(v);
                    }
                    
                    first = false;
                }
                
                if (checkAggregationValues(values,lastValues)) {
                    int ik = rs.getInt("ik");
                    List<EFile> fileFields = EProvenance.getFileFields(this.getInputRelations().get(0));

                    ResultSet relation = EProvenance.loadOrderedParameterSpace(inputRelation.name, ik, aggregationFields);
                    List<String> fields = EProvenance.retrieveFields(inputRelation.name);
                    while (relation.next()) {
                        Integer k = new Integer(relation.getInt("ik"));

                        String[] fValues = new String[fields.size()];
                        for (int i = 0; i < fields.size(); i++) {
                            String f = fields.get(i);
                            Object o = relation.getObject(f);
                            if (o.getClass().equals(Double.class)) {
                                int decimalPlaces = EProvenance.getDecimalPlaces(f, act.operation.getInputRelations().get(0));
                                String d = ChironUtils.formatFloat((Double) o, decimalPlaces);
                                fValues[i] = d;
                            } else {
                                fValues[i] = String.valueOf(relation.getObject(f));
                            }
                        }
                        inputRelation.values.put(k, fValues);
                    }

                    //CActivity dependency = input.dependency;
                    for (EFile f : fileFields) {
                        EFile file = new EFile(f.instrumented, f.fieldName.toUpperCase(), f.fileOper);
                        file.setFileName(rs.getString(file.fieldName));
//                        if (dependency != null){
//                            String depFolder = expDir + dependency.tag + ChironUtils.SEPARATOR;
//                            String depActivationFolder = ChironUtils.getActivationFolder(numActivations, depFolder);
//                            f.setFileDir(depActivationFolder);
//                        }else if (!new File(file.getPath()).exists()) {
//                            file.setFileDir(expDir + "input/");
//                        }

                        activationFiles.add(file);
                    }
                } else {
                    //store activation
                    String activationFolder = ChironUtils.getActivationFolder(numActivations, folderActivity);
                    EActivation newActivation = this.createActivation(act, activationFolder);
                    newActivation.files = activationFiles;

                    ERelation outputRel = new ERelation(output.name, output.fields);
                    newActivation.outputRelation = outputRel;
                    List<EFile> outputfileFields = EProvenance.getFileFields(this.getOutputRelations().get(0));
                    for (EFile f : outputfileFields) {
                        if (!newActivation.hasFile(f.fieldName)) {
                            f.setFileDir(newActivation.workspace);
                            newActivation.files.add(f);
                        }
                    }
                    newActivation.inputRelation = inputRelation;

                    EProvenance.storeActivation(newActivation);
                    numActivations++;
                    ChironUtils.createDirectory(newActivation.workspace);
                    ChironUtils.deleteFile(ChironUtils.relationFile, newActivation.workspace);
                    String csv = newActivation.inputRelation.getCSVHeader() + newActivation.inputRelation.getCSV();
                    ChironUtils.WriteFile(newActivation.workspace + newActivation.inputRelation.name + ".hfrag", csv);

                    //refresh loop
                    inputRelation = new ERelation(input.name, input.fields);
                    activationFiles = new ArrayList<EFile>();

                    int ik = rs.getInt("ik");
                    ResultSet relation = EProvenance.loadOrderedParameterSpace(inputRelation.name, ik, aggregationFields);
                    List<String> fields = EProvenance.retrieveFields(inputRelation.name);
                    while (relation.next()) {
                        Integer k = new Integer(relation.getInt("ik"));
                        String[] fValues = new String[fields.size()];
                        for (int i = 0; i < fields.size(); i++) {
                            String f = fields.get(i);
                            Object o = relation.getObject(f);
                            if(o != null){
                                if (o.getClass().equals(Double.class)) {
                                    int decimalPlaces = EProvenance.getDecimalPlaces(f, act.operation.getInputRelations().get(0));
                                    String d = ChironUtils.formatFloat((Double) o, decimalPlaces);
                                    fValues[i] = d;
                                } else {
                                    fValues[i] = String.valueOf(relation.getObject(f));
                                }
                            }else{
                                fValues[i] = "";
                            }
                        }
                        inputRelation.values.put(k, fValues);
                    }

                    lastValues = new ArrayList<String>();
                    for(String v : values){
                        lastValues.add(v);
                    }
                }
            }

            //Insert the last value
            if (!lastValues.isEmpty()) {
                String activationFolder = ChironUtils.getActivationFolder(numActivations, folderActivity);
                EActivation newActivation = this.createActivation(act, activationFolder);
                newActivation.inputRelation = inputRelation;
                newActivation.files = activationFiles;

                ERelation outputRel = new ERelation(output.name, output.fields);
                newActivation.outputRelation = outputRel;
                List<EFile> outputfileFields = EProvenance.getFileFields(this.getOutputRelations().get(0));
                for (EFile f : outputfileFields) {
                    if (!newActivation.hasFile(f.fieldName)) {
                        f.setFileDir(newActivation.workspace);
                        newActivation.files.add(f);
                    }
                }
                EProvenance.storeActivation(newActivation);
                numActivations++;
                ChironUtils.createDirectory(newActivation.workspace);
                ChironUtils.deleteFile(ChironUtils.relationFile, newActivation.workspace);
                String csv = newActivation.inputRelation.getCSVHeader() + newActivation.inputRelation.getCSV();
                ChironUtils.WriteFile(newActivation.workspace + newActivation.inputRelation.name + ".hfrag", csv);
            }

            act.status = EActivity.StatusType.RUNNING;
            act.numActivations = numActivations;
            EProvenance.storeActivity(act);
        }
    }

    @Override
    public EActivation createPipelineActivation(String expDir, String wfDir, EActivity act, EActivity dep, EActivation activation) {
        String folderActivation = activation.workspace.replace(act.tag, dep.tag);
        ChironUtils.createDirectory(expDir + dep.operation.tag);

        EActivation depActivation = this.createActivation(dep, folderActivation);

        depActivation.inputRelation = new ERelation(this.input.name, this.input.fields);
        depActivation.outputRelation = new ERelation(this.output.name, this.output.fields);
        try {
            List<EFile> fileFields = EProvenance.getFileFields(this.getInputRelations().get(0));
            for (EFile f : fileFields) {
                depActivation.files.add(f);
            }
            fileFields = EProvenance.getFileFields(this.getOutputRelations().get(0));
            for (EFile f : fileFields) {
                if (!depActivation.hasFile(f.fieldName)) {
                    depActivation.files.add(f);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        depActivation.activator = CActivation.newInstance(act);
        return depActivation;
    }
    
    private boolean checkAggregationValues(ArrayList<String> values, ArrayList<String> lastValues) {
        for(int i=0; i<values.size(); i++){
            if(!values.get(i).equals(lastValues.get(i))){
                return false;
            }
        }
        
        return true;
    }

}
