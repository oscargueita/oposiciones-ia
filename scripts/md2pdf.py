#!/usr/bin/env python3
"""Convierte apuntes markdown (formato TAI-2xx) a PDF con 1 TEMA por fichero."""
import re
import sys
from fpdf import FPDF

FONT = "/Library/Fonts/Arial Unicode.ttf"


class ApuntesPDF(FPDF):
    def header(self):
        pass

    def footer(self):
        self.set_y(-15)
        self.set_font("arial", "I", 8)
        self.cell(0, 10, f"{self.page_no()}", align="C")


def wrap_long_tokens(line: str, limit: int = 50) -> str:
    out = []
    for tok in line.split(" "):
        while len(tok) > limit:
            out.append(tok[:limit])
            tok = tok[limit:]
        out.append(tok)
    return " ".join(out)


def render(md_path: str, pdf_path: str):
    pdf = ApuntesPDF()
    pdf.set_auto_page_break(True, margin=20)
    pdf.add_font("arial", "", FONT)
    pdf.add_font("arial", "B", FONT)
    pdf.add_page()
    in_table = False
    for raw in open(md_path, encoding="utf-8"):
        line = raw.rstrip("\n")
        if not line.strip():
            pdf.ln(3)
            in_table = False
            continue
        if line.startswith("# "):
            pdf.set_font("arial", "B", 18)
            pdf.multi_cell(0, 9, line[2:].strip())
            pdf.ln(2)
        elif line.startswith("## "):
            pdf.set_font("arial", "B", 13)
            pdf.set_text_color(30, 60, 120)
            pdf.multi_cell(0, 8, line[3:].strip())
            pdf.set_text_color(0)
            pdf.ln(1)
        elif line.startswith("|"):
            cells = [c.strip() for c in line.strip().strip("|").split("|")]
            if set(cells) <= {"", "-", "---", ":---", "---:"}:
                continue
            pdf.set_font("arial", "", 9)
            pdf.multi_cell(0, 5, " | ".join(cells))
        elif re.match(r"^(\d+\.|-|\*) ", line.strip()):
            pdf.set_font("arial", "", 10)
            pdf.multi_cell(0, 5.5, line.strip())
        else:
            pdf.set_font("arial", "", 10)
            pdf.multi_cell(0, 5.5, wrap_long_tokens(line.strip()))
    pdf.output(pdf_path)
    print("PDF:", pdf_path)


if __name__ == "__main__":
    render(sys.argv[1], sys.argv[2])
