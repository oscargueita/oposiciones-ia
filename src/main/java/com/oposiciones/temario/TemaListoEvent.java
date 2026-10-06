package com.oposiciones.temario;

/** Publicado cuando un tema queda LISTO (ingesta o reemplazo). Lo consume el módulo voz. */
public record TemaListoEvent(Long temaId) {
}
