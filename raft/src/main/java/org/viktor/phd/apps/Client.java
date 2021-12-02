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

        ExecutorService pool = Executors.newFixedThreadPool(10);

        Options options = getOptions();
        CommandLine commandLine;

        CommandLineParser parser = new DefaultParser();
        commandLine = parser.parse(options, args);

        String experimentId = commandLine.getOptionValue("expId");
        String applicationId = commandLine.getOptionValue("aId");
        int rwRatio = Integer.parseInt(commandLine.getOptionValue("rwRatio"));
        Long runTimeMs = Long.parseLong(commandLine.getOptionValue("runTimeMs"));
        String dataKey = commandLine.getOptionValue("key");
        LOGGER.info("Data key: " + dataKey);

        int port = Integer.parseInt(commandLine.getOptionValue("port"));
        List<String> members = Arrays.asList(commandLine.getOptionValue("mIps").split(","));

        CompletableFuture<?>[] futures = new CompletableFuture[rwRatio + 1];

        RaftClient writeClient =
                new RaftClient(
                        port,
                        members,
                        CommunicationStrategy.LEADER,
                        ReadConsistency.SEQUENTIAL
                );
        LOGGER.info("Write client is created");

        RaftClient[] readClients = new RaftClient[rwRatio];

        futures[0] = CompletableFuture.runAsync(new ClientWriteProcess(
                writeClient,
                dataKey,
                runTimeMs,
                new ApplicationDetails(experimentId, applicationId)
        ), pool);
        LOGGER.info("Write process is submitted...");
        for (int i = 1; i <= rwRatio; i++) {
            readClients[i - 1] = new RaftClient(
                    port + i,
                    members,
                    CommunicationStrategy.FOLLOWERS,
                    ReadConsistency.SEQUENTIAL
            );
            futures[i] = CompletableFuture.runAsync(new ClientReadProcess(
                    readClients[i - 1],
                    dataKey,
                    runTimeMs,
                    new ApplicationDetails(experimentId, applicationId + i)
            ), pool);
            LOGGER.info("Read process is submitted...");

        }
        CompletableFuture.allOf(futures).join();

        LOGGER.info("All processes have completed");

        writeClient.shutDown();
        for (RaftClient readClient: readClients) {
            readClient.shutDown();
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

        Option rwRatio = Option.builder("rwRatio")
                .required(true)
                .desc("One write proces and rwRation Read processes will spinUp")
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
        options.addOption(rwRatio);
        options.addOption(runTime);
        return options;
    }
}
