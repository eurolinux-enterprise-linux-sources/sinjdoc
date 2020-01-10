// ThrowsTag.java, created Wed Mar 19 13:03:46 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.List;
/**
 * The <code>ThrowsTag</code> class represents a @throws or @exception
 * documentation tag.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: ThrowsTag.java,v 1.5 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.ThrowsTag
 */
public interface ThrowsTag extends Tag {
    /** Return a <code>Type</code> object representing the exception. */
    public Type exception();
    /** Return the exception comment associated with this
     *  <code>ThrowsTag</code>>. */
    public List<Tag> exceptionComment();
    /** Return the name of the exception associated with this
     *  <code>ThrowsTag</code>. */
    public String exceptionName();
}
