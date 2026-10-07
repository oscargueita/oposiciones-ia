/** Typed client for the local API (same origin in production). */

export interface Topic {
  id: number;
  title: string;
  sourceName: string;
  pageCount: number;
  status: string;
  errorMessage: string | null;
  fragmentCount: number;
}

export interface PlaylistItem {
  fragmentId: number;
  sequence: number;
  page: number;
  durationSec: number | null;
  audioUrl: string;
}

export interface Progress {
  topicId: number;
  fragmentId: number;
  offsetSec: number;
}

export interface Candidate {
  fragmentId: number;
  topicId: number;
  page: number;
  text: string;
  score: number;
  audioUrl: string;
}

export interface Question {
  id: number;
  sequence: number;
  statement: string;
  options: string[];
  citedTopicId: number;
  citedFragmentId: number;
  citedPage: number;
}

export interface GeneratedTest {
  id: number;
  topicId: number;
  difficulty: string;
  questionCount: number;
  status: string;
  notice: string | null;
  questions: Question[];
}

export interface Feedback {
  correct: boolean;
  correctIndex: number;
  explanation: string;
  citedTopicId: number;
  citedFragmentId: number;
  citedPage: number;
}

export interface Grade {
  score: number;
  correctCount: number;
  wrongCount: number;
  details: GradeDetail[];
}

export interface GradeDetail {
  questionId: number;
  statement: string;
  selected: number | null;
  correctIndex: number;
  correct: boolean;
  explanation: string;
}

export interface HistoryEntry {
  id: number;
  topicId: number | null;
  alcance: string;
  difficulty: string;
  questionCount: number;
  status: string;
  score: number | null;
  createdAt: string;
}

async function req<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, init);
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error((body as { error?: string }).error ?? `Error ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export const api = {
  topics: () => req<Topic[]>("/api/v1/temas"),
  upload: (files: FileList | File[]) => {
    const fd = new FormData();
    Array.from(files).forEach((f) => fd.append("files", f));
    return fetch("/api/v1/temas", { method: "POST", body: fd }).then(async (res) => {
      if (!res.ok) throw new Error("No se pudo subir");
      return res.json() as Promise<Topic[]>;
    });
  },
  deleteTopic: (id: number) =>
    fetch(`/api/v1/temas/${id}`, { method: "DELETE" }).then((res) => {
      if (!res.ok) throw new Error("No se pudo borrar");
    }),
  playlist: (id: number) => req<PlaylistItem[]>(`/api/v1/temas/${id}/narracion`),
  progress: (id: number) => req<Progress>(`/api/v1/temas/${id}/progreso`),
  saveProgress: (id: number, fragmentId: number, offsetSec: number) =>
    req<Progress>(`/api/v1/temas/${id}/progreso`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ fragmentId, offsetSec }),
    }),
  review: (q: string, topicId?: number) =>
    req<Candidate[]>(
      `/api/v1/repasar?q=${encodeURIComponent(q)}${topicId ? `&topicId=${topicId}` : ""}`
    ),
  generateTest: (topicId: number, n: number, difficulty: string) =>
    req<GeneratedTest>(`/api/v1/temas/${topicId}/tests?n=${n}&difficulty=${difficulty}`, {
      method: "POST",
    }),
  generateMixed: (topicIds: number[], n: number, difficulty: string) =>
    req<GeneratedTest>(
      `/api/v1/tests?n=${n}&difficulty=${difficulty}${topicIds.length ? `&topicIds=${topicIds.join(",")}` : ""}`,
      { method: "POST" }
    ),
  answer: (testId: number, questionId: number, option: number) =>
    req<Feedback>(`/api/v1/tests/${testId}/responder`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ questionId, option }),
    }),
  finishTest: (testId: number) =>
    req<Grade>(`/api/v1/tests/${testId}/finalizar`, { method: "POST" }),
  history: (topicId: number) => req<HistoryEntry[]>(`/api/v1/temas/${topicId}/tests`),
  historyAll: () => req<HistoryEntry[]>('/api/v1/tests'),
  cheatsheet: (id: number) =>
    fetch(`/api/v1/temas/${id}/chuleta`).then(async (res) => {
      if (!res.ok) throw new Error("Sin chuleta");
      return res.text();
    }),
  mindmap: (id: number) =>
    fetch(`/api/v1/temas/${id}/mapa`).then(async (res) => {
      if (!res.ok) throw new Error("Sin mapa");
      return res.text();
    }),
};
