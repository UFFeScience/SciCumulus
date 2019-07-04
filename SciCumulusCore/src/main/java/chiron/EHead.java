package chiron;

import chiron.EActivity.StatusType;

/**
 * The EHeads are threads responsible to run the activations.
 *
 * @author Jonas, Eduardo
 * @since 2010-12-25
 */
public class EHead extends Thread {

    public EActivation[] activation = null;
    public EActivation[] activationFinished = null;
    public StatusType status = StatusType.READY;
    public boolean constrained = false;
    public EMachine machine = null;
    public int core = 1;
    public long lastCheck;

    public EHead(EMachine machine, int core) {
        super();
        this.machine = machine;
        this.core = core;
        this.lastCheck = System.currentTimeMillis();
    }

    @Override
    @SuppressWarnings({"SleepWhileHoldingLock"})
    public void run() {
//        reliability
        while (status != StatusType.FINISHED || status != StatusType.ABORT) {
            if (!this.gotConstrained()) {
                if (status != StatusType.BLOCKED) {
                    /**
                     * The Head is blocked while constrained equals TRUE and
                     * the EBody threads are still busy processing previous
                     * activations.
                     */
                    if ((activation != null)) {
                        for (int i = 0; i < activation.length; i++) {
                            if (activation[i].status == StatusType.READY) {
                                activation[i].status = EActivity.StatusType.RUNNING;
                                activation[i].machineID = machine.ID;
//                                System.out.println(activation[i].id);
                                activation[i].executeActivation();
                                
                                //reliability
                                if(activation[i].exitStatus==0){
                                    machine.incrementProcessedActivations();
                                }else{
                                    machine.incrementFailuresInActivations();
                                }
                            }
                        }
                        if (this.constrained) {
                            this.constrained = false;
                        }
                        activationFinished = activation;
                        activation = null;
                    } else {
                        ChironUtils.sleep();
                    }
                } else {
                    ChironUtils.sleep();
                }
            }
        }
    }

    private boolean gotConstrained() {
        if (!this.constrained && activation != null) {
            for (EActivation t : activation) {
                if (t.isConstrained()) {
                    this.constrained = true;
                    this.status = StatusType.BLOCKED;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isWaiting() {
        if (this.status.equals(StatusType.BLOCKED)) {
            return true;
        } else if (this.activation == null && this.activationFinished == null) {
            return true;
        } else {
            return false;
        }
    }

    boolean isReliable(double reliability) {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastCheck > ChironUtils.realiabilityCheckInterval){
            lastCheck = currentTime;
            if(machine.computeReliability() < reliability){
                return false;
            }
        }
        
        return true;
    }
}
