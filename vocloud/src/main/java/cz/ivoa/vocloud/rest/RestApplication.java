package cz.ivoa.vocloud.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Created by radiokoza on 1.4.17.
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
    //no implementation here renders automatic scanning for jax-rs annotated resources
}
