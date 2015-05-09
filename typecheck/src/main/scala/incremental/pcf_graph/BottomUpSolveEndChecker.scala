package incremental.pcf_graph

import incremental.ConstraintOps._
import incremental.Node.Node
import incremental.Type.Companion._
import incremental.Node._
import incremental._

/**
 * Created by seba on 13/11/14.
 */
class BottomUpSolveEndChecker extends TypeChecker[Type] {

  val constraint = new ConstraintOps
  import constraint._

  var preparationTime = 0.0
  var typecheckTime = 0.0
  def constraintCount = constraint.constraintCount
  def mergeReqsTime = constraint.mergeReqsTime
  def constraintSolveTime = constraint.constraintSolveTime
  def mergeSolutionTime = constraint.mergeSolutionTime

  type Reqs = Map[Symbol, Type]

  type Result = (Type, Reqs, Seq[Constraint])

  def typecheck(e: Node): Either[Type, TError] = {
    val root = e.withType[Result]

//    val (uninitialized, ptime) = Util.timed {root.uninitialized}
//    preparationTime += ptime

    val (res, ctime) = Util.timed {
      root.visitUninitialized { e =>
        e.typ = typecheckStep(e)
        true
      }

      val (t_, reqs, cons) = root.typ
      val sol = solve(cons)
      val t = t_.normalize

      if (!reqs.isEmpty)
        Right(s"Unresolved context requirements $reqs, type $t, unres ${sol.unsolved}")
      else if (!sol.isSolved)
        Right(s"Unresolved constraints ${sol.unsolved}, type $t")
      else
        Left(t)
    }
    typecheckTime += ctime
    res
  }

  def typecheckStep(e: Node_[Result]): Result = e.kind match {
    case Num => (TNum, Map(), Seq())
    case op if op == Add || op == Mul =>
      val (t1, reqs1, sol1) = e.kids(0).typ
      val (t2, reqs2, sol2) = e.kids(1).typ

      val lcons = EqConstraint(TNum, t1)
      val rcons = EqConstraint(TNum, t2)

      val (mcons, mreqs) = mergeReqMaps(reqs1, reqs2)

      (TNum, mreqs, lcons +: rcons +: (sol1 ++ sol2 ++ mcons))
    case Var =>
      val x = e.lits(0).asInstanceOf[Symbol]
      val X = freshUVar()
      (X, Map(x -> X), Seq())
    case App =>
      val (t1, reqs1, sol1) = e.kids(0).typ
      val (t2, reqs2, sol2) = e.kids(1).typ

      val X = freshUVar()
      val fcons = EqConstraint(TFun(t2, X), t1)
      val (mcons, mreqs) = mergeReqMaps(reqs1, reqs2)

      (X, mreqs, fcons +: (sol1 ++ sol2 ++ mcons))
    case Abs if (e.lits(0).isInstanceOf[Symbol]) =>
      val x = e.lits(0).asInstanceOf[Symbol]
      val (t, reqs, subsol) = e.kids(0).typ

      reqs.get(x) match {
        case None =>
          val X = if (e.lits.size == 2) e.lits(1).asInstanceOf[Type] else freshUVar()
          (TFun(X, t), reqs, subsol)
        case Some(treq) =>
          val otherReqs = reqs - x
          if (e.lits.size == 2) {
            val acons = EqConstraint(e.lits(1).asInstanceOf[Type], treq)
            (TFun(treq, t), otherReqs, acons +: subsol)
          }
          else
            (TFun(treq, t), otherReqs, subsol)
      }
    case Abs if (e.lits(0).isInstanceOf[Seq[_]]) =>
      val xs = e.lits(0).asInstanceOf[Seq[Symbol]]
      val (t, reqs, subsol) = e.kids(0).typ

      val Xs = xs map (_ => freshUVar())

      var restReqs = reqs
      var tfun = t
      for (i <- xs.size-1 to 0 by -1) {
        val x = xs(i)
        restReqs.get(x) match {
          case None =>
            val X = freshUVar()
            tfun = TFun(X, tfun)
          case Some(treq) =>
            restReqs = restReqs - x
            tfun = TFun(treq, tfun)
        }
      }

      (tfun, restReqs, subsol)
    case If0 =>
      val (t1, reqs1, sol1) = e.kids(0).typ
      val (t2, reqs2, sol2) = e.kids(1).typ
      val (t3, reqs3, sol3) = e.kids(2).typ

      val (mcons12, mreqs12) = mergeReqMaps(reqs1, reqs2)
      val (mcons23, mreqs123) = mergeReqMaps(mreqs12, reqs3)

      val cond = EqConstraint(TNum, t1)
      val body = EqConstraint(t2, t3)

      (t2, mreqs123, cond +: body +: (sol1 ++ sol2 ++ sol3 ++ mcons12 ++ mcons23))

    case Fix =>
      val (t, reqs, subsol) = e.kids(0).typ
      val X = freshUVar()
      val fixCons = EqConstraint(t, TFun(X, X))
      (X, reqs, fixCons +: subsol)
  }
}

object BottomUpSolveEndCheckerFactory extends TypeCheckerFactory[Type] {
  def makeChecker = new BottomUpSolveEndChecker
}