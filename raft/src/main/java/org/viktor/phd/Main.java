package org.viktor.phd;

import org.apache.commons.cli.*;
import org.viktor.phd.apps.Client;
import org.viktor.phd.apps.Server;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws ParseException, InterruptedException {
        CommandLine commandLine;

        Option applicationType = Option.builder("type")
                .required(true)
                .hasArg()
                .desc("Server | Client")
                .build();

        Options options = new Options();
        options.addOption(applicationType);

        DefaultParser parser = new DefaultParser();
        commandLine = parser.parse(options, args, true);

        String type = commandLine.getOptionValue("type");

        switch (type) {
            case "Server":
                new Server().run(Arrays.stream(args).skip(2).toArray(String[]::new));
                break;
            case "Client":
                new Client().run(Arrays.stream(args).skip(2).toArray(String[]::new));
                break;
        }

    }
}
