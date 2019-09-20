package br.com.g3solutions.agileintegration;

import java.net.ConnectException;

import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.soap.name.ServiceInterfaceStrategy;
import org.springframework.stereotype.Component;

import com.sun.mdm.index.webservice.ExecuteMatchUpdate;
import com.sun.mdm.index.webservice.PersonEJB;

@Component
public class OutboundRoute  extends RouteBuilder {
	
	private static final String NEXTGATE_URL_ENV_VARIABLE="NEXTGATE_URL";

    @Override
    public void configure() throws Exception {
    	    	
    	from("amqp:queue:nextgate.out").routeId("OutboundRoute")
			.onException(ConnectException.class)
			.maximumRedeliveries(3)
			.handled(Boolean.TRUE)
			.retryAttemptedLogLevel(LoggingLevel.WARN)
			.log(LoggingLevel.WARN, "${exception.message}")
			.setExchangePattern(ExchangePattern.InOnly)
			.to("amqp:queue:nextgate.dlq")
			.end()
    	.log(">>> ExecuteMatchUpdate XML:\n${body}")
    	.unmarshal().jaxb(ExecuteMatchUpdate.class.getPackage().getName())
    	.marshal().soapjaxb(PersonEJB.class.getPackage().getName(), new ServiceInterfaceStrategy(PersonEJB.class, Boolean.TRUE))
    	.log(">>> ExecuteMatchUpdate SOAP:\n${body}")
    	.log(">>> NEXTGATE_URL: {{env:"+NEXTGATE_URL_ENV_VARIABLE+"}}")
    	.to("cxf://{{env:"+NEXTGATE_URL_ENV_VARIABLE+"}}")
    	.log(">>> ExecuteMatchUpdateResponse XML:${body.class.simpleName}\n${body}")
    	.log(">>> DONE");
    }
}