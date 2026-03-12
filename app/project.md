# ⚙️ Módulo: Backend Server (MCP Orchestrator)

## 🎯 Alcance y Funcionalidad
El servidor es el motor de ejecución del sistema. Sus responsabilidades son:
1.  **Server MCP**: Exponer herramientas (Tools) para que el modelo de IA interactúe con el OS.
2.  **Gestor de Despliegue**: Ejecutar scripts de Bash/PowerShell y monitorizar procesos Docker.
3.  **Gestor de Git**: Clonar y actualizar repositorios (locales y remotos con Token configurados por el usuario).
4.  **API de Control**: Proveer endpoints para que el Frontend (Vue 3) gestione los proyectos.
5.  **Persistencia**: Guardar rutas, estados de despliegue e historial de errores.

## 🛠️ Stack Tecnológico Específico
- **Runtime**: Java 21.
- **Framework**: Spring Boot 3.4 (Web, Data JPA).
- **IA/MCP**: Spring AI con soporte para Model Context Protocol.
- **Base de Datos**: SQLite (almacenada en `data/devops.db`).
- **Librerías Clave**: 
    - `JGit` para operaciones Git.
    - `Docker Java SDK` (opcional) o ProcessBuilder para comandos Docker.
- **Logs**: Captura de logs asíncrona mediante WebSockets o SSE.

## 🗄️ Modelo de Datos (Capa Ligera)
- `Project`: ID, Name, Path (Local/Repo), Type (LOCAL/REMOTE), Token (Encrypted), LastStatus.
- `DeploymentLog`: ProjectID, Timestamp, ExitCode, FullLog, AISuggestions.