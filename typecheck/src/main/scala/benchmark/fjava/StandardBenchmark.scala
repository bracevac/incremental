package benchmark.fjava

import org.scalameter.api._
import _root_.benchmark.pcf.Settings
import incremental.Node.Node
import Checkers._
import incremental.fjava.TypeChecker

/**
  * Standard execution, not parallel, not incremental
  */
class StandardBenchmarkClass extends Bench.OfflineReport {

  val opts = org.scalameter.api.Context(
    exec.jvmflags -> (List("-server", "-XX:CompileThreshold=100") ++ Settings("jvmopts"))
  )

  override def reporter: Reporter[Double] = Reporter.Composite(
    new RegressionReporter(tester, historian),
    DsvReporter(' '),
    HtmlReporter(!online)
  )

  def measureT(name: String, checker: TypeChecker[_])(trees: Gen[(Node, Long)]): Unit = {
    measure method (name) in {
      using(trees).
        setUp { case (tree, size) =>
          tree.invalidate
          checker.prepare(tree)
        }.
        in { case (tree, size) => checker.typecheck(tree);println("ok") }
    }
  }

  def measureCheckers(trees: Gen[(Node, Long)]): Unit = {
    measureT("JAVAC", javac)(trees)
    measureT("DU", du)(trees)
    //measureT("BU-End", buEnd)(trees)
    //measureT("BU-Cont", buCont)(trees)
    measureT("BU-Early-Cont", buEarlyCont)(trees)
  }

  measureCheckers(Trees.intAccumSuperHierarchyTrees)
  measureCheckers(Trees.intAccumPrevHierarchyTrees)
  measureCheckers(Trees.intAccumPrevSuperHierarchyTrees)
  measureCheckers(Trees.purelyFunctionalDataStructures)
}

// Profiler friendly version
class StandardBenchmarkClassProfile extends Bench.LocalTime {

  val opts = org.scalameter.api.Context(
    exec.jvmflags -> (List("-server", "-XX:CompileThreshold=100") ++ Settings("jvmopts"))
  )

  def measureT(name: String, checker: TypeChecker[_])(trees: Gen[(Node, Long)]): Unit = {
    measure method (name) in {
      using(trees).
        setUp { case (tree, size) =>
          tree.invalidate
          checker.prepare(tree)
        }.
        in { case (tree, size) => val res = checker.typecheck(tree); {if (res.isRight) println(res)}; println("ok") }
    }
  }

  def measureCheckers(trees: Gen[(Node, Long)]): Unit = {
  //
    //  measureT("JAVAC", javac)(trees)
    measureT("DU", du)(trees)
    //measureT("BU-End", buEnd)(trees)
    //measureT("BU-Cont", buCont)(trees)
    //measureT("BU-Early-Cont", buEarlyCont)(trees)
  }

  measureCheckers(Trees.intAccumSuperHierarchyTrees)
  measureCheckers(Trees.intAccumPrevHierarchyTrees)
  measureCheckers(Trees.intAccumPrevSuperHierarchyTrees)
  measureCheckers(Trees.purelyFunctionalDataStructures)
}

object StandardBenchmark {
  def main(args: Array[String]): Unit = {
    val scalameterArgs = Array("-CresultDir", "./benchmark/fjava")
    new StandardBenchmarkClass().main(scalameterArgs)
  }
}

object StandardBenchmarkProfile {
  def main(args: Array[String]): Unit = {
    println("Prepare profiler, then hit return...")
    System.in.read()
    val scalameterArgs = Array("-CresultDir", "./benchmark/fjava")
    new StandardBenchmarkClassProfile().main(scalameterArgs)
  }
}