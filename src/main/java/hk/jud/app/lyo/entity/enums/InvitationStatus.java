package hk.jud.app.lyo.entity.enums;



public enum InvitationStatus {
    NEW("NEW", "New", "bg-info"),
    INVITED("INVITED", "Invited", "bg-primary"),
    PENDING("PENDING","Pending", "bg-warning"),
    CONFIRMED("CONFIRMED","Confirmed", "bg-success"),
    DECLINED("DECLINED", "Declined", "bg-danger");

	 private final String value;       // internal value (e.g., for DB)
	 private final String label;        // display name (label)
	 private final String badgeClass;  // Bootstrap class

	 InvitationStatus(String value, String label, String badgeClass) {
	        this.value = value;
	        this.label = label;
	        this.badgeClass = badgeClass;
	    }

	    public String getValue() {
	        return value;
	    }

	    public String getLabel() {
	        return label;
	    }

	    public String getBadgeClass() {
	        return badgeClass;
	    }
    
    public static InvitationStatus fromValue(String value) {
        for (InvitationStatus status : values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown InvitationStatus value: " + value);
    }
}