/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devoxx.watson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Starter class for the AskDevoxx
 *
 * @author James Weaver
 */
@SpringBootApplication(exclude = MessageSourceAutoConfiguration.class)
public class ServletInitializer extends SpringBootServletInitializer {

	/*public static void main(String[] args) {
		SpringApplication.run(AskDevoxxApplication.class, args);
	}*/
      @Override
   protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
      return application.sources(ServletInitializer.class);
   }
}
/*
@RestController
class RESTController {

   @RequestMapping("/")
   public String hello() {
      return "<H1>Hello from Devoxx !</H1>";
   }
}*/

