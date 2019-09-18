package br.com.g3solutions.agileintegration;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.customer.app.Person;
import com.sun.mdm.index.webservice.ExecuteMatchUpdate;

@Component
public class XlateRoute  extends RouteBuilder {

    @Override
    public void configure() throws Exception {
    	    	
    	from("amqp:queue:deim.in")
    	.log(">>> amqp:queue:deim.in XML:\n${body}")
//    	.process(new Processor() {
//			
//			@Override
//			public void process(Exchange exchange) throws Exception {
//				Object inbody = exchange.getIn().getBody();
//				System.out.println(inbody);
//				
//				Object messagebody = exchange.getMessage().getBody();
//				System.out.println(messagebody);
//			}
//		})
    	.unmarshal().jaxb(Person.class.getPackage().getName())
    	.convertBodyTo(ExecuteMatchUpdate.class)
    	.log(">>> amqp:queue:deim.in ExecuteMatchUpdate:\n${body}");
    }
}