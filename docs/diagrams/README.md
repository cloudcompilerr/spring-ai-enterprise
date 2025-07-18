# Spring AI Enterprise Architecture Diagrams

This directory contains architecture diagrams for the Spring AI Enterprise project in PlantUML format.

## Diagram Types

1. **High-Level Design (HLD)**: Shows the major components and their interactions at a high level.
2. **Low-Level Design (LLD)**: Shows detailed class diagrams with methods, attributes, and relationships.
3. **C4 Context Diagram**: Shows the system in context with external systems and users.
4. **C4 Container Diagram**: Shows the containers (applications, data stores) that make up the system.

## Viewing the Diagrams

These diagrams are written in PlantUML, a text-based diagramming tool. To view them:

### Option 1: Online PlantUML Server

1. Go to [PlantUML Online Server](https://www.plantuml.com/plantuml/uml/)
2. Copy the content of any .puml file
3. Paste it into the editor
4. The diagram will be rendered automatically

### Option 2: IDE Plugins

Many IDEs have PlantUML plugins:

- **VS Code**: Install the "PlantUML" extension
- **IntelliJ IDEA**: Install the "PlantUML integration" plugin
- **Eclipse**: Install the "PlantUML" plugin

### Option 3: Command Line

1. Install PlantUML (requires Java): `brew install plantuml` (macOS) or download from [PlantUML website](https://plantuml.com/download)
2. Run: `plantuml filename.puml`
3. This will generate an image file in the same directory

## Diagram Descriptions

### High-Level Design (high-level-design.puml)

This diagram shows the major components of the Spring AI Enterprise system and how they interact with each other and external systems. It provides a bird's-eye view of the system architecture.

### Low-Level Design (low-level-design.puml)

This diagram shows the detailed class structure of the system, including classes, interfaces, methods, attributes, and their relationships. It's useful for understanding the implementation details.

### C4 Context Diagram (c4-context.puml)

This diagram shows the Spring AI Enterprise system in context with its users and external systems. It's useful for understanding the system's place in the broader ecosystem.

### C4 Container Diagram (c4-container.puml)

This diagram shows the containers (applications, data stores) that make up the Spring AI Enterprise system and how they interact with each other and external systems. It's useful for understanding the system's internal structure at a high level.

## Updating the Diagrams

To update these diagrams:

1. Edit the corresponding .puml file
2. Use one of the methods above to render the updated diagram
3. Commit the changes to the repository

## References

- [PlantUML Documentation](https://plantuml.com/)
- [C4 Model](https://c4model.com/)
- [C4-PlantUML](https://github.com/plantuml-stdlib/C4-PlantUML)