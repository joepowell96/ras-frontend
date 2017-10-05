package controllers

import models.RasSession
import play.api.mvc.Result

trait PageFlowController extends RasController{

  val MEMBER_NAME = "MemberNameController"
  val MEMBER_NINO = "MemberNinoController"
  val MEMBER_DOB = "MemberDOBController"
  val RESULTS = "ResultsController"


  def nextPage(from: String, session: RasSession): Result = {
    forwardNavigation.get(from) match {
      case Some(redirect) => redirect(session)
      case None => NotFound
    }
  }

  val forwardNavigation: Map[String, RasSession => Result] = Map(
    MEMBER_NAME    -> { (session: RasSession) => Redirect(routes.MemberNinoController.get) },
    MEMBER_NINO    -> { (session: RasSession) => Redirect(routes.MemberDOBController.get) },
    MEMBER_DOB     -> {
      (session: RasSession) =>
        if(session.residencyStatusResult.currentYearResidencyStatus.isEmpty)
          Redirect(routes.ResultsController.noMatchFound())
        else
          Redirect(routes.ResultsController.matchFound())
    }
  )

  def previousPage(from: String, session: RasSession): Result = {
    backNavigation.get(from) match {
      case Some(redirect) => redirect(session)
      case None => NotFound
    }
  }

  val backNavigation: Map[String, RasSession => Result] = Map(
    MEMBER_NAME    -> {
      (session: RasSession) =>
        if(session.residencyStatusResult.currentYearResidencyStatus.isEmpty)
          Redirect(routes.ResultsController.noMatchFound())
        else
          Redirect(routes.ResultsController.matchFound())
    },
    MEMBER_NINO    -> { (session: RasSession) => Redirect(routes.MemberNameController.get) },
    MEMBER_DOB     -> { (session: RasSession) => Redirect(routes.MemberNinoController.get) },
    RESULTS        -> { (session: RasSession) => Redirect(routes.MemberDOBController.get) }
  )

}
