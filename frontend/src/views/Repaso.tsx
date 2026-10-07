import { useState } from 'react';
import { api, type Candidate } from '../api';

export default function Repaso() {
  const [q, setQ] = useState('');
  const [results, setResults] = useState<Candidate[]>([]);
  const [empty, setEmpty] = useState(false);
  const [error, setError] = useState('');
  const [playing, setPlaying] = useState<number | null>(null);

  const search = async () => {
    setError('');
    setEmpty(false);
    try {
      const r = await api.review(q);
      setResults(r);
      setEmpty(r.length === 0);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Error de búsqueda');
    }
  };

  return (
    <div>
      <h2>Repaso por palabra clave</h2>
      <input
        value={q}
        onChange={(e) => setQ(e.target.value)}
        placeholder="p. ej. recurso de alzada"
        onKeyDown={(e) => e.key === 'Enter' && void search()}
      />
      <button onClick={() => void search()}>Repasar</button>
      {error && <p role="alert">{error}</p>}
      {empty && <p>Sin resultados en el temario. Prueba con otra palabra.</p>}
      <ol>
        {results.map((c) => (
          <li key={c.fragmentId}>
            Tema {c.topicId}, pág. {c.page} ({Math.round(c.score * 100)}%)
            <p>{c.text.slice(0, 300)}…</p>
            <button onClick={() => setPlaying(playing === c.fragmentId ? null : c.fragmentId)}>
              {playing === c.fragmentId ? 'Parar' : 'Escuchar'}
            </button>
            {playing === c.fragmentId && <audio src={c.audioUrl} controls autoPlay />}
          </li>
        ))}
      </ol>
    </div>
  );
}
