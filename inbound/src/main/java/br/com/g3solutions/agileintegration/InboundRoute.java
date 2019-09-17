package br.com.g3solutions.agileintegration;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class InboundRoute  extends RouteBuilder {

    @Override
    public void configure() throws Exception {
//        from("timer://foo?period=1000")
    	from("direct:integrateRoute")
        .setBody(simple("Hello World from camel"))
        .log(">>> ${body}");
    }
}