## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in configure aclocal.m4 \
		       scripts/config.guess scripts/config.sub \
		       scripts/install-sh scripts/ltmain.sh \
		       scripts/missing scripts/mkinstalldirs
EXTRA_DIST = autogen.sh $(wildcard lib/*.jar) lib/README classes/README

SUBDIRS = src

pkgdata_DATA = @PACKAGE_TARNAME@.jar

# used to be $(shell find classes -name "*.class") 
@PACKAGE_TARNAME@.jar: $(shell find classes -type f)
	echo "Manifest-Version: 1.0" > manifest
	echo "Main-Class: net.cscott.sinjdoc.Main" >> manifest
#	echo "Class-Path: lib/jutil.jar lib/cup.jar" >> manifest
	echo "Class-Path: lib/cup.jar" >> manifest
	echo "Implementation-Version: $(VERSION)" >> manifest
	cd classes && $(JAR) -cfm ../$@ ../manifest net
	-$(RM) manifest

run:
	$(MAKE)
	$(JAVA) -ea -jar @PACKAGE_TARNAME@.jar

doc-link:
	$(RM) -rf doc-link
	mkdir -p doc doc-link/net/cscott/
	cd doc-link/net/cscott/ ; ln -s ../../../src sinjdoc
doc:
	$(MAKE)
	$(MAKE) doc-link
	$(RM) -rf doc
	$(JAVA) -ea -jar @PACKAGE_TARNAME@.jar \
	   -version -author -breakiterator -source 1.5 -quiet \
	   -overview src/overview.html \
	   -windowtitle "@PACKAGE_STRING@ documentation" \
	   -doctitle "@PACKAGE_STRING@ documentation" \
	   -header "@PACKAGE_STRING@" \
	   -bottom "Copyright &copy; 2003 C. Scott Ananian" \
	   -link http://java.sun.com/j2se/1.4/docs/api \
	   -d doc -sourcepath doc-link \
	   -subpackages net.cscott.sinjdoc -exclude net.cscott.sinjdoc.main
	$(RM) -rf doc-link
doc-clean:
	-$(RM) -rf doc doc-link

clean-local: doc-clean
mostlyclean-local:
	-$(RM) -rf classes/net

list:
	@find . -name "*.java" -print

update: needs-cvs
	cvs -q update -PdA
ChangeLog:
	if [ -d CVS ]; then \
	  $(RM) $@; \
	  rcs2log > $@ ; \
	fi
dist: ChangeLog

backup: dist
	scp $(PACKAGE)-$(VERSION).tar.gz catfish.lcs.mit.edu:
upload: dist doc ChangeLog @PACKAGE_TARNAME@.jar
	$(RM) -rf $(PACKAGE)-$(VERSION)
	mkdir $(PACKAGE)-$(VERSION)
	tar -c doc | tar -C $(PACKAGE)-$(VERSION) -x
	(date ; echo "Released @PACKAGE_STRING@" ; echo " " ; cat ChangeLog)\
		> $(PACKAGE)-$(VERSION)/ChangeLog.txt
	cp $(PACKAGE)-$(VERSION).tar.gz $(PACKAGE)-$(VERSION)/
	cd $(PACKAGE)-$(VERSION) && \
		ln -s $(PACKAGE)-$(VERSION).tar.gz $(PACKAGE).tar.gz
	cp @PACKAGE_TARNAME@.jar $(PACKAGE)-$(VERSION)/
	touch $(PACKAGE)-$(VERSION)/VERSION_$(VERSION)
	tar -c $(PACKAGE)-$(VERSION)/ | \
		ssh catfish.lcs.mit.edu "cd public_html/Projects/GJ/ && /bin/rm -rf $(PACKAGE)-$(VERSION) && tar -xv"
	$(RM) -rf $(PACKAGE)-$(VERSION)

# the 'cvs' rules only make sense if you've got a copy checked out from CVS
needs-cvs:
	@if [ ! -d CVS ]; then \
	  echo This rule needs CVS access to the source tree. ; \
	  exit 1; \
	fi

.PHONY: list update needs-cvs ChangeLog doc
