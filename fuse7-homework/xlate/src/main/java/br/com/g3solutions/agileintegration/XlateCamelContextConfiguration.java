package br.com.g3solutions.agileintegration;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XlateCamelContextConfiguration {

    @Bean
    CamelContextConfiguration contextConfiguration() {
      return new CamelContextConfiguration() {

		@Override
		public void beforeApplicationStart(CamelContext camelContext) {
			camelContext.getTypeConverterRegistry().addTypeConverters(new ExecuteMatchUpdateConverters());
			
		}

		@Override
		public void afterApplicationStart(CamelContext camelContext) {
			//
		}
      };
    }

}
