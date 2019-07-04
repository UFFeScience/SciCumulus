package chiron;

import chiron.EActivity.StatusType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mpi.MPI;

/**
 * This class control the threads on a compute node running Chiron.
 *
 * @author Jonas, Eduardo, Vítor
 * @since 2011-01-13
 */
public class EBody {

    public int MPI_size;
    public int MPI_rank;
    private List<EHead> heads;
    private int threads;
    protected EWorkflow eWorkflow;
    private EHead constrained = null;
    private int blocked = 0;
    private EMachine machine = null;
    protected double reliability;

    /**
     * Construtor da classe EBody
     *
     * @param MPI_rank
     * @param threads
     */
    public EBody(int MPI_size, int MPI_rank, int threads, EMachine machine, Double reliability) {
        this.MPI_size = MPI_size;
        this.MPI_rank = MPI_rank;
        heads = new ArrayList<EHead>();
        this.threads = threads;
        this.machine = machine;
        this.reliability = reliability;
    }

    /**
     * Esse método inicia as threads e as mantém alimentadas com tarefas até que todas as tarefas sejam finalizadas
     *
     * @throws InterruptedException
     * @throws SQLException
     */
    @SuppressWarnings("SleepWhileHoldingLock")
    public void execute() throws InterruptedException, SQLException, Exception {
        //create the threads
        for (int i = 0; i < threads; i++) {
            EHead head = new EHead(machine,i+1);
            heads.add(head);
            head.start();
        }

        //do it until you have threads available on your list of threads
        boolean receivedWait = false;
        int WaitCounter = 0;
        int WaitThreshold = 5;
        while (heads.size() > 0) {
            Iterator<EHead> iter = heads.iterator();
            while (iter.hasNext()) {
                EHead head = iter.next();
                if (this.constrained == null) {
                    if (head.constrained == true) {
                        this.constrained = head;
                        continue;
                    }
                } else {
                    if (head.isWaiting() && this.constrained != head) {
                        head.status = StatusType.BLOCKED;
                        this.blocked++;
                    }
                    if (blocked == heads.size() - 1) {
                        this.constrained.status = StatusType.READY;
                        blocked = 0;
                    }
                }

                if ((head.activation == null) && (!receivedWait || head.activationFinished != null)) {
                    EMessage sendMsg = new EMessage();
                    sendMsg.type = EMessage.Type.REQUEST_ACTIVATION;
                    sendMsg.activation = head.activationFinished;
                    head.activationFinished = null;
                    sendMsg.MPI_rank = this.MPI_rank;

                    //se o body está no modo constrained, mas a thread constrained já terminou, liberar outras threads
                    if (this.constrained == head) {
                        this.constrained = null;
                        this.blocked = 0;
                        if (!aHeadIsConstrained()) {
                            for (EHead h : heads) {
                                if (!h.constrained) {
                                    h.status = StatusType.READY;
                                }
                            }
                        }

                    }
                    //se alguma head está como constrained, bloquear as threads que terminaram suas tarefas
                    //reliability
                    if (this.constrained != null && head.status != StatusType.BLOCKED) {
                        head.status = StatusType.BLOCKED;
                        sendMsg.type = EMessage.Type.STORE;
                    }else if(!head.isReliable(reliability)){
                        head.status = StatusType.ABORT;
                        sendMsg.type = EMessage.Type.STORE;
                        iter.remove();
                    }

                    EMessage recvMsg = this.sendRequest(sendMsg);
                    if (recvMsg.type.equals(EMessage.Type.PROCESS_ACTIVATION)) {
                        head.activation = recvMsg.activation;
                    } else if (recvMsg.type.equals(EMessage.Type.FINISH)) {
                        head.status = EActivity.StatusType.FINISHED;
                        iter.remove();
                    } else {
                        head.activation = null;
                    }
                    if (recvMsg.type.equals(EMessage.Type.WAIT)) {
                        receivedWait = true;
                    }
                }
            }
            
            ChironUtils.sleep();
            
            if (receivedWait) {
                WaitCounter++;
                if (WaitCounter > WaitThreshold) {
                    receivedWait = false;
                    WaitCounter = 0;
                }
            }
        }
        if (MPI_rank > 0) {
            EMessage sendMsg = new EMessage();
            sendMsg.type = EMessage.Type.FINISH;
            sendMsg.activation = null;
            this.sendRequest(sendMsg);
        }
    }

    /**
     * Método responsável pelo envio de um pedido
     *
     * @param sendMsg
     * @return
     * @throws SQLException
     * @throws Exception
     */
    private EMessage sendRequest(EMessage sendMsg) throws SQLException, Exception {
        EMessage recvMsg = null;
        //send request locally
        if (MPI_rank == 0) {
            recvMsg = answerRequest(sendMsg);
        } else {
            //ask the listener (Listener changes the status of the activations to 1 already)
            EMessage sendMsgArray[] = new EMessage[1];
            sendMsgArray[0] = sendMsg;
            MPI.COMM_WORLD.Send(sendMsgArray, 0, 1, MPI.OBJECT, 0, 0);

            if (sendMsg.type != EMessage.Type.FINISH) {
                EMessage recvMsgArray[] = new EMessage[1];
                MPI.COMM_WORLD.Recv(recvMsgArray, 0, 1, MPI.OBJECT, 0, 0);
                recvMsg = recvMsgArray[0];
            }
        }
        return recvMsg;
    }

    /**
     * Método responsável pela resposta do pedido
     *
     * @param questionMsg
     * @return
     * @throws SQLException
     * @throws Exception
     */
    public synchronized EMessage answerRequest(EMessage questionMsg) throws SQLException, Exception {
        EMessage answerMsg = new EMessage();
        if (questionMsg.type.equals(EMessage.Type.REQUEST_ACTIVATION)) {
            if (questionMsg.activation != null) {
                EProvenanceQueue.queue.handleActivations(EProvenanceQueue.ActStoreType.ADD_ACTIVATIONS, questionMsg.activation);
            }
            EActivation[] activation = eWorkflow.iterateExecution(this);
            
            if (activation != null) {
                if ((activation[0] != EActivation.WAIT_ACTIVATION)) {
                    answerMsg.type = EMessage.Type.PROCESS_ACTIVATION;
                    answerMsg.activation = activation;
                    answerMsg.MPI_rank = questionMsg.MPI_rank;
                } else if (activation[0] == EActivation.WAIT_ACTIVATION) {
                    answerMsg.type = EMessage.Type.WAIT;
                }
            } else {
                answerMsg.type = EMessage.Type.FINISH;
            }
        } else if (questionMsg.type.equals(EMessage.Type.STORE)) {
            if (questionMsg.activation != null) {
                EProvenanceQueue.queue.handleActivations(EProvenanceQueue.ActStoreType.ADD_ACTIVATIONS, questionMsg.activation);
            }
        }
        return answerMsg;
    }

    private boolean aHeadIsConstrained() {
        boolean constrain = false;
        for (EHead h : heads) {
            if (h.constrained) {
                constrain = true;
                this.constrained = h;
            }
        }
        return constrain;
    }
}
