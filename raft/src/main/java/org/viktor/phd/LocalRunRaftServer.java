package org.viktor.phd;

import org.viktor.phd.server.RaftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LocalRunRaftServer {

    public static void main(String[] args) throws InterruptedException {
        run();
    }

    private static void run() throws InterruptedException {
        List<RaftServer> servers = new ArrayList<>();


        var s1 = new RaftServer("localhost:6777", List.of("localhost:6777", "localhost:6778", "localhost:6779"));
        var s2 = new RaftServer("localhost:6778", List.of("localhost:6777", "localhost:6778", "localhost:6779"));
        var s3 = new RaftServer("localhost:6779", List.of("localhost:6777", "localhost:6778", "localhost:6779"));
        servers.add(s1);
        servers.add(s2);
        servers.add(s3);
        CountDownLatch latch = new CountDownLatch(3);
        System.out.println("Starting bootstrap process");
        for (int i = 0; i < 3; i++) {
            var s = servers.get(i);
            s.bootstrap().thenRun(latch::countDown);
        }

        latch.await(30000, TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Time is up");

        servers.forEach(RaftServer::shutdown);

    }
}
