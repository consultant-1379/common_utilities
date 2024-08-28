package com.ericsson.eniq.common;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ENIQEntityResolver extends DefaultHandler {

  private final Logger log;

  private File dtdDir = null;

  public ENIQEntityResolver(final String logPrefix) {
    log = Logger.getLogger(logPrefix + ".ENIQEntityResolver");

    final String dir = System.getProperty("CONF_DIR", null);

    if (dir != null) {
      dtdDir = new File(dir + "/" + "dtd");
      if (dtdDir.isDirectory() && dtdDir.canRead()) {
        log.fine("DTD directory: " + dtdDir);
      } else {
        log.warning("DTD directory can't be found");
        dtdDir = null;
      }
    }
  }

  public InputSource resolveEntity(final String publicId, String systemId) throws SAXException {
    log.finest("Resolve entity \"" + systemId + "\"");

    if (dtdDir == null) {
      return returnBogus(publicId, systemId);
    }

    try {

      if (systemId.lastIndexOf("/") >= 0) {
        systemId = systemId.substring(systemId.lastIndexOf("/") + 1);
      }
        
      log.finest("DTD filename \"" + systemId + "\"");

      final File dtdFile = new File(dtdDir, systemId);

      InputSource source = null;

      if (dtdFile.isFile() && dtdFile.canRead()) {
        source = new InputSource(new FileReader(dtdFile));
        source.setPublicId(publicId);
        source.setSystemId(systemId);
      } else {
        source = returnBogus(publicId, systemId);
      }

      return source;

    } catch (Exception e) {
      log.log(Level.FINE, "ResolveEntity failed", e);
      throw new SAXException("ResolveEntity failed", e);
    }

  }

  private InputSource returnBogus(final String publicId, final String systemId) {
    final StringReader reader = new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

    final InputSource source = new InputSource(reader);
    source.setPublicId(publicId);
    source.setSystemId(systemId);

    return source;
  }

}
