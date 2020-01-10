// ExecutableMemberDoc.java, created Wed Mar 19 12:14:29 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.List;
/**
 * The <code>ExecutableMemberDoc</code> class represents a method or
 * constructor of a java class.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: ExecutableMemberDoc.java,v 1.4 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.ExecutableMemberDoc
 */
public interface ExecutableMemberDoc extends MemberDoc {
    /** Return the type variables declared by this method, if it is generic;
     *  otherwise return a zero-length list. */
    public List<MethodTypeVariable> typeParameters();
    /** Return true if this method is native. */
    public boolean isNative();
    /** Return true if this method is synchronized. */
    public boolean isSynchronized();
    /** Get argument information.
     * @return a list of parameters in source file order. */
    public List<Parameter> parameters();
    /** Return the @param tags in this method. */
    public List<ParamTag> paramTags();
    /** Return the signature, which is the parameter list with all types
     *  qualified.  For a method <code>foo(String x, int y)</code> the
     *  signature is "<code>(java.lang.String,int)</code>".
     */
    public String signature();
    /** Returns the flat signature.  The flat signature is similar to
     *  the signature, except that types are not qualified.  For a
     *  method <code>foo(String x, int y)</code> the flat signature
     *  is "<code>(String,int)</code>".
     */
    // XXX IS THIS NEEDED?  IT SEEMS BUG-PRONE.
    //public String flatSignature();
    /** Return exceptions this method or constructor throws. */
    public List<Type> thrownExceptions();
    /** Return the @throws and @exception tags in this method. */
    public List<ThrowsTag> throwsTags();
}
