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

	public static final String ROUTE_ID = "XLATE_ROUTE";

	public static final String FROM_ENDPOINT = "amqp:queue:deim.in";

	public static final String TYPE_CONVERSION_EXCEPTION_DLQ_ENDPOINT_ID = "XLATE_ROUTE_XLATE_CONVERTER_DLQ";

	public static final String NEXTGATE_OUT_QUEUE_ENDPOINT_ID = "XLATE_ROUTE_NEXTGATE_OUT";

	public static final int TYPE_CONVERSION_EXCEPTION_MAX_REDELIVERIES = 3;

    @Override
    public void configure() throws Exception {

    	from(FROM_ENDPOINT).routeId(ROUTE_ID)
			.onException(TypeConversionException.class)
			.maximumRedeliveries(TYPE_CONVERSION_EXCEPTION_MAX_REDELIVERIES)
			.handled(Boolean.TRUE)
			.retryAttemptedLogLevel(LoggingLevel.WARN)
			.log(LoggingLevel.WARN, "${exception.message}")
			.setExchangePattern(ExchangePattern.InOnly)
			.to("amqp:queue:xlate.converter.dlq").id(TYPE_CONVERSION_EXCEPTION_DLQ_ENDPOINT_ID)
			.end()
    	.log(">>> Person XML:\n${body}")
    	.unmarshal().jaxb(Person.class.getPackage().getName())
    	.convertBodyTo(ExecuteMatchUpdate.class)
    	.marshal().jaxb(Boolean.TRUE)
    	.log(">>> ExecuteMatchUpdate XML:\n${body}")
		.setExchangePattern(ExchangePattern.InOnly)
		.to("amqp:queue:nextgate.out").id(NEXTGATE_OUT_QUEUE_ENDPOINT_ID)
    	.log(">>> DONE");
    }
}