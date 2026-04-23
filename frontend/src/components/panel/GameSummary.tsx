'use client';

import { GameAnalysis, CLASSIFICATION_META, Classification } from '@/types';
import MoveIcon from '@/components/classification/MoveIcon';
import styles from './GameSummary.module.css';

interface GameSummaryProps {
  analysis: GameAnalysis;
}

const ORDER: Classification[] = [
  'BRILLIANT',
  'GREAT',
  'BEST',
  'EXCELLENT',
  'GOOD',
  'BOOK',
  'INACCURACY',
  'MISTAKE',
  'BLUNDER'
];

export default function GameSummary({ analysis }: GameSummaryProps) {
  // Determine if there is a clear winner for the white/outlined box styling
  const whiteWinner = analysis.result === '1-0';
  const blackWinner = analysis.result === '0-1';
  
  // If draw, both can be outlined
  const isDraw = analysis.result === '1/2-1/2';

  return (
    <div className={styles.summaryContainer}>
      {/* Accuracy Header Boxes */}
      <div className={styles.accuracySection}>
        <div className={`${styles.accuracyBox} ${whiteWinner || (!blackWinner && !isDraw) ? styles.boxWhite : styles.boxBlack}`}>
          <div className={styles.accuracyValue}>{analysis.white.accuracy.toFixed(1)}%</div>
          <div className={styles.accuracyLabel}>Accuracy</div>
        </div>
        
        <div className={styles.resultMiddle}>{analysis.result}</div>
        
        <div className={`${styles.accuracyBox} ${blackWinner ? styles.boxWhite : styles.boxBlack}`}>
          <div className={styles.accuracyValue}>{analysis.black.accuracy.toFixed(1)}%</div>
          <div className={styles.accuracyLabel}>Accuracy</div>
        </div>
      </div>

      {/* Classifications List */}
      <div className={styles.classificationList}>
        {ORDER.map(cls => {
          const meta = CLASSIFICATION_META[cls];
          const wCount = analysis.summary.whiteClassifications[cls.toLowerCase()] || 0;
          const bCount = analysis.summary.blackClassifications[cls.toLowerCase()] || 0;

          return (
            <div key={cls} className={styles.classRow}>
              <div className={styles.count} style={{ color: meta.color }}>{wCount}</div>
              <div className={styles.classMiddle}>
                <MoveIcon classification={cls} size="md" />
                <span className={styles.classLabel} style={{ color: meta.color }}>{meta.label}</span>
              </div>
              <div className={styles.count} style={{ color: meta.color }}>{bCount}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
