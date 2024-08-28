package com.ericsson.eniq.exception;

/**
 * Generic exception for configuration related issues.
 * 
 * @author etuolem
 */
public class ConfigurationException extends Exception {
	
	public enum Reason { MISSING, MALFORMED };
	
	private final String configLocation;
	private final String parameterName;
	private final Reason reason;
	private final String message;
	
	/**
	 * Constructor without message.
	 * 
	 * @param configLocation For example name of configuration file.
	 * @param parameterName Name of problematic parameter.
	 * @param reason Static reason indicator. Reasons are defined in this class as statics.
	 */
	public ConfigurationException(final String configLocation, final String parameterName, final Reason reason) {
		this(configLocation, parameterName, reason, null);
	}
	
	/**
	 * Constructor with message.
	 * 
	 * @param configLocation For example name of configuration file.
	 * @param parameterName Name of problematic parameter.
	 * @param reason Static reason indicator. Reasons are defined in this class as statics.
	 * @param message Specific message. For example description how parameter is malformed.
	 */
	public ConfigurationException(final String configLocation, final String parameterName, final Reason reason, final String message) {
		this.configLocation = configLocation;
		this.parameterName = parameterName;
		this.reason = reason;
		this.message = message;
	}
	
	/**
	 * Overwritten message method. Creates standard formed message.
	 */
	@Override
	public String getMessage() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Parameter ").append(parameterName);
		
		if(reason == Reason.MISSING) {
			sb.append(" is not defined in ").append(configLocation).append(".");
		} else if (reason == Reason.MALFORMED) {
			sb.append(" in ").append(configLocation).append(" is malformed.");
		}
		
		if (message != null) {
			sb.append(" ").append(message);
		}
				
		return sb.toString();
	}
	
}
