#!/usr/bin/env bash
# Setup de oposiciones-ia en una máquina nueva (macOS / Linux).
# Idempotente: solo instala lo que falta y verifica el resto.
# Uso: ./scripts/setup.sh [--with-piper]
set -euo pipefail

WITH_PIPER=false
[ "${1:-}" = "--with-piper" ] && WITH_PIPER=true

ok()   { echo "OK  $1"; }
warn() { echo "AVISO  $1"; }
die()  { echo "ERROR $1" >&2; exit 1; }

echo "== 1. Java 21 =="
java -version 2>&1 | head -1
[[ "$(java -version 2>&1 | head -1)" == *"21"* ]] || die "instala JDK 21 (Temurin/Adoptium) y reintenta"

echo "== 2. Ollama =="
if ! command -v ollama >/dev/null 2>&1; then
  echo "Instalando Ollama..."
  if [[ "$OSTYPE" == "darwin"* ]]; then
    command -v brew >/dev/null || die "instala Homebrew primero: https://brew.sh"
    brew install --cask ollama
  else
    curl -fsSL https://ollama.com/install.sh | sh
  fi
fi
# Arrancar servidor si no responde
if ! ollama list >/dev/null 2>&1; then
  echo "Arrancando Ollama en fondo..."
  if [[ "$OSTYPE" == "darwin"* ]]; then open -a Ollama 2>/dev/null || (ollama serve >/tmp/ollama.log 2>&1 &); \
  else (ollama serve >/tmp/ollama.log 2>&1 &); fi
  for _ in $(seq 1 30); do sleep 2; ollama list >/dev/null 2>&1 && break; done
fi
ollama list >/dev/null 2>&1 || die "Ollama no responde en http://localhost:11434"
ok "Ollama $(ollama --version 2>/dev/null | head -1)"

echo "== 3. Modelos (descarga una sola vez) =="
ollama pull nomic-embed-text
ollama pull llama3.1:8b
ollama list

echo "== 4. Voz =="
if [[ "$OSTYPE" == "darwin"* ]]; then
  VOZ=$(say -v '?' 2>/dev/null | grep -c "^Mónica" || true)
  [ "$VOZ" -ge 1 ] && ok "say + voz Mónica (por defecto)" || warn "voz Mónica no encontrada; ajusta app.tts.say-voice"
else
  warn "macOS ausente: usa Piper (./scripts/setup.sh --with-piper) y app.tts.motor=piper"
fi
if $WITH_PIPER; then
  command -v uv >/dev/null || { curl -LsSf https://astral.sh/uv/install.sh | sh; export PATH="$HOME/.local/bin:$PATH"; }
  uv tool install piper-tts
  export PATH="$HOME/.local/bin:$PATH"
  mkdir -p data/voces
  cd data/voces
  [ -f es_ES-davefx-medium.onnx ] || curl -sL -o es_ES-davefx-medium.onnx "https://huggingface.co/rhasspy/piper-voices/resolve/main/es/es_ES/davefx/medium/es_ES-davefx-medium.onnx"
  [ -f es_ES-davefx-medium.onnx.json ] || curl -sL -o es_ES-davefx-medium.onnx.json "https://huggingface.co/rhasspy/piper-voices/resolve/main/es/es_ES/davefx/medium/es_ES-davefx-medium.onnx.json"
  cd ../..
  ok "Piper + voz es_ES en data/voces/"
fi

echo "== 5. Compilación y tests =="
./mvnw -B -q -DskipTests package 2>&1 | tail -3 || die "falló la compilación"
ok "compila"

echo
echo "Listo. Arranca con:  ./mvnw spring-boot:run"
echo "Temario: deja tus PDFs en data/temario/ y súbelos con:"
echo '  curl -F "files=@data/temario/tu-tema.pdf" http://localhost:8080/api/v1/temas'
echo "Guía completa en README.md"
