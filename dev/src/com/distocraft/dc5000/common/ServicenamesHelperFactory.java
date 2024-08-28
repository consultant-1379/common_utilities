package com.distocraft.dc5000.common;

public class ServicenamesHelperFactory {

	private static ServicenamesHelper _instance;
	
	public static ServicenamesHelper getInstance() {
		if (_instance == null) {
			_instance = new ServicenamesHelper();
		}
		return _instance;
	}
	
	public static void setInstance(ServicenamesHelper servicenamesHelper) {
		_instance = servicenamesHelper;
	}
	
}
