# Sprint [2] : [Modelo de Datos Core y Git Manager Base]

## Objetivos
- [x] **Configuración de la Base de Datos**: Crear las entidades (`Project`, `DeploymentLog`) usando JPA y validarlos en SQLite.
- [x] **DTOs**: Crear los records DTOs (`ProjectDto`, `ProjectCreateRequest`, etc.) para transferencia de datos entre capas.
- [x] **Repositorios JPA**: Crear las interfaces `ProjectRepository` y `DeploymentLogRepository`.
- [x] **Git Manager Base**: Crear un servicio (`GitManagerService`) que exponga utilidades para clonar y extraer repositorios de forma asíncrona, gestionando tokens de ser necesario.
- [x] **Testing Básico**: Añadir tests para los repositorios JPA y la lógica de clonado básica.

## Validación del Sprint
- [x] Validar que se puedan persistir proyectos en `devops.db`.
- [x] Validar que `GitManagerService` pueda clonar un repositorio de prueba exitosamente en el `/workspace`.

## Pruebas manuales
- 

## Comandos de Verificación
- `mvn clean install`
- `mvn test`

## Fallos y bloqueos
- Se encontró un problema en los tests unitarios donde `SQLite` no tenía acceso al volumen `data/` al crearse en el test profile por defecto, forzando a crear una base de datos local y/o corregir el `application.yml` para los tests.
- La ejecución del test de Path Traversal de JGit descubrió que Java NIO `Paths.normalize()` eliminaba los `../` de forma segura por defecto si no son pasados como string directos resueltos localmente, por lo que refactorizamos el check para garantizar robustez.

## Sugerencia y Deuda técnica
- Implementar mecanismo estandar de inyección de dependencias para el `GitTools` cuando la IA necesite más acciones (Pull, Branch checkout, etc).
- Encriptación real de los tokens de GitHub/GitLab antes de guardarlos en BD SQLite.

## Conclusión
- **Estado**:  Éxito
- **Commit Sugerido**: `feat(core): implement database models, repositories and JGit async cloning service`
