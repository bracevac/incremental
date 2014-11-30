package incremental.pcf

import incremental.{EqConstraint, Type}
import incremental.ConstraintOps._
import incremental.Type.TSubst

/**
 * Created by seba on 13/11/14.
 */
//trait CheckResult {
//  (Type, Map[Symbol, Type], Unsolvable)
//}

case object TNum extends Type {
  def occurs(x: Symbol) = false
  def subst(s: TSubst) = this
  def unify(other: Type, s: TSubst) = other match {
    case TNum => emptySol
    case TVar(x) => other.unify(this, s)
    case _ => never(EqConstraint(this, other))
  }
}

case class TVar(x: Symbol) extends Type {
  def occurs(x2: Symbol) = x == x2
  def subst(s: Map[Symbol, Type]) = s.getOrElse(x, this)
  def unify(other: Type, s: TSubst) =
    if (other == this) emptySol
    else s.get(x) match {
      case Some(t) => t.unify(other, s)
      case None =>
        val t = other.subst(s)
        if (t.occurs(x))
          never(EqConstraint(this, t))
        else
          solution(Map(x -> t))
    }
}

case class TFun(t1: Type, t2: Type) extends Type {
  def occurs(x: Symbol) = t1.occurs(x) || t2.occurs(x)
  def subst(s: Map[Symbol, Type]) = TFun(t1.subst(s), t2.subst(s))
  def unify(other: Type, s: TSubst) = other match {
    case TFun(t1_, t2_) =>
      val sol1 = t1.unify(t1_, s)
      val sol2 = t2.unify(t2_, s)
      sol1 ++ sol2
    case TVar(x) => other.unify(this, s)
    case _ => never(EqConstraint(this, other))
  }
  override def toString= s"($t1 --> $t2)"
}
