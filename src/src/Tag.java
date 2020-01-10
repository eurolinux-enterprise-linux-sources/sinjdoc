// Tag.java, created Wed Mar 19 12:45:35 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

import java.util.List;
/**
 * The <code>Tag</code> class represents a documentation tag.  The
 * tag name and tag text are available for queries; tags with structure
 * or which require special processing are handled by subclasses.
 * <p>
 * Every tag can have both inline and trailing tags, although in practice
 * only top-level 'Text' tags contain trailing tags.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: Tag.java,v 1.7 2003/05/08 03:54:25 cananian Exp $
 * @see com.sun.javadoc.Tag
 */
public interface Tag {
    /** Returns true if this is an inline tag. Inline tags may contain
     *  text with other inline tags (!) but never contain trailing
     *  tags. */
    public boolean isInline();
    /** Returns true if this is a "Text" tag.  Text tags represent a
     *  string of plain-text, possibly with embedded inline tags. */
    public boolean isText();
    /** Returns true if this is a trailing tag.  Trailing tags may
     *  contain content with inline tags, but never contain
     *  other trailing tags. */
    public boolean isTrailing();
    /** Return the plain-text represented by this tag, or null if
     *  <code>isText()</code> is false. */
    public String text();
    /** Return the name of this tag, or null if <code>isText()</code>
     *  is true. */
    public String name();
    /** Return the contents of this tag; that is, the portion beyond
     *  the tag name and before the closing brace.  The returned list
     *  of tags will never contain <code>Tag</code>s with
     *  <code>isTrailing()</code> true. Returns null if
     *  <code>isText()</code> is true. */
    public List<Tag> contents();
    /** Return the source position of this tag.
     *  Will never return <code>null</code>. */
    public SourcePosition position();
    /** Return a human-readable representation of this tag object. */
    public String toString();

    /** Accept a visitor. */
    public <T> T accept(TagVisitor<T> visitor);
}
