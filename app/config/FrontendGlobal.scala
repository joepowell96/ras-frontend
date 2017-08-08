/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import controllers.routes
import net.ceedubs.ficus.Ficus._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import play.api._
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, MissingBearerToken, NoActiveSession}
import uk.gov.hmrc.auth.filter.FilterConfig
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter

import scala.concurrent.Future


object FrontendGlobal extends DefaultFrontendGlobal with MicroserviceFilterSupport with Redirects {

  override val auditConnector = FrontendAuditConnector
  override val loggingFilter = LoggingFilter
  override val frontendAuditFilter = AuditFilter

  lazy val config = Play.current.configuration
  lazy override val env = Environment(Play.current.path, Play.current.classloader, Play.current.mode)

  override def onStart(app: Application) {
    super.onStart(app)
    ApplicationCrypto.verifyConfiguration()
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html =
    views.html.error(pageTitle, heading, message)

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"microservice.metrics")

  override def filters: Seq[EssentialFilter] = super.filters ++ Seq(AuthorisationFilter())

  override def toGGLogin(continueUrl: String): Result = super.toGGLogin(continueUrl)

  override def resolveError(rh: RequestHeader, ex: Throwable) = ex match {

    /*case ex: InsufficientEnrolments =>  Logger.error("Insufficient priviliges" + ex.getMessage);super.resolveError(rh, ex)
    //Redirect(routes.GlobalErrorController.get)// Results.Unauthorized("you are not authorised to access this service")

    case ex:MissingBearerToken =>  Logger.error("Missing Bearer token, user not Logged In" + ex.getMessage);
      super.resolveError(rh, ex)*/
    case _: NoActiveSession => Logger.error("Missing Bearer token, user not Logged In" + ex.getMessage); toGGLogin("/continue-url")//Results.Unauthorized("UNAUTHORISED")
    case _ => super.resolveError(rh, ex)
  }
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object LoggingFilter extends FrontendLoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object AuditFilter extends FrontendAuditFilter with RunMode with AppName with MicroserviceFilterSupport {

  override lazy val maskedFormFields = Seq("password")

  override lazy val applicationPort = None

  override lazy val auditConnector = FrontendAuditConnector

  override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}


object AuthorisationFilter {
  def apply()(implicit m: Materializer) = new uk.gov.hmrc.auth.filter.AuthorisationFilter {
    override def config: FilterConfig = FilterConfig(Play.current.configuration.getConfig("controllers")
      .map(_.underlying)
      .getOrElse(ConfigFactory.empty()))

    override def connector: uk.gov.hmrc.auth.core.AuthConnector = RasFrontendAuthConnector

    override implicit def mat: Materializer = m
  }
}