import { useEffect, useRef, useState } from 'react';
import { api, type PlaylistItem, type Progress } from '../api';

interface Props {
  topicId: number;
  items: PlaylistItem[];
  saved: Progress | null;
  onProgress?: () => void;
}

/** Sequential player: auto-advances on `ended`, saves exact offset on pause. */
export default function Player({ topicId, items, saved, onProgress }: Props) {
  const audioRef = useRef<HTMLAudioElement>(null);
  const [index, setIndex] = useState(0);
  const [started, setStarted] = useState(false);
  const startedRef = useRef(false);

  useEffect(() => {
    if (saved && !startedRef.current) {
      const i = items.findIndex((it) => it.fragmentId === saved.fragmentId);
      if (i >= 0) setIndex(i);
    }
  }, [saved, items]);

  useEffect(() => {
    const el = audioRef.current;
    if (!el) return;
    if (saved && !startedRef.current && items[index]?.fragmentId === saved.fragmentId) {
      el.currentTime = saved.offsetSec;
    }
    if (startedRef.current) void el.play().catch(() => undefined);
  }, [index, saved, items]);

  const persist = async () => {
    const el = audioRef.current;
    const item = items[index];
    if (!el || !item) return;
    try {
      await api.saveProgress(topicId, item.fragmentId, el.currentTime);
      onProgress?.();
    } catch {
      /* offline progress is best-effort */
    }
  };

  const toggle = async () => {
    const el = audioRef.current;
    if (!el) return;
    if (el.paused) {
      startedRef.current = true;
      setStarted(true);
      await el.play().catch(() => undefined);
    } else {
      el.pause();
      await persist();
    }
  };

  const next = () => {
    if (index < items.length - 1) setIndex(index + 1);
  };

  const item = items[index];
  if (!item) return <p>Sin fragmentos.</p>;

  return (
    <div>
      <p>
        Fragmento {index + 1}/{items.length} · pág. {item.page}
        {item.durationSec ? ` · ${Math.round(item.durationSec)}s` : ' · generando audio…'}
      </p>
      <audio
        ref={audioRef}
        src={item.audioUrl}
        controls
        preload="auto"
        onEnded={next}
        onPause={persist}
      />
      <div>
        <button onClick={toggle}>{started ? 'Pausar' : 'Escuchar'}</button>
        <button onClick={next} disabled={index >= items.length - 1}>
          Siguiente
        </button>
        {index > 0 && <button onClick={() => setIndex(index - 1)}>Anterior</button>}
      </div>
    </div>
  );
}
