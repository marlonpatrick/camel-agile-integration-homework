package br.com.g3solutions.agileintegration;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverters;

import com.customer.app.Person;
import com.sun.mdm.index.webservice.CallerInfo;
import com.sun.mdm.index.webservice.ExecuteMatchUpdate;
import com.sun.mdm.index.webservice.PersonBean;
import com.sun.mdm.index.webservice.SystemPerson;

public class ExecuteMatchUpdateConverters implements TypeConverters{

	@Converter
	public ExecuteMatchUpdate convert(Person person, Exchange exchange) {

		SystemPerson systemPerson = new SystemPerson();
		PersonBean personBean = new PersonBean();

		// we only set the Father's name and Gender
		// Any of the other person objects could be set here
		personBean.setFirstName(person.getLegalname().getGiven());
		personBean.setFatherName(person.getFathername());
		personBean.setGender(person.getGender().getCode());
		systemPerson.setPerson(personBean);

		// These only show up in the logs and can be anything
		// We set the user to Xlate here to know the ESB was used
		CallerInfo callerInfo = new CallerInfo();
		callerInfo.setApplication("App");
		callerInfo.setApplicationFunction("Function");
		callerInfo.setAuthUser("Xlate");

		ExecuteMatchUpdate executeMatchUpdate = new ExecuteMatchUpdate();
		executeMatchUpdate.setCallerInfo(callerInfo);
		executeMatchUpdate.setSysObjBean(systemPerson);

		if (exchange != null) {
			exchange.getOut().setBody(executeMatchUpdate);
		}

		return executeMatchUpdate;
	}
}
