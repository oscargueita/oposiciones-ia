package com.examprep.syllabus;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmbeddingRepository extends JpaRepository<ChunkEmbedding, Long> {

  @Query("select e from ChunkEmbedding e where e.chunkId in :chunkIds")
  List<ChunkEmbedding> findByChunkIds(List<Long> chunkIds);

  void deleteByChunkIdIn(List<Long> chunkIds);
}
