package uzcript

import uzcript.configuration.Configuration.MongoConfig
import zio.Has

package object configuration {
  type Configuration = Has[MongoConfig]
}
