/**
 *  Copyright 2005-2018 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package br.com.g3solutions.agileintegration;

import org.apache.camel.component.amqp.AMQPConnectionDetails;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class OutboundApplication {
	
    public static void main(String[] args) {
    	SpringApplication.run(OutboundApplication.class, args);
    }

	@Bean
	AMQPConnectionDetails amqpConnection(Environment env) {
		String host = env.getProperty("amqp.service.host");
		String port = env.getProperty("amqp.service.port");

		return new AMQPConnectionDetails("amqp://" + host + ":" + port, env.getProperty("amqp.service.username"),
				env.getProperty("amqp.service.password"), Boolean.TRUE);
	}
}
