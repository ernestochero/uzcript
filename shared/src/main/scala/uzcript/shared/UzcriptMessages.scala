package uzcript.shared

object UzcriptManagementException {
  case class UzcriptAPIException(message: String) extends Exception
}
object UzcriptMessages {
  sealed trait UzcriptMessage {
    def message: String
  }
  sealed trait UzcriptResponse extends UzcriptMessage {
    val responseCode: String
    val responseMessage: String
    override def message: String =
      s"ResponseCode : $responseCode with message : $responseMessage"
  }
  case class UzcriptSuccessResponse(responseCode: String = "00",
                                    responseMessage: String)
      extends UzcriptResponse

  case class UzcriptFailedResponse(responseCode: String = "01",
                                   responseMessage: String)
      extends UzcriptResponse
}
