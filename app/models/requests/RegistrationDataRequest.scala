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

package models.requests

import models.core.UserAnswers
import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}

case class OptionalRegistrationDataRequest[A](request: Request[A],
                                              internalId: String,
                                              sessionId: String,
                                              userAnswers: Option[UserAnswers],
                                              affinityGroup: AffinityGroup,
                                              enrolments: Enrolments,
                                              agentARN: Option[String] = None) extends WrappedRequest[A](request)

case class RegistrationDataRequest[A](request: Request[A],
                                      internalId: String,
                                      sessionId: String,
                                      userAnswers: UserAnswers,
                                      affinityGroup: AffinityGroup,
                                      enrolments: Enrolments,
                                      agentARN: Option[String] = None) extends WrappedRequest[A](request) {

  def isAgent: Boolean = affinityGroup == AffinityGroup.Agent
}
