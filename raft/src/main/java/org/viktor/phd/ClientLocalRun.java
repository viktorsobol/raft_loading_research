package org.viktor.phd;

import io.atomix.protocols.raft.ReadConsistency;
import io.atomix.protocols.raft.session.CommunicationStrategy;
import org.viktor.phd.client.ApplicationDetails;
import org.viktor.phd.client.ClientReadProcess;
import org.viktor.phd.client.ClientWriteProcess;
import org.viktor.phd.client.RaftClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientLocalRun {

    public static void main(String[] args) throws InterruptedException {
        RaftClient readClient = new RaftClient(6000,
                List.of("localhost:6777","localhost:6778","localhost:6779"),
                CommunicationStrategy.FOLLOWERS,
                ReadConsistency.SEQUENTIAL
                );

        RaftClient readClient1 = new RaftClient(6002,
                List.of("localhost:6777","localhost:6778","localhost:6779"),
                CommunicationStrategy.FOLLOWERS,
                ReadConsistency.SEQUENTIAL
        );


        RaftClient writeClient = new RaftClient(6001,
                List.of("localhost:6777","localhost:6778","localhost:6779"),
                CommunicationStrategy.FOLLOWERS,
                ReadConsistency.SEQUENTIAL
        );
        CompletableFuture<Void> asyncRead = CompletableFuture
                .runAsync(
                        new ClientReadProcess(
                                readClient,
                                "test5",
                                10000L,
                                new ApplicationDetails("local", "main")));

        CompletableFuture<Void> asyncRead1 = CompletableFuture
                .runAsync(
                        new ClientReadProcess(
                                readClient1,
                                "test5",
                                10000L,
                                new ApplicationDetails("local", "main")));


        CompletableFuture<Void> asyncWrite = CompletableFuture
                .runAsync(
                        new ClientWriteProcess(
                                writeClient,
                                "test5",
                                10000L,
                                new ApplicationDetails("local", "main")));
        asyncRead.join();
        asyncRead1.join();
        asyncWrite.join();

        System.out.println("Finished processing write");
        readClient.shutDown();


    }
}
