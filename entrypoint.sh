#!/bin/bash

# Iniciar el servidor de Ollama en segundo plano
ollama serve &

# Esperar a que el puerto 11434 esté abierto (máximo 30 segundos)
echo "Esperando a que Ollama (puerto 11434) esté listo..."
for i in {1..30}; do
  if (echo > /dev/tcp/localhost/11434) >/dev/null 2>&1; then
    echo "--- Puerto 11434 detectado! ---"
    break
  fi
  sleep 1
done

# Crear el modelo si no existe
if ! ollama list | grep -q "devops-agent"; then
  echo "--- Creando modelo devops-agent desde Modelfile ---"
  ollama create devops-agent -f /root/Modelfile
else
  echo "--- El modelo devops-agent ya existe, saltando creación ---"
fi

# Mantener el proceso principal activo
wait -n