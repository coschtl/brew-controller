package at.dcosta.brew.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class ThreadManager {

	private static class ManagedThread extends Thread implements StoppableRunnable {
		private final Runnable runnable;

		public ManagedThread(Runnable runnable) {
			super(runnable, UUID.randomUUID().toString());
			this.runnable = runnable;
		}

		@Override
		public void abort() {
			if (runnable instanceof StoppableRunnable) {
				((StoppableRunnable) runnable).abort();
			}
		}

		@Override
		public boolean mustComplete() {
			if (!isAlive()) {
				return false;
			}
			if (runnable instanceof StoppableRunnable) {
				return ((StoppableRunnable) runnable).mustComplete();
			}
			return true;
		}

	}

	private static final ThreadManager INSTANCE = new ThreadManager();

	public static ThreadManager getInstance() {
		return INSTANCE;
	}

	private Map<String, ManagedThread> threads;
	// private Map<String, Runnable> runnables;

	private ThreadManager() {
		threads = new HashMap<>();
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
		ManagedThread thread = new ManagedThread(runnable);
		threads.put(thread.getName(), thread);
		return thread;
	}

	public synchronized void stopAllThreads() {
		removeDiedThreads();
		for (ManagedThread thread : threads.values()) {
			thread.abort();
		}
		waitForAllThreadsToComplete();
	}

	public synchronized void waitForAllThreadsToComplete() {
		removeDiedThreads();
		for (ManagedThread thread : threads.values()) {
			if (thread.mustComplete()) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}

	private synchronized void removeDiedThreads() {
		List<String> toRemove = new ArrayList<>();
		Iterator<Entry<String, ManagedThread>> it = threads.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, ManagedThread> entry = it.next();
			if (!entry.getValue().isAlive()) {
				toRemove.add(entry.getKey());
			}
		}
		for (String id : toRemove) {
			threads.remove(id);
		}
	}

}
