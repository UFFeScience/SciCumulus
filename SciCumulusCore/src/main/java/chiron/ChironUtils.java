package chiron;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import chiron.EFile.Operation;
import chiron.environment.EnvironmentConfiguration;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

/**
 * Classe ChironUtils
 *
 * @author Eduardo, Vítor
 * @since 2010-12-25
 */
public class ChironUtils {
    
    public static String workflowTag = "%=WFDIR%";
    public static String experimentTag = "%=EXPDIR%";
    public static String resultFile = "EResult.txt";
    public static String errorFile = "EErr.txt";
    public static String relationFile = "ERelation.txt";
    public static String relationSeparator = ";";
    public static String failureFile = "FailureReport.txt";
    
    public static final String JDBC_DRIVER = "org.postgresql.Driver";
    private static final String identation = "        ";
    
    protected static boolean verbose = false;
    public static int SLEEP_INTERVAL = 6;
    public static long realiabilityCheckInterval = 30000;
    public static String SEPARATOR = "/";
    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    private static Locale local = new Locale("en");
    private static DecimalFormatSymbols simbols = new DecimalFormatSymbols(local);
    private static DecimalFormat formatDec[] = {
        new DecimalFormat("###0", simbols),
        new DecimalFormat("###0.0", simbols),
        new DecimalFormat("###0.00", simbols),
        new DecimalFormat("###0.000", simbols),
        new DecimalFormat("###0.0000", simbols),
        new DecimalFormat("###0.00000", simbols),
        new DecimalFormat("###0.000000", simbols),
        new DecimalFormat("###0.0000000", simbols),
        new DecimalFormat("###0.00000000", simbols),
        new DecimalFormat("###0.000000000", simbols),
        new DecimalFormat("###0.0000000000", simbols)
    };

    /**
     * Obtém um valor do tipo FLOAT em uma String, respeitando o número de casas
     * decimais informadas pelo parâmetro decimal
     *
     * @param value
     * @param decimal
     * @return
     */
    public static String formatFloat(Double value, int decimal) {
        if (decimal != -1) {
            return formatDec[decimal].format(value);
        } else {
            return String.valueOf(value);
        }
    }

    public static void sleep() {
        try {
            Thread.sleep(SLEEP_INTERVAL);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void sleep(int value) {
        try {
            Thread.sleep(value);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Método que adiciona uma barra, caso o diretório informado não termine com
     * uma barra
     *
     * @param value
     * @return
     */
    public static String checkDir(String value) {
        if (value != null && value.charAt(value.length() - 1) != '/') {
            value += "/";
        }
        return value;
    }

    /**
     * Cria um diretório através do caminha passado como parâmetro
     *
     * @param directory
     * @return
     */
    public static boolean createDirectory(String directory) {
        boolean result = true;
        File f = new File(directory);
        try {
            f.mkdir();
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        f = null;
        return result;
    }

    /**
     * Cria um arquivo com o nome informado como parâmetro
     *
     * @param fileName
     */
    public static void CreateFile(String fileName) {
        File f = new File(fileName);
        try {
            f.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        f = null;
    }

    /**
     * Cria um arquivo com o nome e o conteúdo passados como parâmetros
     *
     * @param fileName
     * @param Data
     */
    public static void WriteFile(String fileName, String Data) {
        try {
            CreateFile(fileName);
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            out.write(Data);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lê um arquivo
     *
     * @param fileDirectory
     * @return
     * @throws IOException
     */
    public static String ReadFile(String fileDirectory) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(fileDirectory));
        StringBuilder contents = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            contents.append(line);
            contents.append(ChironUtils.LINE_SEPARATOR);
        }
        in.close();
        return contents.toString();
    }

    /**
     * Obtém o caminho de um arquivo na forma canônica
     *
     * @param file
     * @return
     */
    public static String getCanonicalPath(File file) {
        String result = "";
        try {
            result = file.getCanonicalPath();
            result = result.replaceAll("\\\\", "/");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Obtém o nome do arquivo
     *
     * @param file
     * @return
     */
    public static String getFileName(String fullname) {
        if(fullname!=null){
            File file = new File(fullname);
            String result = getFileName(file);
            file = null;
            return result;
        }
        
        return fullname;
    }

    public static String getFileName(File file) {
        String result = "";
        result = file.getName();
        result = result.replaceAll("\\\\", "/");
        return result;
    }

    /**
     * Avalia se o sistema operacional de execução é Windows
     *
     * @return
     */
    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }

    /**
     * Avalia se o sistema operacional de execução é MAC
     *
     * @return
     */
    public static boolean isMacOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("mac") >= 0);
    }

    /**
     * Move um arquivo
     *
     * @param destination
     * @param origin
     * @throws IOException
     * @throws InterruptedException
     */
    public static void moveFile(String origin, String destination) throws IOException, InterruptedException {
        String cmd = "";
        createDirectory(destination);
        if (ChironUtils.isWindows()) {
            cmd = "move " + origin + " " + destination;
            cmd = cmd.replace("/", "\\");
        } else {
            cmd = "mv " + origin + " " + destination;
        }
        runCommand(cmd, null);
    }

    /**
     * Realiza a cópia de um arquivo
     *
     * @param destination
     * @param origin
     * @throws IOException
     * @throws InterruptedException
     */
    public static void copyFile(String origin, String destination) throws IOException, InterruptedException {
        String cmd = "";
        if (ChironUtils.isWindows()) {
            cmd = "xcopy " + origin + " " + destination;
            cmd = cmd.replace("/", "\\");
            cmd += " /q /c /y";
        } else {
            ChironUtils.createDirectory(destination);
            cmd = "cp " + origin + " " + destination;
        }
        runCommand(cmd, null);
    }

    /**
     * Método que copia os arquivos do template
     *
     * @param origin
     * @throws IOException
     * @throws InterruptedException
     */
    public static void copyTemplateFiles(String origin, String destination) throws IOException, InterruptedException {
        String cmd = "";
        if (ChironUtils.isWindows()) {
            cmd = "xcopy " + origin + "/* " + destination;
            cmd = cmd.replace("/", "\\");
            cmd += " /s /q /c /y ";
        } else if (ChironUtils.isMacOS()) {
            cmd = "cp " + origin + "/* " + destination;
        } else {
            cmd = "cp " + origin + "/* " + destination + " -r -f";
        }
        runCommand(cmd, null);
    }

    /**
     * Deleta um arquivo
     *
     * @param fileName
     * @param fileDir
     * @throws IOException
     * @throws InterruptedException
     */
    public static void deleteFile(String fileName, String fileDir) throws IOException, InterruptedException {
        //Código que deleta um arquivo após a execução da tarefa.
        File file = new File(fileDir + fileName);
        file.delete();
    }

    public static boolean isFile(String fileDir) {
        File file = new File(fileDir);
        boolean result = file.exists();
        file = null;
        return result;
    }

    /**
     * Roda um comando genérico, tendo em vista o sistema operacional
     * @param cmd
     * @param run
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    protected static boolean checkFile(String name) {
        File file = new File(name);
        boolean result = true;
        if (!file.exists()) {
            result = false;
        }
        file = null;
        return result;
    }
    
    public static void executeRemoteCommand(Connection conn, String commandLine) throws IOException{
        Session sess = conn.openSession();
        sess.execCommand(commandLine);
    }

    public static int runCommand(String cmd, String dir) throws IOException, InterruptedException {
        ChironUtils.deleteFile(ChironUtils.errorFile, dir);
        ChironUtils.deleteFile(ChironUtils.resultFile, dir);
        
        Runtime run = Runtime.getRuntime();
        int result = 0;
        String command[] = null;
        if (ChironUtils.isWindows()) {
            String cmdWin[] = {"cmd.exe", "/c", cmd};
            command = cmdWin;
        } else {
            String cmdLinux = cmd;
            if (cmd.contains(">")) {
                cmdLinux = cmd.replace(">", ">>");
            }
            String cmdLin[] = {"/bin/bash", "-c", cmdLinux};
            command = cmdLin;
        }
        if (verbose) {
            System.out.println(command[command.length - 1]);
        }
        Process pr = null;
        if (dir == null) {
            pr = run.exec(command);
        } else {
            pr = run.exec(command, null, new File(dir));
        }
        
        pr.waitFor();
        result = pr.exitValue();
        pr.destroy();
        
        return result;
    }

    private static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                // ignored
            }
        }
    }

    /**
     * Método que adiciona quebra de linha
     *
     * @param stream
     * @return
     */
    private static String getStreamAsString(InputStream stream) {
        String out = "";
        Scanner s = new Scanner(stream);
        while (s.hasNextLine()) {
            out.concat(s.nextLine() + ChironUtils.LINE_SEPARATOR);
        }
        return out;
    }

    /**
     * Estipulate the folder for a given activation. Currently it is creating
     * a single folder for each activation on the root directory of the atcivity (@param fodlerActivity).
     * In the future it is important that it creates th efolder hierachically if
     * @param activationNumber The serial number of the activation
     * @param folderActivity The folder path of the activity
     * @param parameterSpaceSize The size of the whole parameter set
     * @return 
     */
    public static String getActivationFolder(int activationNumber, String folderActivity) {
        String folder = folderActivity + String.valueOf(activationNumber) + ChironUtils.SEPARATOR;
        return folder;
    }
    
    public static String correctPath(String path){
        String correctedPath = path;
        if (ChironUtils.isWindows()) {
            correctedPath = path.replaceAll("\\\\", "@/@");
        }
        return correctedPath;
    }
    
    /**
     * Rewrite file including a field and your corresponding value
     * @param filePath The file path
     * @param field The name of field to be included
     * @param fieldValue The field value
     * @return 
     */
    public static void writeFileWithWorkflowID(String path, String filename, String field, int fieldValue) throws FileNotFoundException, IOException{
        while(!checkFile(path + filename)){
            sleep();
        }
        
        FileReader reader = new FileReader(path + filename);
        Scanner in = new Scanner(reader);
        
        String textValue = "";
        int lineIndex = 0;
        boolean containField = false;
        while(in.hasNextLine()){
            String line = in.nextLine();
            if(!line.isEmpty()){
                if(lineIndex==0){
                    if(!line.contains(field)){
                        textValue += field + ";";                   
                    }else{
                        containField = true; 
                    }
                    textValue += line + "\n";
                    lineIndex++;
                }else{
                    textValue += String.valueOf(fieldValue) + ";";
                    if(containField){
                        textValue += line.substring(line.indexOf(";")+1,line.length()) + "\n";
                    }else{
                        textValue += line + "\n";
                    }
                    lineIndex++;
                }
            }
        }
        
        File fout = new File(path + "Temp_" + filename);
        FileOutputStream fo = new FileOutputStream(fout);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fo));
        writer.write(textValue);
        writer.flush();
        writer.close();
    }

    public static void manipulateFile(EFile f, String workspace) throws IOException, InterruptedException {
        if(f.fileOper == Operation.COPY || f.fileOper == Operation.COPY_DELETE){
            ChironUtils.copyFile(f.getFileDir() + f.getFileName(), workspace);
        }else if(f.fileOper == Operation.MOVE || f.fileOper == Operation.MOVE_DELETE){
            ChironUtils.moveFile(f.getFileDir() + f.getFileName(), workspace);
        }
    }
    
    public static void closeSystemWithError(){
        System.exit(1);
    }
    
    public static void printFirstLevel(String text){
        System.out.println(text);
    }
    
    public static void printSecondLevel(String text){
        System.out.println(identation + text);
    }
    
//    Methods for remote connection
    public static void sendDataBySCP(Connection conn, String fileName, String remoteMountPoint) throws IOException {
        SCPClient scp = new SCPClient(conn);
        scp.put(fileName, remoteMountPoint);
    }
    
    public static void getDataBySCP(Connection conn, String filePath, String localPoint) throws IOException {
        SCPClient scp = new SCPClient(conn);
        scp.get(filePath, localPoint);
    }

    public static ArrayList<EMachine> getMachines(String mpjConfigurationFilePath, int cores) throws FileNotFoundException {
        FileReader reader = new FileReader(mpjConfigurationFilePath);
        Scanner in = new Scanner(reader);
        
        ArrayList<EMachine> machines = new ArrayList<EMachine>();
        while(in.hasNextLine()){
            String line = in.nextLine();
            if(!line.isEmpty() && line.contains("@") && !line.contains("@port@rank")){
                String[] columns = line.split("@");
                machines.add(new EMachine(columns[0],Integer.parseInt(columns[2]),cores));
            }
        }
        
        return machines;
    }

    public static String[] invokeRemoteChiron(EnvironmentConfiguration envConfig, ArrayList<EMachine> machines) throws IOException, InterruptedException {
        String[] argsFromLocalMachine = null;
        for(EMachine mac : machines){
            System.out.println(mac.rank + " - " + mac.publicDNS);
            
            try {
                if(mac.rank!=0){
                    ChironInvocation.executeRemoteSciCumulusCore(envConfig.amazonClient, envConfig.configurationFile, mac);
                }else{
                    argsFromLocalMachine = getFullArgumentsForCloud(envConfig.configurationFile, mac);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return argsFromLocalMachine;
    }

    public static String[] getFullArgumentsForCloud(XMLReader confFile, EMachine mac) {
        String[] result = new String[6];
        result[0] = String.valueOf(mac.rank);
        result[1] = confFile.workflowDir + "/machines.conf";
        result[2] = "niodev";
        result[3] = "MPI";
        result[4] = String.valueOf(mac.cores);
        result[5] = confFile.configurationFileName;
        
        return result;
    }
    
    public static String[] getFullArgumentsForLocalWithMPI(XMLReader confFile, String[] args) {
        String[] result = new String[6];
        result[0] = String.valueOf(args[1]); // rank
        result[1] = confFile.workflowDir + "/machines.conf";
        result[2] = "niodev";
        result[3] = "MPI";
        result[4] = String.valueOf(confFile.cores); // number of cores
        result[5] = args[0]; // xml file
        
        return result;
    }
    
    public static String[] getFullArgumentsForLocalWithoutMPI(XMLReader confFile, String[] args) {
        String[] result = new String[2];
        result[0] = String.valueOf(confFile.cores); // number of cores
        result[1] = args[0]; // xml file
        
        return result;
    }

    public static String[] getFullArgumentsForCloud(XMLReader confFile, String[] args) {
        String[] result = new String[6];
        result[0] = String.valueOf(args[1]); // rank
        result[1] = confFile.workflowDir + "/machines.conf";
        result[2] = "niodev";
        result[3] = "MPI";
        result[4] = args[2]; // number of cores
        result[5] = args[0]; // xml file
        
        return result;
    }
}