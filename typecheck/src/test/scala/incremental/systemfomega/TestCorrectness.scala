package incremental.systemfomega

import constraints.CVar
import constraints.normequality.impl.{SolveContinuousSubst, SolveContinuousSubstThreshold, SolveContinuously, SolveEnd}
import constraints.normequality.{ConstraintSystem, Type}
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

  typecheckTest("17", Num(17))(TNum())
  typecheckTest("17+(10+2)", Add(Num(17), Add(Num(10), Num(2))))(TNum())
  typecheckTest("17+(10+5)", Add(Num(17), Add(Num(10), Num(5))))(TNum())
  typecheckTest("\\x. 10+5", Abs('x, Add(Num(10), Num(5))))(TFun(UVar(CVar('x$0)), TNum()))
  typecheckTest("\\x. x+x", Abs('x, Add(Var('x), Var('x))))(TFun(TNum(), TNum()))
  typecheckTestError("\\x. err+x", Abs('x, Add(Var('err), Var('x))))
  typecheckTest("\\x. \\y. x y", Abs('x, Abs('y , App(Var('x), Var('y)))))(TFun(TFun(UVar(CVar('x$1)), UVar(CVar('x$2))), TFun(UVar(CVar('x$1)), UVar(CVar('x$2)))))
  typecheckTest("\\x. \\y. x + y", Abs('x, Abs('y, Add(Var('x), Var('y)))))(TFun(TNum(), TFun(TNum(), TNum())))
  typecheckTest("if0(17, 0, 1)", If0(Num(17), Num(0), Num(1)))(TNum())

  // test polymorphism

  typecheckTestError("\\x. y", Abs('x, Var('y)))
  typecheckTestError("\\x: a . x", Abs('x, TVar.Kind('a), Var('x)))
  typecheckTest("\\a. \\x : a. x", TAbs('a, Abs('x, TVar.Kind('a), Var('x))))(TUniv('a, Some(KStar), TFun(TVar('a), TVar('a))))
  typecheckTestError("\\a. \\x : a. x + x", TAbs('a, Abs('x, TVar.Kind('a), Add(Var('x), Var('x)))))
  typecheckTest("\\a. \\f : a -> a. \\x:a. f x", TAbs('a, Abs('f,TFun.Kind(TVar.Kind('a),TVar.Kind('a)),Abs('x, TVar.Kind('a), App(Var('f),Var('x))))))(TUniv('a, Some(KStar), TFun(TFun(TVar('a), TVar('a)),TFun(TVar('a), TVar('a)))))
  typecheckTestError("\\a. \\b. \\f:a->a . \\x:b. f x", TAbs('a,TAbs('b, Abs('f, TFun.Kind(TVar.Kind('a), TVar.Kind('a)),Abs('x, TVar.Kind('b), App(Var('f),Var('x)))))))
  typecheckTestError("\\a. \\b. \\f:a . \\x:b. f x", TAbs('a,TAbs('b, Abs('f, TVar.Kind('a), Abs('x, TVar.Kind('b), App(Var('f),Var('x)))))))
  typecheckTest("(\\a. \\x : a. x) [Num]", TApp(TAbs('A, Abs('x, TVar.Kind('A), Var('x))), TNum.Kind()))(TFun(TNum(),TNum()))
  typecheckTest("\\b. (\\a. \\x : a. x) [b]", TAbs('B, TApp(TAbs('A, Abs('x, TVar.Kind('A), Var('x))), TVar.Kind('B))))(TUniv('B, Some(KStar), TFun(TVar('B),TVar('B))))
  typecheckTestError("\\x. x [Num]", Abs('x, TApp(Var('x), TNum.Kind())))
  typecheckTestError("\\x. (x [Num]) + 1", Abs('x, Add(TApp(Var('x), TNum.Kind()), Num(1))))
  typecheckTestError("\\x:X. x", Abs('x, TVar.Kind('X), Var('x)))
  typecheckTestError("(\\X.\\x:X. x)[Y]", TApp(TAbs('X, Abs('x, TVar.Kind('X), Var('x))), TVar.Kind('Y)))
  typecheckTest("\\f:(forall a. a)->TNum(). \\x:(forall b. b) f x",
    Abs('f, TFun.Kind(TUniv.Kind('a, KStar, TVar.Kind('a)), TNum.Kind()), Abs('x, TUniv.Kind('b, KStar, TVar.Kind('b)), App(Var('f), Var('x)))))(
    TFun(TFun(TUniv('a, Some(KStar), TVar('a)), TNum()), TFun(TUniv('b, Some(KStar), TVar('b)), TNum()))
  )

  // test simple kinding
  typecheckTest("\\a::*. \\x:a. x", TAbs('a, KStar, Abs('x, TVar.Kind('a), Var('x))))(TUniv('a, Some(KStar), TFun(TVar('a), TVar('a))))
  typecheckTestError("\\a::*=>*. \\x:a. x", TAbs('a, KArrow(KStar, KStar), Abs('x, TVar.Kind('a), Var('x))))
  typecheckTestError("\\a::*=>*. \\x:a->a. x", TAbs('a, KArrow(KStar, KStar), Abs('x, TFun.Kind(TVar.Kind('a), TVar.Kind('a)), Var('x))))

  // test type operators
  def tId = TTAbs.Kind('X, KStar, TVar.Kind('X))
  def tNumOp = TTAbs.Kind('F, KArrow(KStar, KStar), TTApp.Kind(TVar.Kind('F), TNum.Kind()))

  typecheckTest("\\A::*=>* \\x:(A TNum()). x",
    TAbs('a, KArrow(KStar, KStar), Abs('x, TTApp.Kind(TVar.Kind('a), TNum.Kind()), Var('x))))(
    TUniv('a, Some(KArrow(KStar, KStar)), TFun(TTApp(TVar('a), TNum()), TTApp(TVar('a), TNum())))
  )

  typecheckTest("\\x:(tId TNum()). x", Abs('x, TTApp.Kind(tId, TNum.Kind()), Var('x)))(TFun(TNum(), TNum()))
  typecheckTest("\\x:(tId TNum()). x [2]", Abs('x, TTApp.Kind(tId, TNum.Kind()), Var('x)))(TFun(TNum(), TTApp(Type.from(tId), TNum())))

  typecheckTest("\\x:(tNumOp tId). x", Abs('x, TTApp.Kind(tNumOp, tId), Var('x)))(TFun(TNum(), TNum()))
  typecheckTest("\\x:(tNumOp tId). \\y:(tNumOp tId). x + y", Abs('x, TTApp.Kind(tNumOp, tId), Abs('y, TTApp.Kind(tNumOp, tId), Add(Var('x), Var('y)))))(TFun(TNum(), TFun(TNum(), TNum())))
}

class TestDUSolveEndCorrectness extends TestCorrectness("DUSolveEnd", new DUCheckerFactory(SolveEnd))
class TestDUSolveContniuouslyCorrectness extends TestCorrectness("DUSolveContinuously", new DUCheckerFactory(SolveContinuously))

//class TestBUSolveEndCorrectness extends TestCorrectness("BUSolveEnd", new BUCheckerFactory(SolveEnd))
//class TestBUSolveContinuouslyCorrectness extends TestCorrectness("BUSolveContinuously", new BUCheckerFactory(SolveContinuously))
//class TestBUSolveContinuousSubstCorrectness extends TestCorrectness("BUSolveContinuousSubst", new BUCheckerFactory(SolveContinuousSubst))
//class TestBUSolveContinuousSubstThresholdCorrectness extends TestCorrectness("BUSolveContinuousSubstThreshold", new BUCheckerFactory(SolveContinuousSubstThreshold))
//class TestBottomUpEagerSubstEarlyTermCorrectness extends TestCorrectness("BottomUpEagerSubstEarlyTerm", BottomUpEagerSubstEarlyTermCheckerFactory)
