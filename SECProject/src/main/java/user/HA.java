package user;

import com.server.grpc.ServerService.obtLocRepReq;
import com.server.grpc.ServerService.obtLocRepReply;
import com.server.grpc.ServerService.Position;
import com.server.grpc.serverServiceGrpc;
import com.server.grpc.serverServiceGrpc.serverServiceBlockingStub;

import io.grpc.ManagedChannelBuilder;
import shared.Point2D;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class HA {

    private serverServiceBlockingStub serverStub;

    /**************************************************************************************
     * 											-HA class constructor()
     * -
     *
     * ************************************************************************************/
    public HA(int serverPort) throws Exception {
        serverStub = serverServiceGrpc.newBlockingStub(
                ManagedChannelBuilder.forAddress("127.0.0.1", serverPort).usePlaintext().build()
        ).withWaitForReady();
    }

    /**************************************************************************************
     * 											-obtainLocationReport()
     *  returns the position of specific user at specific epoch
     *  - input:
     *      - userId: ID of user to check
     *      - epoch: epoch to check
     *
     * ************************************************************************************/
 /*   public Point2D obtainLocationReport(int userId, int epoch) {

     //   obtLocRepReq reportRequest = obtLocRepReq.newBuilder().setUserID(userId).setEpoch(epoch).build();
      //  obtLocRepReply reply = serverStub.obtainLocationReportHA(reportRequest);

        return new Point2D(reply.getPos().getX(), reply.getPos().getY());
    }*/

    /**************************************************************************************
     * 											-obtainUsersAtLocation()
     *  returns list of users that were at specific position at specific epoch
     *  - input:
     *      - position: Position to search
     *      - epoch: epoch to search
     *
     * ************************************************************************************/
 /*   public List<String> obtainUsersAtLocation(Point2D position, int epoch) {

        Position pos = Position.newBuilder().setX(position.getX()).setY(position.getY()).build();
        obtUseLocReq locationRequest = obtUseLocReq.newBuilder().setPos(pos).setEpoch(epoch).build();
        obtUseLocRep reply = serverStub.obtainUsersAtLocation(locationRequest);

        return reply.getUserListList();
    }*/

 /*   public static void main(String[] args) throws Exception {

        String help = "Accept only following formats:\n\tgetReport <ID> <epoch>\n\tgetUsers <X> <Y> <epoch>";

        HA userHa = new HA(Integer.parseInt(args[0]));

        String cmd, arg1, arg2, arg3;
        Scanner sn = new Scanner(System.in);

        System.out.println(help);

        while(true) {
            cmd = sn.next().toLowerCase(Locale.ROOT);

            if (cmd.equals("getreport")) {
                arg1 = sn.next().toLowerCase(Locale.ROOT);
                arg2 = sn.next().toLowerCase(Locale.ROOT);

                try {
                    Point2D reply = userHa.obtainLocationReport(Integer.parseInt(arg1), Integer.parseInt(arg2));
                    System.out.println("Server replied: " + reply.toString());
                } catch (Exception e) {
                    System.out.println(help);
                }



            } else if (cmd.equals("getusers")) {
                arg1 = sn.next().toLowerCase(Locale.ROOT);
                arg2 = sn.next().toLowerCase(Locale.ROOT);
                arg3 = sn.next().toLowerCase(Locale.ROOT);

                try {
                    Point2D pos = new Point2D(Integer.parseInt(arg1), Integer.parseInt(arg2));
                    List<String> reply = userHa.obtainUsersAtLocation(pos, Integer.parseInt(arg3));
                    System.out.println("Server replied: " + reply.toString());

                } catch (Exception e) {
                    System.out.println(help);

                }

            } else {
                System.out.println(help);
            }

            sn.nextLine();
        }
    }*/
}
