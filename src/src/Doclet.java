// Doclet.java, created Tue Mar 18 14:59:20 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.List;
/**
 * The <code>Doclet</code> class provides the entry-point methods for
 * a documentation generator.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: Doclet.java,v 1.3 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.Doclet
 */
public abstract class Doclet {
    /** Return a short human-friendly string naming this Doclet. */
    public String getName() { return getClass().getName(); }
    /** Check for doclet-added options here.
     * <p> This method is not required and will default gracefully (to zero)
     * if absent.
     * @return Number of arguments to option.  Zero returns means option
     *  not known.  Negative value means error occurred.
     */
    public int optionLength(String option) { return 0; }
    /** Check that options have the correct arguments here.
     *  <p> This method is not required and will default gracefully (to true)
     *  if absent.
     *  <p> Printing option-related error messages (using the provided
     *  <code>DocErrorReporter</code> is the responsibility of this method.
     * @return true if the options are valid.
     */
    public boolean validOptions(List<List<String>> options,
				DocErrorReporter reporter) {
	return true;
    }
    /** Print help text describing the options accepted by this doclet.
     * <p> This method is not required and will default gracefully
     * (not printing anything) if absent.
     * <p> Printing should be done using
     *     <code>reporter.printNotice(msg)</code>.
     */
    public void optionHelp(DocErrorReporter reporter) { /* nop */ }

    /**
     * Generate documentation here.
     * @return true on success.
     */
    public abstract boolean start(RootDoc root);
}
