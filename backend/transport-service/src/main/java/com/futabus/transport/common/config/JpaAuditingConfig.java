package com.futabus.transport.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(dateTimeProviderRef = "vietnamDateTimeProvider")
public class JpaAuditingConfig {
    public static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Bean
    Clock transportClock() {
        return Clock.tick(Clock.system(VIETNAM_ZONE), ChronoUnit.MICROS.getDuration());
    }

    @Bean
    DateTimeProvider vietnamDateTimeProvider(Clock transportClock) {
        return () -> Optional.of(LocalDateTime.now(transportClock));
    }
}
