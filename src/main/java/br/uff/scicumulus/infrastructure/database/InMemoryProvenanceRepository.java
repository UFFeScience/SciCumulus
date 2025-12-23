
package br.uff.scicumulus.infrastructure.database;

import br.uff.scicumulus.core.workflow.Activity;
import java.util.ArrayList;
import java.util.List;

public class InMemoryProvenanceRepository implements ProvenanceRepository {
    private final List<String> records = new ArrayList<>();

    @Override
    public void save(Activity activity) {
        records.add(activity.getId());
    }

    public List<String> getRecords() {
        return records;
    }
}
