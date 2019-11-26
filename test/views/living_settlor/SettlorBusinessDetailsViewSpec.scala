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

package views.living_settlor

import forms.living_settlor.SettlorBusinessDetailsFormProvider
import models.NormalMode
import models.registration.pages.SettlorBusinessDetails
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.living_settlor.SettlorBusinessDetailsView

class SettlorBusinessDetailsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "settlorBusinessDetails"

  val form = new SettlorBusinessDetailsFormProvider()()

  val index = 0

  val fakeName = "Test User"

  val view = viewFor[SettlorBusinessDetailsView](Some(emptyUserAnswers))

  def applyView(form: Form[_]): HtmlFormat.Appendable =
    view.apply(form, NormalMode,index, fakeDraftId, fakeName)(fakeRequest, messages)

  "SettlorBusinessDetailsView" must {

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, fakeName)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithASubmitButton(applyView(form))
  }


  "SettlorBusinessDetailsView" when {

    "rendered" must {

      "contain radio buttons for the value" in {

        val doc = asDocument(applyView(form))

        for (option <- SettlorBusinessDetails.options) {
          assertContainsRadioButton(doc, option.id, "value", option.value, false)
        }
      }
    }

    for (option <- SettlorBusinessDetails.options) {

      s"rendered with a value of '${option.value}'" must {

        s"have the '${option.value}' radio button selected" in {

          val doc = asDocument(applyView(form.bind(Map("value" -> s"${option.value}"))))

          assertContainsRadioButton(doc, option.id, "value", option.value, true)

          for (unselectedOption <- SettlorBusinessDetails.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, unselectedOption.id, "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
