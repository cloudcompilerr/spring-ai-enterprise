# Spring AI Showcase

A comprehensive Spring Boot application demonstrating various aspects of Spring AI in a well-structured, modular format.

## Project Overview

This project showcases the integration of Spring AI capabilities into a Spring Boot application, with a focus on:

- **Prompt Engineering**: Templates and strategies for effective prompting
- **RAG (Retrieval-Augmented Generation)**: Knowledge-based responses using document retrieval
- **Vector Database Integration**: Semantic search using vector embeddings
- **MCP (Model Context Protocol)**: Tools for enhanced AI capabilities
- **API Design**: Clean REST endpoints for AI services
- **Web Interface**: Demo UI for interacting with AI features

## Project Structure

The application is organized into the following modules:

- **Core**: Base configurations and common utilities
- **Prompt Engineering**: Templates and strategies for effective prompting
- **RAG**: Retrieval-Augmented Generation for knowledge-based responses
- **Vector Database**: Integration with pgvector for semantic search
- **MCP**: Model Context Protocol for enhanced AI capabilities
- **API**: REST endpoints for accessing AI services
- **Service**: Business logic layer
- **Demo**: Web interface for demonstrating capabilities

## Prerequisites

- Java 21 or higher
- Maven 3.8 or higher
- PostgreSQL 14 or higher with pgvector extension
- OpenAI API key

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/spring-ai-enterprise.git
   cd spring-ai-enterprise
   ```

2. Set up the PostgreSQL database:
   ```bash
   # Create the database
   createdb spring_ai_db
   
   # Install pgvector extension (if not already installed)
   # This may require superuser privileges
   psql -d spring_ai_db -c "CREATE EXTENSION IF NOT EXISTS vector;"
   ```

3. Configure your OpenAI API key:
   ```bash
   export OPENAI_API_KEY=your_api_key_here
   ```

4. Build the project:
   ```bash
   mvn clean install
   ```

5. Run the application:
   ```bash
   cd demo
   mvn spring-boot:run
   ```

6. Access the application:
   - Web interface: http://localhost:8080
   - API documentation: http://localhost:8080/swagger-ui.html

## Features

### Java 21 Features

This project leverages modern Java 21 features to enhance code quality and developer productivity:

- **Pattern Matching for Switch**: Enhanced switch expressions with type patterns and guards
- **Record Patterns**: Destructuring records in pattern matching contexts
- **String Templates**: More readable string formatting (preview feature)
- **Enhanced Stream API**: Improved collectors and stream operations
- **Sealed Classes**: Controlled inheritance hierarchies for better domain modeling
- **Text Blocks**: Multi-line string literals for improved readability
- **Virtual Threads**: Lightweight threads for high-throughput concurrent applications
- **Enhanced Error Handling**: More expressive try-catch blocks and exception handling

### Prompt Engineering

- Reusable prompt templates
- System and user prompt separation
- Template variables for dynamic content
- Prompt chaining for complex tasks

### RAG (Retrieval-Augmented Generation)

- Document management (upload, search, delete)
- Automatic document chunking and embedding
- Semantic search for relevant content
- Context-aware AI responses

### Vector Database Integration

- pgvector integration for vector storage
- Efficient similarity search
- Metadata filtering
- Vector indexing for performance

### MCP (Model Context Protocol)

- Tool registration and management
- Advanced MCP tools leveraging Java 21 features:
  - Weather tool for location-based weather information
  - Text analyzer tool for sentiment analysis, entity extraction, and text statistics
  - File analyzer tool for file/directory analysis and content inspection
- Tool-augmented chat responses
- Dynamic tool selection and execution

## API Endpoints

### Chat API

- `POST /api/chat/complete`: Generate a chat response
- `POST /api/chat/rag`: Generate a RAG-enhanced response
- `POST /api/chat/template/{templateName}`: Generate a response using a specific template
- `POST /api/chat/tools`: Generate a tool-augmented response

### Document API

- `GET /api/documents`: Get all documents
- `POST /api/documents`: Create a new document
- `GET /api/documents/{id}`: Get a document by ID
- `GET /api/documents/search`: Search for documents by title
- `DELETE /api/documents/{id}`: Delete a document

## Web Interface

The demo module provides a web interface for interacting with the AI features:

- **Home**: Overview of the application
- **Chat**: Basic chat interface with system prompt customization
- **RAG**: Question answering based on the knowledge base
- **Documents**: Document management for the RAG system

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.