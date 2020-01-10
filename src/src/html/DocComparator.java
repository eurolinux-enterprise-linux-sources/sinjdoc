// DocComparator.java, created Tue Apr  1 20:29:33 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cscott@cscott.net>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.html;

import net.cscott.sinjdoc.Doc;
import java.util.Comparator;
/**
 * The <code>DocComparator</code> class allows us to work-around some
 * type system unhappiness by (effectively) casting Doc subclasses to Doc
 * before comparing them.  Sad, but necessary.
 * 
 * @author  C. Scott Ananian <cscott@cscott.net>
 * @version $Id: DocComparator.java,v 1.2 2003/05/08 03:54:25 cananian Exp $
 */
class DocComparator<D extends Doc> implements Comparator<D> {
    /** Creates a <code>DocComparator</code>. */
    DocComparator() { }

    /** Compare two Doc items using the compareTo method of Doc. */
    public int compare(D d1, D d2) {
	return d1.compareTo(d2);
    }
    /** All <code>DocComparator</code>s are alike. */
    public boolean equals(Object o) {
	return o instanceof DocComparator;
    }
}
