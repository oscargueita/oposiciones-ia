package com.examprep.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Forwards UI routes to the React bundle (API untouched). */
@Controller
public class SpaController {

  @GetMapping({"/temas", "/temas/**", "/repaso", "/tests", "/material"})
  public String forward() {
    return "forward:/index.html";
  }
}
