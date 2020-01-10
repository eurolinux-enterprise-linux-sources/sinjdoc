// URLContext.java, created Mon Mar 31 14:26:03 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cscott@cscott.net>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.html;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * The <code>URLContext</code> class encapsulates a "current document"
 * URL.  This can be used to munge root-relative URLs so that they are
 * instead relative to the current document.
 * 
 * @author  C. Scott Ananian <cscott@cscott.net>
 * @version $Id: URLContext.java,v 1.5 2003/05/08 03:54:25 cananian Exp $
 */
class URLContext {
    final String context;
    
    /** Creates a <code>URLContext</code>. */
    public URLContext(String context) { this.context = context; }

    public String makeRelative(String url) {
	StringBuffer result = new StringBuffer();
	assert url.length()==0 || url.charAt(0)!='/';
	// special case a link to a different target in the same url.
	if (url.startsWith(context)) {
	    String trimmed = url.substring(context.length());
	    if (trimmed.startsWith("#")) return trimmed;
	}
	// strip components off the context URL until it matches the start
	// of the desired URL.
	String contextURL = STRIP_FILE.matcher(this.context).replaceFirst("");
	while (!url.startsWith(contextURL)) {
	    contextURL = STRIP_DIR.matcher(contextURL).replaceFirst("");
	    result.append("../");
	}
	result.append(url.substring(contextURL.length()));
	return result.toString();
    }
    private static final Pattern STRIP_FILE=Pattern.compile("[^/]+$");
    private static final Pattern STRIP_DIR=Pattern.compile("[^/]+/+$");

    public File toFile() {
	Matcher matcher = PATHPART.matcher(this.context);
	File f = null;
	while (matcher.find()) {
	    if (f==null) f = new File(matcher.group(1));
	    else f = new File(f, matcher.group(1));
	}
	return f;
    }
    private static final Pattern PATHPART=Pattern.compile("([^/]+)(/+|\\z)");

    public String toString() { return this.context; }
}
