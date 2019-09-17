package br.com.g3solutions.agileintegration;

import java.util.HashMap;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
import org.springframework.stereotype.Component;

import com.customer.app.Person;
import com.customer.app.response.ESBResponse;

@Component
public class DEIMServiceImpl implements DEIMService {

	@Produce(uri = "direct:inboundRoute")
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

			System.out.println("addPerson called");
//			System.out.println(camelResponse);

			if (person != null) {
				return Response.ok().build();
			}

			ESBResponse esbResponse = new ESBResponse();
			esbResponse.setBusinessKey(UUID.randomUUID().toString());
			esbResponse.setPublished(true);

			// Here we hard code the response code values to strings for the demo
			// A better practice would be to have an ENUM class
			String comment = "NONE";
			if (camelResponse.equals("0")) {
				comment = "NO MATCH";
			} else if (camelResponse.equals("1")) {
				comment = "MATCH";
			} else if (camelResponse.equals("2")) {
				comment = "DONE";
			} else {
				comment = "ERROR";
			}
			esbResponse.setComment(comment);

			builder.status(Response.Status.OK);
			builder.entity(esbResponse);

		} catch (CamelExecutionException cee) {
			builder.status(Response.Status.INTERNAL_SERVER_ERROR);
			builder.entity(cee.getMessage());
			cee.printStackTrace();
		} catch (Exception e) {
			builder.status(Response.Status.INTERNAL_SERVER_ERROR);
			builder.entity(e.getMessage());
			e.printStackTrace();
		}

		return builder.build();
	}
}