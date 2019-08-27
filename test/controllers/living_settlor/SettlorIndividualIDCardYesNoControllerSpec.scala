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

package controllers.living_settlor

import base.SpecBase
import controllers.IndexValidation
import models.{FullName, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import pages.living_settlor.{SettlorIndividualDateOfBirthYesNoPage, SettlorIndividualIDCardYesNoPage, SettlorIndividualNINOYesNoPage, SettlorIndividualNamePage}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import forms.living_settlor.SettlorIndividualIDCardYesNoFormProvider
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.living_settlor.SettlorIndividualIDCardYesNoView

class SettlorIndividualIDCardYesNoControllerSpec extends SpecBase with IndexValidation {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new SettlorIndividualIDCardYesNoFormProvider()
  val form = formProvider()
  val index = 0
  val name = FullName("First", Some("Middle"), "Last")

  lazy val settlorIndividualIDCardYesNoRoute = routes.SettlorIndividualIDCardYesNoController.onPageLoad(NormalMode, index, fakeDraftId).url

  "SettlorIndividualIDCardYesNo Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(SettlorIndividualNamePage(index), name).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, settlorIndividualIDCardYesNoRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[SettlorIndividualIDCardYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, fakeDraftId, index, name)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(SettlorIndividualNamePage(index), name).success.value
        .set(SettlorIndividualIDCardYesNoPage(index), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, settlorIndividualIDCardYesNoRoute)

      val view = application.injector.instanceOf[SettlorIndividualIDCardYesNoView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), NormalMode, fakeDraftId, index, name)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val userAnswers = emptyUserAnswers.set(SettlorIndividualNamePage(index), name).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request =
        FakeRequest(POST, settlorIndividualIDCardYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.set(SettlorIndividualNamePage(index), name).success.value
        .set(SettlorIndividualIDCardYesNoPage(index), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request =
        FakeRequest(POST, settlorIndividualIDCardYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[SettlorIndividualIDCardYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, fakeDraftId, index, name)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Settlors Name page when Settlors name is not answered" in {

      val userAnswers = emptyUserAnswers
        .set(SettlorIndividualDateOfBirthYesNoPage(index), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, settlorIndividualIDCardYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SettlorIndividualNameController.onPageLoad(NormalMode, index, fakeDraftId).url

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, settlorIndividualIDCardYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, settlorIndividualIDCardYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "for a GET" must {

      def getForIndex(index: Int): FakeRequest[AnyContentAsEmpty.type] = {
        val route = routes.SettlorIndividualIDCardYesNoController.onPageLoad(NormalMode, index, fakeDraftId).url

        FakeRequest(GET, route)
      }

      validateIndex(
        arbitrary[Boolean],
        SettlorIndividualIDCardYesNoPage.apply,
        getForIndex
      )

    }

    "for a POST" must {
      def postForIndex(index: Int): FakeRequest[AnyContentAsFormUrlEncoded] = {

        val route =
          routes.SettlorIndividualIDCardYesNoController.onPageLoad(NormalMode, index, fakeDraftId).url

        FakeRequest(POST, route)
          .withFormUrlEncodedBody("Value" -> "true")
      }

      validateIndex(
        arbitrary[Boolean],
        SettlorIndividualIDCardYesNoPage.apply,
        postForIndex
      )
    }
  }
}
