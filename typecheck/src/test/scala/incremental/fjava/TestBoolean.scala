package incremental.fjava

import constraints.fjava._
import constraints.fjava.impl._
import incremental.Node._
import incremental.fjava.latemerge.{BUChecker, BUCheckerFactory}
import org.scalatest.{BeforeAndAfterEach, FunSuite}
import util.parse.Interpolators._

import scala.collection.immutable.ListMap

/**
 * Created by lirakuci on 3/29/15.
 */
class TestBoolean[CS <: ConstraintSystem[CS]](classdesc: String, checkerFactory: TypeCheckerFactory[CS]) extends FunSuite with BeforeAndAfterEach {
  val checker: TypeChecker[CS] = checkerFactory.makeChecker

  override def afterEach: Unit = checker.localState.printStatistics()

  def typecheckTest(desc: String, e: => Node)(expected: Type): Unit =
    test(s"$classdesc: Type check $desc") {
      val ev = e
      val actual = checker.typecheck(ev)

//      val typ = ev.withType[checker.Result].typ._1
//      val req = ev.withType[checker.Result].typ._2
//      val creq = ev.withType[checker.Result].typ._3
//      val cons = ev.withType[checker.Result].typ._4
      assert(actual.isLeft, actual.right)

      val sol = SolveContinuousSubstLateMerge.state.withValue(checker.csFactory.state.value) {
        Equal(expected, actual.left.get).solve(SolveContinuousSubstLateMerge.freshConstraintSystem).tryFinalize      }
      assert(sol.isSolved, s"Expected $expected but got ${actual.left.get}. Match failed with ${sol.unsolved}")
    }

  def typecheckTestError(desc: String, e: => Node) =
    test(s"$classdesc: Type check $desc") {
      val actual = checker.typecheck(e)
      assert(actual.isRight, s"Expected type error but got $actual")
    }


  val Bool =
    jclass"""
       class Bool {
         Object not() {
           return new Bool();
         }
         Object ifTrue(Object vthen, Object velse) {
           return new Object();
         }
       }
      """

  val True =
    jclass"""
       class True extends Bool {
         Object not() {
           return new False();
         }

         Object ifTrue(Object vthen, Object velse) {
           return vthen;
         }
       }
      """

  val False =
    jclass"""
       class False extends Bool {
         Object not() {
           return new True();
         }

         Object ifTrue(Object vthen, Object velse) {
           return velse;
         }
       }
      """

  typecheckTest("Bool ok", ProgramM(Bool))(ProgramOK)

  // without True knowing False (and vice versa), the class fails to check
  typecheckTestError("True ok", True)
  typecheckTestError("False ok", False)

  // Taking all classes into consideration, checking should succeed
  typecheckTest("{Boolean, True, False} ok", ProgramM(Bool, True, False))(ProgramOK)
}

class TestDUSolveEndBoolean extends TestBoolean("DUSolveEnd", new DUCheckerFactory(SolveEnd))
class TestBUSolveEndBoolean extends TestBoolean("BUSolveEnd", new BUCheckerFactory(SolveEnd))
class TestBUSolveContinuousSubstBoolean extends TestBoolean("BUSolveContinuousSubst", new BUCheckerFactory(SolveContinuousSubstLateMerge))

class TestBUEarlySolveContinuousSubstBoolean extends TestBoolean("BUEarlySolveContinuousSubst", new earlymerge.BUCheckerFactory(SolveContinuousSubstEarlyMerge))
