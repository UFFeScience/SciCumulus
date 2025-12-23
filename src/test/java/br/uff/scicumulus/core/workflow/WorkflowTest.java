
package br.uff.scicumulus.core.workflow;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class WorkflowTest {
    @Test
    void workflowHoldsActivities() {
        Activity a = new Activity("A1", "echo test");
        Workflow w = new Workflow("WF1", List.of(a));
        assertEquals(1, w.getActivities().size());
    }
}
