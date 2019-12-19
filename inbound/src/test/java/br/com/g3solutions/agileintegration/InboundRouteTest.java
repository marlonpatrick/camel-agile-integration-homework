package br.com.g3solutions.agileintegration;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

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

import com.customer.app.Code;
import com.customer.app.Person;
import com.customer.app.PersonName;

@UseAdviceWith
@RunWith(CamelSpringBootRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = InboundApplication.class, properties = {
		"camel.springboot.java-­routes-­include-­pattern=**/InboundRoute" })
public class InboundRouteTest {

	@Autowired
	private CamelContext camelContext;

	@Autowired
	@Produce (uri = InboundRoute.FROM_ENDPOINT)
	private ProducerTemplate template;

//	@EndpointInject(uri = "mock:test")
//  protected MockEndpoint testEndpoint;

	@Test
	public void testOutput() throws Exception {

		Person person = person();

		camelContext.getRouteDefinition(InboundRoute.ROUTE_ID).adviceWith(camelContext, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveById(InboundRoute.TO_MESSAGING_SYSTEM_ID).replace().to("mock:messaging-system");
			}
		});

		camelContext.start();
				
		while(!camelContext.getStatus().isStarted()) {
			Thread.sleep(500);
		}

		MockEndpoint mockMessagingSystem = camelContext.getEndpoint("mock:messaging-system", MockEndpoint.class);
		mockMessagingSystem.expectedMessageCount(1);
		mockMessagingSystem.expectedHeaderReceived(Exchange.CONTENT_TYPE, "application/xml");
		mockMessagingSystem.expectedBodiesReceived(expectedBody(person));

		String camelResponse = template.requestBodyAndHeaders(template.getDefaultEndpoint(), person,
				new HashMap<String, Object>(), String.class);

		assertEquals("DONE", camelResponse);

		mockMessagingSystem.assertIsSatisfied();
	}

	@Test
	public void testErrorHandler() throws Exception {

		Person person = person();

		camelContext.getRouteDefinition(InboundRoute.ROUTE_ID).adviceWith(camelContext, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveAddFirst().to("mock:simulate-error");
			}
		});

		
		camelContext.start();

		while(!camelContext.getStatus().isStarted()) {
			Thread.sleep(500);
		}

		MockEndpoint mockError = camelContext.getEndpoint("mock:simulate-error", MockEndpoint.class);
		mockError.expectedMessageCount(InboundRoute.EXCEPTION_MAX_REDELIVERIES + 1);
		mockError.whenAnyExchangeReceived(e -> {throw new RuntimeException("Simulated error");});

		String camelResponse = template.requestBodyAndHeaders(template.getDefaultEndpoint(), person,
				new HashMap<String, Object>(), String.class);

		assertEquals("ERROR", camelResponse);

		mockError.assertIsSatisfied();
	}
	
	/**
	 * Preferred for more reliability
	 */
	private String expectedBody(Person person) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		sb.append("<Person xmlns=\"http://www.app.customer.com\">\n");
		sb.append("    <legalname>\n");
		sb.append("        <given>" + person.getLegalname().getGiven() + "</given>\n");
		sb.append("    </legalname>\n");
		sb.append("    <fathername>" + person.getFathername() + "</fathername>\n");
		sb.append("    <mothername>" + person.getMothername() + "</mothername>\n");
		sb.append("    <gender>\n");
		sb.append("        <code>" + person.getGender().getCode() + "</code>\n");
		sb.append("    </gender>\n");
		sb.append("</Person>\n");

		return sb.toString();
	}

	@SuppressWarnings("unused")
	private String dynamicExpectedBody(Person person) throws JAXBException, PropertyException {
		StringWriter stringWriter = new StringWriter();

		JAXBContext jaxbContext = JAXBContext.newInstance(Person.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(person, stringWriter);

		return stringWriter.toString();
	}

	private Person person() {
		PersonName legalName = new PersonName();
		legalName.setGiven("First");

		Code genderCode = new Code();
		genderCode.setCode("Male");

		Person person = new Person();
		person.setLegalname(legalName);
		person.setFathername("Dad");
		person.setMothername("Mom");
		person.setGender(genderCode);
		return person;
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
