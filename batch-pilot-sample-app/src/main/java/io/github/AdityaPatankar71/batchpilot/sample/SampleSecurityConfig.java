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
package io.github.AdityaPatankar71.batchpilot.sample;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Demo users for the sample app:
 * <ul>
 *   <li><b>admin / admin</b> — has BATCH_PILOT_ADMIN, full console access.</li>
 *   <li><b>viewer / viewer</b> — lacks the role, gets 403 (demonstrates enforcement).</li>
 * </ul>
 * The batch-pilot starter contributes the SecurityFilterChain for /batch-pilot/**.
 */
@Configuration
public class SampleSecurityConfig {

    @Bean
    public InMemoryUserDetailsManager users() {
        UserDetails admin = User.withUsername("admin")
                .password("{noop}admin")
                .roles("BATCH_PILOT_ADMIN")
                .build();
        UserDetails viewer = User.withUsername("viewer")
                .password("{noop}viewer")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(admin, viewer);
    }
}
