package com.distocraft.dc5000.common;

import static org.junit.Assert.*;
import java.util.logging.Logger;
import junit.framework.JUnit4TestAdapter;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for the HtmlEntities class in com.distrocraft.dc5000.common. <br>
 * <br>
 * Testing if different HTML tags are converted into characters and the other
 * way around.
 * 
 * @author EJAAVAH
 */
public class HtmlEntitiesTest {

	private static final int ASCII_VALUE_FOR_HALF_SYMBOL = 189;

	private static final int ASCII_VALUE_FOR_EURO_SYMBOL = 8364;

	private static final int ASCII_VALUE_FOR_A_WITH_UMLAUT_SYMBOL = 196;

	private static char halfSymbol;

	private static char aWithUmlautSymbol;

	private static char euroSymbol;

	private static Logger log;

	@BeforeClass
	public static void setUpBeforeClass() {

		log = Logger.getLogger("testLogger");

		// cannot use the actual euro or half symbols, as these don't map correctly
		// in unix
		euroSymbol = (char) new Integer(ASCII_VALUE_FOR_EURO_SYMBOL).intValue();
		halfSymbol = (char) new Integer(ASCII_VALUE_FOR_HALF_SYMBOL).intValue();
		aWithUmlautSymbol = (char) new Integer(ASCII_VALUE_FOR_A_WITH_UMLAUT_SYMBOL).intValue();
	}

	/**
	 * Testing if different html tags are converted to symbolic characaters.
	 */
	@Test
	public void testConvertHtmlEntities() {
		final String TestString = "&Testing html converter; euro - &euro;, Auml - &Auml;, frac12 - &frac12; &#x48; &#48;";
		assertEquals("&Testing html converter; euro - " + euroSymbol + ", Auml - " + aWithUmlautSymbol + ", frac12 - "
				+ halfSymbol + " H 0", HtmlEntities.convertHtmlEntities(TestString, log));
	}

	/**
	 * Testing if different symbolic characters are converted into HTML tags.
	 */
	@Test
	public void testCreateHtmlEntities() {
		// Test for the html creator (symbolic chars to html tags)
		final String TestString = euroSymbol + "-euro," + aWithUmlautSymbol + "-Auml";
		assertEquals("&euro;-euro,&Auml;-Auml", HtmlEntities.createHtmlEntities(TestString));
	}

	// Making the test work with ant 1.6.5 and JUnit 4.x
	/*public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(HtmlEntitiesTest.class);
	}*/ //Commented out because CI makes it redundant.
}
