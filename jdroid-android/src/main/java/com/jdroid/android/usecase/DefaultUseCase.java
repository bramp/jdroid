package com.jdroid.android.usecase;

/**
 * 
 * @param <T> The Listener type
 * @author Maxi Rosson
 */
public interface DefaultUseCase<T> extends Runnable {
	
	public void addListener(T listener);
	
	public void removeListener(T listener);
	
}
