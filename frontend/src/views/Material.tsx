import { useEffect, useState } from 'react';
import ReactMarkdown from 'react-markdown';
import mermaid from 'mermaid';
import { api, type Topic } from '../api';

export default function Material() {
  const [topics, setTopics] = useState<Topic[]>([]);
  const [topicId, setTopicId] = useState<number>(0);
  const [tab, setTab] = useState<'chuleta' | 'mapa'>('chuleta');
  const [markdown, setMarkdown] = useState('');
  const [diagram, setDiagram] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    api.topics().then(setTopics).catch(() => setError('Sin backend'));
    mermaid.initialize({ startOnLoad: false });
  }, []);

  useEffect(() => {
    if (!topicId) return;
    setError('');
    api
      .cheatsheet(topicId)
      .then(setMarkdown)
      .catch(() => setError('Sin chuleta (tema sin contenido)'));
    api
      .mindmap(topicId)
      .then(setDiagram)
      .catch(() => undefined);
  }, [topicId]);

  useEffect(() => {
    if (tab === 'mapa' && diagram) {
      mermaid.render('mapa-svg', diagram).then(({ svg }) => {
        document.getElementById('mapa-svg')!.innerHTML = svg;
      });
    }
  }, [tab, diagram]);

  return (
    <div>
      <h2>Chuleta y mapa</h2>
      {error && <p role="alert">{error}</p>}
      <select value={topicId} onChange={(e) => setTopicId(Number(e.target.value))}>
        <option value={0}>Elige tema…</option>
        {topics.map((t) => (
          <option key={t.id} value={t.id}>
            {t.title}
          </option>
        ))}
      </select>
      <div>
        <button onClick={() => setTab('chuleta')}>Chuleta</button>
        <button onClick={() => setTab('mapa')}>Mapa</button>
      </div>
      {tab === 'chuleta' ? <ReactMarkdown>{markdown}</ReactMarkdown> : <div id="mapa-svg" />}
    </div>
  );
}
