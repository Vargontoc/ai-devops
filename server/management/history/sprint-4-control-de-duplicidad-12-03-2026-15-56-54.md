# Sprint [4] : [Control de Duplicidad en Proyectos]

## Objetivos
- [x] **Validación de Nombre Duplicado**: Implementar lógica para evitar la creación de proyectos con idéntico `name`.
- [x] **Validación de Path Duplicado**: Implementar lógica para evitar el registro o clonado de repositorios con un `path` ya mapeado.
- [x] **Integridad en BD**: Añadir restricciones `UNIQUE` sobre `name` y `path` al esquema de SQLite.
- [x] **Endpoints de error (409 Conflict)**: Atrapar el duplicado en la capa Web y regresar texto inteligible.

## Validación del Sprint
- [x] Verificar vía Unit Testing en Controller y Repositorio que los duplicados sean denegados.

## Comandos de Verificación
- `mvn clean install`
- `mvn test`

## Pruebas manuales
- Levantar servidor web y repetir dos peticiones POST a `/api/v1/projects` con el mismo payload.
- Registrar el proyecto X y el proyecto Y referenciando al mismo repositorio Github, confirmar error en el segundo.

## Fallos y bloqueos
*Registrar aqui cualquier impedimento técnico encontrado.*

## Sugerencia y Deuda técnica
*Registrar aqui ideas para mejorar el código en el futuro.*

## Conclusión
- **Estado**: Éxito
- **Commit Sugerido**: `feat(api): implement unique constraints and 409 conflict validation for duped projects`
