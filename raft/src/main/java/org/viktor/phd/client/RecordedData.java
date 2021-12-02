package org.viktor.phd.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.With;

@With
@Builder
@AllArgsConstructor
public class RecordedData {
    public Long time;
    public Integer version;
}
