## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
JAVAROOT=$(top_builddir)/classes
TEMPLATE_ROOT=$(JAVAROOT)/net/cscott/sinjdoc/html/templates

TEMPLATE_FILES = \
class-page.html \
package-frame.html \
package-summary.html \
allclasses-frame.html \
allclasses-noframe.html \
overview-frame.html \
overview-summary.html \
index-packages.html \
index-nopackages.html \
stylesheet.css

# move templates into place
all-am:	$(addprefix $(TEMPLATE_ROOT)/,$(TEMPLATE_FILES))
$(TEMPLATE_ROOT)/%: %
	mkdir -p $(dir $@)
	cp $< $@

EXTRA_DIST = $(TEMPLATE_FILES)
