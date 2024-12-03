#!/bin/bash
set -e

# Configura el runner si no está ya configurado
if [ ! -f ".runner" ]; then
  echo "Configurando el runner..."
  ./config.sh --url "https://github.com/ignasijuez/CICD_GA" --token ${RUNNER_TOKEN} --name "k8s_runner" --work "_work"
  touch .runner
else
  echo "Runner ya configurado, iniciando..."
fi

# Ejecuta el runner
exec ./run.sh