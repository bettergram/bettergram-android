package ru.johnlife.lifetools.data;

import java.util.Collection;
import java.util.Iterator;

public class Pool<E> implements Iterable<E>{
	private E[] pool;
	private int cursor = 0;
	private boolean firstCycle = true;
	
	public Pool(int capacity) {
		this(null, capacity);
	}

	/**
	 * WARNING: Any add will modify the source array! For instance: 
	 * The next <code>add()</code> will overwrite the first value in the array!!!
	 * @param pool - array to be based on. 
	 * @throws NullPointerException in case <code>pool</code> is <code>null</code>
	 */
	public Pool(E[] pool) {
		this(pool, 0);
	}

	/**
	 * Creates a pool with all elements of <code>pool</code> array, but with greater <code>capacity</code>.
	 * WARNING: In case <code>greaterCapacity</code> is less OR equal to <code>pool.length</code> this constructor will fall back to MODIFYING behavior of <code>Pool(E[] pool)</code>
	 * @throws NullPointerException in case <code>pool</code> is <code>null</code> and <code>greaterCapacity</code> == 0
	 */
	@SuppressWarnings("unchecked")
	public Pool(E[] pool, int greaterCapacity) {
		if (null == pool && greaterCapacity == 0) throw new NullPointerException();
		if (null == pool || pool.length < greaterCapacity) {
			this.pool = (E[])new Object[greaterCapacity];
			cursor = 0;
			firstCycle = true;
			if (pool != null) {
				addAll(pool);
			}
		} else  {
			this.pool = pool;
			cursor = 0;
			firstCycle = false; //we already have full pool =))) 
		}
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			private int iCursor = isEmpty() ? -1 : firstCycle ? 0 : cursor;
			
			@Override
			public boolean hasNext() {
				return iCursor != -1 && pool[iCursor] != null;
			}

			@Override
			public E next() {
				if (iCursor == -1) return null; 
				E value = pool[iCursor];
				if ((++iCursor) >= pool.length ) {
					iCursor = 0; 
				} 
				if (iCursor == cursor) {
					iCursor = -1;
				}
				return value;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("You only can add to pool");				
			}
		};
	}

	public boolean isEmpty() {
		return cursor == 0 && firstCycle;
	}

	public int size() {
		return firstCycle ? cursor : pool.length;
	}

	public int capacity() {
		return pool.length;
	}

	public void add(E object) {
		pool[cursor] = object;
		if ((++cursor) >= pool.length ) {
			cursor = 0; 
			firstCycle = false;
		}
	}

	public void addAll(Collection<? extends E> collection) {
		for (E e : collection) {
			add(e);
		}
	}

	public void addAll(E[] array) {
		for (E e : array) {
			add(e);
		}
	}

	public void clear() {
		cursor = 0;
		firstCycle = true;
	}

	// TODO implement someday 
	/*
	public E get(int location) {
		return null;
	}
	*/

	public boolean contains(Object object) {
		for (E e : this) {
			if (e.equals(object)) return true;
		}
		return false;
	}

	public boolean containsAll(Collection<?> collection) {
		for (Object o : collection) {
			if (!contains(o)) return false;
		}
		return true;
	}


////		List<String> needle = Arrays.asList(new String[]{"value2", "value3"});
//		Pool<String> pool = new Pool<String>(new String[]{"xxlue1", "xxlue2", "xxlue3", "xxlue4", "xxlue5", "xxlue6", "xxlue7", "xxlue8", "xxlue9"}, 15);
//		System.out.println(""+pool.isEmpty()+"|"+pool.size());
//		pool.add("value1");
//		pool.add("value2");
//		pool.add("value3");
//		Pool.<String>dumpPool(pool);
//		System.out.println("------ after 3 add()");
//		System.out.println(""+pool.isEmpty()+"|"+pool.size());
////		pool.add("value4");
////		pool.add("value5");
////		Pool.<String>dumpPool(pool);
////		System.out.println("------ after 5 add()");
////		System.out.println(""+pool.isEmpty()+"|"+pool.size());
////		pool.add("value6");
////		pool.add("value7");
////		Pool.<String>dumpPool(pool);
////		System.out.println("------ after 7 add()");
////		System.out.println(""+pool.isEmpty()+"|"+pool.size());
////		pool.clear();
////		Pool.<String>dumpPool(pool);
////		System.out.println(""+pool.isEmpty()+"|"+pool.size());
////		System.out.println(pool.containsAll(needle));
//		
////		Pool<String> pool = new Pool<String>(new String[]{"xxlue1", "xxlue2", "xxlue3", "xxlue4", "xxlue5", "xxlue6", "xxlue7", "xxlue8", "xxlue9"});
////		Pool.<String>dumpPool(pool);
////		System.out.println(""+pool.isEmpty()+"|"+pool.size());
////		pool.add("value1");
////		pool.add("value2");
////		pool.add("value3");
////		Pool.<String>dumpPool(pool);
////		System.out.println(""+pool.isEmpty()+"|"+pool.size());
//	}
//	
//	private static <T> void dumpPool(Pool<T> pool) {
//		for (T s : pool) {
//			System.out.println(s);
//		}
//	}
	
}
