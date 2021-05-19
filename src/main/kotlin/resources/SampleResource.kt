package resources

import javax.ws.rs.GET
import javax.ws.rs.Path



@Path("/")
class SampleResource {

    @Path("/ok")
    @GET
    fun test() : String{
        return "ok";
    }
}