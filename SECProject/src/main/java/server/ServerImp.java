package server;

import com.server.grpc.ServerService;
import com.server.grpc.ServerService.subLocRepReply;
import com.server.grpc.ServerService.subLocRepReq;

import com.server.grpc.serverServiceGrpc.serverServiceImplBase;
import io.grpc.stub.StreamObserver;


public class ServerImp extends serverServiceImplBase {

    private enum replyCode {OK, WRONG_EPOCH, NOK}

    @Override
    public void submitLocationReport(subLocRepReq request, StreamObserver<subLocRepReply> responseObserver) {
        System.out.println("[Server] Report submit request from " + request.getUserID() +
                           " at epoch " + request.getEpoch());

        ServerService.subLocRepReply.Builder response = reportHandler(request);
        responseObserver.onNext(response.build());

        responseObserver.onCompleted();

    }

    private subLocRepReply.Builder reportHandler(subLocRepReq request) {
        subLocRepReply.Builder response = subLocRepReply.newBuilder();

        if (!checkEpoch(request.getEpoch())) {
            response.setReplycode(replyCode.WRONG_EPOCH.ordinal());
            response.setReplymessage("Provided report is not valid for current epoch");
        }

        if (!submitReport()) {
            response.setReplycode(replyCode.NOK.ordinal());
            response.setReplymessage("Your report was not submitted");
        }

        response.setReplycode(replyCode.OK.ordinal());
        response.setReplymessage("Your report was submitted successfully");

        return response;
    }

    private boolean submitReport() {
        return true;
    }

    private boolean checkEpoch(String epoch) {
        return true;
    }
}
