package com.futa.gateway.config;

import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Bộ lọc toàn cục ngăn chặn mọi truy cập trực tiếp tới các endpoint nội bộ (/internal/**)
 * từ bên ngoài qua API Gateway theo đặc tả kiến trúc FUTA Bus.
 */
@Component
public class InternalPathBlockFilter implements WebFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path != null && (path.startsWith("/internal") || path.contains("/internal/"))) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100; // Ưu tiên chạy trước các filter định tuyến
    }
}
