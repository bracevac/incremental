package incremental.fjava

/**
 * Created by lirakuci on 10/26/16.
 */

import com.typesafe.config.ConfigException.Null
import constraints.Statistics
import constraints.fjavaBU.{ConstraintSystem, _}

import incremental.Node._
import incremental.{pcf, Util, Node_}
import incremental.fjava.ClassReqs
import incremental.fjava.Condition
import incremental.fjava.CtorCReq
import incremental.fjava.ExtCReq
import incremental.fjava.FieldCReq
import incremental.fjava.MethodCReq
/**
 * Created by lirakuci on 24/10/16.
 */

import incremental.fjava.Condition.trueCond

trait CTcls[T <: CTcls[T]] {
  def self: T
}

case class ExtCT(cls: CName, ext: CName) extends CTcls[ExtCT] {
  def self = this
 // def subst(s: CSubst) =  ExtCT(cls.subst(s), ext.subst(s))
  }
case class CtorCT(cls: CName, args: Seq[CName]) extends CTcls[CtorCT] {
  def self = this
 // def subst(s: CSubst) = CtorCReq(cls.subst(s), args.map(_.subst(s)))

}
case class FieldCT(cls: CName, field: Symbol, typ: CName) extends CTcls[FieldCT] {
  def self = this
 // def subst(s: CSubst) = FieldCReq(cls.subst(s), field, typ.subst(s))
}
case class MethodCT(cls: CName, name: Symbol, params: Seq[CName], ret: CName) extends CTcls[MethodCT] {
  def self = this
//  def subst(s: CSubst) = {
//    val cls_ = cls.subst(s)
//    cond.subst(cls_, s) map (MethodCReq(cls_, name, params.map(_.subst(s)), ret.subst(s), _))
//  }
}


case class CT (
                ext: Set[ExtCT] = Set(),
                ctorParams: Set[CtorCT] = Set(),
                fields: Set[FieldCT] = Set(),
                methods: Set[MethodCT] = Set())


case class UnboundVariable(x: Symbol, ctx: Map[Symbol, CName]) extends RuntimeException


case class BUCheckerFactory[CS <: ConstraintSystem[CS]](factory: ConstraintSystemFactory[CS]) extends TypeCheckerFactory[CS] {
  def makeChecker = new BUChecker[CS] {
    type CSFactory = factory.type
    implicit val csFactory: CSFactory = factory
  }
}


abstract class BUChecker[CS <: ConstraintSystem[CS]] extends TypeChecker[CS] {

  import csFactory._

  type Ctx = Map[Symbol, CName]

  type StepResult = (CName, Seq[Constraint], Seq[CS])

  type TError = String

  type Result = (CName, CS)

  val CURRENT_CLASS = '$current

  def field(f : Symbol, cls : CName, ct: CT): Seq[CName] = {
    val posFTyp = ct.fields.find(ftyp => (ftyp.field == f) && (ftyp.cls == cls) )
    posFTyp match {
      case None => ct.ext.toMap[CName,CName].get(cls)   match {
        case None => Seq()
        case Some(superCls) =>  field(f, superCls, ct)
      }
      case Some(fTyp) =>  Seq(fTyp.typ)
    }
  }

  def mtype(m : Symbol, cls : CName, ct: CT): Seq[CName] = {
    val posMTyp = ct.methods.find(ftyp => (ftyp.name == m) && (ftyp.cls == cls) )
    posMTyp match {
      case None => ct.ext.toMap[CName,CName].get(cls)   match {
        case None => Seq()
        case Some(superCls) =>  mtype(m, superCls, ct)
      }
      case Some(mTyp) => mTyp.params ++ Seq(mTyp.ret)
    }
  }

  def extend(cls : CName, ct : CT) : Seq[CName] = {
    ct.ext.toMap[CName, CName].get(cls) match {
      case None => Seq()
      case Some(superCls) => Seq(superCls) // or return ext(cls, superCls)
    }
  }

  def init(cls : CName, ct : CT) : Seq[CName] = { // return CtorCT
    ct.ctorParams.toMap[CName, Seq[CName]].get(cls) match {
      case None => Seq()
      case Some(ctorTyp) => ctorTyp
    }
  }

  def typecheckImpl(e: Node): Either[CName, TError] = {
    val root = e.withType[Result]

    Util.timed(localState -> Statistics.typecheckTime) {
      try{
        val (t, sol_) = typecheckRec(root, Map(), CT())
        val cs = if (e.kind != ClassDec) sol_ else {
          // add inheritance to constraint system
          val c = e.lits(0).asInstanceOf[GroundType]
          val sup = e.lits(1).asInstanceOf[GroundType]
          sol_.extendz(c, sup)
        }
        val sol = sol_.tryFinalize
      if (sol.isSolved)
        Left(t)
      else
        Right(s"Unresolved constraints ${sol.unsolved}, type ${t}")
      } catch {
        case ex: UnboundVariable => Right(s"Unbound variable ${ex.x} in context ${ex.ctx}")
      }
    }
  }

  def typecheckRec(e: Node_[Result], ctx: Ctx, ct : CT): Result = {
  val (t, cons, css) = typecheckStep (e, ctx, ct)
  val subcs = css.foldLeft (freshConstraintSystem) ((cs, res) => cs mergeSubsystem res)
  val cs = subcs addNewConstraints cons
  (cs applyPartialSolution t, cs.propagate)
  }

  def typecheckStep(e: Node_[Result], ctx: Ctx, ct: CT): StepResult = e.kind match {

    case Var =>
      val x = e.lits(0).asInstanceOf[Symbol]
      ctx.get(x) match {
        case None => throw UnboundVariable(x, ctx)
        case Some(t) => (t, Seq(), Seq())
      }
    case FieldAcc =>
      val f = e.lits(0).asInstanceOf[Symbol] //symbol
      val (t, cs) = typecheckRec(e.kids(0), ctx, ct) //subsol
      val fTyp = field(f,t, ct)
      (fTyp.head, Seq(), Seq(cs))

    case Invk =>
      val m = e.lits(0).asInstanceOf[Symbol]
      val (te, cse) = typecheckRec(e.kids(0), ctx, ct)
      val mtyp = mtype(m, te, ct)
      var cs = Seq[CS]()
      var cons = Seq[Constraint]()
      for (i <- 1 until e.kids.seq.size) {
        val (ti, csi) = typecheckRec(e.kids(i), ctx, ct)
        cons = cons :+ Subtype(ti.asInstanceOf[TypeBU], mtyp.toList(i).asInstanceOf[TypeBU])
        cs = cs ++ Seq(csi)
      }
      (mtyp.last , cons, Seq(cse) ++ cs)

    case New =>
      val c = e.lits(0).asInstanceOf[CName]
      val ctor = init(c, ct)
      var cons = Seq[Constraint]()
      var cs = Seq[CS]()
      for (i <- 0 until e.kids.seq.size) {
        val (ti, csi) = typecheckRec(e.kids(i), ctx, ct)
        cons =  cons :+ Subtype(ti,ctor.toList(i).asInstanceOf[TypeBU])
        cs = cs ++ Seq(csi)
      }
      (c, cons, cs)

    case UCast =>
      val (t, cs) = typecheckRec(e.kids(0), ctx, ct)
      val c = e.lits(0).asInstanceOf[CName]

      (c, Seq(Subtype(t.asInstanceOf[TypeBU], c.asInstanceOf[TypeBU])), Seq(cs))

    case DCast =>
      val (t, cs) = typecheckRec(e.kids(0), ctx, ct)
      val c = e.lits(0).asInstanceOf[CName]

      (c, Seq(Subtype(c.asInstanceOf[TypeBU], t.asInstanceOf[TypeBU]), NotEqual(c.asInstanceOf[TypeBU], t.asInstanceOf[TypeBU])), Seq(cs))

    case SCast =>
      val (t, cs) = typecheckRec(e.kids(0), ctx, ct)
      val c = e.lits(0).asInstanceOf[CName]

      (t, Seq(NotSubtype(c.asInstanceOf[TypeBU], t.asInstanceOf[TypeBU]), NotSubtype(t.asInstanceOf[TypeBU], c.asInstanceOf[TypeBU]), StupidCastWarning(t.asInstanceOf[TypeBU], c.asInstanceOf[TypeBU])), Seq(cs))

    case MethodDec =>
      val retT = e.lits(0).asInstanceOf[CName] // return type
      val m = e.lits(1).asInstanceOf[Symbol] // method name
      val params = e.lits(2).asInstanceOf[Seq[(Symbol, CName)]]

      var ctMO = ct

      val cls = ctx.get(CURRENT_CLASS) match {
        case None => Seq()
        case Some(cls) => Seq(cls)
      }
      val mtyp = mtype(m, extend(cls.head, ct).head, ct)
      //overide the signature of the method in super types
      if (!mtyp.isEmpty) ctMO = ct.copy(methods = ct.methods.filter(msupTyp => (msupTyp.name== m && msupTyp.cls == extend(cls.head, ct).head )) ++ Seq(MethodCT(extend(cls.head, ct).head, m, params.toMap.values.toSeq, retT)))

      val (bodyT, csb) = typecheckRec(e.kids(0), ctx, ctMO)
      var cons = Seq[Constraint]()

      // body type is subtype of declared return type
      cons = Subtype(bodyT.asInstanceOf[TypeBU], retT.asInstanceOf[TypeBU]) +: cons

        (MethodOK.asInstanceOf[CName], cons, Seq(csb))

    case ClassDec =>
      val c = e.lits(0).asInstanceOf[CName]
      val sup = e.lits(1).asInstanceOf[CName]
      val ctor = e.lits(2).asInstanceOf[Ctor]
      val fields = e.lits(3).asInstanceOf[Seq[(Symbol, CName)]].toMap

      var cs = Seq[CS]()
    //  var currentClassCons = Seq[Constraint]()

      // handle all methods, satisfying current-class reqs
      for (i <- 0 until e.kids.seq.size) {
        val (t, csi) = typecheckRec(e.kids(i), ctx + (CURRENT_CLASS -> c), ct)
        cs = cs ++ Seq(csi)

      }

      // constructor initializes all local  or super class fields
      val fieldSupInitCons = AllEqual(init(sup, ct).asInstanceOf[Seq[TypeBU]], ctor.superParams.values.toList.asInstanceOf[Seq[TypeBU]])
      // constructor provides correct arguments to super constructor

      //add the super class in CS solver
      (c, Seq(fieldSupInitCons), cs)

    case ProgramM =>

      var cs= Seq[CS]()

      var removeCons = Seq[Constraint]()
      var ctNew = CT()

      // remove class requirements
      for (cls <- e.kids.seq.reverseIterator) {
        val cname = cls.lits(0).asInstanceOf[CName]
        val sup = cls.lits(1).asInstanceOf[CName]
        val ctor = cls.lits(2).asInstanceOf[Ctor]
        val fields = cls.lits(3).asInstanceOf[Seq[(Symbol, CName)]].toMap
        val methods = cls.kids.seq

        ctNew = CT(ctNew.ext + ExtCT(cname, sup), ctNew.ctorParams + CtorCT(cname, ctor.superParams.values.toSeq ++ ctor.fields.values.toSeq ), ctNew.fields ++ fields.map(ftyp => FieldCT(cname, ftyp._1, ftyp._2)) , ctNew.methods ++ methods.map(mtyp => MethodCT(cname, mtyp.lits(1).asInstanceOf[Symbol],mtyp.lits(1).asInstanceOf[Seq[(Symbol, CName)]].map(_._2), mtyp.lits(0).asInstanceOf[CName] )) )
      }

      for (i <- 0 until e.kids.seq.size) {
        val (ct, csi) = typecheckRec(e.kids(i), ctx, ctNew)
        cs = cs ++ Seq(csi)
      }


      (ProgramOK.asInstanceOf[CName], Seq(), cs)

  }


}
