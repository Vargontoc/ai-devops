# Sprint [5] : [Orquestación Agéntica Reactiva y WebSockets]

## Objetivos
- [x] **Desacoplar Git del Alta**: La API ya no clona el proyecto de manera síncrona/automática durante su registro.
- [x] **EntryPoint de Ejecución Asíncrono**: Crear nuevo Endpoint `POST` para recibir peticiones desencadenantes delegando a asincronía (HTTP 202).
- [x] **Inyección de Websockets**: Implementar la capa interna de WebSocket (STOMP Default) para stremear del backend.
- [x] **Orquestación con Inteligencia Artificial**: Utilizando Spring AI, proveer el modelo Ollama con todas las Tools (incluyendo Clone Tool). Dejar que decida el procedimiento. Streamear cada acción y texto devuelto directo al WebSocket del proyecto.
- [x] **Fallo de Conexión Duro**: Capturar excepción de Ollama notificando "Agente detenido" instantáneamente.

## Validación del Sprint
- [x] Ejecutar simulación local de Socket STOMP confirmando emisión.
- [x] Confirmar exclusión de clonado en `ProjectControllerTest.java`.

## Comandos de Verificación
- `mvn clean install`
- `mvn test`

## Pruebas manuales
- 

## Fallos y bloqueos
*Registrar aqui cualquier impedimento técnico encontrado.*

## Sugerencia y Deuda técnica
- Necesidad de Sandbox de comandos: Actualmente un agente sin restricciones podría teóricamente ejecutar `rm -rf /` fuera de control. 

## Conclusión
- **Estado**: Éxito
- **Commit Sugerido**: `feat(agent): refactor to full llm-driven pipeline with async tool execution and websocket streaming`