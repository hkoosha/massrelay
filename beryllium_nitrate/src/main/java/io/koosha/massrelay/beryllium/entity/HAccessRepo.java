package io.koosha.massrelay.beryllium.entity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface HAccessRepo extends CrudRepository<HAccess, Long> {

    @Transactional
    default void tSave(final HAccess t) {
        save(t);
    }

}
