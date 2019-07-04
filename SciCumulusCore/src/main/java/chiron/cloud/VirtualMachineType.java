package chiron.cloud;

/**
 *
 * @author Daniel, Vítor
 */
public class VirtualMachineType {

    String type;
    double financialCost;
    double diskSpace;
    int amountInstantiatedVM;
    int ram;
    double gflops;
    String platform;
    int numberOfCores;

    public void setType(String type) {
        this.type = type;
    }

    public void setNumberOfCores(int numberOfCores) {
        this.numberOfCores = numberOfCores;
    }

    public int getNumberOfCores() {
        return numberOfCores;
    }

    public String getType() {
        return type;
    }

    public VirtualMachineType(double financialCost, double diskSpace, int ram, double gflops, String platform, int numberOfCores) {
        this.financialCost = financialCost;
        this.diskSpace = diskSpace;
        this.ram = ram;
        this.gflops = gflops;
        this.platform = platform;
        this.numberOfCores = numberOfCores;
        this.amountInstantiatedVM = 0;
    }
    
    public VirtualMachineType(VirtualMachineType parent) {
        this.type = parent.type;
        this.financialCost = parent.financialCost;
        this.diskSpace = parent.diskSpace;
        this.ram = parent.ram;
        this.gflops = parent.gflops;
        this.platform = parent.platform;
        this.numberOfCores = parent.numberOfCores;
        this.amountInstantiatedVM = 0;
    }

    public double getFinancialCost() {
        return financialCost;
    }

    public double getDiskSpace() {
        return diskSpace;
    }

    public int getAmountInstantiatedVM() {
        return amountInstantiatedVM;
    }

    public int getRam() {
        return ram;
    }

    public double getGflops() {
        return gflops;
    }

    public String getPlatform() {
        return platform;
    }

    public void setFinancialCost(double financialCost) {
        this.financialCost = financialCost;
    }

    public void setDiskSpace(double diskSpace) {
        this.diskSpace = diskSpace;
    }

    public void setAmountInstantiatedVM(int amountInstantiatedVM) {
        this.amountInstantiatedVM = amountInstantiatedVM;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public void setGflops(double gflops) {
        this.gflops = gflops;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
   
    @Override
    public String toString() {
        return "virtualMachineType{" + "type=" + type + ", financialCost=" + financialCost + ", diskSpace=" + diskSpace + ", amountInstantiatedVM=" + amountInstantiatedVM + ", ram=" + ram + ", gflops=" + gflops + ", platform=" + platform + ", numberOfCores=" + numberOfCores + '}';
    }

    public void addAmountInstantiatedVM() {
        this.amountInstantiatedVM++;
    }

    public void decrementAmountInstantiatedVM() {
        this.amountInstantiatedVM--;
    }
}
