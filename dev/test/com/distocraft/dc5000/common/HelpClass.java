package com.distocraft.dc5000.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class HelpClass {

	public String readFileToString(final File f) throws Exception {

		BufferedReader reader = null;
		String result = null;

		try {
			reader = new BufferedReader(new FileReader(f));
			String input;
			while ((input = reader.readLine()) != null) {
				result = input;
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					System.out.println("Error occured during closing the file");
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * Reads file f and returns content as string. Includes ALL lines in the
	 * file, using eol as the end of line character(s)
	 * 
	 * @param f
	 *            File to read from
	 * @param eol
	 *            End of Line character(s) used in returned string
	 * @return Contents of file as a string
	 * @throws Exception
	 */
	public String readFullFileToString(final File f, final String eol)
			throws Exception {

		BufferedReader reader = null;
		StringBuffer result = new StringBuffer();

		try {
			reader = new BufferedReader(new FileReader(f));
			String input;
			while ((input = reader.readLine()) != null) {
				result.append(input + eol);
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					System.out.println("Error occured during closing the file");
					e.printStackTrace();
				}
			}
		}
		return result.toString();
	}
}
