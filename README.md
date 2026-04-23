# ChessReview ♟️

A sophisticated, enterprise-grade chess game analysis platform that provides deep insights into your chess games. Powered by Stockfish and built with a sleek, responsive, "Seeing Theory"-inspired UI. 

![ChessReview Interface](frontend/public/favicon.ico) <!-- You can replace this with an actual screenshot later -->

## 🌟 Features

- **Deep Engine Analysis**: Uses a robust Spring Boot backend to manage a pool of Stockfish engines for fast, parallel evaluation of chess games.
- **Move Classification**: Automatically categorizes every move (Brilliant, Great, Best, Excellent, Good, Inaccuracy, Mistake, Blunder, Book) just like top chess platforms.
- **Accuracy Calculation**: Features a highly tuned accuracy percentage system based on a logistic curve (centipawn loss to win-probability conversion) to give you realistic, human-friendly accuracy scores.
- **Interactive Game Review**: Step through your game move-by-move with a dynamic evaluation bar, best-move arrows, and instant visual feedback.
- **Beautiful UI**: A highly polished, responsive interface with a dark charcoal/purple theme, glassmorphism elements, and an interactive physics-based particle background that reacts to your cursor.
- **Adjustable Depth**: Choose how deep you want Stockfish to look (Fast, Normal, Deep, Maximum) depending on your patience and need for precision.

## 🛠️ Tech Stack

### Frontend
- **Framework**: [Next.js](https://nextjs.org/) (React, TypeScript)
- **Chess Logic**: `chess.js` & `react-chessboard`
- **Styling**: Vanilla CSS with modern CSS variables, Flexbox/Grid, and responsive design.
- **Animations**: Custom HTML5 Canvas for the interactive particle engine.

### Backend
- **Framework**: [Spring Boot](https://spring.io/projects/spring-boot) (Java)
- **Engine**: [Stockfish](https://stockfishchess.org/) (External executable pool)
- **Chess Lib**: `bhlangonijr/chesslib` for PGN parsing and move generation.
- **Concurrency**: Parallel processing capabilities mapping evaluations across multiple engine instances to significantly speed up full-game analysis.

## 🚀 Getting Started

### Prerequisites
- **Java 17+** (for the Spring Boot backend)
- **Maven** (for building the backend)
- **Node.js 18+** & **npm** (for the frontend)
- **Stockfish**: You must have the Stockfish engine executable downloaded and its path configured in the backend properties.

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Open `src/main/resources/application.properties` and ensure `stockfish.path` points to your local Stockfish executable.
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   *The backend will start on `http://localhost:8080`.*

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
   *The frontend will be available at `http://localhost:3000`.*

## 🧠 How It Works

1. **Input**: You paste a PGN (Portable Game Notation) into the frontend.
2. **Parsing**: The backend parses the PGN into individual board positions using `chesslib`.
3. **Evaluation**: Positions are distributed across a pool of Stockfish engines using parallel streams to calculate the best move and centipawn score for each position.
4. **Classification & Accuracy**:
   - The engine compares the played move against the best possible move.
   - Using a proprietary algorithm, centipawn losses are converted to win-probability shifts to calculate realistic move accuracies.
   - Moves are tagged with classifications (from Blunder to Brilliant) based on context (e.g., sacrifices, turning points).
5. **Visualization**: The Next.js frontend renders the game data, providing an interactive, seamless review experience.

## 👨‍💻 About the Developer

Built by **Abhishek Chaubey**, a Software Engineer and Analyst based in Bengaluru. Designed for players who appreciate deep, uninterrupted focus and clean, highly-optimized systems.
