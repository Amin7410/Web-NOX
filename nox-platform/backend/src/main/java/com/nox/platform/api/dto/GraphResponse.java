package com.nox.platform.api.dto;

import com.nox.platform.core.engine.model.CoreBlock;
import com.nox.platform.core.engine.model.CoreRelation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphResponse {
    private List<CoreBlock> nodes;
    private List<CoreRelation> edges;
}
