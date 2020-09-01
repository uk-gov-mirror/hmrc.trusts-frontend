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

package views.register.settlors.living_settlor

import forms.PassportOrIdCardFormProvider
import models.NormalMode
import models.core.pages.FullName
import models.registration.pages.PassportOrIdCardDetails
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.InputOption
import utils.countryOptions.CountryOptionsNonUK
import views.behaviours.QuestionViewBehaviours
import views.html.register.settlors.living_settlor.SettlorIndividualPassportView

class SettlorIndividualPassportViewSpec extends QuestionViewBehaviours[PassportOrIdCardDetails] {

  val messageKeyPrefix = "settlorIndividualPassport"
  val index = 0
  val name = FullName("First", Some("Middle"), "Last")

  override val form = new PassportOrIdCardFormProvider(frontendAppConfig)("settlorIndividualPassport")

  "SettlorIndividualPassportView" must {

    val view = viewFor[SettlorIndividualPassportView](Some(emptyUserAnswers))

    val countryOptions: Seq[InputOption] = app.injector.instanceOf[CountryOptionsNonUK].options

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, countryOptions, NormalMode, fakeDraftId, index, name)(fakeRequest, messages)

    val applyViewF = (form : Form[_]) => applyView(form)

    behave like dynamicTitlePage(applyView(form), Some("taskList.settlors.label"), messageKeyPrefix, name.toString)

    behave like pageWithBackLink(applyView(form))

    "date fields" must {

      behave like pageWithDateFields(form, applyViewF,
        Some("taskList.settlors.label"),
        messageKeyPrefix,
        "expiryDate",
        name.toString
      )
    }

    "text fields" must {

      behave like pageWithTextFields(
        form,
        applyView,
        Some("taskList.settlors.label"),
        messageKeyPrefix,
        Seq(("number", None)),
        name.toString
      )
    }

    behave like pageWithASubmitButton(applyView(form))
  }
}
