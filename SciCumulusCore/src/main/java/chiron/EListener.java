package chiron;

import mpi.MPI;

/**
 * The EListener only executes on node 0 (MPI_rank=0) only when Chiron is 
 * running under MPI. It receives the requests from other body nodes and send
 * them tasks. When a node sends a request with a task, it means that the 
 * activation has been processed, then Listener can store the activation 
 * in the database.
 * @author Jonas, Eduardo
 * @since 2010-12-25
 */
public class EListener implements Runnable {

    public int nodes = 0;
    private EBody body;

    public EListener(EBody body, int MPI_size) {
        this.nodes = MPI_size - 1;
        this.body = body;
    }

    @Override
    @SuppressWarnings("CallToThreadDumpStack")
    public void run() {
        long startTime = System.currentTimeMillis();
        
        while (nodes > 0) {
            //wait for incoming requests
            try {
                //MPI Recv buffer
                EMessage request[] = new EMessage[1];
                //if there are other EBody on other MPI processes asking for activations
                MPI.COMM_WORLD.Recv(request, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, 0);
                EMessage received = request[0];
                if (received != null) {
                    if (received.type == EMessage.Type.FINISH) {
                        nodes--;
                    } else {
                        int destiny = received.MPI_rank;
                        //if the message contains a processed activation:
                        EMessage message = body.answerRequest(received);
                        //Message buffer for MPI send
                        EMessage answer[] = new EMessage[1];
                        answer[0] = message;
                        MPI.COMM_WORLD.Send(answer, 0, 1, MPI.OBJECT, destiny, 0);
                    }
                }
                
                if(body.eWorkflow != null && body.eWorkflow.redundancy){
                    long currentTime = System.currentTimeMillis();
                    if(currentTime - startTime > 15000){
                        EProvenance.evaluateRunningActivations(body);
                        startTime = currentTime;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
    }
}
