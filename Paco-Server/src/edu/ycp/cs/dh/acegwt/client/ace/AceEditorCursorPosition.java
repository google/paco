package edu.ycp.cs.dh.acegwt.client.ace;

/**
 * Represents a cursor position.
 */
public class AceEditorCursorPosition {
	private final int row, column;
	
	/**
	 * Constructor.
	 * 
	 * @param row     row (0 for first row)
	 * @param column  column (0 for first column)
	 */
	public AceEditorCursorPosition(int row, int column) {
		this.row = row;
		this.column = column;
	}
	
	/**
	 * @return the row (0 for first row)
	 */
	public int getRow() {
		return row;
	}
	
	/**
	 * @return the column (0 for first column)
	 */
	public int getColumn() {
		return column;
	}
	
	@Override
	public String toString() {
		return row + ":" + column;
	}
}
