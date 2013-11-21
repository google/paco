package edu.ycp.cs.dh.acegwt.client.ace;

/**
 * Enumeration for ACE command types.
 */
public enum AceCommand {
	FIND("find"),
	FIND_NEXT("findnext"),
	FIND_PREVIOUS("findprevious"),
	GOTO_LINE("gotoline"),
	REPLACE("replace"),
	REPLACE_ALL("replaceall");

	private final String name;

	private AceCommand(final String name) {
		this.name = name;
	}

	/**
	 * @return the theme name (e.g., "error")
	 */
	public String getName() {
		return name;
	}
}
