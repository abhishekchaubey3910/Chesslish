'use client';

import { Chessboard } from 'react-chessboard';
import { AnalyzedMove, CLASSIFICATION_META, Classification } from '@/types';
import styles from './ChessBoard.module.css';

interface ChessBoardProps {
  position: string;  // FEN
  currentMove?: AnalyzedMove;
  showBestMove?: boolean;
  onPieceDrop?: (sourceSquare: string, targetSquare: string) => boolean;
  boardSize?: number;
}

export default function ChessBoardComponent({ position, currentMove, showBestMove, onPieceDrop, boardSize = 560 }: ChessBoardProps) {
  // Highlight last move squares
  const squareStyles: Record<string, React.CSSProperties> = {};
  let badgeStyle: React.CSSProperties | undefined = undefined;
  let badgeSymbol = '';

  if (currentMove) {
    const from = currentMove.uci.substring(0, 2);
    const to = currentMove.uci.substring(2, 4);
    
    // Explicitly cast to Classification to fix index signature errors
    const clsKey = currentMove.classification as Classification;
    const meta = CLASSIFICATION_META[clsKey];

    if (meta) {
      squareStyles[from] = { backgroundColor: `${meta.color}33` };
      squareStyles[to] = { backgroundColor: `${meta.color}55` };

      // Calculate absolute position for the badge over the 'to' square
      const sqSize = boardSize / 8;
      const file = to.charCodeAt(0) - 'a'.charCodeAt(0);
      const rank = parseInt(to[1], 10);
      const x = file * sqSize;
      const y = (8 - rank) * sqSize;

      badgeStyle = {
        position: 'absolute',
        top: `${y - 10}px`,
        left: `${x + sqSize - 22}px`,
        zIndex: 10,
        backgroundColor: meta.color,
        color: '#fff',
        width: '22px',
        height: '22px',
        borderRadius: '50%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontWeight: '900',
        fontSize: '14px',
        border: `2px solid #fff`,
        boxShadow: '0 2px 5px rgba(0,0,0,0.5)',
        pointerEvents: 'none',
        textShadow: 'none'
      };
      badgeSymbol = meta.symbol;
    }
  }

  // Show best move arrow
  const arrows: { startSquare: string; endSquare: string; color: string }[] = [];
  if (currentMove && currentMove.bestMove) {
    const isMistake = !['BEST', 'EXCELLENT', 'BOOK'].includes(currentMove.classification);
    if (showBestMove || isMistake) {
      const bestFrom = currentMove.bestMove.substring(0, 2);
      const bestTo = currentMove.bestMove.substring(2, 4);
      arrows.push({ startSquare: bestFrom, endSquare: bestTo, color: 'rgba(155, 199, 0, 0.7)' });
    }
  }

  return (
    <div className={styles.boardWrapper} style={{ position: 'relative' }}>
      <Chessboard
        options={{
          position: position,
          boardStyle: {
            borderRadius: '4px',
            boxShadow: '0 4px 20px rgba(0,0,0,0.4)',
          },
          darkSquareStyle: { backgroundColor: '#769656' },
          lightSquareStyle: { backgroundColor: '#eeeed2' },
          squareStyles: squareStyles,
          arrows: arrows,
          allowDragging: !!onPieceDrop,
          onPieceDrop: onPieceDrop
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            ? (args: any) => onPieceDrop(args.sourceSquare, args.targetSquare)
            : undefined,
          animationDurationInMs: 200,
          showNotation: true,
        }}
      />
      {badgeStyle && badgeSymbol && (
        <div style={badgeStyle}>
          {badgeSymbol}
        </div>
      )}
    </div>
  );
}
