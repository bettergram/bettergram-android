package ru.johnlife.lifetools.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.util.SparseArray;

import ru.johnlife.lifetools.optional.Iffy;

public class SparseList<E> extends SparseArray<E> implements Iterable<E> {
	
	private List<E> list;
	private boolean listIsConsistent = false;

	public SparseList() {
		super();
	}

	public SparseList(int initialCapacity) {
		super(initialCapacity);
	}

	public static <T extends Identifiable<Integer>> SparseList<T> from(List<T> list) {
		if (list == null) return new SparseList<>();
		SparseList<T> value = new SparseList<>(list.size());
		for (T item : list) {
			value.put(item.getId(), item);
		}
		return value;
	}

	public List<E> asList() {
		if (listIsConsistent) {
			return list;
		} else {
			int size = size();
			if (list == null) {
				list = new ArrayList<>(size);
			} else {
				list.clear();
			}
			for (int i = 0; i < size; i++) {
				list.add(valueAt(i));
			}
			listIsConsistent = true;
			return list;
		}
	}

	private void modified() {
		listIsConsistent = false;
	}

	@Override
	public void delete(int key) {
		modified();
		super.delete(key);
	}

	@Override
	public void remove(int key) {
		modified();
		super.remove(key);
	}

	@Override
	public void put(int key, E value) {
		modified();
		super.put(key, value);
	}

	@Override
	public void clear() {
		modified();
		super.clear();
	}

	@Override
	public void append(int key, E value) {
		modified();
		super.append(key, value);
	}

	public boolean isEmpty() {
		return 0 == size();
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			int i = 0;
			@Override
			public boolean hasNext() {
				return i < size();
			}

			@Override
			public E next() {
				return valueAt(i++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public Iffy<E> find(int key) {
		return new Iffy<>(get(key));
	}


}
