package chiron.concept;

import chiron.*;
import java.io.File;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author jonasdias
 */
public class Map extends CActivity {

    CRelation input;
    CRelation output;

    protected Map(Operator type) {
        super(type);
    }

    public Map() {
        this(Operator.MAP);
    }

    public static class MapActivation extends CActivation implements Serializable {

        MapActivation(String wfDir, String expDir) {
            super(wfDir, expDir);
        }

        @Override
        @SuppressWarnings("CallToThreadDumpStack")
        public void instrument(EActivation activation) throws Exception {
            activation.stdErr = "";
            activation.stdOut = "";
            try {
                ChironUtils.createDirectory(activation.workspace);

                HashMap<String, String> tuple = activation.inputRelation.getFirst();
                manipulateFile(activation.files, activation.workspace);
                for (EFile file : activation.files) {
                    tuple.put(file.fieldName, file.getPath());
                }

                activation.templateDir = processTags(activation.templateDir, wfDir, expDir, tuple);
                if (activation.templateDir != null && !activation.templateDir.equals("")) {
                    ChironUtils.copyTemplateFiles(activation.templateDir, activation.workspace);
                    
                    File[] oFiles = new File(activation.templateDir).listFiles();
                    for(File file : oFiles){
                        if(file.getName().charAt(0) != '.'){
                            String instrumentFile = activation.workspace + "/" + file.getName();
                            File iFile = new File(instrumentFile);
                            processTags(iFile, wfDir, expDir, tuple);
                        }
                    }
                }

                instrumentFiles(activation.files, wfDir, tuple);
                activation.commandLine = processTags(activation.commandLine, wfDir, expDir, tuple);
                if (activation.extractor != null) {
                    activation.extractor = processTags(activation.extractor, wfDir, expDir, tuple);
                }
                ChironUtils.deleteFile(ChironUtils.relationFile, activation.workspace);
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
                field = field.toUpperCase();
                if (extractedTuple.containsKey(field)) {
                    outputTuple.put(field, extractedTuple.get(field));
                } else if (activation.inputRelation.getFirst().containsKey(field)) {
                    outputTuple.put(field, activation.inputRelation.getFirst().get(field));
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
                } //else if (activation.outputRelation.fields){
                  //  outputTuple.put(file.fieldName, file.getPath());
                //}
            }
            activation.outputRelation = new ERelation(activation.outputRelation.name, activation.inputRelation.getFirstKey(), outputTuple);
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
        List ret = new ArrayList();
        ret.add(input);
        return ret;
    }

    @Override
    public List<CRelation> getOutputRelations() {
        List ret = new ArrayList();
        ret.add(output);
        return ret;
    }

    @Override
    @SuppressWarnings("CallToThreadDumpStack")
    public void generateActivations(EActivity act, String wfDir, String expDir) throws Exception {
        if (input == null) {
            //something is wrong
            throw new NullPointerException("The input relation for activity" + act.tag + " is not available in the list of relations.");
        } else {
            checkDependencies(input);
            ResultSet rs = EProvenance.loadParameterSpace(input);
            String folderActivity = expDir + this.tag + ChironUtils.SEPARATOR;
            ChironUtils.createDirectory(folderActivity);
            int numActivations = 0;

            while (rs.next()) {
                //this new ChironUtils methods can be reimplemented to support a large number of folders creation. For now
                //it is still simple, creating a folder for each activation in the activity directory.
                String activationFolder = ChironUtils.getActivationFolder(numActivations, folderActivity);
                EActivation newActivation = this.createActivation(act, activationFolder);

                newActivation.inputRelation = new ERelation(this.input.name, this.input.fields);//inputRel;
                newActivation.inputRelation.putKey(rs.getInt("ik"));

                List<EFile> fileFields = EProvenance.getFileFields(this.getInputRelations().get(0));

                //CActivity dependency = input.dependency;
                for (EFile f : fileFields) {
                    f.setFileName(rs.getString(f.fieldName));
                    
                    ChironUtils.manipulateFile(f, newActivation.workspace);
                    
                    f.setFileDir(newActivation.workspace);
                    //if (dependency != null){
                    //String depFolder = expDir + dependency.tag + ChironUtils.SEPARATOR;
                    //String depActivationFolder = ChironUtils.getActivationFolder(numActivations, depFolder);
                    //f.setFileDir(depActivationFolder);
                    //}else if(!new File(f.getPath()).exists()){
                    //    f.setFileDir(expDir + "input/");
                    //}
                    newActivation.files.add(f);
                }

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
            }
            act.status = EActivity.StatusType.RUNNING;
            act.numActivations = numActivations;
            EProvenance.storeActivity(act);
        }
    }

    @Override
    @SuppressWarnings("CallToThreadDumpStack")
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
}
