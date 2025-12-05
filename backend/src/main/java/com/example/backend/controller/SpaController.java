package com.example.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpaController {

  private static final Logger logger = LoggerFactory.getLogger(SpaController.class);

  // SPA fallback - возвращаем index.html для всех маршрутов кроме API
  @GetMapping(value = "/{path:[^\\.]*}", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<Resource> spaFallback() {
    logger.debug("Serving SPA fallback - index.html for non-API route");
    Resource resource = new ClassPathResource("static/index.html");
    return ResponseEntity.ok().body(resource);
  }
}
