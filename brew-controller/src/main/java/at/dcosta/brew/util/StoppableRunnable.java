package at.dcosta.brew.util;

public interface StoppableRunnable extends Runnable {

	void abort();

	boolean mustComplete();

}
