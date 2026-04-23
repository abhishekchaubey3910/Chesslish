'use client';

import { PlayerInfo, CLASSIFICATION_META, Classification } from '@/types';
import styles from './GameInfo.module.css';

interface GameInfoProps {
  player: PlayerInfo;
  color: 'white' | 'black';
  classifications?: Record<string, number>;
}

export default function GameInfo({ player, color, classifications }: GameInfoProps) {
  const icon = color === 'white' ? '□' : '■';

  return (
    <div className={styles.gameInfo}>
      <div className={styles.playerRow}>
        <span className={`${styles.playerIcon} ${styles[color]}`}>{icon}</span>
        <span className={styles.playerName}>{player.name}</span>
        {player.rating && player.rating !== '?' && (
          <span className={styles.playerRating}>({player.rating})</span>
        )}
        <span className={styles.accuracy}>{player.accuracy.toFixed(1)}%</span>
      </div>

      {classifications && (
        <div className={styles.classRow}>
          {Object.entries(classifications).map(([key, count]) => {
            if (count === 0 || key === 'book') return null;
            const cls = key.toUpperCase() as Classification;
            const meta = CLASSIFICATION_META[cls];
            if (!meta) return null;
            return (
              <span key={key} className={styles.classBadge} style={{ color: meta.color }}>
                {meta.symbol} {count}
              </span>
            );
          })}
        </div>
      )}
    </div>
  );
}
