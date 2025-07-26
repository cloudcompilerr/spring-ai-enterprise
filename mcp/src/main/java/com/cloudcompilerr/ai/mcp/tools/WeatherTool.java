package com.cloudcompilerr.ai.mcp.tools;

import com.cloudcompilerr.ai.mcp.model.McpTool;
import com.cloudcompilerr.ai.mcp.service.McpService;
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
 * This is a demonstration tool that returns mock weather data.
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
        try {
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
            log.info("Weather tool registered successfully");
        } catch (Exception e) {
            log.error("Failed to register weather tool", e);
        }
    }

    /**
     * Executes the weather tool.
     *
     * @param arguments The arguments for the tool
     * @return The weather information
     */
    private String executeWeatherTool(Map<String, Object> arguments) {
        try {
            // Validate arguments
            if (arguments == null || !arguments.containsKey("location")) {
                return "Error: Location is required";
            }
            
            String location = (String) arguments.get("location");
            if (location == null || location.trim().isEmpty()) {
                return "Error: Location cannot be empty";
            }
            
            String unit = "celsius";
            if (arguments.containsKey("unit")) {
                String requestedUnit = (String) arguments.get("unit");
                if ("fahrenheit".equalsIgnoreCase(requestedUnit) || "celsius".equalsIgnoreCase(requestedUnit)) {
                    unit = requestedUnit.toLowerCase();
                }
            }
            
            // In a real implementation, this would call a weather API
            // For demo purposes, we'll return mock data
            int temperature = random.nextInt(35) + (unit.equals("fahrenheit") ? 40 : 5);
            String[] conditions = {"sunny", "partly cloudy", "cloudy", "rainy", "stormy", "snowy"};
            String condition = conditions[random.nextInt(conditions.length)];
            
            int humidity = random.nextInt(60) + 20; // 20-80%
            int windSpeed = random.nextInt(30); // 0-30 mph/kph
            
            log.info("Weather request for {}: {} and {} degrees {}", location, condition, temperature, unit);
            
            return String.format(
                    "The current weather in %s is %s with a temperature of %d°%s. " +
                    "Humidity is at %d%% with wind speeds of %d %s.",
                    location, condition, temperature, unit.equals("celsius") ? "C" : "F",
                    humidity, windSpeed, unit.equals("celsius") ? "kph" : "mph");
        } catch (Exception e) {
            log.error("Error executing weather tool", e);
            return "Error retrieving weather information: " + e.getMessage();
        }
    }
}