package incremental.java.syntax.expr

import constraints.javacons._
import incremental.Node._
import incremental.SyntaxChecking._
import incremental.java.JavaCheck._
import incremental.java.syntax._
import incremental.java.syntax.expr.Expr._

/**
 * Created by qwert on 29.09.15.
 */

// Assignment Operators
case object Assign extends Expr(simple(ExprName.getClass, cExpr) orElse simple(classOf[FieldAccess], cExpr) orElse simple(ArrayAccess.getClass, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid]): StepResult = ???
}
case object AssignMul extends Expr(simple(ExprName.getClass, cExpr) orElse simple(classOf[FieldAccess], cExpr) orElse simple(ArrayAccess.getClass, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid]): StepResult = ???
}
case object AssignDiv extends Expr(simple(ExprName.getClass, cExpr) orElse simple(classOf[FieldAccess], cExpr) orElse simple(ArrayAccess.getClass, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid]): StepResult = ???
}
case object AssignRemain extends Expr(simple(ExprName.getClass, cExpr) orElse simple(classOf[FieldAccess], cExpr) orElse simple(ArrayAccess.getClass, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid]): StepResult = ???
}
case object AssignPlus extends Expr(simple(ExprName.getClass, cExpr) orElse simple(classOf[FieldAccess], cExpr) orElse simple(ArrayAccess.getClass, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid]): StepResult = ???
}
case object AssignMinus extends Expr(simple(ExprName.getClass, cExpr) orElse simple(classOf[FieldAccess], cExpr) orElse simple(ArrayAccess.getClass, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid]): StepResult = ???
}
case object AssignLeftShift extends Expr(simple(ExprName.getClass, cExpr) orElse simple(classOf[FieldAccess], cExpr) orElse simple(ArrayAccess.getClass, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid]): StepResult = ???
}
case object AssignRightShift extends Expr(simple(ExprName.getClass, cExpr) orElse simple(classOf[FieldAccess], cExpr) orElse simple(ArrayAccess.getClass, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid]): StepResult = ???
}
case object AssignURightShift extends Expr(simple(ExprName.getClass, cExpr) orElse simple(classOf[FieldAccess], cExpr) orElse simple(ArrayAccess.getClass, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid]): StepResult = ???
}
case object AssignAnd extends Expr(simple(ExprName.getClass, cExpr) orElse simple(classOf[FieldAccess], cExpr) orElse simple(ArrayAccess.getClass, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid]): StepResult = ???
}
case object AssignExcOr extends Expr(simple(ExprName.getClass, cExpr) orElse simple(classOf[FieldAccess], cExpr) orElse simple(ArrayAccess.getClass, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid]): StepResult = ???
}
case object AssignOr extends Expr(simple(ExprName.getClass, cExpr) orElse simple(classOf[FieldAccess], cExpr) orElse simple(ArrayAccess.getClass, cExpr)){
  def check(lits: Seq[Any], kids: Seq[Kid]): StepResult = ???
}