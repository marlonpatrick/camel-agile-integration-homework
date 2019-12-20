package br.com.g3solutions.agileintegration;

import java.util.UUID;

import javax.ws.rs.Consumes;
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
	@POST
	@Path("/addPerson")
	@Consumes(MediaType.APPLICATION_XML)
	public Response addPerson(Person person) {

		ResponseBuilderImpl builder = new ResponseBuilderImpl();

		ESBResponse esbResponse = new ESBResponse();

		try {
			String camelResponse = template.requestBody(template.getDefaultEndpoint(), person, String.class);

			esbResponse.setBusinessKey(UUID.randomUUID().toString());
			esbResponse.setPublished(Boolean.TRUE);
			esbResponse.setComment(camelResponse);
			builder.status(Response.Status.OK);
			builder.entity(esbResponse);

		} catch (Exception e) {
			esbResponse.setComment("ERROR");
			builder.status(Response.Status.INTERNAL_SERVER_ERROR);
			builder.entity(e.getMessage());
			e.printStackTrace();
		}

		return builder.build();
	}
}