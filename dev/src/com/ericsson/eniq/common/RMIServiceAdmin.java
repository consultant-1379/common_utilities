package com.ericsson.eniq.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.eniq.RMIServerAdminUtils.Command;
import com.ericsson.eniq.RMIServerAdminUtils.GetNamesBoundToRMICommand;

public class RMIServiceAdmin {

	private static final Map<String, Class<? extends Command>> CMD_TO_CLASS = new HashMap<String, Class<? extends Command>>();
	
	public static void main(String[] args) {
		try {
			//System.setSecurityManager(new com.distocraft.dc5000.etl.engine.ETLCSecurityManager());

			if (args.length < 1) {
				showUsage();
			} else {
				final String commandName = args[0];
				final Command command = createCommand(commandName, args);
				command.validateArguments();
				command.performCommand();
			}
			} catch (final Exception e) {
			System.err.println("General Exception: "+ e);
			e.printStackTrace(System.err);
			System.exit(1);
			}
		
		
		System.exit(0);	

	}
	
	private static void showUsage() {
		System.out.println("Usage: rmiregistry -e command");
		System.out.println("  commands:");
		System.out.println("    getNameBoundToRMI <hostname> <port number>");
		System.exit(1);
	}
	
	static Command createCommand(final String commandName, final String[] args) throws IllegalArgumentException,
	InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException,
	NoSuchMethodException {
		final Class<? extends Command> classToUse = CMD_TO_CLASS.get(commandName);
		if (classToUse == null) {
			throw new NoSuchMethodException("Invalid command entered: " + commandName);
			}
		final Class<? extends String[]> class1 = args.getClass();
		final Constructor<? extends Command> constructor = classToUse.getConstructor(class1);
		final Object constArguments = args;
		return constructor.newInstance(constArguments);
	}
	
	static {
		CMD_TO_CLASS.put("getNameBoundToRMI", GetNamesBoundToRMICommand.class);
	}

}
