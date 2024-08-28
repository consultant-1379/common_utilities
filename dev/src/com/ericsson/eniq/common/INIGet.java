package com.ericsson.eniq.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * This function gets a variable from niq.ini file. Parameters are:<br />
 * <i>file</i> - Path to the niq.ini file.<br />
 * <i>section</i> - Name of the section.<br />
 * <i>parameter</i> - Parameter within that section.<br />
 * <br />
 * The parameter and it's value are set as a ANT project's property with the
 * following style:<br />
 * Property name is <i>section.parameter</i> and value is the value of this
 * section parameter.
 * 
 * @author Berggren
 * 
 */
public class INIGet {

	private String section = "";

	private String parameter = "";

	private String parameterValue = "";

	private String file = "";

	private static final int LINE_SEPARATOR_LENGTH = System.getProperty("line.separator").length();

	/**
	 * This function will start the execution of this ANT task.
	 */
	public void execute(final Logger log) {

		final String targetFilePath = this.file;
		final File targetFile = new File(targetFilePath);

		if (!targetFile.isFile() || !targetFile.canRead()) {
			log.severe("Could not read file " + targetFilePath + ". Please check that the file " + targetFilePath
					+ " exists and it can be read.");
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(targetFile));

			boolean readingCorrectSectionParameters = false;

			String line = null;

			while ((line = reader.readLine()) != null) {
				if (line.startsWith("[")) {
					if (line.startsWith("[" + this.section + "]")) {
						readingCorrectSectionParameters = true;
					} else {
						readingCorrectSectionParameters = false;
					}
				}

				if (readingCorrectSectionParameters) {
					if (line.startsWith(this.parameter + "=")) {
						// Correct section parameter is found.
						this.parameterValue = line.substring((this.parameter + "=").length(), line.length());
					}
				}
			}

		} catch (Exception e) {
			log.log(Level.SEVERE, "Reading of file " + targetFilePath + " failed.", e);
		} finally {
			try {
				reader.close();
			} catch(Exception e) {}
		}
	}

	/**
	 * Read and return contents of specified ini file 
	 * 
	 * @return Contents as a map of maps. First level map stores blocks. Second level map stores individual config settings within each block
	 * @throws IOException Errors reading ini files.
	 */
	public Map<String, Map<String, String>> readIniFile(final File iniFile) throws IOException {

		final RandomAccessFile raf = new RandomAccessFile(iniFile, "r");

		try {
			String line;
			final Map<String, Map<String, String>> iniContents = new HashMap<String, Map<String, String>>();

			// Step through file looking for next block of ini file - a block starts with an identifier
			// within square brackets e.g. [DWH_DBSPACES_MAIN]. Get contents of the block and store as
			// name-value pairs where name is the block identifier and the "value" is another map holding 
			// the config settings defined within the block
			
			while ((line = raf.readLine()) != null) {
				final String blockName = line.trim();

				if (blockName.length() == 0 || blockName.startsWith(";")) {
					continue;
				}

				if (blockName.matches("\\[.*\\]")) {
					final String name = blockName.substring(1, blockName.length() - 1);
					final Map<String, String> block = getIniBlock(raf);
					if (block != null && block.size()>0) {
						iniContents.put(name, block);
					}
				} else {
					System.out.println("?? '" + blockName + "'");
				}
			}

			return iniContents;
		} finally {
			try{
				raf.close();
			} catch (Throwable t){/*-*/}
		}

	}

	/**
	 * Reads the config settings for the next block of an ini file. 
	 * 
	 * @return A name-value map of the config settings within the block
	 * @throws IOException Errors reading ini files.
	 */
	private Map<String, String> getIniBlock(final RandomAccessFile raf) throws IOException {

		String line;
		final Map<String, String> block = new LinkedHashMap<String, String>();

		// step through each line of file to end-of-file or start of next block (whichever comes first)
		// for each line, split contents into name-value pairs and add to block map

		while ((line = raf.readLine()) != null) {
			final String trimmedLine = line.trim();

			if (trimmedLine.length() == 0 || trimmedLine.startsWith(";")) {
				continue;
			}

			if (trimmedLine.startsWith("[")) { // check the trimmed line (line read might have spaces at start...)
				// We're at the next block so step back a line and stop parsing this block
				raf.seek(raf.getFilePointer() - (line.length() + LINE_SEPARATOR_LENGTH)); // new line character
				break;
			}

			final String[] nvpair = trimmedLine.split("=");

			if (nvpair.length == 1) {
				block.put(nvpair[0], null);
			} else {
				block.put(nvpair[0], nvpair[1]);
			}
		}

		return block;

	} 

	public String getParameter() {
		return parameter;
	}

	public void setParameter(final String parameter) {
		this.parameter = parameter;
	}

	public String getSection() {
		return section;
	}

	public void setSection(final String section) {
		this.section = section;
	}

	public String getFile() {
		return file;
	}

	public void setFile(final String file) {
		this.file = file;
	}

	public String getParameterValue() {
		return parameterValue;
	}

	public void setParameterValue(final String parameterValue) {
		this.parameterValue = parameterValue;
	}
}
