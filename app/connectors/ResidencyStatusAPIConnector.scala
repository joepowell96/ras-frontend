package connectors

import models.ResidencyStatus

trait ResidencyStatusAPIConnector {

  def getResidencyStatus(uuid: String) : ResidencyStatus = ???

}

object ResidencyStatusAPIConnector extends ResidencyStatusAPIConnector