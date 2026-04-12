package com.nox.platform.shared.mapping;

import java.util.List;
import java.util.stream.Collectors;

public interface BaseMapper<E, D> {
    
    D toResponse(E entity);
    
    default List<D> toResponseList(List<E> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
