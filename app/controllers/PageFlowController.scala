package controllers

import models.RasSession
import play.api.mvc.Result

trait PageFlowController extends RasController{

  val backNavigation: Map[String, RasSession => Result] = Map(
    PageType.MEMBER_NAME    -> { (session: RasSession) => Redirect(routes.MemberNameController.get) },
    PageType.MEMBER_NINO    -> { (session: RasSession) => Redirect(routes.MemberNinoController.get) },
    PageType.MEMBER_DOB     -> { (session: RasSession) => Redirect(routes.MemberDOBController.get) },
    PageType.MATCH_FOUND    -> { (session: RasSession) => Redirect(routes.ResultsController.matchFound()) },
    PageType.NO_MATCH_FOUND -> { (session: RasSession) => Redirect(routes.ResultsController.noMatchFound()) }
  )

  def previousPage(from: String, session: RasSession): Result = {
    backNavigation.get(from) match {
      case Some(redirect) => redirect(session)
      case None => NotFound
    }
  }
}

object PageType {
  val MEMBER_NAME = "MemberNameController"
  val MEMBER_NINO = "MemberNinoController"
  val MEMBER_DOB = "MemberDOBController"
  val MATCH_FOUND = "ResultsController"
  val NO_MATCH_FOUND = "ResultsController"
}
