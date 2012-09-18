package com.jdroid.android.repository;

import com.jdroid.java.domain.Identifiable;
import com.jdroid.java.repository.Repository;

/**
 * 
 * @param <T>
 * @author Maxi Rosson
 */
public interface SynchronizedRepository<T extends Identifiable> extends Repository<T> {
	
	/**
	 * @return The last update timestamp
	 */
	public Long getLastUpdateTimestamp();
	
	/**
	 * @return Whether the repository data is outdated or not
	 */
	public Boolean isOutdated();
	
	/**
	 * Reset the last update timestamp making the repository data outdated
	 */
	public void resetLastUpdateTimestamp();
}
