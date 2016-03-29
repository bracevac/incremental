package incremental.java.syntax.expr

import constraints.javacons._
import incremental.Node._
import incremental.java.JavaCheck._
import incremental.java.syntax._
import incremental.{Context, NodeKind, SyntaxChecking}
import incremental.java.syntax.expr.Expr._

/**
 * Created by qwert on 29.09.15.
 */

// Array Access
case object ArrayAccess extends Expr(simple(cExpr, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid], context: Context[Constraint]): Result = ???
}

// Array Creation
case object NewArray extends Expr(_ => ArrayCreationSyntax){
  def check(lits: Seq[Any], kids: Seq[Kid], context: Context[Constraint]): Result = ???
}

trait ArrayBaseType{} // extends PrimType with TypeName{}
case class UnboundWld(t: TypeName) extends ArrayBaseType

abstract class Dimension(syntaxcheck: SyntaxChecking.SyntaxCheck) extends NodeKind[Constraint, Result](syntaxcheck){
  def check(lits: Seq[Any], kids: Seq[Kid], context: Context[Constraint]): Result = ???
}
case object Dim extends Dimension(simple())
case object DimExpr extends Dimension(simple(cExpr))