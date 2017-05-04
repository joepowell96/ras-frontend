package controllers

import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future


object HelloWorldController extends HelloWorldController

trait HelloWorldController extends FrontendController {

  def helloWorld = Action.async { implicit request =>
		Future.successful(Ok(uk.gov.hmrc.rasfrontend.views.html.helloworld.hello_world()))
  }

}
