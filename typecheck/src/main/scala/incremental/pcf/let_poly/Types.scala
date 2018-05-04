package incremental.pcf.let_poly

import constraints.equality.CSubst
import constraints.equality.CSubst.CSubst
import constraints.CVar
import constraints.equality.{ConstraintSystem, ConstraintSystemFactory, EqConstraint, Type}

/**
 * Created by seba on 13/11/14.
 */

case class UVar(x: CVar[Type]) extends Type {
  def isGround = false
  def occurs(x2: CVar[_]) = x == x2
  def subst(s: CSubst):Type = s.hgetOrElse(x, this)
  def unify[CS <: ConstraintSystem[CS]](other: Type, cs: CS) =
    if (other == this) cs
    else if (other.isInstanceOf[USchema]) other.unify(this, cs)
    else cs.substitution.hget(x) match {
      case Some(t) => t.unify(other, cs)
      case None =>
        val t = other.subst(cs.substitution)
        if (this == t)
          cs
        else if (t.occurs(x))
          cs.never(EqConstraint(this, t))
        else
          cs.solved(CSubst(x -> t))
    }
}

case object TNum extends Type {
  def isGround = true
  def occurs(x: CVar[_]) = false
  def subst(s: CSubst) = this
  def unify[CS <: ConstraintSystem[CS]](other: Type, cs: CS) = other match {
    case TNum => cs
    case UVar(x) => other.unify(this, cs)
    case USchema(x) => other.unify(this, cs)
    case InstS(typ) => typ.unify(this, cs)
    case TSchema(typ, lst) => typ.unify(this, cs)
    case _ => cs.never(EqConstraint(this, other))
  }
}

case object TFloat extends Type {
  def isGround = true
  def occurs(x: CVar[_]) = false
  def subst(s: CSubst) = this
  def unify[CS <: ConstraintSystem[CS]](other: Type, cs: CS) = other match {
    case TFloat => cs
    case UVar(x) => other.unify(this, cs)
    case USchema(x) => other.unify(this, cs)
    case InstS(typ) => typ.unify(this, cs)
    case _ => cs.never(EqConstraint(this, other))
  }
}

case object TChar extends Type {
  def isGround = true
  def occurs(x: CVar[_]) = false
  def subst(s: CSubst) = this
  def unify[CS <: ConstraintSystem[CS]](other: Type, cs: CS) = other match {
    case TChar => cs
    case UVar(x) => other.unify(this, cs)
    case USchema(x) => other.unify(this, cs)
    case InstS(typ) => typ.unify(this, cs)
    case _ => cs.never(EqConstraint(this, other))
  }
}

case object TBool extends Type {
  def isGround = true
  def occurs(x: CVar[_]) = false
  def subst(s: CSubst) = this
  def unify[CS <: ConstraintSystem[CS]](other: Type, cs: CS) = other match {
    case TBool => cs
    case UVar(x) => other.unify(this, cs)
    case USchema(x) => other.unify(this, cs)
    case InstS(typ) => typ.unify(this, cs)
    case _ => cs.never(EqConstraint(this, other))
  }
}


case class TFun(t1: Type, t2: Type) extends Type {
  def isGround = true
  def occurs(x: CVar[_]) = t1.occurs(x) || t2.occurs(x)
  def subst(s: CSubst) = {
    var args = List(t1.subst(s))
    var res = t2
    while (res.isInstanceOf[TFun]) {
      val resfun = res.asInstanceOf[TFun]
      args = resfun.t1.subst(s) :: args
      res = resfun.t2
    }
    res = res.subst(s)
    for (a <- args)
      res = TFun(a, res)
    res
  }
  def unify[CS <: ConstraintSystem[CS]](other: Type, cs: CS) = other match {
    case TFun(t1_, t2_) => t2.unify(t2_, t1.unify(t1_, cs))
    case USchema(x) => other.unify(this, cs)
   // case TSchema(typ, lst) => typ.unify(this, cs)
   // case InstS(typ) => typ.unify(this, cs)
    case UVar(x) => other.unify(this, cs)
    case _ => cs.never(EqConstraint(this, other))
  }
  //
  override def toString= "TFun" //s"($t1 --> $t2)"
}

case class TSchema(typ : Type, lTyVar: Seq[UVar]) extends Type{
  def isGround = true
  def freeTVars = Set()
  def occurs(x: CVar[_]) = false
  def subst(s: CSubst) = typ.subst(s)
  def unify[CS <: ConstraintSystem[CS]](other: Type, cs: CS) = other match {
    case TSchema(t2, ltvar2) => cs
    case USchema(x) => other.unify(this, cs)
    //case UVar(x) => other.unify(this, cs)
    case _ => cs.never(EqConstraint(this, other))
  }
}

case class USchema(x : CVar[Type]) extends Type{
  def isGround = false
  def freeTVars = Set()
  def occurs(x2: CVar[_]) = x == x2
  def subst(s: CSubst) = s.hgetOrElse(x, this)
  def unify[CS <: ConstraintSystem[CS]](other: Type, cs: CS) =
    if (other == this) cs
    else cs.substitution.hget(x) match {
      case Some(t) => t.unify(other, cs)
      case None =>
        val t = other.subst(cs.substitution)
        if (this == t)
          cs
        else if (t.occurs(x))
          cs.never(EqConstraint(this, t))
        else
          cs.solved(CSubst(USchema(x).x -> t))
    }
}

case class InstS(typ : Type) extends Type {
  def isGround = false

  def freeTVars = Set()

  def occurs(x2: CVar[_]) = x2 == typ

  def instanti(typ: Type): Type = {
    if (typ.isGround) typ
    else {
      typ match {
        case UVar(x) => UVar(x)
        case TFun(t1, t2) => TFun(instanti(t1), instanti(t2))
        case _ => InstS(typ)
      }
    }
  }

  def subst(s: CSubst) = instanti(typ.subst(s))

  //    if (typ.subst(s).isGround) typ.subst(s)
  //    else if (typ.subst(s).isInstanceOf[UVar]) typ.subst(s)
  //    else if (typ.subst(s).isInstanceOf[TFun])
  //    else InstS(typ.subst(s))


  def instVar[CS <: ConstraintSystem[CS]](t: UVar, lts: Seq[UVar], cs: CS): Type = {
    if (lts.isEmpty) t
    else {
      if (lts.contains(t))
        UVar(cs.gen.freshSymbol("x$"))
      else t
    }
  }

  def inst[CS <: ConstraintSystem[CS]](t: Type, lts: Seq[UVar], cs: CS): Type = {
    t match {
      case UVar(x) => instVar(UVar(x), lts, cs)
      case TFun(t1, t2) => TFun(inst(t1, lts, cs), inst(t2, lts, cs))
      case _ => t
    }
  }

  def instFresh[CS <: ConstraintSystem[CS]](typ: Type, cs: CS): Type = {
    typ match {
      case USchema(x) => InstS(USchema(x))
      case UVar(x) => UVar(x)
      case TSchema(t, lts) => inst(t, lts, cs)
      case _ => typ
    }
  }

  def unify[CS <: ConstraintSystem[CS]](other: Type, cs: CS) = other match {
    case UVar(x) => other.unify(instFresh(this, cs), cs)
    case TNum => other.unify(this, cs)
    //case InstS(t2) => instFresh(other, cs).unify((instFresh(this, cs)), cs)
    case _ => cs.never(EqConstraint(this, other))
  }
}