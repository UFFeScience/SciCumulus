package chiron;

import java.io.Serializable;

/**
 * EMessage transports EActivations to be consumed by the computing nodes.
 *
 * @author Jonas, Eduardo, Vítor.
 * @since 2010-12-25
 */
public class EMessage implements Serializable {

    public enum Type {
        REQUEST_ACTIVATION, STORE, PROCESS_ACTIVATION, WAIT, FINISH
    }
    
    protected Type type = Type.REQUEST_ACTIVATION;
    protected EActivation [] activation = null;
    protected int MPI_rank = 0;
}
