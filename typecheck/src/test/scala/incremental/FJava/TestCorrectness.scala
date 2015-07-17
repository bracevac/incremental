package incremental.FJava

import constraints.CVar
import constraints.equality.impl._
import constraints.equality._
import incremental.Node._
import incremental.{ Node_, Util}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
 * Created by lirakuci on 3/29/15.
 */
class TestCorrectness[CS <: ConstraintSystem[CS]](classdesc: String, checkerFactory: BUCheckerFactory[CS]) extends FunSuite with BeforeAndAfterEach {
  val checker: BUChecker[CS] = checkerFactory.makeChecker

  override def afterEach: Unit = checker.localState.printStatistics()

  def typecheckTest(desc: String, e: => Node)(expected: Type): Unit =
    test(s"$classdesc: Type check $desc") {
      val actual = checker.typecheck(e)
      assert(actual.isLeft, s"Expected $expected but got $actual")
      val sol = SolveContinuously.state.withValue(checker.csFactory.state.value) {
        expected.unify(actual.left.get, SolveContinuously.freshConstraintSystem).tryFinalize
      }
      assert(sol.isSolved, s"Expected $expected but got ${actual.left.get}. Match failed with ${sol.unsolved}")
    }

  def typecheckTestFJ(desc: String, e: => Node)(expected: Type): Unit =
    test(s"$classdesc: Type check $desc") {
      val ev = e
      val actual = checker.typecheck(ev)

      val typ = ev.withType[checker.Result].typ._1
      val req = ev.withType[checker.Result].typ._2
      val creq = ev.withType[checker.Result].typ._3
      val cons = ev.withType[checker.Result].typ._4
      assert(actual.isLeft, s"Type = $typ, Reqs = $req, CReqs = $creq, Constraint = $cons")

    }

  def typecheckTestError(desc: String, e: => Node) =
    test(s"$classdesc: Type check $desc") {
      val actual = checker.typecheck(e)
      assert(actual.isRight, s"Expected type error but got $actual")
    }


  typecheckTestError("e0.f : C ", Fields('f, Var('e0))) //(UCName(CVar('C)))
  typecheckTestError("new C(x):C", New(Seq(CName('c)), Seq(Var('x)))) //(CName('c))
  typecheckTestError("e0.m() : void", Invk(Seq('m), Seq(Var('e0)))) //(UCName(CVar('void)))
  typecheckTestError("e0.m(e) : Double", Invk(Seq('m), Seq(Var('e0), Var('e))))//(UCName(CVar('Double)))
  typecheckTestError("Point.add(p1, p2) : Point", Invk(Seq('add), Seq(Var('Point), Var('p1), Var('p2)))) //(UCName(CVar('Point)))
  typecheckTestError("(C) e0 :C", UCast(CName('c), Var('e)))

  typecheckTestError("new Pair(first) : Pair", New(CName('Pair), Var('first)))
  typecheckTestError("new Pair(snd): Pair", New(CName('Pair), Var('object)))
  typecheckTestError("new Pair(first).setfst(first) : U ", Invk(Seq('setfst), Seq(New(Seq(CName('Pair)), Seq(Var('first))), Var('first))))//(CName('int))
  typecheckTestError("(Object)first : Object", UCast(CName('Object), Var('first)))
  typecheckTestError("(Pair) first : Pair", New(CName('Pair), Var('first)))
  typecheckTestError("(Pair) first : Pair, second : Pair", New(CName('Pair), Var('first), Var('second)))
  typecheckTestError("new Object()", New(CName('Object)))//(CName('Object)), created object class with empty fields and methods
  typecheckTestError("new Pair(fst : First, snd : Second)", New(Seq(CName('Pair)), Seq(Var('First), Var('Second))))//(CName('Pair)) // see again why eq constraint does not work

  typecheckTestError(" (new Pair(first)).first : Int", Fields('f, New(CName('Pair), Var('f)))) //(UCName(CVar('Int)))


  typecheckTestError("'void m(x, y) ", MethodDec(Seq(CName('void), 'm, Seq(('x, UCName(CVar('x))), ('y, UCName(CVar('y))))),
    Seq(New(CName('Object)))))//(CName('Object))
  typecheckTestFJ("'Double m( e0.m(x) ) in C", MethodDec(Seq(CName('void), 'm, Seq(('c, UCName(CVar('c))))),
    Seq(Invk(Seq('m), Seq(Var('e0), Var('c))))))(UCName(CVar('e0))) // recoursive call, so it is correct
  typecheckTestError("void m() {New Pair first}", MethodDec(Seq(CName('void), 'm, Seq()), Seq(Invk(Seq('setfst),
    Seq(New(CName('Pair), Var('first)), Var('first))))))//(CName('Pair))
  typecheckTestError("Pair first second", MethodDec(Seq(CName('Double), 'getSum, Seq()), Seq(Invk(Seq('add),
    Seq(Var('Point), Var('fst), Var('snd))))))//(CName('Double))

  //typecheckTestFJ("Tnum add(first, second) {first + seconds}", MethodDec(Seq(CName('TNum), 'getSum, Seq(('fst, UCName(CVar('fst))), ('snd, UCName(CVar('snd))))), Seq(Invk(Seq('add), Seq(Var('Point), Var('fst), Var('snd))))))(CName('Point))

  typecheckTestError("String add(first, second) {first + seconds}", MethodDec(Seq(CName('String), 'getSum, Seq(('fst, CName('TNum)), ('snd, CName('TNum)))),
    Seq(Add(Num(1), Num(2)))))//(CName('TNum))
  typecheckTestFJ("TNum add(first, second) {first + seconds}", MethodDec(Seq(CName('TNum), 'getSum, Seq(('fst, CName('TNum)), ('snd, CName('TNum)))),
    Seq(Add(Num(1), Num(2)))))(CName('TNum))

  //typecheckTestError("(C) e0 : C", DCast(CName('c),Var('e)))
  //typecheckTestError("(C) e0 : C", SCast(CName('c),'e))
  typecheckTestFJ("Int getX(x: Int) {return Int} ", MethodDec(Seq(CName('Int), 'getX, Seq(('x, UCName(CVar('Int))))),
    Seq(New(CName('Int)))))(CName('Number))
  typecheckTestFJ("Int getX(x: Int) {Number.getX(x): Int}", MethodDec(Seq(CName('Int), 'getX, Seq(('x, UCName(CVar('Int))))),
    Seq(Invk(Seq('getX), Seq(Var('Number), Var('x))))))(CName('Number))

  typecheckTestFJ("Int getX(x: Int) {(new Nr).getX(x): Int} ", MethodDec(Seq(CName('Int), 'getX, Seq(('x, UCName(CVar('Int))))),
    Seq(Invk(Seq('getX), Seq(New(CName('Nr)), Var('x))))))(CName('Number))

  typecheckTestError("Int getX(x: Int) {(new Number).gX(x): Int}", MethodDec(Seq(CName('Int), 'getX, Seq(('x, UCName(CVar('Int))))),
    Seq(Invk(Seq('gX), Seq(New(CName('Number)), Var('x))))))//(CName('Number))

  typecheckTestError("Int getX(x: Int) {(new Number(x)).getX(x): Int} ", MethodDec(Seq(CName('Int), 'getX, Seq()),
    Seq(Invk(Seq('getX), Seq(New(CName('Number)), Var('x))))))//(CName('Number))

  typecheckTestError("Int getX(x: Int) {(new Number(x).x : Int in Number}", MethodDec(Seq(CName('Int), 'getX, Seq()),
    Seq(Fields('x, New(CName('Number), Var('x))))))//(CName('Number))

  typecheckTestError("Int getX(x: Int) {(new Number(x,y).x : Int in Number}", MethodDec(Seq(CName('Int), 'getX, Seq()),
    Seq(Fields('x, New(CName('Number), Var('x), Var('y))))))//(ßCName('Number))
  // /typecheckTestError("Int getXY(x,y) {return Int} in Number", Method(CName('Number), CName('Int), 'getXY, Seq('x, 'y),Var('e0)))


  typecheckTestError("e0.x + e0.x : TNum", Add(Fields('f, Num(1)), Fields('f, Num(1))))//(TNum)
  typecheckTestError("e0.x + e0.x : C", Add(Fields('f, Num(1)), Fields('f, Num(1))))//(CName('C))
  typecheckTestError("New Pair(new A(), new B()).snd", Fields('snd, New(Seq(CName('Pair)), Seq((New(CName('A))), (New(CName('B)))))))//(CName('B))
  typecheckTestError("New Pair(new A(), 'snd).snd", Fields('snd, New(Seq(CName('Pair)), Seq((New(CName('A))), Var('snd)))))//(CName('B))

  typecheckTestError("e0.m(x) + e0.m(x)", Add(Invk(Seq('m), Seq(Var('e0), Var('x))), Invk(Seq('m), Seq(Var('e0), Var('x)))))//(TNum)
  typecheckTest("x + x", Add(Var('x), Var('x)))(CName('TNum))

  typecheckTestError("(New Pair(fst, snd)).setFirst(fst)", Invk(Seq('setFrist), Seq(New(Seq(CName('Pair)), Seq(Var('frt), Var('snd))), Var('frt))))//(TNum)

  typecheckTestFJ(" Pair Int First ", ClassDec(Seq(CName('Pair), CName('Object), Seq(('First, CName('Int)))), Seq()))(CName('Pair))

  typecheckTestFJ(" Pair Int First, Int {(new Number(x)).getX(x): Int} ", ClassDec(Seq(CName('Pair), CName('Object), Seq(('First, CName('Int)))),
    Seq(MethodDec(Seq(CName('Int), 'getX, Seq(('x, CName('Int)))), Seq(New(CName('Int)))))))(CName('Pair))
  typecheckTestError(" Pair String First, Double getX(First : Double){ return (New Pair).First} ", ClassDec(Seq(CName('Pair), CName('Object), Seq(('First, CName('String)))),
    Seq(MethodDec(Seq(CName('Double), 'getX, Seq()), Seq(Fields('First, New(CName('Pair), Var('First))))))))//(CName('Pair))
  typecheckTestFJ(" Pair Double First, Double getX(First : Double){ return (New Pair).First}  ", ClassDec(Seq(CName('Pair), CName('Object), Seq(('First, CName('Double)))),
    Seq(MethodDec(Seq(CName('Double), 'getX, Seq()), Seq(Fields('First, New(CName('Pair), Var('First))))))))(CName('Pair))

  typecheckTestFJ(" Pair Int First, Int Second, Int setFirst() ", ClassDec(Seq(CName('Pair), CName('Object), Seq(('First, CName('Int)), ('Second, CName('Int)))),
    Seq(MethodDec(Seq(CName('Int), 'setFirst, Seq()), Seq(Fields('First,New(CName('Pair))))))))(CName('Pair))
  typecheckTestError(" Pair Int First, Int Second, String setFirst() ", ClassDec(Seq(CName('Pair), CName('Object), Seq(('First, CName('Int)), ('Second, CName('Int)))),
    Seq(MethodDec(Seq(CName('String), 'setFirst, Seq()), Seq(Fields('First,New(CName('Pair))))))))//(CName('Pair))

  typecheckTestFJ("Pair Int First, Int Second, Int add() { return (New Pair).add(first, second) }", ClassDec(Seq(CName('Pair), CName('Object),
    Seq(('First, CName('TNum)), ('Second, CName('TNum)))), Seq(MethodDec(Seq(CName('TNum), 'add, Seq()),
    Seq(Add(Fields('First, New(CName('Pair), Var('First))), Fields('Second, New(CName('Pair), Var('Second)))))))))(CName('Pair))

  typecheckTestFJ("Pair Int First, Int Second, Int addField() { return add(first Int, second Int) }", ClassDec(Seq(CName('TNum), CName('Object),
    Seq(('First, CName('TNum)), ('Second, CName('TNum)))), Seq(MethodDec(Seq(CName('TNum), 'add, Seq()),
    Seq(Add(Fields('First, New(CName('TNum))), Fields('Second, New(CName('TNum)))))))))(CName('TNum))

  typecheckTestError("Pair Int First, Int Second, String addField() { return add(first Int, second Int) }", ClassDec(Seq(CName('TNum), CName('Object),
    Seq(('First, CName('TNum)), ('Second, CName('TNum)))), Seq(MethodDec(Seq(CName('String), 'add, Seq()),
    Seq(Add(Fields('First, New(CName('TNum))), Fields('Second, New(CName('TNum)))))))))//(CName('TNum))

  typecheckTestError("Pair String First, Int Second, Int addField() { return add(first, second) }", ClassDec(Seq(CName('TNum), CName('Object),
    Seq(('First, CName('String)), ('Second, CName('TNum)))), Seq(MethodDec(Seq(CName('TNum), 'add, Seq()),
    Seq(Add(Fields('First, New(CName('TNum))), Fields('Second, New(CName('TNum))))))))) //(CName('TNum))

  typecheckTestError("Pair Int First, Int Second, Int addField() { return add(first : String, second : TNum) }", ClassDec(Seq(CName('TNum), CName('Object),
    Seq(('First, CName('TNum)), ('Second, CName('TNum)))), Seq(MethodDec(Seq(CName('TNum), 'add, Seq()),
    Seq(Add(Fields('First, New(CName('String))), Fields('Second, New(CName('TNum)))))))))//(CName('TNum))

  typecheckTestFJ("Pair Int x, Int getX(){ New(Pair(Int).x)}}",
    ClassDec(Seq(CName('Pair), CName('Object), Seq(('x, CName('Int)))),
      Seq(MethodDec(Seq(CName('Int), 'getX, Seq()), Seq(Fields('x, New(CName('Pair), Var('x))))))))(CName('Pair))

  typecheckTestFJ("Class C, Int foo(){return 1+1}} ", ClassDec(Seq(CName('C), CName('Object), Seq()),
    Seq(MethodDec(Seq(CName('TNum), 'foo, Seq()), Seq(Add(Num(1), Num(1)))))))(CName('C))
  typecheckTestError("String foo(){return 1+1}", MethodDec(Seq(CName('String), 'foo, Seq()), Seq(Add(Num(1), Num(1)))))//(CName('C))

  typecheckTestFJ("Int foo(){return 1+1}", MethodDec(Seq(CName('TNum), 'foo, Seq()), Seq(Add(Num(1), Num(1)))))(CName('C))
  typecheckTestError("string c.foo", MethodDec(Seq(CName('String), 'bar, Seq()), Seq(Invk(Seq('foo), Seq(New(CName('C)))))))//(CName('C))

  typecheckTestError("Class C, Int foo(){return 1+1}, String bar(){return foo();} ", ClassDec(Seq(CName('C), CName('Object), Seq()),
    Seq(MethodDec(Seq(CName('TNum), 'foo, Seq()), Seq(Add(Num(1), Num(1)))),
      MethodDec(Seq(CName('String), 'bar, Seq()), Seq(Invk(Seq('foo), Seq(New(CName('C)))))))))//(CName('C))

  typecheckTestFJ("Class C, Tnum foo(){return 1+1}, TNum bar(){return foo();} ", ClassDec(Seq(CName('C), CName('Object), Seq()),
    Seq(MethodDec(Seq(CName('TNum), 'foo, Seq()), Seq(Add(Num(1), Num(1)))),
      MethodDec(Seq(CName('TNum), 'bar, Seq()), Seq(Invk(Seq('foo), Seq(New(CName('C)))))))))(CName('C))

  typecheckTestFJ("Class C, Tnum foo(){return 1+1}, TNum bar(){return foo()}, TNum getBar() {return bar} ", ClassDec(Seq(CName('C), CName('Object), Seq()),
    Seq(MethodDec(Seq(CName('TNum), 'foo, Seq()), Seq(Add(Num(1), Num(1)))),
      MethodDec(Seq(CName('TNum), 'bar, Seq()), Seq(Invk(Seq('foo), Seq(New(CName('C)))))),
    MethodDec(Seq(CName('TNum), 'getbar, Seq()), Seq(Invk(Seq('bar), Seq(New(CName('C)))))))))(CName('C))

  typecheckTestError("Class C, Tnum foo(){return 1+1}, TNum bar(){return foo()}, String getBar() {return bar} ", ClassDec(Seq(CName('C), CName('Object), Seq()),
    Seq(MethodDec(Seq(CName('TNum), 'foo, Seq()), Seq(Add(Num(1), Num(1)))),
      MethodDec(Seq(CName('TNum), 'bar, Seq()), Seq(Invk(Seq('foo), Seq(New(CName('C)))))),
      MethodDec(Seq(CName('String), 'getbar, Seq()), Seq(Invk(Seq('bar), Seq(New(CName('C)))))))))//(CName('C))


  typecheckTestFJ("Class C, TNum add(a Int, b Int){ C.add(1, 2)}", ClassDec(Seq(CName('C), CName('Object), Seq()),
    Seq(MethodDec(Seq(CName('TNum), 'add, Seq(('a, CName('TNum)), ('b, CName('TNum)))), Seq(Invk(Seq('add), Seq(New(CName('C)), Num(1), Num(2))))))))(CName('C))

}

class TestBUSolveEndCorrectness extends TestCorrectness("BUSolveEnd", new BUCheckerFactory(SolveEnd))
//class TestBUSolveContinuouslyCorrectness extends TestCorrectness("BUSolveContinuously", new BUCheckerFactory(SolveContinuously))
//class TestBUSolveContinuousSubstCorrectness extends TestCorrectness("BUSolveContinuousSubst", new BUCheckerFactory(SolveContinuousSubst))
//class TestBUSolveContinuousSubstThresholdCorrectness extends TestCorrectness("BUSolveContinuousSubstThreshold", new BUCheckerFactory(SolveContinuousSubstThreshold))
