package br.com.g3solutions.agileintegration;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class InboundRoute  extends RouteBuilder {

    @Override
    public void configure() throws Exception {
    	
    	from("direct:inboundRoute").routeId("InboundRoute")
		.marshal().jaxb(Boolean.TRUE)
        .log(">>> direct:inboundRoute XML:\n${body}")
		.setExchangePattern(ExchangePattern.InOnly)
		.to("amqp:queue:deim.in")
		.transform().constant("DONE")
    	.log(">>> DONE");
    }
}