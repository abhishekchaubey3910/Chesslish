'use client';

import { useRef, useEffect } from 'react';
import { AnalyzedMove, CLASSIFICATION_META, Classification } from '@/types';
import MoveIcon from '@/components/classification/MoveIcon';
import styles from './MoveList.module.css';

interface MoveListProps {
  moves: AnalyzedMove[];
  currentIndex: number;
  onMoveClick: (index: number) => void;
}

export default function MoveList({ moves, currentIndex, onMoveClick }: MoveListProps) {
  const activeRef = useRef<HTMLButtonElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to active move
  useEffect(() => {
    if (activeRef.current && containerRef.current) {
      const container = containerRef.current;
      const element = activeRef.current;
      const containerRect = container.getBoundingClientRect();
      const elementRect = element.getBoundingClientRect();

      if (elementRect.top < containerRect.top || elementRect.bottom > containerRect.bottom) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }
  }, [currentIndex]);

  // Group moves into pairs (white + black)
  const moveRows: { moveNum: number; white?: { move: AnalyzedMove; idx: number }; black?: { move: AnalyzedMove; idx: number } }[] = [];

  for (let i = 0; i < moves.length; i++) {
    const move = moves[i];
    if (move.color === 'white') {
      moveRows.push({
        moveNum: move.moveNumber,
        white: { move, idx: i },
      });
    } else {
      const lastRow = moveRows[moveRows.length - 1];
      if (lastRow && lastRow.moveNum === move.moveNumber && !lastRow.black) {
        lastRow.black = { move, idx: i };
      } else {
        moveRows.push({
          moveNum: move.moveNumber,
          black: { move, idx: i },
        });
      }
    }
  }

  return (
    <div className={styles.moveList} ref={containerRef} id="move-list">
      {moveRows.map((row, rowIdx) => (
        <div key={rowIdx} className={styles.moveRow}>
          <span className={styles.moveNumber}>{row.moveNum}.</span>

          {row.white ? (
            <button
              ref={row.white.idx === currentIndex ? activeRef : undefined}
              className={`${styles.moveBtn} ${row.white.idx === currentIndex ? styles.active : ''}`}
              onClick={() => onMoveClick(row.white!.idx)}
              style={row.white.idx === currentIndex ? {
                backgroundColor: CLASSIFICATION_META[row.white.move.classification as Classification].bgColor,
                borderColor: CLASSIFICATION_META[row.white.move.classification as Classification].color + '44',
              } : undefined}
            >
              <span className={styles.san}>{row.white.move.san}</span>
              <MoveIcon classification={row.white.move.classification} size="sm" />
            </button>
          ) : (
            <span className={styles.emptyMove}>...</span>
          )}

          {row.black ? (
            <button
              ref={row.black.idx === currentIndex ? activeRef : undefined}
              className={`${styles.moveBtn} ${row.black.idx === currentIndex ? styles.active : ''}`}
              onClick={() => onMoveClick(row.black!.idx)}
              style={row.black.idx === currentIndex ? {
                backgroundColor: CLASSIFICATION_META[row.black.move.classification as Classification].bgColor,
                borderColor: CLASSIFICATION_META[row.black.move.classification as Classification].color + '44',
              } : undefined}
            >
              <span className={styles.san}>{row.black.move.san}</span>
              <MoveIcon classification={row.black.move.classification} size="sm" />
            </button>
          ) : (
            <span className={styles.emptyMove}></span>
          )}
        </div>
      ))}
    </div>
  );
}
