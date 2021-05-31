package org.golde.router.objects;

import java.util.List;
import java.util.Set;

/**
 * A wrapper around com.sun.net.httpserver.Headers.
 * Renames some variables, and removes some methods that are not used.
 * @author Eric Golde
 *
 */
public class Headers {

	private final com.sun.net.httpserver.Headers sunHeaders;

	/**
	 * Creates the Headers object. Constructor used internally, not really for public use.
	 * @param sunHeaders the com.sun.net.httpserver.Headers
	 */
	Headers(com.sun.net.httpserver.Headers sunHeaders) {
		this.sunHeaders = sunHeaders;
	}

	/**
	 * Removes all of the mappings from this map (optional operation). The map will be empty after this call returns.
	 */
	public void clear() {
		sunHeaders.clear();
	}

	/**
	 * Returns true if this map contains a mapping for the specified key. More formally, returns true if and only if this map contains a mapping for a key k such that (key==null ? k==null : key.equals(k)). (There can be at most one such mapping.)
	 * @param key value whose presence in this map is to be tested
	 * @return true if this map maps one or more keys to the specified value
	 */
	public boolean containsKey(String key) {
		return sunHeaders.containsKey(key);
	}

	/**
	 * returns the first value from the List of String values for the given key (if at least one exists).
	 * @param key the key to search for
	 * @return the first string value associated with the key
	 */
	public String getFirst(String key) {
		return sunHeaders.getFirst(key);
	}

	/**
	 * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
	 * More formally, if this map contains a mapping from a key k to a value v such that (key==null ? k==null : key.equals(k)), then this method returns v; otherwise it returns null. (There can be at most one such mapping.)
	 * If this map permits null values, then a return value of null does not necessarily indicate that the map contains no mapping for the key; it's also possible that the map explicitly maps the key to null. The containsKey operation may be used to distinguish these two cases.
	 * @param key the key whose associated value is to be returned
	 * @return a list of values to which the specified key is mapped, or null if this map contains no mapping for the key
	 */
	public List<String> get(String key) {
		return sunHeaders.get(key);
	}

	/**
	 * Returns a Set view of the keys contained in this map. The set is backed by the map, so changes to the map are reflected in the set, and vice-versa. If the map is modified while an iteration over the set is in progress (except through the iterator's own remove operation), the results of the iteration are undefined. The set supports element removal, which removes the corresponding mapping from the map, via the Iterator.remove, Set.remove, removeAll, retainAll, and clear operations. It does not support the add or addAll operations.
	 * @return a set view of the keys contained in this map
	 */
	public Set<String> getKeys() {
		return sunHeaders.keySet();
	}

	/**
	 * adds the given value to the list of headers for the given key. If the mapping does not already exist, then it is created
	 * @param key the header name
	 * @param value the header value to set
	 */
	public void set(String key, String value) {
		sunHeaders.add(key, value);
	}

	//Not sure why you would ever need this, but may add it if needed
	//	public List<String> remove(String key) {
	//		return sunHeaders.remove(key);
	//	}

	/**
	 * Returns the number of key-value mappings in this map. If the map contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
	 * @return the number of key-value mappings in this map
	 */
	public int size() {
		return sunHeaders.size();
	}

}
