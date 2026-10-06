package com.oposiciones.temario;

import jakarta.persistence.*;

@Entity
@Table(name = "fragmentos", uniqueConstraints = @UniqueConstraint(columnNames = {"tema_id", "orden"}))
public class Fragmento {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tema_id", nullable = false)
  private Long temaId;

  @Column(nullable = false)
  private int orden;

  @Column(nullable = false)
  private int pagina;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String texto;

  protected Fragmento() {}

  public Fragmento(Long temaId, int orden, int pagina, String texto) {
    this.temaId = temaId;
    this.orden = orden;
    this.pagina = pagina;
    this.texto = texto;
  }

  public Long getId() { return id; }
  public Long getTemaId() { return temaId; }
  public int getOrden() { return orden; }
  public int getPagina() { return pagina; }
  public String getTexto() { return texto; }
}
