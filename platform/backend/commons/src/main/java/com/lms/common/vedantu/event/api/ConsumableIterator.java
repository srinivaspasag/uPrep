package com.lms.common.vedantu.event.api;

import java.util.Collection;
import java.util.Iterator;

public class ConsumableIterator<E extends IConsumable> implements Iterator<E> {

	private Iterator<E> iterator;

	private int size;

	public ConsumableIterator(Collection<E> consumables) {
		if (null != consumables) {
			this.iterator = consumables.iterator();
			size = consumables.size();
		}
	}

	@Override
	public boolean hasNext() {
		return null != iterator && iterator.hasNext();
	}

	@Override
	public E next() {
		return null != iterator ? iterator.next() : null;
	}

	@Override
	public void remove() {
		if (null != iterator) {
			iterator.remove();
		}
	}

	public int size() {
		return size;
	}

}
