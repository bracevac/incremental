package incremental.pcf.let_poly

import constraints.Statistics
import constraints.equality_letpoly._
import incremental.{Node_, Util}
import incremental.Node.Node


case class NonEmpty() extends RuntimeException

abstract class BUChecker[CS <: ConstraintSystem[CS]] extends TypeChecker[CS] {
  import csFactory._

  type TError = String
  type Reqs = Map[Symbol, Type]

  type StepResult = (Type, Reqs, Seq[Constraint])
  type Result = (Type, Reqs, CS)

  def typecheckImpl(e: Node): Either[Type, TError] = {
    try {
      val root = e.withType[Result]

      Util.timed(localState -> Statistics.typecheckTime) {
        root.visitUninitialized { e =>
          typecheckRec(e)
          true
        }

        val (t_, reqs, sol_) = root.typ
        val sol = sol_.tryFinalize
        val t = t_.subst(sol.substitution)

        def printtyp(t : Type) :Seq[Type] = {
          var seTyp = Seq[Type]()
          if (t.isInstanceOf[TFun]) {
            val TFun(t1 ,t2) = t.asInstanceOf[TFun]
            seTyp = seTyp ++ printtyp(t1) ++ printtyp(t2)

          }
          else
            seTyp = seTyp :+ t
          seTyp
        }

        println(s"Resulting type of the expression is ${printtyp(t).mkString(" -> ")}")

        if (!reqs.isEmpty)
          Right(s"Unresolved context requirements $reqs, type $t, unres ${sol.unsolved}")
        else if (!sol.isSolved)
          Right(s"Unresolved constraints ${sol.unsolved}, type $t")
        else
          Left(t)
      }
    }
    catch {
      case ex: NonEmpty => Right(s"List is empty, not able to access the head")
    }
  }

  def typecheckRec(e: Node_[Result]): Unit = {
    val res@(t, reqs, cons) = typecheckStep(e)
    val subcs = e.kids.seq.foldLeft(freshConstraintSystem)((cs, res) => cs mergeSubsystem res.typ._3)
    val cs = subcs addNewConstraints cons
    val reqs2 = cs.applyPartialSolutionIt[(Symbol, T), Map[Symbol, T], T](reqs, p => p._2)
    e.typ = (cs applyPartialSolution t, reqs2, cs.propagate)
  }

  def typecheckStep(e: Node_[Result]): StepResult = e.kind match {
    case Num => (TNum, Map(), Seq())
    case Float => (TFloat, Map(), Seq())
    case Char => (TChar, Map(), Seq())
    case Bool => (TBool, Map(), Seq())
    case True => (TBool, Map(), Seq())
    case False => (TBool, Map(), Seq())

    case TupleE =>
      val (t1, req1, _) = e.kids(0).typ
      val (t2, req2, _) = e.kids(1).typ

      val (mcons, mreq) = mergeReqMaps(req1, req2)

      (TupleL(t1, t2), mreq, mcons)

    case IsLower =>
      val (t1, req1, _) = e.kids(0).typ

      (TBool, req1, Seq(EqConstraint(t1, TChar)))

    case op if op == Add || op == Mul =>
      val (t1, reqs1, _) = e.kids(0).typ
      val (t2, reqs2, _) = e.kids(1).typ

      val lcons = EqConstraint(TNum, t1)
      val rcons = EqConstraint(TNum, t2)
      val (mcons, mreqs) = mergeReqMaps(reqs1, reqs2)

      (TNum, mreqs, mcons :+ lcons :+ rcons)

    case Var =>
      val x = e.lits(0).asInstanceOf[Symbol]
      val X = freshUVar()
      (X, Map(x -> X), Seq())
    case VarL =>
      val x = e.lits(0).asInstanceOf[Symbol]
      val u = freshUVar()
      val X = freshUSchema()
      val cons = InstConstraint(u, X)
      (u, Map(x -> X), Seq(cons))
    case App =>
      val (t1, reqs1, _) = e.kids(0).typ
      val (t2, reqs2, _) = e.kids(1).typ

      val X = freshUVar()
      val fcons = EqConstraint(TFun(t2, X), t1)
      val (mcons, mreqs) = mergeReqMaps(reqs1, reqs2)

      (X, mreqs, mcons :+ fcons)
    case Abs if (e.lits(0).isInstanceOf[Symbol]) =>
      val x = e.lits(0).asInstanceOf[Symbol]
      val (t, reqs, _) = e.kids(0).typ

      reqs.get(x) match {
        case None =>
          val X = if (e.lits.size == 2) e.lits(1).asInstanceOf[Type] else freshUVar()
          (TFun(X, t), reqs, Seq())
        case Some(treq) =>
          val otherReqs = reqs - x
          val X = freshUVar()
          if (e.lits.size == 2) {
            (TFun(X, t), otherReqs, Seq(EqConstraint(treq, X), EqConstraint(e.lits(1).asInstanceOf[Type], treq)))
          }
          else
            (TFun(X, t), otherReqs, Seq(EqConstraint(treq, X)))
      }
    case Abs if (e.lits(0).isInstanceOf[Seq[_]]) =>
      val xs = e.lits(0).asInstanceOf[Seq[Symbol]]
      val (t, reqs, _) = e.kids(0).typ

      val Xs = xs map (_ => freshUVar())

      var restReqs = reqs
      var tfun = t
      for (i <- xs.size - 1 to 0 by -1) {
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

      (tfun, restReqs, Seq())
    case If0 =>
      val (t1, reqs1, _) = e.kids(0).typ
      val (t2, reqs2, _) = e.kids(1).typ
      val (t3, reqs3, _) = e.kids(2).typ

      val (mcons, mreqs) = mergeReqMaps(reqs1, reqs2, reqs3)

      val cond = EqConstraint(TNum, t1)
      val body = EqConstraint(t2, t3)

      (t2, mreqs, mcons :+ cond :+ body)

    case Fix =>
      val (t, reqs, _) = e.kids(0).typ
      val X = freshUVar()
      val fixCons = EqConstraint(t, TFun(X, X))
      (X, reqs, Seq(fixCons))

    case LetV =>
      val (tb, reqsb, _) = e.kids(1).typ

      val (td, reqsd, _) = e.kids(0).typ
      val X = e.lits(0).asInstanceOf[Symbol]

      var rreqsb = reqsb
      var cons = Seq[Constraint]()

      reqsb.get(X) match {
        case None => cons
        case Some(typ) => rreqsb = rreqsb - X
          cons = cons :+ GenConstraint(typ, td, reqsd, reqsb)
      }

      val (mcons, mreq) = mergeReqMaps(reqsd, rreqsb)

      (tb, mreq, mcons ++ cons)

    case LetRec =>

      val X = e.lits(0).asInstanceOf[Symbol]

      var cons = Seq[Constraint]()

      val (t1, reqs1, _) = e.kids(0).typ
      var rreqs1 = reqs1
      reqs1.get(X) match {
        case None => cons
        case Some(typ) => rreqs1 = rreqs1 - X
          cons = cons :+ EqConstraint(typ, t1)
      }

      val (t2, reqs2, _) = e.kids(1).typ
      var rreqs2 = reqs2
      rreqs2.get(X) match {
        case None => cons
        case Some(typ) => rreqs2 = rreqs2 - X
          cons = cons :+ GenConstraint(typ, t1, reqs1, reqs2)
      }


      val (mcons, mreq) = mergeReqMaps(rreqs1, rreqs2)

      (t2, mreq, mcons ++ cons)

    case IfElse =>
      val (t1, reqs1, _) = e.kids(0).typ
      val (t2, reqs2, _) = e.kids(1).typ
      val (t3, reqs3, _) = e.kids(2).typ

      val (mcons, mreqs) = mergeReqMaps(reqs1, reqs2, reqs3)

      val cond = EqConstraint(TBool, t1)
      val body = EqConstraint(t2, t3)

      (t2, mreqs, mcons :+ cond :+ body)

    case Match =>
      val (eL, reqsL, _) = e.kids(0).typ
      val (eR, reqsR, _) = e.kids(1).typ

      val (eF, reqsF, _) = e.kids(2).typ
      val TFun(te, TFun(tl, tr))  = eF.asInstanceOf[TFun]

      var cons = Seq[Constraint]()
      cons = cons :+  EqConstraint(eL, tl) :+ EqConstraint(eR, tr) :+ EqConstraint(eL, ListT(Some(te))) //ListEqConstraint(eL.getTyp, te) :+

      val (mcons, mreq) = mergeReqMaps(reqsL, reqsR, reqsF)

      (tr, mreq, mcons ++ cons)

    case MatchP =>
      val (l, reqs, _) = e.kids(0).typ
      val (e1, reqs1,  _) = e.kids(1).typ
      val (e2, reqs2, _) = e.kids(2).typ
      val (e3, reqs3, _) = e.kids(3).typ

      val TFun(e1E, e1R) = e1.asInstanceOf[TFun]
      val TFun(e2E, TFun(e2L, e2R))  = e2.asInstanceOf[TFun]

      var cons = Seq[Constraint]()
      cons = cons :+  EqConstraint(l, ListT(Some(e1E))) :+ EqConstraint(l, ListT(Some(e2E))) :+ EqConstraint(l, e2L) :+ EqConstraint(e1R, e2R) :+ EqConstraint(e1R, e3)

      val (mcons, mreq) = mergeReqMaps(reqs, reqs1, reqs2, reqs3)

      (e1R, mreq, mcons ++ cons)


    case Error =>
      val s = e.lits(0).asInstanceOf[String]
      val X = freshUVar()

      (X, Map(), Seq())

    //------------ LIST ---------------------//

    case ListL =>
      var cons = Seq[Constraint]()
      if (e.kids.seq.isEmpty)
        (ListT(None), Map(), cons)
      else {
        val (t1, req1, _) = e.kids(0).typ
        var reqs = Seq(req1)
        for (i <- 1 until e.kids.seq.size) {
          val (t, req, _) = e.kids.seq(i).typ
          cons = cons :+ EqConstraint(t1, t)
          reqs = reqs :+ req
        }
        val (mcons, mreq) = mergeReqMaps(reqs)

        (ListT(Some(t1)), mreq, cons ++ cons)
      }

    case AppendE =>
      val (tL, req1, _) = e.kids(0).typ
      val (tE, req2, _) = e.kids(1).typ
      val (mcon, mreq) = mergeReqMaps(req1, req2)

      (tL, mreq, mcon :+ ListEqConstraint(tL, tE))

    case ++ =>
      val (l1, reqs1, _) = e.kids(0).typ
      val (l2, reqs2, _) = e.kids(1).typ

      val (mcons, mreq) = mergeReqMaps(reqs1, reqs2)


      if (e.kids(0).kids.seq.isEmpty && e.kids(1).kids.seq.isEmpty)
        (ListT(None), Map(), Seq())
      else if (e.kids(0).kids.seq.isEmpty)
        (l2, mreq, mcons :+ ListEqConstraint(l1, l2))
      else
        (l1, mreq, mcons :+ ListEqConstraint(l1, l2))

    case +: =>
      val (te, ereqs, _) = e.kids(0).typ
      val (tl, lreqs, _) = e.kids(1).typ

      val (mcons, mreqs) = mergeReqMaps(ereqs, lreqs)

      (tl, mreqs, mcons :+ EqConstraint(tl, ListT(Some(te))))

    case && =>
      val (t1, reqs1, _) = e.kids(0).typ
      val (t2, reqs2, _) = e.kids(1).typ

      val (mcons, mreq) = mergeReqMaps(reqs1, reqs2)

      (TBool, mreq, mcons :+ EqConstraint(t1, TBool) :+ EqConstraint(t2, TBool))

    case || =>
      val (t1, reqs1, _) = e.kids(0).typ
      val (t2, reqs2, _) = e.kids(1).typ

      val (mcons, mreq) = mergeReqMaps(reqs1, reqs2)

      (TBool, mreq, mcons :+ EqConstraint(t1, TBool) :+ EqConstraint(t2, TBool))



    case Head =>
      val (t, reqs, _) = e.kids(0).typ
      var X = freshUVar()
      //TODO Check the case when list is empty
      (X, reqs, Seq(EqConstraint(t, ListT(Some(X)))))

    case Last =>
      val (t, reqs, _) = e.kids(0).typ
      val X = freshUVar()
      //TODO Check the case when list is empty
      (X, reqs, Seq(EqConstraint(t, ListT(Some(X)))))


    case Tail =>
      val (t, reqs, _) = e.kids(0).typ
      val X = freshUVar()
      //TODO Check the case when list is empty
      (t, reqs, Seq(EqConstraint(t, ListT(Some(X)))))


    case Init =>
      val (tl, reqs, _) = e.kids(0).typ

//      if (e.kids(0).kids.seq.isEmpty) {
//        throw new NonEmpty()
//      }
//      else {
//        var resRes = Seq[Reqs]()
//        var cons = Seq[Constraint]()
//        for ( i <- 0 until e.kids(0).kids.seq.size - 1) {
//          val (t, req, _) = e.kids(0).kids.seq(i).typ
//          resRes = resRes :+ req
//          cons = cons :+ EqConstraint(tl, ListT(Some(t)))
//        }
//          val (mcons, mreq) = mergeReqMaps(resRes)
        (tl, reqs, Seq())
  }

  private val init: (Seq[Constraint], Reqs) = (Seq(), Map())

  def mergeReqMaps(req: Reqs, reqs: Reqs*): (Seq[Constraint], Reqs) = mergeReqMaps(req +: reqs)

  def mergeReqMaps(reqs: Seq[Reqs]): (Seq[Constraint], Reqs) =
    Util.timed(localState -> Statistics.mergeReqsTime) {
      reqs.foldLeft[(Seq[Constraint], Reqs)](init)(_mergeReqMaps)
    }

  private def _mergeReqMaps(was: (Seq[Constraint], Reqs), newReqs: Reqs) = {
    val wasReqs = was._2
    var mcons = was._1
    var mreqs = wasReqs
    for ((x, r2) <- newReqs)
      wasReqs.get(x) match {
        case None => mreqs += x -> r2
        case Some(r1) =>  mcons = EqConstraint(r1, r2) +: mcons
      }
    (mcons, mreqs)
  }

}



case class BUCheckerFactory[CS <: ConstraintSystem[CS]](factory: ConstraintSystemFactory[CS]) extends TypeCheckerFactory[CS] {
  def makeChecker = new BUChecker[CS] {
    type CSFactory = factory.type
    implicit val csFactory: CSFactory = factory
  }
}