package br.com.g3solutions.agileintegration;

import java.net.ConnectException;

import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.soap.name.ServiceInterfaceStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.sun.mdm.index.webservice.ExecuteMatchUpdate;
import com.sun.mdm.index.webservice.PersonEJB;

@Component
public class OutboundRoute  extends RouteBuilder {
	
	public static final String ROUTE_ID = "OUTBOUND_ROUTE";

	public static final String FROM_ENDPOINT = "amqp:queue:nextgate.out";

	public static final String CONNECT_EXCEPTION_DLQ_ENDPOINT_ID = "OUTBOUND_ROUTE_NEXTGATE_DLQ";

	public static final String NEXTGATE_SERVICE_ENDPOINT_ID = "OUTBOUND_ROUTE_NEXTGATE_SERVICE";

	public static final int CONNECT_EXCEPTION_MAX_REDELIVERIES = 3;
	
	@Autowired
	private Environment env;
		
    @Override
    public void configure() throws Exception {
		    	    	
    	from(FROM_ENDPOINT).routeId(ROUTE_ID)
			.onException(ConnectException.class)
			.maximumRedeliveries(CONNECT_EXCEPTION_MAX_REDELIVERIES)
			.handled(Boolean.TRUE)
			.retryAttemptedLogLevel(LoggingLevel.WARN)
			.log(LoggingLevel.WARN, "${exception.message}")
			.setExchangePattern(ExchangePattern.InOnly)
			.to("amqp:queue:nextgate.dlq").id(CONNECT_EXCEPTION_DLQ_ENDPOINT_ID)
			.end()
    	.log(">>> ExecuteMatchUpdate XML:\n${body}")
    	.unmarshal().jaxb(ExecuteMatchUpdate.class.getPackage().getName())
    	.marshal().soapjaxb(PersonEJB.class.getPackage().getName(), new ServiceInterfaceStrategy(PersonEJB.class, Boolean.TRUE))
    	.log(">>> ExecuteMatchUpdate SOAP:\n${body}")

//		With Enviroment Variable    	
//    	.log(">>> NEXTGATE_URL: {{env:"+NEXTGATE_URL_ENV_VARIABLE+"}}")
//    	.to("cxf://{{env:"+NEXTGATE_URL_ENV_VARIABLE+"}}").id(NEXTGATE_SERVICE_ENDPOINT_ID)

    	.log(">>> NEXTGATE_URL: " + nextgateServiceURL())
    	.to("cxf://" + nextgateServiceURL()).id(NEXTGATE_SERVICE_ENDPOINT_ID)

    	.log(">>> ExecuteMatchUpdateResponse XML:${body.class.simpleName}\n${body}")
    	.log(">>> DONE");
    }

	private String nextgateServiceURL() {
		String host = env.getProperty("nextgate.service.host");
		String port = env.getProperty("nextgate.service.port");
		String path = env.getProperty("nextgate.service.path");
		
		return "http://" + host + ":" + port + path + "/PersonEJBService/PersonEJB?serviceClass=com.sun.mdm.index.webservice.PersonEJB&dataFormat=MESSAGE";
	}
}