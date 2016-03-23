This directory contains documentation for the NITE XML Toolkit.

---------------------
PROJECT STATUS
--------------------

This documentation effort was started in October 2006. Before that
point, most of the documentation was hand-authored in HTML on 
http://www.ltg.ed.ac.uk/NITE, with some separate information
in other formats (particularly the search manual and some of
the JavaHelp for the configurable end user tools). 

In October 2006 we decided to move over to producing documentation in
Docbook format (a) to make it more organized, so it would be easier to
find and see the holes; (b) to make it printable; and (c) to make it
possible to get at more of it from within the tools.  Docbook is an
XML format from which it is possible to obtain PDF, HTML, and
JavaHelp, among others - it's just in XML document type but there are
various tools to support its use.  We have never worked with Docbook
before, so we don't recommend making use of our temporary information
as we set the project up.

In January 2007, we decided that the documentation was far enough
along that users might find an early release of the PDF version useful.
When it is complete, it will be versioned 1.0.   [Before the first release,
we should sort out the formatting so the section hierarchy is clear -
the styling of the section headers is pretty bad, both in the document
and in the table of contents.]

-----------------
JUNE 2009 STATUS 
-----------------

In June 2009 we revisited the documentation. There is now an 'ant'
build file that will require some setup to work in your system, but
makes building and publishing simpler in the long run. If you prefer
command line, skip this bit..

BUILD USING ANT

You need to have these tools and to edit build.xml appropriately
 saxon - http://saxon.sourceforge.net/
 prince - http://www.princexml.com/
 xmlto - http://cyberelk.net/tim/software/xmlto/

Once that's done
 ant clean
 ant build

will build for print (nxtdoc.pdf) and web (XML in the 'site'
directory; HTML in 'html' directory). If you think those look OK, you
can also set up the 'publish' target to copy the files to the web
site. 

BUILD USING COMMAND LINE

 * direct web rendering of chunked XML files with added
   context-sensitive navigation:

> java -cp /path/to/saxon9.jar  net.sf.saxon.Transform  -s:nxtdoc.top.total.xml -xsl:XSLT/split.xsl

gives you a directory called 'site' with XML files. go into that directory and 
> ln -s ../src/wysiwygdocbook1.02 .
> ln -s ../src/images .

to get rendered version. Some issues:
   1. Doesn't work in IE at all.
   2. Slightly nasty to need to use xhtml namespaced things but it's required for cross-browser viweing.

IE version can be achieved using

> xmlto -o html html nxtdoc.top.total.xml

 * rendering of PDF directly from XML (using Prince) is done like this:

> java -cp /path/to/Saxon/saxon9.jar net.sf.saxon.Transform -s:nxtdoc.top.total.xml -xsl:XSLT/table_of_contents.xsl > total.xml
> prince total.xml

This is not perfect for a couple of reasons (mainly CSS):
   1. table of content entries should be PDF links (though bookmarks help this).
  You can also get PDF without table-of-contents using
   2. Ordered lists do not render correctly so they're transformed to 

> xmlto pdf nxtdoc.top.total.xml

---------------------
DOCUMENTATION SOURCE - FORMAT AND DIVISION INTO FILES
---------------------

The documentation source is in the doc/src directory (in cvs).  It is in docbook
format divided into a set of source files that include others using
entity references, so that different subsets of it can be formatted
for different purposes.

The top level files for the documentation are:

nxtdoc.top.total.xml - to get a format with total existing documentation,
for use in generating the website or a PDF file.

# first version doesn't use includes, but I want it in CVS!
# Next step is to decompose it into pieces.

nxtdoc.top.query.xml - to get a format with the query language documentation,
for use in generating a query language reference manual

nxtdoc.top.onlinequery.xml - to get a format with the query language
documentation and the end user information about how to use the NXT Search
GUI, for use in generating on line help for the NXT Search GUI.

nxtdoc.top.signallabeller.xml - to get a format with documentation
about the GUI for signal labelling.

nxtdoc.top.decoder.xml - to get a format with documentation
about the discourse entity coder.

nxtdoc.top.dscoder.xml - to get a format with documentation
about the discourse segment coder.

nxtdoc.top.genericdisplay.xml - to get a format with documentation
about the generic display.


The remaining files contain individual sections of the documentation.

----------------------------
DOCBOOK TAGS USED
----------------------------

Since we aren't entirely sure how to use docbook yet, this section of
the README records our choices about tags - we should keep these the
same throughout the documentation.

Docbook appears to have lost some tags since version 3.1, like
comment for things that only belong in the draft  - and my list of tags
shows mixed case for things that only seem to validate when all
lowercase.

Comments showing things we need to think about are marked 
<!-- TEMP ... --> or <remark>, which renders in the output .


Here's a list of the semantic docbook tags used so far (3 December 2007). Tags used to create tables, lists and other formatting objects have not been included. On the other hand, you will find some other tags in the actual files, but they will probably need to be replaced by another in this list (or vice versa, things are not set in stone).

<section> with recursive descent for sections

<firstterm> for first time a term is used.

<code> for query language snippets and code in-line.

<programlisting> and <screen>: for query language snippets and code not in-line. It maintains the exact format of the given code.

<synopsis> for formal definitions

<literal> for smaller inline snippets of code, like examples, variable values, operators, etc.

<classname> for Java classes

<application> for software applications 

<filename> for filenames

<package> used for software packages or similar collections of small programs

<property> used generically for properties of guis or xml elements etc.

<guibutton>, <guilabel>, <guimenu>, <guimenuitem>, <interface> for names of GUI elements that are being described

<function> for names of methods, functions or other small programmes (smaller than an application, like for example, a linux command)

<caution>, <note>, <tip> for tips and warnings throughout the documentation

<parameter> for example, linux command parameters or any other settings of this kind.

<orgname> for names of organizations

<replaceable> for place holders in examples, such as "filename" in the command "cp filename ."

<smgltag> for XML tag and attribute value names. There are 4 classes of this tag, which have been used accordingly: "element", "attribute", "attvalue" and "namspace".

<graphic fileref=> for displayed graphics

<acronym> for acronyms

<remark> Editing remarks. When the documentation is complete there shouldn't be any <remark> tags in it.


-----------------------------
BUILDING THE DOCUMENTATION
-----------------------------

There are many ways of transforming Docbook into output formats.
Build.xml specifies targets for building the formats we need for
the document subsets we need.  It uses the Docbook XSL stylesheets, Xalan,
and FOP.  


---------------------
VALIDATION
---------------------

Output format problems are sometimes caused by invalid Docbook -
don't forget to validate your source.  Either

rxp document.xml
or online
http://www.cogsci.ed.ac.uk/~richard/xml-check.html 

or to use Xalan, with xalansamples.jar on your classpath.

java Validate document.xml

or 

xmlstarlet val document.xml

--------------------------------------------------
THOUGHTS ON A NEW BUILD PROCEDURE AT APRIL 2009
--------------------------------------------------

I don't see a build for the docbook stuff in CVS, but time
moving seems to have made this easier to do.

It used to be that we would go through two transforms to get to end
user formats.  First, we'd transform (using an XSL stylesheet) to
XSL-FO (XSL formatting objects), and then we'd transform and style
that.  This makes things pretty complicated because there are too many
places where you can change something to affect the output.

Now, browsers can apply CSS to XML directly - any XML,
Docbook-conformant XML included.  And Prince,
http://www.princexml.com, can take XML and a CSS print stylesheet, and
make PDF.  This seems preferable to me.  I don't see what we gain by
going through XSL-FO particularly.  Our current transforms do a couple
of useful things on the way to XSL-FO that we don't get straight off
the Docbook and CSS: they create a table of contents, and they allow
for a chunked version of the HTML output (i.e., HTML rendered on
multiple pages with "previous" and "next" buttons.  However, that part
of the transform is just some XSL stylesheets.  We can take the same
approach in our own stylesheets to create XML files that convey the
bits of material as we want them, and then style those directly.

Due diligence: are there common browsers that can't render XML+CSS?
IE6 we can live without (this is for scientists), but what about the
others?  If so, then we fallback to going via XSL-FO, but we can perhaps
use xmlto to make that route a bit easier.  It does it and 
omes as standard on e.g. Fedora.

 xmlto --skip-validation -o pdfout  pdf nxtdoc.top.total.xml 

Similarly:
 xmlto --skip-validation -o htmlout html nxtdoc.top.total.xml 

produces HTML in pretty much exactly the same form as results from
running the lxt, fop stuff described below, though I found I needed to
add reference to the stylesheet afterwards using: 
sed -i 's/<\/title>/<\/title><link href="nxtdoc.css" rel="stylesheet" type="text\/css"\/>/' *.html

The stuff you can download from DocBook (docbook-xsl-1.75
http://sourceforge.net/project/showfiles.php?group_id=21935) again
does very similar things via FO:
 
 xsltproc --output HTML/ --stringparam  section.autolabel 1 --stringparam  section.label.includes.component.label 1 /path/to/docbook-xsl-1.75.1/html/chunk.xsl nxt.top.total.xml

----------------------
STYLING XML IN A WEB BROWSER
----------------------

To style XML in a web browser, it just takes a processing instruction
like

<?xml-stylesheet href="file:///home/user/xmlmind/xxe-perso-4_3_0/addon/config/docbook/css/docbook.css" type="text/css"?>

However, at least in Firefox and maybe all browsers (since they
probably use non-validating parsers), this excludes the out-of-file
entities, so the XML to be styled isn't the straight repository
source, it's a version put together from the repository bits:

xmlstarlet c14n nxtdoc.top.total.xml > output.xml

There must be lots of ways to do this without xmlstarlet (e.g. rxp
nxtdoc.top.total.xml > output.xml).

Then we need to write decent CSS for the rendering. Possible starting points:

   * XMLMind comes with extensive CSS for Docbook.  We need to check
      the licensing - we're allowed to modify the CSS for our use but
      "Licensee may not distribute the Software, or part of the
      Software, alone or bundled with another product, without written
      permission from Licensor." Does it count as distribution if it's
      used for rendering on the web?  A few parts of the rendering are
      terrible for our current tags, but some are fine.

   * We're certainly allowed to change
     http://www.informatik.fh-wiesbaden.de/~werntges/home_t/proj/wysiwyg-dbk01.html
     See the src/wysiwygdocbook1.02 directory. Two different
     customisations: navigate.css for web and print.css for print have
     been attempted but there are some issues.

   * for bits of the rendering we like from the XSL-FO approach, we
     can read what it says in the .fo source, since much of the
     tagging there is based on CSS, but do it the simpler way.


----------------------
OBTAINING CHUNKED OUTPUT
----------------------

Write XSL stylesheets to do the chunking ourselves based on examples,
I think. There is an XSLT2.0 stylesheet in src/XSLT called
split.xsl. Run using: 
 java -cp /path/to/saxon9.jar net.sf.saxon.Transform  -s:nxtdoc.top.total.xml -xsl:XSLT/split.xsl
Output goes to a directory called 'site'; from the 'site' directory,
link src/wysiwygdocbook1.02 and src/images to see styling (only really
works in FireFox).

The 'split.xsl' stylesheet also attempts to make its own
context-sensitive table of contents for each page (nicer than prev,
next, up style navigation but not working cross platform).

----------------------
GETTING A TABLE OF CONTENTS
----------------------

There's no table of contents in the Docbook itself - that's part of
what the XSL-FO stylesheets produce. However you can create a DocBook
table of contents using:
 
 xsltproc -o mytoc.xml --stringparam chunk.section.depth 8 --stringparam chunk.first.sections 1 /path/to/docbook-xsl-1.75.1/html/maketoc.xsl nxtdoc.top.total.xml

This produces a pseudo-DocBook style table of contents, but with
processing instructions that are specific to a HTML transform. In fact
including it into a DocBook file does not produce valid XML: xslto
complains about nested 'tocentry' elements.  PDF rendering using
Prince does not produce correctly rendered PDF or working links.

It seems pretty specific to HTML via FO transform
(http://www.sagehill.net/docbookxsl/Chunking.html) though it may be
usable.

------------ NO - can't use SSIs directly on XML, that's only for HTML ------------
I think it's easy enough to write a stylesheet that gathers a table of
contents off the DocBook and sticks it with information about the
links in a new, separate XML file, which can then be rendered as a
server side include no matter what page of the documentation we're
showing.  I'd like to see the CSS for the div with the table of
contents to have fixed positioning, so that no matter where you're
scrolled, you can get at it.  It would be nice if it behaved more like
a navigation toolbar that took as context where you are in the
document, but I don't know as we have good examples of that kind of
behaviour.

Basic layout could be like Jean's home page (stolen from Steve) or maybe there are other techniques like at 
https://www.servage.net/blog/2009/03/20/create-a-cool-css-based-drop-down-menu/
or more at
http://sixrevisions.com/css/30-exceptional-css-navigation-techniques/.
No idea what's sensible/works across browsers.

----------------------
GETTING GOOD PRINT RENDERING
----------------------

There are problems with rendering for print using Prince directly with
XML and CSS: 

 * you need processing to get a table of contents, and it seems really
   hard to get page numbers on the TOC;
 * section numbering needs further (non-trivial?) processing;
 * internal links don't work;
 * for all non-FireFox browsers no links work;

Links, section numbering and table of contents unfortunately seem to
work much better via flow objects. 
 

--------------------------------------------------------------------
(These instructions are from 2006; more stuff just works on Linux distros now.)
---------
PREPARING TO BUILD - DTD ACCESS
---------

Before building, you need to ensure that the XSL processor can 
find the docbook DTD.  You can rely on network access for this,
but it can be very slow.  You can also hardwire the filename for
the DTD in the docbook document, but this means the document
won't be portable.  A better option is to use an XML catalog to
lookup where to find the DTD.

(1) Ensure Docbook is in an XML catalog. The minimal catalog
for this is

<!DOCTYPE catalog PUBLIC "-//OASIS//DTD XML Catalogs V1.0//EN" "catalog.dtd">

<catalog prefer="public">
    <rewriteSystem
        systemIdStartString="http://www.oasis-open.org/docbook/xml/4.4/"
        rewritePrefix="docbook-xml-4.4/" />
</catalog>

but using the correct rewrite prefix for your installation.

The catalog that has been used is in doc/src/catalog.xml (CVS)

(2) Ensure the catalog is accessible to your stylesheet processor.
For Xalan, you need a CatalogManager.properties file on your classpath
that contains something like the following:

catalogs=/group/ltg/projects/lcontrib/share/lib/xml/catalog.xml
relative-catalogs=false
static-catalog=yes
catalog-class-name=org.apache.xml.resolver.Resolver
verbosity=1

Other stylesheet processors will work differently and may not be
catalog-aware.

(3) ensure the apache xml commons catalog resolver from 
http://xml.apache.org/mirrors.cgi is on your CLASSPATH.

---------
PREPARING TO BUILD - STYLESHEET ACCESS
---------

There are several options here:

(a) Specify a URI, either in a processing instruction in the document
or at the command line, and rely on network access.

[This works but I often get I/O errors, I think due to networks
problems, and its slow.]

(b) Specify a URI, either in a processing instruction in the document
or at the command line, specifying where to find the stylesheet
corresponding to that URI on the local machine in the XML catalog.

(c) Specify a local filename for the stylesheet.

We don't want to change the document when we change target formats, so
we'd rather have the stylesheet specified at the command line.  Xalan
2.6.0 can't resolve URIs from the command line using the catalog file,
so we're stuck with file names for now.  When this changes, see
"ATTEMPT TO GET CATALOG ACCESS TO STYLESHEETS" below.

Also see doc/src/catalog.xml
--------
RUNNING DOCBOOK XSL (SKETCH)
--------


Set the following environment variables:

JAVA_HOME - the directory containing your java installation

DOCBOOK_XSL_HOME - the directory containing your Docbook XSL installation.
Stylesheet paths will be specified relative to this.  This is not
necessary if you are using an XML catalog that contains the docbook
stylesheets and a stylesheet processor that can either use the XML
catalog from the command line or via a processing instruction. 

FOP_HOME - the directory containing your FOP installation (with
fop.sh).

Ensure xalan, resolver, and XML catalog containing Docbook dtd are all
on your classpath.

Choose your stylesheet for the target output format and run using
xalan.

java org.apache.xalan.xslt.Process -out  source.html     -in
source.xml     -xsl $DOCBOOK_XSL_HOME/html/docbook.xsl

Or with lxt

lxt nxtdoc.top.total.xml $DOCBOOK_XSL_HOME/fo/docbook.xsl > nxtdoc.total.fo

There are three scripts that have been used for that

docbook2html.sh  : converts docbook files to html
docbook2fo.sh  : converts docbook files to fo format
fo2pdf.sh  : converts the fo file to pdf

--------
FUTURE BUILD FILE WORK
-------

We intend to write a build file using ant.

The most useful targets are:

all - build all documentation targets

website - build NXT documentation website

javahelp - build all the javahelp files for end user tools

# ... fill in here ...


*************************************************************************
BELOW:  TEMPORARY NOTES WHILST SETTING UP THE DOCUMENTATION SYSTEM
*************************************************************************

Our test decomposition into files to make different cuts on the documentation
works, but we need to create more different sections, making sure that nothing
(much) goes in the top level documents.


-----------------------------
NEXT STEPS
-----------------------------

(1) Consider using xincludes instead of entities?  with entities,
it's hard to do xml processing on the individual files because the
entities get expanded.

(2) Write a build file (Jonathan)

(3) The tables don't end up in the PDF output by our route - find
out what we can do to the source to make this work. Update 3/12/2007: tables are output, but the column width is set in a stupid way; the resulting tables are not always elegant, and in one case the text overlaps the table cell.

(4) Figure out the outline of the documentation set.

(5) figure out what to do about authorship.  So far we just have
one big list but of course there were individual authors for sections.
Do we just list the right people in the top files, or do we do something 
more elaborate than that?

(6) Tranfer heaps of documentation over, write new...

--------------
TO GET FROM DOCBOOK TO PDF (ON DICE)
--------------
There are ready files for this in doc/src : first use docbook2fo.sh and then fo2pdf.sh on the result

(1) SETUP PATHS

export DOCBOOK_XSL_HOME="/group/ltg/projects/lcontrib/share/lib/xml/docbook-xsl-1.71.0/"

export JAVA_HOME="/etc/alternatives/java_sdk_1.5.0"

export FOP_HOME="/group/ltg/projects/lcontrib/share/lib/java/fop-0.20.5"

(2) APPLY DOCBOOK XSL TO GET FO

lxt nxtdoc.top.total.xml $DOCBOOK_XSL_HOME/fo/docbook.xsl > nxtdoc.total.fo

to include section numbering, need to pass it
lxt  -p  section.autolabel 1 -p  section.label.includes.component.label 1 nxtdoc.top.total.xml $DOCBOOK_XSL_HOME/fo/docbook.xsl > nxtdoc.total.fo



See http://sagehill.net/docbookxsl/ for more formatting customizations.
We can do anything we want by writing our own customization layer, a stylesheet
that imports the docbook xsl stylesheet we'd ordinarily be calling, and calling
that instead.

There are some customization files that have already been created:
common_customizations.xsl  : customizations for both html and fo output.
html_customizations.xsl : customizations for html output
fo_customizations.xsl : customizations for fo output

(3) USE FOP TO GET PDF

sh $FOP_HOME/fop.sh nxtdoc.total.fo nxtdoc.total.pdf

# SAME TWO STEPS FOR EVERY *.top.*.xml

for target in total query onlinequery signallabeller; do lxt nxtdoc.top.$target.xml  $DOCBOOK_XSL_HOME/fo/docbook.xsl > nxtdoc.$target.fo; sh $FOP_HOME/fop.sh nxtdoc.$target.fo nxtdoc.$target.pdf; done



--------------
TO GET FROM DOCBOOK TO JAVAHELP (ON DICE)
--------------

lxt nxtdoc.top.signallabeller.xml $DOCBOOK_XSL_HOME/javahelp/javahelp.xsl 

This creates a set of files.

Writing ar01s02.html for section
Writing index.html for article
Writing jhelpset.hs
Writing jhelptoc.xml
Writing jhelpmap.jhm
Writing jhelpidx.xml

I need to figure out how to control where they go and find out if they
actually work for something (I don't know how to use them).

--------------
TO GET FROM DOCBOOK TO HTML (ON DICE)
--------------
There's a ready script to do this: docbook2html.sh
There is also a css file tweaking the look of the html output. The file is nxtdoc.css (Note: it is set up so that the css file needs to be in the same directory as the html output files. This can be changed in html_customizations.xsl)

lxt nxtdoc.top.total.xml $DOCBOOK_XSL_HOME/html/chunk.xsl 

This gets a set of html pages with prev/next links. They look kind
of OK.  We wouldn't really want the whole website to look like this.
Read the docbook xsl documentation for other html output formats,
and consider some additional styling.

------------------
EDITING DOCBOOK SOURCE
------------------

I like doing it with the XML Mind XML Editor, but I only have
that on the Windows laptop.

To replace all occurrences of a tag with something else,
 
for file in nxtdoc.*.xml; do cat $file | lxreplace -q sect -n '"section"' > ./new/$file; done

Except that expands entities, and this is undesirable behaviour.


------------------------
OUTLINE
-----------------------

We don't know yet.

Serve following needs:

tool end user
search user/data analyst
configuration file user (someone setting up software)
corpus designer (including import from other sources)
programmer writing tailored tools.

----------------------------------
LEGACY SEARCH MANUAL
----------------------------------

We have access to PDF from our site; more recent PDF from
Stuttgart; HTML from Stuttgart; a version in NXT's on-line help
(nxt/lib/helpset.jar).

Holger's original format was JManual.

http://www.ims.uni-stuttgart.de/projekte/TIGER/TIGERSearch/subprojects.shtml#JManual

"The idea of the JManual project is to define an XML-based manual
encoding format. Manuals encoded in this format are then transformed
by XSLT stylesheets to several output formats: HTML, hyperlinked PDF,
and JavaHelp. The latter format can also be integrated as an help
system into Java applications."

The source for JManual appears not to be available; we just have
a jar file with html, no XML.

JManual sounds like a DocBook wanna-be.  It isn't supported any more
from what we can see, and the source doesn't appear to be available.

--------------------
DOCBOOK
--------------------


-----
Docbook XML -> HTML can be done 
-----

(1) using stylesheets from http://docbook.sourceforge.net/ which
can either construct one HTML page or a whole set of them.  The
current css stylesheet offeres some suggestions. It includes all 
html tags that are output from docbook to make any subsequent 
changes a bit easier.

(2) using a CSS2 stylesheet to go from XML to the web browser display
directly - use a declaration like <?xml-stylesheet
href="../docbook.xml.css" type="text/css"?>.  There are such
stylesheets packaged as part of the XMLmind XML Editor
http://www.xmlmind.com/xmleditor/ but for the free (standard) edition
you have to use the Docbook schema, not the DTD.

-----
Docbook XML -> JavaHelp can be done
-----

(1) using a stylesheet from http://docbook.sourceforge.net/.

-----
Docbook XML -> PDF can be done
-----

(1) via XSL-FO

(2) via DSSSL

-----
Docbook XML -> XSL-FO can be done
-----

(1) using a stylesheet from http://docbook.sourceforge.net/ to get
to XSL-FO format

on laptop, in /home/jeanc/nxt/search-manual/docbook-xsl-1.71.0/fo/docbook.xsl.

(2) using a CSS2 stylesheet to go from XML to XSL-FO 
(http://www.re.be/css2xslfo/) 

-----
XSL-FO -> PDF can be done
-----

(1) using FOP (http://xmlgraphics.apache.org/fop/), which is Java

-----
Docbook XML -> DSSSL can be done
-----

(1) using jade (which we have on DICE)

-----
DSSSL -> PDF can be done
-----

(2) using jadetex? (there is some standard route, just can't remember
what it said).

-------------
PREFERRED ROUTES
------------

The css stylesheets in the commercial editor from XMLmind are likely to be 
better than creating html and then styling it - for html, try that first.
The licensing appears OK with this as long as we don't incorporate the
editor or stylesheets into other software (even for internal use).  The
stylesheets for anything but HTML aren't attached to the menu system in
the free version and it isn't clear to me where they are, but the HTML
looks nicer than with the Docbook XSL stylesheet. 

DSSSL is ancient, and XSL-FO is standard, prefer the latter. Prefer
Docbook stylesheets since it's Norm Walsh and we know him and because
we have to use that route for the JavaHelp anyway.

--------------
DOCBOOK STYLESHEETS
--------------





--------------------
CATALOG ACCESS TO STYLESHEETS
-------------------

You will find catalog.xml and CatalogManager.properties in doc/src (CVS). The .sh scripts also found there use the two files mentioned.
The following process has worked (using xalan):

STEP 1: Put the docbook stylesheets in an XML catalog. 

[For local DICE installation at Edinburgh, used
/group/ltg/projects/lcontrib/share/lib/xml/catalog.xml]

STEP 2: Put CatalogManager.properties somewhere on the classpath 
with content such as

catalogs=/group/ltg/projects/lcontrib/share/lib/xml/catalog.xml
relative-catalogs=false
static-catalog=yes
catalog-class-name=org.apache.xml.resolver.Resolver
verbosity=1

(verbosity 4 to really know what's going on )

STEP 3:  Get apache xml commons catalog resolver from 
http://xml.apache.org/mirrors.cgi 

[for local DICE Installation at Edinburgh, used
/group/ltg/projects/lcontrib/share/lib/xml]

STEP 4: Put the catalog resolver, xalan, and xerces on the classpath

The classpath the documentation says is:
-cp "/usr/java/xerces.jar:/usr/java/xalan.jar:\
/docbook-xsl/extensions/xalan25.jar:\
/usr/share/resolver.jar:/usr/share" \

STEP 5:  Run as:

java org.apache.xalan.xslt.Process  \
   -ENTITYRESOLVER  org.apache.xml.resolver.tools.CatalogResolver \
   -URIRESOLVER  org.apache.xml.resolver.tools.CatalogResolver \
   -out  source.testcat.html  \
   -in  source.xml  \
   -xsl  http://docbook.sourceforge.net/release/xsl/current/html/docbook.xsl 


Richard says lxt is catalog aware.  He says there's a recently
introduced Docbook bug that means it doesn't check for extensions
before running them and lxt often doesn't implement extensions, so it
might not be a good strategy.

 lxt  source.xml  http://docbook.sourceforge.net/release/xsl/current/html/docbook.xsl   > source.lxt.html 

doesn't work - how do I get it to look for the catalog? Richard
says he thinks the environment variable XML_CATALOG_FILES but 
that didn't do anything for me.

---------------
OTHER INFORMATION SOURCES
--------------

http://wiki.docbook.org/topic/DocBookCssStylesheets

http://sagehill.net/docbookxsl/

