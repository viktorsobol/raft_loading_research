package org.viktor.phd.apps;

import io.atomix.protocols.raft.ReadConsistency;
import io.atomix.protocols.raft.session.CommunicationStrategy;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.viktor.phd.client.ApplicationDetails;
import org.viktor.phd.client.ClientReadProcess;
import org.viktor.phd.client.ClientWriteProcess;
import org.viktor.phd.client.RaftClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private static Integer port = 7000;

    private Integer nextPort() {
        return port++;
    }

    public void run(String[] args) throws ParseException, InterruptedException {

        Options options = getOptions();
        CommandLine commandLine;

        CommandLineParser parser = new DefaultParser();
        commandLine = parser.parse(options, args);

        String experimentId = commandLine.getOptionValue("expId");
        Long runTimeMs = Long.parseLong(commandLine.getOptionValue("runTimeMs"));

        int totalThreads = Integer.parseInt(commandLine.getOptionValue("totalThreads"));
        int totalWriteThreads = Integer.parseInt(commandLine.getOptionValue("writeThreads"));
        int readThreadsPerWriteOp = (totalThreads - totalWriteThreads) / totalWriteThreads;

        List<String> members = Arrays.asList(commandLine.getOptionValue("mIps").split(","));

        List<CompletableFuture<?>> futures = new ArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(totalThreads);

        List<RaftClient> readClients = new ArrayList<>();
        RaftClient[] writeClients = new RaftClient[totalWriteThreads];

        // Delay before execution for 30 seconds
        // so all the clients will start working roughly at the same time
        Long startTime = System.currentTimeMillis() + 120000;

        LOGGER.info("Creating clients...");
        for (int i = 0; i < totalWriteThreads; i++) {

            // Submitting one write client
            String dataKey = "key_" + i;
            writeClients[i] = new RaftClient(
                    nextPort(),
                    members,
                    CommunicationStrategy.LEADER,
                    ReadConsistency.LINEARIZABLE
            );
            var writeFuture = CompletableFuture.runAsync(new ClientWriteProcess(
                    writeClients[i],
                    dataKey,
                    runTimeMs,
                    new ApplicationDetails(experimentId, "0"),
                    startTime
            ), pool);
            futures.add(writeFuture);
            LOGGER.info("Write process is submitted...");
//
//            CommunicationStrategy.ANY,
//                    ReadConsistency.SEQUENTIAL
            // Submitting readThreadsPerWriteOp read clients per one read operation for readThreadsPerWriteOp
            for (int j = 0; j < readThreadsPerWriteOp; j++) {
                RaftClient client = new RaftClient(
                        nextPort(),
                        members,
                        CommunicationStrategy.ANY,
                        ReadConsistency.SEQUENTIAL
                );

                var readFuture = CompletableFuture.runAsync(new ClientReadProcess(
                        client,
                        dataKey,
                        runTimeMs,
                        new ApplicationDetails(experimentId, Integer.toString(j)),
                        startTime
                ), pool);

                readClients.add(client);
                futures.add(readFuture);
                LOGGER.info("Read process is submitted...");

            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        LOGGER.info("All processes have completed");

        for (RaftClient readClient: readClients) {
            readClient.shutDown();
        }

        for (RaftClient wc: writeClients) {
            wc.shutDown();
        }

        LOGGER.info("All clients have shutdown");

        pool.shutdown();
    }

    private Options getOptions() {

        Option optionMembersIps = Option.builder("mIps")
                .required(true)
                .hasArg()
                .desc("Ip list of all the members in a cluster")
                .build();

        Option expId = Option.builder("expId")
                .required(true)
                .hasArg()
                .build();

        Option totalThreads = Option.builder("totalThreads")
                .required(true)
                .desc("How many threads in total")
                .hasArg()
                .build();

        Option totalWriteThreads = Option.builder("writeThreads")
                .required(true)
                .desc("How many write threads")
                .hasArg()
                .build();

        Option runTime = Option.builder("runTimeMs")
                .required(true)
                .desc("total runtime of an experiment")
                .hasArg()
                .build();

        Options options = new Options();
        options.addOption(optionMembersIps);
        options.addOption(expId);
        options.addOption(totalThreads);
        options.addOption(totalWriteThreads);
        options.addOption(runTime);
        return options;
    }
}
