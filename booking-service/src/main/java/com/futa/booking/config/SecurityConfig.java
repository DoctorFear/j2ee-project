// Xác minh JWT và quyền sử dụng API đặt vé
package com.futa.booking.config;

import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  // Kiểm tra chữ ký và thông tin định danh do Auth cấp
  @Bean
  JwtDecoder decoder(BookingProperties properties) {
    if (properties.getJwtSecret() == null
        || properties.getJwtSecret().getBytes(StandardCharsets.UTF_8).length < 32)
      throw new IllegalStateException("AUTH_JWT_SECRET phải khớp Auth và có ít nhất 32 byte");
    var decoder =
        NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(
                    properties.getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
            .build();
    OAuth2TokenValidator<Jwt> audience =
        token ->
            token.getAudience().contains(properties.getJwtAudience())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(properties.getJwtIssuer()), audience));
    return decoder;
  }

  // Chỉ công khai danh mục chuyến và callback có chữ ký của MoMo
  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    var authorities = new JwtGrantedAuthoritiesConverter();
    authorities.setAuthoritiesClaimName("role");
    authorities.setAuthorityPrefix("ROLE_");
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authorities);
    return http.csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/api/bookings/catalog/**", "/api/bookings/payments/momo/ipn")
                    .permitAll()
                    .requestMatchers("/internal/**")
                    .permitAll()
                    .anyRequest()
                    .hasRole("Member"))
        .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter)))
        .build();
  }
}
