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

package views.register.trust_details

import forms.TrustNameFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.register.trust_details.TrustNameView

class TrustNameViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "trustName"
  val hintKey = "trustName.hint.hasUtr"

  val form = new TrustNameFormProvider()()

  "TrustNameView view" when {

    "the trust is an existing trust" must {

      val view = viewFor[TrustNameView](Some(emptyUserAnswers))

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, NormalMode, fakeDraftId, hintTextShown = true)(fakeRequest, messages)

      behave like normalPage(applyView(form), Some("taskList.trustDetails.label"), messageKeyPrefix)

      behave like pageWithBackLink(applyView(form))

      behave like stringPage(form,
        applyView,
        Some("taskList.trustDetails.label"),
        messageKeyPrefix,
        Some(hintKey)
      )

      behave like pageWithASubmitButton(applyView(form))
    }

    "the trust is a new trust" must {

      val view = viewFor[TrustNameView](Some(emptyUserAnswers))

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, NormalMode, fakeDraftId, hintTextShown = false)(fakeRequest, messages)

      behave like normalPage(applyView(form), Some("taskList.trustDetails.label"), messageKeyPrefix)

      behave like pageWithBackLink(applyView(form))

      behave like stringPage(
        form,
        applyView,
        Some("taskList.trustDetails.label"),
        messageKeyPrefix
      )

      behave like pageWithASubmitButton(applyView(form))
    }

  }
}
