package com.distocraft.dc5000.etl.engine.system;

import java.io.Serializable;
import java.util.List;

/**
 * @author epiituo
 */
public class SetStatusTO implements Serializable {
	
	/**
	 * A generated UID
	 */
	private static final long serialVersionUID = -686139162498760815L;

	private String setStatus;
	private List<StatusEvent> statusEvents;
	
	public SetStatusTO(final String setStatus, final List<StatusEvent> statusEvents) {
		this.setStatus = setStatus;
		this.statusEvents = statusEvents;
	}

	public void setSetStatus(final String setStatus) {
		this.setStatus = setStatus;
	}

	public String getSetStatus() {
		return this.setStatus;
	}

	public void setStatusEvents(final List<StatusEvent> statusEvents) {
		this.statusEvents = statusEvents;
	}

	public List<StatusEvent> getStatusEvents() {
		return this.statusEvents;
	}
}
