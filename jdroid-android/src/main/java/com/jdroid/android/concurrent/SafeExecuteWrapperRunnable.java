package com.jdroid.android.concurrent;

import android.support.v4.app.Fragment;

/**
 * Runnable implementation to wrap runnables to run them safely in the UI thread from a fragment. This class only call
 * the "run()" method of the wrapped runnable if the fragment is not detached.
 * 
 */
public class SafeExecuteWrapperRunnable implements Runnable {
	
	private Fragment fragment;
	private Runnable runnable;
	
	public SafeExecuteWrapperRunnable(Fragment fragment, Runnable runnable) {
		this.fragment = fragment;
		this.runnable = runnable;
	}
	
	@Override
	public void run() {
		if (!fragment.isDetached()) {
			runnable.run();
		}
	}
}
