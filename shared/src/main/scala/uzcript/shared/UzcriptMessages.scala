package uzcript.shared

import play.api.libs.json._

object UzcriptManagementException {
  case class UzcriptAPIException(message: String) extends Exception
}

object UzcriptMessages {
  sealed trait UzcriptResponse {
    val responseCode: String
    val responseMessage: String
    def message: String =
      s"ResponseCode : $responseCode with message : $responseMessage"
  }
  case class UzcriptSuccessResponse(responseCode: String = "00",
                                    responseMessage: String)
      extends UzcriptResponse

  case class UzcriptFailedResponse(responseCode: String = "01",
                                   responseMessage: String)
      extends UzcriptResponse

  case class UzcriptSuccessGenericResponse[T](responseCode: String = "02",
                                              responseMessage: String,
                                              model: T)
      extends UzcriptResponse
}

object UzcriptMessagesImplicit {
  import UzcriptMessages._
  implicit def uzcriptSuccessGenericResponseWrites[T: Writes] =
    Json.writes[UzcriptSuccessGenericResponse[T]]
  implicit def uzcriptSuccessGenericResponseReads[T: Reads] =
    Json.reads[UzcriptSuccessGenericResponse[T]]
  implicit def uzcriptSuccessResponseWrites =
    Json.writes[UzcriptSuccessResponse]
  implicit def uzcriptSuccessResponseReads = Json.reads[UzcriptSuccessResponse]
  implicit def uzcriptFailedResponseWrites = Json.writes[UzcriptFailedResponse]
  implicit def uzcriptFailedResponseReads = Json.reads[UzcriptFailedResponse]
  val uzcriptResponseFormat = Json.format[UzcriptResponse]
}
