// MemberDoc.java, created Wed Mar 19 12:18:43 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

/**
 * The <code>MemberDoc</code> class represents a member of a java class:
 * field, constructor, or method.  This is an abstract class dealing with
 * information common to method, constructor, and field members.  Class
 * members of a class (inner classes) are represented instead by
 * <code>ClassDoc</code>.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: MemberDoc.java,v 1.4 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.MemberDoc
 */
public interface MemberDoc extends ProgramElementDoc {
    /** Returns true if this member was synthesized by the compiler. */
    public boolean isSynthetic();
}
