package com.example.adoptions.adoptions;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.DirectChannelSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.modulith.events.Externalized;

@Externalized(ExternalizedIntegrationFlowConfiguration.CHANNEL_NAME)
public record DogAdoptionEvent(int dogId) {
}

@Configuration
class ExternalizedIntegrationFlowConfiguration {

    static final String CHANNEL_NAME = "outboundRequests";

    @Bean(name = CHANNEL_NAME)
    DirectChannelSpec outboundRequestsChannel() {
        return MessageChannels.direct();
    }

    @Bean
    IntegrationFlow integrationFlow(@Qualifier(CHANNEL_NAME) MessageChannel channel) {
        return IntegrationFlow
                .from(channel)
                .handle((payload, headers) -> {
                    System.out.println(payload);
                    headers.forEach((k, v) -> System.out.println(k + ": " + v));
                    return null;
                })

//                .transform(DogAdoptionEvent.class, DogAdoptionEvent::dogId)
//                .split()
//                .aggregate()
//                .route()
//                .filter()
                .get();
    }
}