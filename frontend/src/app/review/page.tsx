'use client';

import { useState, useEffect, useCallback } from 'react';
import { GameAnalysis } from '@/types';
import { analyzeGame } from '@/lib/api';
import { Chess } from 'chess.js';
import ChessBoardComponent from '@/components/board/ChessBoard';
import EvalBar from '@/components/board/EvalBar';
import MoveList from '@/components/panel/MoveList';
import NavigationBar from '@/components/controls/NavigationBar';
import PgnInput from '@/components/panel/PgnInput';
import GameInfo from '@/components/panel/GameInfo';
import GameSummary from '@/components/panel/GameSummary';
import ParticleBackground from '@/components/board/ParticleBackground';
import styles from './page.module.css';

const STARTING_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';

export default function ReviewPage() {
  const [analysis, setAnalysis] = useState<GameAnalysis | null>(null);
  const [moveIndex, setMoveIndex] = useState(-1);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [customFen, setCustomFen] = useState<string | null>(null);
  const [showBestMove, setShowBestMove] = useState(false);
  const [activeTab, setActiveTab] = useState<'moves' | 'summary'>('summary');
  const [boardSize, setBoardSize] = useState(560);

  // Dynamic board size based on viewport
  useEffect(() => {
    const calcSize = () => {
      const maxSize = Math.min(560, window.innerHeight - 170);
      setBoardSize(Math.max(300, maxSize));
    };
    calcSize();
    window.addEventListener('resize', calcSize);
    return () => window.removeEventListener('resize', calcSize);
  }, []);

  // Current position
  const currentFen = customFen ?? (analysis
    ? (moveIndex >= 0 ? analysis.moves[moveIndex].fen : analysis.startingFen)
    : STARTING_FEN);

  const currentMove = (analysis && moveIndex >= 0 && !customFen) ? analysis.moves[moveIndex] : undefined;
  const totalMoves = analysis?.moves.length ?? 0;

  // Current eval
  const currentEval = currentMove?.eval ?? 0;
  const currentEvalType = currentMove?.evalType ?? 'cp';
  const currentMateIn = currentMove?.mateIn ?? 0;

  // Navigation callbacks (reset custom variations)
  const goFirst = useCallback(() => { setMoveIndex(-1); setCustomFen(null); }, []);
  const goPrev = useCallback(() => { setMoveIndex(i => Math.max(-1, i - 1)); setCustomFen(null); }, []);
  const goNext = useCallback(() => { setMoveIndex(i => Math.min(totalMoves - 1, i + 1)); setCustomFen(null); }, [totalMoves]);
  const goLast = useCallback(() => { setMoveIndex(totalMoves - 1); setCustomFen(null); }, [totalMoves]);

  // Keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.target instanceof HTMLTextAreaElement || e.target instanceof HTMLInputElement) return;
      switch (e.key) {
        case 'ArrowLeft':  e.preventDefault(); goPrev(); break;
        case 'ArrowRight': e.preventDefault(); goNext(); break;
        case 'Home':       e.preventDefault(); goFirst(); break;
        case 'End':        e.preventDefault(); goLast(); break;
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [goFirst, goPrev, goNext, goLast]);

  // Submit analysis
  const handleAnalyze = async (pgn: string, depth: number) => {
    setIsLoading(true);
    setError(null);
    setAnalysis(null);
    setMoveIndex(-1);
    setCustomFen(null);
    setActiveTab('summary');

    try {
      const result = await analyzeGame(pgn, depth);
      setAnalysis(result);
      setMoveIndex(0);
    } catch (err: any) {
      setError(err.message || 'Analysis failed. Is the backend running?');
    } finally {
      setIsLoading(false);
    }
  };

  const handlePieceDrop = (sourceSquare: string, targetSquare: string): boolean => {
    try {
      const chess = new Chess(customFen || currentFen);
      const move = chess.move({ from: sourceSquare, to: targetSquare, promotion: 'q' });
      if (move) {
        setCustomFen(chess.fen());
        return true;
      }
    } catch {
      return false;
    }
    return false;
  };

  const resetAnalysis = () => { setAnalysis(null); setMoveIndex(-1); };

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <a href="/" className={styles.logo}>
          <span className={styles.logoIcon}>♟</span>
          <span className={styles.logoText}>ChessReview</span>
        </a>
        <nav className={styles.nav}>
          <a href="/review" className={`${styles.navLink} ${styles.navActive}`}>Review</a>
        </nav>
      </header>

      <ParticleBackground />

      <main className={styles.main}>
        {!analysis && (
          <div className={styles.inputSection}>
            <PgnInput onSubmit={handleAnalyze} isLoading={isLoading} />
            {error && (
              <div className={styles.error}>
                <span>⚠</span> {error}
              </div>
            )}
          </div>
        )}

        {analysis && (
          <div className={styles.analysisLayout}>
            <div className={styles.boardSection} style={{ '--board-size': `${boardSize}px` } as React.CSSProperties}>
              <div className={styles.boardContainer}>
                <EvalBar eval_score={currentEval} evalType={currentEvalType} mateIn={currentMateIn} />
                <div className={styles.boardAndControls}>
                  <ChessBoardComponent
                    position={currentFen}
                    currentMove={currentMove}
                    showBestMove={showBestMove}
                    onPieceDrop={handlePieceDrop}
                    boardSize={boardSize}
                  />
                  <NavigationBar
                    currentIndex={moveIndex}
                    totalMoves={totalMoves}
                    onFirst={goFirst}
                    onPrev={goPrev}
                    onNext={goNext}
                    onLast={goLast}
                  />
                </div>
              </div>
            </div>

            <div className={styles.sidePanel}>
              <div className={styles.tabHeader}>
                <button
                  className={`${styles.tabBtn} ${activeTab === 'summary' ? styles.tabActive : ''}`}
                  onClick={() => setActiveTab('summary')}
                >
                  Game Review
                </button>
                <button
                  className={`${styles.tabBtn} ${activeTab === 'moves' ? styles.tabActive : ''}`}
                  onClick={() => setActiveTab('moves')}
                >
                  Analysis
                </button>
              </div>

              {activeTab === 'summary' ? (
                <>
                  <GameSummary analysis={analysis} />
                  <div className={styles.newAnalysis}>
                    <button className={styles.newBtn} onClick={resetAnalysis}>← New Analysis</button>
                  </div>
                </>
              ) : (
                <>
                  <div className={styles.settingsRow}>
                    <label className={styles.toggleLabel}>
                      <input type="checkbox" checked={showBestMove} onChange={e => setShowBestMove(e.target.checked)} />
                      Always Show Best Move Arrow
                    </label>
                  </div>

                  <GameInfo player={analysis.black} color="black" classifications={analysis.summary.blackClassifications} />
                  <MoveList moves={analysis.moves} currentIndex={moveIndex} onMoveClick={(idx) => { setMoveIndex(idx); setCustomFen(null); }} />
                  <GameInfo player={analysis.white} color="white" classifications={analysis.summary.whiteClassifications} />

                  {currentMove && (
                    <div className={styles.engineInfo}>
                      <div className={styles.engineEval}>
                        <span className={styles.engineLabel}>Eval</span>
                        <span className={styles.engineValue}>
                          {currentMove.evalType === 'mate'
                            ? `M${Math.abs(currentMove.mateIn)}`
                            : (currentMove.eval >= 0 ? '+' : '') + currentMove.eval.toFixed(2)}
                        </span>
                      </div>
                      {currentMove.bestLine?.length > 0 && (
                        <div className={styles.engineLine}>
                          <span className={styles.engineLabel}>Best line</span>
                          <span className={styles.lineText}>{currentMove.bestLine.slice(0, 6).join(' ')}</span>
                        </div>
                      )}
                    </div>
                  )}

                  <div className={styles.newAnalysis}>
                    <button className={styles.newBtn} onClick={resetAnalysis}>← New Analysis</button>
                  </div>
                </>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
