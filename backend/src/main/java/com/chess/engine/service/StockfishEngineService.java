package com.chess.engine.service;

import com.chess.engine.model.EvalResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
public class StockfishEngineService {

    private static final Logger log = LoggerFactory.getLogger(StockfishEngineService.class);

    @Value("${stockfish.path}")
    private String stockfishPath;

    @Value("${stockfish.hash:128}")
    private int hashSize;

    @Value("${stockfish.threads:1}")
    private int threads;

    @Value("${stockfish.pool-size:4}")
    private int poolSize;

    private BlockingQueue<EngineInstance> enginePool;
    private boolean running = false;

    private class EngineInstance {
        Process process;
        BufferedWriter writer;
        BufferedReader reader;
        int id;

        EngineInstance(int id, Path enginePath) throws IOException {
            this.id = id;
            ProcessBuilder pb = new ProcessBuilder(enginePath.toString());
            pb.redirectErrorStream(true);
            this.process = pb.start();
            this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        }

        void sendCommand(String command) throws IOException {
            writer.write(command);
            writer.newLine();
            writer.flush();
        }

        void waitForResponse(String expected) throws IOException {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals(expected)) return;
            }
        }

        void close() {
            try {
                sendCommand("quit");
                process.waitFor();
            } catch (Exception e) {
                process.destroyForcibly();
            }
        }
    }

    @PostConstruct
    public void startEngine() {
        try {
            Path enginePath = Paths.get(stockfishPath).toAbsolutePath();
            if (!enginePath.toFile().exists()) {
                log.error("Stockfish binary not found at: {}", enginePath);
                return;
            }

            enginePool = new ArrayBlockingQueue<>(poolSize);

            for (int i = 0; i < poolSize; i++) {
                EngineInstance engine = new EngineInstance(i, enginePath);
                engine.sendCommand("uci");
                engine.waitForResponse("uciok");
                engine.sendCommand("setoption name Hash value " + hashSize);
                engine.sendCommand("setoption name Threads value " + threads);
                engine.sendCommand("isready");
                engine.waitForResponse("readyok");
                enginePool.offer(engine);
            }

            running = true;
            log.info("Started {} Stockfish engines successfully (Hash={}MB, Threads={} per engine)", poolSize, hashSize, threads);

        } catch (Exception e) {
            log.error("Failed to start Stockfish engine pool", e);
        }
    }

    @PreDestroy
    public void stopEngine() {
        running = false;
        if (enginePool != null) {
            for (EngineInstance engine : enginePool) {
                engine.close();
            }
            enginePool.clear();
        }
        log.info("Stockfish engine pool stopped");
    }

    public EvalResult getEvaluation(String fen, int depth) {
        if (!running) {
            throw new IllegalStateException("Stockfish engines are not running");
        }

        EngineInstance engine = null;
        try {
            engine = enginePool.take(); // blocks until an engine is available
            engine.sendCommand("position fen " + fen);
            
            // Use movetime for guaranteed fast throughput: depth 10 = 100ms
            int movetimeMs = depth > 0 ? depth * 10 : 150;
            engine.sendCommand("go movetime " + movetimeMs);

            return parseSearchOutput(engine, fen);
        } catch (Exception e) {
            log.error("Error evaluating position: {}", fen, e);
            return new EvalResult(0, false, 0, "", List.of(), 0);
        } finally {
            if (engine != null) {
                enginePool.offer(engine); // return to pool
            }
        }
    }

    private EvalResult parseSearchOutput(EngineInstance engine, String fen) throws IOException {
        int scoreCp = 0;
        boolean isMate = false;
        int mateIn = 0;
        String bestMove = "";
        List<String> pvLine = new ArrayList<>();
        int depthReached = 0;

        boolean blackToMove = fen.contains(" b ");

        String line;
        while ((line = engine.reader.readLine()) != null) {
            if (line.startsWith("info depth")) {
                if (line.contains("upperbound") || line.contains("lowerbound")) continue;
                if (line.contains(" multipv ") && !line.contains(" multipv 1")) continue;

                int depthIdx = line.indexOf("depth ") + 6;
                int depthEnd = line.indexOf(' ', depthIdx);
                int currentDepth = Integer.parseInt(line.substring(depthIdx, depthEnd > 0 ? depthEnd : line.length()));

                if (line.contains("score cp ")) {
                    int cpIdx = line.indexOf("score cp ") + 9;
                    int cpEnd = line.indexOf(' ', cpIdx);
                    int cp = Integer.parseInt(line.substring(cpIdx, cpEnd > 0 ? cpEnd : line.length()));

                    scoreCp = blackToMove ? -cp : cp;
                    isMate = false;
                    mateIn = 0;
                    depthReached = currentDepth;

                } else if (line.contains("score mate ")) {
                    int mateIdx = line.indexOf("score mate ") + 11;
                    int mateEnd = line.indexOf(' ', mateIdx);
                    int mate = Integer.parseInt(line.substring(mateIdx, mateEnd > 0 ? mateEnd : line.length()));

                    mateIn = blackToMove ? -mate : mate;
                    isMate = true;
                    scoreCp = mateIn > 0 ? 100000 : -100000;
                    depthReached = currentDepth;
                }

                if (line.contains(" pv ")) {
                    int pvIdx = line.indexOf(" pv ") + 4;
                    String pvStr = line.substring(pvIdx).trim();
                    pvLine = Arrays.asList(pvStr.split("\\s+"));
                }

            } else if (line.startsWith("bestmove")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    bestMove = parts[1];
                }
                break;
            }
        }

        return new EvalResult(scoreCp, isMate, mateIn, bestMove, pvLine, depthReached);
    }

    public boolean isRunning() {
        return running;
    }
}
