// TagVisitor.java, created Fri Mar 28 11:09:55 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

/**
 * <code>TagVisitor</code> implements the visitor pattern for <code>Tag</code>
 * objects.  It allows easy encapsulation of <code>Tag</code>-type dependent
 * behavior.
 *
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: TagVisitor.java,v 1.3 2003/05/08 03:54:25 cananian Exp $
 */
public abstract class TagVisitor<T> {
    /** Visit a <code>Tag</code> t. */
    public abstract T visit(Tag t);
    /** Visit a <code>ParamTag</code>. */
    public T visit(ParamTag t) { return visit((Tag)t); }
    /** Visit a <code>SeeTag</code>. */
    public T visit(SeeTag t) { return visit((Tag)t); }
    /** Visit a <code>SerialFieldTag</code>. */
    public T visit(SerialFieldTag t) { return visit((Tag)t); }
    /** Visit a <code>ThrowsTag</code>. */
    public T visit(ThrowsTag t) { return visit((Tag)t); }
}// TagVisitor
