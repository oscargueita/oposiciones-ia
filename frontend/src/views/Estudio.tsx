import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api, type PlaylistItem, type Progress } from '../api';
import Player from '../components/Player';

export default function Estudio() {
  const { id } = useParams();
  const topicId = Number(id);
  const [items, setItems] = useState<PlaylistItem[]>([]);
  const [saved, setSaved] = useState<Progress | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .playlist(topicId)
      .then(setItems)
      .catch(() => setError('Tema sin contenido narrable o audios en curso'));
    api
      .progress(topicId)
      .then(setSaved)
      .catch(() => setSaved(null));
  }, [topicId]);

  return (
    <div>
      <h2>Estudio del tema {topicId}</h2>
      {error && <p role="alert">{error}</p>}
      {saved && (
        <p>
          Tienes progreso guardado: fragmento en pág. {items.find((i) => i.fragmentId === saved.fragmentId)?.page ?? '?'} +{' '}
          {Math.round(saved.offsetSec)}s — pulsa Escuchar para continuar.
        </p>
      )}
      {items.length > 0 && <Player topicId={topicId} items={items} saved={saved} />}
    </div>
  );
}
