package com.yippee.util;

import com.yippee.db.model.DocAug;
import com.yippee.indexer.Parser;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class FancyExtractorTest {
    /* Testing for fancy HTML extraction */
	
    String testHTML = "<HTML><HEAD><TITLE>CSE455/CIS555 HW2 Grading Data</TITLE></HEAD><BODY>" +
            "<H3>XML to be crawled</H3>" +
            "<UL>" +
            "<LI><B><A HREF=\"rss/cnnp.xml\">CNN politics - MATCHED</A></B></LI>" +
            "<LI><I><A HREF=\"rss/cnnt.xml\">CNN top stories - MATCHED</A></I></LI>" +
            "<LI><A HREF=\"rss/cnnl.xml\">CNN Laws - NOT MATCHED</A></LI>" +
            "</UL>" +
            "<H3>Other XML data</H3>" +
            "<UL>" +
            "<LI><A HREF=\"restrict/frontpage.xml\">BBC frontpage - restricted</A></LI>" +
            "<LI><A HREF=\"eurofxref-hist.xml\">Historical Euro exchange rate data - too large</A></LI>" +

            // these will probably fail (level 3)
            "      <li><a href=\"/~nvas/something/Africa.html\">Africa</a></li>\n" +
            "      <li><a href=\"./Americas.html\">Americas</a></li>\n" +
            "      <li><a href=\"../AsiaPacific.html\">Asia</a></li>\n" +

            //these should pass, are nothing special (level 0)
            "<li><a href=\"http://d.o.t.y/~nvas/something/Africa.html\">dots</a></li>\n" +
            "<li><a href=\"http://domain:8080/./Americas.html\">port</a></li>\n" +
            "<li><a href=\"http://www.seas.upenn/../AsiaPacific.html\">..</a></li>\n" +

            "<li><a href=\"http://we.com/index.php\">domain</a></li>\n" +
            "<li><a href=\"http://we.com/index\">domain</a></li>\n" +
            "<li><a href=\"http://we.com/index/\">domain</a></li>\n" +
            "<li><a href=\"http://we.com\">domain</a></li>\n" +
            "<li><a href=\"http://we.com/\">domain</a></li>\n" +

            // these are a bit advanced (level 2) -- these should have different semantics depending whether we are in a dir or page!
            "      <li><a href=\"nothingSpecial/./ea.html\">Business</a></li>\n" +
            "      <li><a href=\"nothingSpecial/\">Europe</a></li>\n" +
            "      <li><a href=\"nothingSpecial\">Front Page</a></li>\n" +
            "      <li><a href=\"nothingSpecial/whatevah/../ea.html\">Middle East</a></li>\n" +

            "      <li><a href=\"?who=me\">params</a></li>" +
            "      <li><a href=\"#tag\">tags</a></li>" +
            "</UL>" +
            "<H3>NON XML files</H3>" +
            "<UL>" +
            "<B><LI><A HREF=\"1.txt\">1.txt</A></LI></B>" +
            "<LI><A HREF=\"2.png\">2.png</A></LI>" +
            "</UL>" +
            "</BODY></HTML>";

    @Test
    public void testFancyExtract() {    	
    	Parser parser = new Parser();    	
    	DocAug docAug = new DocAug();
    	
    	docAug.setId("http://crawltest.cis.upenn.edu/index.html");
    	docAug.setDoc(testHTML);
    	docAug.setUrl("http://crawltest.cis.upenn.edu/index.html");
    	
    	FancyExtractor fe = new FancyExtractor(docAug.getId());
    	
    	Document doc = null;
		try {
			doc = parser.parseDoc(docAug);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	fe.extract("http://crawltest.cis.upenn.edu", doc);    	
    }
}