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
      services.grpc.BotGRPC.Acknowledgement> getProcessQueryGRPCMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "processQueryGRPC",
      requestType = services.grpc.BotGRPC.Identifier.class,
      responseType = services.grpc.BotGRPC.Acknowledgement.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<services.grpc.BotGRPC.Identifier,
      services.grpc.BotGRPC.Acknowledgement> getProcessQueryGRPCMethod() {
    io.grpc.MethodDescriptor<services.grpc.BotGRPC.Identifier, services.grpc.BotGRPC.Acknowledgement> getProcessQueryGRPCMethod;
    if ((getProcessQueryGRPCMethod = BotServicesGrpc.getProcessQueryGRPCMethod) == null) {
      synchronized (BotServicesGrpc.class) {
        if ((getProcessQueryGRPCMethod = BotServicesGrpc.getProcessQueryGRPCMethod) == null) {
          BotServicesGrpc.getProcessQueryGRPCMethod = getProcessQueryGRPCMethod =
              io.grpc.MethodDescriptor.<services.grpc.BotGRPC.Identifier, services.grpc.BotGRPC.Acknowledgement>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "processQueryGRPC"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.Identifier.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.Acknowledgement.getDefaultInstance()))
              .setSchemaDescriptor(new BotServicesMethodDescriptorSupplier("processQueryGRPC"))
              .build();
        }
      }
    }
    return getProcessQueryGRPCMethod;
  }

  private static volatile io.grpc.MethodDescriptor<services.grpc.BotGRPC.BotNetworkingInformations,
      services.grpc.BotGRPC.Acknowledgement> getJoinAdvertiseGRPCMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "joinAdvertiseGRPC",
      requestType = services.grpc.BotGRPC.BotNetworkingInformations.class,
      responseType = services.grpc.BotGRPC.Acknowledgement.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<services.grpc.BotGRPC.BotNetworkingInformations,
      services.grpc.BotGRPC.Acknowledgement> getJoinAdvertiseGRPCMethod() {
    io.grpc.MethodDescriptor<services.grpc.BotGRPC.BotNetworkingInformations, services.grpc.BotGRPC.Acknowledgement> getJoinAdvertiseGRPCMethod;
    if ((getJoinAdvertiseGRPCMethod = BotServicesGrpc.getJoinAdvertiseGRPCMethod) == null) {
      synchronized (BotServicesGrpc.class) {
        if ((getJoinAdvertiseGRPCMethod = BotServicesGrpc.getJoinAdvertiseGRPCMethod) == null) {
          BotServicesGrpc.getJoinAdvertiseGRPCMethod = getJoinAdvertiseGRPCMethod =
              io.grpc.MethodDescriptor.<services.grpc.BotGRPC.BotNetworkingInformations, services.grpc.BotGRPC.Acknowledgement>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "joinAdvertiseGRPC"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.BotNetworkingInformations.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.Acknowledgement.getDefaultInstance()))
              .setSchemaDescriptor(new BotServicesMethodDescriptorSupplier("joinAdvertiseGRPC"))
              .build();
        }
      }
    }
    return getJoinAdvertiseGRPCMethod;
  }

  private static volatile io.grpc.MethodDescriptor<services.grpc.BotGRPC.BotNetworkingInformations,
      services.grpc.BotGRPC.Acknowledgement> getCrashAdvertiseGRPCMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "crashAdvertiseGRPC",
      requestType = services.grpc.BotGRPC.BotNetworkingInformations.class,
      responseType = services.grpc.BotGRPC.Acknowledgement.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<services.grpc.BotGRPC.BotNetworkingInformations,
      services.grpc.BotGRPC.Acknowledgement> getCrashAdvertiseGRPCMethod() {
    io.grpc.MethodDescriptor<services.grpc.BotGRPC.BotNetworkingInformations, services.grpc.BotGRPC.Acknowledgement> getCrashAdvertiseGRPCMethod;
    if ((getCrashAdvertiseGRPCMethod = BotServicesGrpc.getCrashAdvertiseGRPCMethod) == null) {
      synchronized (BotServicesGrpc.class) {
        if ((getCrashAdvertiseGRPCMethod = BotServicesGrpc.getCrashAdvertiseGRPCMethod) == null) {
          BotServicesGrpc.getCrashAdvertiseGRPCMethod = getCrashAdvertiseGRPCMethod =
              io.grpc.MethodDescriptor.<services.grpc.BotGRPC.BotNetworkingInformations, services.grpc.BotGRPC.Acknowledgement>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "crashAdvertiseGRPC"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.BotNetworkingInformations.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  services.grpc.BotGRPC.Acknowledgement.getDefaultInstance()))
              .setSchemaDescriptor(new BotServicesMethodDescriptorSupplier("crashAdvertiseGRPC"))
              .build();
        }
      }
    }
    return getCrashAdvertiseGRPCMethod;
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
    public void processQueryGRPC(services.grpc.BotGRPC.Identifier request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement> responseObserver) {
      asyncUnimplementedUnaryCall(getProcessQueryGRPCMethod(), responseObserver);
    }

    /**
     */
    public void joinAdvertiseGRPC(services.grpc.BotGRPC.BotNetworkingInformations request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement> responseObserver) {
      asyncUnimplementedUnaryCall(getJoinAdvertiseGRPCMethod(), responseObserver);
    }

    /**
     */
    public void crashAdvertiseGRPC(services.grpc.BotGRPC.BotNetworkingInformations request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement> responseObserver) {
      asyncUnimplementedUnaryCall(getCrashAdvertiseGRPCMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getProcessQueryGRPCMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                services.grpc.BotGRPC.Identifier,
                services.grpc.BotGRPC.Acknowledgement>(
                  this, METHODID_PROCESS_QUERY_GRPC)))
          .addMethod(
            getJoinAdvertiseGRPCMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                services.grpc.BotGRPC.BotNetworkingInformations,
                services.grpc.BotGRPC.Acknowledgement>(
                  this, METHODID_JOIN_ADVERTISE_GRPC)))
          .addMethod(
            getCrashAdvertiseGRPCMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                services.grpc.BotGRPC.BotNetworkingInformations,
                services.grpc.BotGRPC.Acknowledgement>(
                  this, METHODID_CRASH_ADVERTISE_GRPC)))
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
    public void processQueryGRPC(services.grpc.BotGRPC.Identifier request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getProcessQueryGRPCMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void joinAdvertiseGRPC(services.grpc.BotGRPC.BotNetworkingInformations request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getJoinAdvertiseGRPCMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void crashAdvertiseGRPC(services.grpc.BotGRPC.BotNetworkingInformations request,
        io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCrashAdvertiseGRPCMethod(), getCallOptions()), request, responseObserver);
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
    public services.grpc.BotGRPC.Acknowledgement processQueryGRPC(services.grpc.BotGRPC.Identifier request) {
      return blockingUnaryCall(
          getChannel(), getProcessQueryGRPCMethod(), getCallOptions(), request);
    }

    /**
     */
    public services.grpc.BotGRPC.Acknowledgement joinAdvertiseGRPC(services.grpc.BotGRPC.BotNetworkingInformations request) {
      return blockingUnaryCall(
          getChannel(), getJoinAdvertiseGRPCMethod(), getCallOptions(), request);
    }

    /**
     */
    public services.grpc.BotGRPC.Acknowledgement crashAdvertiseGRPC(services.grpc.BotGRPC.BotNetworkingInformations request) {
      return blockingUnaryCall(
          getChannel(), getCrashAdvertiseGRPCMethod(), getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<services.grpc.BotGRPC.Acknowledgement> processQueryGRPC(
        services.grpc.BotGRPC.Identifier request) {
      return futureUnaryCall(
          getChannel().newCall(getProcessQueryGRPCMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<services.grpc.BotGRPC.Acknowledgement> joinAdvertiseGRPC(
        services.grpc.BotGRPC.BotNetworkingInformations request) {
      return futureUnaryCall(
          getChannel().newCall(getJoinAdvertiseGRPCMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<services.grpc.BotGRPC.Acknowledgement> crashAdvertiseGRPC(
        services.grpc.BotGRPC.BotNetworkingInformations request) {
      return futureUnaryCall(
          getChannel().newCall(getCrashAdvertiseGRPCMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PROCESS_QUERY_GRPC = 0;
  private static final int METHODID_JOIN_ADVERTISE_GRPC = 1;
  private static final int METHODID_CRASH_ADVERTISE_GRPC = 2;

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
        case METHODID_PROCESS_QUERY_GRPC:
          serviceImpl.processQueryGRPC((services.grpc.BotGRPC.Identifier) request,
              (io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement>) responseObserver);
          break;
        case METHODID_JOIN_ADVERTISE_GRPC:
          serviceImpl.joinAdvertiseGRPC((services.grpc.BotGRPC.BotNetworkingInformations) request,
              (io.grpc.stub.StreamObserver<services.grpc.BotGRPC.Acknowledgement>) responseObserver);
          break;
        case METHODID_CRASH_ADVERTISE_GRPC:
          serviceImpl.crashAdvertiseGRPC((services.grpc.BotGRPC.BotNetworkingInformations) request,
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
              .addMethod(getProcessQueryGRPCMethod())
              .addMethod(getJoinAdvertiseGRPCMethod())
              .addMethod(getCrashAdvertiseGRPCMethod())
              .build();
        }
      }
    }
    return result;
  }
}
