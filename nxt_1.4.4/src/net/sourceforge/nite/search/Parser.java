/* Generated By:JJTree&JavaCC: Do not edit this line. Parser.java */
package net.sourceforge.nite.search;

class Parser/*@bgen(jjtree)*/implements ParserTreeConstants, ParserConstants {/*@bgen(jjtree)*/
  protected JJTParserState jjtree = new JJTParserState();public static void main(String args[]) {
    System.out.println("V 0.010");
    System.out.println("Reading from standard input...");
    Parser t = new Parser(System.in);
    try {
      NodeQuery n = t.Query();
      n.dump("");
      System.out.println("Thank you.");
    } catch (Exception e) {
      System.out.println("Oops.");
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  //testing if variable has its definition
  private java.util.List knowingVariables = new java.util.ArrayList();
  private void addVariable(String var, Token token) throws ParseException
  {
    if( !knowingVariables.contains(var) ){
      knowingVariables.add(var);
    } else {
      ParseException exception =
        new ParseException("Double variable declaration: variable name "+var+" already used.");
      exception.currentToken = token;
      throw exception;
    }
  }
  private void testVariable(String var, Token token) throws ParseException
  {
    if( !knowingVariables.contains(var) ){
      ParseException exception =
        new ParseException("Variable declaration for "+var+" is missing.");
      exception.currentToken = token;
      throw exception;
    }
  }

  final public NodeQuery Query() throws ParseException {
                     /*@bgen(jjtree) Query */
  NodeQuery jjtn000 = new NodeQuery(JJTQUERY);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      SimpleQuery();
      label_1:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 49:
          ;
          break;
        default:
          jj_la1[0] = jj_gen;
          break label_1;
        }
        jj_consume_token(49);
        SimpleQuery();
      }
      jj_consume_token(0);
    jjtree.closeNodeScope(jjtn000, true);
    jjtc000 = false;
    {if (true) return jjtn000;}
    } catch (Throwable jjte000) {
    if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
    throw new Error("Missing return statement in function");
  }

  final public void SimpleQuery() throws ParseException {
 /*@bgen(jjtree) SimpleQuery */
  NodeSimpleQuery jjtn000 = new NodeSimpleQuery(JJTSIMPLEQUERY);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);boolean isExists = false, isForAll = false; Token t;
    try {
      label_2:
      while (true) {
        jj_consume_token(50);
                                      isExists = false;
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case EXISTS:
        case FORALL:
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case EXISTS:
            jj_consume_token(EXISTS);
                                      isExists = true;
            break;
          case FORALL:
            jj_consume_token(FORALL);
                                      isForAll = true;
            break;
          default:
            jj_la1[1] = jj_gen;
            jj_consume_token(-1);
            throw new ParseException();
          }
          break;
        default:
          jj_la1[2] = jj_gen;
          ;
        }
        t = jj_consume_token(VAR);
                                      jjtn000.addVar(t.image, isExists, isForAll);
                                      addVariable(t.image, t);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case TYPE:
          t = jj_consume_token(TYPE);
                                      jjtn000.addType(t.image);
          label_3:
          while (true) {
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case OR:
            case 51:
              ;
              break;
            default:
              jj_la1[3] = jj_gen;
              break label_3;
            }
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case OR:
              jj_consume_token(OR);
              break;
            case 51:
              jj_consume_token(51);
              break;
            default:
              jj_la1[4] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
            }
            t = jj_consume_token(TYPE);
                                      jjtn000.addType(t.image);
          }
          break;
        default:
          jj_la1[5] = jj_gen;
          ;
        }
        jj_consume_token(52);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 50:
          ;
          break;
        default:
          jj_la1[6] = jj_gen;
          break label_2;
        }
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 53:
        jj_consume_token(53);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case NOT:
        case TEXT:
        case START:
        case END:
        case DURATION:
        case CENTER:
        case ID:
        case TIMED:
        case VALUE:
        case VARATR:
        case VAR:
        case 50:
          Logical();
          break;
        default:
          jj_la1[7] = jj_gen;
          ;
        }
        break;
      default:
        jj_la1[8] = jj_gen;
        ;
      }
    } catch (Throwable jjte000) {
    if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
  }

  final public void Logical() throws ParseException {
 /*@bgen(jjtree) Logical */
  NodeLogical jjtn000 = new NodeLogical(JJTLOGICAL);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);Token t; boolean negated = false;  NodeCondition c;
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
        jj_consume_token(NOT);
                         negated = true;
        break;
      default:
        jj_la1[9] = jj_gen;
        ;
      }
      c = Condition();
                         jjtn000.addOrElement(c);
                         if (negated) c.negate();
      label_4:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case IMPLICATION:
        case AND:
        case OR:
          ;
          break;
        default:
          jj_la1[10] = jj_gen;
          break label_4;
        }
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case OR:
          t = jj_consume_token(OR);
          break;
        case AND:
          t = jj_consume_token(AND);
          break;
        case IMPLICATION:
          t = jj_consume_token(IMPLICATION);
          break;
        default:
          jj_la1[11] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
                         negated = false;
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case NOT:
          jj_consume_token(NOT);
                         negated = true;
          break;
        default:
          jj_la1[12] = jj_gen;
          ;
        }
        c = Condition();
                         if      (t.kind==OR)  jjtn000.addOrElement(c);
                         else if (t.kind==AND) jjtn000.addAndElement(c);
                         else                  jjtn000.addImplicationElement(c);
                         if (negated) c.negate();
      }
    jjtree.closeNodeScope(jjtn000, true);
    jjtc000 = false;
    jjtn000.makeDNF();
    } catch (Throwable jjte000) {
    if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
  }

  final public NodeCondition Condition() throws ParseException {
 /*@bgen(jjtree) Condition */
  NodeCondition jjtn000 = new NodeCondition(JJTCONDITION);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);Token t; Token s; Token u; Token v; Token n;
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 50:
        jj_consume_token(50);
        Logical();
        jj_consume_token(52);
                          jjtn000.isGroup = true;
        break;
      case TIMED:
        jj_consume_token(TIMED);
        jj_consume_token(50);
        s = jj_consume_token(VAR);
        jj_consume_token(52);
                                          testVariable( jjtn000.setAVar(s.image), s );
                                          jjtn000.setType(jjtn000.TIMED);
                                          jjtn000.image = "timed(" + s.image + ")";
        break;
      case TEXT:
      case START:
      case END:
      case DURATION:
      case CENTER:
      case ID:
      case VALUE:
      case VARATR:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case VARATR:
          s = jj_consume_token(VARATR);
                                            testVariable( jjtn000.setA( jjtn000.ATTRIBUTE, s.image ), s );
          break;
        case VALUE:
          s = jj_consume_token(VALUE);
                                                          jjtn000.setA( jjtn000.VALUE,     s.image );
          break;
        case TEXT:
          s = jj_consume_token(TEXT);
          jj_consume_token(50);
          u = jj_consume_token(VAR);
          jj_consume_token(52);
                                            testVariable( jjtn000.setA( jjtn000.TEXT,      u.image ), u );
          break;
        case START:
          s = jj_consume_token(START);
          jj_consume_token(50);
          u = jj_consume_token(VAR);
          jj_consume_token(52);
                                            testVariable( jjtn000.setA( jjtn000.START,     u.image ), u );
          break;
        case END:
          s = jj_consume_token(END);
          jj_consume_token(50);
          u = jj_consume_token(VAR);
          jj_consume_token(52);
                                            testVariable( jjtn000.setA( jjtn000.END,       u.image ), u );
          break;
        case DURATION:
          s = jj_consume_token(DURATION);
          jj_consume_token(50);
          u = jj_consume_token(VAR);
          jj_consume_token(52);
                                            testVariable( jjtn000.setA( jjtn000.DURATION,  u.image ), u );
          break;
        case CENTER:
          s = jj_consume_token(CENTER);
          jj_consume_token(50);
          u = jj_consume_token(VAR);
          jj_consume_token(52);
                                            testVariable( jjtn000.setA( jjtn000.CENTER,    u.image ), u );
          break;
        case ID:
          s = jj_consume_token(ID);
          jj_consume_token(50);
          u = jj_consume_token(VAR);
          jj_consume_token(52);
                                            testVariable( jjtn000.setA( jjtn000.ID,        u.image ), u );
          break;
        default:
          jj_la1[13] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
          jjtn000.image = s.image;
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case EQ:
        case NE:
        case GE:
        case LE:
        case GT:
        case LT:
        case EQREGEXP:
        case NEREGEXP:
        case POINTER:
        case PRECEDENCE:
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case EQ:
          case NE:
          case GE:
          case LE:
          case GT:
          case LT:
          case POINTER:
          case PRECEDENCE:
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case EQ:
              jj_consume_token(EQ);
                               jjtn000.setType(jjtn000.EQ);        jjtn000.image += "==";
              break;
            case NE:
              jj_consume_token(NE);
                               jjtn000.setType(jjtn000.NE);        jjtn000.image += "!=";
              break;
            case GE:
              jj_consume_token(GE);
                               jjtn000.setType(jjtn000.GE);        jjtn000.image += ">=";
              break;
            case LE:
              jj_consume_token(LE);
                               jjtn000.setType(jjtn000.LE);        jjtn000.image += "<=";
              break;
            case GT:
              jj_consume_token(GT);
                               jjtn000.setType(jjtn000.GT);        jjtn000.image += ">";
              break;
            case POINTER:
              jj_consume_token(POINTER);
                               jjtn000.setType(jjtn000.GT);        jjtn000.image += ">";
              break;
            case LT:
              jj_consume_token(LT);
                               jjtn000.setType(jjtn000.LT);        jjtn000.image += "<";
              break;
            case PRECEDENCE:
              jj_consume_token(PRECEDENCE);
                               jjtn000.setType(jjtn000.LT);        jjtn000.image += "<";
              break;
            default:
              jj_la1[14] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
            }
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case VARATR:
              t = jj_consume_token(VARATR);
                                                testVariable( jjtn000.setB( jjtn000.ATTRIBUTE, t.image ), t );
              break;
            case VALUE:
              t = jj_consume_token(VALUE);
                                                jjtn000.setB( jjtn000.VALUE,     t.image );
              break;
            case TEXT:
              t = jj_consume_token(TEXT);
              jj_consume_token(50);
              v = jj_consume_token(VAR);
              jj_consume_token(52);
                                                testVariable( jjtn000.setB( jjtn000.TEXT,      v.image ), v );
              break;
            case START:
              t = jj_consume_token(START);
              jj_consume_token(50);
              v = jj_consume_token(VAR);
              jj_consume_token(52);
                                                testVariable( jjtn000.setB( jjtn000.START,     v.image ), v );
              break;
            case END:
              t = jj_consume_token(END);
              jj_consume_token(50);
              v = jj_consume_token(VAR);
              jj_consume_token(52);
                                                testVariable( jjtn000.setB( jjtn000.END,       v.image ), v );
              break;
            case DURATION:
              t = jj_consume_token(DURATION);
              jj_consume_token(50);
              v = jj_consume_token(VAR);
              jj_consume_token(52);
                                                testVariable( jjtn000.setB( jjtn000.DURATION,  v.image ), v );
              break;
            case CENTER:
              t = jj_consume_token(CENTER);
              jj_consume_token(50);
              v = jj_consume_token(VAR);
              jj_consume_token(52);
                                                testVariable( jjtn000.setB( jjtn000.CENTER,    v.image ), v );
              break;
            case ID:
              t = jj_consume_token(ID);
              jj_consume_token(50);
              v = jj_consume_token(VAR);
              jj_consume_token(52);
                                                testVariable( jjtn000.setB( jjtn000.ID,        v.image ), v );
              break;
            default:
              jj_la1[15] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
            }
              jjtn000.image = t.image;
            break;
          case EQREGEXP:
            jj_consume_token(EQREGEXP);
            t = jj_consume_token(REGEXP);
                                      jjtn000.setType(jjtn000.REGEX);    jjtn000.setPattern(t.image);
            break;
          case NEREGEXP:
            jj_consume_token(NEREGEXP);
            t = jj_consume_token(REGEXP);
                                      jjtn000.setType(jjtn000.NOTREGEX); jjtn000.setPattern(t.image);
            break;
          default:
            jj_la1[16] = jj_gen;
            jj_consume_token(-1);
            throw new ParseException();
          }
          break;
        default:
          jj_la1[17] = jj_gen;
          ;
        }
        break;
      case VAR:
        s = jj_consume_token(VAR);
                           testVariable( jjtn000.setAVar(s.image), s );
                           jjtn000.image = s.image;
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case EQ:
          jj_consume_token(EQ);
                                     jjtn000.setType(jjtn000.EQUAL);               jjtn000.image += "==";
          break;
        case NE:
          jj_consume_token(NE);
                                     jjtn000.setType(jjtn000.INEQUAL);             jjtn000.image += "!=";
          break;
        case DOMINANCE:
          jj_consume_token(DOMINANCE);
                                      jjtn000.setType(jjtn000.DOMINANCE); jjtn000.image += "^";
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case NUMBER:
            n = jj_consume_token(NUMBER);
                                      jjtn000.setType(jjtn000.DOMINANCE_WITH_DISTANCE);
                                      jjtn000.setDistance(n.image);                jjtn000.image = n.image;
            break;
          default:
            jj_la1[18] = jj_gen;
            ;
          }
          break;
        case PRECEDENCE:
          jj_consume_token(PRECEDENCE);
          jj_consume_token(POINTER);
                                      jjtn000.setType(jjtn000.PRECEDENCE);         jjtn000.image += "<>";
          break;
        case POINTER:
          jj_consume_token(POINTER);
                                      jjtn000.setType(jjtn000.POINTER);            jjtn000.image += ">";
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case VALUE:
            n = jj_consume_token(VALUE);
                                      jjtn000.setType(jjtn000.POINTER_WITH_ROLE);
                                      jjtn000.setRole(n.image);
                                      jjtn000.image += ">" + n.image;
            break;
          default:
            jj_la1[19] = jj_gen;
            ;
          }
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case DOMINANCE:
            jj_consume_token(DOMINANCE);
                                      jjtn000.setType( jjtn000.getType() == jjtn000.POINTER ?
                                                         jjtn000.SUBDOM :
                                                         jjtn000.SUBDOM_WITH_ROLE );
                                      jjtn000.image += "^";
            break;
          default:
            jj_la1[20] = jj_gen;
            ;
          }
          break;
        case OVERLAPS_LEFT:
          jj_consume_token(OVERLAPS_LEFT);
                                      jjtn000.setType(jjtn000.OVERLAPS_LEFT);      jjtn000.image += "%";
          break;
        case LEFT_ALIGNED_WITH:
          jj_consume_token(LEFT_ALIGNED_WITH);
                                      jjtn000.setType(jjtn000.LEFT_ALIGNED_WITH);  jjtn000.image += "[[";
          break;
        case RIGHT_ALIGNED_WITH:
          jj_consume_token(RIGHT_ALIGNED_WITH);
                                      jjtn000.setType(jjtn000.RIGHT_ALIGNED_WITH); jjtn000.image += "]]";
          break;
        case INCLUDES:
          jj_consume_token(INCLUDES);
                                      jjtn000.setType(jjtn000.INCLUDES);           jjtn000.image += "@";
          break;
        case SAME_EXTENT_AS:
          jj_consume_token(SAME_EXTENT_AS);
                                      jjtn000.setType(jjtn000.SAME_EXTENT_AS);     jjtn000.image += "[]";
          break;
        case OVERLAPS_WITH:
          jj_consume_token(OVERLAPS_WITH);
                                      jjtn000.setType(jjtn000.OVERLAPS_WITH);      jjtn000.image += "#";
          break;
        case CONTACT_WITH:
          jj_consume_token(CONTACT_WITH);
                                      jjtn000.setType(jjtn000.CONTACT_WITH);       jjtn000.image += "][";
          break;
        case PRECEDES:
          jj_consume_token(PRECEDES);
                                      jjtn000.setType(jjtn000.PRECEDES);           jjtn000.image += "<<";
          break;
        default:
          jj_la1[21] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        t = jj_consume_token(VAR);
                              testVariable( jjtn000.setBVar(t.image), t );
                              jjtn000.image += t.image;
        break;
      default:
        jj_la1[22] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    jjtree.closeNodeScope(jjtn000, true);
    jjtc000 = false;
    {if (true) return jjtn000;}
    } catch (Throwable jjte000) {
    if (jjtc000) {
      jjtree.clearNodeScope(jjtn000);
      jjtc000 = false;
    } else {
      jjtree.popNode();
    }
    if (jjte000 instanceof RuntimeException) {
      {if (true) throw (RuntimeException)jjte000;}
    }
    if (jjte000 instanceof ParseException) {
      {if (true) throw (ParseException)jjte000;}
    }
    {if (true) throw (Error)jjte000;}
    } finally {
    if (jjtc000) {
      jjtree.closeNodeScope(jjtn000, true);
    }
    }
    throw new Error("Missing return statement in function");
  }

  public ParserTokenManager token_source;
  JavaCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[23];
  final private int[] jj_la1_0 = {0x0,0x0,0x0,0x400,0x400,0x0,0x0,0x3f80080,0x0,0x80,0x700,0x700,0x80,0x1f80000,0x6001f800,0x1f80000,0x6007f800,0x6007f800,0x0,0x0,0x10000000,0xf0001800,0x3f80000,};
  final private int[] jj_la1_1 = {0x20000,0x600,0x600,0x80000,0x80000,0x2000,0x40000,0x41880,0x200000,0x0,0x0,0x0,0x0,0x880,0x0,0x880,0x0,0x0,0x4000,0x80,0x0,0x7f,0x41880,};

  public Parser(java.io.InputStream stream) {
    jj_input_stream = new JavaCharStream(stream, 1, 1);
    token_source = new ParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 23; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 23; i++) jj_la1[i] = -1;
  }

  public Parser(java.io.Reader stream) {
    jj_input_stream = new JavaCharStream(stream, 1, 1);
    token_source = new ParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 23; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 23; i++) jj_la1[i] = -1;
  }

  public Parser(ParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 23; i++) jj_la1[i] = -1;
  }

  public void ReInit(ParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 23; i++) jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;

  final public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[54];
    for (int i = 0; i < 54; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 23; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 54; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

}
