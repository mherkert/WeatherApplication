package vandy.mooc.cache;

public interface Cache<K, V> {

	public void put(K key, V value, long timeToLive);
	
	public V get(K key);
}
