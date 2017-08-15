package constraints.fjavaMO.impl

import constraints.{CTermBase, CVar, Statistics}
import constraints.fjavaMO.CSubst.CSubst
import incremental.fjavaMO.{CName, UCName}
import constraints.fjavaMO._
import incremental.Util
import incremental.fjavaMO.latemerge.{Condition, Conditional}

import scala.collection.generic.CanBuildFrom

object SolveContinuousSubstLateMerge extends ConstraintSystemFactory[SolveContinuousSubstCSLateMerge] with Serializable {
  def freshConstraintSystem = SolveContinuousSubstCSLateMerge(Map(), Map(), Seq(), Seq(), Map(), Map())
}

case class SolveContinuousSubstCSLateMerge(substitution: CSubst, bounds: Map[Type, Set[Type]], _notyet: Seq[Constraint], never: Seq[Constraint], extend: Map[GroundType, GroundType], minsel: Map[Seq[Type], Seq[Seq[Type]]]) extends ConstraintSystem[SolveContinuousSubstCSLateMerge] {
  //invariant: substitution maps to ground types
  //invariant: there is at most one ground type in each bound, each key does not occur in its bounds, keys of solution and bounds are distinct

  def state = SolveContinuousSubstLateMerge.state.value

  def notyet = {
    var cons = _notyet
    for ((t, tbnds) <- bounds; bound <- tbnds) {
      cons = Subtype(t, bound) +: cons
    }
    cons
  }

  def notyet(c: Constraint) = {
    val c_ = c.asInstanceOf[Conditional]
    SolveContinuousSubstCSLateMerge(substitution, bounds, c +: _notyet, never, extend, minsel)
  }

  def never(c: Constraint) = SolveContinuousSubstCSLateMerge(substitution, bounds, _notyet, c +: never, extend, minsel)

  def mergeSubsystem(that: SolveContinuousSubstCSLateMerge) = {
    var msubst = substitution ++ that.substitution
    var mnotyet = _notyet ++ that._notyet
    var mnever = never ++ that.never
    val init = SolveContinuousSubstCSLateMerge(msubst, this.bounds, mnotyet, mnever, this.extend, this.minsel)
    val extendedCS = that.extend.foldLeft(init) { case (cs, (t1, t2)) => cs.extendz(t1, t2) }
    that.bounds.foldLeft(extendedCS) { case (cs, (t, ts)) =>
      ts.foldLeft(cs) { case (cs2, t2) => cs2.addUpperBound(t, t2) }
    }
    val minselCS  = that.minsel.foldLeft(init) { case (cs, (t1, t2)) => cs.MinSelT(t1, t2, t1.drop(t1.length/2)) }
    that.minsel.foldLeft(minselCS) { case (cs, (t, ts)) =>
      ts.foldLeft(cs) { case (cs2, t2) => cs2.addMinSel(t, t2) }
    }
  }


  def addNewConstraint(c: Constraint) = {
    state += Statistics.constraintCount -> 1
    Util.timed(state -> Statistics.constraintSolveTime) {
      c.solve(this).trySolve
    }
  }

  def addNewConstraints(cons: Iterable[Constraint]) = {
    state += Statistics.constraintCount -> cons.size
    Util.timed(state -> Statistics.constraintSolveTime) {
      cons.foldLeft(this)((cs, c) => c.solve(cs)).trySolve
    }
  }

  def tryFinalize: SolveContinuousSubstCSLateMerge =
    Util.timed(state -> Statistics.finalizeTime) {
      tryFinalize(0)
    }

  def tryFinalize(steps: Int): SolveContinuousSubstCSLateMerge = {
    var newBounds = Map[Type, Set[Type]]()
    var lt = Set[Type]()
    for ((low, ups) <- this.bounds) {
      val newlow = low.subst(substitution)
      val newups = ups.map(_.subst(substitution))
      lt = newups
      if (newlow.isGround && (extend.contains(newlow.asInstanceOf[GroundType]) || newlow == CName('Object))) {
        newups.foreach { f =>
          if (isSubtype(newlow, f))
            lt = lt - f
          else lt
        }
      }
      if (lt.isEmpty) newBounds = newBounds
      else newBounds = newBounds + (newlow -> lt)
    }
    //    for ((cvar, setT ) <- this.minsel){
    //
    //    }
    val newcs = _notyet.foldLeft(copy(bounds = newBounds, _notyet = Seq()))((cs, cons) => cons.solve(cs))

    val startSize = notyet.size
    val endSize = newcs.notyet.size
    if (endSize > 0 && endSize < startSize)
      newcs.tryFinalize(steps + 1)
    else
      newcs
  }

  def trySolve: SolveContinuousSubstCSLateMerge = this

  def extendz(t1: GroundType, t2: GroundType) = {
    if (t1 == t2 || isSubtype(t2, t1))
      never(Subtype(t1, t2))
    else {
      t1 match {
        case CName('Object) =>
          this.never(Subtype(t1, t2))
        case _ =>
          extend.get(t1) match {
            case None =>
              extendMap(t1, t2)
            case Some(t3) =>
              Equal(t2, t3).solve(this)
          }
      }
    }
  }

  private def extendMap(t1: GroundType, t2: GroundType) =
    SolveContinuousSubstCSLateMerge(this.substitution, this.bounds, this._notyet, this.never, this.extend + (t1 -> t2), this.minsel)


  def isSubtype(t1: Type, t2: Type): Boolean =
    if (t1 == t2)
      true
    else if (t2 == CName('Object))
      true
    else if (t1 == CName('Object))
      false
    else if (t1.isGround)
      extend.get(t1.asInstanceOf[GroundType]) match {
        case None => false
        case Some(u) => u == t2 || isSubtype(u, t2)
      }
    else
      false


  def isAllSubtype(t1: Seq[Type], t2: Seq[Type]): Boolean = {
    var res = true
    for (i <- 0 until t1.length)
      if (!isSubtype(t1(i), t2(i)))
        res = false
    res
  }

  def minsel(setT: Seq[Seq[Type]], lowerB: Seq[Type]): Seq[Type] = {
    val len = lowerB.length
    var res: Seq[Type] = Seq.fill(len)(CName('Object))
    for (i <- 0 until setT.size)
      if (isAllSubtype(lowerB, setT(i)) && isAllSubtype(setT(i), res))
        res = setT(i)
      else res
    res
  }

  def MinSelT(cvar: Seq[Type], setT: Seq[Seq[Type]], lowerB: Seq[Type]) = {
    if (cvar.size != lowerB.size)
      SolveContinuousSubstCSLateMerge(this.substitution, this.bounds, this._notyet, this.never, this.extend, this.minsel)
    else {
      var newcs = SolveContinuousSubstCSLateMerge(this.substitution, this.bounds, this._notyet, this.never, this.extend, this.minsel)
      for (i <- 0 until cvar.size)
        newcs = cvar(i).unify(minsel(setT, lowerB)(i), newcs)
      newcs
    }
  }
//TODO lira encounter the case when it is not all CNAME or other corner cases
    def addMinSel(cvar1 : Seq[Type], seqT : Seq[Type]) : SolveContinuousSubstCSLateMerge =
      if (cvar1.length == seqT.length )
        this
      else
        extendMinSel(cvar1, seqT)

  private def extendMinSel(cvar1: Seq[Type], seqT: Seq[Type]) = {
  val var1 = minsel.getOrElse(cvar1, Seq[Seq[Type]]())
  SolveContinuousSubstCSLateMerge(this.substitution, this.bounds, this._notyet, this.never, this.extend, minsel + (cvar1 -> (var1 :+ seqT)))
}

  def addUpperBound(t1: Type, t2: Type): SolveContinuousSubstCSLateMerge =
    if (isSubtype(t1, t2))
      this
    else (t1, t2) match {
      case (CName('Object), UCName(x)) =>
        Equal(t1, t2).solve(this)
      case (CName('Object), CName(n)) if n != 'Object =>
        this.never(Subtype(t1, t2))
      case _  =>
        extendBound(t1, t2)
    }


  private def extendBound(t1: Type, t2: Type) = {
    val t1bnds = bounds.getOrElse(t1, Set[Type]())
    SolveContinuousSubstCSLateMerge(this.substitution, bounds + (t1 -> (t1bnds + t2)), this._notyet, this.never, this.extend, this.minsel)
  }

  def shouldApplySubst: Boolean = true

  def applyPartialSolution[CT <: constraints.CTerm[Gen, Constraint, CT]](t: CT) = t.subst(substitution)

  def applyPartialSolutionIt[U, C <: Iterable[U], CT <: constraints.CTerm[Gen, Constraint, CT]]
    (it: C, f: U=>CT)
    (implicit bf: CanBuildFrom[Iterable[U], (U, CT), C])
  = it.map(u => (u, f(u).subst(substitution)))

  def propagate: SolveContinuousSubstCSLateMerge = {
    if (substitution.isEmpty)
      return this

    var newBounds = Map[Type, Set[Type]]()
    bounds.foreach { case (low, ups) =>
      val newlow = low.subst(substitution)
      val newups = ups.flatMap { up =>
        val newup = up.subst(substitution)
        if (isSubtype(newlow, newup)) None
        else Some(newup)
      }
      if (newups.nonEmpty)
        newBounds.get(newlow) match {
          case None => newBounds += (newlow -> newups)
          case Some(currentUps) => newBounds += (newlow -> (currentUps ++ newups))
        }
    }

    val newNever = never.map(_.subst(substitution))
    var newNotyet = _notyet.map(_.subst(substitution))

    SolveContinuousSubstCSLateMerge(Map(), newBounds, newNotyet, newNever, extend, minsel)
  }

  def solved(s: CSubst): SolveContinuousSubstCSLateMerge = {
    var mysubst = this.substitution.mapValues(x => x.subst(s)).view.force
    var newcons = Seq[Constraint]()
    for ((x, t2) <- s) {
      mysubst.get(x) match {
        case None => mysubst = mysubst + (x -> t2.subst(mysubst))
        case Some(t1) => newcons = t1.compatibleWith(t2) +: newcons
      }
    }
    SolveContinuousSubstCSLateMerge(mysubst, bounds, this._notyet, this.never, this.extend, this.minsel).addNewConstraints(newcons)
  }

}