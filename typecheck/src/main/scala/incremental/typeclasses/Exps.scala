package incremental.typeclasses

import constraints.equality.Type
import incremental.Node._
import incremental.{SyntaxChecking, NodeKind}

/**
 * Created by seba on 13/11/14.
 */

abstract class Exp(syntaxcheck: SyntaxChecking.SyntaxCheck) extends NodeKind(syntaxcheck)
object Exp {
  val cExp = classOf[Exp]
}
import Exp._

case object Num  extends Exp(simple(Seq(classOf[Integer])))
case object CFloat  extends Exp(simple(Seq(classOf[Integer])))
case object CDouble  extends Exp(simple(Seq(classOf[Integer])))
case object CInt  extends Exp(simple(Seq(classOf[Integer])))
case object CChar  extends Exp(simple(Seq(classOf[Symbol])))

case object TAdd  extends Exp(simple(cExp, cExp))
case object TMul  extends Exp(simple(cExp, cExp))

case object Add  extends Exp(simple(cExp, cExp))
case object Mul  extends Exp(simple(cExp, cExp))
case object Var  extends Exp(simple(Seq(classOf[Symbol])))
case object Abs  extends Exp(simple(Seq(classOf[Symbol]), cExp) orElse simple(Seq(classOf[Symbol], classOf[PolType]), cExp) orElse (simple(Seq(classOf[Seq[Symbol]]), cExp)))
case object TAbs extends Exp(simple(Seq(classOf[Symbol]), cExp))
case object App  extends Exp(simple(cExp, cExp))
case object TApp extends Exp(simple(Seq(classOf[PolType]), cExp))
case object If0  extends Exp(simple(cExp, cExp, cExp))
case object Fix  extends Exp(simple(cExp))
case object Let extends Exp(simple(Seq(classOf[Symbol]), cExp, cExp))
case object Inst extends Exp(simple(Seq(classOf[Symbol], classOf[PolType]), cExp, cExp))