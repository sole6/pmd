/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.annotation.InternalApi;

public class ASTArguments extends AbstractJavaNode {

    @InternalApi
    @Deprecated
    public ASTArguments(int id) {
        super(id);
    }

    @InternalApi
    @Deprecated
    public ASTArguments(JavaParser p, int id) {
        super(p, id);
    }

    public int getArgumentCount() {
        if (this.jjtGetNumChildren() == 0) {
            return 0;
        }
        return this.jjtGetChild(0).jjtGetNumChildren();
    }

    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
