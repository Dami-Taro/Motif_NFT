package src.main;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Constants {

    private Constants() {}

    private static final Set<String> BURN_ENTITIES;

    static {
        Set<String> temp = new HashSet<>();
        temp.add("Burn Addresses");
        BURN_ENTITIES = Collections.unmodifiableSet(temp);
    }

    public static boolean isBURN(String entity) {
        if (entity == null) return false;

        String normalized = entity.trim(); //.toLowerCase();
        return BURN_ENTITIES.contains(normalized);
    }
}