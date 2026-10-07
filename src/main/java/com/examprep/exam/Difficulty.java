package com.examprep.exam;

/** Eligible difficulty with its drafting instruction for the prompt. */
public enum Difficulty {
  EASY("Redacta una pregunta literal: la answer aparece tal cual en el text. "
      + "Ejemplo: 'Según el artículo X, ¿cuál es el plazo de...'."),
  MEDIUM("Parafrasea el contenido: la answer exige comprender el text, no copiarlo. "
      + "Distractores verosímiles tomados del mismo fragment."),
  HARD("Caso aplicado: plantea una situación práctica que combine dos ideas del text. "
      + "Solo quien comprende el fondo acierta.");

  private final String instruccion;

  Difficulty(String instruccion) {
    this.instruccion = instruccion;
  }

  public String getInstruccion() {
    return instruccion;
  }
}
