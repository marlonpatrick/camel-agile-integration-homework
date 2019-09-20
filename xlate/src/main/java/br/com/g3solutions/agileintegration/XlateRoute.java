package br.com.g3solutions.agileintegration;

import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.TypeConversionException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.customer.app.Person;
import com.sun.mdm.index.webservice.ExecuteMatchUpdate;

@Component
public class XlateRoute  extends RouteBuilder {

    @Override
    public void configure() throws Exception {

    	from("amqp:queue:deim.in").routeId("XlateRoute")
			.onException(TypeConversionException.class)
			.maximumRedeliveries(3)
			.handled(Boolean.TRUE)
			.retryAttemptedLogLevel(LoggingLevel.WARN)
			.log(LoggingLevel.WARN, "${exception.message}")
			.setExchangePattern(ExchangePattern.InOnly)
			.to("amqp:queue:xlate.converter.dlq")
			.end()
    	.log(">>> Person XML:\n${body}")
    	.unmarshal().jaxb(Person.class.getPackage().getName())
    	.convertBodyTo(ExecuteMatchUpdate.class)
    	.marshal().jaxb(Boolean.TRUE)
    	.log(">>> ExecuteMatchUpdate XML:\n${body}")
		.setExchangePattern(ExchangePattern.InOnly)
		.to("amqp:queue:nextgate.out")
    	.log(">>> DONE");
    }
}