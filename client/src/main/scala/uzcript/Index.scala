package uzcript

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.annotation.JSExportTopLevel
@JSExportTopLevel("Index")
object Index {
  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = dom.document.createElement("p")
    parNode.textContent = text
    targetNode.appendChild(parNode)
    ()
  }

  @JSExportTopLevel("addClickedMessage")
  def addClickedMessage(): Unit = {
    val contentNode = dom.document.getElementById("content")
    appendPar(contentNode, "You clicked the button!")
  }

  @JSExportTopLevel("getBlockGenesisResponse")
  def getBlockGenesisResponse(): Unit = {
    (for {
      response <- Ajax.get("/genesis")
      _ = println(response.responseText)
      text = response.responseText
      node = dom.document.getElementById("genesis-information")
      _ = appendPar(node, text)
    } yield ()).onComplete(_ => ())
  }

  def setupUI(): Unit = ()

  def main(args: Array[String]): Unit = ()
}
