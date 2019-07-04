package chiron;

import chiron.EActivity.StatusType;
import java.util.ArrayList;
import java.util.Arrays;
import vs.database.M_DB;

/**
 *
 * @author Eduardo
 */
public class EProvenanceQueue extends Thread {
    public static EProvenanceQueue queue;

    public enum ActStoreType {

        ADD_ACTIVATIONS, REMOVE_ACTIVATIONS
    }
    public M_DB db = null;
    public StatusType status = StatusType.READY;
    ArrayList<EActivation> activations = new ArrayList<EActivation>();
    

    public synchronized EActivation[] handleActivations(ActStoreType Oper, EActivation[] chironActivations) {
        EActivation result[] = null;
        if (Oper == ActStoreType.ADD_ACTIVATIONS) {
            activations.addAll(Arrays.asList(chironActivations));
        } else if (Oper == ActStoreType.REMOVE_ACTIVATIONS) {
            result = new EActivation[activations.size()];
            for (int i = 0; i < activations.size(); i++) {
                result[i] = activations.get(i);
            }
            activations.clear();
        }
        return result;
    }
    
    @Override
    @SuppressWarnings({"SleepWhileHoldingLock"})
    public void run() {
        while (status != StatusType.FINISHED) {
            storeActivations();
            ChironUtils.sleep();
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    public void storeActivations() {
        if (db == null) {
            return;
        }
        EActivation[] activationList = handleActivations(ActStoreType.REMOVE_ACTIVATIONS, null);
        for (int i = 0; i < activationList.length; i++) {
            try {
                EProvenance.storeActivation(activationList[i]);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
