package com.cloudcompilerr.ai.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents a Model Context Protocol (MCP) tool.
 * MCP tools are functions that can be called by AI models.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpTool {

    /**
     * The name of the tool.
     */
    private String name;
    
    /**
     * Description of what the tool does.
     */
    private String description;
    
    /**
     * The parameters that the tool accepts.
     */
    private Map<String, ParameterInfo> parameters;
    
    /**
     * Whether the tool is required.
     */
    private boolean required;
    
    /**
     * Information about a parameter.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterInfo {
        /**
         * The type of the parameter.
         */
        private String type;
        
        /**
         * Description of the parameter.
         */
        private String description;
        
        /**
         * Whether the parameter is required.
         */
        private boolean required;
        
        /**
         * Enum values if the parameter is an enum.
         */
        private List<String> enumValues;
    }
}