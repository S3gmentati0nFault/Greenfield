package adminServer;

import beans.BotIdentity;
import beans.BotPositions;
import extra.Position.Position;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("admin")
public class AdminServer {
    @Path("join")
    @POST
    @Consumes({"application/json"})
    public Response joinBot(BotIdentity identity) {
        Position botPosition = BotPositions.getInstance().joinBot(identity);
        if(botPosition == null){
            return Response.serverError().build();
        }
        else{
            String jsonString = BotPositions.getInstance().jsonBuilder(botPosition);
            return Response.ok(jsonString).build();
        }
    }

    @Path("bots")
    @GET
    public Response getBots() {
        System.out.println(BotPositions.getInstance().getBotPositioning());
        return Response.ok().build();
    }

//    @Path("remove")
//    @DELETE
//    @Consumes({"application/json"})
//    public Response deleteBot(BotIdentity identity) {
//        System.out.println("1");
//        BotPositions.getInstance().deleteBot(identity);
//        if(true){
//            return Response.ok().build();
//        }
//        else{
//            return Response.serverError().build();
//        }
//    }

    @Path("measurements/{id}/{number}")
    @GET
    public Response getAvgMeasurement(@PathParam("id") int robotID,
                                      @PathParam("number") int numberOfMeasurements) {
        return Response.ok().build();
    }
}
