package com.examprep.resumen;

import com.examprep.syllabus.TopicContentDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Invalidates cheatsheet + mindmap when topic content is deleted/replaced. */
@Component
public class ResumenListener {

  private final CheatsheetRepository cheatsheets;
  private final MindmapRepository mindmaps;

  public ResumenListener(CheatsheetRepository cheatsheets, MindmapRepository mindmaps) {
    this.cheatsheets = cheatsheets;
    this.mindmaps = mindmaps;
  }

  @EventListener
  public void invalidate(TopicContentDeletedEvent event) {
    cheatsheets.deleteById(event.topicId());
    mindmaps.deleteById(event.topicId());
  }
}
