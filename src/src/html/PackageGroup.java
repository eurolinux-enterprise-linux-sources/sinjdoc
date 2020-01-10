// PackageGroup.java, created Fri Apr  4 22:12:25 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cscott@cscott.net>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.html;

import net.cscott.sinjdoc.PackageDoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * A <code>PackageGroup</code> represents a named group of packages
 * (specified by a pattern string) which will appear on the
 * documentation overview page.  <code>PackageGroup</code>s are
 * specified on the command-line with the <code>-group</code> option.
 * 
 * @author  C. Scott Ananian <cscott@cscott.net>
 * @version $Id: PackageGroup.java,v 1.3 2003/08/01 00:01:16 cananian Exp $
 */
class PackageGroup {
    /** This text is placed in the table heading for the group. */
    public final String heading;
    /** The package patterns that this group matches. */
    public final List<String> patterns;
    /** The <code>PackageDoc</code> objects corresponding to this group. */
    private final List<PackageDoc> packageList = new ArrayList<PackageDoc>();
    /** Creates a <code>PackageGroup</code>. */
    public PackageGroup(String heading, String patternList) {
	this.heading=heading;
	this.patterns=Collections.unmodifiableList
	    (Arrays.asList(COLON.split(patternList)));
    }
    private static final Pattern COLON = Pattern.compile(";");
    public List<PackageDoc> packages() {
	return Collections.unmodifiableList(packageList);
    }

    static void groupPackages(List<PackageGroup> groups,
			      List<PackageDoc> allPackages) {
	List<PackageDoc> pkgs = new ArrayList<PackageDoc>(allPackages);
	for (PackageGroup pg : groups) {
	    pg.packageList.clear();
	    for (String pattern : pg.patterns){
		for (Iterator<PackageDoc> it=pkgs.iterator(); it.hasNext();){
		    PackageDoc pd = it.next();
		    if (matches(pattern, pd.name())) {
			pg.packageList.add(pd);
			it.remove();
		    }
		}
	    }
	}
	assert pkgs.size()==0 : "catch-all rule not actually catching all";
    }
    private static boolean matches(String pattern, String packageName) {
	int idx = pattern.indexOf('*');
	if (idx < 0) return pattern.equals(packageName);
	else return packageName.startsWith(pattern.substring(0,idx));
    }
}
