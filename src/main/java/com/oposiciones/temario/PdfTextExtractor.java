package com.oposiciones.temario;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.ToXMLContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;

/**
 * Extrae el texto del PDF página a página. El parser PDF de Tika envuelve
 * cada página en {@code <div class="page">} en su salida XHTML.
 */
@Component
public class PdfTextExtractor {

  private static final Pattern PAGE_SPLIT = Pattern.compile("<div class=\"page\">");
  private static final Pattern TAG = Pattern.compile("<[^>]+>");

  public record PdfDocument(List<String> pages) {
    public String fullText() { return String.join("\n\n", pages); }
  }

  public PdfDocument extract(String filename, byte[] bytes) {
    try (InputStream in = new ByteArrayInputStream(bytes)) {
      AutoDetectParser parser = new AutoDetectParser();
      ContentHandler handler = new ToXMLContentHandler();
      parser.parse(in, handler, new Metadata(), new ParseContext());
      return new PdfDocument(splitPages(handler.toString()));
    } catch (Exception e) {
      throw new TemarioException("PDF corrupto o ilegible: " + filename, 422, e);
    }
  }

  private List<String> splitPages(String xhtml) {
    String[] parts = PAGE_SPLIT.split(xhtml, -1);
    List<String> pages = new ArrayList<>();
    // parts[0] es el preámbulo antes de la primera página
    for (int i = 1; i < parts.length; i++) {
      String text = TAG.matcher(parts[i]).replaceAll(" ")
          .replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")
          .replaceAll("[ \\t\\x0B\\f\\r]+", " ").strip();
      pages.add(text);
    }
    return pages;
  }
}
