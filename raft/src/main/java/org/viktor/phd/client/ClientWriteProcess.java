package org.viktor.phd.client;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.viktor.phd.experiments.OperationType;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.atomix.primitive.operation.PrimitiveOperation.operation;
import static org.viktor.phd.Serializers.CLIENT_SERIALIZER;
import static org.viktor.phd.client.RaftClient.PUT;

public class ClientWriteProcess extends ExperimentProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientWriteProcess.class);

    private final RaftClient raftClient;

    public ClientWriteProcess(RaftClient raftClient, String key, Long runTime, ApplicationDetails applicationDetails, Long startTime) {
        super(runTime, key, OperationType.WRITE, applicationDetails, startTime);
        this.raftClient = raftClient;
    }


    protected LinkedList<RecordedData> collectData() {
        LinkedList<RecordedData> savedData = new LinkedList<>();

        Integer version = 1;
        while (isOn.get()) {
            raftClient
                    .execute(
                            operation(PUT, CLIENT_SERIALIZER.encode(Maps.immutableEntry(key, String.valueOf(version))))
                    ).join();

            savedData.add(new RecordedData(Instant.now().toEpochMilli(), version));
            version++;
        }
        LOGGER.info("Saved data size - " + savedData.size());

        return savedData;
    }
}
