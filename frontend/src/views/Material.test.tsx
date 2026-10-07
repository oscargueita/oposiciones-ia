import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import Material from './Material';
import { api } from '../api';

vi.mock('../api', () => ({
  api: {
    topics: vi.fn().mockResolvedValue([{ id: 2, title: 'T', sourceName: 't', pageCount: 1, status: 'READY', errorMessage: null, fragmentCount: 1 }]),
    cheatsheet: vi.fn().mockResolvedValue('## Chuleta\n- punto (tema, pág. 1)'),
    mindmap: vi.fn().mockResolvedValue('mindmap\n  Raíz\n    A\n    B\n    C\n    D\n    E\n    F\n    G\n    H'),
  },
}));

vi.mock('mermaid', () => ({
  default: {
    initialize: vi.fn(),
    render: vi.fn().mockResolvedValue({ svg: '<svg>mapa</svg>' }),
  },
}));

describe('Material', () => {
  it('renders markdown cheatsheet and diagram', async () => {
    render(<Material />);
    expect(await screen.findByRole('option', { name: 'T' })).toBeInTheDocument();
    fireEvent.change(screen.getByRole('combobox'), { target: { value: '2' } });
    await waitFor(() => expect(screen.getByText('Chuleta')).toBeInTheDocument());
    expect(await screen.findByText('punto (tema, pág. 1)')).toBeInTheDocument();
    fireEvent.click(screen.getByText('Mapa'));
    await waitFor(() => expect(api.mindmap).toHaveBeenCalledWith(2));
  });
});
