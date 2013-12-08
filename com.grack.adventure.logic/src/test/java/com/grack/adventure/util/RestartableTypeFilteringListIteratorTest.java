package com.grack.adventure.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.grack.adventure.util.RestartableTypeFilteringListIterator;

public class RestartableTypeFilteringListIteratorTest {
	@Test
	public void filterNumbers() {
		List<Number> list = Lists.newArrayList();
		list.add((int) 1);
		list.add((double) 1);
		list.add((int) 2);

		RestartableTypeFilteringListIterator<Number, Integer> iterator = 
				new RestartableTypeFilteringListIterator<Number, Integer>(list,
				Predicates.instanceOf(Integer.class), 0);

		assertTrue(iterator.hasNext());
		assertEquals(0, iterator.getNextIndex());
		assertEquals(1, (int)(Integer)iterator.next());
		assertEquals(1, iterator.getNextIndex());
		assertTrue(iterator.hasNext());
		assertEquals(2, iterator.getNextIndex());
		assertEquals(2, (int)(Integer)iterator.next());
		assertEquals(3, iterator.getNextIndex());
		assertFalse(iterator.hasNext());
	}
	
	@Test
	public void noneOfType() {
		List<Number> list = Lists.newArrayList();
		list.add((double) 1);
		list.add((double) 1);
		list.add((double) 1);

		RestartableTypeFilteringListIterator<Number, Integer> iterator = 
				new RestartableTypeFilteringListIterator<Number, Integer>(list,
				Predicates.instanceOf(Integer.class), 0);

		assertFalse(iterator.hasNext());
		assertEquals(3, iterator.getNextIndex());
	}
	
	@Test
	public void empty() {
		List<Number> list = Lists.newArrayList();
		RestartableTypeFilteringListIterator<Number, Integer> iterator = 
				new RestartableTypeFilteringListIterator<Number, Integer>(list,
				Predicates.instanceOf(Integer.class), 0);

		assertFalse(iterator.hasNext());
		assertEquals(0, iterator.getNextIndex());
	}
	
	@Test
	public void restart() {
		List<Number> list = Lists.newArrayList();
		list.add((int) 1);
		list.add((double) 1);
		list.add((int) 2);

		Predicate<Object> isOfT = Predicates.instanceOf(Integer.class);
		RestartableTypeFilteringListIterator<Number, Integer> iterator = 
				new RestartableTypeFilteringListIterator<Number, Integer>(list,
				isOfT, 0);

		assertTrue(iterator.hasNext());
		assertEquals(1, (int)(Integer)iterator.next());
		
		iterator = new RestartableTypeFilteringListIterator<Number, Integer>(list, isOfT, 1);
		assertTrue(iterator.hasNext());
		assertEquals(2, (int)(Integer)iterator.next());

		iterator = new RestartableTypeFilteringListIterator<Number, Integer>(list, isOfT, 2);
		assertTrue(iterator.hasNext());
		assertEquals(2, (int)(Integer)iterator.next());

		iterator = new RestartableTypeFilteringListIterator<Number, Integer>(list, isOfT, 3);
		assertFalse(iterator.hasNext());
	}
}
