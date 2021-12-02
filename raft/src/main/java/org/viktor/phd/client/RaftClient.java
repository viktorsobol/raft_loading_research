package org.viktor.phd.client;

import io.atomix.cluster.Member;
import io.atomix.cluster.MemberId;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.MessagingService;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.core.map.DistributedMapType;
import io.atomix.primitive.PrimitiveBuilder;
import io.atomix.primitive.PrimitiveManagementService;
import io.atomix.primitive.PrimitiveType;
import io.atomix.primitive.config.PrimitiveConfig;
import io.atomix.primitive.event.EventType;
import io.atomix.primitive.operation.OperationId;
import io.atomix.primitive.operation.PrimitiveOperation;
import io.atomix.primitive.partition.PartitionId;
import io.atomix.primitive.service.*;
import io.atomix.primitive.session.SessionClient;
import io.atomix.protocols.raft.ReadConsistency;
import io.atomix.protocols.raft.protocol.RaftClientProtocol;
import io.atomix.protocols.raft.session.CommunicationStrategy;
import io.atomix.utils.concurrent.ThreadModel;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.viktor.phd.apps.Client;
import org.viktor.phd.messaging.RaftClientMessagingProtocol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.atomix.primitive.operation.PrimitiveOperation.operation;
import static org.viktor.phd.Serializers.CLIENT_SERIALIZER;
import static org.viktor.phd.Serializers.PROTOCOL_SERIALIZER;

public class RaftClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RaftClient.class);

    public static final OperationId PUT = OperationId.command("put");
    public static final OperationId GET = OperationId.query("get");
    public static final OperationId REMOVE = OperationId.command("remove");
    public static final OperationId INDEX = OperationId.command("index");

    private static final ReadConsistency READ_CONSISTENCY = ReadConsistency.SEQUENTIAL;
    private static final CommunicationStrategy COMMUNICATION_STRATEGY = CommunicationStrategy.FOLLOWERS;

    private final io.atomix.protocols.raft.RaftClient raftClient;
    private final SessionClient sessionClient;
    private final Map<MemberId, Address> clusterAddressMap = new ConcurrentHashMap<>();


    public RaftClient(Integer port, List<String>  clusterIps) {
        this(port, clusterIps, COMMUNICATION_STRATEGY, READ_CONSISTENCY);
    }

    public RaftClient(Integer port, List<String> clusterIps, CommunicationStrategy strategy, ReadConsistency consistency) {
        Address clientAddress = Address.from(port);

        Member clientMember = Member.builder(MemberId.from("Client - " + String.valueOf(port)))
                .withAddress(clientAddress)
                .build();

        for (String ip : clusterIps) {
            clusterAddressMap.put(MemberId.from(ip), Address.from(ip));
        }

        MessagingService messagingService = new NettyMessagingService("Raft Cluster", clientMember.address(), new MessagingConfig()).start().join();
        RaftClientProtocol protocol = new RaftClientMessagingProtocol(messagingService, PROTOCOL_SERIALIZER, clusterAddressMap::get);

        io.atomix.protocols.raft.RaftClient client = io.atomix.protocols.raft.RaftClient.builder()
                .withMemberId(clientMember.id())
                .withPartitionId(PartitionId.from("test", 1))
                .withProtocol(protocol)
                .withThreadModel(ThreadModel.SHARED_THREAD_POOL)
                .build();

        raftClient = client.connect(clusterIps.stream().map(MemberId::from).collect(Collectors.toList())).join();

        sessionClient = client
                .sessionBuilder(
                        "raft-performance-test",
                        TestPrimitiveType.INSTANCE,
                        new ServiceConfig()
                )
                .withReadConsistency(consistency)
                .withCommunicationStrategy(strategy)
                .build();

        sessionClient.connect().whenComplete((c, er) -> {
            if (er != null) {
                LOGGER.error("", er);
            }
            LOGGER.info("Session connect is completed for raft client on port " + port);
        }).join();
    }

    public void shutDown() {
        sessionClient.close().join();
        raftClient.close().join();
    }


    public CompletableFuture<byte[]> execute(PrimitiveOperation operation) {
        return sessionClient.execute(operation);
    }

    public static class TestPrimitiveType implements PrimitiveType {
        private static final TestPrimitiveType INSTANCE = new TestPrimitiveType();

        @Override
        public String name() {
            return "raft-performance-test";
        }

        @Override
        public PrimitiveConfig newConfig() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrimitiveBuilder newBuilder(String primitiveName, PrimitiveConfig config, PrimitiveManagementService managementService) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrimitiveService newService(ServiceConfig config) {
            return new PerformanceService();
        }
    }

    public static class PerformanceService extends AbstractPrimitiveService {
        private Map<String, String> map = new HashMap<>();

        public PerformanceService() {
            super(TestPrimitiveType.INSTANCE);
        }

        @Override
        public Serializer serializer() {
            return CLIENT_SERIALIZER;
        }

        @Override
        protected void configure(ServiceExecutor executor) {
            executor.register(PUT, this::put);
            executor.register(GET, this::get);
            executor.register(REMOVE, this::remove);
            executor.register(INDEX, this::index);
        }

        @Override
        public void backup(BackupOutput writer) {
            writer.writeInt(map.size());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                writer.writeString(entry.getKey());
                writer.writeString(entry.getValue());
            }
        }

        @Override
        public void restore(BackupInput reader) {
            map = new HashMap<>();
            int size = reader.readInt();
            for (int i = 0; i < size; i++) {
                String key = reader.readString();
                String value = reader.readString();
                map.put(key, value);
            }
        }

        protected long put(Commit<Map.Entry<String, String>> commit) {
            map.put(commit.value().getKey(), commit.value().getValue());
            return commit.index();
        }

        protected String get(Commit<String> commit) {
            return map.get(commit.value());
        }

        protected long remove(Commit<String> commit) {
            map.remove(commit.value());
            return commit.index();
        }

        protected long index(Commit<Void> commit) {
            return commit.index();
        }
    }
}
