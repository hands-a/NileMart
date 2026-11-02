package org.route;

public enum Governorate {
    // GREATER_CAIRO (3)
    CAIRO("Cairo", Zone.GREATER_CAIRO),
    GIZA("Giza", Zone.GREATER_CAIRO),
    QALYUBIA("Qalyubia", Zone.GREATER_CAIRO),

    // ALEX_DELTA (8)
    ALEXANDRIA("Alexandria", Zone.ALEX_DELTA),
    BEHEIRA("Beheira", Zone.ALEX_DELTA),
    KAFR_EL_SHEIKH("Kafr El Sheikh", Zone.ALEX_DELTA),
    DAKAHLIA("Dakahlia", Zone.ALEX_DELTA),
    DAMIETTA("Damietta", Zone.ALEX_DELTA),
    GHARBIA("Gharbia", Zone.ALEX_DELTA),
    MONUFIA("Monufia", Zone.ALEX_DELTA),
    SHARQIA("Sharqia", Zone.ALEX_DELTA),

    // CANAL (3)
    PORT_SAID("Port Said", Zone.CANAL),
    ISMAILIA("Ismailia", Zone.CANAL),
    SUEZ("Suez", Zone.CANAL),

    // UPPER_EGYPT (8)
    FAIYUM("Faiyum", Zone.UPPER_EGYPT),
    BENI_SUEF("Beni Suef", Zone.UPPER_EGYPT),
    MINYA("Minya", Zone.UPPER_EGYPT),
    ASYUT("Asyut", Zone.UPPER_EGYPT),
    SOHAG("Sohag", Zone.UPPER_EGYPT),
    QENA("Qena", Zone.UPPER_EGYPT),
    LUXOR("Luxor", Zone.UPPER_EGYPT),
    ASWAN("Aswan", Zone.UPPER_EGYPT),

    // REMOTE (5)
    MATROUH("Matrouh", Zone.REMOTE),
    NORTH_SINAI("North Sinai", Zone.REMOTE),
    SOUTH_SINAI("South Sinai", Zone.REMOTE),
    RED_SEA("Red Sea", Zone.REMOTE),
    NEW_VALLEY("New Valley", Zone.REMOTE);

    private final String displayName;
    private final Zone zone;

    Governorate(String displayName, Zone zone) {
        this.displayName = displayName;
        this.zone = zone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Zone getZone() {
        return zone;
    }

    public static Governorate fromString(String input) {
        if (input == null || input.isBlank()) {
            throw new StoreException("Governorate cannot be empty.");
        }
        String clean = input.trim().replace("-", " ").replace("_", " ");
        for (Governorate g : values()) {
            if (g.name().equalsIgnoreCase(input.trim()) ||
                g.displayName.equalsIgnoreCase(clean) ||
                g.name().replace("_", " ").equalsIgnoreCase(clean)) {
                return g;
            }
        }
        throw new StoreException("Unknown governorate: " + input);
    }
}
