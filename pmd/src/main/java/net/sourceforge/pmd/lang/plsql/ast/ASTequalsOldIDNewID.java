/* Generated By:JJTree: Do not edit this line. ASTequalsOldIDNewID.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package net.sourceforge.pmd.lang.plsql.ast;

public
class ASTequalsOldIDNewID extends SimpleNode {
  public ASTequalsOldIDNewID(int id) {
    super(id);
  }

  public ASTequalsOldIDNewID(PLSQLParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PLSQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=1c8902f986d5eb349866bc4a2677f3a3 (do not edit this line) */