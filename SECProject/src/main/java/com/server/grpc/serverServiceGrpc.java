package com.server.grpc;

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
    comments = "Source: serverService.proto")
public final class serverServiceGrpc {

  private serverServiceGrpc() {}

  public static final String SERVICE_NAME = "serverService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest,
      com.server.grpc.ServerService.secureReplay> getSubmitLocationReportMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "submitLocationReport",
      requestType = com.server.grpc.ServerService.secureRequest.class,
      responseType = com.server.grpc.ServerService.secureReplay.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest,
      com.server.grpc.ServerService.secureReplay> getSubmitLocationReportMethod() {
    io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest, com.server.grpc.ServerService.secureReplay> getSubmitLocationReportMethod;
    if ((getSubmitLocationReportMethod = serverServiceGrpc.getSubmitLocationReportMethod) == null) {
      synchronized (serverServiceGrpc.class) {
        if ((getSubmitLocationReportMethod = serverServiceGrpc.getSubmitLocationReportMethod) == null) {
          serverServiceGrpc.getSubmitLocationReportMethod = getSubmitLocationReportMethod = 
              io.grpc.MethodDescriptor.<com.server.grpc.ServerService.secureRequest, com.server.grpc.ServerService.secureReplay>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "serverService", "submitLocationReport"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.server.grpc.ServerService.secureRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.server.grpc.ServerService.secureReplay.getDefaultInstance()))
                  .setSchemaDescriptor(new serverServiceMethodDescriptorSupplier("submitLocationReport"))
                  .build();
          }
        }
     }
     return getSubmitLocationReportMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest,
      com.google.protobuf.Empty> getSubmitReportEchoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "submitReportEcho",
      requestType = com.server.grpc.ServerService.secureRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest,
      com.google.protobuf.Empty> getSubmitReportEchoMethod() {
    io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest, com.google.protobuf.Empty> getSubmitReportEchoMethod;
    if ((getSubmitReportEchoMethod = serverServiceGrpc.getSubmitReportEchoMethod) == null) {
      synchronized (serverServiceGrpc.class) {
        if ((getSubmitReportEchoMethod = serverServiceGrpc.getSubmitReportEchoMethod) == null) {
          serverServiceGrpc.getSubmitReportEchoMethod = getSubmitReportEchoMethod = 
              io.grpc.MethodDescriptor.<com.server.grpc.ServerService.secureRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "serverService", "submitReportEcho"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.server.grpc.ServerService.secureRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
                  .setSchemaDescriptor(new serverServiceMethodDescriptorSupplier("submitReportEcho"))
                  .build();
          }
        }
     }
     return getSubmitReportEchoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest,
      com.google.protobuf.Empty> getSubmitReportReadyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "submitReportReady",
      requestType = com.server.grpc.ServerService.secureRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest,
      com.google.protobuf.Empty> getSubmitReportReadyMethod() {
    io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest, com.google.protobuf.Empty> getSubmitReportReadyMethod;
    if ((getSubmitReportReadyMethod = serverServiceGrpc.getSubmitReportReadyMethod) == null) {
      synchronized (serverServiceGrpc.class) {
        if ((getSubmitReportReadyMethod = serverServiceGrpc.getSubmitReportReadyMethod) == null) {
          serverServiceGrpc.getSubmitReportReadyMethod = getSubmitReportReadyMethod = 
              io.grpc.MethodDescriptor.<com.server.grpc.ServerService.secureRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "serverService", "submitReportReady"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.server.grpc.ServerService.secureRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
                  .setSchemaDescriptor(new serverServiceMethodDescriptorSupplier("submitReportReady"))
                  .build();
          }
        }
     }
     return getSubmitReportReadyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest,
      com.server.grpc.ServerService.secureReplay> getObtainLocationReportMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "obtainLocationReport",
      requestType = com.server.grpc.ServerService.secureRequest.class,
      responseType = com.server.grpc.ServerService.secureReplay.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest,
      com.server.grpc.ServerService.secureReplay> getObtainLocationReportMethod() {
    io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest, com.server.grpc.ServerService.secureReplay> getObtainLocationReportMethod;
    if ((getObtainLocationReportMethod = serverServiceGrpc.getObtainLocationReportMethod) == null) {
      synchronized (serverServiceGrpc.class) {
        if ((getObtainLocationReportMethod = serverServiceGrpc.getObtainLocationReportMethod) == null) {
          serverServiceGrpc.getObtainLocationReportMethod = getObtainLocationReportMethod = 
              io.grpc.MethodDescriptor.<com.server.grpc.ServerService.secureRequest, com.server.grpc.ServerService.secureReplay>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "serverService", "obtainLocationReport"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.server.grpc.ServerService.secureRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.server.grpc.ServerService.secureReplay.getDefaultInstance()))
                  .setSchemaDescriptor(new serverServiceMethodDescriptorSupplier("obtainLocationReport"))
                  .build();
          }
        }
     }
     return getObtainLocationReportMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest,
      com.server.grpc.ServerService.secureReplay> getObtainLocationReportHAMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "obtainLocationReportHA",
      requestType = com.server.grpc.ServerService.secureRequest.class,
      responseType = com.server.grpc.ServerService.secureReplay.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest,
      com.server.grpc.ServerService.secureReplay> getObtainLocationReportHAMethod() {
    io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest, com.server.grpc.ServerService.secureReplay> getObtainLocationReportHAMethod;
    if ((getObtainLocationReportHAMethod = serverServiceGrpc.getObtainLocationReportHAMethod) == null) {
      synchronized (serverServiceGrpc.class) {
        if ((getObtainLocationReportHAMethod = serverServiceGrpc.getObtainLocationReportHAMethod) == null) {
          serverServiceGrpc.getObtainLocationReportHAMethod = getObtainLocationReportHAMethod = 
              io.grpc.MethodDescriptor.<com.server.grpc.ServerService.secureRequest, com.server.grpc.ServerService.secureReplay>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "serverService", "obtainLocationReportHA"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.server.grpc.ServerService.secureRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.server.grpc.ServerService.secureReplay.getDefaultInstance()))
                  .setSchemaDescriptor(new serverServiceMethodDescriptorSupplier("obtainLocationReportHA"))
                  .build();
          }
        }
     }
     return getObtainLocationReportHAMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest,
      com.server.grpc.ServerService.secureReplay> getObtainUsersAtLocationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "obtainUsersAtLocation",
      requestType = com.server.grpc.ServerService.secureRequest.class,
      responseType = com.server.grpc.ServerService.secureReplay.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest,
      com.server.grpc.ServerService.secureReplay> getObtainUsersAtLocationMethod() {
    io.grpc.MethodDescriptor<com.server.grpc.ServerService.secureRequest, com.server.grpc.ServerService.secureReplay> getObtainUsersAtLocationMethod;
    if ((getObtainUsersAtLocationMethod = serverServiceGrpc.getObtainUsersAtLocationMethod) == null) {
      synchronized (serverServiceGrpc.class) {
        if ((getObtainUsersAtLocationMethod = serverServiceGrpc.getObtainUsersAtLocationMethod) == null) {
          serverServiceGrpc.getObtainUsersAtLocationMethod = getObtainUsersAtLocationMethod = 
              io.grpc.MethodDescriptor.<com.server.grpc.ServerService.secureRequest, com.server.grpc.ServerService.secureReplay>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "serverService", "obtainUsersAtLocation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.server.grpc.ServerService.secureRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.server.grpc.ServerService.secureReplay.getDefaultInstance()))
                  .setSchemaDescriptor(new serverServiceMethodDescriptorSupplier("obtainUsersAtLocation"))
                  .build();
          }
        }
     }
     return getObtainUsersAtLocationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.server.grpc.ServerService.DHKeyExcReq,
      com.server.grpc.ServerService.DHKeyExcRep> getDHKeyExchangeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DHKeyExchange",
      requestType = com.server.grpc.ServerService.DHKeyExcReq.class,
      responseType = com.server.grpc.ServerService.DHKeyExcRep.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.server.grpc.ServerService.DHKeyExcReq,
      com.server.grpc.ServerService.DHKeyExcRep> getDHKeyExchangeMethod() {
    io.grpc.MethodDescriptor<com.server.grpc.ServerService.DHKeyExcReq, com.server.grpc.ServerService.DHKeyExcRep> getDHKeyExchangeMethod;
    if ((getDHKeyExchangeMethod = serverServiceGrpc.getDHKeyExchangeMethod) == null) {
      synchronized (serverServiceGrpc.class) {
        if ((getDHKeyExchangeMethod = serverServiceGrpc.getDHKeyExchangeMethod) == null) {
          serverServiceGrpc.getDHKeyExchangeMethod = getDHKeyExchangeMethod = 
              io.grpc.MethodDescriptor.<com.server.grpc.ServerService.DHKeyExcReq, com.server.grpc.ServerService.DHKeyExcRep>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "serverService", "DHKeyExchange"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.server.grpc.ServerService.DHKeyExcReq.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.server.grpc.ServerService.DHKeyExcRep.getDefaultInstance()))
                  .setSchemaDescriptor(new serverServiceMethodDescriptorSupplier("DHKeyExchange"))
                  .build();
          }
        }
     }
     return getDHKeyExchangeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static serverServiceStub newStub(io.grpc.Channel channel) {
    return new serverServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static serverServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new serverServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static serverServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new serverServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class serverServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void submitLocationReport(com.server.grpc.ServerService.secureRequest request,
        io.grpc.stub.StreamObserver<com.server.grpc.ServerService.secureReplay> responseObserver) {
      asyncUnimplementedUnaryCall(getSubmitLocationReportMethod(), responseObserver);
    }

    /**
     */
    public void submitReportEcho(com.server.grpc.ServerService.secureRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getSubmitReportEchoMethod(), responseObserver);
    }

    /**
     */
    public void submitReportReady(com.server.grpc.ServerService.secureRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getSubmitReportReadyMethod(), responseObserver);
    }

    /**
     */
    public void obtainLocationReport(com.server.grpc.ServerService.secureRequest request,
        io.grpc.stub.StreamObserver<com.server.grpc.ServerService.secureReplay> responseObserver) {
      asyncUnimplementedUnaryCall(getObtainLocationReportMethod(), responseObserver);
    }

    /**
     */
    public void obtainLocationReportHA(com.server.grpc.ServerService.secureRequest request,
        io.grpc.stub.StreamObserver<com.server.grpc.ServerService.secureReplay> responseObserver) {
      asyncUnimplementedUnaryCall(getObtainLocationReportHAMethod(), responseObserver);
    }

    /**
     */
    public void obtainUsersAtLocation(com.server.grpc.ServerService.secureRequest request,
        io.grpc.stub.StreamObserver<com.server.grpc.ServerService.secureReplay> responseObserver) {
      asyncUnimplementedUnaryCall(getObtainUsersAtLocationMethod(), responseObserver);
    }

    /**
     */
    public void dHKeyExchange(com.server.grpc.ServerService.DHKeyExcReq request,
        io.grpc.stub.StreamObserver<com.server.grpc.ServerService.DHKeyExcRep> responseObserver) {
      asyncUnimplementedUnaryCall(getDHKeyExchangeMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSubmitLocationReportMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.server.grpc.ServerService.secureRequest,
                com.server.grpc.ServerService.secureReplay>(
                  this, METHODID_SUBMIT_LOCATION_REPORT)))
          .addMethod(
            getSubmitReportEchoMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.server.grpc.ServerService.secureRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_SUBMIT_REPORT_ECHO)))
          .addMethod(
            getSubmitReportReadyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.server.grpc.ServerService.secureRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_SUBMIT_REPORT_READY)))
          .addMethod(
            getObtainLocationReportMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.server.grpc.ServerService.secureRequest,
                com.server.grpc.ServerService.secureReplay>(
                  this, METHODID_OBTAIN_LOCATION_REPORT)))
          .addMethod(
            getObtainLocationReportHAMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.server.grpc.ServerService.secureRequest,
                com.server.grpc.ServerService.secureReplay>(
                  this, METHODID_OBTAIN_LOCATION_REPORT_HA)))
          .addMethod(
            getObtainUsersAtLocationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.server.grpc.ServerService.secureRequest,
                com.server.grpc.ServerService.secureReplay>(
                  this, METHODID_OBTAIN_USERS_AT_LOCATION)))
          .addMethod(
            getDHKeyExchangeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.server.grpc.ServerService.DHKeyExcReq,
                com.server.grpc.ServerService.DHKeyExcRep>(
                  this, METHODID_DHKEY_EXCHANGE)))
          .build();
    }
  }

  /**
   */
  public static final class serverServiceStub extends io.grpc.stub.AbstractStub<serverServiceStub> {
    private serverServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private serverServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected serverServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new serverServiceStub(channel, callOptions);
    }

    /**
     */
    public void submitLocationReport(com.server.grpc.ServerService.secureRequest request,
        io.grpc.stub.StreamObserver<com.server.grpc.ServerService.secureReplay> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSubmitLocationReportMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void submitReportEcho(com.server.grpc.ServerService.secureRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSubmitReportEchoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void submitReportReady(com.server.grpc.ServerService.secureRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSubmitReportReadyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void obtainLocationReport(com.server.grpc.ServerService.secureRequest request,
        io.grpc.stub.StreamObserver<com.server.grpc.ServerService.secureReplay> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getObtainLocationReportMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void obtainLocationReportHA(com.server.grpc.ServerService.secureRequest request,
        io.grpc.stub.StreamObserver<com.server.grpc.ServerService.secureReplay> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getObtainLocationReportHAMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void obtainUsersAtLocation(com.server.grpc.ServerService.secureRequest request,
        io.grpc.stub.StreamObserver<com.server.grpc.ServerService.secureReplay> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getObtainUsersAtLocationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dHKeyExchange(com.server.grpc.ServerService.DHKeyExcReq request,
        io.grpc.stub.StreamObserver<com.server.grpc.ServerService.DHKeyExcRep> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDHKeyExchangeMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class serverServiceBlockingStub extends io.grpc.stub.AbstractStub<serverServiceBlockingStub> {
    private serverServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private serverServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected serverServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new serverServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.server.grpc.ServerService.secureReplay submitLocationReport(com.server.grpc.ServerService.secureRequest request) {
      return blockingUnaryCall(
          getChannel(), getSubmitLocationReportMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty submitReportEcho(com.server.grpc.ServerService.secureRequest request) {
      return blockingUnaryCall(
          getChannel(), getSubmitReportEchoMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty submitReportReady(com.server.grpc.ServerService.secureRequest request) {
      return blockingUnaryCall(
          getChannel(), getSubmitReportReadyMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.server.grpc.ServerService.secureReplay obtainLocationReport(com.server.grpc.ServerService.secureRequest request) {
      return blockingUnaryCall(
          getChannel(), getObtainLocationReportMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.server.grpc.ServerService.secureReplay obtainLocationReportHA(com.server.grpc.ServerService.secureRequest request) {
      return blockingUnaryCall(
          getChannel(), getObtainLocationReportHAMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.server.grpc.ServerService.secureReplay obtainUsersAtLocation(com.server.grpc.ServerService.secureRequest request) {
      return blockingUnaryCall(
          getChannel(), getObtainUsersAtLocationMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.server.grpc.ServerService.DHKeyExcRep dHKeyExchange(com.server.grpc.ServerService.DHKeyExcReq request) {
      return blockingUnaryCall(
          getChannel(), getDHKeyExchangeMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class serverServiceFutureStub extends io.grpc.stub.AbstractStub<serverServiceFutureStub> {
    private serverServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private serverServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected serverServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new serverServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.server.grpc.ServerService.secureReplay> submitLocationReport(
        com.server.grpc.ServerService.secureRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSubmitLocationReportMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> submitReportEcho(
        com.server.grpc.ServerService.secureRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSubmitReportEchoMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> submitReportReady(
        com.server.grpc.ServerService.secureRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSubmitReportReadyMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.server.grpc.ServerService.secureReplay> obtainLocationReport(
        com.server.grpc.ServerService.secureRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getObtainLocationReportMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.server.grpc.ServerService.secureReplay> obtainLocationReportHA(
        com.server.grpc.ServerService.secureRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getObtainLocationReportHAMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.server.grpc.ServerService.secureReplay> obtainUsersAtLocation(
        com.server.grpc.ServerService.secureRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getObtainUsersAtLocationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.server.grpc.ServerService.DHKeyExcRep> dHKeyExchange(
        com.server.grpc.ServerService.DHKeyExcReq request) {
      return futureUnaryCall(
          getChannel().newCall(getDHKeyExchangeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SUBMIT_LOCATION_REPORT = 0;
  private static final int METHODID_SUBMIT_REPORT_ECHO = 1;
  private static final int METHODID_SUBMIT_REPORT_READY = 2;
  private static final int METHODID_OBTAIN_LOCATION_REPORT = 3;
  private static final int METHODID_OBTAIN_LOCATION_REPORT_HA = 4;
  private static final int METHODID_OBTAIN_USERS_AT_LOCATION = 5;
  private static final int METHODID_DHKEY_EXCHANGE = 6;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final serverServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(serverServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SUBMIT_LOCATION_REPORT:
          serviceImpl.submitLocationReport((com.server.grpc.ServerService.secureRequest) request,
              (io.grpc.stub.StreamObserver<com.server.grpc.ServerService.secureReplay>) responseObserver);
          break;
        case METHODID_SUBMIT_REPORT_ECHO:
          serviceImpl.submitReportEcho((com.server.grpc.ServerService.secureRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_SUBMIT_REPORT_READY:
          serviceImpl.submitReportReady((com.server.grpc.ServerService.secureRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_OBTAIN_LOCATION_REPORT:
          serviceImpl.obtainLocationReport((com.server.grpc.ServerService.secureRequest) request,
              (io.grpc.stub.StreamObserver<com.server.grpc.ServerService.secureReplay>) responseObserver);
          break;
        case METHODID_OBTAIN_LOCATION_REPORT_HA:
          serviceImpl.obtainLocationReportHA((com.server.grpc.ServerService.secureRequest) request,
              (io.grpc.stub.StreamObserver<com.server.grpc.ServerService.secureReplay>) responseObserver);
          break;
        case METHODID_OBTAIN_USERS_AT_LOCATION:
          serviceImpl.obtainUsersAtLocation((com.server.grpc.ServerService.secureRequest) request,
              (io.grpc.stub.StreamObserver<com.server.grpc.ServerService.secureReplay>) responseObserver);
          break;
        case METHODID_DHKEY_EXCHANGE:
          serviceImpl.dHKeyExchange((com.server.grpc.ServerService.DHKeyExcReq) request,
              (io.grpc.stub.StreamObserver<com.server.grpc.ServerService.DHKeyExcRep>) responseObserver);
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

  private static abstract class serverServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    serverServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.server.grpc.ServerService.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("serverService");
    }
  }

  private static final class serverServiceFileDescriptorSupplier
      extends serverServiceBaseDescriptorSupplier {
    serverServiceFileDescriptorSupplier() {}
  }

  private static final class serverServiceMethodDescriptorSupplier
      extends serverServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    serverServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (serverServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new serverServiceFileDescriptorSupplier())
              .addMethod(getSubmitLocationReportMethod())
              .addMethod(getSubmitReportEchoMethod())
              .addMethod(getSubmitReportReadyMethod())
              .addMethod(getObtainLocationReportMethod())
              .addMethod(getObtainLocationReportHAMethod())
              .addMethod(getObtainUsersAtLocationMethod())
              .addMethod(getDHKeyExchangeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
