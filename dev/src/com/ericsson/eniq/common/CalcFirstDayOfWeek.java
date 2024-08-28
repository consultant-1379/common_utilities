/**
 * 
 */
package com.ericsson.eniq.common;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Class will be used to get the firstDayOfTheWeekk value from
 *  static.properties AdminUI and Monitoring classes will use this class
 *  to get firstDayOfTheWeek value
 *  @author eninkar
 */
public class CalcFirstDayOfWeek {

	private static Logger log = Logger.getLogger(CalcFirstDayOfWeek.class.getName());

	private CalcFirstDayOfWeek() {

	}

	public static int calcFirstDayOfWeek() {
		int firstDayOfWeek = 2;
		final Properties prop = new Properties();
		try {

			final File conffile = new File(System.getProperty("CONF_DIR"), "static.properties");
      final FileInputStream fis = new FileInputStream(conffile);
      try {
        prop.load(fis);
        firstDayOfWeek = Integer.parseInt(prop.getProperty("firstDayOfTheWeek", "2"));
        log.finest("firstDayOfWeek is: " + firstDayOfWeek);
      } finally {
        fis.close();
      }

		} catch (NumberFormatException e) {
			firstDayOfWeek = 2;
			log.log(Level.WARNING, "Static Property firstDayOfTheWeek not in range 1-7. Defaulted to 2 (Monday)");
		} catch (Exception e) {
			log.log(Level.INFO, "Error reading property file. ", e);
		}
		return firstDayOfWeek;
	}

}
