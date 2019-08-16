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

package views.shares

import views.behaviours.ViewBehaviours
import views.html.shares.ShareAnswersView

class ShareAnswerPageViewSpec extends ViewBehaviours {

  val index = 0

  "ShareAnswerPageView view" must {

    val view = viewFor[ShareAnswersView](Some(emptyUserAnswers))

    val applyView = view.apply(index, fakeDraftId, Nil)(fakeRequest, messages)

    behave like normalPage(applyView, "shareAnswers")

    behave like pageWithBackLink(applyView)
  }
}
