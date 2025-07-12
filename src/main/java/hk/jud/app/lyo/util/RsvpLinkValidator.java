package hk.jud.app.lyo.util;

import java.util.regex.Pattern;

public class RsvpLinkValidator {

	private static final Pattern RSVP_LINK_PATTERN = Pattern.compile("^https?://[a-zA-Z0-9.-]+(/[a-zA-Z0-9._-]*)*$");

	public static boolean isValidRsvpLink(String rsvpLink) {
		if (rsvpLink == null || rsvpLink.trim().isEmpty()) {
			return false;
		}
		return RSVP_LINK_PATTERN.matcher(rsvpLink).matches();
	}

	public static String getValidationError(String rsvpLink) {
//		if (rsvpLink == null || rsvpLink.trim().isEmpty()) {
//			return "RSVP link cannot be empty.";
//		}
		if (rsvpLink != null && !rsvpLink.trim().isEmpty()) {
			if (!RSVP_LINK_PATTERN.matcher(rsvpLink).matches()) {
				return "RSVP link is invalid URL";
			}
		}
		return null;
	}
}