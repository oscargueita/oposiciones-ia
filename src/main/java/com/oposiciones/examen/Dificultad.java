package com.oposiciones.examen;

/** Dificultad elegible con su instrucción de redacción para el prompt. */
public enum Dificultad {
  FACIL("Redacta una pregunta literal: la respuesta aparece tal cual en el texto. "
      + "Ejemplo: 'Según el artículo X, ¿cuál es el plazo de...'."),
  MEDIO("Parafrasea el contenido: la respuesta exige comprender el texto, no copiarlo. "
      + "Distractores verosímiles tomados del mismo fragmento."),
  DIFICIL("Caso aplicado: plantea una situación práctica que combine dos ideas del texto. "
      + "Solo quien comprende el fondo acierta.");

  private final String instruccion;

  Dificultad(String instruccion) {
    this.instruccion = instruccion;
  }

  public String getInstruccion() {
    return instruccion;
  }
}
