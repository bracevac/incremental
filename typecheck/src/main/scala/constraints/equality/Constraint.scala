package constraints.equality

import Type.Companion._

trait Constraint {
  def solve[CS <: ConstraintSystem[CS]](s: ConstraintSystem[CS], csf: ConstraintSystemFactory[CS]): CS
  def finalize[CS <: ConstraintSystem[CS]](s: ConstraintSystem[CS], csf: ConstraintSystemFactory[CS]): CS
  def subst(s: TSubst): Constraint
}

case class EqConstraint(expected: Type, actual: Type) extends Constraint {
  def solve[CS <: ConstraintSystem[CS]](s: ConstraintSystem[CS], csf: ConstraintSystemFactory[CS]): CS = expected.unify(actual, s.substitution)(csf)
  def finalize[CS <: ConstraintSystem[CS]](s: ConstraintSystem[CS], csf: ConstraintSystemFactory[CS]): CS = solve(s, csf)
  def subst(s: TSubst): EqConstraint = EqConstraint(expected.subst(s), actual.subst(s))
}