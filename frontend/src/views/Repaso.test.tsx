import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import Repaso from './Repaso';
import { api } from '../api';

vi.mock('../api', () => ({
  api: { review: vi.fn() },
}));

describe('Repaso', () => {
  it('renders candidates and empty state', async () => {
    vi.mocked(api.review).mockResolvedValueOnce([
      { fragmentId: 1, topicId: 2, page: 3, text: 'texto del concepto', score: 0.9, audioUrl: '/a/1' },
    ]);
    render(<Repaso />);
    fireEvent.change(screen.getByPlaceholderText(/recurso de alzada/), { target: { value: 'plazo' } });
    fireEvent.click(screen.getByText('Repasar'));
    expect(await screen.findByText(/Tema 2, pág. 3/)).toBeInTheDocument();
    vi.mocked(api.review).mockResolvedValueOnce([]);
    fireEvent.click(screen.getByText('Repasar'));
    await waitFor(() => expect(screen.getByText(/Sin resultados/)).toBeInTheDocument());
  });
});
