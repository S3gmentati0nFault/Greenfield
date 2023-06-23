package adminServer;

import beans.BotIdentity;
import beans.BotPositions;
import extra.Logger.Logger;
import extra.Position.Position;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * REST class for the Administration server. This is the REST class that
 * handles incoming connections for the administration server.
 */
@Path("admin")
public class AdminServer {

    /**
     * This function handles the addition of a new bot to the network.
     * @param identity the identity of the bot, this is an object containing
     *                 the bot's ID
     *                 the bot's port
     *                 the bot's ip address
     * @return The return value is an HTTP response which is either 200 ok or
     * Error
     */
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

    /**
     * This function returns the set of bots registered within the city.
     * @return The return value is an HTTP response which is only 200 ok.
     */
    @Path("bots")
    @GET
    public Response getBots() {
        System.out.println(BotPositions.getInstance().getBotPositioning());
        return Response.ok().build();
    }

   @Path("remove")
   @DELETE
   @Consumes({"application/json"})
   public Response deleteBot(BotIdentity botIdentity) {
       Logger.blue("DELETE");
        if(!BotPositions.getInstance().deleteBot(botIdentity)) {
            return Response.noContent().build();
        }
        return Response.ok().build();
   }

    @Path("measurements/{id}/{number}")
    @GET
    public Response getAvgMeasurement(@PathParam("id") int robotID,
                                      @PathParam("number") int numberOfMeasurements) {
        return Response.ok().build();
    }
}
