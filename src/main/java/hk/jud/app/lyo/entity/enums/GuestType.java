package hk.jud.app.lyo.entity.enums;

public enum GuestType {
    SYSTEM("SYSTEM", "System", false),
    SENIOR_COUNCIL("SENIOR_COUNCIL", "Senior Council", true),
    GENERAL_GUEST1a("GENERAL_GUEST1a", "Guest with no tea reception (with RSVP)", true),
    GENERAL_GUEST1b("GENERAL_GUEST1b", "Guest with no tea reception (No RSVP)", true),
    GENERAL_GUEST2a("GENERAL_GUEST2a", "Guest with tea reception (with RSVP)", true),
    GENERAL_GUEST2b("GENERAL_GUEST2b", "Guest with tea reception (No RSVP)", true);

	private final String value;
    private final String label;
    private final boolean showInOption;

    GuestType(String value, String label, boolean showInOption) {
        this.value = value;
        this.label = label;
        this.showInOption = showInOption;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public boolean isShowInOption() {
        return showInOption;
    }

    public static GuestType fromValue(String value) {
        for (GuestType type : values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown GuestType value: " + value);
    }
    
}