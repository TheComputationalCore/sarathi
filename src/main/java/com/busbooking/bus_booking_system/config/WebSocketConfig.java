package com.busbooking.bus_booking_system.config;

import com.busbooking.bus_booking_system.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger =
            LoggerFactory.getLogger(WebSocketConfig.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final Environment environment;

    public WebSocketConfig(JwtUtil jwtUtil,
                           UserDetailsService userDetailsService,
                           Environment environment) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.environment = environment;
    }

    // =========================================================
    // CUSTOM TASK SCHEDULER (NO CIRCULAR DEPENDENCY)
    // =========================================================

    @Bean
    public ThreadPoolTaskScheduler webSocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    // =========================================================
    // MESSAGE BROKER
    // =========================================================

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(webSocketTaskScheduler());

        registry.setApplicationDestinationPrefixes("/app");
    }

    // =========================================================
    // STOMP ENDPOINT
    // =========================================================

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        boolean isProd = Arrays.asList(environment.getActiveProfiles())
                .contains("prod");

        String frontendUrl = environment.getProperty(
                "app.frontend.url",
                "http://localhost:3000"
        );

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(isProd
                        ? frontendUrl
                        : "http://localhost:3000")
                .withSockJS()
                .setHeartbeatTime(10000)
                .setSessionCookieNeeded(false);
    }

    // =========================================================
    // JWT AUTH FOR CONNECT
    // =========================================================

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(
                    Message<?> message,
                    MessageChannel channel) {

                StompHeaderAccessor accessor =
                        StompHeaderAccessor.wrap(message);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                    List<String> authHeaders =
                            accessor.getNativeHeader("Authorization");

                    if (authHeaders == null || authHeaders.isEmpty()) {
                        throw new IllegalArgumentException("Missing Authorization header");
                    }

                    String rawHeader = authHeaders.get(0);

                    if (!rawHeader.startsWith("Bearer ")) {
                        throw new IllegalArgumentException("Invalid Authorization format");
                    }

                    String token = rawHeader.substring(7);

                    try {

                        String username = jwtUtil.extractUsername(token);

                        UserDetails userDetails =
                                userDetailsService.loadUserByUsername(username);

                        if (!jwtUtil.validateToken(token, userDetails)) {
                            throw new IllegalArgumentException("Invalid JWT");
                        }

                        accessor.setUser(new StompPrincipal(username));

                        logger.info("WebSocket authenticated for {}", username);

                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid WebSocket JWT");
                    }
                }

                return message;
            }
        });
    }

    // =========================================================
    // MESSAGE CONVERTER
    // =========================================================

    @Override
    public boolean configureMessageConverters(List<MessageConverter> converters) {

        converters.add(new MappingJackson2MessageConverter());
        return false;
    }

    // =========================================================
    // PRINCIPAL
    // =========================================================

    private static class StompPrincipal implements Principal {

        private final String name;

        public StompPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}