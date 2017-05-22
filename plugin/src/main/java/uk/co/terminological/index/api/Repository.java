package uk.co.terminological.index.api;

import java.util.Set;

/**
 * A repository holds all the data available and provides simple means
 * to access indexes for different types in the repository
 * 
 * @author terminological
 *
 */
public interface Repository {

	public Set<Index<?>> getIndexes();
	public <V extends Object> Index<V> getIndex(Class<V> type);
	
	
}
