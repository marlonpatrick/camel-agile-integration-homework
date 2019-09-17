package br.com.g3solutions.agileintegration;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class InboundRoute  extends RouteBuilder {

    @Override
    public void configure() throws Exception {
//        from("timer://foo?period=1000")
//        .setBody(simple("Hello World from camel"))
//        .log(">>> ${body}");
    	
    	from("direct:inboundRoute")
//			.onException(Exception.class)
//			.handled(Boolean.FALSE)
//			.log("Exception Log: ${body}")
//			.log("Exception ExchangePattern Before: ${exchange.pattern}")		
//			.setExchangePattern(ExchangePattern.InOnly)
//			.log("Exception ExchangePattern After: ${exchange.pattern}")		
//			.to("amqp:queue:errorQueue")
//			.end()
//		.marshal().jaxb(Boolean.TRUE)
        .log(">>> calling direct:inboundRoute: ${body}");
    }
}