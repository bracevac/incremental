package incremental.fjava.earlymerge

import constraints.fjava.CSubst.CSubst
import constraints.fjava._
import Condition.trueCond
import incremental.Util
import incremental.fjava.CName

import scala.collection.generic.CanBuildFrom

trait CReq[T <: CReq[T]] {
  def self: T
  val cls: Type
  def withCls(t: Type): T = withCls(t, Condition(Set(), Set(), cond.others + (cls -> Condition(cond.not, cond.same, Map()))))
  def withCls(t: Type, newcond: Condition): T
  val cond: Condition
  def canMerge(other: CReq[T]): Boolean
  def assert(other: CReq[T]): Option[Constraint] = for (c2 <- alsoCond(other.cond); c3 <- c2.alsoSame(other.cls)) yield assert(other, c3.cond)
  def assert(other: CReq[T], cond: Condition): Constraint
  def subst(s: CSubst): Option[T]

  def withCond(cond: Condition): T
  def alsoNot(n: Type): Option[T] = cond.alsoNot(cls, n) map (withCond(_))
  def alsoSame(n: Type): Option[T] = cond.alsoSame(cls, n) map (withCond(_))
  def alsoCond(other: Condition): Option[T] = cond.alsoCond(cls, other) map (withCond(_))
}
case class ExtCReq(cls: Type, ext: Type, cond: Condition = trueCond) extends CReq[ExtCReq] {
  def self = this
  def withCls(t: Type, newcond: Condition) = copy(cls=t, cond=newcond)
  def canMerge(other: CReq[ExtCReq]) = true
  def assert(other: CReq[ExtCReq], cond: Condition) = Conditional(cls, cond, Equal(ext, other.self.ext))
  def subst(s: CSubst) = {
    val cls_ = cls.subst(s)
    cond.subst(cls_, s) map (ExtCReq(cls_, ext.subst(s), _))
  }
  def withCond(c: Condition) = copy(cond = c)
}
case class CtorCReq(cls: Type, args: Seq[Type], cond: Condition = trueCond) extends CReq[CtorCReq] {
  def self = this
  def withCls(t: Type, newcond: Condition) = copy(cls=t, cond=newcond)
  def canMerge(other: CReq[CtorCReq]) = true
  def assert(other: CReq[CtorCReq], cond: Condition) = Conditional(cls, cond, AllEqual(args, other.self.args))
  def subst(s: CSubst) = {
    val cls_ = cls.subst(s)
    cond.subst(cls_, s) map (CtorCReq(cls_, args.map(_.subst(s)), _))
  }
  def withCond(c: Condition) = copy(cond = c)
}
case class FieldCReq(cls: Type, field: Symbol, typ: Type, cond: Condition = trueCond) extends CReq[FieldCReq] {
  def self = this
  def withCls(t: Type, newcond: Condition) = copy(cls=t, cond=newcond)
  def canMerge(other: CReq[FieldCReq]): Boolean = field == other.self.field
  def assert(other: CReq[FieldCReq], cond: Condition) = Conditional(cls, cond, Equal(typ, other.self.typ))
  def subst(s: CSubst) = {
    val cls_ = cls.subst(s)
    cond.subst(cls_, s) map (FieldCReq(cls_, field, typ.subst(s), _))
  }
  def withCond(c: Condition) = copy(cond = c)
}
case class MethodCReq(cls: Type, name: Symbol, params: Seq[Type], ret: Type, optionallyDefined: Boolean = false, cond: Condition = trueCond) extends CReq[MethodCReq] {
  def self = this
  def withCls(t: Type, newcond: Condition) = copy(cls=t, cond=newcond)
  def canMerge(other: CReq[MethodCReq]): Boolean = name == other.self.name
  def assert(other: CReq[MethodCReq], cond: Condition) = Conditional(cls, cond, AllEqual(params :+ ret, other.self.params :+ other.self.ret))
  def subst(s: CSubst) = {
    val cls_ = cls.subst(s)
    cond.subst(cls_, s) map (MethodCReq(cls_, name, params.map(_.subst(s)), ret.subst(s), optionallyDefined, _))
  }
  def withCond(c: Condition) = copy(cond = c)
}

object Condition {
  val trueCond = new Condition(Set(), Set(), Map()) {
    override def subst(cls: Type, s: CSubst): Option[Condition] = Some(this)
    override def alsoNot(cls: Type, n: Type): Option[Condition] = Some(this)
    override def alsoSame(cls: Type, n: Type): Option[Condition] = Some(this)
    override def alsoCond(cls: Type, other: Condition): Option[Condition] = Some(this)
    override def isGround: Boolean = true
  }
}
case class Condition(not: Set[Type], same: Set[Type], others: Map[Type, Condition]){
  def subst(cls: Type, s: CSubst): Option[Condition] = {
    val newnot = not flatMap { n =>
      val n2 = n.subst(s)
      if (cls == n2)
        return None
      else if (cls.isGround && n2.isGround) // && cls != n2 (implicit)
        None
      else
        Some(n2)
    }
    val newsame = same flatMap { n =>
      val n2 = n.subst(s)
      if (cls == n2)
        None
      else if (cls.isGround && n2.isGround) // && cls != n2 (implicit)
        return None
      else
        Some(n2)
    }
    val newothers = others map { kv =>
      val cls = kv._1.subst(s)
      kv._2.subst(cls, s) match {
        case Some(cond) => cls -> cond
        case None => return None
      }
    }
    Some(Condition(newnot, newsame, newothers))
  }

  def alsoNot(cls: Type, n: Type): Option[Condition] =
    if (cls == n || same.contains(n))
      None
    else
      Some(Condition(not + n, same, others))
  def alsoSame(cls: Type, n: Type): Option[Condition] =
    if (cls.isGround && n.isGround && cls != n || not.contains(n))
      None
    else
      Some(Condition(not, same + n, others))
  def alsoCond(cls: Type, other: Condition): Option[Condition] =
    if (other.not.contains(cls) || other.not.exists(same.contains(_)) || not.exists(other.same.contains(_)))
      None
    else
      Some(Condition(not ++ other.not, same ++ other.same, others))

  def isGround: Boolean =
    not.forall(_.isGround) && same.forall(_.isGround) && others.forall(_._2.isGround)
}

case class Conditional(cls: Type, cond: Condition, cons: Constraint) extends Constraint {
  def solve[CS <: ConstraintSystem[CS]](cs: CS) = {
    val cls_ = cls.subst(cs.substitution)
    cond.subst(cls_, cs.substitution) match {
      case None => cs // discard this constraint because condition is false
      case Some(cond_) if cond_.isGround => cons.solve(cs)
      case Some(cond_) => cs.notyet(Conditional(cls_, cond_, cons.subst(cs.substitution)))
    }
  }

  override def subst(s: CSubst): Constraint = {
    val cls_ = cls.subst(s)
    Conditional(cls_, cond.subst(cls_, s).getOrElse(Condition.trueCond), cons.subst(s))
  }
}



case class ClassReqs (
                       currentCls: Option[Type] = None,
                       exts: Set[ExtCReq] = Set(),
                       ctors: Set[CtorCReq] = Set(),
                       fields: Set[FieldCReq] = Set(),
                       methods: Set[MethodCReq] = Set(),
                       optMethods: Set[MethodCReq] = Set()) {

  override def toString =
    s"ClassReqs(current=$currentCls, ext=$exts, ctorParams=$ctors, fields=$fields, methods=$methods, optMethods=$optMethods)"

  def subst(s: CSubst): ClassReqs = ClassReqs(
    currentCls.map(_.subst(s)),
    exts.flatMap(_.subst(s)),
    ctors.flatMap(_.subst(s)),
    fields.flatMap(_.subst(s)),
    methods.flatMap(_.subst(s)),
    optMethods.flatMap(_.subst(s)))

  def isEmpty = currentCls.isEmpty && exts.isEmpty && ctors.isEmpty && fields.isEmpty && methods.isEmpty

  def merge(crs: ClassReqs): (ClassReqs, Seq[Constraint]) = {
    val (currentX, cons0) = (currentCls.orElse(crs.currentCls), for (t1 <- currentCls; t2 <- crs.currentCls) yield Equal(t1, t2))
    val (extX, cons1) = merge(exts, crs.exts)
    val (ctorX, cons2) = merge(ctors, crs.ctors)
    val (fieldsX, cons3) = merge(fields, crs.fields)
    val (methodsX, cons4) = merge(methods, crs.methods)
    val (optMethodsX, cons5) = merge(optMethods, crs.optMethods)
    val cons = cons0.toSeq ++ cons1 ++ cons2 ++ cons3 ++ cons4 ++ cons5
    (ClassReqs(currentX, extX, ctorX, fieldsX, methodsX, optMethodsX), cons)
  }

  private def merge[T <: CReq[T]](crs1: Set[T], crs2: Set[T]): (Set[T], Seq[Constraint]) = {
    if (crs1.isEmpty)
      return (crs2, Seq())

    Util.loop[Set[T], T, Constraint](addRequirement)(crs1, crs2)
  }

  def addRequirement[T <: CReq[T]](crs: Set[T], req: T): (Set[T], Seq[Constraint]) = {
    var diffreq: Option[T] = Some(req)
    val diffcres = crs.flatMap{ creq =>
      if (creq.canMerge(req)) {
        val diff = creq.alsoNot(req.cls)
        val same = creq.alsoSame(req.cls).flatMap(_.alsoCond(req.cond))
        diffreq = diffreq.flatMap(_.alsoNot(creq.cls))
        Seq(diff, same).flatten
      }
      else
        Seq(creq)
    }
    (diffcres ++ diffreq, Seq())
  }

  def satisfyExtends(cls: Type, superT: Type): (ClassReqs, Seq[Constraint]) = {
    val ext = ExtCReq(cls, superT)
    val (creqs, cons) = satisfyCReq[ExtCReq](ext, this.exts, x=>copy(exts=x))
    val newFields = satisfyExt(ext, creqs.fields)
    val newMethods = satisfyExt(ext, creqs.methods)
    val newOptMethods = satisfyExt(ext, creqs.optMethods)
    (creqs.copy(fields = newFields, methods = newMethods, optMethods = newOptMethods), cons)
  }

  def satisfyCtor(cls: Type, args: Seq[Type]): (ClassReqs, Seq[Constraint]) =
    satisfyCReq[CtorCReq](CtorCReq(cls, args), ctors, x=>copy(ctors=x))

  def satisfyField(cls: Type, field: Symbol, typ: Type): (ClassReqs, Seq[Constraint]) =
    satisfyCReq[FieldCReq](FieldCReq(cls, field, typ), fields, x=>copy(fields=x))

  def satisfyMethod(cls: Type, name: Symbol, params: Seq[Type], ret: Type): (ClassReqs, Seq[Constraint]) = {
    val method = MethodCReq(cls, name, params, ret)
    val (creqs1, cons1) = satisfyCReq[MethodCReq](method, methods, x=>copy(methods=x))
    val (creqs2, cons2) = satisfyCReq[MethodCReq](method, optMethods, x=>creqs1.copy(optMethods=x))
    (creqs2, cons1 ++ cons2)
  }

  private def satisfyExt[T <: CReq[T]](ext: ExtCReq, crs: Set[T]): Set[T] = crs.flatMap { req =>
    // TODO Currently, this method assumes that the removal of an extends clause implies no further declarations appear for req.cls

    // ext.cls != req.cls
    val diff = req.alsoNot(ext.cls)
    // ext.cls == req.cls
    val same = req.alsoSame(ext.cls).map(_.withCls(ext.ext))
    Seq(diff, same).flatten
  }

  def satisfyCReq[T <: CReq[T]](creq1: T, crs: Set[T], make: Set[T] => ClassReqs): (ClassReqs, Seq[Constraint]) = {
    var cons = Seq[Constraint]()
    val newcrs = crs flatMap ( creq2 =>
      if (creq1.canMerge(creq2)) {
        creq1.assert(creq2) foreach (c => cons = cons :+ c)
        creq2.alsoNot(creq1.cls)
      }
      else
        Some(creq2)
      )
    (make(newcrs), cons)
  }

  def many[T <: CReq[T]](f: ClassReqs => T => (ClassReqs, Seq[Constraint]), reqs: Iterable[T]): (ClassReqs, Seq[Constraint]) = {
    var cons = Seq[Constraint]()
    var creqs = this
    for (req <- reqs) {
      val (newcreqs, newcons) = f(creqs)(req)
      creqs = newcreqs
      cons = cons ++ newcons
    }
    (creqs, cons)
  }
}
