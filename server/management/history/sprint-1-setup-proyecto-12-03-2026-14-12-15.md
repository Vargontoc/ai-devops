# Sprint [1] : [Setup proyecto]

## Objetivos
- [x] Creacion estructura incial proyecto
- [x] Paquete base del proyecto es  `es.vargontoc.agents.devops`
- [x] Agregar dependencias necesarias para cumplir con el alcance del proyecto
- [x] Creación de dockerfile para el proyecto
- [x] Agregar servicio `devops-agent-server` en ../docker-compose.yaml para levantar el proyecto   

## Validación del Sprint
- [x] Validar que el proyecto se construye correctamente
- [x] Validar que los tests se ejecutan correctamente    

## Comandos de Verificación
- mvn clean install 
- mvn test

## Fallos y bloqueos
Ninguno encontrado. La resolución de dependencias y ejecución de tests ha sido exitosa.

## Sugerencia y Deuda técnica
- Considerar agregar la configuración centralizada de perfiles (dev, prod) en el futuro.
- Configurar logs en archivos de forma programática o por profiles.

## Conclusión
- **Estado**: Éxito
- **Commit Sugerido**: `feat(core): setup initial spring boot backend project and docker config`
