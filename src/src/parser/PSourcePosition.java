// SourcePosition.java, created Tue Mar 18 18:31:42 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import net.cscott.sinjdoc.DocErrorReporter;
import java.io.File;

/**
 * The <code>PSourcePosition</code> interface describes a source position:
 * filename, line number, and column number.  The implementation represents
 * the position as a zero-based character index which it lazily converts
 * to line and column number when needed.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PSourcePosition.java,v 1.11 2003/07/29 20:28:54 cananian Exp $
 */
class PSourcePosition
    implements net.cscott.sinjdoc.SourcePosition {
    // Represent source position as a File and a raw character index.
    // calls to the interface accessors lazily convert the index to a
    // line and column number.
    final PFile pfile; final int charIndex;
    PSourcePosition(File file, String encoding, boolean isUnicodeEscaped,
		    DocErrorReporter reporter) {
	this(PFile.get(file, encoding, isUnicodeEscaped, reporter), 0);
    }
    private PSourcePosition(PFile pfile, int charIndex) {
	this.pfile = pfile; this.charIndex = charIndex;
	assert pfile!=null;
	assert charIndex >= 0;
    }
    private PSourcePosition() { // for NO_INFO constructor.
	this.pfile=null; this.charIndex=-1;
    }
    public File file() { return pfile.file; }
    public int line() { return sp().line(); }
    public int column() { return sp().column(); }
    /** mathematical operations. */
    PSourcePosition add(int i) {
	if (i==0) return this; // short circuit.
	assert charIndex >= 0 && (charIndex+i) >= 0;
	return new PSourcePosition(pfile, charIndex+i);
    }
    final PSourcePosition subtract(int i) { return add(-i); }

    /** Convert the source position to the form "Filename:line".
     */
    public final String toString() {
	String filename = file()!=null?file().getPath():"<unknown>";
	String line = (line()>0) ? Integer.toString(line()) : "<unknown>";
	return filename+":"+line;
    }
    // lookup line and column and cache.
    private transient net.cscott.sinjdoc.SourcePosition sp=null;
    private net.cscott.sinjdoc.SourcePosition sp() {
	if (sp==null) sp=pfile.convert(charIndex);
	assert sp!=null;
	return sp;
    }
    public static final PSourcePosition NO_INFO = new PSourcePosition() {
	    public File file() { return null; }
	    public int line() { return 0; }
	    public int column() { return 0; }
	};
}
