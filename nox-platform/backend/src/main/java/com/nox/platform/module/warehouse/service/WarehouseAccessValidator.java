package com.nox.platform.module.warehouse.service;

import com.nox.platform.module.warehouse.domain.OwnerType;
import java.util.UUID;

public interface WarehouseAccessValidator {
    void validateReadAccess(UUID targetOwnerId, OwnerType ownerType);
    void validateWriteAccess(UUID targetOwnerId, OwnerType ownerType);
}
