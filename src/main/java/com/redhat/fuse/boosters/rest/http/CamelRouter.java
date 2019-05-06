package com.redhat.fuse.boosters.rest.http;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

/**
 * A simple Camel REST DSL route that implements the greetings service.
 * 
 */
@Component
public class CamelRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // @formatter:off
        restConfiguration()
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Shinsei Veirify REST API")
                .apiProperty("api.version", "1.0")
                .apiProperty("cors", "true")
                .apiProperty("base.path", "camel/")
                .apiProperty("api.path", "/")
                .apiProperty("host", "")
                .apiContextRouteId("doc-api")
                .enableCORS(true)
                .corsAllowCredentials(true)
                .corsHeaderProperty("Access-Control-Allow-Origin","*")
            .component("servlet")
            .bindingMode(RestBindingMode.json);
        
        rest("/shinsei").description("Shinsei Veirify")
            .post("/verify").type(com.tokaicom.genbo.Shinsei.class).outType(com.tokaicom.genbo.Result.class)
                .route().routeId("verify-api")
                .to("direct:shinseiVerifyImpl")
                .setHeader("Origin",constant("*"));

        from("direct:shinseiVerifyImpl").description("Shinsei Verify REST service implementation route")
            .streamCaching()
            .process(new ShinseiVerifyProccesor());     
        // @formatter:on
    }

}