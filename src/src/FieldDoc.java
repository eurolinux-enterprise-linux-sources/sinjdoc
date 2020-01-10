// FieldDoc.java, created Wed Mar 19 12:17:04 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.List;
/**
 * The <code>FieldDoc</code> class represents a field in a java class.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: FieldDoc.java,v 1.5 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.FieldDoc
 */
public interface FieldDoc extends MemberDoc {
    /** Get the value of a constant field, wrapped in an object if it
     *  has a primitive type.  If the field is not constant, returns
     *  <code>null</code>. */
    public Object constantValue();
    /** The text of a Java language expression whose value is the value
     *  of the constant.  The expression uses no identifiers other than
     *  primitive literals.  If the field is not constant, returns
     *  <code>null</code>. */
    public String constantValueExpression();
    /** Return true if this field is transient. */
    public boolean isTransient();
    /** Return true if this field is volatile. */
    public boolean isVolatile();
    /** Return the @serialField tags for this item. */
    public List<SerialFieldTag> serialFieldTags();
    /** Return the type of this field. */
    public Type type();
}
