package com.example.adoptions.adoptions;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
class AssistantConfiguration {

    @Bean
    McpSyncClient mcpClient() {
        var mcp = McpClient
                .sync(new HttpClientSseClientTransport("http://localhost:8082"))
                .build();
        mcp.initialize();
        return mcp;
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, McpSyncClient mcpSyncClient,
                          DogRepository repository,
                          VectorStore store) {

        if (false)
            repository.findAll().forEach(dog -> {
                var dogument = new Document("id: %s, name: %s, description: %s".formatted(
                        dog.id(), dog.name(), dog.description()
                ));
                store.add(List.of(dogument));
            });

        var system = """
                    You are an AI powered assistant to help people adopt a dog from the adoption\s
                    agency named Pooch Palace with locations in Montreal, Seoul, Tokyo, Singapore, Paris,\s
                    Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs available\s
                    will be presented below. If there is no information, then return a polite response suggesting we\s
                    don't have any dogs available.
                """;
        return builder
                .defaultTools(new SyncMcpToolCallbackProvider(mcpSyncClient))
                .defaultSystem(system)
                .defaultAdvisors(new QuestionAnswerAdvisor(store))
                .build();
    }

}

@Controller
@ResponseBody
class AssistantController {

    private final Map<String, PromptChatMemoryAdvisor> advisors =
            new ConcurrentHashMap<>();

    private final ChatClient chatClient;

    AssistantController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/{user}/inquire")
    String inquire(@PathVariable String user, @RequestParam String question) {
        var advisor = this.advisors.computeIfAbsent(user, _ ->
                PromptChatMemoryAdvisor.builder(new InMemoryChatMemory()).build());
        return this.chatClient
                .prompt()
                .advisors(advisor)
                .user(question)
                .call()
                .content();
    }

}

