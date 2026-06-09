package com.smarthome.command;

import com.smarthome.exceptions.SensorReadingException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CommandScheduler implements Runnable {
    private final BlockingQueue<SensorCommand> queue = new LinkedBlockingQueue<>();

    private final Deque<SensorCommand> undoStack = new ArrayDeque<>();
    private final List<String> executionLog = new ArrayList<>();
    private volatile boolean running = false;
    private Thread workerThread;

    public void start() {
        running = true;
        workerThread = new Thread(this, "CommandScheduler-worker");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public void shutdown() throws InterruptedException {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
            workerThread.join();
        }
    }

    public void schedule(SensorCommand command) {
        if (!running) {
            throw new IllegalStateException("CommandScheduler is not running. Call start() first.");
        }
        queue.offer(command);
    }

    @Override
    public void run() {
        System.out.println("[CommandScheduler] Worker thread started");
        while (running || !queue.isEmpty()) {
            try {
                SensorCommand command = queue.poll();
                if (command == null) {
                    Thread.sleep(10);   //wait for work
                    continue;
                }
                executeCommand(command);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        // execute any remaining commands before closing
        SensorCommand remaining;
        while ((remaining = queue.poll()) != null) {
            executeCommand(remaining);
        }
        System.out.println("[CommandScheduler] Worker thread stopped");
    }

    private void executeCommand(SensorCommand command) {
        try {
            System.out.printf("[CommandScheduler] Executing: %s%n", command.describe());
            command.execute();
            undoStack.push(command);
            executionLog.add("EXECUTED: " + command.describe());
        } catch (SensorReadingException e) {
            String entry = "FAILED: " + command.describe() + " → " + e.getMessage();
            executionLog.add(entry);
            System.err.println("[CommandScheduler] " + entry);
        }
    }

    public void undoLast() {
        if (undoStack.isEmpty()) {
            System.out.println("[CommandScheduler] Nothing to undo");
            return;
        }
        SensorCommand command = undoStack.pop();
        command.undo();
        executionLog.add("UNDONE: " + command.describe());
    }

    public void undoAll() {
        while (!undoStack.isEmpty()) {
            undoLast();
        }
    }

    public List<String> getExecutionLog() {
        return List.copyOf(executionLog);
    }

    public int getPendingCount()  { return queue.size(); }
    public int getUndoStackSize() { return undoStack.size(); }

    public void schedule(DeactivateSensorCommand deactivateSensorCommand) {
    }
}