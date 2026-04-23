'use client';

import { useState } from 'react';
import styles from './PgnInput.module.css';

interface PgnInputProps {
  onSubmit: (pgn: string, depth: number) => void;
  isLoading: boolean;
}

const SAMPLE_PGN = `[Event "Example Game"]
[Site "Chess.com"]
[Date "2024.01.15"]
[White "Player1"]
[Black "Player2"]
[Result "1-0"]
[WhiteElo "1500"]
[BlackElo "1400"]

1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Nb8 10. d4 Nbd7 1-0`;

export default function PgnInput({ onSubmit, isLoading }: PgnInputProps) {
  const [pgn, setPgn] = useState('');
  const [depth, setDepth] = useState(15);

  const handleSubmit = () => {
    const trimmed = pgn.trim();
    if (trimmed) onSubmit(trimmed, depth);
  };

  return (
    <div className={styles.pgnInput} id="pgn-input">
      <div className={styles.header}>
        <span className={styles.title}>📋 Paste PGN</span>
        <button className={styles.sampleBtn} onClick={() => setPgn(SAMPLE_PGN)} disabled={isLoading}>
          Load Sample
        </button>
      </div>
      <textarea
        className={styles.textarea}
        value={pgn}
        onChange={(e) => setPgn(e.target.value)}
        placeholder={'Paste your PGN here...\n\n[Event "My Game"]\n[White "Player1"]\n[Black "Player2"]\n\n1. e4 e5 2. Nf3 Nc6 ...'}
        disabled={isLoading}
        rows={6}
        id="pgn-textarea"
      />

      <div className={styles.controlsRow}>
        <div className={styles.depthControl}>
          <label className={styles.depthLabel}>Engine Depth</label>
          <select
            className={styles.depthSelect}
            value={depth}
            onChange={(e) => setDepth(Number(e.target.value))}
            disabled={isLoading}
          >
            <option value={10}>Fast (Depth 10)</option>
            <option value={15}>Normal (Depth 15)</option>
            <option value={20}>Deep (Depth 20)</option>
            <option value={25}>Maximum (Depth 25)</option>
          </select>
        </div>

        <button
          className={styles.analyzeBtn}
          onClick={handleSubmit}
          disabled={isLoading || !pgn.trim()}
          id="analyze-btn"
        >
          {isLoading ? (
            <>
              <span className={styles.spinner}></span>
              Analyzing...
            </>
          ) : (
            <>
              <span className={styles.analyzeIcon}>⬟</span>
              Analyze Game
            </>
          )}
        </button>
      </div>
    </div>
  );
}
