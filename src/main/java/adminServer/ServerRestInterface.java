package adminServer;

import cleaningBot.Position;
import beans.BotIdentity;
import beans.BotPositions;
import beans.AverageList;
import extra.Logger.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * REST class for the Administration server. This is the REST class that
 * handles incoming connections for the administration server.
 */
@Path("admin")
public class ServerRestInterface {

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
        String jsonString = BotPositions.getInstance().jsonBuilder(null);
        return Response.ok(jsonString).build();
    }

    /**
     * This function deletes a bot from the city.
     * @return The return function is a value that can be 200 ok if the bot was actually
     * present in the city, and it was correctly deleted from the administration server.
     */
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

    @Path("number")
    @GET
    public Response getNumber() {
        return Response.ok(String.valueOf(AdminServer.getStack().size())).build();
    }

    @Path("measurements/bot/{id}/{number}")
    @GET
    public Response getAvgMeasurement(@PathParam("id") int robotID,
                                      @PathParam("number") int numberOfMeasurements) {

        List<AverageList> averageLists = AdminServer.getStack();

        if(averageLists.isEmpty()) {
            return Response.serverError().build();
        }

        int total = 0;
        float averageOfAverages = 0f;
        for (int i = averageLists.size() - 1; i >= 0; --i) {
            AverageList averageList = averageLists.get(i);
            if(averageList.getIdentity() == robotID) {
                int listSize = averageList.getAverages().size();
                if(total + listSize <= numberOfMeasurements) {
                    System.out.println("total + listSize <= numberOfMeasurements");
                    for (Float average : averageList.getAverages()) {
                        System.out.println("average: " + average);
                        averageOfAverages += average;
                        System.out.println(averageOfAverages);
                    }
                    total += listSize;
                }
                else {
                    System.out.println("total + listSize > numberOfMeasurements");
                    int reminder = total + listSize - numberOfMeasurements;
                    if(reminder > 0) {
                        for(int j = averageList.getAverages().size() - 1; j >= reminder; --j) {
                            System.out.println("averageList.getAverages.get " + averageList.getAverages().get(j));
                            averageOfAverages += averageList.getAverages().get(j);
                            System.out.println(averageOfAverages);
                        }
                    }
                    averageOfAverages = averageOfAverages / numberOfMeasurements;
                    return Response.ok(String.valueOf(averageOfAverages)).build();
                }
            }
        }
        return Response.serverError().build();
    }

    @Path("measurements/timestamp/{beginning}/{ending}")
    @GET
    public Response getAvgMeasurement(@PathParam("beginning") long beginning,
                                      @PathParam("ending") long ending) {

        List<AverageList> averageLists = AdminServer.getStack();

        if(beginning == -1) {
            beginning = 0;
        }
        if(ending == -1) {
            ending = averageLists.get(averageLists.size() - 1).getTimestamp();
        }

        if(averageLists.isEmpty()) {
            return Response.serverError().build();
        }

        int total = 0;
        float averageOfAverages = 0f;
        for (int i = averageLists.size() - 1; i >= 0; --i) {
            AverageList averageList = averageLists.get(i);
            if(beginning <= averageList.getTimestamp() && ending >= averageList.getTimestamp()) {
                for (Float average : averageList.getAverages()) {
                    System.out.println("average: " + average);
                    averageOfAverages += average;
                    System.out.println(averageOfAverages);
                }
                total += averageList.getSize();
            }
        }
        if(total != 0) {
            return Response.ok(averageOfAverages / total).build();
        }
        return Response.serverError().build();
    }
}
