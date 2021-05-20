package com.user.grpc;

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
    value = "by gRPC proto compiler (version 1.15.0)",
    comments = "Source: Listener.proto")
public final class ListenerServiceGrpc {

  private ListenerServiceGrpc() {}

  public static final String SERVICE_NAME = "ListenerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.user.grpc.Listener.secureRequest,
      com.google.protobuf.Empty> getInformAboutNewWriteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "informAboutNewWrite",
      requestType = com.user.grpc.Listener.secureRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.user.grpc.Listener.secureRequest,
      com.google.protobuf.Empty> getInformAboutNewWriteMethod() {
    io.grpc.MethodDescriptor<com.user.grpc.Listener.secureRequest, com.google.protobuf.Empty> getInformAboutNewWriteMethod;
    if ((getInformAboutNewWriteMethod = ListenerServiceGrpc.getInformAboutNewWriteMethod) == null) {
      synchronized (ListenerServiceGrpc.class) {
        if ((getInformAboutNewWriteMethod = ListenerServiceGrpc.getInformAboutNewWriteMethod) == null) {
          ListenerServiceGrpc.getInformAboutNewWriteMethod = getInformAboutNewWriteMethod = 
              io.grpc.MethodDescriptor.<com.user.grpc.Listener.secureRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ListenerService", "informAboutNewWrite"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.user.grpc.Listener.secureRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
                  .setSchemaDescriptor(new ListenerServiceMethodDescriptorSupplier("informAboutNewWrite"))
                  .build();
          }
        }
     }
     return getInformAboutNewWriteMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ListenerServiceStub newStub(io.grpc.Channel channel) {
    return new ListenerServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ListenerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ListenerServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ListenerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ListenerServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class ListenerServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void informAboutNewWrite(com.user.grpc.Listener.secureRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getInformAboutNewWriteMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getInformAboutNewWriteMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.user.grpc.Listener.secureRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_INFORM_ABOUT_NEW_WRITE)))
          .build();
    }
  }

  /**
   */
  public static final class ListenerServiceStub extends io.grpc.stub.AbstractStub<ListenerServiceStub> {
    private ListenerServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ListenerServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ListenerServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ListenerServiceStub(channel, callOptions);
    }

    /**
     */
    public void informAboutNewWrite(com.user.grpc.Listener.secureRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getInformAboutNewWriteMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ListenerServiceBlockingStub extends io.grpc.stub.AbstractStub<ListenerServiceBlockingStub> {
    private ListenerServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ListenerServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ListenerServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ListenerServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.google.protobuf.Empty informAboutNewWrite(com.user.grpc.Listener.secureRequest request) {
      return blockingUnaryCall(
          getChannel(), getInformAboutNewWriteMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ListenerServiceFutureStub extends io.grpc.stub.AbstractStub<ListenerServiceFutureStub> {
    private ListenerServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ListenerServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ListenerServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ListenerServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> informAboutNewWrite(
        com.user.grpc.Listener.secureRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getInformAboutNewWriteMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_INFORM_ABOUT_NEW_WRITE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ListenerServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ListenerServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_INFORM_ABOUT_NEW_WRITE:
          serviceImpl.informAboutNewWrite((com.user.grpc.Listener.secureRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
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

  private static abstract class ListenerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ListenerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.user.grpc.Listener.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ListenerService");
    }
  }

  private static final class ListenerServiceFileDescriptorSupplier
      extends ListenerServiceBaseDescriptorSupplier {
    ListenerServiceFileDescriptorSupplier() {}
  }

  private static final class ListenerServiceMethodDescriptorSupplier
      extends ListenerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ListenerServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (ListenerServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ListenerServiceFileDescriptorSupplier())
              .addMethod(getInformAboutNewWriteMethod())
              .build();
        }
      }
    }
    return result;
  }
}
