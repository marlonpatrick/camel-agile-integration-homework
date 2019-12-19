package br.com.g3solutions.agileintegration;

import java.util.HashMap;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
import org.springframework.stereotype.Component;

import com.customer.app.Person;
import com.customer.app.response.ESBResponse;

@Component
public class DEIMServiceImpl implements DEIMService {

	@Produce(uri = InboundRoute.FROM_ENDPOINT)
	ProducerTemplate template;

	@Override
	@GET
	@Path("/test")
	public String test() {
		return "CXF Homework Test";
	}

	@Override
	@POST
	@Path("/addPerson")
	@Consumes(MediaType.APPLICATION_XML)
	public Response addPerson(Person person) {

		System.out.println("calling addPerson...");

		ResponseBuilderImpl builder = new ResponseBuilderImpl();

		try {
			String camelResponse = template.requestBodyAndHeaders(template.getDefaultEndpoint(), person, new HashMap<String, Object>(),
					String.class);

			ESBResponse esbResponse = new ESBResponse();
			esbResponse.setBusinessKey(UUID.randomUUID().toString());
			esbResponse.setPublished(true);
			esbResponse.setComment(camelResponse);
			builder.status(Response.Status.OK);
			builder.entity(esbResponse);

		} catch (Exception e) {
			builder.status(Response.Status.INTERNAL_SERVER_ERROR);
			builder.entity(e.getMessage());
			e.printStackTrace();
		}

		return builder.build();
	}
}