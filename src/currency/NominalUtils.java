package currency;

import java.util.Map;
import java.util.TreeMap;

public final class NominalUtils {

    private NominalUtils() {}

    public static Map<Nominal, Integer> getSortedNominalMap(Map<Nominal, Integer> unsortedMap) {
        Map<Nominal, Integer> sortedMap = new TreeMap<>(Nominal.getComparator());
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }
}
