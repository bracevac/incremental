package incremental.systemf

import constraints.equality.Type.Companion.TSubst
import constraints.equality._

// body[alpha := substitute] = result
case class EqSubstConstraint(body: Type, alpha: Symbol, alphaIsInternal: Boolean, substitute: Type, result: Type) extends Constraint {
  private def withResult[CS <: ConstraintSystem[CS]](t: Type, cs: CS) : CS = t.unify(result, cs)

  private def substAlpha(s: TSubst) =
    if (!alphaIsInternal) (alpha, false)
    else s.get(alpha) match {
      case Some(TVar(beta)) => (beta, false)
      case Some(UVar(beta)) => (beta, true)
      case None => (alpha, alphaIsInternal)
      case Some(_) => throw new IllegalArgumentException(s"Cannot replace type bound by non-variable type")
    }

  def solve[CS <: ConstraintSystem[CS]](cs: CS): CS = {
    val tbody = body.subst(cs.substitution)
    val (beta, betaIsInternal) = substAlpha(cs.substitution)

    tbody match {
      case TVar(`beta`) | UVar(`beta`) => withResult(substitute, cs)
      case TVar(_) if !betaIsInternal => withResult(tbody, cs) // because alpha is user-defined and different

      case TNum => withResult(TNum, cs)
      case TFun(t1, t2) =>
        val X = cs.gen.freshUVar()
        val Y = cs.gen.freshUVar()
        val cons1 = EqSubstConstraint(t1, beta, betaIsInternal, substitute, X)
        val cons2 = EqSubstConstraint(t2, beta, betaIsInternal, substitute, Y)
        withResult(TFun(X, Y), cons1.solve(cons2.solve(cs)))
      case TUniv(`beta`, _) => withResult(tbody, cs)
      case TUniv(gamma, t) if !betaIsInternal =>
        val X = cs.gen.freshUVar()
        val tcons = EqSubstConstraint(t, beta, betaIsInternal, substitute, X)
        withResult(TUniv(gamma, X), tcons.solve(cs))

      // either:
      // - tbody == TVarInternal   <== Example: x. (x [Num]) + (x [Num -> Num])
      // - tbody == TVar && betaIsInternal ==> cannot happen, type argument always has to become concrete
      // - tbody == TUnivInternal ==> cannot happen, universal types have explicit argument in TAbs('alpha, t)
      // - tbody == TUni && betaIsInternal ==> cannot happen, type argument always has to become concrete
      case _ => cs.notyet(EqSubstConstraint(tbody, beta, betaIsInternal, substitute, result))
    }
  }

  def finalize[CS <: ConstraintSystem[CS]](cs: CS) = solve(cs)

  def subst(s: TSubst) = {
    val (newalpha, newalphaIsInternal) = substAlpha(s)
    EqSubstConstraint(body.subst(s), newalpha, newalphaIsInternal, substitute.subst(s), result.subst(s))
  }
}