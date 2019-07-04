package adaptability;

import ch.ethz.ssh2.Connection;
import java.io.FileOutputStream;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import environment.AmazonMachine;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import main.SCProvenance;
import workflow.ActivityStatistics;
import main.SCUtils;
import main.XMLReader;

/**
 *
 * @author Daniel
 */
public class Dimensioner {

    String DB_URL;
    java.sql.Connection conection;
    String macServer;
    String macUser;
    String macPassword;
    boolean verbose;
    
//    constraints
    double maxTime;
    double maxFinancialCost;
    int maxVMAmount;
    double totalRAM;
    double totaldisk;
    double alfa1, alfa2, alfa3;
    
//    calculating dimensioning
    List<VirtualMachineType> vmTypes;
    String workflowTag;
    String workflowExecTag;
    String absolutePath;
    
    public Dimensioner(XMLReader config, AmazonMachine controlNode) throws SQLException {
        this.DB_URL = "jdbc:postgresql://" + config.getDbServer() + ":" + config.getDbPort() + "/" + config.getDbName() + "?chartset=UTF8";
        this.macUser = config.getMacUser();
        this.macPassword = config.getMacPassword();
        this.macServer = controlNode.publicDNS;
        this.maxTime = config.getMaxTime();
        this.maxFinancialCost = config.getMaxFinancialCost();
        this.maxVMAmount = config.getMaxVMAmount();
        this.totalRAM = config.getTotalRAM();
        this.totaldisk = config.getTotalDisk();
        this.alfa1 = config.getAlfa1();
        this.alfa2 = config.getAlfa2();
        this.alfa3 = config.getAlfa3();
        this.vmTypes = config.vmTypes;
        this.workflowTag = config.wkfTag;
        this.workflowExecTag = config.wkfExecTag;
        this.absolutePath = config.absolutePath;
        this.verbose = config.verbose;
        
        try {
            Class.forName(SCUtils.JDBC_DRIVER).newInstance();
            conection = DriverManager.getConnection(DB_URL, config.getDbUser(), config.getDbPassword());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public List<VirtualMachineType> calculateVMDimensioning() throws IOException, InterruptedException, SQLException {
        SCUtils.printFirstLevel("Dimensioning cloud environment...", verbose);
        
        int num;
        double gflop = 0.00;
        List<String> firstActTags = SCProvenance.getRunningActivities(workflowTag);
        if(firstActTags.isEmpty()){
            firstActTags = SCProvenance.getFirstActivities(workflowTag);
        }
        
        for(String act : firstActTags){
            num = calculateNumberOfActivationsFromActivity(workflowTag, workflowExecTag, act);
            List<ActivityStatistics> cloudActivities = calculateAverageExecutionTime(workflowTag, act);
            gflop += calculateGFlops(cloudActivities, num);
        }
        
        double maxExecutionTime = maxTime * 60;
        maxExecutionTime -= (SCProvenance.getWorkflowElapsedTime(workflowExecTag) * 60);
        if(maxExecutionTime < 0){
            maxExecutionTime = 0;
        }
        
        //first act tag
        String fileName = generateDimensioningFile(vmTypes, firstActTags.get(0), gflop, maxExecutionTime);

        // Connect with the remote dim server      
        Connection conn = new ch.ethz.ssh2.Connection(this.macServer, 22);
        conn.connect();
        
        boolean connected = conn.authenticateWithPassword(this.macUser, this.macPassword);
        
        List<VirtualMachineType> updatedVM = new ArrayList<VirtualMachineType>();
        if (connected) {
            String controlFileName = "run_Grasp.sh";
            String outputFileName = generateControlFile(controlFileName, firstActTags.get(0));

            SCUtils.runCommand("mkdir GraspCC-Heuristic", ".");
            SCUtils.runCommand("mkdir GraspCC-Heuristic/inst", ".");
            SCUtils.runCommand("mkdir GraspCC-Heuristic/saida", ".");
            SCUtils.runCommand("cp " + fileName + " GraspCC-Heuristic/inst/", ".");
            SCUtils.runCommand("chmod 777 " + controlFileName, ".");
            
            String suffix = "";
            if(!SCUtils.isWindows()){
                suffix = "./";
            }
            SCUtils.runCommand(suffix + controlFileName, ".");
            
            //remote
//            Session sess = conn.openSession();
//            String line = "mkdir ~/GraspCC-Heuristic/inst";
//            sess.execCommand(line);
//            
//            sess = conn.openSession();
//            line = "mkdir ~/GraspCC-Heuristic/saida";
//            sess.execCommand(line);
//            
//            SCPClient scp = new SCPClient(conn);
//            scp.put("GraspCC-Heuristic/inst/" + fileName, "~/GraspCC-Heuristic/inst/");
//
//            scp = new SCPClient(conn);
//            scp.put(controlFileName, "~");
//
//            sess = conn.openSession();
//            line = "chmod 777 " + controlFileName;
//            sess.execCommand(line);
//
//            // remotely unzip the compressed workspace
//            sess = conn.openSession();
//            line = "./" + controlFileName;
//            sess.execCommand(line);
//            sess.close();
//
//            File outputFile = new File(outputFileName);
//            OutputStream fop = new FileOutputStream(outputFile);
//
//            scp = new SCPClient(conn);
//            scp.get("~/GraspCC-Heuristic/saida/" + outputFileName, fop);
//            fop.flush();
//            fop.close();
//            BufferedReader buffRead = new BufferedReader(new FileReader(outputFileName));

            BufferedReader buffRead = new BufferedReader(new FileReader("GraspCC-Heuristic/saida/" + outputFileName));
            
            int existeVM = 0;
            String linha = "";
            
            int contadorVMType = 0;
            int contadorVM = 0;
            while (true) {
                if (linha != null) {
                    if (linha.contains("Package [")) {      
                        if (existeVM > 0) {
                            VirtualMachineType vm = vmTypes.get(contadorVMType);
                            vm.setAmountInstantiatedVM(contadorVM);
                            updatedVM.add(vm);
                            contadorVM = 0;
                            contadorVMType++;
                        }
                        existeVM++;
                    } else {
                        if (linha.contains("Function cost")) {
                            VirtualMachineType vm = vmTypes.get(contadorVMType);
                            vm.setAmountInstantiatedVM(contadorVM);
                            updatedVM.add(vm);
                            contadorVM = 0;
                        } else {
                            if (existeVM > 0) {
                                contadorVM++;
                            }
                        }
                    }
                } else {
                    break;
                }
                linha = buffRead.readLine();
            }
            buffRead.close();
            
            SCUtils.deleteFile(outputFileName, absolutePath);
        }

        return updatedVM;
    }

    public int calculateNumberOfActivationsFromActivity(String workflowTag, String workflowExecTag, String actTag) {
        int numberofCloudActivities = 0;
        SCUtils.printSecondLevel("Calculating the amount of cloud activities to process...", verbose);
        String query = "select count(*) as number from "
                + "eactivity a, eactivation ac, eworkflow w\n"
                + "where ac.actid = a.actid\n"
                + "and w.ewkfid = a.wkfid\n"
                + "and a.tag = '" + actTag + "'\n"
                + "and w.tagexec = '" + workflowExecTag + "'\n"
                + "and w.tag = '" + workflowTag + "'\n"
                + "and a.status = 'READY'";
        try {
            Statement st = conection.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                numberofCloudActivities = rs.getInt("number");
                SCUtils.printSecondLevel("The number of cloud activities for activity " + actTag + " is " + numberofCloudActivities + "...", verbose);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return numberofCloudActivities;
    }

    public List<ActivityStatistics> calculateAverageExecutionTime(String tag, String actTag) {
//        In seconds
        List<ActivityStatistics> cloudActivities = new ArrayList<ActivityStatistics>();
        ActivityStatistics ca;

        SCUtils.printSecondLevel("Calculating the average execution time of activity " + actTag + "...", verbose);
        String query = "select extract(epoch from avg(ac.endtime - ac.starttime)) as media, count(distinct ac.machineid) as NUMBER\n" +
                    "from eactivity a, eworkflow w, eactivation ac\n" +
                    "where w.ewkfid = a.wkfid\n" +
                    "and ac.actid = a.actid\n" +
                    "and a.tag = '" + actTag + "'\n" +
                    "and w.tag = '" + tag + "'\n" +
                    "and ac.endtime IS NOT NULL\n" +
                    "and ac.starttime IS NOT NULL\n" +
                    "order by media desc";
        try {
            Statement st = conection.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                ca = new ActivityStatistics();
                ca.setNumberCores(rs.getInt("NUMBER"));
                ca.setAvgTime(rs.getDouble("MEDIA"));
                cloudActivities.add(ca);
                SCUtils.printSecondLevel("The average execution time of activity " + actTag + " is " + ca.getAvgTime() + " using " + ca.getNumberCores() + " virtual machines...", verbose);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return cloudActivities;
    }

    public double calculateGFlops(List<ActivityStatistics> cas, int num) {
        double gFlopPerProcessor = 19.2;
        double avgGFlop = 0;
        double tempGFlop = 0;
        for (ActivityStatistics ca : cas) {
            tempGFlop = ca.getAvgTime() * gFlopPerProcessor * ca.getNumberCores();
            avgGFlop = avgGFlop + tempGFlop;
        }
        avgGFlop = avgGFlop / cas.size();
        SCUtils.printSecondLevel("Total necessary GFlops: " + avgGFlop, verbose);
        return avgGFlop;

    }

    public String generateControlFile(String controlFileName, String firstActTag) throws IOException, InterruptedException {
        String line = new String();
        String inputFileName = MessageFormat.format("instance_{0}.conf", firstActTag);
        SCUtils.runCommand("mv " + inputFileName + " GraspCC-Heuristic/inst", ".");
        String outputFileName = MessageFormat.format("output_instance_{0}.conf", firstActTag);

        File outputFile = new File(controlFileName);
        outputFile.createNewFile();
        try (BufferedWriter outputBuffer = new BufferedWriter(new FileWriter(outputFile))) {

            line = "cd ~/GraspCC-Heuristic/\n";
            outputBuffer.write(line);

            line = "./Release/GraspCC inst/" + inputFileName + " " + alfa1 + " " + alfa2 + " > saida/" + outputFileName + "\n";
            outputBuffer.write(line);

            outputBuffer.flush();
            outputBuffer.close();
        }
        return outputFileName;
    }

    public String generateDimensioningFile(List<VirtualMachineType> vmTypes, String actTag, double GFlops, double maxExecutionTime) throws IOException {
        //This method creates a file of the type machine.conf on the fileName location
        String lineGflops, lineram, linedisk, lineprice, lineplatform, fileName;
        fileName = MessageFormat.format("instance_{0}.conf", actTag);
        File outputFile = new File(fileName);
        outputFile.createNewFile();
        try (BufferedWriter outputBuffer = new BufferedWriter(new FileWriter(outputFile))) {
            String line = "";
            line = "# Amount of Virtual Machine Types\n";
            outputBuffer.write(line);
            line = MessageFormat.format("package {0}\n", vmTypes.size());
            outputBuffer.write(line);
            line = "# Gflops of each package type\n";
            outputBuffer.write(line);
            lineGflops = "Gflops ";
            linedisk = "disk ";
            lineram = "ram ";
            lineprice = "price ";
            lineplatform = "plataform ";
            for (VirtualMachineType vmType : vmTypes) {
                lineGflops = lineGflops + vmType.getGflops() + " ";
                linedisk = MessageFormat.format("{0}{1} ", linedisk, vmType.getDiskSpace());
                lineram = MessageFormat.format("{0}{1} ", lineram, vmType.getRam());
                lineprice = lineprice + vmType.getFinancialCost() + " ";
                lineplatform = MessageFormat.format("{0}{1} ", lineplatform, vmType.getPlatform());
            }
            lineGflops = MessageFormat.format("{0}\n", lineGflops);
            linedisk = MessageFormat.format("{0}\n", linedisk);
            lineram = MessageFormat.format("{0}\n", lineram);
            lineprice = MessageFormat.format("{0}\n", lineprice);
            lineplatform = MessageFormat.format("{0}\n", lineplatform);
            outputBuffer.write(lineGflops);
            line = "# GBs of RAM of each package type\n";
            outputBuffer.write(line);
            outputBuffer.write(lineram);
            line = "# the plataform type\n";
            outputBuffer.write(line);
            outputBuffer.write(lineplatform);
            line = "# GBs of disk (HD) of each package type\n";
            outputBuffer.write(line);
            outputBuffer.write(linedisk);
            line = "# the price per time unit of each package type\n";
            outputBuffer.write(line);
            outputBuffer.write(lineprice);
            line = "# Scientists' Requirements:\n";
            outputBuffer.write(line);
            line = "# the maximum cost that the scientists can pay\n";
            outputBuffer.write(line);
            line = "C " + maxFinancialCost + "\n";
            outputBuffer.write(line);
            line = "# the minimum Gflops that the client\n";
            outputBuffer.write(line);
            line = "F " + GFlops + "\n";
            outputBuffer.write(line);
            line = "# the total RAM that the client needs\n";
            outputBuffer.write(line);
            line = "M " + totalRAM + "\n";
            outputBuffer.write(line);
            line = "# the total disk that the client needs\n";
            outputBuffer.write(line);
            line = "D " + totaldisk + "\n";
            outputBuffer.write(line);
            line = "# The maximum allowed execution time\n";
            outputBuffer.write(line);
            line = "T " + maxExecutionTime + "\n";
            outputBuffer.write(line);
            line = "# The maximum package number that it can be utilized\n";
            outputBuffer.write(line);
            line = "P " + maxVMAmount + "\n";
            outputBuffer.write(line);
            outputBuffer.flush();
            outputBuffer.close();
            return fileName;
        }
    }
}
