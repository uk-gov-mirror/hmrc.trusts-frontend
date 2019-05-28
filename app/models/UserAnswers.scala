/*
 * Copyright 2019 HM Revenue & Customs
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

package models

import java.time.LocalDateTime

import models.RegistrationProgress.NotStarted
import pages._
import play.api.Logger
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

final case class UserAnswers(
                              id: String,
                              data: JsObject = Json.obj(),
                              progress : RegistrationProgress = NotStarted,
                              createdAt : LocalDateTime = LocalDateTime.now
                            ) {

  def get[A](page: QuestionPage[A])(implicit rds: Reads[A]): Option[A] = {
    Reads.at(page.path).reads(data) match {
      case JsSuccess(value, _) => Some(value)
      case JsError(errors) =>
        val errorPaths = errors.collectFirst{ case (path, _) => path}
        Logger.error(s"[UserAnswers] unable to read to ${page.path} due to errors $errorPaths")
        None
    }
  }

  def set[A](page: QuestionPage[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        val errorPaths = errors.collectFirst{ case (path, e) => s"$path $e"}
        Logger.warn(s"[UserAnswers] unable to set path ${page.path} due to errors $errorPaths")
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy (data = d)
        page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](page: QuestionPage[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, JsNull) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy (data = d)
        page.cleanup(None, updatedAnswers)
    }
  }
}

object UserAnswers {

  implicit lazy val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").read[String] and
      (__ \ "data").read[JsObject] and
      (__ \ "progress").read[RegistrationProgress] and
      (__ \ "createdAt").read(MongoDateTimeFormats.localDateTimeRead)
    ) (UserAnswers.apply _)
  }

  implicit lazy val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").write[String] and
      (__ \ "data").write[JsObject] and
      (__ \ "progress").write[RegistrationProgress] and
      (__ \ "createdAt").write(MongoDateTimeFormats.localDateTimeWrite)
    ) (unlift(UserAnswers.unapply))
  }
}
