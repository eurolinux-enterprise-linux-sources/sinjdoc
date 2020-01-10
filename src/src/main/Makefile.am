## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in

AM_JAVACFLAGS = -g -source 1.5 -warnunchecked
# JAVAROOT directory must exist!
JAVAROOT=$(top_builddir)/classes

CLASSPATH_ENV = CLASSPATH=$(JAVAROOT):$(top_srcdir)/lib/jutil.jar:$$CLASSPATH

# build Main.java last, because it has dependencies on html and parser.
# note that the package statement in Main.java doesn't agree with its
# location here.  What can I say, it's a hack.
noinst_JAVA = \
Main.java

# automake 1.6.3 is broken: it doesn't actually include java sources
# in the distribution.  The following line works around this.
EXTRA_DIST = $(noinst_JAVA)
