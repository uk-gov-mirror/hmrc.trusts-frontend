/*
 * Copyright 2021 HM Revenue & Customs
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

package models.core

import _root_.pages.register.suitability.TrustTaxableYesNoPage
import models.MongoDateTimeFormats
import models.registration.pages.RegistrationStatus
import models.registration.pages.RegistrationStatus.NotStarted
import play.api.Logging
import play.api.libs.json._
import queries.{Gettable, Settable}

import java.time.LocalDateTime
import scala.util.{Failure, Success, Try}

trait ReadableUserAnswers {
  val data: JsObject
  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] = {
    Reads.at(page.path).reads(data) match {
      case JsSuccess(value, _) => Some(value)
      case JsError(_) =>
        None
    }
  }
}

case class ReadOnlyUserAnswers(data: JsObject) extends ReadableUserAnswers

object ReadOnlyUserAnswers {
  implicit lazy val formats: OFormat[ReadOnlyUserAnswers] = Json.format[ReadOnlyUserAnswers]
}

final case class UserAnswers(
                              draftId: String,
                              data: JsObject = Json.obj(),
                              progress : RegistrationStatus = NotStarted,
                              createdAt : LocalDateTime = LocalDateTime.now,
                              internalAuthId :String
                            ) extends ReadableUserAnswers with Logging {

  import UserAnswerImplicits._

  def isTaxable: Boolean = !this.get(TrustTaxableYesNoPage).contains(false)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        val errorPaths = errors.collectFirst{ case (path, e) => s"$path $e"}
        logger.warn(s"Unable to set path ${page.path} due to errors $errorPaths")
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy (data = d)
        page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](query: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(query.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy (data = d)
        query.cleanup(None, updatedAnswers)
    }
  }

  def deleteAtPath(path: JsPath): Try[UserAnswers] = {
    data.removeObject(path).map(obj => copy(data = obj)).fold(
      _ => Success(this),
      result => Success(result)
    )
  }
}

object UserAnswers {

  implicit lazy val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").read[String] and
      (__ \ "data").read[JsObject] and
      (__ \ "progress").read[RegistrationStatus] and
      (__ \ "createdAt").read(MongoDateTimeFormats.localDateTimeRead) and
      (__ \ "internalId").read[String]
    ) (UserAnswers.apply _)
  }

  implicit lazy val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").write[String] and
      (__ \ "data").write[JsObject] and
      (__ \ "progress").write[RegistrationStatus] and
      (__ \ "createdAt").write(MongoDateTimeFormats.localDateTimeWrite) and
      (__ \ "internalId").write[String]
    ) (unlift(UserAnswers.unapply))
  }
}
