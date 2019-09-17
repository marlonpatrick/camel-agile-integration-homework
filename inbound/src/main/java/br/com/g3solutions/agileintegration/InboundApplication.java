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

import java.util.Arrays;

import org.apache.camel.CamelContext;
import org.apache.camel.component.amqp.AMQPConnectionDetails;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@SpringBootApplication
public class InboundApplication {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private Bus bus;
    
    @Autowired
    private DEIMServiceImpl deimServiceImpl;

    public static void main(String[] args) {
        SpringApplication.run(InboundApplication.class, args);
    }
    
    @Bean
    public Server rsServer() {
        JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
        endpoint.setBus(bus);
        endpoint.setAddress("/");
        endpoint.setServiceBeans(Arrays.<Object>asList(deimServiceImpl));
        endpoint.setProvider(new JacksonJsonProvider());
        endpoint.setFeatures(Arrays.asList(new Swagger2Feature()));
        return endpoint.create();
    }

    /**
     * export AMQP_SERVICE_HOST = "localhost"
     * export AMQP_SERVICE_PORT = "5672"
     * export AMQP_SERVICE_USERNAME = "username"
     * export AMQP_SERVICE_PASSWORD = "password"
     */
    @Bean
    AMQPConnectionDetails amqpConnection() {
      return AMQPConnectionDetails.discoverAMQP(camelContext);
    }
}
