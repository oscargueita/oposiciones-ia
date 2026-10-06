package com.oposiciones.temario;

import jakarta.persistence.*;

@Entity
@Table(name = "chunk_embedding")
public class ChunkEmbedding {

  @Id
  @Column(name = "chunk_id")
  private Long chunkId;

  @Column(nullable = false)
  private byte[] embedding;

  protected ChunkEmbedding() {}

  public ChunkEmbedding(Long chunkId, byte[] embedding) {
    this.chunkId = chunkId;
    this.embedding = embedding;
  }

  public Long getChunkId() { return chunkId; }
  public byte[] getEmbedding() { return embedding; }
}
