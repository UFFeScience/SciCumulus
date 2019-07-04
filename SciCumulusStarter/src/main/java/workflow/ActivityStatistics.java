package workflow;

/**
 *
 * @author Daniel, Vítor
 */
public class ActivityStatistics {
    
    double avgTime;
    int numberCores;

    public ActivityStatistics() {
    }

    public void setAvgTime(double avgTime) {
        this.avgTime = avgTime;
    }

    public double getAvgTime() {
        return avgTime;
    }

    public int getNumberCores() {
        return numberCores;
    }

    public void setNumberCores(int numberCores) {
        this.numberCores = numberCores;
    }
}
