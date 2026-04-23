import { GameAnalysis } from '@/types';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export async function analyzeGame(pgn: string, depth: number = 15): Promise<GameAnalysis> {
  const response = await fetch(`${API_BASE}/api/analyze`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ pgn, depth }),
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`Analysis failed (${response.status}): ${text}`);
  }

  return response.json();
}
