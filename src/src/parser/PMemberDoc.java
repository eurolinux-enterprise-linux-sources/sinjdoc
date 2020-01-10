// MemberDoc.java, created Wed Mar 19 12:18:43 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import java.lang.reflect.Modifier;
/**
 * The <code>PMemberDoc</code> class represents a member of a java class:
 * field, constructor, or method.  This is an abstract class dealing with
 * information common to method, constructor, and field members.  Class
 * members of a class (inner classes) are represented instead by
 * <code>PClassDoc</code>.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PMemberDoc.java,v 1.10 2003/05/08 03:54:26 cananian Exp $
 */
abstract class PMemberDoc extends PProgramElementDoc
    implements net.cscott.sinjdoc.MemberDoc {
    final String name;
    final String commentText;
    final PSourcePosition commentPosition;

    boolean isSynthetic = false;

    PMemberDoc(ParseControl pc, PClassDoc containingClass, int modifiers,
	       String name, PSourcePosition position,
	       String commentText, PSourcePosition commentPosition) {
	super(pc, containingClass.containingPackage(), containingClass,
	      modifiers, position);
	this.name = name;
	this.commentText = commentText;
	this.commentPosition = commentPosition;
	assert name!=null;
    }
    public boolean isSynthetic() { return isSynthetic; }
    // methods abstract in PDoc
    public String name() { return name; }
    public String getRawCommentText() { return commentText; }
    public PSourcePosition getRawCommentPosition() { return commentPosition; }
}
