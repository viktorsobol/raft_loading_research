package org.viktor.phd.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ApplicationDetails {
    private String experimentId;
    private String applicationId;
}
