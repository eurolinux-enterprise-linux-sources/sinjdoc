## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
AM_JAVACFLAGS = -g -source 1.5 -warnunchecked
SUBDIRS = templates
EXTRA_DIST := package.html

JAVAROOT=$(top_builddir)/classes

CLASSPATH_ENV = CLASSPATH=$(JAVAROOT):$(top_srcdir)/lib/jutil.jar:$$CLASSPATH

noinst_JAVA = \
DocComparator.java \
HTMLDoclet.java \
HTMLOptions.java \
HTMLUtil.java \
PackageGroup.java \
Pair.java \
ReplayReader.java \
SimpleCharSequence.java \
TagEmitter.java \
TemplateContext.java \
TemplateWriter.java \
URLContext.java \
Version.java

# automake 1.6.3 is broken: it doesn't actually include java sources
# in the distribution.  The following line works around this.
EXTRA_DIST += $(noinst_JAVA)
