package io.koosha.massrelay.beryllium.entity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface HLeftClientRequestHistoryRepo extends CrudRepository<HLeftClientRequestHistory, Long> {

    @Transactional
    default void tSave(final HLeftClientRequestHistory t) {
        save(t);
    }

}
