package at.dcosta.brew.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class ThreadManager {

	private static final ThreadManager INSTANCE = new ThreadManager();

	public static ThreadManager getInstance() {
		return INSTANCE;
	}

	private Map<String, Thread> threads;
	private Map<String, Runnable> runnables;

	private ThreadManager() {
		threads = new HashMap<>();
		runnables = new HashMap<>();
	}

	public synchronized Thread getThread(String id) {
		removeDiedThreads();
		return threads.get(id);
	}

	public synchronized boolean isThreadRunning(String id) {
		removeDiedThreads();
		Thread t = getThread(id);
		if (t == null) {
			return false;
		}
		return t.isAlive();
	}

	public synchronized Thread newThread(Runnable runnable) {
		removeDiedThreads();
		String id = UUID.randomUUID().toString();
		Thread t = new Thread(runnable, id);
		threads.put(id, t);
		runnables.put(id, runnable);
		return t;
	}

	public synchronized void stopAllThreads() {
		removeDiedThreads();
		for (Runnable r : runnables.values()) {
			if (r instanceof StoppableRunnable) {
				((StoppableRunnable)r).stop();
			}
		}
		waitForAllThreadsToComplete();
	}

	public synchronized void waitForAllThreadsToComplete() {
		removeDiedThreads();
		for (Thread t : threads.values()) {
			try {
				if (t.isAlive()) {
					t.join();
				}
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	private synchronized void removeDiedThreads() {
		List<String> toRemove = new ArrayList<>();
		Iterator<Entry<String, Thread>> it = threads.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Thread> entry = it.next();
			if (!entry.getValue().isAlive()) {
				toRemove.add(entry.getKey());
			}
		}
		for (String id : toRemove) {
			threads.remove(id);
			runnables.remove(id);
		}
	}

}
