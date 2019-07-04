package workflow;

/**
 *
 * @author Daniel, Vítor
 */
public class Activity {
    public String tag;
    public int order;
    public boolean isFinished;

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setisFinished(boolean isExecuted) {
        this.isFinished = isExecuted;
    }

    public boolean isIsFinished() {
        return isFinished;
    }

    public void setIsFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    public String getTag() {
        return tag;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public Activity(String tag) {
        this.tag = tag;
        this.isFinished = false;
    }

    @Override
    public String toString() {
        return "Activity{" + "tag=" + tag + ", isExecuted=" + isFinished + '}';
    }
    
}
