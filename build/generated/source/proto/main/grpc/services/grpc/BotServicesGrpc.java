package services.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.25.0)",
    comments = "Source: BotGRPC.proto")
public final class BotServicesGrpc {

  private BotServicesGrpc() {}

  public static final String SERVICE_NAME = "services.grpc.BotServices";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<services.grpc.BotGRPC.Identifier,
      services.grpc.BotGRPC.Acknowledgement> getMaintenanceRequestGRPCMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "maintenanceRequestGRPC",
      requestType = services.grpc.BotGRPC.Identifier.class,
      responseType = services.grpc.BotGRPC.Acknowledgement.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<services.grpc.BotGRPC.Identifier,
      services.grpc.BotGRPC.Acknowledgement> getMaintenanceRequestGRPCMethod() {
    io.grpc.MethodDescriptor<services.grpc.BotGRPC.Identifier, services.grpc.BotGRPC.Acknowledgement> getMaintenanceRequestGRPCMethod;
    if ((getMaintenanceRequestGRPCMethod = BotServicesGrpc.getMaintenanceRequestGRPCMethod) == null) {
      synchronized (BotServicesGrpc.class) {
        if ((getMaintenanceRequestGRPCMethod = BotServicesGrpc.getMaintenanceRequestGRPCMethod) == null) {
          BotServicesGrpc.getMaintenanceRequestGRPCMethod = getMaintenanceRequestGRPCMethod =
              io.grpc.MethodDescriptor.<services.grpc.BotGRPC.Identifier, services.grpc.BotGRPC.Acknowledgement>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "maintenanceRequestGRPC"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.Identifier.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.Acknowledgement.getDefaultInstance()))
              .setSchemaDescriptor(new BotServicesMethodDescriptorSupplier("maintenanceRequestGRPC"))
              .build();
        }
      }
    }
    return getMaintenanceRequestGRPCMethod;
  }

  private static volatile io.grpc.MethodDescriptor<services.grpc.BotGRPC.BotNetworkingInformations,
      services.grpc.BotGRPC.Acknowledgement> getJoinRequestGRPCMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "joinRequestGRPC",
      requestType = services.grpc.BotGRPC.BotNetworkingInformations.class,
      responseType = services.grpc.BotGRPC.Acknowledgement.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<services.grpc.BotGRPC.BotNetworkingInformations,
      services.grpc.BotGRPC.Acknowledgement> getJoinRequestGRPCMethod() {
    io.grpc.MethodDescriptor<services.grpc.BotGRPC.BotNetworkingInformations, services.grpc.BotGRPC.Acknowledgement> getJoinRequestGRPCMethod;
    if ((getJoinRequestGRPCMethod = BotServicesGrpc.getJoinRequestGRPCMethod) == null) {
      synchronized (BotServicesGrpc.class) {
        if ((getJoinRequestGRPCMethod = BotServicesGrpc.getJoinRequestGRPCMethod) == null) {
          BotServicesGrpc.getJoinRequestGRPCMethod = getJoinRequestGRPCMethod =
              io.grpc.MethodDescriptor.<services.grpc.BotGRPC.BotNetworkingInformations, services.grpc.BotGRPC.Acknowledgement>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "joinRequestGRPC"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.BotNetworkingInformations.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.Acknowledgement.getDefaultInstance()))
              .setSchemaDescriptor(new BotServicesMethodDescriptorSupplier("joinRequestGRPC"))
              .build();
        }
      }
    }
    return getJoinRequestGRPCMethod;
  }

  private static volatile io.grpc.MethodDescriptor<services.grpc.BotGRPC.BotNetworkingInformations,
      services.grpc.BotGRPC.IntegerValue> getCrashNotificationGRPCMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "crashNotificationGRPC",
      requestType = services.grpc.BotGRPC.BotNetworkingInformations.class,
      responseType = services.grpc.BotGRPC.IntegerValue.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<services.grpc.BotGRPC.BotNetworkingInformations,
      services.grpc.BotGRPC.IntegerValue> getCrashNotificationGRPCMethod() {
    io.grpc.MethodDescriptor<services.grpc.BotGRPC.BotNetworkingInformations, services.grpc.BotGRPC.IntegerValue> getCrashNotificationGRPCMethod;
    if ((getCrashNotificationGRPCMethod = BotServicesGrpc.getCrashNotificationGRPCMethod) == null) {
      synchronized (BotServicesGrpc.class) {
        if ((getCrashNotificationGRPCMethod = BotServicesGrpc.getCrashNotificationGRPCMethod) == null) {
          BotServicesGrpc.getCrashNotificationGRPCMethod = getCrashNotificationGRPCMethod =
              io.grpc.MethodDescriptor.<services.grpc.BotGRPC.BotNetworkingInformations, services.grpc.BotGRPC.IntegerValue>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "crashNotificationGRPC"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.BotNetworkingInformations.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.IntegerValue.getDefaultInstance()))
              .setSchemaDescriptor(new BotServicesMethodDescriptorSupplier("crashNotificationGRPC"))
              .build();
        }
      }
    }
    return getCrashNotificationGRPCMethod;
  }

  private static volatile io.grpc.MethodDescriptor<services.grpc.BotGRPC.Position,
      services.grpc.BotGRPC.Acknowledgement> getMoveRequestGRPCMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "moveRequestGRPC",
      requestType = services.grpc.BotGRPC.Position.class,
      responseType = services.grpc.BotGRPC.Acknowledgement.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<services.grpc.BotGRPC.Position,
      services.grpc.BotGRPC.Acknowledgement> getMoveRequestGRPCMethod() {
    io.grpc.MethodDescriptor<services.grpc.BotGRPC.Position, services.grpc.BotGRPC.Acknowledgement> getMoveRequestGRPCMethod;
    if ((getMoveRequestGRPCMethod = BotServicesGrpc.getMoveRequestGRPCMethod) == null) {
      synchronized (BotServicesGrpc.class) {
        if ((getMoveRequestGRPCMethod = BotServicesGrpc.getMoveRequestGRPCMethod) == null) {
          BotServicesGrpc.getMoveRequestGRPCMethod = getMoveRequestGRPCMethod =
              io.grpc.MethodDescriptor.<services.grpc.BotGRPC.Position, services.grpc.BotGRPC.Acknowledgement>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "moveRequestGRPC"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.Position.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.Acknowledgement.getDefaultInstance()))
              .setSchemaDescriptor(new BotServicesMethodDescriptorSupplier("moveRequestGRPC"))
              .build();
        }
      }
    }
    return getMoveRequestGRPCMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BotServicesStub newStub(io.grpc.Channel channel) {
    return new BotServicesStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BotServicesBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new BotServicesBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static BotServicesFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new BotServicesFutureStub(channel);
  }

  /**
   */
  public static abstract class BotServicesImplBase implements io.grpc.BindableService {

    /**
     */
    public void maintenanceRequestGRPC(services.grpc.BotGRPC.Identifier request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement> responseObserver) {
      asyncUnimplementedUnaryCall(getMaintenanceRequestGRPCMethod(), responseObserver);
    }

    /**
     */
    public void joinRequestGRPC(services.grpc.BotGRPC.BotNetworkingInformations request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement> responseObserver) {
      asyncUnimplementedUnaryCall(getJoinRequestGRPCMethod(), responseObserver);
    }

    /**
     */
    public void crashNotificationGRPC(services.grpc.BotGRPC.BotNetworkingInformations request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.IntegerValue> responseObserver) {
      asyncUnimplementedUnaryCall(getCrashNotificationGRPCMethod(), responseObserver);
    }

    /**
     */
    public void moveRequestGRPC(services.grpc.BotGRPC.Position request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement> responseObserver) {
      asyncUnimplementedUnaryCall(getMoveRequestGRPCMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getMaintenanceRequestGRPCMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                services.grpc.BotGRPC.Identifier,
                services.grpc.BotGRPC.Acknowledgement>(
                  this, METHODID_MAINTENANCE_REQUEST_GRPC)))
          .addMethod(
            getJoinRequestGRPCMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                services.grpc.BotGRPC.BotNetworkingInformations,
                services.grpc.BotGRPC.Acknowledgement>(
                  this, METHODID_JOIN_REQUEST_GRPC)))
          .addMethod(
            getCrashNotificationGRPCMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                services.grpc.BotGRPC.BotNetworkingInformations,
                services.grpc.BotGRPC.IntegerValue>(
                  this, METHODID_CRASH_NOTIFICATION_GRPC)))
          .addMethod(
            getMoveRequestGRPCMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                services.grpc.BotGRPC.Position,
                services.grpc.BotGRPC.Acknowledgement>(
                  this, METHODID_MOVE_REQUEST_GRPC)))
          .build();
    }
  }

  /**
   */
  public static final class BotServicesStub extends io.grpc.stub.AbstractStub<BotServicesStub> {
    private BotServicesStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BotServicesStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BotServicesStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BotServicesStub(channel, callOptions);
    }

    /**
     */
    public void maintenanceRequestGRPC(services.grpc.BotGRPC.Identifier request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getMaintenanceRequestGRPCMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void joinRequestGRPC(services.grpc.BotGRPC.BotNetworkingInformations request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getJoinRequestGRPCMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void crashNotificationGRPC(services.grpc.BotGRPC.BotNetworkingInformations request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.IntegerValue> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCrashNotificationGRPCMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void moveRequestGRPC(services.grpc.BotGRPC.Position request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getMoveRequestGRPCMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class BotServicesBlockingStub extends io.grpc.stub.AbstractStub<BotServicesBlockingStub> {
    private BotServicesBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BotServicesBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BotServicesBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BotServicesBlockingStub(channel, callOptions);
    }

    /**
     */
    public services.grpc.BotGRPC.Acknowledgement maintenanceRequestGRPC(services.grpc.BotGRPC.Identifier request) {
      return blockingUnaryCall(
          getChannel(), getMaintenanceRequestGRPCMethod(), getCallOptions(), request);
    }

    /**
     */
    public services.grpc.BotGRPC.Acknowledgement joinRequestGRPC(services.grpc.BotGRPC.BotNetworkingInformations request) {
      return blockingUnaryCall(
          getChannel(), getJoinRequestGRPCMethod(), getCallOptions(), request);
    }

    /**
     */
    public services.grpc.BotGRPC.IntegerValue crashNotificationGRPC(services.grpc.BotGRPC.BotNetworkingInformations request) {
      return blockingUnaryCall(
          getChannel(), getCrashNotificationGRPCMethod(), getCallOptions(), request);
    }

    /**
     */
    public services.grpc.BotGRPC.Acknowledgement moveRequestGRPC(services.grpc.BotGRPC.Position request) {
      return blockingUnaryCall(
          getChannel(), getMoveRequestGRPCMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class BotServicesFutureStub extends io.grpc.stub.AbstractStub<BotServicesFutureStub> {
    private BotServicesFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BotServicesFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BotServicesFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BotServicesFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<services.grpc.BotGRPC.Acknowledgement> maintenanceRequestGRPC(
        services.grpc.BotGRPC.Identifier request) {
      return futureUnaryCall(
          getChannel().newCall(getMaintenanceRequestGRPCMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<services.grpc.BotGRPC.Acknowledgement> joinRequestGRPC(
        services.grpc.BotGRPC.BotNetworkingInformations request) {
      return futureUnaryCall(
          getChannel().newCall(getJoinRequestGRPCMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<services.grpc.BotGRPC.IntegerValue> crashNotificationGRPC(
        services.grpc.BotGRPC.BotNetworkingInformations request) {
      return futureUnaryCall(
          getChannel().newCall(getCrashNotificationGRPCMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<services.grpc.BotGRPC.Acknowledgement> moveRequestGRPC(
        services.grpc.BotGRPC.Position request) {
      return futureUnaryCall(
          getChannel().newCall(getMoveRequestGRPCMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_MAINTENANCE_REQUEST_GRPC = 0;
  private static final int METHODID_JOIN_REQUEST_GRPC = 1;
  private static final int METHODID_CRASH_NOTIFICATION_GRPC = 2;
  private static final int METHODID_MOVE_REQUEST_GRPC = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final BotServicesImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(BotServicesImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_MAINTENANCE_REQUEST_GRPC:
          serviceImpl.maintenanceRequestGRPC((services.grpc.BotGRPC.Identifier) request,
              (io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement>) responseObserver);
          break;
        case METHODID_JOIN_REQUEST_GRPC:
          serviceImpl.joinRequestGRPC((services.grpc.BotGRPC.BotNetworkingInformations) request,
              (io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement>) responseObserver);
          break;
        case METHODID_CRASH_NOTIFICATION_GRPC:
          serviceImpl.crashNotificationGRPC((services.grpc.BotGRPC.BotNetworkingInformations) request,
              (io.grpc.stub.StreamObserver<services.grpc.BotGRPC.IntegerValue>) responseObserver);
          break;
        case METHODID_MOVE_REQUEST_GRPC:
          serviceImpl.moveRequestGRPC((services.grpc.BotGRPC.Position) request,
              (io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class BotServicesBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    BotServicesBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return services.grpc.BotGRPC.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("BotServices");
    }
  }

  private static final class BotServicesFileDescriptorSupplier
      extends BotServicesBaseDescriptorSupplier {
    BotServicesFileDescriptorSupplier() {}
  }

  private static final class BotServicesMethodDescriptorSupplier
      extends BotServicesBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    BotServicesMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (BotServicesGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new BotServicesFileDescriptorSupplier())
              .addMethod(getMaintenanceRequestGRPCMethod())
              .addMethod(getJoinRequestGRPCMethod())
              .addMethod(getCrashNotificationGRPCMethod())
              .addMethod(getMoveRequestGRPCMethod())
              .build();
        }
      }
    }
    return result;
  }
}
