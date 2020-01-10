// TypeVisitor.java, created Mon Apr  7 12:48:14 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cscott@cscott.net>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

/**
 * <code>TypeVisitor</code> implements the visitor pattern for
 * objects representing <code>Type</code>s.  It allows easy encapsulation
 * of <code>Type</code>-subclass-dependent behavior & ensures that
 * all possible <code>Type</code> cases are covered.
 * 
 * @author  C. Scott Ananian <cscott@cscott.net>
 * @version $Id: TypeVisitor.java,v 1.2 2003/05/08 03:54:25 cananian Exp $
 */
public abstract class TypeVisitor<T> {
    /** Visit an <code>ArrayType</code>. */
    public abstract T visit(ArrayType t);
    /** Visit a <code>ClassType</code>. */
    public abstract T visit(ClassType t);
    /** Visit a <code>ParameterizedType</code>. */
    public abstract T visit(ParameterizedType t);
    /** Visit a <code>TypeVariable</code>. */
    public abstract T visit(TypeVariable t);

    /** Visit a <code>ClassTypeVariable</code> (optional). */
    public T visit(ClassTypeVariable t) { return visit((TypeVariable)t); }
    /** Visit a <code>MethodTypeVariable</code> (optional). */
    public T visit(MethodTypeVariable t) { return visit((TypeVariable)t); }
} // TypeVisitor
