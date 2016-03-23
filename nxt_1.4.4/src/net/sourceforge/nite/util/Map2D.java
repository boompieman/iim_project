package net.sourceforge.nite.util;

import java.util.*;

/**
 * This class is used to store Objects in a map with 2 keys, used very heavily in the TimespanAnalyser.
 * For each combination of 2 (ordered) keys, one entry can exist, just as with
 * the usual Map.
 * So, this is something like a very inefficient matrix, bad for performance, good 
 * for flexible construction and access.
 * Width and height information is not really correct: width returns the number of
 * keys that have been used as columnkeys, even if some of those keys are no longer
 * in use. (and the same for height). Only if you call clear() will this information 
 * be flushed and will w/h again be 0/0. But then your matrix is empty :)
 * Too bad, but since I don't need the correct w/h information now, I'm not going to 
 * fix it. Anybody feels like, go ahead!
 * @author Dennis Reidsma, UTwente
 */
public class Map2D {
    
    /**
     * Internally a 2 level Map is used. The first level is addressed as row, and stored Maps which are the columns.
     */
    private HashMap rows = new HashMap();
    private Set allColumnKeys = new HashSet();
    public Map2D() {
    }

    
    /**
     * Add a value to the Map2D for the given two keys. If an entry already existed for the given keys, it is returned.
     */
    public Object put(Object rowKey, Object columnKey, Object value) {
		Map column = (Map)rows.get(rowKey);
		if (column == null) {
		    //System.out.println("new row: " + rowKey);
		    //System.out.println("existing data: " + rows);
		    column = new HashMap();
		    rows.put(rowKey,column);
		}
		Object result = column.put(columnKey, value);
		allColumnKeys.add(columnKey);
		return result;
    }
    /**
     * returns the value in the Map2D for the given two keys. 
     */
    public Object get(Object rowKey, Object columnKey) {
        if ((rowKey==null)||(columnKey==null)) {
            System.out.println("Map2D.get: one of the keys is null: "+ rowKey + "," + columnKey);
        }
//        System.out.println(rows);
//        System.out.println(allColumnKeys);
//        System.out.println("get from map2d: " +  rowKey.getClass().getName() +rowKey + "," + columnKey.getClass().getName() + columnKey);
//        System.out.println("column: " +  rows.get(rowKey));
        
		Map column = (Map)rows.get(rowKey);
		if (column == null) {
		    return null;
		}
		return column.get(columnKey);
    }    

    public void clear() {
        rows.clear();
        allColumnKeys.clear();
    }
    /**
     * returns all rowkeys in the tree
     */
    public Set rowKeySet() {
    	return rows.keySet();
    }
    /**
     * returns all columnkeys in the tree
     */
    public Set columnKeySet() {
    	return allColumnKeys;
    }    
    /**
     * Returns al values stored in the Map2D, without duplicates.
     */
    //    public Set values() {
    //    	Set result = new HashSet();
    //    	for (int i = 0; i < rows.size(); i++) {
    //    	    result.addAll(((Map)rows.get(i)).values());
    //    	}
    // 	    return result;
    //    }

    
    /**
     * 
     * @return boolean true iff the tree contains the specified rowkey
     */
    public boolean containsRowKey(String key) {
    	return rows.containsKey(key);
    }
    
    /**
     * Removes all entries for the given key
     */
    public Object remove(Object rowKey, Object columnKey) {
    	Map column = (Map)rows.get(rowKey);
		if (column == null) {
		    return null;
		}
		return column.remove(columnKey);
    }
    
    /**
     * Removes all entries in the given row
     */
    public void removeRow(Object rowKey) {
    	rows.remove(rowKey);
    }
//    /**
//     * Removes all entries in the given column
//     */
//    public void removeColumn(String columnKey) {
//        for (int i = 0; i < rows.size(); i++) {
//    	    ((Map)rows.get(i)).remove(columnKey);
//    	}
//    }
    

    /**
     * @return w 
     */
    public int width(){ 
        return allColumnKeys.size();
    }
    
    /**
     * @return h 
     */
    public int height(){ 
        return rows.size();
    }
    
    
    /**
     * For debug: dump the matrix to text.
     */
    public String dumpToString() {
        String result = "";
        List rKeys = new ArrayList(rows.keySet());
        List cKeys = new ArrayList(allColumnKeys);
        Collections.sort(rKeys);
        Collections.sort(cKeys);
        result += "|| ||";
        for (int j = 0; j < cKeys.size();j++) {
            Object ck = cKeys.get(j);
            result +=ck+"||";
        }
        result +="\n";
        for (int i = 0; i < rKeys.size();i++) {
            Object rk = rKeys.get(i);
            result += "||"+rk+"||";
            for (int j = 0; j < cKeys.size();j++) {
                Object ck = cKeys.get(j);
                Object o = get(rk,ck);
                if (o==null) {
                    result +="0||";
                } else {
                    result +=o.toString()+"||";
                }
            }
            result +="\n";
        }
        return result;
    }
    
}
