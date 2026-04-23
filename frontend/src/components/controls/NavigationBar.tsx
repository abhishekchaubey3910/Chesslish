'use client';

import styles from './NavigationBar.module.css';

interface NavigationBarProps {
  currentIndex: number;
  totalMoves: number;
  onFirst: () => void;
  onPrev: () => void;
  onNext: () => void;
  onLast: () => void;
}

export default function NavigationBar({
  currentIndex, totalMoves, onFirst, onPrev, onNext, onLast,
}: NavigationBarProps) {
  return (
    <div className={styles.navBar} id="nav-bar">
      <button
        className={styles.navBtn}
        onClick={onFirst}
        disabled={currentIndex < 0}
        title="First move"
        id="nav-first"
      >
        ⏮
      </button>
      <button
        className={styles.navBtn}
        onClick={onPrev}
        disabled={currentIndex < 0}
        title="Previous move (←)"
        id="nav-prev"
      >
        ◀
      </button>
      <span className={styles.moveCounter}>
        {currentIndex + 1} / {totalMoves}
      </span>
      <button
        className={styles.navBtn}
        onClick={onNext}
        disabled={currentIndex >= totalMoves - 1}
        title="Next move (→)"
        id="nav-next"
      >
        ▶
      </button>
      <button
        className={styles.navBtn}
        onClick={onLast}
        disabled={currentIndex >= totalMoves - 1}
        title="Last move"
        id="nav-last"
      >
        ⏭
      </button>
    </div>
  );
}
