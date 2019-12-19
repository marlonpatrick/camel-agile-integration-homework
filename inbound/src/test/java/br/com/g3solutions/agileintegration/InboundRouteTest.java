package br.com.g3solutions.agileintegration;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

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

	@Test
	public void testOutput() throws Exception {

		Person person = person();

		camelContext.getRouteDefinition(InboundRoute.ROUTE_ID).adviceWith(camelContext, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				weaveById(InboundRoute.TO_DEIM_IN_ENDPOINT_ID).replace().to("mock:deim.in");
			}
		});

		camelContext.start();
				
		while(!camelContext.getStatus().isStarted()) {
			Thread.sleep(500);
		}

		MockEndpoint mockDeimIn = camelContext.getEndpoint("mock:deim.in", MockEndpoint.class);
		mockDeimIn.expectedMessageCount(1);
		mockDeimIn.expectedHeaderReceived(Exchange.CONTENT_TYPE, "application/xml");
		mockDeimIn.expectedBodiesReceived(toXML(person));

		String camelResponse = template.requestBody(template.getDefaultEndpoint(), person, String.class);

		assertEquals("DONE", camelResponse);

		mockDeimIn.assertIsSatisfied();
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

		String camelResponse = template.requestBody(template.getDefaultEndpoint(), person, String.class);

		assertEquals("ERROR", camelResponse);

		mockError.assertIsSatisfied();
	}
	
	/**
	 * Preferred over dynamic generation for more reliability
	 * TODO: Improvement: The static part can be moved to a file.
	 */
	private String toXML(Person person) {
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
	private String dynamicToXML(Person person) throws JAXBException, PropertyException {
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
}
