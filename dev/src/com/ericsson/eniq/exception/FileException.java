package com.ericsson.eniq.exception;

/**
 * Generic exception for file related issues.
 * 
 * @author etuolem
 */
public class FileException extends Exception {

	public enum Reason {
		NOT_EXISTS, CANT_READ, CANT_WRITE, NOT_FILE, NOT_DIR
	}

	private final String fileName;
	private final Reason[] reasons;
	private final String message;

	/**
	 * FileException with predefined failure reason.
	 */
	public FileException(final String fileName, final Reason... reasons) {
		this.fileName = fileName;
		this.reasons = reasons;
		this.message = null;
	}

	/**
	 * FileException with predefined failure reason.
	 */
	public FileException(final String fileName, final Exception cause, final Reason... reasons) {
		super(fileName, cause);

		this.fileName = fileName;
		this.reasons = reasons;
		this.message = null;
	}

	/**
	 * FileException with message.
	 */
	public FileException(final String fileName, final String message) {
		this.fileName = fileName;
		this.reasons = new Reason[0];
		this.message = message;
	}

  public String getFilename(){
    return this.fileName;
  }

  public Reason[] getReasons(){
    return this.reasons;
  }

	/**
	 * Overwritten message method. Creates standard formed message.
	 */
	@Override
	public String getMessage() {
		final StringBuilder sb = new StringBuilder();

		if (message != null) {
			sb.append("    ").append(message);
		} else {
			for (Reason reason : reasons) {

				if (reason == Reason.NOT_EXISTS) {
					sb.append(" or does not exist");
				} else if (reason == Reason.NOT_FILE) {
					sb.append(" or is not a file");
				} else if (reason == Reason.NOT_DIR) {
					sb.append(" or is not a directory");
				} else if (reason == Reason.CANT_READ) {
					sb.append(" or cannot read");
				} else if (reason == Reason.CANT_WRITE) {
					sb.append(" or cannot write");
				}

			}
		}

		if (sb.length() > 0) {
			sb.delete(0, 3);
			sb.insert(0, " file");
		}
		sb.insert(0, "\"");
		sb.insert(0, fileName);
		sb.insert(0, "Error accessing file \"");

		return sb.toString();
	}

}
