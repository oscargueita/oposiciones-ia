#!/usr/bin/env bash
# Compila el frontend y lo deja servido por Boot en la misma URL.
# Uso: ./scripts/build-frontend.sh  (después: ./mvnw spring-boot:run)
set -euo pipefail
cd "$(dirname "$0")/../frontend"
npm run build
cd ..
rm -rf src/main/resources/static
cp -r frontend/dist src/main/resources/static
echo "Frontend servido en http://localhost:8080 (misma URL que la API)"
