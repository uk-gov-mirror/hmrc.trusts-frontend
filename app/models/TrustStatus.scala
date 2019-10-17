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

import play.api.libs.json._


sealed trait TrustStatus

case object Processing extends TrustStatus

case object Closed extends TrustStatus

object TrustStatus {

  implicit object TrustStatusReads extends Reads[TrustStatus] {
    override def reads(json:JsValue): JsResult[TrustStatus] = json("responseHeader")("status") match {
      case JsString("In Processing") => JsSuccess(Processing)
      case JsString("Closed") => JsSuccess(Closed)
    }
  }

}
