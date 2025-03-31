package com.example.scheduler;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Instant;

@SpringBootApplication
public class SchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }

    @Bean
    MethodToolCallbackProvider toolCallbackProvider(DogAdoptionScheduler scheduler) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(scheduler)
                .build();
    }
}


@Component
class DogAdoptionScheduler {

    @Tool(description = "schedule an appointment to pickup or adopt a dog ")
    String scheduleForAdoptionAndPickup(@ToolParam(description = "the id of the dog") int dogId,
                                        @ToolParam(description = "the name of the dog") String dogName) {
        var instant = Instant
                .now()
                .toString();
        System.out.println("scheduled adoption for dog: " + dogName + " at " + instant);
        return instant;
    }
}
