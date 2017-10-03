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

package repositories

import models.JourneyHistory
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DefaultDB
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.Repository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class JourneyHistoryMongoRepository (implicit mongo: () => DefaultDB)
  extends ReactiveRepository[JourneyHistory, BSONObjectID]("ras-journey-history",mongo, JourneyHistory.formats)
  with JourneyHistoryRepository{

  override def insertDocument(journeyHistory: JourneyHistory): Future[Boolean] = {
    collection.insert(journeyHistory).map(res => res.ok)
  }

}

trait JourneyHistoryRepository extends Repository[JourneyHistory, BSONObjectID]{

  def insertDocument(journeyHistory: JourneyHistory): Future[Boolean]

}

object JourneyHistoryRepository extends MongoDbConnection {
  private lazy val repository = new JourneyHistoryMongoRepository
  def apply(): JourneyHistoryMongoRepository = repository
}