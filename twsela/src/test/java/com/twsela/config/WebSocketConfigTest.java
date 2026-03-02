package com.twsela.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    @Mock private WebSocketAuthInterceptor authInterceptor;
    @Mock private MessageBrokerRegistry brokerRegistry;
    @Mock private StompEndpointRegistry endpointRegistry;
    @Mock private StompWebSocketEndpointRegistration endpointRegistration;
    @Mock private ChannelRegistration channelRegistration;

    @Test
    @DisplayName("configureMessageBroker enables /topic and /queue prefixes")
    void configureMessageBroker_setsCorrectPrefixes() {
        WebSocketConfig config = new WebSocketConfig(authInterceptor);
        when(brokerRegistry.enableSimpleBroker("/topic", "/queue")).thenReturn(null);

        config.configureMessageBroker(brokerRegistry);

        verify(brokerRegistry).enableSimpleBroker("/topic", "/queue");
        verify(brokerRegistry).setApplicationDestinationPrefixes("/app");
        verify(brokerRegistry).setUserDestinationPrefix("/user");
    }

    @Test
    @DisplayName("registerStompEndpoints registers /ws with SockJS")
    void registerStompEndpoints_registersWsEndpoint() {
        WebSocketConfig config = new WebSocketConfig(authInterceptor);
        when(endpointRegistry.addEndpoint("/ws")).thenReturn(endpointRegistration);
        when(endpointRegistration.setAllowedOriginPatterns("*")).thenReturn(endpointRegistration);

        config.registerStompEndpoints(endpointRegistry);

        verify(endpointRegistry).addEndpoint("/ws");
        verify(endpointRegistration).setAllowedOriginPatterns("*");
        verify(endpointRegistration).withSockJS();
    }

    @Test
    @DisplayName("configureClientInboundChannel wires auth interceptor")
    void configureClientInboundChannel_wiresInterceptor() {
        WebSocketConfig config = new WebSocketConfig(authInterceptor);

        config.configureClientInboundChannel(channelRegistration);

        verify(channelRegistration).interceptors(authInterceptor);
    }
}
