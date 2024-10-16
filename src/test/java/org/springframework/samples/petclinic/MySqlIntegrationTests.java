/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mysql")
@Testcontainers(disabledWithoutDocker = true)
@DisabledInNativeImage
@DisabledInAotMode
class MySqlIntegrationTests {

	@BeforeAll
	public static void setUp() {
		System.out.println("$$$ MySQL Container before check");
	}

	@ServiceConnection
	@Container
	static MySQLContainer<?> container = new MySQLContainer<>("mysql:8.4");

	@DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

	@BeforeAll
	public static void setUp2() {
		//container.start();  // Ensure the container starts
		System.out.println("$$$ MySQL Container after check");
		// Verify container is running and log the JDBC URL
		System.out.println("MySQL Container is running: " + container.isRunning());
		System.out.println("MySQL Container JDBC URL: " + container.getJdbcUrl());

		// Set system properties for datasource configuration
		System.setProperty("spring.datasource.url", container.getJdbcUrl());
		System.setProperty("spring.datasource.username", container.getUsername());
		System.setProperty("spring.datasource.password", container.getPassword());

		// Optional: Check if container has started correctly
		if (container.isRunning()) {
			System.out.println("MySQL container started successfully!");
		} else {
			throw new IllegalStateException("MySQL container failed to start.");
		}
	}

	@Test
	void checkInitScripts() throws Exception {
		System.out.println("$$$ AAAA");
    	// Run a command inside the MySQL container to list the files in the init directory
    	String[] command = { "ls", "/docker-entrypoint-initdb.d/" };
    	ExecResult execResult = container.execInContainer(command);
    
   	 	System.out.println("Files in /docker-entrypoint-initdb.d/:");
    	System.out.println(execResult.getStdout());

    	// You can also check if specific files are present
    	assertThat(execResult.getStdout()).contains("02_schema.sql", "03_data.sql");
	}

	@LocalServerPort
	int port;

	@Autowired
	private VetRepository vets;

	@Autowired
	private RestTemplateBuilder builder;

	@Test
	void testFindAll() throws Exception {
		vets.findAll();
		vets.findAll(); // served from cache
	}

	@Test
	void testOwnerDetails() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<String> result = template.exchange(RequestEntity.get("/owners/1").build(), String.class);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}
