import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import Tests from './Tests';
import { api } from '../api';

vi.mock('../api', () => ({
  api: {
    topics: vi.fn().mockResolvedValue([{ id: 2, title: 'T', sourceName: 't', pageCount: 1, status: 'READY', errorMessage: null, fragmentCount: 1 }]),
    generateTest: vi.fn().mockResolvedValue({
      id: 9, topicId: 2, difficulty: 'MEDIUM', questionCount: 1, status: 'PENDING', notice: null,
      questions: [{ id: 5, sequence: 0, statement: '¿Plazo?', options: ['1 mes', '2 meses'], citedTopicId: 2, citedFragmentId: 1, citedPage: 1 }],
    }),
    answer: vi.fn().mockResolvedValue({ correct: true, correctIndex: 0, explanation: 'Porque sí', citedTopicId: 2, citedFragmentId: 1, citedPage: 1 }),
    finishTest: vi.fn().mockResolvedValue({ score: 10, correctCount: 1, wrongCount: 0, details: [] }),
    history: vi.fn().mockResolvedValue([]),
  },
}));

describe('Tests', () => {
  it('generates, answers with feedback and grades', async () => {
    render(<Tests />);
    expect(await screen.findByRole('option', { name: 'T' })).toBeInTheDocument();
    fireEvent.change(screen.getAllByRole('combobox')[0], { target: { value: '2' } });
    fireEvent.click(screen.getByText('Generar test'));
    expect(await screen.findByText('¿Plazo?')).toBeInTheDocument();
    fireEvent.click(screen.getByText('1 mes'));
    expect(await screen.findByText(/Correcta/)).toBeInTheDocument();
    fireEvent.click(screen.getByText('Siguiente'));
    await waitFor(() => expect(screen.getByText(/Nota: 10/)).toBeInTheDocument());
    expect(api.finishTest).toHaveBeenCalledWith(9);
  });
});
