// ParamTag.java, created Wed Mar 19 12:42:42 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.List;
/**
 * The <code>ParamTag</code> class represents a @param documentation tag.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: ParamTag.java,v 1.5 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.ParamTag
 */
public interface ParamTag extends Tag {
    /** Return the parameter comment associated with this
     *  <code>ParamTag</code>. */
    public List<Tag> parameterComment();
    /** Return the name of the parameter associated with this
     *  <code>ParamTag</code>. */
    public String parameterName();
}
