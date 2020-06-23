package uzcript.symbol

import java.math.{BigDecimal, BigInteger}

import io.nem.symbol.sdk.api._
import io.nem.symbol.sdk.infrastructure.vertx.RepositoryFactoryVertxImpl
import io.nem.symbol.sdk.model.account.{Account, Address, UnresolvedAddress}
import io.nem.symbol.sdk.model.blockchain.{BlockDuration, BlockInfo}
import io.nem.symbol.sdk.model.message.{Message, PlainMessage}
import io.nem.symbol.sdk.model.mosaic._
import io.nem.symbol.sdk.model.namespace._
import io.nem.symbol.sdk.model.network.{NetworkType, RentalFees}
import io.nem.symbol.sdk.model.transaction._
import zio.{Task, UIO, ZIO}

import scala.jdk.CollectionConverters._
import uzcript.commons.Transformers._
import uzcript.commons.Constants._
import uzcript.shared.MosaicInformation
import uzcript.shared.UzcriptMessages.UzcriptSuccessResponse
object SymbolNem {
  def buildRepositoryFactory(
    endPoint: String
  ): Task[RepositoryFactoryVertxImpl] =
    ZIO.effect(new RepositoryFactoryVertxImpl(endPoint))
  def getBlockGenesis(blockRepository: BlockRepository): Task[BlockInfo] =
    blockRepository.getBlockByHeight(BigInteger.valueOf(1)).toFuture.toTask

  def getGenerationHash(blockInfo: BlockInfo): String =
    blockInfo.getGenerationHash

  def buildMosaicDefinitionTransaction(
    account: Account,
    blockDuration: BlockDuration,
    isSupplyMutable: Boolean,
    isTransferable: Boolean,
    isRestrictable: Boolean,
    divisibility: Int,
    networkType: NetworkType
  ): MosaicDefinitionTransaction = {
    val mosaicNonce = MosaicNonce.createRandom()
    MosaicDefinitionTransactionFactory
      .create(
        networkType,
        mosaicNonce,
        MosaicId.createFromNonce(mosaicNonce, account.getPublicAccount),
        MosaicFlags.create(isSupplyMutable, isTransferable, isRestrictable),
        divisibility,
        blockDuration
      )
      .build()
  }

  def buildMosaicSupplyChangeTransaction(
    mosaicDefinitionTransaction: MosaicDefinitionTransaction,
    delta: Int,
    divisibility: Int,
    networkType: NetworkType
  ): MosaicSupplyChangeTransaction =
    MosaicSupplyChangeTransactionFactory
      .create(
        networkType,
        mosaicDefinitionTransaction.getMosaicId,
        MosaicSupplyChangeActionType.INCREASE,
        BigDecimal.valueOf(delta * Math.pow(10, divisibility)).toBigInteger
      )
      .build()

  def modifyMosaicSupply(
    mosaicId: MosaicId,
    divisibility: Int,
    delta: Int,
    supplyChangeActionType: MosaicSupplyChangeActionType,
    networkType: NetworkType
  ): MosaicSupplyChangeTransaction =
    MosaicSupplyChangeTransactionFactory
      .create(
        networkType,
        mosaicId,
        supplyChangeActionType,
        BigDecimal.valueOf(delta * Math.pow(10, divisibility)).toBigInteger,
      )
      .build()

  def aggregateTransaction(transactions: List[Transaction],
                           feeAmount: BigInteger,
                           networkType: NetworkType): AggregateTransaction =
    AggregateTransactionFactory
      .createComplete(networkType, transactions.asJava)
      .maxFee(feeAmount)
      .build()

  def signTransaction(account: Account,
                      transaction: Transaction,
                      generationHash: String): SignedTransaction =
    account.sign(transaction, generationHash)

  def announceTransaction(
    transactionRepository: TransactionRepository,
    signedTransaction: SignedTransaction
  ): Task[TransactionAnnounceResponse] =
    transactionRepository.announce(signedTransaction).toFuture.toTask

  def getNamespaceInfo(
    namespaceId: NamespaceId,
    namespaceRepository: NamespaceRepository
  ): Task[NamespaceInfo] =
    namespaceRepository
      .getNamespace(namespaceId)
      .toFuture
      .toTask

  def getNamespaceNameFromMosaicId(
    mosaicId: MosaicId,
    namespaceRepository: NamespaceRepository
  ): ZIO[Any, Throwable, Option[NamespaceName]] =
    for {
      mosaicNames <- namespaceRepository
        .getMosaicsNames(List(mosaicId).asJava)
        .toTask
      namespaceName = mosaicNames.asScala.headOption
        .flatMap(_.getNames.asScala.headOption)
    } yield namespaceName

  def getAliasTypeFromNamespace(alias: Alias[_]): (String, String) =
    alias.getType match {
      case AliasType.ADDRESS =>
        ("address", alias.getAliasValue.asInstanceOf[Address].pretty())
      case AliasType.MOSAIC =>
        ("mosaic", alias.getAliasValue.asInstanceOf[MosaicId].getIdAsHex)
      case AliasType.NONE => ("none", "-")
    }

  def getNamespaceNameFromAccount(
    address: Address,
    namespaceRepository: NamespaceRepository
  ): Task[List[String]] =
    for {
      accountNames <- namespaceRepository
        .getAccountsNames(List(address).asJava)
        .toTask
      namespaceNames = accountNames.asScala
        .filter(_.getAddress == address)
        .flatMap(_.getNames.asScala.map(_.getName))
        .toList
    } yield namespaceNames

  def buildNamespaceRegistrationTransaction(
    networkType: NetworkType,
    namespaceName: String,
    duration: BigInteger
  ): NamespaceRegistrationTransaction =
    NamespaceRegistrationTransactionFactory
      .createRootNamespace(networkType, namespaceName, duration)
      .build()

  def buildMosaicAliasTransaction(
    networkType: NetworkType,
    namespaceId: NamespaceId,
    mosaicId: MosaicId,
    aliasAction: AliasAction
  ): MosaicAliasTransaction =
    MosaicAliasTransactionFactory
      .create(networkType, aliasAction, namespaceId, mosaicId)
      .build()

  def registerSubNamespace(
    subNamespaceName: String,
    parentId: NamespaceId,
    fee: BigInteger
  )(implicit networkType: NetworkType): NamespaceRegistrationTransaction =
    NamespaceRegistrationTransactionFactory
      .createSubNamespace(networkType, subNamespaceName, parentId)
      .maxFee(fee)
      .build()

  def buildMosaicToSend(networkCurrencyMosaicId: MosaicId,
                        networkCurrencyDivisibility: Int,
                        amount: BigInteger): Mosaic =
    new Mosaic(
      networkCurrencyMosaicId,
      amount.multiply(BigInteger.valueOf(10).pow(networkCurrencyDivisibility))
    )

  def createPlainMessage(payload: String): PlainMessage =
    PlainMessage.create(payload)

  def calculateAbsoluteAmount(amount: BigInteger,
                              divisibility: Int): BigInteger =
    amount.multiply(BigInteger.valueOf(10).pow(divisibility))

  def buildTransferTransaction(recipientAddress: UnresolvedAddress,
                               mosaics: List[Mosaic],
                               message: Message,
                               networkType: NetworkType): TransferTransaction =
    TransferTransactionFactory
      .create(networkType, recipientAddress, mosaics.asJava, message)
      .build()

  def getMosaicInfoWithNamespace(
    repositoryFactory: RepositoryFactoryVertxImpl,
    address: Address,
    mosaicId: MosaicId
  ): Task[MosaicInformation] = {
    val accountRepository = repositoryFactory.createAccountRepository()
    val mosaicRepository = repositoryFactory.createMosaicRepository()
    val namespaceRepository = repositoryFactory.createNamespaceRepository()
    for {
      namespaceName <- getNamespaceNameFromMosaicId(
        mosaicId,
        namespaceRepository
      )
      mosaicInfo <- mosaicRepository.getMosaic(mosaicId).toTask
      account <- accountRepository.getAccountInfo(address).toTask
      mosaic = account.getMosaics.asScala
        .find(_.getIdAsHex == mosaicId.getIdAsHex)
    } yield
      MosaicInformation(
        mosaicInfo.getMosaicId.getIdAsHex,
        namespaceName.map(_.getName),
        mosaicInfo.getSupply.toString,
        mosaic.map(_.getAmount),
        mosaicInfo.getDivisibility,
        mosaicInfo.isTransferable,
        mosaicInfo.isSupplyMutable,
        mosaicInfo.isTransferable
      )
  }

  def getMosaicCreationRentalFee(
    networkRepository: NetworkRepository
  ): Task[BigInteger] =
    networkRepository.getRentalFees.toTask.map(_.getEffectiveMosaicRentalFee)

  def getRootNameSpaceCreationRentalFee(
    networkRepository: NetworkRepository
  ): Task[BigInteger] =
    networkRepository.getRentalFees.toTask
      .map(_.getEffectiveRootNamespaceRentalFeePerBlock)

  def getChildNameSpaceCreationRentalFee(
    networkRepository: NetworkRepository
  ): Task[BigInteger] =
    networkRepository.getRentalFees.toTask
      .map(_.getEffectiveChildNamespaceRentalFee)

  // todo: implement a solution to get from encrypted cache
  def getPrivateKey(address: Address, password: String): UIO[String] =
    Task.succeed(
      "8625BE64A548F1E361D1D5E6A0613D327A72823BFBA90D8A1BBBFA82B22D8567"
    )

  def sendMosaic(from: Address,
                 recipientAddress: Address,
                 mosaicId: MosaicId,
                 amount: BigInteger,
                 message: String,
                 generationHash: String,
                 transactionRepository: TransactionRepository,
                 mosaicRepository: MosaicRepository,
                 networkRepository: NetworkRepository,
                 networkType: NetworkType): Task[UzcriptSuccessResponse] = {
    for {
      privateKey <- getPrivateKey(from, "@1234")
      account = Account.createFromPrivateKey(privateKey, networkType)
      mosaicInfo <- mosaicRepository.getMosaic(mosaicId).toTask
      feeAmount <- getMosaicCreationRentalFee(networkRepository)
      mosaic = new Mosaic(
        mosaicId,
        calculateAbsoluteAmount(amount, mosaicInfo.getDivisibility)
      )
      transferTransaction = SymbolNem.buildTransferTransaction(
        recipientAddress,
        List(mosaic),
        PlainMessage.create(message),
        networkType
      )
      transactions = List(
        transferTransaction.toAggregate(account.getPublicAccount)
      )
      aggregateTransaction = SymbolNem.aggregateTransaction(
        transactions,
        feeAmount,
        networkType
      )
      signedTransaction = SymbolNem.signTransaction(
        account,
        aggregateTransaction,
        generationHash
      )
      announcedTransaction <- announceTransaction(
        transactionRepository,
        signedTransaction
      )
    } yield
      UzcriptSuccessResponse(responseMessage = announcedTransaction.getMessage)
  }
}
