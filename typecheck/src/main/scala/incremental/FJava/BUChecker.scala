package incremental.fjava

import constraints.equality.EqConstraint
import constraints.fjava.CSubst.CSubst
import constraints.{CVar, Statistics}
import constraints.fjava._
import constraints.fjava.impl
import incremental.{NodeKind, Node_, Util}
import incremental.Node._
import scala.collection.immutable.ListMap
/**
 * Created by lirakuci on 3/2/15.
 */


case class FieldName(x: Symbol)
case class Param(x: Symbol)
case class Ctor(params: ListMap[Symbol, CName], superCall: List[Symbol], fieldDefs: ListMap[Symbol, Symbol])
case class Fields(fld: Map[Symbol, Type])
case class Methods(m : Map[Symbol, (Type, List[Type])])

case class ExtendD( ext : Map[Type,Type])

case class ClassReq(extendc: Option[Type], ctorParams: Option[List[Type]], fields: Fields, methods: Methods, cmethods : Methods) {
  def subst(s: CSubst) = ClassReq(extendc.map(_.subst(s)).map(_.subst(s)), ctorParams.map(_.map(_.subst(s))), Fields(fields.fld.mapValues(_.subst(s)).mapValues(_.subst(s))), Methods(methods.m.mapValues {case (ret, args) => (ret.subst(s).subst(s), args.map(_.subst(s)).map(_.subst(s)))}), Methods(cmethods.m.mapValues {case (ret, args) => (ret.subst(s), args.map(_.subst(s)))}))
}//(supertype, Fields, Methods)

case class CR(cr : Map[Type, ClassReq])

abstract class BUChecker[CS <: ConstraintSystem[CS]] extends TypeChecker[CS] {

  import csFactory._

 // type Methods = Map[Symbol, (Type, List[Type])]

//  type Fields = Map[Symbol, Type]

  //case class ClassR(extendc: Option[Type], ctorParams: Option[List[Type]], fields: Fields, methods: Methods, cmethods : Methods) {
 //   def subst(s: CSubst) = ClassR(extendc.map(_.subst(s)), ctorParams.map(_.map(_.subst(s))), fields.mapValues(_.subst(s)), methods.mapValues {case (ret, args) => (ret.subst(s), args.map(_.subst(s)))}, cmethods.mapValues {case (ret, args) => (ret.subst(s), args.map(_.subst(s)))})
 // }//(supertype, Fields, Methods)


  type Reqs = Map[Symbol, Type]

  type CReqs = Map[Type, ClassReq]

  type StepResult = (Type, Reqs, CR, Seq[Constraint])

 // case class ClassDef(name: CName, superClass: CName, fields: Fields, methods: Methods, ctor: Ctor)
 // case class MethodDef(name: Symbol, params: ListMap[Symbol, CName], returnType: CName, body: Node_[Any])

  type TError = String

  type Prg = String

  var extD = Map[Type, Type]()

  type Result = (Type, Reqs, CR, CS)
  var s = Map[Type,Type]()
  val extt = ExtendD(s)

  def finalT(t : Type, cs : CS) : Type = {
   val c = t.subst(cs.substitution)
    if (c == CName) c
    else finalT(t.subst(cs.substitution), cs)
  }

  def typecheckImpl(e: Node): Either[Type, TError] = {
    val root = e.withType[Result]

    Util.timed(localState -> Statistics.typecheckTime) {
      root.visitUninitialized { e =>
        val (t, reqs, creqs, cons) = typecheckStep(e)
        var cr = CR(Map[Type, ClassReq]())
        for (i <- 0 until e.kids.seq.size){
          val (t, req, creq, _) = e.kids(i).typ
              cr = CR(cr.cr ++ creq.cr)
        }
        val  subcs = e.kids.seq.foldLeft(freshConstraintSystem)((cs, res) => cs mergeFJavaSubsystem (res.typ._4, ExtendD(extD)))
        val cs = subcs addNewConstraints cons
        val reqs2 = cs.applyPartialSolutionIt[(Symbol, Type), Map[Symbol, Type], Type](reqs, p => p._2)
        var creqs2 = CR(Map())
        for ((ts, decl) <- creqs.cr) {
          val decl2 = decl.subst(cs.substitution)
        //  val c = finalT(ts, cs)
         // val (mcons, cres) = mergeCReqMaps(creqs2,Map(c -> decl2))
          //creqs2 = cres
          val c = ts.subst(cs.substitution).subst(cs.substitution).subst(cs.substitution).subst(cs.substitution)
                creqs2 = CR(creqs2.cr + (c -> decl2))
          }

        e.typ = (cs applyPartialSolution t, reqs2, creqs2, cs.propagate)
        true
      }

      val (t_, reqs, creqs, cs_) = root.typ
      val cs = cs_.tryFinalize
      val t = t_.subst(cs.substitution)

      if (!reqs.isEmpty)
        Right(s"Unresolved variable requirements $reqs, type $t, unres ${cs.unsolved}")
      else if (!creqs.cr.isEmpty)
        Right(s"Unresolved type-variable requirements $creqs, type $t, unres ${cs.unsolved}")
      else if (!cs.isSolved)
        Right(s"Unresolved constraints ${cs.unsolved}, type $t")
      else
        Left(t)
    }
  }

  def addFieldReq(creqs: CR, t: Type, f: Symbol, U: Type): (Seq[Constraint], CR) = {
    //if (t == CName('Object))
    //  return (Seq()) TODO shouldn't class reqs be constraints?
    creqs.cr.get(t) match {
      case None =>
        val res: CR = CR(creqs.cr + (t -> ClassReq(None, None, Fields(Map(f -> U)), Methods(Map()), Methods(Map()))))
        (Seq(), res)
      case Some(ClassReq(sup, ctor, fields, methods, cmethods)) =>
        fields.fld.get(f) match {
          case None =>
            (Seq(), CR(creqs.cr + (t -> ClassReq(sup, ctor, Fields(fields.fld + (f -> U)), methods, cmethods))))
          case Some(t2) =>
            (Seq(Equal(t2, U)), creqs)
        }
    }
  }

  def addFieldDec(creqs: CR, t: Type, f: Symbol, U: Type): (Seq[Constraint], CR) = {
    //if (t == CName('Object))
    //  return (Seq()) TODO shouldn't class reqs be constraints?
    creqs.cr.get(t) match {
      case None =>
        (Seq(), creqs)
      case Some(ClassReq(sup, ctor, fields, methods, cmethods)) =>
        fields.fld.get(f) match {
          case None =>
            (Seq(), creqs)
          case Some(t2) =>
            val fnew = Fields(fields.fld - f)
            (Seq(Equal(t2, U)), CR(creqs.cr + (t -> ClassReq(sup, ctor, fnew, methods, cmethods))))
        }
    }
  }

  def addMethodReq(creqs: CR, t: Type, m: Symbol, args: List[Type], ret: Type): (Seq[Constraint], CR) = {
    creqs.cr.get(t) match {
      case None =>
        val res: CR = CR(creqs.cr + (t -> ClassReq(None, None, Fields(Map()), Methods(Map(m ->(ret, args))), Methods(Map()))))
        (Seq(), res)
      case Some(ClassReq(sup, ctor, fields, methods, cmethods)) =>
        methods.m.get(m) match {
          case None =>
            (Seq(), CR(creqs.cr + (t -> ClassReq(sup, ctor, fields, Methods(methods.m + (m ->(ret, args))), cmethods))))

          case Some((ret2, args2)) =>
            if (args.length == args2.length) {
              val cons = Equal(ret2, ret) +: (args2 zip args).map(p => Equal(p._1, p._2))
              (cons, creqs)
            }
            else
              (Seq(Equal(ret, ret2), Never(AllEqual(args, args2))), creqs)
        }
    }
  }

  def addCMethodReq(creqs: CR, t: Type, m: Symbol, args: List[Type], ret: Type): (Seq[Constraint], CR) = {
    var res = creqs
    if (t == CName('Object)) {
      (Seq(), creqs)
    }
    else {
      creqs.cr.get(t) match {
        case None =>
          val res: CR = CR(creqs.cr - t + (t -> ClassReq(None, None, Fields(Map()), Methods(Map()), Methods(Map(m ->(ret, args))))))
          (Seq(), res)
        case Some(ClassReq(sup, ctor, fields, methods, cmethods)) =>
          cmethods.m.get(m) match {
            case None =>
              (Seq(), CR(creqs.cr + (t -> ClassReq(sup, ctor, fields, methods, Methods(cmethods.m + (m ->(ret, args)))))))

            case Some((ret2, args2)) =>
              if (args.length == args2.length) {
                val cons = Equal(ret2, ret) +: (args2 zip args).map(p => Equal(p._1, p._2))
                (cons, creqs)
              }
              else
                (Seq(Equal(ret, ret2), Never(AllEqual(args, args2))), creqs)
          }
      }
    }
  }

  def addMethodDec(creqs: CR, t: Type, m: Symbol, args: List[Type], ret: Type): (Seq[Constraint], CR) = {
    creqs.cr.get(t) match {
      case None =>
        (Seq(), creqs)
      case Some(ClassReq(sup, ctor, fields, methods, cmethods)) =>
        methods.m.get(m) match {
          case None =>
            (Seq(), creqs)
          case Some((ret2, args2)) =>
            if (args.length == args2.length) {
              val cons = Equal(ret2, ret) +: (args2 zip args).map(p => Equal(p._1, p._2))
              val mnew = methods.m - m
              (cons, CR(creqs.cr - t + (t -> ClassReq(sup, ctor, fields, Methods(mnew), cmethods))))
            }
            else
              (Seq(Equal(ret, ret2), Never(AllEqual(args, args2))), creqs)
        }
    }
  }


  def addCtorReq(creqs: CR, t: Type, params: List[Type]): (Seq[Constraint], CR) = {
    var res = creqs
    if (t == CName('Object)) {
      (Seq(), creqs)
    }
    else {
      creqs.cr.get(t) match {
        case None =>
          res = CR(creqs.cr + (t -> ClassReq(None, Some(params), Fields(Map()), Methods(Map()), Methods(Map()))))
          (Seq(), res)
        case Some(ClassReq(sup, ctor, fields, methods, cmethods)) =>
          ctor match {
            case None =>
              (Seq(), CR(res.cr + (t -> ClassReq(sup, Some(params), fields, methods, cmethods))))
            case Some(params2) =>
              if (params.length == params2.length) {
                val cons = (params zip params2).map(p => Equal(p._1, p._2))
                (cons, res)
              }
              else
                (Seq(Never(AllEqual(params, params2))), res)
          }
      }
    }
  }

  def addSupertypeReq(creqs: CR, t: Type, tsuper: Type): (Seq[Constraint], CR) = {
    var res = creqs
    if (tsuper == CName('Object)) {
      (Seq(), creqs)
    }
    else {
      res = CR(res.cr + (tsuper -> ClassReq(None, None, Fields(Map()), Methods(Map()), Methods(Map()))))
      creqs.cr.get(t) match {
        case None =>
          res = CR(res.cr + (t -> ClassReq(Some(tsuper), None, Fields(Map()), Methods(Map()), Methods(Map()))))
          (Seq(), res)
        case Some(ClassReq(sup, ctor, fields, methods, cmethods)) =>
          sup match {
            case None =>
              (Seq(), CR(res.cr + (t -> ClassReq(Some(tsuper), ctor, fields, methods, cmethods))))
            case Some(t2) =>
              (Seq(Equal(tsuper, t2)),res)
          }
      }
    }
  }

  def typecheckStep(e: Node_[Result]): StepResult = e.kind match {

    case Num =>
      (CName('TNum), Map(), CR(Map()),Seq())
    case Str =>
      (CName('TString), Map(),CR(Map()), Seq())

    case op if op == Add || op == Mul =>
      val (t1, reqs1, creqs1, _) = e.kids(0).typ
      val (t2, reqs2, creqs2, _) = e.kids(1).typ

      val lcons = Subtype(t1, CName('TNum))
      val rcons = Subtype(t2, CName('TNum))
      val (mcons, mreqs) = mergeReqMaps(reqs1, reqs2)
      val (mcCons, mCreqs) = mergeCReqMaps(creqs1, creqs2)

      (CName('TNum), mreqs, mCreqs, mcons :+ lcons :+ rcons)

    case Var =>
      val x = e.lits(0).asInstanceOf[Symbol]
      val X = freshCName()
      (X, Map(x -> X), CR(Map()), Seq()) // Map(X -> cld), needed at some examples

    case FieldAcc =>
      val f = e.lits(0).asInstanceOf[Symbol] //symbol
      val (t, reqs, creqs, _) = e.kids(0).typ //subsol
      val U = freshCName()
      val (cons, mcreqs) = addFieldReq(creqs, t, f, U)

      (U, reqs, mcreqs, cons) //subsol

    case Invk =>
      val m = e.lits(0).asInstanceOf[Symbol]
      val (te, reqs0, creqs0,  _) = e.kids(0).typ
      val U = freshCName()
      var cons = Seq[Constraint]()
      var reqss: Seq[Reqs] = Seq(reqs0)
      var creqss: Seq[CR] = Seq(creqs0)
      var param = List[Type]()
      for (i <- 1 until e.kids.seq.size) {
        val (ti, subreqs, subcreqs, _) = e.kids(i).typ
        val Ui = freshCName()
       reqss = reqss :+ subreqs
       cons = cons :+ Subtype(ti, Ui) //or should be subtype
       creqss = creqss :+ subcreqs
       param = param :+ Ui
      }

      val (mcons, mreqs) = mergeReqMaps(reqss)
      val (cCons, creqs) = mergeCReqMaps(creqss)
      val (mcCons, mcreqs) = addMethodReq(creqs, te, m, param, U)

      (U, mreqs, mcreqs, cons ++ mcons ++ cCons ++ mcCons )

    case New =>
      val c = e.lits(0).asInstanceOf[CName]
      val U = freshCName()
      var reqss = Seq[Reqs]()
      var creqss = Seq[CR]()
      var cons = Seq[Constraint]()
      var ctor: List[Type] = Nil
      for (i <- 0 until e.kids.seq.size) {
        val (ti, subreqs, subcreqs, _) = e.kids.seq(i).typ
        val Ui = freshCName()
        ctor = ctor :+ Ui
        reqss = reqss :+ subreqs
        creqss = creqss :+ subcreqs
       cons =  cons :+ Subtype(ti, Ui)//or should be subtype
      }
      val (mcons, mreqs) = mergeReqMaps(reqss)
      val (cCons, creqs) = mergeCReqMaps(creqss)

      val (mcCons, mcreqs) = addCtorReq(creqs, c, ctor)
      (c, mreqs, mcreqs, cons ++ mcons ++ cCons ++ mcCons)

    case DCast =>
      val (t, reqs, creqs, _) = e.kids(0).typ
      val c = e.lits(0).asInstanceOf[CName]

      (c, reqs, creqs, Seq((NotEqual(t, c))))

    case UCast =>
      val c = e.lits(0).asInstanceOf[CName]
      val (t, reqs, creqs,_) = e.kids(0).typ

      (c, reqs, creqs, Seq(Subtype(t, c)))

    case SCast =>
      val c = e.lits(0).asInstanceOf[CName]
      val (t, reqs, creqs, _) = e.kids(0).typ

      (t, reqs, creqs, Seq())

    case MethodDec =>
      val (e0, reqs, creqs, _) = e.kids(0).typ
      val retT = e.lits(0).asInstanceOf[CName]
      val m = e.lits(1).asInstanceOf[Symbol]
      val params = e.lits(2).asInstanceOf[Seq[(Symbol, Type)]]
      var restReqs = reqs
      var cons = Seq[Constraint]()
   //   var cCreqs = creqs
      val Uc = freshCName()
      val Ud = freshCName()
      cons = cons :+ Subtype(e0, retT) :+ Subtype(Uc, Ud)

      for ((x, xC) <- params) {
        reqs.get(x) match {
          case None => restReqs = restReqs
          case Some(typ) =>
            restReqs = restReqs - x
            cons = Equal(xC, typ) +: cons
        }
      }
//     val (ccons, cres)= addCMethodReq(cCreqs, Ud, m, params.toMap.valuesIterator.toList, retT)
      val (ccons, cres) = mergeCReqMaps(creqs, CR(Map(Uc -> ClassReq(Some(Ud), None, Fields(Map()), Methods(Map()), Methods(Map())))))
      restReqs.get('this) match {
        case None => restReqs = restReqs
        case Some(typ) =>
          cons = Equal(typ, Uc) +: cons // Subtype(Uc, typ)
          restReqs = restReqs - 'this
       }
      cons= cons ++ ccons ++ cccons
      (MethodOK(Uc), restReqs, ccres, cons )

    case ClassDec =>
      val c = e.lits(0).asInstanceOf[CName]
      val sup = e.lits(1).asInstanceOf[CName]
      val Ctor(params, superCall, fieldDefs) = e.lits(2).asInstanceOf[Ctor]
      val fields = e.lits(3).asInstanceOf[Seq[(Symbol, Type)]].toMap
      var restCreq = Seq[CR]()
      var cons = Seq[Constraint]()
      var restReqs = Seq[Reqs]()
      for (i <- 0 until e.kids.seq.size) {
        val (t, req, creq, _) = e.kids(i).typ
        restReqs = restReqs :+ req
        restCreq = restCreq :+ creq
        cons = cons :+ Equal(c,t.asInstanceOf[MethodOK].in)
      }
      val (conss, cr) = mergeCReqMaps(restCreq)
      val (rcons, req) = mergeReqMaps(restReqs)

      cons = cons ++ conss ++ rcons// :+ Subtype(c, sup)
      val (mconsD, crD) = addSupertypeReq(cr, c, sup)
      val lst = Ctor(params, superCall, fieldDefs).params.valuesIterator.toList
      val ( consC,cresC) = addCtorReq(cr, c, lst)
      val ( consD, cres) = addCtorReq(cresC, sup, lst.dropRight(Ctor(params, superCall, fieldDefs).fieldDefs.size))

     cons = cons ++ mconsD ++ consC
   //   println(s"classs reqs $crD")

      (c, req, cres, cons)


    case ProgramM =>

      def applyFinal(t : Type , s : CSubst) : Type ={
        if (t == CName) t
        else applyFinal(t.subst(s), s)
      }


      var CT = Map[Type,(Type,  List[Type], Map[Symbol, Type], Map[Symbol, (Type, List[Type])])]()
      var restCreq = Seq[CR]()
      var resC = CR(Map())
      var resCC = CR(Map())
      var ext = ExtendD(Map())
      var cons = Seq[Constraint]()
      for (i <- 0 until e.kids.seq.size) {
        val c = e.kids(i).lits(0).asInstanceOf[CName]
        val sup = e.kids(i).lits(1).asInstanceOf[CName]
        val Ctor(params, superCall, fieldDefs) = e.kids(i).lits(2).asInstanceOf[Ctor]
        val fields = e.kids(i).lits(3).asInstanceOf[Seq[(Symbol, Type)]]
        var methods = Map[Symbol, (Type, List[Type])]()
        for (j <- 0 until e.kids(i).kids.seq.size) {
          val (c0, reqs, creqs, _)= e.kids(i).kids.seq(j).typ
          cons = cons :+ Equal(c, c0.asInstanceOf[MethodOK].in)
          val retT = e.kids(i).kids(j).lits(0).asInstanceOf[CName]
          val m = e.kids(i).kids(j).lits(1).asInstanceOf[Symbol]
          val params = e.kids(i).kids(j).lits(2).asInstanceOf[Seq[(Symbol, Type)]]
          var par = List[Type]()
          for((t, p) <- params.toMap)
            par = par :+ p
          methods = methods + (m -> (retT, par))
          restCreq = restCreq :+ creqs

        }
        var (mCcons, mcreqs) = mergeCReqMaps(restCreq)

        resC = mcreqs
        CT =  CT + (c -> (sup,  Ctor(params, superCall, fieldDefs).params.valuesIterator.toList, fields.toMap, methods))
        val (consD, crD) = addSupertypeReq(mcreqs, c, sup)
        cons = cons ++  mCcons ++ consD

        val (kot, kotc) = resC.cr.foldLeft((cons, resC)){case ((mcns, creqs), (ts, dec)) =>
          val (mconsfold, creqsfold) = addSupertypeReq(creqs, c, sup)
          (mcns ++ mconsfold, creqsfold)
        }
        resC = kotc
        ext = ExtendD(ext.ext + (c -> sup))
      }

      extD = ext.ext

      val  subcs = e.kids.seq.foldLeft(freshConstraintSystem)((cs, res) => cs mergeFJavaSubsystem (res.typ._4, ext))

     // cons = cons ++ mCcons
      val cs = subcs addNewConstraints cons

   //   val cres = removeCondReq(resC)

      val (prv, prvC) = resC.cr.foldLeft((cons, resC)){case ((mcns, creqs), (ts, dec)) =>
        val (mconsfold, creqsfold) = mergeCReqMaps(creqs, CR(Map(ts.subst(cs.substitution).subst(cs.substitution).subst(cs.substitution) -> dec.subst(cs.substitution).subst(cs.substitution))))
        (mcns ++ mconsfold, creqsfold)
      }

    /*  for ((ts, decl) <- resC.cr) {
        val decl2 = decl.subst(cs.substitution)
        //  val c = finalT(ts, cs)
        // val (mcons, cres) = mergeCReqMaps(creqs2,Map(c -> decl2))
        //creqs2 = cres
        val c = ts.subst(cs.substitution)

        println(s"REQQQQQQQQ $resC")

        val (mcons, cres) = mergeCReqMaps(resCC,CR(Map(c -> decl2)))
        //resC= CR(cres.cr - ts)
resCC = cres
        println(s"Extend at cons $mcons")
cons = cons ++ mcons
        // resC = CR(resC.cr - ts + (c -> decl2))
      /*  if (c == CName) {
          val (mcons, cres) = mergeCReqMaps(resC,CR(Map(c -> decl2)))
          resC = cres }
        else {
          val c2 = c.subst(cs.substitution)
          val (mcons, cres) = mergeCReqMaps(resC,CR(Map(c2 -> decl2)))
          resC = cres
        }*/
      }*/

      println(s"smtheods are $prvC")

      cons = cons ++ prv

      val (cr, ctcons)=  remove(CT, prvC, cs addNewConstraints(cons))
      cons = cons ++ ctcons

      val  cs1 = cs addNewConstraints(cons)
      cs1.solvedFJ(cs1.substitution, ext)


      val (prv2, prvC2) = cr.cr.foldLeft((cons, resC)){case ((mcns, creqs), (ts, dec)) =>
        val (mconsfold, creqsfold) = mergeCReqMaps(creqs, CR(Map(ts.subst(cs1.substitution).subst(cs1.substitution).subst(cs1.substitution) -> dec.subst(cs1.substitution).subst(cs1.substitution))))
        (mcns ++ mconsfold, creqsfold)
      }

      val (cr2, ctcons2)=  remove(CT, prvC2, cs1 addNewConstraints(cons ++ prv2))
      cons = cons ++ ctcons2

      val  cs2 = cs1 addNewConstraints(cons)
      cs2.solvedFJ(cs2.substitution, ext)

      println(s"constrains are VCCCCCC $cons")
      println(s"Constraint after remove $cs2")

      println(s"REEEEESSSSS $prvC2")

      (CName('Object), Map(),cr2, cons)
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
        case Some(r1) =>
         mcons = mcons :+ Equal(r1, r2)
      }
    (mcons, mreqs)
  }

  private def remove(CT : Map[Type, (Type, List[Type],  Map[Symbol, Type], Map[Symbol, (Type, List[Type])])], creq : CR, cs :CS) : (CR, Seq[Constraint]) = {
    var cr = creq
    var cons = Seq[Constraint]()
    var ct = CT
    for ((c, cld) <- creq.cr) {
      var stype = cld.extendc
      stype match {
        case None => stype
        case Some(t) =>

      }
      var ctor = cld.ctorParams
      var fields = cld.fields
      var methods = cld.methods
      var cmethods = cld.cmethods
      CT.get(c) match {
        case None => cr
        case Some(clsT) =>
          cld.extendc match {
            case None => stype
            case Some(t) =>
              cons = cons :+ Equal(clsT._1, t)

          }
          cld.ctorParams match {
            case None => ctor
            case Some(lt) =>  cons = cons :+ AllEqual(clsT._2, lt)
          }

          for ((f, typ) <- cld.fields.fld) {
            clsT._3.get(f) match {
              case None => fields = fields
              case Some(typ2) =>
                cons = cons :+ Equal(typ2, typ)
                fields = Fields(fields.fld - f)
            }
          }
          for ((m, rt) <- cld.methods.m) {
            clsT._4.get(m) match {
              case None => methods = methods
              case Some(rt2) =>
                cons = cons :+ Equal(rt._1, rt2._1)
                cons = cons :+ AllEqual(rt._2, rt2._2)
                methods = Methods(methods.m - m)
            }
          }
          for ((m, rt) <- cld.cmethods.m) {
            clsT._4.get(m) match {
              case None => cmethods = Methods(cmethods.m - m)
              case Some(rt2) =>
                cons = cons :+ Equal(rt2._1, rt._1)
                cons = cons :+ AllEqual(rt._2, rt2._2)
                cmethods = Methods(cmethods.m - m)
            }
          }
      }
      for ((d, cldD) <- CT) {
        if (cs.isSubtype(c, d)) {
          for ((f, typ) <- fields.fld) {
            cldD._3.get(f) match {
              case None => fields = fields
              case Some(typ2) =>
                fields = Fields(fields.fld - f)
                cons = cons :+ Equal(typ, typ2)
            }
          }
          for ((m, rt) <- methods.m) {
            cldD._4.get(m) match {
              case None => methods = methods
              case Some(rt2) =>
                cons = cons :+ Equal(rt._1, rt2._1)
                cons = cons :+ AllEqual(rt._2, rt2._2)
                methods = Methods(methods.m - m)
            }
          }
        }
      }
      if (fields.fld.isEmpty && methods.m.isEmpty && CT.exists( _._1 == c)) // &&cld.cmethods.m.isEmpty
        cr = CR(cr.cr - c)
    }
     for ((c, cld) <- cr.cr){
        if (c.isInstanceOf[UCName]) {
          cr = CR(cr.cr - c)
        }
        else cr
      }

  (cr,cons)
  }

 /*def removeCondReq(creq : CR) : CR = {
   var cres = creq
   var cldm = Methods(Map[Symbol, (Type, List[Type])]())
   var cldCm = Methods(Map[Symbol, (Type, List[Type])]())
   for ((c, cld) <- creq.cr) {
     if (c == CName('Object)) cres = CR(cres.cr - c)
     else {
       cldCm = cld.cmethods
       cldm = cld.methods
       for ((m, dec) <- cld.cmethods.m) {
         cld.methods.m.get(m) match {
           case None => cldCm = Methods(cldCm.m - m)
           case Some(dec2) =>
             cldm = Methods(cldm.m + (m -> (dec._1, dec._2)))
           //  cons = cons +: Equal(dec._1, dec2._1) ++ AllEqual(dec._2, dec2._2) TODO Not sure if constrains should be added or not. OR just replace the signature
             cldCm = Methods(cldCm.m - m)
         }
       }
       cres = CR(cres.cr + (c -> ClassReq(cld.extendc,cld.ctorParams, cld.fields, cldm, cldCm)))

     }
   }
   cres
 }*/

  private val cinit: (Seq[Constraint], CR) = (Seq(), CR(Map()))

  def mergeCCld(cld1: ClassReq, cld2: ClassReq): (Seq[Constraint], ClassReq) = {
    var ctor : Option[List[Type]] = cld1.ctorParams
    val newF = cld2.fields
    val wasF = cld1.fields
    var rF = wasF
    val wasM = cld1.methods
    var mcons = Seq[Constraint]()
    var cldm = wasM
    var cldmc = cld1.cmethods
      (cld1.ctorParams, cld2.ctorParams) match {
        case (None, None) => ctor
        case (None, Some(_)) => ctor = cld2.ctorParams
        case (Some(_), None) => ctor
       case (Some(t1), Some(t2)) => ctor
         if (t1.size == t2.size) mcons = mcons :+ AllEqual(t1, t2)
          else mcons = mcons :+ Never(AllEqual(t1, t2))
      }
    for ((f, typ) <- newF.fld){
      wasF.fld.get(f) match {
        case None => rF = Fields(rF.fld + (f -> typ))
        case Some(typ2) =>
          mcons = mcons :+ Equal(typ2, typ)
      }
    }
    for ((m, mbody) <- cld2.methods.m) {
      wasM.m.get(m) match {
        case None => cldm = Methods(cldm.m + (m -> mbody)) // mdoby = return type + list of parameters
        case Some(mbody2) =>
          mcons = mcons :+ Equal(mbody2._1, mbody._1)
          if (mbody._2.length == mbody2._2.length)
            mcons = mcons :+ AllEqual(mbody._2, mbody2._2)
          else mcons = mcons :+ Never(AllEqual(mbody._2, mbody2._2))
      }
    }
    for ((m, mbody) <- cld2.cmethods.m) {
      cld1.cmethods.m.get(m) match {
        case None => cldmc = Methods(cldmc.m + (m -> mbody))// mdoby = return type + list of parameters
        case Some(mbody2) =>
          mcons = mcons :+ Equal(mbody2._1, mbody._1)
          if (mbody._2.length == mbody2._2.length)
            mcons = mcons :+ AllEqual(mbody._2, mbody2._2)
          else mcons = mcons :+ Never(AllEqual(mbody._2, mbody2._2))
      }
    }
    var styp : Option[Type] = cld1.extendc
    (cld1.extendc, cld2.extendc) match {
      case (None, None) => styp
      case (None, Some(_)) => styp = cld2.extendc
      case (Some(_), None) => styp
      case (Some(t1), Some(t2)) => styp
        mcons = mcons :+ Equal(t1, t2)


    }
    (mcons, ClassReq(styp, ctor, rF, cldm, cldmc))
  }


  def mergeCReqMaps(creq: CR, creqs: CR*): (Seq[Constraint], CR) = mergeCReqMaps(creq +: creqs)

  def mergeCReqMaps(creqs: Seq[CR]): (Seq[Constraint], CR) =
    Util.timed(localState -> Statistics.mergeCReqsTime) {
      creqs.foldLeft[(Seq[Constraint], CR)](cinit)(_mergeCReqMaps)
    }

  private def _mergeCReqMaps(was: (Seq[Constraint], CR), newCReqs: CR) = {
    val wasCReqs = was._2
    var mcons = was._1
    var mcreqs = wasCReqs
    for ((t, cld2) <- newCReqs.cr)
      wasCReqs.cr.get(t) match {
        case None => mcreqs = CR(mcreqs.cr + (t -> cld2))
        case Some(cld1) => mcreqs = CR(mcreqs.cr + (t -> mergeCCld(cld1, cld2)._2))
          mcons = mergeCCld(cld1, cld2)._1 ++ mcons
      }
    (mcons, mcreqs)
  }


 /* def mergeBoolReq(creqs: CReqs, breqs : Seq[BCReqs]) : Seq[Constraint] = {
    var cons = Seq[Constraint]()
    for ((d, cld, bol)<-  breqs) {
      (d, cld, bol) match {
        case (None, m1, false) => cons
        case (Some(t), m, false) => cons
        case (Some(t), (m, re, param), true) =>
      creqs.get(t) match {
        case None => cons
        case Some(cld2) =>
         for((m1, body) <- cld2.methods ) {
           if (m1 == m) {
               re match {
                 case None => cons
                 case Some(re2) =>
                   cons = cons :+ Equal(body._1, re2) :+ AllEqual(param, body._2)
               }}
           else cons
         }}}}
    cons
  }*/

}


case class BUCheckerFactory[CS <: ConstraintSystem[CS]](factory: ConstraintSystemFactory[CS]) extends TypeCheckerFactory[CS] {
  def makeChecker = new BUChecker[CS] {
    type CSFactory = factory.type
    implicit val csFactory: CSFactory = factory
  }
}
