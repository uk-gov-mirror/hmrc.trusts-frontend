/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.actions.register

import javax.inject.Inject
import models.core.UserAnswers
import models.requests.{IdentifierRequest, OptionalRegistrationDataRequest}
import play.api.mvc.ActionTransformer
import repositories.RegistrationsRepository

import scala.concurrent.{ExecutionContext, Future}

class RegistrationDataRetrievalActionImpl @Inject()(val registrationsRepository: RegistrationsRepository)
                                                   (implicit val executionContext: ExecutionContext) extends RegistrationDataRetrievalAction {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalRegistrationDataRequest[A]] = {

    def createdOptionalDataRequest(request: IdentifierRequest[A], userAnswers: Option[UserAnswers]) =
      OptionalRegistrationDataRequest(request.request, request.identifier, userAnswers, request.affinityGroup, request.enrolments, request.agentARN)

    registrationsRepository.getDraftRegistrations(request.identifier).flatMap {
      ids =>
        ids.headOption match {
          case None =>
            Future.successful(createdOptionalDataRequest(request, None))
          case Some(userAnswer) =>
            registrationsRepository.get(userAnswer.draftId, userAnswer.internalAuthId).map {
              case None =>
                createdOptionalDataRequest(request, None)
              case Some(userAnswers) =>
                createdOptionalDataRequest(request, Some(userAnswers))
            }
        }
    }
  }
}

trait RegistrationDataRetrievalAction extends ActionTransformer[IdentifierRequest, OptionalRegistrationDataRequest]
