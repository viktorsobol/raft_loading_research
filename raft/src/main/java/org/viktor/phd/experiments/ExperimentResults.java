package org.viktor.phd.experiments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.viktor.phd.client.RecordedData;

import java.util.List;

@With
@Getter
@Builder
@AllArgsConstructor
public class ExperimentResults {

    private String expId;

    private String applicationId;

    private List<RecordedData> observedData;

    private OperationType type;

    private Long durationMs;
}
