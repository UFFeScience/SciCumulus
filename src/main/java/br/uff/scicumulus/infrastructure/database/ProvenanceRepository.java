
package br.uff.scicumulus.infrastructure.database;

import br.uff.scicumulus.core.workflow.Activity;

public interface ProvenanceRepository {
    void save(Activity activity);
}
