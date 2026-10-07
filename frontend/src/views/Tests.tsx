import { useEffect, useState } from 'react';
import { api, type GeneratedTest, type Grade, type HistoryEntry, type Topic } from '../api';

export default function Tests() {
  const [topics, setTopics] = useState<Topic[]>([]);
  const [topicId, setTopicId] = useState<number>(0);
  const [selected, setSelected] = useState<number[]>([]);
  const [mode, setMode] = useState<'single' | 'multi' | 'all'>('single');
  const [difficulty, setDifficulty] = useState('MEDIUM');
  const [test, setTest] = useState<GeneratedTest | null>(null);
  const [step, setStep] = useState(0);
  const [feedback, setFeedback] = useState<string>('');
  const [grade, setGrade] = useState<Grade | null>(null);
  const [history, setHistory] = useState<HistoryEntry[]>([]);
  const [globalHistory, setGlobalHistory] = useState<HistoryEntry[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api.topics().then(setTopics).catch(() => setError('Sin backend'));
    api.historyAll().then(setGlobalHistory).catch(() => undefined);
  }, []);

  useEffect(() => {
    if (topicId) api.history(topicId).then(setHistory).catch(() => undefined);
  }, [topicId]);

  const generate = async () => {
    setError('');
    setGrade(null);
    setStep(0);
    setFeedback('');
    try {
      if (mode === 'single') {
        setTest(await api.generateTest(topicId, 5, difficulty));
      } else if (mode === 'all') {
        setTest(await api.generateMixed([], 5, difficulty));
      } else {
        setTest(await api.generateMixed(selected, 5, difficulty));
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo generar');
    }
  };

  const answer = async (option: number) => {
    if (!test) return;
    const q = test.questions[step];
    const fb = await api.answer(test.id, q.id, option);
    setFeedback(
      fb.correct ? `Correcta. ${fb.explanation}` : `Fallaste. Correcta: ${q.options[fb.correctIndex]}. ${fb.explanation}`
    );
  };

  const next = async () => {
    if (!test) return;
    setFeedback('');
    if (step < test.questions.length - 1) {
      setStep(step + 1);
    } else {
      setGrade(await api.finishTest(test.id));
      if (topicId) setHistory(await api.history(topicId));
    }
  };

  const q = test?.questions[step];

  return (
    <div>
      <h2>Tests</h2>
      {error && <p role="alert">{error}</p>}
      <div role="radiogroup" aria-label="Alcance">
        <label>
          <input type="radio" checked={mode === 'single'} onChange={() => setMode('single')} /> Un
          tema
        </label>
        <label>
          <input type="radio" checked={mode === 'multi'} onChange={() => setMode('multi')} /> Varios
        </label>
        <label>
          <input type="radio" checked={mode === 'all'} onChange={() => setMode('all')} /> Todos
        </label>
      </div>
      {mode === 'single' && (
        <select value={topicId} onChange={(e) => setTopicId(Number(e.target.value))}>
          <option value={0}>Elige tema…</option>
          {topics.map((t) => (
            <option key={t.id} value={t.id}>
              {t.title}
            </option>
          ))}
        </select>
      )}
      {mode === 'multi' && (
        <fieldset>
          {topics.map((t) => (
            <label key={t.id}>
              <input
                type="checkbox"
                checked={selected.includes(t.id)}
                onChange={(e) =>
                  setSelected(
                    e.target.checked ? [...selected, t.id] : selected.filter((x) => x !== t.id)
                  )
                }
              />{' '}
              {t.title}
            </label>
          ))}
        </fieldset>
      )}
      <select value={difficulty} onChange={(e) => setDifficulty(e.target.value)}>
        <option value="EASY">Fácil</option>
        <option value="MEDIUM">Media</option>
        <option value="HARD">Difícil</option>
      </select>
      <button
        onClick={() => void generate()}
        disabled={mode === 'single' ? !topicId : mode === 'multi' && selected.length === 0}
      >
        Generar test
      </button>
      {q && !grade && (
        <div>
          <p>
            Pregunta {step + 1}/{test!.questions.length}
          </p>
          <p>{q.statement}</p>
          {q.options.map((op, i) => (
            <button key={i} onClick={() => void answer(i)}>
              {op}
            </button>
          ))}
          {feedback && (
            <div>
              <p>{feedback}</p>
              <button onClick={() => void next()}>Siguiente</button>
            </div>
          )}
        </div>
      )}
      {grade && (
        <p>
          Nota: {grade.score} ({grade.correctCount} aciertos, {grade.wrongCount} fallos)
        </p>
      )}
      <h3>Historial</h3>
      <ul>
        {history.map((h) => (
          <li key={h.id}>
            {h.difficulty} — {h.score ?? 'pendiente'} ({h.createdAt.slice(0, 10)})
          </li>
        ))}
      </ul>
      <h3>Todos los intentos</h3>
      <ul>
        {globalHistory.map((h) => (
          <li key={h.id}>
            {h.alcance ?? h.difficulty} — {h.score ?? 'pendiente'} (
            {h.createdAt.slice(0, 10)})
          </li>
        ))}
      </ul>
    </div>
  );
}
