// DocErrorReporter.java, created Tue Mar 18 18:18:02 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

/**
 * The <code>DocErrorReporter</code> interface provides error, warning, and
 * notice printing.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: DocErrorReporter.java,v 1.3 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.DocErrorReporter
 */
public interface DocErrorReporter {
    /**
     * Print error message and increment error count.
     * @param msg the message to print.
     */
    public void printError(String msg);
    /**
     * Print error message and increment error count.
     * @param pos the position item where the error occurs.
     * @param msg the message to print.
     */
    public void printError(SourcePosition pos, String msg);
    /**
     * Print warning message and increment warning count.
     * @param msg the message to print.
     */
    public void printWarning(String msg);
    /**
     * Print warning message and increment warning count.
     * @param pos the position item where the warning occurs.
     * @param msg the message to print.
     */
    public void printWarning(SourcePosition pos, String msg);
    /**
     * Print a message.
     * @param msg the message to print.
     */
    public void printNotice(String msg);
    /**
     * Print a message.
     * @param pos the position item where the message occurs.
     * @param msg the message to print.
     */
    public void printNotice(SourcePosition pos, String msg);
}
