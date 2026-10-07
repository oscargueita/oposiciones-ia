#!/usr/bin/env python3
"""Descarga los 33 temas TAI de OPOAGE (lectura abierta, uso personal),
los convierte a 1 PDF por tema y los deja en data/temario/OPOAGE/.
Uso: python3 scripts/fetch_opoage.py
"""
import html
import os
import re
import ssl
import time
import urllib.request

try:
    import certifi
    CTX = ssl.create_default_context(cafile=certifi.where())
except ImportError:
    CTX = ssl.create_default_context()

BASE = "https://opoage.es/oposicion/tecnicos-auxiliares-informatica-estado/"
OUTDIR = "/Users/OscarG/repositories_training/opositions/data/temario/OPOAGE"
UA = {"User-Agent": "oposiciones-ia/1.0 (estudio personal)"}


def get(url: str) -> str:
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=60, context=CTX) as r:
        return r.read().decode("utf-8", errors="replace")


def to_latin1(s: str) -> str:
    repl = {"—": "-", "–": "-", "→": "->", "“": '"', "”": '"', "‘": "'",
            "’": "'", "«": '"', "»": '"', "·": "-", "₂": "2", "⊕": "(+)",
            "…": "...", "€": "EUR", "✓": "v", "✗": "x", "\u00a0": " "}
    for a, b in repl.items():
        s = s.replace(a, b)
    return s.encode("latin-1", errors="replace").decode("latin-1")


def article_text(page_html: str) -> tuple[str, str]:
    m = re.search(r'<article class="tema-publico-content markdown-content">(.*?)</article>',
                  page_html, flags=re.S)
    if not m:
        raise ValueError("sin article")
    art = m.group(1)
    title = ""
    mt = re.search(r"<h1[^>]*>(.*?)</h1>", art, flags=re.S)
    if mt:
        title = re.sub(r"\s+", " ", re.sub(r"<[^>]+>", "", mt.group(1))).strip()
    # headings -> lines, rest -> paragraphs
    art = re.sub(r"</(h1|h2|h3|p|li|tr)>", "\n", art)
    art = re.sub(r"<li[^>]*>", "\n- ", art)
    art = re.sub(r"<[^>]+>", " ", art)
    text = html.unescape(re.sub(r"[ \t]+", " ", art))
    lines = [ln.strip() for ln in text.split("\n")]
    lines = [ln for ln in lines if ln]
    return title, "\n\n".join(lines)


def xml_escape(s: str) -> str:
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def to_pdf(num: int, title: str, body: str, path: str):
    from reportlab.lib.pagesizes import A4
    from reportlab.lib.styles import ParagraphStyle
    from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
    h1 = ParagraphStyle("h1", fontName="Helvetica-Bold", fontSize=16, spaceAfter=12)
    h2 = ParagraphStyle("h2", fontName="Helvetica-Bold", fontSize=12, spaceAfter=6)
    normal = ParagraphStyle("normal", fontName="Helvetica", fontSize=10, leading=14)
    doc = SimpleDocTemplate(path, pagesize=A4)
    story = [Paragraph(xml_escape(to_latin1(f"TEMA {num}. {title}")), h1), Spacer(1, 6)]
    for para in body.split("\n\n"):
        para = to_latin1(para)
        if len(para) < 120 and (para.isupper() or para.endswith(":")):
            story.append(Paragraph(xml_escape(para), h2))
        else:
            story.append(Paragraph(xml_escape(para), normal))
    doc.build(story)


def main():
    os.makedirs(OUTDIR, exist_ok=True)
    index = get(BASE)
    urls = sorted(set(re.findall(
        r'href="(https://opoage\.es/oposicion/tecnicos-auxiliares-informatica-estado/tema-\d+-[^"]*)"',
        index)))
    print("temas:", len(urls))
    for i, url in enumerate(urls, 1):
        m = re.search(r"/tema-(\d+)-", url)
        num = int(m.group(1))
        dest = f"{OUTDIR}/OPOAGE-{num:02d}.pdf"
        if os.path.exists(dest):
            print(f"[{i}/{len(urls)}] TEMA {num}: ya existe, salto")
            continue
        title, body = article_text(get(url))
        # evita falsos multi-tema: solo el título declara número de tema
        body = re.sub(r"(?m)^\s*[Tt][Ee][Mm][Aa]\s+\d+\b.*$", "", body)
        to_pdf(num, title or f"Tema {num}", body, dest)
        print(f"[{i}/{len(urls)}] TEMA {num}: {title[:60]} ({os.path.getsize(dest)//1024} KB)")
        time.sleep(1)


if __name__ == "__main__":
    main()
