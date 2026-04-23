'use client';

import { Classification, CLASSIFICATION_META } from '@/types';
import styles from './MoveIcon.module.css';

interface MoveIconProps {
  classification: Classification;
  size?: 'sm' | 'md' | 'lg';
}

export default function MoveIcon({ classification, size = 'md' }: MoveIconProps) {
  const meta = CLASSIFICATION_META[classification];

  return (
    <span
      className={`${styles.icon} ${styles[size]}`}
      style={{ color: '#fff', backgroundColor: meta.color }}
      title={meta.label}
    >
      {meta.symbol}
    </span>
  );
}
