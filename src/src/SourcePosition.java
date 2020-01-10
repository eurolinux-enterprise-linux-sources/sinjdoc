// SourcePosition.java, created Tue Mar 18 18:31:42 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.io.File;

/**
 * The <code>SourcePosition</code> interface describes a source position:
 * filename, line number, and column number.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: SourcePosition.java,v 1.3 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.SourcePosition
 */
public interface SourcePosition {
    /** The source file.  Returns null if no file information is available. */
    public File file();
    /** The line in the source file.  The first line is numbered 1; 0 means
     *  no line number information is available. */
    public int line();
    /** The column in the source file.  The first column is numbered 1; 0 means
     *  no column information is available.  Columns count characters in the
     *  input stream; a tab advances the column number to the next 8-column
     *  tab stop. */
    public int column();
    /** Convert the source position to the form "Filename:line".
     */
    public String toString();
}
