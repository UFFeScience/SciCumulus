package chiron;

import chiron.cloud.CloudUtils;
import chiron.concept.CWorkflow;
import chiron.environment.EnvironmentConfiguration;
import chiron.environment.EnvironmentType;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import mpi.MPI;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

//Executar como single instance com x threads: x exp/chiron.xml
//Executar com MPJ no no y com x threads; y dir/mpj.conf niodev MPI x exp/chiron.xml
/**
 * Classe principal do chiron
 *
 * @author Eduardo, Jonas, Vítor
 * @version 0.8
 * @since 2011-2-25
 */
public class Chiron {
    //MPI Attributes
    private boolean isMPI = false;
    private static int MPI_rank = 0;
    protected static int MPI_size = 1;
    private CWorkflow cworkflow;
    private EWorkflow eworkflow;
    private EnvironmentConfiguration environment;
    private Thread listenerThread;
    private EListener listener = null;
    private EBody hBody;
    protected static boolean mainNode = false;
    protected EMachine emachine = null;
    private ArrayList<EMachine> machines;

    /**
     * Método principal do Chiron
     *
     * @param args argumentos para execução do Chiron
     * @return void
     */
    @SuppressWarnings({"CallToThreadDumpStack", "static-access"})
    public static void main(String[] args) {
        Chiron chiron = new Chiron();
        try {
            System.out.println("Chiron...");

            System.out.println("Prepare...");
            chiron.prepare(args);

            System.out.println("Open...");
            if (chiron.mainNode) {
                chiron.open();
            }
            System.out.println("Execute...");
            chiron.execute();
            
            System.out.println("Close...");
            chiron.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Prepara para executar o Chiron
     *
     * @param args
     * @return void
     * @throws ParsingException
     * @throws ValidityException
     * @throws IOException
     * @throws SQLException
     * @throws Exception
     */
    @SuppressWarnings("static-access")
    private void prepare(String[] args) throws ParsingException, ValidityException, IOException, SQLException, Exception {
        String configurationFilePath = args[0];
        
        String tmpArgs = "";
        for(int i=0; i<args.length; i++){
            String arg = args[i];
            if(!arg.equals(">>")){
                if(!tmpArgs.isEmpty()){
                    tmpArgs += " ";
                }
                tmpArgs += arg;
            }else{
                break;
            }
        }
        args = tmpArgs.split(" ");
        
        XMLReader configurationFile = new XMLReader(configurationFilePath);
        configurationFile.readXMLConfiguration();
        environment = new EnvironmentConfiguration(configurationFile);
        emachine = new EMachine();
        if(args.length == 6){
            emachine.rank = Integer.parseInt(args[0]);
            emachine.cores = Integer.parseInt(args[4]);
        }else if(args.length==2){
            try{
                emachine.cores = Integer.parseInt(args[0]);
            } catch(Exception ex){
            }
        }
        
        if(environment.type == EnvironmentType.CLOUD_AMAZON){
            if(args.length == 1){
                machines = CloudUtils.getMachinesFromCluster(environment.amazonClient, configurationFile);

                ChironInvocation.generatePoolControlFile(configurationFile, machines);

                args = ChironUtils.invokeRemoteChiron(environment, machines);
                System.out.println("Executing local SciCumulusCore instances...");
                invokeChiron(args, configurationFile);
            } else if(args.length >= 3){
                args = ChironUtils.getFullArgumentsForCloud(configurationFile, args);
                invokeChiron(args, configurationFile);
            } else{
                System.out.println("Abort...");
                System.exit(0);
            }
        } else if(environment.type == EnvironmentType.LOCAL){
            if(args.length >= 2){
                args = ChironUtils.getFullArgumentsForLocalWithMPI(configurationFile, args);
                invokeChiron(args, configurationFile);
            }else if(args.length == 1){
                args = ChironUtils.getFullArgumentsForLocalWithoutMPI(configurationFile, args);
                invokeChiron(args, configurationFile);
            }else{
                System.out.println("Abort...");
                System.exit(0);
            }
        }else{
            System.out.println("Abort...");
            System.exit(0);
        }
    }

    /**
     * Open
     *
     * @return void
     * @throws Exception
     */
    public void open() throws Exception {
        File direct = new File(eworkflow.expDir);
        if (!direct.exists()) {
            direct.mkdirs();
        }
        direct = null;
        
        int tmpWkfId = EProvenance.matchEWorkflow(cworkflow.tag, eworkflow.exeTag);
        if (tmpWkfId >= 0) {
            //the workflow was partially executed before
            eworkflow.wkfId = tmpWkfId;
            EProvenance.matchActivities(EProvenance.db, eworkflow);
        }
        
        EProvenance.storeWorkflow(EProvenance.db, eworkflow);
        for (int i = 0; i < eworkflow.activities.size(); i++) {
            EActivity act = eworkflow.activities.get(i);
            EProvenance.storeActivity(act);
            EProvenance.updateRunningActivations(EProvenance.db, act);
        }
        eworkflow.evaluateDependencies();
        eworkflow.checkPipeline();
    }

    /**
     * Executa o Chiron
     *
     * @return void
     * @throws IOException
     * @throws InterruptedException
     * @throws SQLException
     * @throws Exception
     */
    public void execute() throws IOException, InterruptedException, SQLException, Exception {
        Double reliability = environment.configurationFile.getReliability();
        
        EProvenance.storeMachine(EProvenance.db, emachine);
        hBody = new EBody(MPI_size, MPI_rank, environment.configurationFile.cores, emachine, reliability);

        //start Listener if is MPI and in main node
        if ((MPI_size > 1) && MPI_rank == 0) {
            listener = new EListener(hBody, MPI_size);
            listenerThread = new Thread(listener);
            listenerThread.start();
            environment.listener = listenerThread;
        }

        //initializes on main node
        if (Chiron.mainNode) {
            hBody.eWorkflow = eworkflow;
        }

        hBody.execute();

        if ((MPI_size > 1) && (MPI_rank == 0)) {
            while (listener.nodes > 0) {
                ChironUtils.sleep();
            }
        }

        if (Chiron.mainNode) {
            EProvenanceQueue.queue.status = EActivity.StatusType.FINISHED;
        }
        ChironUtils.sleep();
    }

    /**
     * Close
     *
     * @return void
     */
    private void close() throws SQLException {
        if(Chiron.mainNode){
            EProvenance.db.getConn().close();
        }
        
        finalizeMPI();
        
        System.exit(0);
    }

    public void initMPI(String[] args) {
        MPI.Init(args);
//        System.out.println("finish mpi");
        this.MPI_size = MPI.COMM_WORLD.Size();
        this.MPI_rank = MPI.COMM_WORLD.Rank();
        this.isMPI = true;
    }

    private void finalizeMPI() {
        if (isMPI) {
            MPI.Finalize();
        }
    }

    private void invokeChiron(String[] args, XMLReader configurationFile) throws Exception {
//        System.out.println("Starting MPI");
//        System.out.println("args");
//        for(String arg: args){
//            System.out.println(arg);
//        }
        if(args.length == 6){
            initMPI(args);
        }
            
        if (!this.isMPI || this.MPI_rank == 0) {
            Chiron.mainNode = true;
            
            cworkflow = configurationFile.getConceptualWorkflow();
            eworkflow = configurationFile.getExecutionWorkflow();
            eworkflow.environment = environment;

            if(!Chiron.mainNode){
                EProvenance.db.getConn().close();
            }
        }
    }
}
