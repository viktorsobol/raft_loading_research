package org.viktor.phd.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.viktor.phd.experiments.ExperimentResults;
import org.viktor.phd.experiments.OperationType;
import org.viktor.phd.experiments.Saver;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ExperimentProcess implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientReadProcess.class);

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    protected final AtomicBoolean isOn = new AtomicBoolean(true);

    private final Long runTime;

    protected final String key;

    private final OperationType type;

    private final ApplicationDetails applicationDetails;

    public ExperimentProcess(Long runTime, String key, OperationType type, ApplicationDetails applicationDetails) {
        this.runTime = runTime;
        this.key = key;
        this.type = type;
        this.applicationDetails = applicationDetails;
    }

    @Override
    public void run() {
        scheduleShutDown();

        LOGGER.info("Starting " + type.name() + " process...");

        long startTime = System.currentTimeMillis();
        LinkedList<RecordedData> recordedData = collectData();
        long totalTime = System.currentTimeMillis() - startTime;
        persistData(recordedData, totalTime);

        LOGGER.info(type.name()  + " processed for key - " + key + " has finished. " +
                "Total time - " + totalTime + " ms. Total operations - " + recordedData.size());

    }

    protected abstract LinkedList<RecordedData> collectData();

    protected void scheduleShutDown() {
        // loop runs for specific amount of time
        scheduler.schedule(() -> {
            LOGGER.info("Setting off the reading process");
            isOn.set(false);
        }, runTime, TimeUnit.MILLISECONDS);
    }

    protected void pause() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void persistData(List<RecordedData> data, long totalTime) {
        var results = ExperimentResults.builder()
                .expId(applicationDetails.getExperimentId())
                .applicationId(applicationDetails.getApplicationId())
                .type(type)
                .durationMs(totalTime)
                .observedData(data)
                .build();

        new Saver().save(results);
    }
}
