/**
 *
 */
package com.ericsson.eniq.common;

import com.distocraft.dc5000.common.StaticProperties;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author esunbal
 *         This class has helper methods common to various modules.
 */
public class CommonUtils {

  private CommonUtils(){}

  private static final Logger log = Logger.getLogger("etlengine.CommonUtils");

  public static final int NO_MOUNT_POINTS = 0;
  public static final String DEFAULT_SYSTEM_TYPE = "RAW";
  private static final File ROOT_PATH = new File("/");
  private static String regexDigits = "\\d+";

  public static final String ETLDATA_DIR = "ETLDATA_DIR";
  public static final String ETLDATA_DIR_DEFAULT = "/eniq/data/etldata";

  public static final String EVENTS_ETLDATA_DIR = "EVENTS_ETLDATA_DIR";
  public static final String EVENTS_ETLDATA_DIR_VAL = "/eniq/data/etldata_";

  public static final String DC_CONFIG_DIR_PROPERTY_NAME = "dc.config.dir";
  public static final String CONFIG_DIR_PROPERTY_NAME = "CONF_DIR";

  public static final String CONF_DIR_DEFAULT = "/eniq/sw/conf";

  public static final String DWH_INI_FILENAME = "dwh.ini";

  public static final String NIQ_INI_FILENAME = "niq.ini";
  
  public static final String CONF_DIR = "CONF_DIR";
  
  static {
    try {
      StaticProperties.reload();
    } catch (IOException e) {
      //Ignore.....
    } catch (RuntimeException e){
      //Ignore
    }
    try {
      regexDigits = StaticProperties.getProperty("etldata_digits", regexDigits);
    } catch (NullPointerException e) {
      // Ignore, use defaults.
    }
  }

  /*
   * This method reads the number of output directories from niq.ini file.
   * If any exception occurs or parameter is not defined then 0 is returned.
   * Section : DIRECTORY_STRUCTURE
   * Parameter : NoOfDirectories
   */
  public static Integer getNumOfDirectories(final Logger log) {

    final File iniFile = new File(System.getProperty("CONF_DIR"), "niq.ini");
    if(iniFile.exists()){
      try {
        final INIGet iniGet = new INIGet();
        iniGet.setFile(iniFile.getPath());
        iniGet.setSection("DIRECTORY_STRUCTURE");
        iniGet.setParameter("FileSystems");
        iniGet.execute(log);
        final Integer numOfDirectories = new Integer(iniGet.getParameterValue());

        if (numOfDirectories == null || numOfDirectories == 0) {
          return NO_MOUNT_POINTS;
        } else {
          return numOfDirectories;
        }

      } catch (Exception e) {
        log.fine("FileSystems parameters not available in niq.ini file. " +
          "Proceeding with single directory structure.");
        return NO_MOUNT_POINTS;
      }
    } else {
      log.fine("Couldn't read DIRECTORY_STRUCTURE.FileSystems parameters from niq.ini as the file " +
        "'"+iniFile+"' doesn't exist!");
      return NO_MOUNT_POINTS;
    }
  }
  
  /**
 * This method returns the type of system ZFS or RAW. The default is RAW.
 * @param log
 * @return
 */
//public static String getSystemType(final Logger log) {
//
//	    final File iniFile = new File(System.getProperty("CONF_DIR"), "niq.ini");
//	    if(iniFile.exists()){
//	      try {
//	        final INIGet iniGet = new INIGet();
//	        iniGet.setFile(iniFile.getPath());
//	        iniGet.setSection("SYSTEM_INFO");
//	        iniGet.setParameter("SYSTEM_TYPE");
//	        iniGet.execute(log);
//	        final String systemType = iniGet.getParameterValue();
//
//	        if (systemType == null) {
//	          return DEFAULT_SYSTEM_TYPE;
//	        } else {
//	          return systemType;
//	        }
//
//	      } catch (Exception e) {
//	        log.fine("SYSTEM_TYPE parameter not available in niq.ini file. " +
//	          "Assuming system to be zfs.");
//	        return DEFAULT_SYSTEM_TYPE;
//	      }
//	    } else {
//	      log.fine("Couldn't read SYSTEM_INFO.SYSTEM_TYPE parameter from niq.ini as the file " +
//	        "'"+iniFile+"' doesn't exist!");
//	      return DEFAULT_SYSTEM_TYPE;
//	    }
//	  }

  /**
   * Returns the expanded paths of the loader directories based on the no. of filesystems configured.
   * For e.g. Horizontal Scalability with 4 filesystems will have dirs as
   * </eniq/data/etldata/00/dc_e_cpp_nnisaaltp/raw>
   * </eniq/data/etldata/01/dc_e_cpp_nnisaaltp/raw>
   * </eniq/data/etldata/02/dc_e_cpp_nnisaaltp/raw>
   * </eniq/data/etldata/03/dc_e_cpp_nnisaaltp/raw>
   *
   * @param linkPath    file/dir path to expand (can contain ${} tokens)
   * @param expandCount The number of ${base} subdirs
   * @return List of files expanded.
   */
  public static List<File> expandEtlPathWithMountPoints(final String linkPath, final int expandCount) {
    return expandEtlPathWithMountPoints(linkPath, expandCount, true);
  }

  /**
   * Returns the expanded paths of the loader directories (etldata) based on the no. of filesystems configured.
   * For e.g. Horizontal Scalability with 4 filesystems will have dirs as
   * </eniq/data/etldata/00/dc_e_cpp_nnisaaltp/raw>
   * </eniq/data/etldata/01/dc_e_cpp_nnisaaltp/raw>
   * </eniq/data/etldata/02/dc_e_cpp_nnisaaltp/raw>
   * </eniq/data/etldata/03/dc_e_cpp_nnisaaltp/raw>
   * <p/>
   * This will only expand for sub directories of /eniq/data/etldata, all other directories are ignored.
   * /eniq/data/etldata itself is ignored and not expanded
   *
   * @param linkPath    file/dir path to expand (can contain ${} tokens)
   * @param expandCount The number of ${base} subdirs
   * @param prune       remove entries that dont exist
   * @return List of files expanded.
   */
  public static List<File> expandEtlPathWithMountPoints(final String linkPath,
                                                        final int expandCount, final boolean prune) {

    //Find out if the path is EVENTS(etldata_) or STATS(etldata) format
    final String stats = System.getProperty(ETLDATA_DIR, ETLDATA_DIR_DEFAULT);
    final String events = System.getProperty(EVENTS_ETLDATA_DIR, EVENTS_ETLDATA_DIR_VAL);
    final File etldata = new File(stats);
    final File etldata_ = new File(events);


    final String absolutePath = expandPathWithVariable(linkPath);
    final File absoluteFile = new File(absolutePath);

    final File etlDataDir;

    if(events.length() > 0 && absoluteFile.getPath().startsWith(etldata_.getPath())){
      etlDataDir = etldata_;
    } else {
      //stats
      etlDataDir = etldata;
    }

    final List<File> expandedFiles = new ArrayList<File>();
    if(etlDataDir.equals(absoluteFile)){
      //dont expand the etldir itself
      addDir(expandedFiles, absoluteFile, prune);
    } else if (NO_MOUNT_POINTS == expandCount ||
      !absoluteFile.getPath().startsWith(etlDataDir.getPath())) {
      addDir(expandedFiles, absoluteFile, prune);
    } else {
      final String etlRelPath = absolutePath.substring(etlDataDir.getPath().length());
      final String restOfPath = absolutePath.substring(etlDataDir.getPath().length());

      //Check if the path is already expanded, if it is just add it to the list and finish
      File tmp = new File(restOfPath);
      //work back up tree to get first dir in the rest of the path e.g.
      // if restOfPath == /00/dc_e_abc/ we're looking for 00
      while (tmp.getParentFile() != null && !tmp.getParentFile().equals(ROOT_PATH)) {
        tmp = tmp.getParentFile();
      }
      //tmp should now point to 00 (or some combination of digits) or something else
      final String nextDir = tmp.getName();
      if (nextDir != null && Pattern.matches(regexDigits, nextDir)) {
        //path already expanded...
        addDir(expandedFiles, absoluteFile, prune);
        return expandedFiles;
      }
      //tmp above wasn't all digits to expand awayyyyyy
      for (int i = 0; i < expandCount; i++) {
        String mountNumber = i <= 9 ? "0" : "";
        mountNumber += i;
        final File expandedDir = new File(new File(etldata_, mountNumber), etlRelPath);
        addDir(expandedFiles, expandedDir, prune);
      }
    }
    return expandedFiles;
  }

  @SuppressWarnings({"PMD.ConfusingTernary"})
  private static void addDir(final List<File> list, final File file, final boolean prune) {
    if (prune) {
      if (!file.exists()) {
        log.warning("Directory " + file + " doesn't exist!");
        return;
      } else if (!file.isDirectory()) {
        log.warning("Directory " + file + " isn't an actual directory!");
        return;
      } else if (!file.canRead()) {
        log.warning("Directory " + file + " isn't readable!");
        return;
      }
    }
    if(!list.contains(file)){
      list.add(file);
    }
  }

  /**
   * Expands paths that contain env/sys variables e.g.
   * ${ETLDATA_DIR}/abc would get expanded to /eniq/data/etldata/abc
   * Where -DETLDATA_DIR=/eniq/data/etldata
   *
   * @param varPath Path to expand
   * @return expanded path
   */
  public static String expandPathWithVariable(final String varPath) {
    if (varPath.contains("${")) {
      final int start = varPath.indexOf("${");
      final int end = varPath.indexOf("}", start);
      if (end >= 0) {
        final String variable = varPath.substring(start + 2, end);
        final String val = System.getProperty(variable);
        if (val == null) {
          log.warning("No Property/Value defined for ${" + variable + "} so can't expand path " + varPath);
        }
        return varPath.substring(0, start) + val + varPath.substring(end + 1);
      }
    }
    return varPath;
  }

	public static String getServerType() {
		File iniFile;
		// First look for dwh.ini file. If it isn't found, fall back to niq.ini
		iniFile = new File(System.getProperty(CONF_DIR, CONF_DIR_DEFAULT), DWH_INI_FILENAME);
		if (!iniFile.exists()) {
			iniFile = new File(System.getProperty(CONF_DIR, CONF_DIR_DEFAULT), NIQ_INI_FILENAME);
		}

		final INIGet iniGet = new INIGet();

		iniGet.setFile(iniFile.getPath());
		iniGet.setSection("ETLC");
		iniGet.setParameter("Server_Type");
		iniGet.execute(log);
		return iniGet.getParameterValue().toString();
	}
  
}

