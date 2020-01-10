// SerialFieldTag.java, created Wed Mar 19 13:01:22 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.List;
/**
 * The <code>SerialFieldTag</code> class documents a Serializable field
 * defined by an ObjectStreamField.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: SerialFieldTag.java,v 1.4 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.SerialFieldTag
 */
public interface SerialFieldTag extends Tag, Comparable<SerialFieldTag> {
    public int compareTo(SerialFieldTag tag);
    public List<Tag> description();
    public String fieldName();
    public String fieldType();
    public ClassDoc fieldTypeDoc();
}
