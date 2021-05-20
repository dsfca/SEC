// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Listener.proto

package com.user.grpc;

public final class Listener {
  private Listener() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface secureRequestOrBuilder extends
      // @@protoc_insertion_point(interface_extends:secureRequest)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>int32 serverID = 1;</code>
     */
    int getServerID();

    /**
     * <code>string confidentMessage = 2;</code>
     */
    java.lang.String getConfidentMessage();
    /**
     * <code>string confidentMessage = 2;</code>
     */
    com.google.protobuf.ByteString
        getConfidentMessageBytes();

    /**
     * <code>string MessageDigitalSignature = 3;</code>
     */
    java.lang.String getMessageDigitalSignature();
    /**
     * <code>string MessageDigitalSignature = 3;</code>
     */
    com.google.protobuf.ByteString
        getMessageDigitalSignatureBytes();
  }
  /**
   * Protobuf type {@code secureRequest}
   */
  public  static final class secureRequest extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:secureRequest)
      secureRequestOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use secureRequest.newBuilder() to construct.
    private secureRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private secureRequest() {
      serverID_ = 0;
      confidentMessage_ = "";
      messageDigitalSignature_ = "";
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private secureRequest(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {

              serverID_ = input.readInt32();
              break;
            }
            case 18: {
              java.lang.String s = input.readStringRequireUtf8();

              confidentMessage_ = s;
              break;
            }
            case 26: {
              java.lang.String s = input.readStringRequireUtf8();

              messageDigitalSignature_ = s;
              break;
            }
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.user.grpc.Listener.internal_static_secureRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.user.grpc.Listener.internal_static_secureRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.user.grpc.Listener.secureRequest.class, com.user.grpc.Listener.secureRequest.Builder.class);
    }

    public static final int SERVERID_FIELD_NUMBER = 1;
    private int serverID_;
    /**
     * <code>int32 serverID = 1;</code>
     */
    public int getServerID() {
      return serverID_;
    }

    public static final int CONFIDENTMESSAGE_FIELD_NUMBER = 2;
    private volatile java.lang.Object confidentMessage_;
    /**
     * <code>string confidentMessage = 2;</code>
     */
    public java.lang.String getConfidentMessage() {
      java.lang.Object ref = confidentMessage_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        confidentMessage_ = s;
        return s;
      }
    }
    /**
     * <code>string confidentMessage = 2;</code>
     */
    public com.google.protobuf.ByteString
        getConfidentMessageBytes() {
      java.lang.Object ref = confidentMessage_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        confidentMessage_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int MESSAGEDIGITALSIGNATURE_FIELD_NUMBER = 3;
    private volatile java.lang.Object messageDigitalSignature_;
    /**
     * <code>string MessageDigitalSignature = 3;</code>
     */
    public java.lang.String getMessageDigitalSignature() {
      java.lang.Object ref = messageDigitalSignature_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        messageDigitalSignature_ = s;
        return s;
      }
    }
    /**
     * <code>string MessageDigitalSignature = 3;</code>
     */
    public com.google.protobuf.ByteString
        getMessageDigitalSignatureBytes() {
      java.lang.Object ref = messageDigitalSignature_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        messageDigitalSignature_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (serverID_ != 0) {
        output.writeInt32(1, serverID_);
      }
      if (!getConfidentMessageBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 2, confidentMessage_);
      }
      if (!getMessageDigitalSignatureBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 3, messageDigitalSignature_);
      }
      unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (serverID_ != 0) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, serverID_);
      }
      if (!getConfidentMessageBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, confidentMessage_);
      }
      if (!getMessageDigitalSignatureBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, messageDigitalSignature_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof com.user.grpc.Listener.secureRequest)) {
        return super.equals(obj);
      }
      com.user.grpc.Listener.secureRequest other = (com.user.grpc.Listener.secureRequest) obj;

      boolean result = true;
      result = result && (getServerID()
          == other.getServerID());
      result = result && getConfidentMessage()
          .equals(other.getConfidentMessage());
      result = result && getMessageDigitalSignature()
          .equals(other.getMessageDigitalSignature());
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + SERVERID_FIELD_NUMBER;
      hash = (53 * hash) + getServerID();
      hash = (37 * hash) + CONFIDENTMESSAGE_FIELD_NUMBER;
      hash = (53 * hash) + getConfidentMessage().hashCode();
      hash = (37 * hash) + MESSAGEDIGITALSIGNATURE_FIELD_NUMBER;
      hash = (53 * hash) + getMessageDigitalSignature().hashCode();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static com.user.grpc.Listener.secureRequest parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.user.grpc.Listener.secureRequest parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.user.grpc.Listener.secureRequest parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.user.grpc.Listener.secureRequest parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.user.grpc.Listener.secureRequest parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.user.grpc.Listener.secureRequest parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.user.grpc.Listener.secureRequest parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.user.grpc.Listener.secureRequest parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.user.grpc.Listener.secureRequest parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static com.user.grpc.Listener.secureRequest parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.user.grpc.Listener.secureRequest parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.user.grpc.Listener.secureRequest parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(com.user.grpc.Listener.secureRequest prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code secureRequest}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:secureRequest)
        com.user.grpc.Listener.secureRequestOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.user.grpc.Listener.internal_static_secureRequest_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.user.grpc.Listener.internal_static_secureRequest_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.user.grpc.Listener.secureRequest.class, com.user.grpc.Listener.secureRequest.Builder.class);
      }

      // Construct using com.user.grpc.Listener.secureRequest.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        serverID_ = 0;

        confidentMessage_ = "";

        messageDigitalSignature_ = "";

        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.user.grpc.Listener.internal_static_secureRequest_descriptor;
      }

      @java.lang.Override
      public com.user.grpc.Listener.secureRequest getDefaultInstanceForType() {
        return com.user.grpc.Listener.secureRequest.getDefaultInstance();
      }

      @java.lang.Override
      public com.user.grpc.Listener.secureRequest build() {
        com.user.grpc.Listener.secureRequest result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public com.user.grpc.Listener.secureRequest buildPartial() {
        com.user.grpc.Listener.secureRequest result = new com.user.grpc.Listener.secureRequest(this);
        result.serverID_ = serverID_;
        result.confidentMessage_ = confidentMessage_;
        result.messageDigitalSignature_ = messageDigitalSignature_;
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return (Builder) super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.user.grpc.Listener.secureRequest) {
          return mergeFrom((com.user.grpc.Listener.secureRequest)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.user.grpc.Listener.secureRequest other) {
        if (other == com.user.grpc.Listener.secureRequest.getDefaultInstance()) return this;
        if (other.getServerID() != 0) {
          setServerID(other.getServerID());
        }
        if (!other.getConfidentMessage().isEmpty()) {
          confidentMessage_ = other.confidentMessage_;
          onChanged();
        }
        if (!other.getMessageDigitalSignature().isEmpty()) {
          messageDigitalSignature_ = other.messageDigitalSignature_;
          onChanged();
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.user.grpc.Listener.secureRequest parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.user.grpc.Listener.secureRequest) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private int serverID_ ;
      /**
       * <code>int32 serverID = 1;</code>
       */
      public int getServerID() {
        return serverID_;
      }
      /**
       * <code>int32 serverID = 1;</code>
       */
      public Builder setServerID(int value) {
        
        serverID_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>int32 serverID = 1;</code>
       */
      public Builder clearServerID() {
        
        serverID_ = 0;
        onChanged();
        return this;
      }

      private java.lang.Object confidentMessage_ = "";
      /**
       * <code>string confidentMessage = 2;</code>
       */
      public java.lang.String getConfidentMessage() {
        java.lang.Object ref = confidentMessage_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          confidentMessage_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string confidentMessage = 2;</code>
       */
      public com.google.protobuf.ByteString
          getConfidentMessageBytes() {
        java.lang.Object ref = confidentMessage_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          confidentMessage_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string confidentMessage = 2;</code>
       */
      public Builder setConfidentMessage(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        confidentMessage_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string confidentMessage = 2;</code>
       */
      public Builder clearConfidentMessage() {
        
        confidentMessage_ = getDefaultInstance().getConfidentMessage();
        onChanged();
        return this;
      }
      /**
       * <code>string confidentMessage = 2;</code>
       */
      public Builder setConfidentMessageBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        
        confidentMessage_ = value;
        onChanged();
        return this;
      }

      private java.lang.Object messageDigitalSignature_ = "";
      /**
       * <code>string MessageDigitalSignature = 3;</code>
       */
      public java.lang.String getMessageDigitalSignature() {
        java.lang.Object ref = messageDigitalSignature_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          messageDigitalSignature_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>string MessageDigitalSignature = 3;</code>
       */
      public com.google.protobuf.ByteString
          getMessageDigitalSignatureBytes() {
        java.lang.Object ref = messageDigitalSignature_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          messageDigitalSignature_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string MessageDigitalSignature = 3;</code>
       */
      public Builder setMessageDigitalSignature(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        messageDigitalSignature_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string MessageDigitalSignature = 3;</code>
       */
      public Builder clearMessageDigitalSignature() {
        
        messageDigitalSignature_ = getDefaultInstance().getMessageDigitalSignature();
        onChanged();
        return this;
      }
      /**
       * <code>string MessageDigitalSignature = 3;</code>
       */
      public Builder setMessageDigitalSignatureBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        
        messageDigitalSignature_ = value;
        onChanged();
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:secureRequest)
    }

    // @@protoc_insertion_point(class_scope:secureRequest)
    private static final com.user.grpc.Listener.secureRequest DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new com.user.grpc.Listener.secureRequest();
    }

    public static com.user.grpc.Listener.secureRequest getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<secureRequest>
        PARSER = new com.google.protobuf.AbstractParser<secureRequest>() {
      @java.lang.Override
      public secureRequest parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new secureRequest(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<secureRequest> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<secureRequest> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public com.user.grpc.Listener.secureRequest getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_secureRequest_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_secureRequest_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016Listener.proto\032\033google/protobuf/empty." +
      "proto\"\\\n\rsecureRequest\022\020\n\010serverID\030\001 \001(\005" +
      "\022\030\n\020confidentMessage\030\002 \001(\t\022\037\n\027MessageDig" +
      "italSignature\030\003 \001(\t2P\n\017ListenerService\022=" +
      "\n\023informAboutNewWrite\022\016.secureRequest\032\026." +
      "google.protobuf.EmptyB\017\n\rcom.user.grpcb\006" +
      "proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.EmptyProto.getDescriptor(),
        }, assigner);
    internal_static_secureRequest_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_secureRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_secureRequest_descriptor,
        new java.lang.String[] { "ServerID", "ConfidentMessage", "MessageDigitalSignature", });
    com.google.protobuf.EmptyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}