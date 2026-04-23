// Types matching the Java backend DTOs

export interface AnalyzedMove {
  moveNumber: number;
  color: 'white' | 'black';
  san: string;
  uci: string;
  fen: string;
  eval: number;
  evalType: 'cp' | 'mate';
  mateIn: number;
  bestMove: string;
  bestLine: string[];
  cpLoss: number;
  classification: Classification;
  bookMove: boolean;
}

export type Classification =
  | 'BRILLIANT'
  | 'GREAT'
  | 'BEST'
  | 'EXCELLENT'
  | 'GOOD'
  | 'INACCURACY'
  | 'MISTAKE'
  | 'BLUNDER'
  | 'BOOK';

export interface PlayerInfo {
  name: string;
  rating: string;
  accuracy: number;
}

export interface AnalysisSummary {
  totalMoves: number;
  whiteClassifications: Record<string, number>;
  blackClassifications: Record<string, number>;
}

export interface GameAnalysis {
  white: PlayerInfo;
  black: PlayerInfo;
  result: string;
  opening: string;
  startingFen: string;
  moves: AnalyzedMove[];
  summary: AnalysisSummary;
}

// Classification metadata for UI rendering
export const CLASSIFICATION_META: Record<Classification, {
  label: string;
  symbol: string;
  color: string;
  bgColor: string;
}> = {
  BRILLIANT: { label: 'Brilliant',  symbol: '!!', color: '#1baba0', bgColor: 'rgba(27,171,160,0.15)' },
  GREAT:     { label: 'Great Move', symbol: '!',  color: '#5c8bb0', bgColor: 'rgba(92,139,176,0.15)' },
  BEST:      { label: 'Best Move',  symbol: '★',  color: '#82b74b', bgColor: 'rgba(130,183,75,0.15)' },
  EXCELLENT: { label: 'Excellent',  symbol: '👍', color: '#96bc4b', bgColor: 'rgba(150,188,75,0.15)' },
  GOOD:      { label: 'Good',       symbol: '✔',  color: '#979797', bgColor: 'rgba(151,151,151,0.15)' },
  INACCURACY:{ label: 'Inaccuracy', symbol: '?!', color: '#e4ab22', bgColor: 'rgba(228,171,34,0.15)' },
  MISTAKE:   { label: 'Mistake',    symbol: '?',  color: '#f0802f', bgColor: 'rgba(240,128,47,0.15)' },
  BLUNDER:   { label: 'Blunder',    symbol: '??', color: '#b33430', bgColor: 'rgba(179,52,48,0.15)' },
  BOOK:      { label: 'Book',       symbol: '📖', color: '#a88865', bgColor: 'rgba(168,136,101,0.15)' },
};
