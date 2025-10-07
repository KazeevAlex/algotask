package currency;

import java.util.Comparator;

public interface Nominal {
    Integer getNominal();

    static Comparator<Nominal> getComparator() {
        return Comparator.comparingInt(Nominal::getNominal).reversed();
    }
}
