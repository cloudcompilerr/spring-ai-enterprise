package com.example.ai.mcp.tools;

import com.example.ai.mcp.model.McpTool;
import com.example.ai.mcp.service.McpService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Example MCP tool for getting weather information.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherTool {

    private final McpService mcpService;
    private final Random random = new Random();

    /**
     * Registers the weather tool with the MCP service.
     */
    @PostConstruct
    public void register() {
        Map<String, McpTool.ParameterInfo> parameters = new HashMap<>();
        
        parameters.put("location", McpTool.ParameterInfo.builder()
                .type("string")
                .description("The city and state, e.g. San Francisco, CA")
                .required(true)
                .build());
        
        parameters.put("unit", McpTool.ParameterInfo.builder()
                .type("string")
                .description("The unit of temperature, either 'celsius' or 'fahrenheit'")
                .required(false)
                .enumValues(List.of("celsius", "fahrenheit"))
                .build());
        
        McpTool weatherTool = McpTool.builder()
                .name("get_current_weather")
                .description("Get the current weather in a given location")
                .parameters(parameters)
                .required(false)
                .build();
        
        mcpService.registerTool(weatherTool, this::executeWeatherTool);
    }

    /**
     * Executes the weather tool.
     *
     * @param arguments The arguments for the tool
     * @return The weather information
     */
    private String executeWeatherTool(Map<String, Object> arguments) {
        String location = (String) arguments.get("location");
        String unit = arguments.containsKey("unit") ? (String) arguments.get("unit") : "celsius";
        
        // In a real implementation, this would call a weather API
        // For demo purposes, we'll return mock data
        int temperature = random.nextInt(35) + (unit.equals("fahrenheit") ? 40 : 5);
        String[] conditions = {"sunny", "partly cloudy", "cloudy", "rainy", "stormy", "snowy"};
        String condition = conditions[random.nextInt(conditions.length)];
        
        log.info("Weather request for {}: {} and {} degrees {}", location, condition, temperature, unit);
        
        return String.format("The current weather in %s is %s with a temperature of %d°%s",
                location, condition, temperature, unit.equals("celsius") ? "C" : "F");
    }
}