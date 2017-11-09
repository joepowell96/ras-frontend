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

package services

import java.io.{BufferedReader, InputStream, InputStreamReader}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import connectors.FileUploadConnector
import helpers.FromListToCaseClass
import models.{MemberDetails, MemberName, RasDate, RawMemberDetails}
import org.joda.time.LocalDate
import uk.gov.hmrc.http.HeaderCarrier

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

object FileProcessingService extends FileProcessingService {

  override val fileUploadConnector: FileUploadConnector = FileUploadConnector
}

trait FileProcessingService {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val fileUploadConnector: FileUploadConnector

  //val results:ListBuffer[String] = ListBuffer.empty

  def readFile(envelopeId: String, fileId: String)(implicit hc: HeaderCarrier): Future[List[String] ] = {

    fileUploadConnector.getFile(envelopeId, fileId).map{
      case Some(inputStream) => Source.fromInputStream(inputStream).getLines().toList
      case None => Nil
    }
  }

  private def inputStreamToString(is: InputStream) = {
    val inputStreamReader = new InputStreamReader(is)
    val bufferedReader = new BufferedReader(inputStreamReader)
    Iterator continually bufferedReader.readLine takeWhile (_ != null) mkString
  }

  def createMatchingData(inputRow:String): Option[RawMemberDetails] = {
//    if (inputRow.isEmpty) None

    fromListToCaseClass[RawMemberDetails](parseString(inputRow))
  }

  private def parseString(inputRow: String): Array[String] = {
    val cols = inputRow.split(",")

    cols ++ (for (x <- 0 until 4-cols.length ) yield "")
  }

  private def fromListToCaseClass[T] = new FromListToCaseClass[T]
}

