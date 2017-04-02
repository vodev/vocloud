package cz.ivoa.vocloud.rest;

import cz.ivoa.vocloud.ejb.TokenAuthBean;
import cz.ivoa.vocloud.entity.AuthToken;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by radiokoza on 1.4.17.
 */
@Stateless
@Path("/jupyter")
public class JupyterAuthRS {

    @EJB
    private TokenAuthBean tokenAuthBean;

    private Response renderError(String errorMessage) {
        return Response.status(400).entity(new ErrorEntity(errorMessage)).build();
    }

    @Path("/token")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response consumeToken(@FormParam("token") String token) {
        if (token == null) {
            return renderError("Missing 'token' parameter");
        }
        AuthToken tokenObj = tokenAuthBean.consumeToken(token);
        if (tokenObj == null) {
            //token is invalid or expired
            return renderError("Invalid or expired token");
        }
        //token is valid
        return Response.status(200).header("Cache-Control", "no-store").entity(tokenObj).build();
    }


    private static class ErrorEntity {
        private String error;

        public ErrorEntity(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }

}
