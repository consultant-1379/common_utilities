package com.distocraft.dc5000.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on Jan 21, 2005
 * 
 * @author lemminkainen
 */
public class ProcessedFiles {

  private static Logger log = Logger.getLogger("etlengine.common.ProcessedFiles");

  private final String processedDir;

  private final String fileNameFormat;

  private final Map<String,Vector<String>> processedFilesMap = Collections.synchronizedMap(new HashMap<String,Vector<String>>());

  private static Map<String,SoftReference<Set<String>>> cache = new HashMap<String,SoftReference<Set<String>>>();

  /**
   * Initializes a ProcessedFiles object.
   * add
   * @throws Exception
   *           on intialization failure
   */
  public ProcessedFiles(final java.util.Properties conf) {

    log.finest("initializing...");

    fileNameFormat = conf.getProperty("ProcessedFiles.fileNameFormat");
    log.finest("  fileNameFormat: " + this.fileNameFormat);

    processedDir = getProcessedDir(conf.getProperty("ProcessedFiles.processedDir"));
    log.finest("  processedDir: " + this.processedDir);
    
    final File procDir = new File(this.processedDir);

    if (!procDir.exists()) {

      log.finer("Trying to create processedDir " + processedDir);

      procDir.mkdirs();

    }

  }

  public String getProcessedDir(final String dir) {
    
  	String processedDir = dir;
  	
    if (processedDir.endsWith(File.separator)) {
      processedDir = processedDir.substring(0, processedDir.length() - 1);
    }
      
    if (processedDir.indexOf("${") >= 0) {
      final int sti = processedDir.indexOf("${");
      final int eni = processedDir.indexOf("}", sti);

      if (eni >= 0) {
        final String variable = processedDir.substring(sti + 2, eni);
        final String val = System.getProperty(variable);
        final String result = processedDir.substring(0, sti) + val + processedDir.substring(eni + 1);
        processedDir = result;
      }
    }
    
    return processedDir;
    
  }
  
  /**
   * Method receives a file name and looks it from a processedList.
   * 
   * @param fileDesc
   *          Name of the file which is looked for from a text file
   */
  public boolean isProcessed(final String fileDesc, final String source) throws IOException {

    log.finest("isProcessed " + fileDesc);

    String fileListName = "";

    try {

      fileListName = parseFileName(fileDesc, source);

    } catch (FileNotFoundException e) {
      log.warning("Could not parse identifier from filename: " + fileDesc + ", no doublicate check is done! ");
      throw (e);
    }

    log.finest("check from processed map.");
    final Vector<String> vec = processedFilesMap.get(fileListName);
    if (vec != null && vec.contains(fileDesc)) {
      log.finer("file is processed");
      return true;
    }

    Set<String> fileList = null;

    synchronized (cache) {

      SoftReference<Set<String>> sr = cache.get(fileListName);

      // no cache entry or soft reference broken
      if (sr == null || (fileList = sr.get()) == null) {
        fileList = readFileList(fileListName);
        sr = new SoftReference<Set<String>>(fileList);
        cache.put(fileListName, sr);
      }

    }

    log.finest("check from cache");
    if (fileList.contains(fileDesc)) {
      log.finer("file is processed");
      return true;
    }

    return false;
  }

  /**
   * Method receives a name of file and adds this name to fileList and text file
   * The name of the text file is same than a date string which is parsed from
   * the name of the received file.
   * 
   * @param fileDesc
   *          The name of the file which is added to correct text file
   */
  public void addToProcessed(final String fileDesc, final String source) throws FileNotFoundException {

    log.finest("addToProcessed " + fileDesc);
    String fileListName = "";

    try {

      fileListName = parseFileName(fileDesc, source);

      synchronized (cache) {

        // update cache

        final SoftReference<Set<String>> sr = cache.get(fileListName);

        if (sr != null) { // we have cached entry

          final Set<String> fileList = sr.get();

          if (fileList != null) { // found from cache
            fileList.add(fileDesc);
          } else { // soft reference is already gone
            cache.remove(fileListName);
          }
        }

      }

      log.finer("Adding " + fileDesc + " to processed");
      
      synchronized(processedFilesMap) {

      if (processedFilesMap.containsKey(fileListName)) {

        processedFilesMap.get(fileListName).add(fileDesc);

      } else {

        final Vector<String> vec = new Vector<String>();
        vec.add(fileDesc);
        processedFilesMap.put(fileListName, vec);
      }
      
      } 

    } catch (FileNotFoundException e) {
      log.warning("Could not parse identifier from filename: " + fileDesc + ", file is NOT added to processed! ");
      throw (e);
    }
  }

  /**
   * Writes the processed files to a textfile.
   */
  public void writeProcessedToFile() {

    final Iterator<String> keys = processedFilesMap.keySet().iterator();

    log.log(Level.INFO, "Writing filenames to "+ processedFilesMap.size() +" files");
    
    while (keys.hasNext()) {

      final String fileListName = keys.next();
      final Vector<String> vec = processedFilesMap.get(fileListName);
      FileOutputStream output = null;

      try {

        //output = new FileOutputStream(processedDir + "/" + fileListName, true);
        output = new FileOutputStream(fileListName, true);
        log.finer("Opening file: " + fileListName);
        final PrintWriter writer = new PrintWriter(output);

        final Iterator<String> values = vec.iterator();
        while (values.hasNext()) {
          String fileDesc = "";
          try {
            fileDesc = values.next();
            log.finer("Appending: " + fileDesc);
            writer.println(fileDesc);
          } catch (Exception e) {
            log.log(Level.WARNING, "Error while appending " + fileDesc + " to file: " + fileListName, e);
          }
        }

        writer.close();
      } catch (Exception e) {

        log.log(Level.WARNING, "Error while opening/writing to/closing file: " + fileListName, e);

      } finally {

        if (output != null) {
          try {
            output.close();
          } catch (Exception e) {
            log.log(Level.WARNING, "Error closing file", e);
          }
        }
      }
    }

    processedFilesMap.clear();
    
    log.log(Level.INFO, "Done writing processed files");
    
  }

  /**
   * Method returns all file names from a single text file. The name of the text
   * file which content is required is parsered from the received dateTime
   * string.
   * 
   * @deprecated
   * @param fileListName
   *          Filename of fileList.
   * @return Set of file names included in which are processed and added to
   *         correct text file
   */
  public Set<String> getLoadedFiles(final String fileListName) {

    log.finest("getLoadedFiles");

    try {
      return readFileList(fileListName);
    } catch (Exception e) {
      return new HashSet<String>();
    }

  }

  /**
   * Extracts the fileList filename from specified filename based on filename
   * pattern
   */
  private String parseFileName(final String fileName, final String source) throws FileNotFoundException {

    try {

      final Pattern pattern = Pattern.compile(fileNameFormat);
      final Matcher matcher = pattern.matcher(fileName);

      if (matcher.matches()) {
        final String listFileName = source + "_" + matcher.group(1) + ".txt";
        log.finest("parsedFileName: \"" + listFileName + "\"");

        return listFileName;
      } else {
        throw new FileNotFoundException("FileName " + fileName + " doesn't match defined pattern " + fileNameFormat);
      }

    } catch (IndexOutOfBoundsException iob) {
      throw new FileNotFoundException("Filename parsing failed. No caption group defined in fileNameFormat.");
    }

  }

  /**
   * Reads processed fileList to Set.
   * 
   * @throws Exception
   *           in case of failure
   * @return loaded Map
   */
  private Set<String> readFileList(final String listFileName) throws FileNotFoundException, IOException {

    log.finest("Trying to read from " + listFileName);

    final Set<String> set = new HashSet<String>();

    if (listFileName == null || listFileName.length() <= 0) {
      log.info("Tried to readProcessed from empty file.");
      return set;
    }

    final File checkedDir = new File(this.processedDir);

    if (checkedDir.isDirectory() && checkedDir.canRead()) {

      //File checkedFile = new File(this.processedDir + File.separator + listFileName);
      final File checkedFile = new File(listFileName);

      if (checkedFile.exists() && checkedFile.canRead()) {

        log.finest("Load information file exists: " + checkedFile);

        BufferedReader reader = null;

        try {

          reader = new BufferedReader(new FileReader(checkedFile));

          String input;

          int lines = 0;
          while ((input = reader.readLine()) != null) {
            set.add(input);
            lines++;
          }

          log.fine("Read " + lines + " processed files from list");

        } finally {
          if (reader != null) {
            try {
              reader.close();
            } catch (Exception e) {
              log.log(Level.WARNING, "Error closing file", e);
            }
          }
        }

      } else {
        log.finest("Load information file not created yet");
      }

    }

    return set;

  }

}
