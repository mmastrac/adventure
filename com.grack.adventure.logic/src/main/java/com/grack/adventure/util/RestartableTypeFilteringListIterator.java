package com.grack.adventure.util;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;

/**
 * Filters stuff out of {@link List} that is of type T. Assumes that the List
 * doesn't change.
 */
public class RestartableTypeFilteringListIterator<U, T extends U> implements Iterator<T> {
	private final Predicate<? super U> isOfT;
	private int index;
	private boolean needNext = true;
	private final List<U> list;

	public RestartableTypeFilteringListIterator(List<U> list, Predicate<? super U> isOfT) {
		this(list, isOfT, 0);
	}

	public RestartableTypeFilteringListIterator(List<U> list, Predicate<? super U> isOfT, int startIndex) {
		this.list = list;
		this.isOfT = isOfT;
		this.index = startIndex;
	}

	private void prepareNext() {
		if (needNext) {
			needNext = false;

			for (; index < list.size(); index++) {
				if (isOfT.apply(list.get(index))) {
					return;
				}
			}

			index = list.size();
		}
	}

	public boolean hasNext() {
		prepareNext();
		return index < list.size();
	}

	@SuppressWarnings("unchecked")
	public T next() {
		prepareNext();
		if (index >= list.size())
			throw new IllegalStateException("Past end of list");
		needNext = true;
		return (T) list.get(index++);
	}

	public int getNextIndex() {
		return index;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
