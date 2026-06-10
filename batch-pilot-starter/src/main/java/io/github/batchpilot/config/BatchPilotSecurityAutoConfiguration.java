/*
 * Copyright 2026 Aditya Patankar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.batchpilot.config;

import io.github.batchpilot.service.JobQueryService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Secures the console when the host application has Spring Security on the
 * classpath. Registers a path-scoped {@link SecurityFilterChain} for
 * {@code /batch-pilot/**} requiring the configured role, ahead of (and without
 * disturbing) the host's own security chain. CSRF stays on, with the token
 * exposed via cookie so the Angular SPA can echo it back.
 *
 * <p>Disable with {@code batch-pilot.security.enabled=false}.
 */
@AutoConfiguration(after = BatchPilotAutoConfiguration.class)
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnBean(JobQueryService.class)
@ConditionalOnProperty(prefix = "batch-pilot.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BatchPilotProperties.class)
public class BatchPilotSecurityAutoConfiguration {

    @Bean
    @Order(1)
    public SecurityFilterChain batchPilotSecurityFilterChain(HttpSecurity http,
                                                             BatchPilotProperties properties) throws Exception {
        String role = properties.getSecurity().getRole();
        // Plain (non-XOR) handler with eager loading: the raw token is written to
        // the XSRF-TOKEN cookie so the SPA can echo it back in X-XSRF-TOKEN.
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);
        http.securityMatcher("/batch-pilot/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole(role))
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfHandler));
        return http.build();
    }
}
