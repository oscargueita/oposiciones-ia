package com.examprep.syllabus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Generates minimal valid PDFs (one page = one text) without extra dependencies. */
public final class TestPdf {
  private TestPdf() {}

  public static byte[] ofPages(String... pageTexts) {
    List<byte[]> objs = new ArrayList<>();
    objs.add("<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.US_ASCII)); // 1
    List<Integer> pageObjNums = new ArrayList<>();
    List<byte[]> pageObjs = new ArrayList<>();
    List<byte[]> contentObjs = new ArrayList<>();
    int next = 4; // 3 = font
    for (String ignored : pageTexts) {
      pageObjNums.add(next);
      next += 2;
    }
    StringBuilder kids = new StringBuilder();
    for (int n : pageObjNums) kids.append(n).append(" 0 R ");
    objs.add(("<< /Type /Pages /Kids [" + kids + "] /Count " + pageTexts.length + " >>")
        .getBytes(StandardCharsets.US_ASCII)); // 2
    objs.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>".getBytes(StandardCharsets.US_ASCII)); // 3
    for (int i = 0; i < pageTexts.length; i++) {
      int contentNum = pageObjNums.get(i) + 1;
      pageObjs.add(("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents "
          + contentNum + " 0 R /Resources << /Font << /F1 3 0 R >> >> >>")
          .getBytes(StandardCharsets.US_ASCII));
      contentObjs.add(stream(pageTexts[i]));
    }
    List<byte[]> all = new ArrayList<>(objs);
    for (int i = 0; i < pageTexts.length; i++) {
      all.add(pageObjs.get(i));
      all.add(contentObjs.get(i));
    }
    byte[] header = "%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII);
    List<Integer> offsets = new ArrayList<>();
    int pos = header.length;
    List<byte[]> blocks = new ArrayList<>();
    for (int i = 0; i < all.size(); i++) {
      byte[] body = all.get(i);
      String pre = (i + 1) + " 0 obj\n";
      byte[] preB = pre.getBytes(StandardCharsets.US_ASCII);
      byte[] suf = "\nendobj\n".getBytes(StandardCharsets.US_ASCII);
      offsets.add(pos);
      pos += preB.length + body.length + suf.length;
      blocks.add(concat(preB, body, suf));
    }
    StringBuilder xref = new StringBuilder("xref\n0 " + (all.size() + 1) + "\n");
    xref.append("0000000000 65535 f \n");
    for (int o : offsets) xref.append(String.format("%010d 00000 n \n", o));
    xref.append("trailer\n<< /Size ").append(all.size() + 1).append(" /Root 1 0 R >>\n");
    xref.append("startxref\n").append(pos).append("\n%%EOF\n");
    byte[] xrefB = xref.toString().getBytes(StandardCharsets.US_ASCII);
    byte[] out = new byte[header.length + (pos - header.length) + xrefB.length];
    System.arraycopy(header, 0, out, 0, header.length);
    int p = header.length;
    for (byte[] b : blocks) { System.arraycopy(b, 0, out, p, b.length); p += b.length; }
    System.arraycopy(xrefB, 0, out, p, xrefB.length);
    return out;
  }

  private static byte[] stream(String text) {
    StringBuilder sb = new StringBuilder();
    int y = 720;
    for (String line : text.split("\n")) {
      sb.append("1 0 0 1 72 ").append(y).append(" Tm (")
        .append(line.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)"))
        .append(") Tj\n");
      y -= 20;
    }
    byte[] data = sb.toString().getBytes(StandardCharsets.US_ASCII);
    String head = "<< /Length " + data.length + " >>\nstream\n";
    return concat(head.getBytes(StandardCharsets.US_ASCII), data,
        "\nendstream".getBytes(StandardCharsets.US_ASCII));
  }

  private static byte[] concat(byte[]... parts) {
    int n = 0;
    for (byte[] p : parts) n += p.length;
    byte[] out = new byte[n];
    int p = 0;
    for (byte[] part : parts) { System.arraycopy(part, 0, out, p, part.length); p += part.length; }
    return out;
  }
}
