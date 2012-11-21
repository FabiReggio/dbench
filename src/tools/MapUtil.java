package tools;

import java.util.*;

public class MapUtil 
{
    // --- Fields ---
    
    // --- Constructors ---
    
    // --- Methods ---
    /**
     * Sorts HashMap by Value
     * @param map
     * @param ascending
     * @return
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> 
            sortByValue(Map<K, V> map, boolean ascending) 
    {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
                map.entrySet());
        
        if (ascending) {
            Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
                public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                    return (o1.getValue()).compareTo(o2.getValue());
                }
            });
        } else {
            Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
                public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                    return (o2.getValue()).compareTo(o1.getValue());
                }
            });
        }

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}

