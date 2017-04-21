package at.dcosta.brew.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ThreadManager {

	private static final ThreadManager INSTANCE = new ThreadManager();

	public static ThreadManager getInstance() {
		return INSTANCE;
	}

	private Map<String, ManagedThread<?>> threads;

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

	public synchronized <T extends Runnable> ManagedThread<T> newThread(T runnable, String name) {
		removeDiedThreads();
		ManagedThread<T> thread = new ManagedThread<T>(runnable, name);
		threads.put(thread.getName(), thread);
		return thread;
	}

	public synchronized void stopAllThreads() {
		removeDiedThreads();
		for (ManagedThread<?> thread : threads.values()) {
			thread.abort();
		}
		waitForAllThreadsToComplete();
	}

	public synchronized void waitForAllThreadsToComplete() {
		System.out.println("waitForAllThreadsToComplete");
		removeDiedThreads();
		for (ManagedThread<?> thread : threads.values()) {
			if (thread.mustComplete()) {
				System.out.println("must wait for thread " + thread.getName() + " to complete");
				try {
					thread.join();
				} catch (InterruptedException e) {
					// ignore
				}
			} else {
				thread.abort();
			}
		}
	}

	private synchronized void removeDiedThreads() {
		List<String> toRemove = new ArrayList<>();
		Iterator<Entry<String, ManagedThread<?>>> it = threads.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, ManagedThread<?>> entry = it.next();
			if (!entry.getValue().isAlive()) {
				toRemove.add(entry.getKey());
			}
		}
		for (String id : toRemove) {
			threads.remove(id);
		}
	}

}
