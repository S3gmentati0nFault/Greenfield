package adminServer;

import java.util.ArrayList;
import java.util.List;

import beans.BotIdentity;
import beans.BotPositions;
import cleaningBot.CleaningBot;
import extra.Position.Position;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("admin")
public class AdminServer {
    List<CleaningBot> bots;

    public AdminServer(){
        bots = new ArrayList<CleaningBot>();
    }

    @Path("join")
    @POST
    @Consumes({"application/json", "application/xml"})
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
    public Response getBots(){
        System.out.println(BotPositions.getInstance().getBotPositioning());
        return Response.ok().build();
    }
}
