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
}
