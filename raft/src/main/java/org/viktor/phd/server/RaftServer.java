package org.viktor.phd.server;

import com.google.common.collect.Lists;
import io.atomix.cluster.BootstrapService;
import io.atomix.cluster.Member;
import io.atomix.cluster.MemberId;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.cluster.impl.DefaultClusterMembershipService;
import io.atomix.cluster.impl.DefaultNodeDiscoveryService;
import io.atomix.cluster.messaging.*;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.cluster.protocol.HeartbeatMembershipProtocol;
import io.atomix.cluster.protocol.HeartbeatMembershipProtocolConfig;
import io.atomix.protocols.raft.protocol.RaftServerProtocol;
import io.atomix.protocols.raft.storage.RaftStorage;
import io.atomix.storage.StorageLevel;
import io.atomix.utils.Version;
import io.atomix.utils.concurrent.ThreadModel;
import io.atomix.utils.net.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.viktor.phd.messaging.RaftServerMessagingProtocol;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static org.viktor.phd.Serializers.PROTOCOL_SERIALIZER;
import static org.viktor.phd.Serializers.STORAGE_NAMESPACE;

public class RaftServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RaftServer.class);

    private ManagedMessagingService messagingService;

    private final Map<MemberId, Address> addressMap = new HashMap<>();

    private final io.atomix.protocols.raft.RaftServer server;

    private final  List<MemberId> memberIds = new ArrayList<>();

    public RaftServer(String selfIp, List<String> memberIps) {
        this(selfIp, memberIps, 3000L);
    }

    // memberIps must contain all ips including the one currently created
    public RaftServer(String selfIp, List<String> memberIps, Long waitTimeBeforeBootstrapMs) {
        LOGGER.info("Attempting to create a raft server...");

        Address selfAddress = Address.from(selfIp);
        Member member = Member.builder(MemberId.from(String.valueOf(selfIp)))
                .withAddress(selfAddress)
                .build();

        List<Member> members = memberIps.stream().map(m -> {
                    Address address = Address.from(m);

            Member build = Member.builder(MemberId.from(String.valueOf(m)))
                    .withAddress(address)
                    .build();
            addressMap.put(build.id(), address);
            memberIds.add(build.id());
            return build;

        }
        ).collect(toList());

        server = createServer(member, Lists.newArrayList(members));
        try {
            Thread.sleep(waitTimeBeforeBootstrapMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<io.atomix.protocols.raft.RaftServer> bootstrap() {
        return server.bootstrap(memberIds).whenComplete((raftServer, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Unable to create Raft Server", throwable);
            } else {
                LOGGER.info("Raft server was created successfully");
            }
        });
    }

    public void shutdown() {
        try {
            if (server.isRunning()) {
                server.shutdown().get(1, TimeUnit.SECONDS);
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isRunning() {
        return server.isRunning();
    }

    private io.atomix.protocols.raft.RaftServer createServer(Member member, List<Node> members) {
        RaftServerProtocol protocol;
        messagingService = (ManagedMessagingService) new NettyMessagingService("Raft Cluster", member.address(), new MessagingConfig())
                .start()
                .join();
        protocol = new RaftServerMessagingProtocol(messagingService, PROTOCOL_SERIALIZER, addressMap::get);


        BootstrapService bootstrapService = new BootstrapService() {
            @Override
            public MessagingService getMessagingService() {
                return messagingService;
            }

            @Override
            public UnicastService getUnicastService() {
                return new UnicastServiceAdapter();
            }

            @Override
            public BroadcastService getBroadcastService() {
                return new BroadcastServiceAdapter();
            }
        };

        Version from = Version.from("1.0.0");
        HeartbeatMembershipProtocol protocol1 = new HeartbeatMembershipProtocol(new HeartbeatMembershipProtocolConfig());
        DefaultClusterMembershipService membershipService = new DefaultClusterMembershipService(
                member,
                from,
                new DefaultNodeDiscoveryService(bootstrapService, member, new BootstrapDiscoveryProvider(members)),
                bootstrapService,
                protocol1);
        io.atomix.protocols.raft.RaftServer.Builder builder = io.atomix.protocols.raft.RaftServer.builder(member.id())
                .withProtocol(protocol)
                .withThreadModel(ThreadModel.THREAD_PER_SERVICE)
                .withMembershipService(membershipService)
                .withStorage(RaftStorage.builder()
                        .withStorageLevel(StorageLevel.DISK)
                        .withDirectory(new File(String.format("raft/%s", member.id())))
                        .withNamespace(STORAGE_NAMESPACE)
                        .withMaxSegmentSize(1024 * 1024 * 64)
                        .withDynamicCompaction()
                        .withFlushOnCommit(false)
                        .build());

        return builder.build();
    }

    private static class UnicastServiceAdapter implements UnicastService {
        @Override
        public void unicast(Address address, String subject, byte[] message) {

        }

        @Override
        public void addListener(String subject, BiConsumer<Address, byte[]> listener, Executor executor) {

        }

        @Override
        public void removeListener(String subject, BiConsumer<Address, byte[]> listener) {

        }
    }

    private static class BroadcastServiceAdapter implements BroadcastService {
        @Override
        public void broadcast(String subject, byte[] message) {

        }

        @Override
        public void addListener(String subject, Consumer<byte[]> listener) {

        }

        @Override
        public void removeListener(String subject, Consumer<byte[]> listener) {

        }
    }


}


