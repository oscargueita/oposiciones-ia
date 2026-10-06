# Research: Estudio Tema por Voz (002)

**Fecha**: 2026-10-07 | **Base**: 001 (Tema/Fragmento/chunks por página). Stack: Java 21, Boot 3.3, SQLite + Flyway.

## Decision 1: Interfaz TtsService desacoplada

- **Decision**: `TtsService { Audio sintetizar(String texto); }` con `formato=WAV`, `muestras` 22050Hz mono 16-bit. Ningún caso de uso conoce el motor.
- **Rationale**: Constitution V exige desacoplo; permite cambiar say→Piper→Coqui sin tocar dominio.
- **Alternatives considered**: Llamar al binario directamente desde el servicio (rechazado: acopla dominio a motor).

## Decision 2: Motor v1 = `say` macOS, Piper opcional

- **Decision**: `SayTtsService` por defecto (`say -v <voz-es> -o <fichero> --file-format=WAVE --data-format=LEI16@22050`), voz configurable `app.tts.say-voice` (p. ej. `Mónica`/`Jorge`). `PiperTtsService` implementado tras property `app.tts.motor=piper` + `app.tts.piper-bin` y modelo `es_ES` (~60MB, descarga manual a `data/voces/`).
- **Rationale**: `say` funciona hoy sin descargas ni binarios (offline tras instalación macOS); Piper da mejor calidad pero exige modelo externo en v1. Ambos cumplen local-first.
- **Alternatives considered**: Solo Piper (bloquea v1 hasta descargar modelo); Coqui/edge-TTS cloud (rechazado: nube).

## Decision 3: Audio por fragmento en ficheros + tabla de estado

- **Decision**: `data/audio/{temaId}/{fragmentoId}.wav` (gitignored) + tabla `fragmento_audio(fragmento_id PK, duracion_seg, generado_en)`. Playlist = fragmentos ordenados de 001 con URL de audio cada uno.
- **Rationale**: Implementa aclaración B (audio por fragmento en orden); ficheros permiten streaming/serve directo y regeneración selectiva; la tabla evita regenerar lo ya generado.
- **Alternatives considered**: Un WAV por tema (rompe pausa exacta y regeneración parcial); BLOB en SQLite (hincha la DB y complica serve).

## Decision 4: Generación en segundo plano tras ingesta

- **Decision**: Evento `TemaListoEvent(temaId)` publicado por `TemarioService` al pasar a LISTO; listener `@Async` en módulo `voz` genera WAV por fragmento en orden y marca `fragmento_audio`. Reintento simple: fragmentos sin fila se generan al pedir su audio (lazy fallback).
- **Rationale**: Aclaración C (escucha inmediata); `@Async` desacopla 001 de 002 sin llamadas directas; el fallback perezoso cubre caídas a mitad de generación.
- **Alternatives considered**: Generar en la petición de escucha (espera de minutos, rompe SC); job programado (complejidad innecesaria en v1).

## Decision 5: Progreso = fragmento + offset en segundos

- **Decision**: Tabla `progreso_escucha(tema_id PK, fragmento_id, offset_seg REAL, actualizado_en)`; `PUT` lo graba el cliente al pausar/cerrar; `GET` lo devuelve para continuar. Borrado/reemplazo del tema borra su progreso (FK cascade + limpieza en `borrarContenido`).
- **Rationale**: Aclaración B (segundo exacto); REAL admite fracciones; cascade implementa FR-006.
- **Alternatives considered**: Solo fragmento (rechazado por aclaración B); offset en muestras (acopla a formato WAV).
