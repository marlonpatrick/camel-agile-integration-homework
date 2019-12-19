package br.com.g3solutions.agileintegration;

import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class InboundRoute  extends RouteBuilder {

	public static final String ROUTE_ID = "INBOUND_ROUTE";

	public static final String FROM_ENDPOINT = "direct:inboundRoute";

	public static final String TO_MESSAGING_SYSTEM_ID = "INBOUND_ROUTE_DEIM_IN";

	public static final int EXCEPTION_MAX_REDELIVERIES = 3;

    @Override
    public void configure() throws Exception {
    	
    	from(FROM_ENDPOINT).routeId(ROUTE_ID)
			.onException(Exception.class)
			.maximumRedeliveries(EXCEPTION_MAX_REDELIVERIES)
			.handled(Boolean.TRUE)
			.retryAttemptedLogLevel(LoggingLevel.WARN)
			.log(LoggingLevel.WARN, "${exception.message}")
			.transform().constant("ERROR")
			.end()
		.marshal().jaxb(Boolean.FALSE)
        .log(">>> direct:inboundRoute XML:\n${body}")
		.setExchangePattern(ExchangePattern.InOnly)
		.to("amqp:queue:deim.in").id(TO_MESSAGING_SYSTEM_ID)
		.transform().constant("DONE")
    	.log(">>> DONE");
    }
}