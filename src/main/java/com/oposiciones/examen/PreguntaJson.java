package com.oposiciones.examen;

import java.util.List;

/** Esquema JSON exigido al modelo (vía BeanOutputConverter). */
public record PreguntaJson(String enunciado, List<String> opciones, int correcta,
    String explicacion) {
}
