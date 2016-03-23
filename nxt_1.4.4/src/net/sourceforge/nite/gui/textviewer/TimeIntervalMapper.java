/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Class for efficiently selecting objects which overlap
 * particular points on a timeline. Jonathan added span handling 16/9/04.
 * 
 * @author judy
 */
public class TimeIntervalMapper {

    /*
     * Constants for reasoning about the relative positions of
     * two buckets b1, b2. The first part identifies whether b1's
     * start is less or more than b2's start. The second part
     * describes the relative values of the end positions.
     */
    private static final int SAME_LESS = 0x10;
    private static final int SAME_SAME = 0x11;
    private static final int SAME_MORE = 0x12;
    private static final int LESS_SAME = 0x01;
    private static final int MORE_SAME = 0x21;
    private static final int LESS_LESS = 0x00;
    private static final int LESS_MORE = 0x02;
    private static final int MORE_LESS = 0x20;
    private static final int MORE_MORE = 0x22;
    private static final double TINY_TIME = 0.0000001;

    /** Is the mapping built yet? */
    private boolean isFinished;

    /** Minimum start time of any bucket. */
    private double minStartTime;

    /** Maximum end time of any bucket. */
    private double maxEndTime;

    /** The List of buckets currently known about. */
    private LinkedList buckets = new LinkedList();

    /**
     * Construct an empty <code>TimeIntervalMapper</code>.
     */
    public TimeIntervalMapper() {
        // Nothing needed for now.	
    }

    /**
     * Remove the specified object from the buckets
     * @param o
     * @param startTime
     * @param endTime
     */
    public void removeObject(Object o, double startTime, double endTime) {
        // I'm not using the BucketIterator because it returns
        // unmodifiable sets, and I don't want to mess that up in case
        // it has knock-on effects
        Iterator bucketIterator = buckets.iterator();
        while (bucketIterator.hasNext()) {
            Bucket bucket = (Bucket) bucketIterator.next();
            //if the bucket relates to the specified time frame
           
            if ((bucket.startTime >= startTime)
               
                || (bucket.endTime <= endTime))
                 {
                if (bucket.objects != Collections.EMPTY_SET && bucket.objects != null) {
                    if (bucket.objects.contains(o)) {

                        bucket.objects.remove(o);
                    }
                    //System.out.println("Removed " + o);
                }
            
        }
    }
}

/**
 * Method addObject.
 * 
 * @param o 		The object to add.
 * @param startTime	The start time of the object.
 * @param endTime	The end time of the object.
 */
public void addObject(Object o, double startTime, double endTime) {

    // don't add untimed or negative time elements to the map.  note
    // that we now add zero-length elements to the map - just requires
    // a more lenient 'containsTime' implementation to make it work
    // (see Bucket class below).
    if (Double.isNaN(startTime) || startTime<0 || 
        Double.isNaN(endTime) || endTime<0 
	)//		||   startTime==endTime) 
	{ 
	    return; 
	}

    // Make a new bucket with supplied details.
    Bucket newBucket = new Bucket(startTime, endTime);
    newBucket.objects = new HashSet();
    newBucket.objects.add(o);

    //System.err.println("Adding: " + newBucket);

    if (buckets.isEmpty()) {
        // If there are no buckets, we need to set the initial
        // maximum extents of the timeline.
        minStartTime = newBucket.startTime;
        maxEndTime = newBucket.endTime;

        // Add the new bucket. And that's all we need to do since there
        // are no other buckets with which to interact.
        buckets.add(newBucket);

    } else {

        // Extend buckets to the left if necessary.
        if (newBucket.startTime < minStartTime) {
            Bucket emptyBucket = new Bucket(newBucket.startTime, minStartTime);
            buckets.addFirst(emptyBucket);
            extendTimeLine(emptyBucket);
        }

        // Extend buckets to the right if necessary.
        if (newBucket.endTime > maxEndTime) {
            Bucket emptyBucket = new Bucket(maxEndTime, newBucket.endTime);
            buckets.addLast(emptyBucket);
            extendTimeLine(emptyBucket);
        }

        // Find buckets intersecting start and end of new bucket.
        int[] overlapIndices =
            findOverlappingBucketIndices(
                newBucket.startTime,
                newBucket.endTime);

        // Assert that some indices were returned.
        //assert overlapIndices != null;

        // Extract start and end indices for convenience.				
        int startBucketIndex = overlapIndices[0];
        int endBucketIndex = overlapIndices[1];

        // Buckets should always be found. And stuff.
        assert(startBucketIndex >= 0) && (endBucketIndex >= 0);
        assert endBucketIndex >= startBucketIndex;

        // Extract the sublist containing only the overlapping buckets.
        List overlapList =
            buckets.subList(startBucketIndex, endBucketIndex + 1);

        // We should always have some overlap.
        assert !overlapList.isEmpty();

        for (ListIterator it = overlapList.listIterator(); it.hasNext();) {
            Bucket other = (Bucket) it.next();

            // Test position of other bucket with respect to the new one.
            int cmp = compareBuckets(other, newBucket);

            switch (cmp) {
                // These cases are all equivalent because the new bucket does
                // not straddle the edges of any existing buckets; so we can
                // simply add the contents of the new bucket to the existing one.
                case SAME_SAME :
                case MORE_LESS :
                case SAME_LESS :
                case MORE_SAME :
                    other.addContentsOf(newBucket);
                    break;

                    // If the new bucket is completely contained within an 
                    // existing bucket, we need to insert the new bucket between
                    // two buckets with the same content as other.
                case LESS_MORE :
                    Bucket rightHandBucket = insertBucket(other, newBucket);
                    it.add(newBucket);
                    it.add(rightHandBucket);
                    break;

                    // If the new bucket overlaps with the other, but 
                    // starts and ends LATER, we must create a new bucket
                    // for the overlapping portion.
                case LESS_LESS :
                    Bucket united1 = uniteBuckets(other, newBucket);
                    it.add(united1);
                    break;

                    // If the new bucket overlaps with the other, but 
                    // starts and ends SOONER, we must create a new bucket
                    // for the overlapping portion.
                case MORE_MORE :
                    Bucket united2 = uniteBuckets(newBucket, other);
                    it.previous();
                    it.add(united2);
                    it.next();
                    break;

                    // If the new bucket starts AFTER the existing one, but
                    // ends at the same point, just truncate the existing
                    // bucket and insert the new one into the gap.
                case LESS_SAME :
                    newBucket.addContentsOf(other);
                    other.endTime = newBucket.startTime;
                    it.add(newBucket);
                    break;

                    // If the new bucket starts at the same point as the
                    // existing one, but ends BEFORE it, truncate the existing
                    // bucket and insert the new one into the gap.
                case SAME_MORE :
		    // special case for instantaneous elements to avoid loop
		    if (newBucket.startTime==newBucket.endTime) {
		    	other.addContentsOf(newBucket);
		    } else {
			newBucket.addContentsOf(other);
			other.startTime = newBucket.endTime;
			it.previous();
			it.add(newBucket);
		    }
                    break;

                    // The above cases should be exhaustive, anything else
                    // is treated as an error.
                default :
                    throw new IllegalStateException("Unexpected comparison result");
            }

        }

    }

    return;

}

/**
 * Get the set of timed objects which overlap the specified time.
 * 
 * @param time The time at which objects should be found.
 * 
 * @return 	A <code>Set</code> of matching objects, or an empty
 * 			set for no matches.
 */
public Set getObjectsForTime(double time) {
    Bucket matchingBucket = findBucketForTime(time);
    if (matchingBucket != null && matchingBucket.objects!=null) {
        return Collections.unmodifiableSet(matchingBucket.objects);
    } else {
        return Collections.EMPTY_SET;
    }
}

/**
 * Get a {@see TimeIntervalIterator} object which allows efficient
 * iteration across the contents of this <code>TimeIntervalMapper</code>.
 * 
 * @return TimeIntervalIterator
 */
public TimeIntervalIterator getTimeIntervalIterator() {
    return new BucketIterator();
}

/**
 * Utility method to dump the internal "bucket" data structures used
 * by this <code>TimeIntervalMapper</code>.
 */
public void dumpBuckets() {
    for (Iterator it = buckets.iterator(); it.hasNext();) {
        Bucket b = (Bucket) it.next();
        System.out.println(b);
    }
}

//////////////////////////////////////////////////////////////////////////
// Internal Utilities
//////////////////////////////////////////////////////////////////////////

/**
 * Unite two overlapping buckets, producing a third bucket which contains
 * the overlap.
 * 
 * @param b1 The first bucket.
 * @param b2 The second bucket.
 * 
 * @return The bucket uniting the overlap of <code>b1</code> and <code>b2</code>.
 */
private Bucket uniteBuckets(Bucket b1, Bucket b2) {

    // Check that b2 >= b1.
    if (b2.startTime < b1.startTime) {
        throw new IllegalArgumentException("Argument b2 must start at or after b1");
    }

    // Create a new bucket with the contents of both b1 and b2.
    Bucket unitedBucket = new Bucket(b2.startTime, b1.endTime);
    unitedBucket.addContentsOf(b1);
    unitedBucket.addContentsOf(b2);

    // Move b1 and b2 aside to accomodate new bucket.
    b1.endTime = unitedBucket.startTime;
    b2.startTime = unitedBucket.endTime;

    return unitedBucket;
}

/**
 * Given an existing Bucket <code>existing</code> and a new bucket
 * <code>newBucket</code> which is <b>entirely contained</b> within
 * <code>existing</code> modify the existing bucket and new buckets
 * appropriately so that they become disjoint; and produce a further
 * "filler" bucket which should be inserted to the right of the 
 * new bucket.
 * 
 * @param existing		An existing <code>Bucket</code>.
 * @param newBucket		A new, completely contained <code>Bucket</code>.
 *
 * @return The right-hand filler bucket.
 */
private Bucket insertBucket(Bucket existing, Bucket newBucket) {
    // Check for proper containment.
    if (!existing.containsBucket(newBucket)) {
        throw new IllegalArgumentException("New bucket must be strictly contained.");
    }

    //System.out.println("CONTAINMENT");

    // Create a new bucket with the same contents as existing bucket,
    // to go on the other side of the new bucket.
    Bucket fillerBucket = new Bucket(newBucket.endTime, existing.endTime);
    if (existing.objects != null) {
        fillerBucket.objects = new HashSet(existing.objects);
    }

    // Move the end of existing bucket back to before the new one.
    existing.endTime = newBucket.startTime-TINY_TIME;

    // Add the contents of the existing bucket to the new one.
    newBucket.addContentsOf(existing);

    return fillerBucket;
}

/**
 * Ensure that the maximum bucket start and end times encompass those
 * of the supplied bucket <code>b</code>.
 * 
 * @param b Some <code>Bucket</code>. 
 */
private void extendTimeLine(Bucket b) {
    if (b.startTime < minStartTime) {
        minStartTime = b.startTime;
    }
    if (b.endTime >= maxEndTime) {
        maxEndTime = b.endTime;
    }
}

/**
 * Find the indices, within the full bucket list, of the first and last
 * <code>Bucket</code>s which overlap the supplied range as specified by
 * a start and end time. The result is returned in a two element array.
 * 
 * @param startTime The start time of the range.
 * @param endTime	The end time of the range.
 * 
 * @return Two-element integer array containing start and end indices.
 */
private int[] findOverlappingBucketIndices(double startTime, double endTime) {
    int[] indices = new int[2];

    // Iterate over all the buckets and record the first and
    // last matching ones.
    boolean foundfirst=false;
    for (ListIterator it = buckets.listIterator(); it.hasNext();) {
        int currentIndex = it.nextIndex();
        Bucket bucket = (Bucket) it.next();

        // If a bucket contains the start time, record it.
        if (bucket.containsTime(startTime)) {
            indices[0] = currentIndex;
	    foundfirst=true;
        }

        // If a bucket contains the end time, record it and finish.					
        if (bucket.containsTimeInclusive(endTime)) {
            indices[1] = currentIndex;
	    // this check was added by Jonathan 29.8.5. There was some
	    // buggy behaviour here as it's possible to have not
	    // located the start time bucket but to get here (i.e. you
	    // get an unassigned start bucket: bucket zero!)
	    if (foundfirst==false) {
		indices[0]=currentIndex;
	    }
            return indices;
        }
    }

    // Return null if we found nothing.
    return null;
}

/**
 * Find the Bucket which contains a particular time.
 * 
 * @param time The time to look for.
 *
 * @return The matching Bucket.
 */
private Bucket findBucketForTime(double time) {
    for (Iterator it = buckets.iterator(); it.hasNext();) {
        Bucket bucket = (Bucket) it.next();
        if (bucket.containsTime(time)) {
            return bucket;
        }
    }
    return null;
}

/**
 * Determine whether the supplied time is outside the range of all
 * the buckets known to this <code>TimeIntervalMapper</code>.
 */
private boolean isTimeOutsideAllBuckets(double time) {

    Bucket firstBucket = (Bucket) buckets.getFirst();
    Bucket lastBucket = (Bucket) buckets.getLast();
    return time < firstBucket.startTime || time >= lastBucket.endTime;

}

/**
 * Compare two <code>Bucket</code>s to produce a single comparison
 * integer, one of the constants described above.
 * 
 * @param b1 The first bucket.
 * @param b2 The second bucket.
 * 
 * @return A bucket comparison code.
 */
private int compareBuckets(Bucket b1, Bucket b2) {
    int cmpStart = Double.compare(b1.startTime, b2.startTime);
    int cmpEnd = Double.compare(b1.endTime, b2.endTime);

    int result = 0;

    if (cmpStart < 0) {
        result += 0 << 4;
    } else if (cmpStart == 0) {
        result += 1 << 4;
    } else if (cmpStart > 0) {
        result += 2 << 4;
    }

    if (cmpEnd < 0) {
        result += 0;
    } else if (cmpEnd == 0) {
        result += 1;
    } else if (cmpEnd > 0) {
        result += 2;
    }

    return result;
}

/**
 * An internal implementation of the {@see TimeIntervalIterator}
 * interface, used to provide efficient support for iteration
 * across the timeline described by a <code>TimeIntervalMapper</code>,
 * especially in the case where iteration moves forwards slowly.
 */
private class BucketIterator implements TimeIntervalIterator {

    private double currentTime = 0;
    private double currentStartTime = 0;
    private double currentEndTime = 0;
    private ListIterator iterator = null;
    private boolean isTimeSet = false;
    private boolean isTimeSpanSet = false;
    private Bucket lastConsideredBucket = null;

    /** Set a single time-point to be found */
    public void setTime(double time) {
	/*
        currentStartTime = time;
        currentEndTime = time;
	currentTime = 0;
        isTimeSet = false;
        isTimeSpanSet = true;
	*/

        if (isTimeSet && time < currentTime) {
            iterator = null;
        }
        currentTime = time;
        isTimeSet = true;
        isTimeSpanSet = false;

    }

    /** Set a time span to be found */
    public void setTimes(double stime, double etime) {
        currentStartTime = stime;
        currentEndTime = etime;
	currentTime = 0;
        isTimeSet = false;
        isTimeSpanSet = true;
    }

    /** return the current time of the iterator */
    public double getTime() {
        if (!isTimeSet) {
            throw new IllegalStateException("Time has not been set");
        }
        return currentTime;
    }

    /** return the set of objects at the time point (or time span) given */
    public Set getMatchingObjects() {
	if (isTimeSpanSet) {
	    return getMatchingObjectsInSpan();
	}

	//System.out.println("Get matching objects");

        if (!isTimeSet) {
            throw new IllegalStateException("Time has not been set");
        }
	
        if (buckets.size() > 0) {

            // Don't bother doing any work if the time is outside the
            // entire set of buckets.
            if (isTimeOutsideAllBuckets(currentTime)) {
                return Collections.unmodifiableSet(Collections.EMPTY_SET);
            }

            // If necessary, get the iterator.
            if (iterator == null) {
                iterator = buckets.listIterator();
            }

            while (iterator.hasNext()) {
                Bucket bucket = (Bucket) iterator.next();

                if (bucket.containsTime(currentTime)) {
                    // Rewind the iterator so that the next time round,
                    // we will start with this bucket.
                    iterator.previous();

                    // Remember which bucket we found.					
                    lastConsideredBucket = bucket;

                    // Return unmodifiable wrapper over matching bucket's
                    // object set.
                    if (bucket.objects != null) {
                        return Collections.unmodifiableSet(bucket.objects);
                    } else {
                        return Collections.unmodifiableSet(Collections.EMPTY_SET);
                    }

                }
            }
        }
        iterator = null;
        return Collections.unmodifiableSet(Collections.EMPTY_SET);
    }

    /** return the set of objects within the time span.  */
    private Set getMatchingObjectsInSpan() {
	Set obs = new HashSet();
	boolean started=false;

        if (!isTimeSpanSet) {
            throw new IllegalStateException("Time span has not been set");
        }

	//System.out.println("Get matching objects in span");
	
        if (buckets.size() > 0) {
            // Don't bother doing any work if the time is outside the
            // entire set of buckets.
            if (isTimeOutsideAllBuckets(currentStartTime) && isTimeOutsideAllBuckets(currentEndTime)) {
                return Collections.unmodifiableSet(Collections.EMPTY_SET);
            }

            // If necessary, get the iterator.
            if (iterator == null) {
                iterator = buckets.listIterator();
            }

	    // Note that I believe buckets are always in increasing
	    // temporal order so I stop when I reach the first
	    // non-matching one after some matches.
            while (iterator.hasNext()) {
                Bucket bucket = (Bucket) iterator.next();

		//System.out.println("Bucket: " + bucket.startTime + "; " + bucket.endTime);
		if (bucket.containsTime(currentStartTime) || bucket.containsTime(currentEndTime) ||
		    (bucket.isAfter(currentStartTime) && bucket.isBefore(currentEndTime))) {

                    // Remember which bucket we found last
                    lastConsideredBucket = bucket;

                    if (bucket.objects != null) {
			started=true;
			obs.addAll(bucket.objects);
                    } 
                } else if (started) {
		    break;
		} 
            }
	    
	    if (started) {
		// Rewind the iterator so that the next time round,
		// we will start with this bucket.
		iterator.previous();
	    }

	    // Return unmodifiable wrapper over matching bucket's
	    // object set.
	    return Collections.unmodifiableSet(obs);	    
        }
        iterator = null;
        return Collections.unmodifiableSet(Collections.EMPTY_SET);
    }

    public boolean hasObjectSetChanged() {
        if (!isTimeSet) {
            throw new IllegalStateException("Time has not been set");
        }

        if (isTimeOutsideAllBuckets(currentTime)) {
            if (lastConsideredBucket != null) {
                lastConsideredBucket = null;
                return true;
            } else {
                return false;
            }
        } else {
            return lastConsideredBucket == null
                || !lastConsideredBucket.containsTime(currentTime);
        }
    }

}

/**
 * A bucket containing a set of objects, with a start and
 * end time. This is a lightweight internal data structure
 * whose members are manipulated directly. It has some utility
 * methods for comparisons against other buckets, etc.
 */
private class Bucket {
    public Set objects;
    public double startTime;
    public double endTime;
    public double duration;

    public Bucket(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = endTime - startTime;
    }

    /**
     * Test whether the object contains the specified time.
     */
    public boolean containsTime(double time) {

	// Jonathan - to allow instantaneous events we must allow end
	// time to be equal in all tests, not just start time.
	//return (time >= startTime && time < endTime);
	return (time >= startTime && time <= endTime);
    }


    public boolean containsTimeInclusive(double time) {
    	
        return ((time >= startTime && time <= endTime)) ;
    }

    public boolean isBefore(double time) {
	//        return (endTime < time);
        return (endTime < time);
    }

    public boolean isAfter(double time) {
	//return (startTime >= time);
	return (startTime > time);
    }

    public boolean isEmpty() {
        return (objects == null || objects.isEmpty());
    }

    public boolean startsBefore(Bucket other) {
        return startTime < other.startTime;
    }

    public boolean endsAfter(Bucket other) {
        return endTime > other.endTime;
    }

    public boolean hasSameExtent(Bucket other) {
        return (startTime == other.startTime) && (endTime == other.endTime);
    }

    public boolean containsBucket(Bucket other) {
	//        return (other.startTime > startTime) && (other.endTime < endTime);
        return (other.startTime >= startTime) && (other.endTime <= endTime);
    }

    public void addContentsOf(Bucket other) {
        if (objects == null) {
            objects = new HashSet();
        }
	//System.out.println("addContentsOf objects: " + objects + "; other objects: " + other.objects);
        if (other.objects != null) {
            objects.addAll(other.objects);
        }
    }

    public String toString() {
        return "(" + startTime + ", " + endTime + ", {" + objects + "})";
    }

}

}
