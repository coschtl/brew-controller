package at.dcosta.brew.util;

public class ManagedThread<T extends Runnable> extends Thread implements StoppableRunnable {
	private final T runnable;

	public ManagedThread(T runnable, String name) {
		super(runnable, name);
		this.runnable = runnable;
	}

	@Override
	public void abort() {
		if (runnable instanceof StoppableRunnable) {
			((StoppableRunnable) runnable).abort();
		}
	}

	public T getRunnable() {
		return runnable;
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