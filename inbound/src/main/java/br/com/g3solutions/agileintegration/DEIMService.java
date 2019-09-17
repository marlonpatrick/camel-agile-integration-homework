package br.com.g3solutions.agileintegration;

import javax.ws.rs.core.Response;
import com.customer.app.Person;

public interface DEIMService {
  public Response addPerson(Person person);
  
  public String test();

}
