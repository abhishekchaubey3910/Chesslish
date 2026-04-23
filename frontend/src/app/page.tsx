'use client';

import { useRouter } from 'next/navigation';
import ParticleBackground from '@/components/board/ParticleBackground';
import styles from './page.module.css';

export default function Home() {
  const router = useRouter();

  return (
    <div className={styles.landing}>
      {/* Unified Header */}
      <header className={styles.header}>
        <a href="/" className={styles.logo}>
          <span className={styles.logoIcon}>♟</span>
          <span className={styles.logoText}>ChessReview</span>
        </a>
        <nav className={styles.nav}>
          <a href="/review" className={styles.navLink}>Review</a>
        </nav>
      </header>

      {/* Particle Background */}
      <ParticleBackground />
      
      <div className={styles.contentContainer}>
        {/* Hero Section */}
        <section className={styles.heroSection}>
          <h1 className={styles.title}>Seeing Chess</h1>
          <p className={styles.subtitle}>A visual introduction to precision analysis.</p>
          <button
            className={styles.ctaButton}
            onClick={() => router.push('/review')}
            id="start-review-btn"
          >
            Start
          </button>
        </section>

        {/* About Section */}
        <section className={styles.aboutSection}>
          <h2 className={styles.aboutTitle}>About the Developer</h2>
          <div className={styles.aboutContent}>
            <p>
              Hi, I'm <strong>Abhishek Chaubey</strong>, a Software Engineer and Analyst based in Bengaluru. I've always been drawn to solving complex problems, writing clean code, and building highly optimized systems.
            </p>
            <p>
              I built this platform because I believe in the power of deep, uninterrupted focus. I often describe my own working style as a "single-core processor"—I thrive when I can lock into a single, complex task without distractions or task-switching. Chess requires that exact same level of singular concentration, which is why I wanted to build an analysis tool that strips away the noise and gives players clear, undeniable feedback on their games.
            </p>
            <p>
              With a background in competitive programming and a love for optimizing algorithms, the challenge of building a bridge to the Stockfish engine was a natural fit. Having previously architected data-heavy platforms and recommendation engines using Java and Spring Boot, I wanted to apply that same enterprise-grade structure here—ensuring that even at maximum engine depths, the backend runs seamlessly.
            </p>
            <p>
              Looking ahead, as I continue to expand my focus into Data Science and Machine Learning, my goal is to keep evolving this platform, bringing even smarter, data-driven insights to how we review and learn from every move.
            </p>
          </div>
        </section>
      </div>
    </div>
  );
}
