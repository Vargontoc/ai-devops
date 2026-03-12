# Orquestador DevOps Inteligente (Local-Agent)

## Visión General
Este proyecto es un sistema de orquestación DevOps inteligente que permite automatizar tareas de despliegue y gestión de proyectos. Está diseñado para ser utilizado con herramientas MCP y permite la ejecución de comandos en un entorno local. 

## Estructura del sistema
- **/server**: API en Spring Boot 3.4. Actua como servidor MCP y puente entre UI y Ollama. Gestiona Git y la ejecución de procesos Docker.
- **/app**: Interfaz en Vue 3 + Pinia para la gestión de proyectos, visualización de logs en tiempo real y triggering manual.
- **/infraestructure**: Contiene el Docker compose con soporte dual (CPI/GPU) y el ModelFile del Agente. Tambien se incluirá los servicios de Server y App en el mismo docker compose para facilitar el despliegue.
- **/workspace**: Directorio (volumen persistente) donde se alojan los proyectos gestionados

## Flujo de trabajo del agente
1. **Identificación**: Localiza proyectos en '/workspace' o clona mediante URL + Token.
2. **Validación Crucial**: Si el proyecto NO tiene un archivo 'deploy.md', es marcado como **INVÁLIDO**
3. **Ejecución**: Lee 'deploy.md', ejecuta el script indicado y monitoriza el estado del contenedor Docker resultante.4. **Análisis**: En caso de error, lee el 'std_err', genera un log y propone una solución técnica al usuario si reconoce el stack, si no lo reconoce, responde con el log del fallo y "No se reconoce el stack".  

## Stack
- **LLM**: Ollama (llama3.2:3b o superior)
- **Backend**: Java 21, Spring Boot, Spring AI, SQLite
- **Frontend**: Vue 3, Pinia, Tailwind CSS, Websockets
