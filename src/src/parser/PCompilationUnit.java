// PCompilationUnit.java, created Mon Mar 24 14:44:10 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian (cscott@cscott.net)
// Licensed under the terms of the GNU GPL; see COPYING for details.
package net.cscott.sinjdoc.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The <code>PCompilationUnit</code> class encapsulates the information
 * associated with a compilation unit (source file).
 *
 * @author  C. Scott Ananian (cscott@cscott.net)
 * @version $Id: PCompilationUnit.java,v 1.4 2003/07/29 16:02:47 cananian Exp $
 */

class PCompilationUnit {
    final File file;
    final List<String> singleTypeImport = new ArrayList<String>();
    final List<String> staticSingleTypeImport = new ArrayList<String>();
    final List<PPackageDoc> onDemandImport = new ArrayList<PPackageDoc>();
    final List<String> staticOnDemandImport = new ArrayList<String>();
    final List<PClassDoc> classes = new ArrayList<PClassDoc>();

    PCompilationUnit(File file) { this.file = file; }
    public String toString() {
	return file+"["+
	    "singleTypeImport="+singleTypeImport+", "+
	    "staticSingleTypeImport="+staticSingleTypeImport+", "+
	    "onDemandImport="+onDemandImport+", "+
	    "staticOnDemandImport="+staticOnDemandImport+", "+
	    "classes="+classes+"]";
    }
}// PCompilationUnit
