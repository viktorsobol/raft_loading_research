package org.viktor.phd;

import com.google.common.collect.Maps;
import io.atomix.cluster.MemberId;
import io.atomix.primitive.operation.OperationType;
import io.atomix.primitive.operation.PrimitiveOperation;
import io.atomix.primitive.operation.impl.DefaultOperationId;
import io.atomix.primitive.session.SessionId;
import io.atomix.protocols.raft.RaftError;
import io.atomix.protocols.raft.ReadConsistency;
import io.atomix.protocols.raft.cluster.RaftMember;
import io.atomix.protocols.raft.cluster.impl.DefaultRaftMember;
import io.atomix.protocols.raft.protocol.*;
import io.atomix.protocols.raft.storage.log.entry.*;
import io.atomix.protocols.raft.storage.system.Configuration;
import io.atomix.utils.serializer.Namespace;
import io.atomix.utils.serializer.Serializer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Serializers {

    public static final Serializer CLIENT_SERIALIZER = Serializer.using(Namespace.builder()
            .register(ReadConsistency.class)
            .register(Maps.immutableEntry("", "").getClass())
            .build());

    public static final Serializer PROTOCOL_SERIALIZER = Serializer.using(Namespace.builder()
            .register(HeartbeatRequest.class)
            .register(HeartbeatResponse.class)
            .register(OpenSessionRequest.class)
            .register(OpenSessionResponse.class)
            .register(CloseSessionRequest.class)
            .register(CloseSessionResponse.class)
            .register(KeepAliveRequest.class)
            .register(KeepAliveResponse.class)
            .register(QueryRequest.class)
            .register(QueryResponse.class)
            .register(CommandRequest.class)
            .register(CommandResponse.class)
            .register(MetadataRequest.class)
            .register(MetadataResponse.class)
            .register(JoinRequest.class)
            .register(JoinResponse.class)
            .register(LeaveRequest.class)
            .register(LeaveResponse.class)
            .register(ConfigureRequest.class)
            .register(ConfigureResponse.class)
            .register(ReconfigureRequest.class)
            .register(ReconfigureResponse.class)
            .register(InstallRequest.class)
            .register(InstallResponse.class)
            .register(PollRequest.class)
            .register(PollResponse.class)
            .register(VoteRequest.class)
            .register(VoteResponse.class)
            .register(AppendRequest.class)
            .register(AppendResponse.class)
            .register(PublishRequest.class)
            .register(ResetRequest.class)
            .register(RaftResponse.Status.class)
            .register(RaftError.class)
            .register(RaftError.Type.class)
            .register(PrimitiveOperation.class)
            .register(ReadConsistency.class)
            .register(byte[].class)
            .register(long[].class)
            .register(CloseSessionEntry.class)
            .register(CommandEntry.class)
            .register(ConfigurationEntry.class)
            .register(InitializeEntry.class)
            .register(KeepAliveEntry.class)
            .register(MetadataEntry.class)
            .register(OpenSessionEntry.class)
            .register(QueryEntry.class)
            .register(PrimitiveOperation.class)
            .register(DefaultOperationId.class)
            .register(OperationType.class)
            .register(ReadConsistency.class)
            .register(ArrayList.class)
            .register(Collections.emptyList().getClass())
            .register(HashSet.class)
            .register(DefaultRaftMember.class)
            .register(MemberId.class)
            .register(SessionId.class)
            .register(RaftMember.Type.class)
            .register(Instant.class)
            .register(Configuration.class)
            .build());

    public static final Namespace STORAGE_NAMESPACE = Namespace.builder()
            .register(CloseSessionEntry.class)
            .register(CommandEntry.class)
            .register(ConfigurationEntry.class)
            .register(InitializeEntry.class)
            .register(KeepAliveEntry.class)
            .register(MetadataEntry.class)
            .register(OpenSessionEntry.class)
            .register(QueryEntry.class)
            .register(PrimitiveOperation.class)
            .register(DefaultOperationId.class)
            .register(OperationType.class)
            .register(ReadConsistency.class)
            .register(ArrayList.class)
            .register(HashSet.class)
            .register(DefaultRaftMember.class)
            .register(MemberId.class)
            .register(RaftMember.Type.class)
            .register(Instant.class)
            .register(Configuration.class)
            .register(byte[].class)
            .register(long[].class)
            .build();
}
