package com.grack.adventure.util;

import java.util.List;

import com.google.common.base.Predicate;

public class RestartableTypeFilteringListIterable<U, T extends U> implements Iterable<T> {
	private List<U> list;
	private Predicate<? super U> isOfT;

	public RestartableTypeFilteringListIterable(List<U> list, Predicate<? super U> isOfT) {
		this.list = list;
		this.isOfT = isOfT;
	}

	public RestartableTypeFilteringListIterator<U, T> iterator() {
		return new RestartableTypeFilteringListIterator<U, T>(list, isOfT);
	}
}
