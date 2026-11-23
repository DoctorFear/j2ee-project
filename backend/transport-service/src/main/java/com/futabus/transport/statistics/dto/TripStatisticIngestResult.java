package com.futabus.transport.statistics.dto;

import com.futabus.transport.statistics.dto.response.TripStatisticResponse;
import lombok.Builder;

@Builder
public record TripStatisticIngestResult(
        TripStatisticResponse statistic,
        boolean created
) {
}
