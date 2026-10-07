package com.examprep.syllabus;

import jakarta.persistence.*;

@Entity
@Table(name = "fragmentos", uniqueConstraints = @UniqueConstraint(columnNames = {"tema_id", "orden"}))
public class Fragment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tema_id", nullable = false)
  private Long topicId;

  @Column(name = "orden", nullable = false)
  private int sequence;

  @Column(name = "pagina", nullable = false)
  private int page;

  @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
  private String text;

  protected Fragment() {}

  public Fragment(Long topicId, int sequence, int page, String text) {
    if (topicId == null) throw new IllegalArgumentException("Tema obligatorio");
    if (sequence < 0) throw new IllegalArgumentException("Orden >= 0");
    if (page < 1) throw new IllegalArgumentException("Página >= 1");
    if (text == null || text.isBlank()) throw new IllegalArgumentException("Texto no vacío");
    this.topicId = topicId;
    this.sequence = sequence;
    this.page = page;
    this.text = text;
  }

  public boolean belongsTo(Long topicId) {
    return this.topicId.equals(topicId);
  }

  public Long getId() { return id; }
  public Long getTopicId() { return topicId; }
  public int getSequence() { return sequence; }
  public int getPage() { return page; }
  public String getText() { return text; }
}
