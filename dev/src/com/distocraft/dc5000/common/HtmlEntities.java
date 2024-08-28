package com.distocraft.dc5000.common;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class handles functionality for decoding and encoding of HTML-entities from HTML-formatted text.
 * Copyright Distocraft 2006.
 * @author berggren
 *
 */
public class HtmlEntities {

  /**
   * Translation table for HTML entities.<br/>
   * reference: W3C - Character entity references in HTML 4 [<a href="http://www.w3.org/TR/html401/sgml/entities.html" target="_blank">http://www.w3.org/TR/html401/sgml/entities.html</a>].
   */
  private static final Object[][] html_entities_table = { { "&Aacute;", new Integer(193) },
      { "&aacute;", new Integer(225) }, { "&Acirc;", new Integer(194) },
      { "&acirc;", new Integer(226) }, { "&acute;", new Integer(180) },
      { "&AElig;", new Integer(198) }, { "&aelig;", new Integer(230) },
      { "&Agrave;", new Integer(192) }, { "&agrave;", new Integer(224) },
      { "&alefsym;", new Integer(8501) }, { "&Alpha;", new Integer(913) },
      { "&alpha;", new Integer(945) }, { "&amp;", new Integer(38) },
      { "&and;", new Integer(8743) }, { "&ang;", new Integer(8736) },
      { "&apos;", new Integer(39) }, { "&Aring;", new Integer(197) },
      { "&aring;", new Integer(229) }, { "&asymp;", new Integer(8776) },
      { "&Atilde;", new Integer(195) }, { "&atilde;", new Integer(227) },
      { "&Auml;", new Integer(196) }, { "&auml;", new Integer(228) },
      { "&bdquo;", new Integer(8222) }, { "&Beta;", new Integer(914) },
      { "&beta;", new Integer(946) }, { "&brvbar;", new Integer(166) },
      { "&bull;", new Integer(8226) }, { "&cap;", new Integer(8745) },
      { "&Ccedil;", new Integer(199) }, { "&ccedil;", new Integer(231) },
      { "&cedil;", new Integer(184) }, { "&cent;", new Integer(162) },
      { "&Chi;", new Integer(935) }, { "&chi;", new Integer(967) },
      { "&circ;", new Integer(710) }, { "&clubs;", new Integer(9827) },
      { "&cong;", new Integer(8773) }, { "&copy;", new Integer(169) },
      { "&crarr;", new Integer(8629) }, { "&cup;", new Integer(8746) },
      { "&curren;", new Integer(164) }, { "&dagger;", new Integer(8224) },
      { "&Dagger;", new Integer(8225) }, { "&darr;", new Integer(8595) },
      { "&dArr;", new Integer(8659) }, { "&deg;", new Integer(176) },
      { "&Delta;", new Integer(916) }, { "&delta;", new Integer(948) },
      { "&diams;", new Integer(9830) }, { "&divide;", new Integer(247) },
      { "&Eacute;", new Integer(201) }, { "&eacute;", new Integer(233) },
      { "&Ecirc;", new Integer(202) }, { "&ecirc;", new Integer(234) },
      { "&Egrave;", new Integer(200) }, { "&egrave;", new Integer(232) },
      { "&empty;", new Integer(8709) }, { "&emsp;", new Integer(8195) },
      { "&ensp;", new Integer(8194) }, { "&Epsilon;", new Integer(917) },
      { "&epsilon;", new Integer(949) }, { "&equiv;", new Integer(8801) },
      { "&Eta;", new Integer(919) }, { "&eta;", new Integer(951) },
      { "&ETH;", new Integer(208) }, { "&eth;", new Integer(240) },
      { "&Euml;", new Integer(203) }, { "&euml;", new Integer(235) },
      { "&euro;", new Integer(8364) }, { "&exist;", new Integer(8707) },
      { "&fnof;", new Integer(402) }, { "&forall;", new Integer(8704) },
      { "&frac12;", new Integer(189) }, { "&frac14;", new Integer(188) },
      { "&frac34;", new Integer(190) }, { "&frasl;", new Integer(8260) },
      { "&Gamma;", new Integer(915) }, { "&gamma;", new Integer(947) },
      { "&ge;", new Integer(8805) }, { "&gt;", new Integer(62) },
      { "&harr;", new Integer(8596) }, { "&hArr;", new Integer(8660) },
      { "&hearts;", new Integer(9829) }, { "&hellip;", new Integer(8230) },
      { "&Iacute;", new Integer(205) }, { "&iacute;", new Integer(237) },
      { "&Icirc;", new Integer(206) }, { "&icirc;", new Integer(238) },
      { "&iexcl;", new Integer(161) }, { "&Igrave;", new Integer(204) },
      { "&igrave;", new Integer(236) }, { "&image;", new Integer(8465) },
      { "&infin;", new Integer(8734) }, { "&int;", new Integer(8747) },
      { "&Iota;", new Integer(921) }, { "&iota;", new Integer(953) },
      { "&iquest;", new Integer(191) }, { "&isin;", new Integer(8712) },
      { "&Iuml;", new Integer(207) }, { "&iuml;", new Integer(239) },
      { "&Kappa;", new Integer(922) }, { "&kappa;", new Integer(954) },
      { "&Lambda;", new Integer(923) }, { "&lambda;", new Integer(955) },
      { "&lang;", new Integer(9001) }, { "&laquo;", new Integer(171) },
      { "&larr;", new Integer(8592) }, { "&lArr;", new Integer(8656) },
      { "&lceil;", new Integer(8968) }, { "&ldquo;", new Integer(8220) },
      { "&le;", new Integer(8804) }, { "&lfloor;", new Integer(8970) },
      { "&lowast;", new Integer(8727) }, { "&loz;", new Integer(9674) },
      { "&lrm;", new Integer(8206) }, { "&lsaquo;", new Integer(8249) },
      { "&lsquo;", new Integer(8216) }, { "&lt;", new Integer(60) },
      { "&macr;", new Integer(175) }, { "&mdash;", new Integer(8212) },
      { "&micro;", new Integer(181) }, { "&middot;", new Integer(183) },
      { "&minus;", new Integer(8722) }, { "&Mu;", new Integer(924) },
      { "&mu;", new Integer(956) }, { "&nabla;", new Integer(8711) },
      { "&nbsp;", new Integer(32) }, { "&ndash;", new Integer(8211) },
      { "&ne;", new Integer(8800) }, { "&ni;", new Integer(8715) },
      { "&not;", new Integer(172) }, { "&notin;", new Integer(8713) },
      { "&nsub;", new Integer(8836) }, { "&Ntilde;", new Integer(209) },
      { "&ntilde;", new Integer(241) }, { "&Nu;", new Integer(925) },
      { "&nu;", new Integer(957) }, { "&Oacute;", new Integer(211) },
      { "&oacute;", new Integer(243) }, { "&Ocirc;", new Integer(212) },
      { "&ocirc;", new Integer(244) }, { "&OElig;", new Integer(338) },
      { "&oelig;", new Integer(339) }, { "&Ograve;", new Integer(210) },
      { "&ograve;", new Integer(242) }, { "&oline;", new Integer(8254) },
      { "&Omega;", new Integer(937) }, { "&omega;", new Integer(969) },
      { "&Omicron;", new Integer(927) }, { "&omicron;", new Integer(959) },
      { "&oplus;", new Integer(8853) }, { "&or;", new Integer(8744) },
      { "&ordf;", new Integer(170) }, { "&ordm;", new Integer(186) },
      { "&Oslash;", new Integer(216) }, { "&oslash;", new Integer(248) },
      { "&Otilde;", new Integer(213) }, { "&otilde;", new Integer(245) },
      { "&otimes;", new Integer(8855) }, { "&Ouml;", new Integer(214) },
      { "&ouml;", new Integer(246) }, { "&para;", new Integer(182) },
      { "&part;", new Integer(8706) }, { "&permil;", new Integer(8240) },
      { "&perp;", new Integer(8869) }, { "&Phi;", new Integer(934) },
      { "&phi;", new Integer(966) }, { "&Pi;", new Integer(928) },
      { "&pi;", new Integer(960) }, { "&piv;", new Integer(982) },
      { "&plusmn;", new Integer(177) }, { "&pound;", new Integer(163) },
      { "&prime;", new Integer(8242) }, { "&Prime;", new Integer(8243) },
      { "&prod;", new Integer(8719) }, { "&prop;", new Integer(8733) },
      { "&Psi;", new Integer(936) }, { "&psi;", new Integer(968) },
      { "&quot;", new Integer(34) }, { "&radic;", new Integer(8730) },
      { "&rang;", new Integer(9002) }, { "&raquo;", new Integer(187) },
      { "&rarr;", new Integer(8594) }, { "&rArr;", new Integer(8658) },
      { "&rceil;", new Integer(8969) }, { "&rdquo;", new Integer(8221) },
      { "&real;", new Integer(8476) }, { "&reg;", new Integer(174) },
      { "&rfloor;", new Integer(8971) }, { "&Rho;", new Integer(929) },
      { "&rho;", new Integer(961) }, { "&rlm;", new Integer(8207) },
      { "&rsaquo;", new Integer(8250) }, { "&rsquo;", new Integer(8217) },
      { "&sbquo;", new Integer(8218) }, { "&Scaron;", new Integer(352) },
      { "&scaron;", new Integer(353) }, { "&sdot;", new Integer(8901) },
      { "&sect;", new Integer(167) }, { "&shy;", new Integer(173) },
      { "&Sigma;", new Integer(931) }, { "&sigma;", new Integer(963) },
      { "&sigmaf;", new Integer(962) }, { "&sim;", new Integer(8764) },
      { "&spades;", new Integer(9824) }, { "&sub;", new Integer(8834) },
      { "&sube;", new Integer(8838) }, { "&sum;", new Integer(8721) },
      { "&sup1;", new Integer(185) }, { "&sup2;", new Integer(178) },
      { "&sup3;", new Integer(179) }, { "&sup;", new Integer(8835) },
      { "&supe;", new Integer(8839) }, { "&szlig;", new Integer(223) },
      { "&Tau;", new Integer(932) }, { "&tau;", new Integer(964) },
      { "&there4;", new Integer(8756) }, { "&Theta;", new Integer(920) },
      { "&theta;", new Integer(952) }, { "&thetasym;", new Integer(977) },
      { "&thinsp;", new Integer(8201) }, { "&THORN;", new Integer(222) },
      { "&thorn;", new Integer(254) }, { "&tilde;", new Integer(732) },
      { "&times;", new Integer(215) }, { "&trade;", new Integer(8482) },
      { "&Uacute;", new Integer(218) }, { "&uacute;", new Integer(250) },
      { "&uarr;", new Integer(8593) }, { "&uArr;", new Integer(8657) },
      { "&Ucirc;", new Integer(219) }, { "&ucirc;", new Integer(251) },
      { "&Ugrave;", new Integer(217) }, { "&ugrave;", new Integer(249) },
      { "&uml;", new Integer(168) }, { "&upsih;", new Integer(978) },
      { "&Upsilon;", new Integer(933) }, { "&upsilon;", new Integer(965) },
      { "&Uuml;", new Integer(220) }, { "&uuml;", new Integer(252) },
      { "&weierp;", new Integer(8472) }, { "&Xi;", new Integer(926) },
      { "&xi;", new Integer(958) }, { "&Yacute;", new Integer(221) },
      { "&yacute;", new Integer(253) }, { "&yen;", new Integer(165) },
      { "&yuml;", new Integer(255) }, { "&Yuml;", new Integer(376) },
      { "&Zeta;", new Integer(918) }, { "&zeta;", new Integer(950) },
      { "&zwj;", new Integer(8205) }, { "&zwnj;", new Integer(8204) } };
  
  /**
   * Map to convert extended characters in html entities.
   */
  private static final Map<Integer,String> htmlentities_map = new HashMap<Integer,String>();

  /**
   * Map to convert html entities in extended characters.
   */
  private static final Map<String,Integer> unhtmlentities_map = new HashMap<String,Integer>();

  static {
    for (int i = 0 ; i < html_entities_table.length ; ++i) {
      htmlentities_map.put((Integer)html_entities_table[i][1], (String)html_entities_table[i][0]);
      unhtmlentities_map.put((String)html_entities_table[i][0], (Integer)html_entities_table[i][1]);
    }
  }

  private HtmlEntities() {
  	
  }
  
  /**
   * This method converts the htmlentities to normal characters from a given text.
   * For example &amp; is replaced to &, &auml; is replaced to ä etc.
   * @param htmlText is the text to be converted.
   * @param Logger log is the log to report errors.
   */
  public static String convertHtmlEntities(final String targetString, final Logger log) {
    final StringBuilder buffer = new StringBuilder();

    for (int i = 0; i < targetString.length(); ++i) {
      final char ch = targetString.charAt(i);
      if (ch == '&') {
        final int semi = targetString.indexOf(';', i + 1);
        if ((semi == -1) || ((semi - i) > 7)) {
          buffer.append(ch);
          continue;
        }
        final String entity = targetString.substring(i, semi + 1);
        Integer iso;
        if (entity.charAt(1) == ' ') {
          buffer.append(ch);
          continue;
        }
        if (entity.charAt(1) == '#') {
          if (entity.charAt(2) == 'x') {
            iso = Integer.parseInt(entity.substring(3, entity.length() - 1), 16);
          } else {
            iso = Integer.valueOf(entity.substring(2, entity.length() - 1));
          }
        } else {
          iso = unhtmlentities_map.get(entity);
          log.finest("HtmlEntities.convertHtmlEntries: Found entity " + entity +". Iso integer value is " + iso.toString());
        }
        
        buffer.append((char) (iso.intValue()));
        log.finest("HtmlEntities.convertHtmlEntries: appended value " + (char) (iso.intValue()));
        
        i = semi;
      } else {
        buffer.append(ch);
      }
    }
    return buffer.toString();
  }

  /**
   * This method convert's special and extended characters into HTML entitities.
   * @param str input string
   * @return formatted string
   * @see #unhtmlentities(String)
   */
  public static String createHtmlEntities(final String targetString) {

    if (targetString == null) {
      return "";
    }

    final StringBuilder buffer = new StringBuilder(); //the otput string buffer

    for (int i = 0; i < targetString.length(); ++i) {
      final char ch = targetString.charAt(i);
      final String entity = htmlentities_map.get(Integer.valueOf(ch)); // Get the equivalent html entity.
      if (entity == null) { // Entity has not been found.
        if (((int) ch) > 128) { // Check if it is an extended character.
          buffer.append("&#" + ((int) ch) + ";"); // Convert to extended character.
        } else {
          buffer.append(ch); // Append the character as it is.
        }
      } else {
        buffer.append(entity); // Append the html entity.
      }
    }
    return buffer.toString();
  }

}
