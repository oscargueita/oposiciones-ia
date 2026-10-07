import { render, screen, fireEvent } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import Player from './Player';
import { api } from '../api';

vi.mock('../api', () => ({
  api: { saveProgress: vi.fn().mockResolvedValue({}) },
}));

const items = [
  { fragmentId: 1, sequence: 0, page: 1, durationSec: 10, audioUrl: '/a/1' },
  { fragmentId: 2, sequence: 1, page: 2, durationSec: 10, audioUrl: '/a/2' },
];

describe('Player', () => {
  it('advances automatically on ended and saves progress on pause', async () => {
    HTMLMediaElement.prototype.play = vi.fn().mockResolvedValue(undefined);
    render(<Player topicId={7} items={items} saved={null} />);
    expect(screen.getByText(/Fragmento 1\/2/)).toBeInTheDocument();
    const audio = document.querySelector('audio')!;
    fireEvent.ended(audio);
    expect(await screen.findByText(/Fragmento 2\/2/, { selector: 'p' })).toBeInTheDocument();
    fireEvent.pause(audio);
    expect(api.saveProgress).toHaveBeenCalledWith(7, 2, expect.any(Number));
  });

  it('resumes from saved offset', () => {
    HTMLMediaElement.prototype.play = vi.fn().mockResolvedValue(undefined);
    render(
      <Player
        topicId={7}
        items={items}
        saved={{ topicId: 7, fragmentId: 2, offsetSec: 4 }}
      />
    );
    expect(screen.getByText(/Fragmento 2\/2/, { selector: 'p' })).toBeInTheDocument();
  });
});
