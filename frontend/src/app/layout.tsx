import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "ChessReview — Game Analysis Platform",
  description: "Analyze your chess games with Stockfish engine. Get move-by-move evaluations, accuracy scores, and Chess.com-style move classifications.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
