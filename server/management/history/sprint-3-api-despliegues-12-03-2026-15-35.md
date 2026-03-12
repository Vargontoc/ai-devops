# Sprint [3] : [API REST de Proyectos y Motor de Despliegue Base]

## Objetivos
- [x] **Actualizar modelos**: Al agregar proyecto el usuario indicara la rama de ese proyecto, tambien se debe poder actualizar la rama de un proyecto. (Se añadió a BD y DTOs).
- [x] **API de Proyectos (`ProjectController`)**: Crear endpoints basicos CRUD para gestionar los proyectos desde el frontend (Listar, Crear, Obtener).
- [x] **Integrar API con GitManager**: El endpoint de creacion de un `Project` remoto debe disparar el clonado asincrono (Añadido seleccion de la rama especifica de clonado).
- [x] **Servicio de Despliegues (`DeploymentService`)**: Crear la logica que lea el `deploy.md` en el `/workspace/{proyecto_path}` e instancie el OS Command wrapper usando ProcessBuilder, aislando logs a la BD.
- [x] **Validacion de Sandbox en Despliegue**: Asegurar la existencia del folder destino y del `deploy.md` antes de procesar comandos de script.
- [x] **DeployTool Creada**: Añadido el envoltorio de Spring AI para poder invocar la acción desde el MCP.

## Validacion del Sprint
- [x] Pruebas unitarias de los endpoints de la API con MockMvc (`ProjectControllerTest`).
- [x] Pruebas de integracion probando la ejecución de bash simulada vía el `DeploymentServiceTest` capturando logs temporales correctamente.

## Fallos y bloqueos
- Test-Compile falló reiteradamente al no detectar los enums y métodos actualizados debido a clases cacheadas o faltas de método. Se limpió `.db` y la carpeta `target` logrando superar compilación. 
- Los imports de Java util no usados creaban ruidos en el linter.

## Conclusión
- **Estado**:  Exito
- **Commit Sugerido**: `feat(deploy): implement project rest api and deployment execution engine`
