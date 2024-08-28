package com.ericsson.eniq.common.lwp;

import static junit.framework.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;
import com.ericsson.eniq.common.testutilities.ServicenamesTestHelper;

/**
 * User: eeipca
 * Date: 03/07/12
 * Time: 11:56
 */
public class LwpBase {
  private static final List<String> CWIN_OK =
    Arrays.asList("C:/Windows/system32/cmd.exe", "/c", "ping", "127.0.0.1", "-n", "1", "-w", "1000");
  private static final List<String> CWIN_ERROR =
    Arrays.asList("C:/Windows/system32/cweqwemd.exe", "/c", "ping", "127.0.0.1", "-n", "1", "-w", "1000");
  private static final List<String> CWIN_FMT_ERROR =
    Arrays.asList("C:/Windows/system32/cmd.exe", "/c", "ping", "127.0.0.1", "-n");

  private static final List<String> CNIX_OK =
    Arrays.asList("/usr/sbin/ping", "127.0.0.1", "1");
  private static final List<String> CNIX_ERROR =
    Arrays.asList("/usr/sbin/pasdasing", "127.0.0.1", "1");
  private static final List<String> CNIX_FMT_ERROR =
    Arrays.asList("/usr/sbin/ping", "-h", "1");
  
  private static final List<String> CLINUX_OK =
      Arrays.asList("/bin/ping", "127.0.0.1", "-c", "1");
    private static final List<String> CLINUX_ERROR =
      Arrays.asList("/bin/pasdasing", "127.0.0.1", "1");
    private static final List<String> CLINUX_FMT_ERROR =
      Arrays.asList("/bin/ping", "-h", "1");

  protected static final List<String> CMD_OK = getCmdOk();
  protected static final List<String> CMD_EXE_ERROR = getCmdExeError();
  protected static final List<String> CMD_ARGS_ERROR = getCmdArgsError();

  protected static final String registryHost = "localhost";
  protected static final int registryPort = 1201;
  protected static File TMP_DIR = null;

  private static Process registryProcess = null;


  @AfterClass
  public static void afterClass() {
    if (TMP_DIR != null) {
      DirectoryHelper.delete(TMP_DIR);
    }
  }


  private static List<String> getCmdOk(){
    String osName = System.getProperty("os.name");
    System.out.println(osName);
    if(osName.contains("Win")){
      return CWIN_OK;
    }else if(osName.contains("Lin")){
      return CLINUX_OK;
    } else{ 
      return CNIX_OK;
    }
  }
  
  private static List<String> getCmdExeError(){
    String osName = System.getProperty("os.name");
    System.out.println(osName);
    if(osName.contains("Win")){
      return CWIN_ERROR;
    }else if(osName.contains("Lin")){
      return CLINUX_ERROR;
    } else{ 
      return CNIX_ERROR;
    }
  }
  
  private static List<String> getCmdArgsError(){
    String osName = System.getProperty("os.name");
    System.out.println(osName);
    if(osName.contains("Win")){
      return CWIN_FMT_ERROR;
    }else if(osName.contains("Lin")){
      return CLINUX_FMT_ERROR;
    } else{ 
      return CNIX_FMT_ERROR;
    }
  }
  protected static void stopRegistry() {
    if (registryProcess != null) {
      registryProcess.destroy();
      try {
        registryProcess.waitFor();
      } catch (InterruptedException e) {
        throw new Error(e);
      }
      registryProcess = null;
    }
  }

  protected static void startRegistry() {
    stopRegistry();
    /*Start the RMI Registry as an external process, saves the hassle of weird GC interactions...*/
    final File javaHome = new File(System.getProperty("java.home"));
    final String extension = System.getProperty("path.separator").equals(";") ? ".exe" : "";
    final File rmiregistry = new File(javaHome, "bin/rmiregistry" + extension);

    if (!rmiregistry.exists()) {
      throw new Error(rmiregistry.getPath() + " not found");
    } else if (!rmiregistry.canExecute()) {
      throw new Error(rmiregistry.getPath() + " not executable");
    }
    final ProcessBuilder pBuilder = new ProcessBuilder(rmiregistry.getPath(), Integer.toString(registryPort));
    try {
      registryProcess = pBuilder.start();
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  protected static void setupCodebase() {
    /* Setup codebase RMI stuff */
    final String[] packages = ILWPHelperRMI.class.getName().split("\\.");
    URL url = ClassLoader.getSystemResource(ILWPHelperRMI.class.getName().replace(".", File.separator) + ".class");
    if (url == null) {
      fail("Cant find " + ILWPHelperRMI.class.getName() + " on classpath");
    }
    File classDir;
    try {
      classDir = new File(url.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    while (!classDir.getName().equals(packages[0])) {
      classDir = classDir.getParentFile();
    }
    classDir = classDir.getParentFile();
    System.setProperty("java.rmi.server.codebase", "file:///" + classDir.getPath() + File.separator);
    System.setProperty("java.rmi.server.ignoreStubClasses", "true");
  }

  public static void createEtlcProperties() throws IOException {
    if (TMP_DIR == null) {
      throw new NullPointerException("TMP_DIR not initialized");
    }
    /* Setup RMI location stuff */
    final Properties properties = new Properties();
    properties.setProperty(LwpServer.ENGINE_HOST, registryHost);
    properties.setProperty(LwpServer.ENGINE_PORT, Integer.toString(registryPort));
    ServicenamesTestHelper.createEtlcProperties(TMP_DIR, properties);
  }
}
