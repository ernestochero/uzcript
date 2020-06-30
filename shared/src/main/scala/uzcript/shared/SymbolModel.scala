package uzcript.shared

import play.api.libs.json._
import java.math.BigInteger

final case class MosaicInformationFromAddress(IdAsHex: String,
                                              amount: BigInteger)
object MosaicInformationFromAddress {
  implicit val mosaicInformationFromAddressWrites =
    Json.writes[MosaicInformationFromAddress]
  implicit val mosaicInformationFromAddressReads =
    Json.reads[MosaicInformationFromAddress]
}

final case class MosaicInformation(mosaicId: String,
                                   nameSpaceName: Option[String] = None,
                                   supply: String,
                                   balance: Option[BigInteger],
                                   divisibility: Int,
                                   transferable: Boolean,
                                   mutable: Boolean,
                                   restrictable: Boolean)

object MosaicInformation {
  implicit val mosaicInformationWrites = Json.writes[MosaicInformation]
  implicit val mosaicInformationReads = Json.reads[MosaicInformation]
}

final case class AccountInformation(address: String,
                                    importances: List[BigInteger],
                                    publicKey: String,
                                    aliases: List[String],
                                    mosaics: List[MosaicInformationFromAddress])

object AccountInformation {
  implicit val accountInformationWrites = Json.writes[AccountInformation]
  implicit val accountInformationReads = Json.reads[AccountInformation]
}

final case class NamespaceInformation(namespaceName: String,
                                      hexadecimal: String,
                                      startHeight: String,
                                      endHeight: String,
                                      expired: Boolean,
                                      aliasType: String,
                                      alias: String)

object NamespaceInformation {
  implicit val namespaceInformationWrites = Json.writes[NamespaceInformation]
  implicit val namespaceInformationReads = Json.reads[NamespaceInformation]
}

sealed trait AliasActionType
object AliasActionType {
  case object LINK extends AliasActionType
  case object UNLINK extends AliasActionType
}

sealed trait SupplyActionType
object SupplyActionType {
  case object INCREASE extends SupplyActionType
  case object DECREASE extends SupplyActionType
}
