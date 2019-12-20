package br.com.g3solutions.agileintegration;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.TypeConversionException;
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
@SpringBootTest(classes = XlateApplication.class, properties = {
		"camel.springboot.java-­routes-­include-­pattern=**/XlateRoute" })
public class XlateRouteTest {

	@Autowired
	private CamelContext camelContext;

	@Autowired
	@Produce (uri = "direct:from")
	private ProducerTemplate template;

	private XmlExpectationsHelper xmlExpectationsHelper = new XmlExpectationsHelper();

	@Test
	public void testOutput() throws Exception {

		String personXML = personXML("First", "Dad", "Mom", "Male");

		camelContext.getRouteDefinition(XlateRoute.ROUTE_ID).adviceWith(camelContext, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				replaceFromWith("direct:from");
				weaveById(XlateRoute.NEXTGATE_OUT_QUEUE_ENDPOINT_ID).replace().to("mock:nextgate.out");
			}
		});

		camelContext.start();
				
		while(!camelContext.getStatus().isStarted()) {
			Thread.sleep(500);
		}

		MockEndpoint mockNextateOut = camelContext.getEndpoint("mock:nextgate.out", MockEndpoint.class);
		mockNextateOut.expectedMessageCount(1);
		mockNextateOut.expectedHeaderReceived(Exchange.CONTENT_TYPE, "application/xml");

		template.sendBody(template.getDefaultEndpoint(), personXML);

		mockNextateOut.assertIsSatisfied();
		
		String nextgateOutMessage = mockNextateOut.getExchanges().get(0).getIn().getBody(String.class);
		
		xmlExpectationsHelper.assertXmlEqual(nextgateOutMessage, executeMatchUpdateXML("First", "Dad", "Male"));
	}

	@Test
	public void testTypeConvertionErrorHandler() throws Exception {

		String personXML = personXML("First", "Dad", "Mom", "Male");

		camelContext.getRouteDefinition(XlateRoute.ROUTE_ID).adviceWith(camelContext, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				replaceFromWith("direct:from");
				weaveAddFirst().to("mock:simulate-error");
				weaveById(XlateRoute.TYPE_CONVERSION_EXCEPTION_DLQ_ENDPOINT_ID).replace().to("mock:type-conversion-error-dlq");
			}
		});
		
		camelContext.start();

		while(!camelContext.getStatus().isStarted()) {
			Thread.sleep(500);
		}

		MockEndpoint mockError = camelContext.getEndpoint("mock:simulate-error", MockEndpoint.class);
		mockError.expectedMessageCount(XlateRoute.TYPE_CONVERSION_EXCEPTION_MAX_REDELIVERIES + 1);
		mockError.whenAnyExchangeReceived(e -> {throw new TypeConversionException("Simulated error", Object.class, new RuntimeException("Simulated error"));});

		MockEndpoint mockErrorDLQ = camelContext.getEndpoint("mock:type-conversion-error-dlq", MockEndpoint.class);
		mockErrorDLQ.expectedMessageCount(1);
		mockErrorDLQ.expectedBodiesReceived(personXML);

		template.sendBody(template.getDefaultEndpoint(), personXML);

		mockError.assertIsSatisfied();
		mockErrorDLQ.assertIsSatisfied();
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

	/**
	 * Preferred over dynamic generation for more reliability
	 * TODO: Improvement: The static part can be moved to a file.
	 */
	private String personXML(String givenLegalName, String fathername, String mothername, String genderCode) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		sb.append("<Person xmlns=\"http://www.app.customer.com\">\n");
		sb.append("    <legalname>\n");
		sb.append("        <given>" + givenLegalName + "</given>\n");
		sb.append("    </legalname>\n");
		sb.append("    <fathername>" + fathername + "</fathername>\n");
		sb.append("    <mothername>" + mothername + "</mothername>\n");
		sb.append("    <gender>\n");
		sb.append("        <code>" + genderCode + "</code>\n");
		sb.append("    </gender>\n");
		sb.append("</Person>\n");

		return sb.toString();
	}
}
