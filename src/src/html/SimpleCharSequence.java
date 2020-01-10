// SimpleCharSequence.java, created Mon Mar 31 19:23:09 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cscott@cscott.net>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.html;

/**
 * The <code>SimpleCharSequence</code> class is a simple and efficient
 * implementation of <code>CharSequence</code> that doesn't do any
 * content-copying (i.e. all operations are O(1)).
 * 
 * @author  C. Scott Ananian <cscott@cscott.net>
 * @version $Id: SimpleCharSequence.java,v 1.2 2003/05/08 03:54:25 cananian Exp $
 */
class SimpleCharSequence implements CharSequence  {
    private final char[] buf; final int off, len;
    public SimpleCharSequence(char[] buf, int off, int len) {
	this.buf = buf; this.off = off; this.len = len;
    }
    public char charAt(int index) { return buf[off+index]; }
    public int length() { return len; }
    public String toString() { return new String(buf, off, len); }
    public CharSequence subSequence(int start, int end) {
	return new SimpleCharSequence(buf, off+start, end-start);
    }
}
