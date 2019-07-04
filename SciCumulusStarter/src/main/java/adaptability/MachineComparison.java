package adaptability;

import java.util.ArrayList;

/**
 *
 * @author vitor
 */
public class MachineComparison {
    
    ArrayList<VirtualMachineType> machinesToAllocate;
    ArrayList<VirtualMachineType> machinesToDeallocate;
    boolean equalToInstanciatedMachines;
    
    public MachineComparison(){
        machinesToAllocate = new ArrayList<VirtualMachineType>();
        machinesToDeallocate = new ArrayList<VirtualMachineType>();
    }

    public ArrayList<VirtualMachineType> getMachinesToAllocate() {
        return machinesToAllocate;
    }

    public void setMachinesToAllocate(ArrayList<VirtualMachineType> machinesToBeAllocated) {
        this.machinesToAllocate = machinesToBeAllocated;
    }

    public ArrayList<VirtualMachineType> getMachinesToDeallocate() {
        return machinesToDeallocate;
    }

    public void setMachinesToDeallocate(ArrayList<VirtualMachineType> machinesToDeallocate) {
        this.machinesToDeallocate = machinesToDeallocate;
    }
    
    public void setMachinesToAllocate(VirtualMachineType newMachineType) {
        this.machinesToAllocate.add(newMachineType);
    }
    
    public void addMachinesToDeallocate(VirtualMachineType machineType) {
        this.machinesToDeallocate.add(machineType);
    }
    
    public void addMachinesToAllocate(VirtualMachineType machineType) {
        this.machinesToAllocate.add(machineType);
    }

    public boolean isEqualToInstanciatedMachines() {
        return equalToInstanciatedMachines;
    }

    public void setEqualToInstanciatedMachines(boolean equalToInstanciatedMachines) {
        this.equalToInstanciatedMachines = equalToInstanciatedMachines;
    }
}
