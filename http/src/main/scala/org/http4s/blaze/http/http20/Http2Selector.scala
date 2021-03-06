package org.http4s.blaze.http.http20

import java.nio.ByteBuffer
import javax.net.ssl.SSLEngine

import org.http4s.blaze.http._
import org.http4s.blaze.http.http20.NodeMsg.Http2Msg
import org.http4s.blaze.pipeline.{LeafBuilder, TailStage}
import org.log4s.getLogger

import scala.concurrent.duration.Duration

object Http2Selector {

  private val logger = getLogger

  private val HTTP_1_1 = "http/1.1"
  private val H2       = "h2"
  private val H2_14    = "h2-14"

  def apply(engine: SSLEngine, 
           service: HttpService,
            config: HttpServerStageConfig): ALPNSelector = {

    def builder(s: String): LeafBuilder[ByteBuffer] = s match {
      case H2 | H2_14 => LeafBuilder(http2Stage(service, config))
      case _          => LeafBuilder(http1xStage(service, config))
    }

    def selector(protocols: Seq[String]): String = {
      logger.debug(s"Available protocols: $protocols")
      protocols.find {
        case H2    => true
        case H2_14 => true
        case _     => false
      } getOrElse(HTTP_1_1)
    }
    
    new ALPNSelector(engine, selector, builder)
  }

  private def http1xStage(service: HttpService, config: HttpServerStageConfig): TailStage[ByteBuffer] =
    new Http1ServerStage(service, config)

  private def http2Stage(service: HttpService, config: HttpServerStageConfig): TailStage[ByteBuffer] = {

    def newNode(streamId: Int): LeafBuilder[Http2Msg] = {
      LeafBuilder(new Http2ServerStage(streamId, service, config))
    }

    Http2Stage(
      nodeBuilder = newNode,
      timeout = Duration.Inf,
      ec = config.serviceExecutor,
      maxHeadersLength = config.maxNonBodyBytes
    )
  }
}
