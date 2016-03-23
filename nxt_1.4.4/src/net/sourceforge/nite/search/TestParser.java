package net.sourceforge.nite.search;

/**
 * This class merely provides access to the Parser class, which is generated 
 * as a protected class, by extending it with a public class. Some classes 
 * in the rewriter package create their own parsers in order to run unit
 * tests on their own functionality. This class enables them to do that, 
 * without changing access to the Parser.
 */
public class TestParser extends Parser {

    public TestParser(java.io.InputStream stream) { super(stream); }
	public TestParser(java.io.Reader stream) { super(stream); }
	public TestParser(ParserTokenManager tm) { super(tm); }

}
