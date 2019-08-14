package forms

import java.time.{LocalDate, ZoneOffset}

import forms.behaviours.DateBehaviours
import play.api.data.FormError

class SettlorIndividualDateOfBirthFormProviderSpec extends DateBehaviours {

  val form = new SettlorIndividualDateOfBirthFormProvider()()

  ".value" should {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "settlorIndividualDateOfBirth.error.required.all")
  }
}
