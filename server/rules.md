# ⚖️ Rules: Backend Development

## 1. Implementación de MCP Tools
- Toda herramienta capaz de ejecutar código o leer archivos debe estar anotada con `@Tool`.
- **Validación de Sandbox**: Antes de cualquier operación de I/O, verificar que el `Path` esté dentro del volumen `/workspace`.
- Las respuestas de las herramientas deben ser estructuradas: separar `stdout` de `stderr`.

## 2. Ejecución de Despliegues
- Los despliegues NO deben bloquear el hilo de la petición HTTP. Usar `@Async` o un `ExecutorService`.
- Cada ejecución debe generar un archivo de log físico en `workspace/.logs/` además de guardarse en la DB.
- **Validación Obligatoria**: Si no se encuentra `deploy.md` en la raíz del proyecto objetivo, la Tool debe retornar un error descriptivo y abortar.

## 3. Persistencia y DTOs
- Usar **Java Records** para todos los DTOs y respuestas de API.
- Las Entidades JPA deben usar nombres de tabla explícitos para compatibilidad con SQLite.
- Mantener la lógica de negocio en `@Service`, nunca en los `@Controller`.

## 4. Gestión de Git
- Implementar un sistema de "Clean up" para proyectos remotos si el despliegue falla críticamente.
- Los Tokens de acceso deben manejarse mediante variables de entorno o configuraciones seguras, nunca hardcodeados.

## 5. Integración con Ollama
- El servidor debe comunicarse con Ollama (puerto 11434) usando el cliente de Spring AI.
- Se debe validar la conexión con el modelo `devops-agent` al arrancar el servidor.

## 6. Ciclo de Vida de Desarrollo (Sprints)
- Antes de escribir cualquier línea de código, el agente DEBE leer `management/sprint-review.md`.
- Solo se trabajarán los objetivos listados en el sprint actual. 
- Si el agente detecta una tarea necesaria no listada, debe proponerla como "Sugerencia" en el documento antes de ejecutarla.
- Al finalizar un objetivo, marcar con `[x]`. 
- **Cierre de Sprint**: Al completar todos los puntos, el agente debe generar automáticamente el resumen de la conclusión y el mensaje de commit sugerido.
- Al cerrar el sprint, el agente debe moverlo a `management/history/` guardandolo con el nombre `sprint-[N]-[Nombre corto]-[Fecha(formato dd-MM-yyyy-HH-mm-ss)].md` y crear un sprint-review.md basado en el sprint_template.md 