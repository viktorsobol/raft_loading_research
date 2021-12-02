package org.viktor.phd.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.viktor.phd.experiments.OperationType;

import java.util.LinkedList;

import static io.atomix.primitive.operation.PrimitiveOperation.operation;
import static org.viktor.phd.Serializers.CLIENT_SERIALIZER;
import static org.viktor.phd.client.RaftClient.GET;

public class ClientReadProcess extends ExperimentProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientReadProcess.class);

    private final RaftClient raftClient;

    public ClientReadProcess(RaftClient raftClient, String key, Long runTime, ApplicationDetails applicationDetails) {
        super(runTime, key, OperationType.READ, applicationDetails);
        this.raftClient = raftClient;
    }

    protected LinkedList<RecordedData> collectData() {
        LinkedList<RecordedData> observedData = new LinkedList<>();
        while(isOn.get()) {
            String res = raftClient
                    .execute(operation(GET, CLIENT_SERIALIZER.encode(key)))
                    .thenApply(result -> {
                        if (result == null) {
                            return "-1";
                        } else {
                            return CLIENT_SERIALIZER.decode(result);
                        }
                    })
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            LOGGER.error("Unable to query key - " + key, error);
                        }
                    })
                    .join();

            if (res.equals("-1")) {
                LOGGER.warn("Data is not available yet");
            }

            observedData.add(new RecordedData(System.currentTimeMillis(), Integer.valueOf(res)));
        }
        LOGGER.info("Observed data size - " + observedData.size());

        return observedData;
    }


}
