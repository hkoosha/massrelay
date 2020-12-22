package io.koosha.massrelay.beryllium.entity;

import io.koosha.massrelay.aluminum.base.value.Funcode;
import org.springframework.data.repository.CrudRepository;

public interface PermissionRepo extends CrudRepository<Permission, Long> {

    Permission findByLeftAndRightAndFuncode(Client left,
                                            Client right,
                                            String funcode);

    default Permission find(final Client left,
                            final Client right,
                            final Funcode funcode) {
        return this.findByLeftAndRightAndFuncode(left, right, funcode.name());
    }

}
