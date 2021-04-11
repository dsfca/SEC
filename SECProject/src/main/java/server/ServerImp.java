package server;

import com.server.grpc.ServerService;
import com.server.grpc.ServerService.Position;
import com.server.grpc.ServerService.subLocRepReply;
import com.server.grpc.ServerService.subLocRepReq;
import com.server.grpc.ServerService.obtLocRepReq;
import com.server.grpc.ServerService.obtLocRepReply;
import com.server.grpc.ServerService.obtUseLocReq;
import com.server.grpc.ServerService.obtUseLocRep;


import com.server.grpc.serverServiceGrpc.serverServiceImplBase;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;


public class ServerImp extends serverServiceImplBase {

    /**************************************************************************************
     *                                  - submitLocationReport()
     *  RPC: server received location report from a user; handle it and send reply
     *  - input:
     *      - request: the submitLocationReportRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
    @Override
    public void submitLocationReport(subLocRepReq request, StreamObserver<subLocRepReply> responseObserver) {
        System.out.println("[Server] Report submit request from " + request.getUserID() +
                " at epoch " + request.getEpoch());

        ServerService.subLocRepReply.Builder response = submitReportHandler(request);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

    }

    /**************************************************************************************
     *                                  - obtainLocationReport()
     *  RPC: server received request from a user or HA to provide user location in specific epoch.
     *  In case RPC is called by low privileged user, return only his own location history.
     *  In case RPC is called by HA, history of any user can be returned.
     *  This method handles the requests and sends a reply
     *  - input:
     *      - request: the obtainLocationReportRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
    @Override
    public void obtainLocationReport(obtLocRepReq request, StreamObserver<obtLocRepReply> responseObserver) {
        System.out.println("[Server] Location report request from " + request.getUserID() +
                " at epoch " + request.getEpoch());

        ServerService.obtLocRepReply.Builder response = obtainReportHandler(request);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

    }

    /**************************************************************************************
     *                                  - obtainUsersAtLocation()
     *  RPC: server received request from HA to provide a list of users that were at specific
     *  time on specific location.
     *  - input:
     *      - request: the obtainUsersLocationRequest defined in serverService.proto
     *      - responseObserver: allows to respond to specific user
     *
     * ************************************************************************************/
    @Override
    public void obtainUsersAtLocation(obtUseLocReq request, StreamObserver<obtUseLocRep> responseObserver) {
        System.out.println("[Server] Users locations request from HA: users in epoch " + request.getEpoch() +
                " at location (" + request.getPos().getX() + ", " + request.getPos().getY() + ")");

        ServerService.obtUseLocRep.Builder response = obtainReportsHandler(request);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /** Possible reply codes to add into submitReportReply */
    private enum replyCode {OK, WRONG_EPOCH, NOK}
    /**************************************************************************************
     * 											- reportHandler()
     *  Handles submitLocationReportRequest received in submitLocationReport() method.
     *  It should validate the request and define appropriate content of subLocRepReply
     *  - input:
     *      - request: The submit request received from a user
     *
     * ************************************************************************************/
    private subLocRepReply.Builder submitReportHandler(subLocRepReq request) {
        subLocRepReply.Builder response = subLocRepReply.newBuilder();

        /**
         * TODO: the code below is just an example of possible reaction to request
         * */
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
        // TODO add report to database
        return true;
    }

    private boolean checkEpoch(int epoch) {
        // TODO check if current epoch is asked
        return true;
    }


    /**************************************************************************************
     * 											- reportHandler()
     *  Handles submitLocationReportRequest received in submitLocationReport() method.
     *  It should validate the request and define appropriate content of subLocRepReply
     *  - input:
     *      - request: The submit request received from a user
     *
     * ************************************************************************************/
    private obtLocRepReply.Builder obtainReportHandler(obtLocRepReq request) {
        obtLocRepReply.Builder response = obtLocRepReply.newBuilder();

        /**
         * TODO: if user is not HA and queries not his ID => permission denied
         *      else build response
         * */

        response.setPos(getUserPositionAtEpoch(request.getUserID(), request.getEpoch()));
        response.setUserID(request.getUserID());

        return response;
    }

    private boolean isHA(int userId) {
        // TODO check if he is HA
        return false;
    }

    private Position getUserPositionAtEpoch(int userId, int epoch) {
        // TODO some db query to get position (beware of byzantines)
        Position ret = Position.newBuilder().setX(-1).setY(-1).build();
        return ret;
    }

    /**************************************************************************************
     * 											- reportHandler()
     *  Handles submitLocationReportRequest received in submitLocationReport() method.
     *  It should validate the request and define appropriate content of subLocRepReply
     *  - input:
     *      - request: The submit request received from a user
     *
     * ************************************************************************************/
    private obtUseLocRep.Builder obtainReportsHandler(obtUseLocReq request) {
        obtUseLocRep.Builder response = obtUseLocRep.newBuilder();

        /**
         * TODO: if user is not HA => permission denied else build response
         * */

        response.setEpoch(request.getEpoch());
        response.addAllUserList(getUsersAtEpoch(request.getEpoch()));

        return response;
    }

    private List<String> getUsersAtEpoch(int epoch) {
        // TODO get users at epoch; beware of byzantines
        return new ArrayList<>();
    }

}
