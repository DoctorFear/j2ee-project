package com.futabus.gateway.configuration;

import com.futabus.gateway.configuration.properties.FutaGatewayProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalPathBlockFilter implements WebFilter, Ordered {
    FutaGatewayProperties futaGatewayProperties;

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        boolean blocked = futaGatewayProperties.getBlockedPaths()
                .stream()
                .anyMatch(gateway -> path.equals(gateway) || path.startsWith(gateway + "/"));

        if (blocked) {
            exchange.getResponse()
                    .setStatusCode(HttpStatus.FORBIDDEN);

            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
