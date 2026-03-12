# Reglas de Desarrollo del Orquestador

## 1. Arquitectura de Herramientas (MCP)
- Cualquier funcionalidad de "acción" (leer disco, ejecutar bash, clonar git) debe implementarse como una **Tool** en el backend de Spring Boot siguiendo el protocolo MCP.
- No permitas que el LLM ejecute comandos directos en el host; siempre debe pasar por la capa de validación del Backend.

## 2. Validación de Proyectos
- **REGLA DE ORO**: El sistema debe rechazar cualquier operación de despliegue si no existe `deploy.md`. 
- El código de validación debe ser centralizado y reutilizable.

## 3. Manejo de Errores
- Todos los errores de despliegue deben ser capturados y persistidos en la base de datos local (SQLite).
- El agente debe formatear los errores de consola para que sean legibles por el modelo de Ollama.

## 4. Estilo de Código
- **Backend**: Seguir arquitectura limpia (Controller -> Service -> Repository). Usar Records para DTOs.
- **Frontend**: Composition API en Vue 3. Mantener los componentes pequeños y enfocados en una sola tarea (ej. `ProjectCard.vue`, `TerminalLog.vue`).

## 5. Docker y Seguridad
- Las rutas de archivos siempre deben ser relativas al volumen `/workspace` configurado en el orquestador.
