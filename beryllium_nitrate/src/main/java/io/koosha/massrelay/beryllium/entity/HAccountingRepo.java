package io.koosha.massrelay.beryllium.entity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface HAccountingRepo extends CrudRepository<HAccounting, Long> {

    @Transactional
    default void tSave(final HAccounting t) {
        save(t);
    }

}
