// ReplayReader.java, created Thu Apr  3 17:21:46 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cscott@cscott.net>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.html;

import java.io.IOException;
import java.io.Reader;

/**
 * The <code>ReplayReader</code> class allows mark and replay of
 * selected portions of a <code>Reader</code>'s contents.
 * 
 * @author  C. Scott Ananian <cscott@cscott.net>
 * @version $Id: ReplayReader.java,v 1.3 2003/05/08 03:54:25 cananian Exp $
 */
public class ReplayReader extends Reader {
    /** The underlying reader. */
    final Reader delegate;
    /** Temporary storage for replayed content. */
    StringBuffer replayBuffer = new StringBuffer();
    /** Current location in the replayBuffer. */
    int replayLoc=0;
    /** An internal mark to support the <code>Reader.mark()</code> and
     *  <code>Reader.reset()</code> methods. */
    Mark mark = null;
    /** Whether we've saved a mark for this reader yet. */
    boolean hasMark=false;

    /** Creates a <code>ReplayReader</code>. */
    public ReplayReader(Reader r) {
	this.delegate = r;
    }

    public boolean ready() throws IOException {
	if (replayLoc < replayBuffer.length()) return true;
	else return delegate.ready();
    }
    public void close() throws IOException {
	delegate.close();
	replayBuffer.setLength(0);
	replayLoc=0;
	mark=null;
	hasMark=false;
    }
    public int read(char[] cbuf, int off, int len) throws IOException {
	if (len==0) return 0; // quick case.
	if (replayLoc < replayBuffer.length()) { // read from buffer.
	    int nchars = Math.min(len, replayBuffer.length()-replayLoc);
	    replayBuffer.getChars(replayLoc, replayLoc+nchars, cbuf, off);
	    replayLoc+=nchars;
	    return nchars;
	}
	// read from underlying stream, increasing the size of the replay
	// buffer if necessary.
	if (!hasMark) return delegate.read(cbuf, off, len);
	char[] buf = new char[1024];
	int nchars = delegate.read(buf, 0, buf.length);
	if (nchars<=0) return nchars;
	replayBuffer.append(buf, 0, nchars);
	return read(cbuf, off, len); // recurse to do it again.
    }
    /** @return true */
    public boolean markSupported() { return true; }
    public void mark(int ignore) { this.mark = getMark(); }
    /** Returns a <code>Mark</code> representing the current stream position.*/
    public Mark getMark() {
	this.hasMark=true;
	return new ReplayMark(replayLoc);
    }
    public void reset() { reset(this.mark); }
    /** Reset the stream to the position of the given <code>Mark</code>. */
    public void reset(Mark m) {
	this.replayLoc = ((ReplayMark)m).loc;
    }
    
    /** An abstract object used to represent a stream position in a
     *  <code>ReplayReader</code>. */
    public static interface Mark { }
    /** Internal implementation of <code>Mark</code> used by
     *  <code>ReplayReader</code>.  */
    private static class ReplayMark implements Mark {
	public final int loc;
	ReplayMark(int loc) { this.loc = loc; }
    }
}
