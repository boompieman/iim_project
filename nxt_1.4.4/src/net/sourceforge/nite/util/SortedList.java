package net.sourceforge.nite.util;

import java.util.*;

/**
 * <p>This class implements a sorted list. It is constructed with a comparator
 * that can compare two objects and sort objects accordingly. When you add an
 * object to the list, it is inserted in the correct place. Object that are
 * equal according to the comparator, will be in the list in the order that
 * they were added to this list. Add only objects that the comparator can
 * compare.</p>
 */
public class SortedList extends ArrayList {
	private Comparator comparator;
	
	/**
	 * <p>Constructs a new sorted list. The objects in the list will be sorted
	 * according to the specified comparator.</p>
	 *
	 * @param c a comparator
	 */
	public SortedList(Comparator c) {
		this.comparator = c;
	}
	
	/**
	 * <p>This method has no effect. It is not allowed to specify an index to
	 * insert an element as this might violate the sorting order of the objects
	 * in the list.</p>
	 */
	public void add(int index, Object element) {
		return;
	}
	
	/**
	 * <p>Adds an object to the list. The object will be inserted in the correct
	 * place so that the objects in the list are sorted. When the list already
	 * contains objects that are equal according to the comparator, the new
	 * object will be inserted immediately after these other objects.</p>
	 *
	 * @param o the object to be added
	 */
	public boolean add(Object o) {
		int i = 0;
		boolean found = false;
		while (!found && (i < size())) {
			found = comparator.compare(o,get(i)) < 0;
			if (!found) i++;
		}
		super.add(i,o);
		return true;
	}
}
