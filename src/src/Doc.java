// Doc.java, created Wed Mar 19 12:04:15 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc;

// Temporarily removed dependency on jutil.
//import net.cscott.jutil.ReferenceUnique;

import java.util.List;

/**
 * The <code>Doc</code> class is the abstract base class representing
 * all java language constructs (classes, packages, methods, etc) which
 * have comments and have been processed by this run of SinjDoc.  All
 * <code>Doc</code> items are <code>ReferenceUnique</code>, that is, they
 * are == comparable.
 * 
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: Doc.java,v 1.8 2003/07/31 23:05:26 cananian Exp $
 * @see com.sun.javadoc.Doc
 */
public interface Doc extends /*ReferenceUnique,*/ Comparable<Doc> {
    /** Return the text of the comment for this doc item, with all
     *  tags (including inline tags) removed. */
    public String commentText();
    /** Compares this <code>Doc</code> with the specified <code>Doc</code>
     *  for order. */
    public int compareTo(Doc d);
    /**
     * Return the first sentence of the comment as tags.  This includes
     * inline tags embedded in the first sentence, but not regular tags
     * (which would not constitute the first sentence).  Each section
     * of plain text is represented as a <code>Tag</code> of kind
     * "Text".  An example of an inline tags is the <code>SeeTag</code> of
     * kind "link".  The sentence end will be determined by
     * <code>java.text.BreakIterator.getSentenceInstance(Locale)</code>.
     * @return a list of <code>Tag</code>s representing the first sentence
     * of the comment.
     */
    public List<Tag> firstSentenceTags();
    /**
     * Return the full unprocessed text of the comment.  Tags are included
     * as text.
     */
    public String getRawCommentText();
    /**
     * Return the documentation comment as tags.  Each section of plain
     * text is represented as a <code>Tag</code> of kind "Text", and
     * inline tags are also represented as <code>Tag</code> objects.
     * The list of <code>Tag</code>s does not include regular tags, only
     * "text" and inline tags.
     */
    public List<Tag> inlineTags();
    /** Returns true if this doc item represents a non-interface class. */
    public boolean isClass();
    /** Returns true if this doc item represents a constructor. */
    public boolean isConstructor();
    /** Returns true if this doc item represents an error class. */
    public boolean isError();
    /** Returns true if this doc item represents an exception class. */
    public boolean isException();
    /** Returns true if this doc item represents a field. */
    public boolean isField();
    /** Returns true if javadoc will be generated from this item. */
    public boolean isIncluded();
    /** Returns true if this doc item represents an interface. */
    public boolean isInterface();
    /** Returns true if this doc item represents a non-constructor method. */
    public boolean isMethod();
    /** Returns true if this doc item represents a non-interface class which 
     *  is not an exception or error class. */
    public boolean isOrdinaryClass();
    /** Returns the non-qualified name of this <code>Doc</code> item. */
    public String name();
    /** Returns the source position of the documented entity.
     *  Will never return <code>null</code>. */
    public SourcePosition position();
    /** Return all tags in this doc item. */
    public List<Tag> tags();
    /** Return tags of the specified kind in this doc item.  For example
     *  if tagname has value "serial", all tags in this item of the form
     *  "@serial" will be returned. */
    public List<Tag> tags(String tagname);
}
