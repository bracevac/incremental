package benchmark.pcf

import incremental.pcf.Exp.Exp
import incremental.{TypeChecker, TypeCheckerFactory}
import org.scalameter.DSL
import org.scalameter.api._
import benchmark.ExpGenerator._

import incremental.pcf._
import incremental.Node._

abstract class IncrementalPerformanceTest(maxHeight: Int) extends PerformanceTest {
  val heights: Gen[Int] = Gen.range("height")(2, maxHeight, 2)

  def measureCheckers(maxtree: Node, heights: Gen[Int]): Unit = {
//    val du = DownUpCheckerFactory.makeChecker
//    val bu1 = BottomUpSolveEndCheckerFactory.makeChecker
//    val bu2 = BottomUpSometimesEagerSubstCheckerFactory.makeChecker
//    val bu3 = BottomUpEagerSubstCheckerFactory.makeChecker
//    val bu4 = BottomUpSometimesEagerSubstCheckerFactory.makeChecker(10)

//    measureIncremental("DownUp", (e:Node) => du.typecheck(e))(maxtree, heights)
//    measureIncremental("BottomUpSolveEnd", (e:Exp) => bu1.typecheck(maxtree))(maxtree, heights)
//    measureIncremental("BottomUpIncrementalSolve", (e:Node) => bu2.typecheck(e))(maxtree, heights)
//    measureIncremental("BottomUpEagerSubst", (e:Node) => bu3.typecheck(e))(maxtree, heights)
//    measureIncremental(s"BottomUpSometimesEagerSubst-10", (e:Node) => bu4.typecheck(e))(maxtree, heights)

    //    val thresholds = Gen.exponential("threshold")(10, 10000, 10)
    //    val tupled = Gen.tupled(trees,thresholds)
    //    measureTwith(s"BottomUpSometimesEagerSubst", (e:(Exp,Int)) => BottomUpSometimesEagerSubstCheckerFactory.makeChecker(e._2).typecheck(e._1))(tupled)
  }

  def measureIncremental(name: String, check: Node => _)(maxtree: Node, heights: Gen[Int]): Unit = {
    var firstime = true
    measure method (name) in {
      using(heights).
      setUp { h =>
        if (firstime) {
          check(maxtree)
          firstime = false
        }

        var e = maxtree
        while (e.isInstanceOf[Abs[Any]])
          e = e.kids(0)
        for (i <- 1 to maxHeight - h - 1)
          e = e.kids(0)
        e.invalidate
      }.
      in { _ => check(maxtree) }
    }
  }

  //  def measureTwith[T](name: String, check: ((Exp,T)) => _)(trees: Gen[(Exp,T)]): Unit = {
  //    measure method (name) in {
  //      using(trees).
  //        setUp { _._1.invalidate }.
  //        in { check }
  //    }
  //  }



  /* ADD */

  performance of "Tree{Add,[1..n]}" in {
    val maxtree = makeBinTree[Exp](maxHeight, Add.apply(_,_), stateLeaveMaker[Int, Exp](1, i => i + 1, i => Num(i)))
    measureCheckers(maxtree, heights)
  }

  performance of "Abs{x,Tree{Add,[x..x]}}" in {
    val maxtree = Abs('x, makeBinTree[Exp](maxHeight, Add.apply(_,_), constantLeaveMaker(Var('x))))
    measureCheckers(maxtree, heights)
  }

  performance of "Abs{x,Tree{Add,[x1..xn]}}" in {
    val maxtree = AbsMany(usedVars(maxHeight), makeBinTree[Exp](maxHeight, Add.apply(_,_), stateLeaveMaker[Int,Exp](1, i => i + 1, i => Var(Symbol(s"x$i")))))
    measureCheckers(maxtree, heights)
  }



  /* APP */

  performance of "Tree{App,[1..n]}" in {
    val maxtree = makeBinTree[Exp](maxHeight, App.apply(_,_), stateLeaveMaker[Int,Exp](1, i => i + 1, i => Num(i)))
    measureCheckers(maxtree, heights)
  }

  performance of "Abs{x,Tree{App,[x..x]}}" in {
    val maxtree = Abs('x, makeBinTree[Exp](maxHeight, App.apply(_,_), constantLeaveMaker(Var('x))))
    measureCheckers(maxtree, heights)
  }

  performance of "Abs{x,Tree{App,[x1..xn]}}" in {
    val maxtree = AbsMany(usedVars(maxHeight), makeBinTree[Exp](maxHeight, App.apply(_,_), stateLeaveMaker[Int,Exp](1, i => i + 1, i => Var(Symbol(s"x$i")))))
    measureCheckers(maxtree, heights)
  }
}




object Incremental {
  def main(args: Array[String]): Unit = {
    if (args.size != 2)
      throw new IllegalArgumentException("Expected arguments: (report|micro) maxHeight")

    val kind = args(0).toLowerCase
    val maxHeight = args(1).toInt

    val scalameterArgs = Array("-CresultDir", "./benchmark/incremental")

    if (kind == "report" || kind == "offlinereport")
      new IncrementalOfflineReport(maxHeight).main(scalameterArgs)
    else if (kind == "micro" || kind == "microbenchmark")
      new IncrementalMicroBenchmark(maxHeight).main(scalameterArgs)
  }
}

class IncrementalMicroBenchmark(maxHeight: Int)
  extends IncrementalPerformanceTest(maxHeight)
  with PerformanceTest.Quickbenchmark

class IncrementalOfflineReport(maxHeight: Int)
  extends IncrementalPerformanceTest(maxHeight)
  with PerformanceTest.OfflineReport {

  override def reporter: Reporter = Reporter.Composite(
    new RegressionReporter(tester, historian),
    DsvReporter(' '),
    HtmlReporter(!online)
  )
}
