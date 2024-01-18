package com.liferay.samples.fbo.segment.cache;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class RandomKeyUtil {

    public static int generateUniqueKey(Map<?, ?> map) {
        Map<?, ?> sortedMap = new TreeMap<>(map); // Sorts the map by keys
        int result = 17; // Starting with a non-zero constant

        for (Map.Entry<?, ?> entry : sortedMap.entrySet()) {
            int keyHash = (entry.getKey() == null) ? 0 : entry.getKey().hashCode();
            int valueHash;
            if("requestParameters".equals(entry.getKey()) || "cookies".equals(entry.getKey())) {
            	valueHash = Arrays.toString(((String[])entry.getValue())).hashCode();
            } else {
                valueHash = (entry.getValue() == null) ? 0 : entry.getValue().hashCode();
            }

            result = 31 * result + keyHash; // Combine hash of key
            result = 31 * result + valueHash; // Combine hash of value
        }

        return result;
    }
    
    public static int generateUniqueKey(long[] array) {
        if (array == null) {
            return 0;
        }

        int result = 17; // Starting with a non-zero constant
        for (long element : array) {
            int elementHash = Long.hashCode(element);
            result = 31 * result + elementHash; // 31 is a prime number
        }

        return result;
    }
    
}
