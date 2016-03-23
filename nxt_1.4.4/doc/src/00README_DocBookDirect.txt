JK 9/6/9

Attempt to have one-stop direct DocBook rendering of website.

 java -cp /path/to/saxon9.jar net.sf.saxon.Transform  -s:nxtdoc.top.total.xml -xsl:XSLT/split.xsl

Creates a directory called 'site' containing split XML files with navigation
added. One more step is required for rendering:

 cd site
 ln -s ../src/wysiwygdocbook1.02 .
 ln -s ../src/images .

allows access to the CSS styling.


------------------------------------


Issues:

* Can't include boilerplate / HTML form for search and have it
  rendered corerctly because we're displaying XML directly. Search
  could be made available on a standalone page, or just on the home
  page. Some people who render XML directly use frames(!)

* Sub-section navigation doesn't work - most sub-sections do not have
  IDs but even those that do are not navigated-to successfully via the
  standard HTML trick - FireFox claims to have some XLink capabilities
  but this doesn't appear to be included.

* Links. You can force link URLs to be displayed in the status bar
  using FireFox's XLink stuff. Need to also do that for links in the
  document itself. It probably won't work with IE.
