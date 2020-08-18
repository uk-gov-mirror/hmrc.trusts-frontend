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

package navigation.routes

import models.NormalMode
import models.core.UserAnswers
import pages.register.suitability._
import pages.{Page, QuestionPage}
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup

object SuitabilityRoutes {
  def route(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case TaxLiabilityInCurrentTaxYearYesNoPage => _ => ua =>
      yesNoNav(
        ua,
        TaxLiabilityInCurrentTaxYearYesNoPage,
        ???,
        controllers.register.suitability.routes.UndeclaredTaxLiabilityYesNoController.onPageLoad(NormalMode, draftId)
      )
  }

  private def yesNoNav(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call = {
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.register.routes.SessionExpiredController.onPageLoad())
  }
}

