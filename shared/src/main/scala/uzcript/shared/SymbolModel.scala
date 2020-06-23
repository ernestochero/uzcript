package uzcript.shared

import java.math.BigInteger

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import cats.Show
final case class MosaicInformation(mosaicId: String,
                                   nameSpaceName: Option[String] = None,
                                   supply: String,
                                   balance: Option[BigInteger],
                                   divisibility: Int,
                                   transferable: Boolean,
                                   mutable: Boolean,
                                   restrictable: Boolean)

object MosaicInformation {
  implicit val mosaicInformationEnc: Encoder[MosaicInformation] =
    deriveEncoder[MosaicInformation]
  implicit val mosaicInformationDec: Decoder[MosaicInformation] =
    deriveDecoder[MosaicInformation]
  implicit val mosaicInformationShow: Show[MosaicInformation] =
    Show.fromToString[MosaicInformation]
}

final case class AccountInformation(address: String,
                                    importances: List[BigInteger],
                                    publicKey: String,
                                    aliases: List[String],
                                    mosaics: List[MosaicInformationFromAddress])

object AccountInformation {
  implicit val accountInformationEnc: Encoder[AccountInformation] =
    deriveEncoder[AccountInformation]
  implicit val accountInformationDec: Decoder[AccountInformation] =
    deriveDecoder[AccountInformation]
  implicit val accountInformationShow: Show[AccountInformation] =
    Show.fromToString[AccountInformation]
}

final case class NamespaceInformation(namespaceName: String,
                                      hexadecimal: String,
                                      startHeight: String,
                                      endHeight: String,
                                      expired: Boolean,
                                      aliasType: String,
                                      alias: String)

object NamespaceInformation {
  implicit val namespaceInformationEnc: Encoder[NamespaceInformation] =
    deriveEncoder[NamespaceInformation]
  implicit val namespaceInformationDec: Decoder[NamespaceInformation] =
    deriveDecoder[NamespaceInformation]
  implicit val namespaceInformationShow: Show[NamespaceInformation] =
    Show.fromToString[NamespaceInformation]
}

final case class MosaicInformationFromAddress(IdAsHex: String,
                                              amount: BigInteger)
object MosaicInformationFromAddress {
  implicit val mosaicInformationFromAddressEnc
    : Encoder[MosaicInformationFromAddress] =
    deriveEncoder[MosaicInformationFromAddress]
  implicit val mosaicInformationFromAddressDec
    : Decoder[MosaicInformationFromAddress] =
    deriveDecoder[MosaicInformationFromAddress]
  implicit val mosaicInformationFromAddressShow
    : Show[MosaicInformationFromAddress] =
    Show.fromToString[MosaicInformationFromAddress]
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
