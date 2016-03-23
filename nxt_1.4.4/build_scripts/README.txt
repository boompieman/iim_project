USING BUILD SCRIPTS

The XML files in this directory are 'ant' build scripts. To use them,
place one in the 'nxt' directory of your NXT distribution and name it
'build.xml'. Typing 'ant' at the command line will then build your
application.

There are currently three files in this directory:
 build.xml - builds NXT and puts a new nxt.jar into the 'lib'
             directory.

 build_NQL.xml - builds the subset of NXT comprising NQL and NOM, and
             puts a new jar called 'nql.jar' into the 'lib' directory.

 build_release.xml - normally users won't need this one - it's used by
   developers to produce a new release.
