
package br.uff.scicumulus.core.provenance;

import br.uff.scicumulus.core.workflow.Activity;
import br.uff.scicumulus.infrastructure.database.ProvenanceRepository;

public class ProvenanceService {
    private final ProvenanceRepository repository;

    public ProvenanceService(ProvenanceRepository repository) {
        this.repository = repository;
    }

    public void record(Activity activity) {
        repository.save(activity);
    }
}
