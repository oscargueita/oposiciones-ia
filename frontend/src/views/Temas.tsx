import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, type Topic } from '../api';

export default function Temas() {
  const [topics, setTopics] = useState<Topic[]>([]);
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(false);

  const load = () =>
    api
      .topics()
      .then(setTopics)
      .catch(() => setError('No hay conexión con el backend. Arráncalo con ./mvnw spring-boot:run'));

  useEffect(() => {
    void load();
  }, []);

  const upload = async (files: FileList | null) => {
    if (!files || files.length === 0) return;
    setUploading(true);
    setError('');
    try {
      await api.upload(files);
      await load();
    } catch {
      setError('No se pudo subir el PDF');
    } finally {
      setUploading(false);
    }
  };

  const remove = async (id: number) => {
    if (!confirm('¿Borrar el tema y todo su contenido?')) return;
    await api.deleteTopic(id);
    await load();
  };

  return (
    <div>
      <h2>Temas ({topics.length})</h2>
      {error && <p role="alert">{error}</p>}
      <label>
        Subir PDFs:{' '}
        <input
          type="file"
          accept="application/pdf"
          multiple
          disabled={uploading}
          onChange={(e) => void upload(e.target.files)}
        />
      </label>
      <ul>
        {topics.map((t) => (
          <li key={t.id}>
            <Link to={`/temas/${t.id}`}>{t.title}</Link> — {t.status} · {t.fragmentCount}{' '}
            fragmentos
            {t.status === 'READY' && (
              <>
                {' '}
                <Link to={`/material?tema=${t.id}`}>chuleta/mapa</Link>
              </>
            )}
            <button onClick={() => void remove(t.id)}>Borrar</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
