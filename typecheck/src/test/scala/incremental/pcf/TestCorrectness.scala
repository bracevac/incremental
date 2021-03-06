package incremental.pcf

import constraints.CVar
import constraints.equality.impl._
import constraints.equality._
import incremental.Node._
import incremental.Util
import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
 * Created by seba on 14/11/14.
 */
class TestCorrectness[CS <: ConstraintSystem[CS]](classdesc: String, checkerFactory: TypeCheckerFactory[CS]) extends FunSuite with BeforeAndAfterEach {
  val checker: TypeChecker[CS] = checkerFactory.makeChecker

  override def afterEach: Unit = checker.localState.printStatistics()

  def typecheckTest(desc: String, e: =>Node)(expected: Type): Unit =
    test (s"$classdesc: Type check $desc") {
      val actual = checker.typecheck(e)
      assert(actual.isLeft, s"Expected $expected but got $actual")

      val sol = SolveContinuously.state.withValue(checker.csFactory.state.value) {
        expected.unify(actual.left.get, SolveContinuously.freshConstraintSystem).tryFinalize
      }
      assert(sol.isSolved, s"Expected $expected but got ${actual.left.get}. Match failed with ${sol.unsolved}")
    }

  def typecheckTestError(desc: String, e: =>Node) =
    test (s"$classdesc: Type check $desc") {
      val actual = checker.typecheck(e)
      assert(actual.isRight, s"Expected type error but got $actual")
    }

  typecheckTest("17", Num(17))(TNum)
  typecheckTest("17+(10+2)", Add(Num(17), Add(Num(10), Num(2))))(TNum)
  typecheckTest("17+(10+5)", Add(Num(17), Add(Num(10), Num(5))))(TNum)
  typecheckTest("\\x. 10+5", Abs('x, Add(Num(10), Num(5))))(TFun(UVar(CVar('x$0)), TNum))
  typecheckTest("\\x. x+x", Abs('x, Add(Var('x), Var('x))))(TFun(TNum, TNum))
  typecheckTestError("\\x. err+x", Abs('x, Add(Var('err), Var('x))))
  typecheckTest("\\x. \\y. x y", Abs('x, Abs('y, App(Var('x), Var('y)))))(TFun(TFun(UVar(CVar('x$1)), UVar(CVar('x$2))), TFun(UVar(CVar('x$1)), UVar(CVar('x$2)))))
  typecheckTest("\\x. \\y. x + y", Abs('x, Abs('y, Add(Var('x), Var('y)))))(TFun(TNum, TFun(TNum, TNum)))
  typecheckTest("if0(17, 0, 1)", If0(Num(17), Num(0), Num(1)))(TNum)
  typecheckTestError("\\x. x + (x 5)", Abs('x, Add(Var('x), App(Var('x), Num(5)))))

  lazy val mul = Fix(Abs('f, TFun(TNum, TFun(TNum, TNum)),
                   Abs('m, TNum, Abs('n, TNum,
                     If0(Var('m), Num(0), App(App(Var('f), Add(Var('m), Num(-1))), Var('n)))))))
  typecheckTest("multiplication", mul)(TFun(TNum, TFun(TNum, TNum)))

  lazy val fac = Fix(Abs('f, Abs('n, If0(Add(Var('n), Num(-1)), Num(1), App(App(mul, Var('n)), App(Var('f), Add(Var('n), Num(-2))))))))
  typecheckTest("factorial", fac)(TFun(TNum, TNum))

  lazy val fac1 = Fix(Abs('f, Abs('n, If0(Add(Var('n), Num(-1)), Num(1), App(App(mul, Var('n)), App(Var('f), Add(Var('n), Num(-2))))))))
  typecheckTest("factorial1", fac1)(TFun(TNum, TNum))
  lazy val fac2 = {fac1.kids(0).kids(0).kids(0).kids(0) = Var('n); fac1}
  typecheckTest("factorial2", fac2)(TFun(TNum, TNum))
  lazy val fac3 = {fac1.kids(0).kids(0).kids(0).kids(2).kids(1).kids(1).kids(1) = Num(-1); fac1}
  typecheckTest("factorial3", fac3)(TFun(TNum, TNum))
  lazy val fac3_2 = {
    val absn = fac1.kids(0).kids(0)
    fac1.kids(0).kids(0) = Abs('x, absn.kids(0))
    fac1
  }
  typecheckTestError("factorial3_2", fac3_2)
  lazy val fac4 = Fix(Abs('f, Abs('n, If0(Add(Var('n), Num(-1)), Num(1), App(App(mul, Var('n)), App(Var('f), Add(Var('n), Num(-2))))))))
  typecheckTest("factorial4", fac4)(TFun(TNum, TNum))
  lazy val fac5 = {fac4.kids(0).kids(0).kids(0).kids(0) = Var('n); fac4.kids(0).kids(0).kids(0).kids(2).kids(1).kids(1).kids(1) = Num(-1); fac4}
  typecheckTest("factorial5", fac5)(TFun(TNum, TNum))

  typecheckTest("eta-expanded factorial", Abs('x, App(fac, Var('x))))((TFun(TNum, TNum)))

  lazy val fib = Fix(Abs('f, Abs('n,
    If0(Var('n), Num(1),
      If0(Add(Var('n), Num(-1)), Num(1),
        Add(App(Var('f), Add(Var('n), Num(-1))),
          App(Var('f), Add(Var('n), Num(-2)))))))))
  typecheckTest("fibonacci", fib)(TFun(TNum, TNum))
  typecheckTest("factorial + fibonacci", Abs('x, Add(App(fac, Var('x)), App(fib, Var('x)))))(TFun(TNum, TNum))
  typecheckTest("\\y. y", Abs('y, Var('y)))(TFun(UVar(CVar('x$0)), UVar(CVar('x$0))))
  typecheckTestError("\\x. x x", Abs('x, App(Var('x), Var('x))))
}

/*class TestDUSolveEndCorrectness extends TestCorrectness("DUSolveEnd", new DUCheckerFactory(SolveEnd))
class TestDUSolveContniuouslyCorrectness extends TestCorrectness("DUSolveContinuously", new DUCheckerFactory(SolveContinuously))

class TestBUSolveEndCorrectness extends TestCorrectness("BUSolveEnd", new BUCheckerFactory(SolveEnd))
class TestBUSolveContinuouslyCorrectness extends TestCorrectness("BUSolveContinuously", new BUCheckerFactory(SolveContinuously))*/
class TestBUSolveContinuousSubstCorrectness extends TestCorrectness("BUSolveContinuousSubst", new BUCheckerFactory(SolveContinuousSubst))
//class TestBUSolveContinuousSubstThresholdCorrectness extends TestCorrectness("BUSolveContinuousSubstThreshold", new BUCheckerFactory(SolveContinuousSubstThreshold))
//class TestBottomUpEagerSubstEarlyTermCorrectness extends TestCorrectness("BottomUpEagerSubstEarlyTerm", BottomUpEagerSubstEarlyTermCheckerFactory)

class TestFuturisticBUSolveContinuousSubstCorrectness extends TestCorrectness("FuturisticBUSolveContinuousSubst", new FuturisticBUCheckerFactory(SolveContinuousSubst))
