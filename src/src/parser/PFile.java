package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.DocErrorReporter;
import net.cscott.sinjdoc.SourcePosition;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * The <code>PFile</code> class wraps a <code>java.io.File</code> object
 * and a lazily-created table which allows translation of raw character
 * indices to line numbers.
 *
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PFile.java,v 1.7 2003/07/29 20:28:54 cananian Exp $
 */
public class PFile {
    public final File file;
    public final String encoding;
    public final boolean isUnicodeEscaped;
    public final DocErrorReporter reporter;
    public PFile (File file, String encoding, boolean isUnicodeEscaped,
		  DocErrorReporter reporter) {
	this.file = file; this.encoding = encoding;
	this.isUnicodeEscaped = isUnicodeEscaped;
	this.reporter = reporter;
    }

    /** Build the line-starting position and tab-position tables. */
    private void buildTables() {
	assert lineStarts==null && tabIndex==null;
	List<String> lines = new ArrayList<String>();
	String contents = FileUtil.snarf(file, encoding, isUnicodeEscaped,
					 reporter).left;
	IntVector lineStartsVec = new IntVector();
	IntVector tabIndexVec = new IntVector();
	Matcher lineMatch = LINES.matcher(contents);
	while (lineMatch.find())
	    lineStartsVec.add(lineMatch.start());
	lineStartsVec.add(contents.length()); // sentinel at end.
	Matcher tabMatch = TAB.matcher(contents);
	while (tabMatch.find())
	    tabIndexVec.add(tabMatch.start());
	lineStarts = lineStartsVec.toArray();
	tabIndex = tabIndexVec.toArray();
	assert lineStarts!=null && tabIndex!=null;
    }
    /** sorted list of line starting indices. */
    transient int[] lineStarts;
    /** sorted list of tab character positions. */
    transient int[] tabIndex;
    /** Pattern which matches a line. */
    private static final Pattern LINES = Pattern.compile
	("^.*$", Pattern.MULTILINE);
    /** Pattern which matches a tab character. */
    private static final Pattern TAB = Pattern.compile("\\t");
    /** Helper class to keep track of a growing vector of integers. */
    private static class IntVector {
	int[] buf=new int[16]; int size=0;
	void add(int val) {
	    if (size==buf.length) resize();
	    buf[size++]=val;
	}
	private void resize() {
	    int[] nbuf = new int[buf.length*2];
	    System.arraycopy(buf, 0, nbuf, 0, buf.length);
	    buf = nbuf;
	}
	int[] toArray() {
	    int[] nbuf = new int[size];
	    System.arraycopy(buf, 0, nbuf, 0, size);
	    return nbuf;
	}
    }
    /** Convert the character index to a line and column number. */
    SourcePosition convert(int charIndex) {
	if (lineStarts==null) buildTables();
	// locate start of line.
	int line = Arrays.binarySearch(lineStarts, charIndex);
	if (line<0) line=-(1+line)-1;
	if (line<0 || line>=lineStarts.length-1)
	    return new SourcePosition() {
		    public File file() { return file; }
		    public int line() { return 0; }
		    public int column() { return 0; }
		};
	// now correct for tabs.
	int startOfLine = lineStarts[line];
	int firstTab = Arrays.binarySearch(tabIndex, startOfLine);
	if (firstTab<0) firstTab=-(1+firstTab);
	int lastTab = Arrays.binarySearch(tabIndex, charIndex);
	if (lastTab<0) lastTab=-(1+lastTab);
	int lastPos = startOfLine, column = 0;
	for (int i=firstTab; i<lastTab; i++) {
	    int tabPos = tabIndex[i];
	    column += (tabPos-lastPos);
	    // now correct for the tab.
	    column = ((column+8)/8)*8;
	    // update lastPos
	    lastPos = tabPos+1;
	}
	// adjust column to get to charIndex (no more tabs in between)
	column += (charIndex-lastPos);
	// okay, done.
	final int LINE = line+1; // one-based
	final int COLUMN = column+1; // one-based
	return new SourcePosition() {
		public File file() { return file; }
		public int line() { return LINE; }
		public int column() { return COLUMN; }
	    };
    }
    static PFile get(File f, String encoding, boolean isUnicodeEscaped,
		     DocErrorReporter reporter) {
	File canon;
	try { // canonicalize if possible.
	    canon = f.getCanonicalFile();
	} catch (java.io.IOException e) { // okay, then don't.
	    canon = f;
	}
	// lock because this is a static object; hence potentially shared
	// between multiple threads running the tool concurrently.
	synchronized(cache) {
	    if (!cache.containsKey(canon))
		cache.put(canon, new PFile(f/* preserve original name */,
					   encoding, isUnicodeEscaped,
					   reporter));
	    return cache.get(canon);
	}
    }
    private static final Map<File,PFile> cache = new HashMap<File,PFile>();
}
