package controllers

import helpers.helpers.I18nHelper
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PageFlowControllerSpec extends UnitSpec with WithFakeApplication with I18nHelper with MockitoSugar{

  object PageFlowControllerSpec extends PageFlowController{

  }

}
