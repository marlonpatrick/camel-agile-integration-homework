package br.com.g3solutions.agileintegration;

import java.net.ConnectException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.util.XmlExpectationsHelper;

@UseAdviceWith
@RunWith(CamelSpringBootRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = OutboundApplication.class, properties = {
		"camel.springboot.java-­routes-­include-­pattern=**/OutboundRoute" })
public class OutboundRouteTest {

	@Autowired
	private CamelContext camelContext;

	@Autowired
	@Produce (uri = "direct:from")
	private ProducerTemplate template;

	private XmlExpectationsHelper xmlExpectationsHelper = new XmlExpectationsHelper();

	@Test
	public void testOutput() throws Exception {

		String executeMatchUpdateXML = executeMatchUpdateXML("First", "Dad", "Male");

		camelContext.getRouteDefinition(OutboundRoute.ROUTE_ID).adviceWith(camelContext, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				replaceFromWith("direct:from");
				weaveById(OutboundRoute.NEXTGATE_SERVICE_ENDPOINT_ID).replace().to("mock:nextgate.service");
			}
		});

		camelContext.start();
				
		while(!camelContext.getStatus().isStarted()) {
			Thread.sleep(500);
		}

		MockEndpoint mockNextateService = camelContext.getEndpoint("mock:nextgate.service", MockEndpoint.class);
		mockNextateService.expectedMessageCount(1);
		mockNextateService.expectedHeaderReceived(Exchange.CONTENT_TYPE, "application/xml");

		template.sendBody(template.getDefaultEndpoint(), executeMatchUpdateXML);

		mockNextateService.assertIsSatisfied();
		
		String nextateServiceMessage = mockNextateService.getExchanges().get(0).getIn().getBody(String.class);
		
		xmlExpectationsHelper.assertXmlEqual(nextateServiceMessage, executeMatchUpdateSOAPXML("First", "Dad", "Male"));
	}

	@Test
	public void testConnectErrorHandler() throws Exception {

		String executeMatchUpdateXML = executeMatchUpdateXML("First", "Dad", "Male");

		camelContext.getRouteDefinition(OutboundRoute.ROUTE_ID).adviceWith(camelContext, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				replaceFromWith("direct:from");
				weaveAddFirst().to("mock:simulate-error");
				weaveById(OutboundRoute.CONNECT_EXCEPTION_DLQ_ENDPOINT_ID).replace().to("mock:connect-error-dlq");
			}
		});
		
		camelContext.start();

		while(!camelContext.getStatus().isStarted()) {
			Thread.sleep(500);
		}
		
		MockEndpoint mockError = camelContext.getEndpoint("mock:simulate-error", MockEndpoint.class);
		mockError.expectedMessageCount(OutboundRoute.CONNECT_EXCEPTION_MAX_REDELIVERIES + 1);
		mockError.whenAnyExchangeReceived(e -> {throw new ConnectException("Simulated error");});

		MockEndpoint mockErrorDLQ = camelContext.getEndpoint("mock:connect-error-dlq", MockEndpoint.class);
		mockErrorDLQ.expectedMessageCount(1);
		mockErrorDLQ.expectedBodiesReceived(executeMatchUpdateXML);

		template.sendBody(template.getDefaultEndpoint(), executeMatchUpdateXML);

		mockError.assertIsSatisfied();
		mockErrorDLQ.assertIsSatisfied();
	}

	/**
	 * Preferred over dynamic generation for more reliability
	 * TODO: Improvement: The static part can be moved to a file.
	 */
	private String executeMatchUpdateSOAPXML(String givenLegalName, String fathername, String genderCode) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		sb.append("<ns2:Envelope xmlns:ns2=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns3=\"http://webservice.index.mdm.sun.com/\">\n");
		sb.append("    <ns2:Body>\n");
		sb.append("        <ns3:executeMatchUpdate>\n");
		sb.append("            <callerInfo>\n");
		sb.append("                <application>App</application>\n");
		sb.append("                <applicationFunction>Function</applicationFunction>\n");
		sb.append("                <authUser>Xlate</authUser>\n");
		sb.append("            </callerInfo>\n");
		sb.append("            <sysObjBean>\n");
		sb.append("                <person>\n");
		sb.append("                    <fatherName>"+fathername+"</fatherName>\n");
		sb.append("                    <firstName>"+givenLegalName+"</firstName>\n");
		sb.append("                    <gender>"+genderCode+"</gender>\n");
		sb.append("                </person>\n");
		sb.append("            </sysObjBean>\n");
		sb.append("        </ns3:executeMatchUpdate>\n");
		sb.append("    </ns2:Body>\n");
		sb.append("</ns2:Envelope>\n");

		return sb.toString();
	}

	/**
	 * Preferred over dynamic generation for more reliability
	 * TODO: Improvement: The static part can be moved to a file.
	 */
	private String executeMatchUpdateXML(String givenLegalName, String fathername, String genderCode) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		sb.append("<ns2:executeMatchUpdate xmlns:ns2=\"http://webservice.index.mdm.sun.com/\">\n");
		sb.append("    <callerInfo>\n");
		sb.append("        <application>App</application>\n");
		sb.append("        <applicationFunction>Function</applicationFunction>\n");
		sb.append("        <authUser>Xlate</authUser>\n");
		sb.append("    </callerInfo>\n");
		sb.append("    <sysObjBean>\n");
		sb.append("        <person>\n");
		sb.append("            <fatherName>"+fathername+"</fatherName>\n");
		sb.append("            <firstName>"+givenLegalName+"</firstName>\n");
		sb.append("            <gender>"+genderCode+"</gender>\n");
		sb.append("        </person>\n");
		sb.append("    </sysObjBean>\n");
		sb.append("</ns2:executeMatchUpdate>\n");

		return sb.toString();
	}

//	mock.expectedBodiesReceived
//	mock.expectedMessageCount(2)
//	mock.message(0).body().contains("Camel");
//	mock.message(1).body().contains("Camel");
//	mock.allMessages().body().contains("Camel");
//	mock.message(0).header("JMSPriority").isEqualTo(4);
//	mock.whenAnyExchangeReceived(e ->
//		e.getIn().setBody("ID=123,STATUS=IN PROGRESS")
//	);

//	List<Exchange> list = mock.getReceivedExchanges();
//	String body1 = list.get(0).getIn().getBody(String.class);
//	String body2 = list.get(1).getIn().getBody(String.class);
//	assertTrue(body1.contains("Camel"));
//	assertTrue(body2.contains("Camel"));

//	from("direct:file")
//	.process(new Processor()) {
//	public void process(Exchange exchange) throws Exception {
//	throw new ConnectException("Simulated error");
//	}
//	})
//	.to("mock:http");

//	from("direct:file")
//	.process(e -> { throw new ConnectException("Simulated error"); })
//	.to("mock:http");

//	MockEndpoint http = getMockEndpoint("mock:http");
//	http.whenAnyExchangeReceived(new Processor() {
//	public void process(Exchange exchange) throws Exception {
//	throw new ConnectException("Simulated connection error");
//	}
//	});

//	route.adviceWith(context, new AdviceWithRouteBuilder() {
//		public void configure() throws Exception {
//		weaveById("transform").replace()
//		 1 
//		 Uses weave to select the
//		.transform().simple("${body.toUpperCase()}");
//		 node to be replaced
//		weaveAddLast().to("mock:result");
//		 2  Routes to a mock endpoint at
//		}
//		 the end of the route
//		});
//		context.start();

//	RouteDefinition route = context.getRouteDefinition("quotes");
//	 be advised
//	route.adviceWith(context, new AdviceWithRouteBuilder() {
//	3 Replaces 
//	 the route
//	public void configure() throws Exception {
//	input endpoint with a
//	replaceFromWith("direct:hitme");
//	 direct endpoint
//	mockEndpoints("seda:*");
//	}
//	});
//	context.start();
//	getMockEndpoint("mock:seda:camel")
//	.expectedBodiesReceived("Camel rocks");
//	getMockEndpoint("mock:seda:other")
//	.expectedBodiesReceived("Bad donkey");
//	6  Sets expectations on the
//	mock endpoints
//	template.sendBody("direct:hitme", "Camel rocks");
//	template.sendBody("direct:hitme", "Bad donkey");
//	7  Sends in test messages
//	to the direct endpoint
//	assertMockEndpointsSatisfied();

//	public void testSimulateErrorUsingInterceptors() throws Exception {
//		RouteDefinition route = context.getRouteDefinitions().get(0);
//		route.adviceWith(context, new RouteBuilder() {
//			public void configure() throws Exception {
//				interceptSendToEndpoint("http://*").skipSendToOriginalEndpoint();
//			}
//		});
//	}
//
//	@Test
//	public void testMoveFile() throws Exception {
//
//		NotifyBuilder notify = new NotifyBuilder(context).whenDone(1).create();
//
//		template.sendBodyAndHeader("file://target/inbox", "Hello World", Exchange.FILE_NAME, "hello.txt");
//
//		assertTrue(notify.matchesMockWaitTime());// replaces Thread.sleep(2000);
//
//		File target = new File("target/outbox/hello.txt");
//		assertTrue("File not moved", target.exists());
//
//		String content = context.getTypeConverter().convertTo(String.class, target);
//		assertEquals("Hello World", content);
//	}
//
//	@Override extends extends CamelSpringTestSupport
//	protected AbstractApplicationContext createApplicationContext() {
//		return new ClassPathXmlApplicationContext("camelinaction/firststep.xml");
// Declares the location of the Spring XML file
//	}

//	@Override
//	protected AbstractApplicationContext createApplicationContext() {
//		AnnotationConfigApplicationContext acc = new AnnotationConfigApplicationContext();
//		acc.register(MyApplication.class); // in the example MyApplication isn't a @SpringBootApplication, only a @Configuration
//		return acc;
//	}

//	@Override extends CamelTestSupport
//	protected RouteBuilder createRouteBuilder() throws Exception {
//		return new InboundRoute();
////		return new RouteBuilder() {
////			public void configure() throws Exception {
////				from("file://target/inbox").to("file://target/outbox");
////			}
////		};
//	}

}
