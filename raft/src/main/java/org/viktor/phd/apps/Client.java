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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    public void run(String[] args) throws ParseException, InterruptedException {

        Options options = getOptions();
        CommandLine commandLine;

        CommandLineParser parser = new DefaultParser();
        commandLine = parser.parse(options, args);

        String experimentId = commandLine.getOptionValue("expId");
        String applicationId = commandLine.getOptionValue("aId");
        int readClientsCount = Integer.parseInt(commandLine.getOptionValue("readClients"));
        int writeClientsCount = Integer.parseInt(commandLine.getOptionValue("writeClients"));
        Long runTimeMs = Long.parseLong(commandLine.getOptionValue("runTimeMs"));
        String dataKey = commandLine.getOptionValue("key");
        LOGGER.info("Data key: " + dataKey);

        int port = Integer.parseInt(commandLine.getOptionValue("port"));
        List<String> members = Arrays.asList(commandLine.getOptionValue("mIps").split(","));

        CompletableFuture<?>[] futures = new CompletableFuture[readClientsCount + writeClientsCount];

        ExecutorService pool = Executors.newFixedThreadPool(readClientsCount + writeClientsCount);

        RaftClient[] readClients = new RaftClient[readClientsCount];
        RaftClient[] writeClients = new RaftClient[writeClientsCount];

        // Delay before execution for 30 seconds
        // so all the clients will start working roughly at the same time
        Long startTime = System.currentTimeMillis() + 30000;

        LOGGER.info("Creating write clients...");
        for (int i = 0; i < writeClientsCount; i++) {
            writeClients[i] = new RaftClient(
                    port + i,
                    members,
                    CommunicationStrategy.LEADER,
                    ReadConsistency.LINEARIZABLE
            );
            futures[i] = CompletableFuture.runAsync(new ClientWriteProcess(
                    writeClients[i],
                    dataKey,
                    runTimeMs,
                    new ApplicationDetails(experimentId, applicationId + "-" + i),
                    startTime
            ), pool);
            LOGGER.info("Write process is submitted...");
        }

        for (int i = 0; i < readClientsCount; i++) {
            readClients[i] = new RaftClient(
                    port + i + writeClientsCount,
                    members,
                    CommunicationStrategy.LEADER,
                    ReadConsistency.LINEARIZABLE
            );
            futures[writeClientsCount + i] = CompletableFuture.runAsync(new ClientReadProcess(
                    readClients[i],
                    dataKey,
                    runTimeMs,
                    new ApplicationDetails(experimentId, applicationId + "-"+ i + writeClientsCount),
                    startTime
            ), pool);
            LOGGER.info("Read process is submitted...");

        }
        CompletableFuture.allOf(futures).join();

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
        Option optionIp = Option.builder("port")
                .required(true)
                .hasArg()
                .desc("Port for running raft client")
                .build();

        Option key = Option.builder("key")
                .required(true)
                .hasArg()
                .desc("Key for db")
                .build();

        Option optionMembersIps = Option.builder("mIps")
                .required(true)
                .hasArg()
                .desc("Ip list of all the members in a cluster")
                .build();

        Option appId = Option.builder("aId")
                .required(true)
                .hasArg()
                .desc("application Id")
                .build();

        Option expId = Option.builder("expId")
                .required(true)
                .hasArg()
                .build();

        Option readClients = Option.builder("readClients")
                .required(true)
                .desc("Quantity of read processes")
                .hasArg()
                .build();

        Option writeClients = Option.builder("writeClients")
                .required(true)
                .desc("Quantity of write processes")
                .hasArg()
                .build();

        Option runTime = Option.builder("runTimeMs")
                .required(true)
                .desc("One write proces and rwRation Read processes will spinUp")
                .hasArg()
                .build();

        Options options = new Options();
        options.addOption(optionIp);
        options.addOption(optionMembersIps);
        options.addOption(key);
        options.addOption(appId);
        options.addOption(expId);
        options.addOption(readClients);
        options.addOption(writeClients);
        options.addOption(runTime);
        return options;
    }
}
