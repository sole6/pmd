/* Generated By:JJTree: Do not edit this line. ASTcollection_type_name.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package net.sourceforge.pmd.lang.plsql.ast;

public
class ASTcollection_type_name extends SimpleNode {
  public ASTcollection_type_name(int id) {
    super(id);
  }

  public ASTcollection_type_name(PLSQLParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PLSQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=502ce61df6171f2be28242d72c4c33c5 (do not edit this line) */