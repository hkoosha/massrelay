package io.koosha.massrelay.beryllium.svc;

import io.koosha.massrelay.aluminum.base.value.Funcode;
import io.koosha.massrelay.beryllium.entity.Client;
import io.koosha.massrelay.beryllium.entity.HAccess;
import io.koosha.massrelay.beryllium.entity.HAccessRepo;
import io.koosha.massrelay.beryllium.entity.PermissionEvent;
import io.koosha.massrelay.beryllium.entity.PermissionRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.koosha.massrelay.aluminum.base.Util.bg;

@Singleton
@Component
public final class LeftToRightAccessService {

    private static final Logger log = LoggerFactory.getLogger(LeftToRightAccessService.class);

    private final PermissionRepo permissionRepo;
    private final HAccessRepo hAccessRepo;

    @Inject
    public LeftToRightAccessService(final PermissionRepo permissionRepo,
                                    final HAccessRepo hAccessRepo) {
        this.permissionRepo = permissionRepo;
        this.hAccessRepo = hAccessRepo;
    }

    public PermissionEvent check(final Client left,
                                 final Client right,
                                 final Funcode funcode) {
        final PermissionEvent event;
        if (permissionRepo.find(left, right, funcode) == null) {
            log.warn("access denied, no permission record, left={} right={} funcode={}",
                left.getId(), right.getId(), funcode);
            event = PermissionEvent.DENY;
        }
        else {
            log.warn("access granted: left={} right={} funcode={}",
                left.getId(), right.getId(), funcode);
            event = PermissionEvent.GRANT;
        }

        bg("LeftToRightAccessService",
            () -> hAccessRepo.tSave(HAccess.create(left, right, funcode, event)));

        return event;
    }

}
