package br.com.g3solutions.agileintegration;

import java.util.HashMap;
import java.util.Map;
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

import com.customer.app.Person;
import com.customer.app.response.ESBResponse;

public class DEIMServiceImpl implements DEIMService {

  @Produce(uri = "direct:integrateRoute")
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
	  
	  if(person != null) {
		  System.out.println("addPerson called");
		  System.out.println(person);
		  return Response.ok().build();
	  }

    ResponseBuilderImpl builder = new ResponseBuilderImpl();

    // This header is used to direct the message in the Camel route
    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("METHOD", "match");

    try {
      String camelResponse = template.requestBodyAndHeaders(template.getDefaultEndpoint(),
      person, headers, String.class);

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
    }catch(Exception e){
      builder.status(Response.Status.INTERNAL_SERVER_ERROR);
      builder.entity(e.getMessage());
      e.printStackTrace();
    }

    return builder.build();
  }
}