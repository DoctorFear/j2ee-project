package com.futabus.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GatewayApplicationTests {

	@Autowired
	RouteLocator routeLocator;

	@Test
	void contextLoads() {
	}

	@Test
	void routeAndRouteStopRequestsMatchTransportService() {
		var route = routeLocator.getRoutes()
				.filter(candidate -> candidate.getId().equals("transport-service-route"))
				.blockFirst(Duration.ofSeconds(5));
		assertNotNull(route);
		assertEquals("http://localhost:8081", route.getUri().toString());
		for (String path : List.of("/api/routes", "/api/routes/1", "/api/route-stops", "/api/route-stops/1")) {
			var exchange = MockServerWebExchange.from(MockServerHttpRequest.get(path));
			assertEquals(Boolean.TRUE, Mono.from(route.getPredicate().apply(exchange))
					.block(Duration.ofSeconds(5)), path);
		}
	}

}
