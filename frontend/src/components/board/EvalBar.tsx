'use client';

import styles from './EvalBar.module.css';

interface EvalBarProps {
  eval_score: number;    // in pawns, positive = white advantage
  evalType: 'cp' | 'mate';
  mateIn?: number;
}

export default function EvalBar({ eval_score, evalType, mateIn }: EvalBarProps) {
  // Convert eval to white percentage (50% = equal)
  let whitePercent: number;

  if (evalType === 'mate') {
    whitePercent = mateIn && mateIn > 0 ? 100 : 0;
  } else {
    // Sigmoid-like mapping: eval in pawns → percentage
    // At +3.0, white has ~95%. At -3.0, white has ~5%.
    const clampedEval = Math.max(-10, Math.min(10, eval_score));
    whitePercent = 50 + 50 * (2 / (1 + Math.exp(-0.7 * clampedEval)) - 1);
  }

  // Format display text
  let displayText: string;
  if (evalType === 'mate' && mateIn !== undefined && mateIn !== 0) {
    displayText = `M${Math.abs(mateIn)}`;
  } else {
    const absEval = Math.abs(eval_score);
    displayText = absEval >= 10 ? absEval.toFixed(0) : absEval.toFixed(1);
  }

  const isWhiteAdvantage = eval_score >= 0;

  return (
    <div className={styles.evalBar} id="eval-bar">
      <div
        className={styles.blackSection}
        style={{ height: `${100 - whitePercent}%` }}
      >
        {!isWhiteAdvantage && (
          <span className={styles.evalText}>{displayText}</span>
        )}
      </div>
      <div
        className={styles.whiteSection}
        style={{ height: `${whitePercent}%` }}
      >
        {isWhiteAdvantage && (
          <span className={styles.evalText}>{displayText}</span>
        )}
      </div>
    </div>
  );
}
