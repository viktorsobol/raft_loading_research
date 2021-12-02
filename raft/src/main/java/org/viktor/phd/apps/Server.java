package org.viktor.phd.apps;

import org.apache.commons.cli.*;
import org.viktor.phd.server.RaftServer;

import java.util.Arrays;
import java.util.List;

public class Server {

    public void run(String[] args) throws ParseException, InterruptedException {

        CommandLine commandLine;

        Option optionIp = Option.builder("ip")
                .required(true)
                .hasArg()
                .desc("IP Address of node running raft server")
                .build();

        Option optionMembersIps = Option.builder("mIps")
                .required(true)
                .hasArg()
                .desc("Ip list of all the members in a cluster")
                .build();

        Options options = new Options();
        options.addOption(optionIp);
        options.addOption(optionMembersIps);

        CommandLineParser parser = new DefaultParser();
        commandLine = parser.parse(options, args);

        String ip = commandLine.getOptionValue("ip");
        List<String> members = Arrays.asList(commandLine.getOptionValue("mIps").split(","));

        RaftServer server = new RaftServer(ip, members);
        server.bootstrap();
        while(server.isRunning()) {
            Thread.sleep(100);
        }
        server.shutdown();
    }
}
