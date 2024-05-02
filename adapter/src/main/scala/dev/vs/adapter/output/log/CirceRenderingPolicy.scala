package dev.vs.adapter.output.log

import dev.vs.adapter.input.config.AppInfo
import io.circe._
import io.circe.syntax._
import izumi.fundamentals.platform.time.IzTime._
import izumi.logstage.api.Log
import izumi.logstage.api.Log.LogArg
import izumi.logstage.api.rendering.LogstageCodec
import izumi.logstage.api.rendering.RenderedMessage
import izumi.logstage.api.rendering.RenderedParameter
import izumi.logstage.api.rendering.RenderingOptions
import izumi.logstage.api.rendering.RenderingPolicy
import izumi.logstage.api.rendering.json.LogstageCirceWriter
import izumi.logstage.api.rendering.logunits.LogFormat

import java.time.format.DateTimeFormatter
import scala.collection.mutable

class CirceRenderingPolicy(
  prettyPrint: Boolean,
  info: AppInfo
) extends RenderingPolicy {

  import CirceRenderingPolicy._

  protected def EventKey = "event"
  protected def ContextKey = "context"
  protected def MetaKey = "meta"
  protected def MessageKey = "message"

  override def render(entry: Log.Entry): String = {
    val result = mutable.ArrayBuffer[(String, Json)]()

    val formatted = Format.formatMessage(
      entry,
      RenderingOptions(withExceptions = false, colored = false, hideKeys = false)
    )

    val timestamp = entry.context.dynamic.tsMillis.asEpochMillisUtcZoned
      .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    val fields = Seq(
      "@timestamp" -> Json.fromString(timestamp),
      "level" -> Json.fromString(entry.context.dynamic.level.toString.toLowerCase)
    )

    fields.foreach(result += _)

    info.toMap.foreach { case (key, value) => result += key -> Json.fromString(value) }

    val params = parametersToJson[RenderedParameter](
      formatted.parameters ++ formatted.unbalanced,
      _.normalizedName,
      repr
    )

    if (params.nonEmpty) {
      result += EventKey -> params.asJson
    }

    val ctx = parametersToJson[LogArg](
      entry.context.customContext.values,
      _.name,
      v => repr(Format.formatArg(v, withColors = false))
    )

    if (ctx.nonEmpty) {
      result += ContextKey -> ctx.asJson
    }

    result ++= makeEventEnvelope(entry, formatted)

    val json = Json.fromFields(result)

    dump(json)
  }

  protected def dump(json: Json): String = {
    if (prettyPrint) {
      json.printWith(Printer.spaces2)
    } else {
      json.noSpaces
    }
  }

  protected def makeEventEnvelope(
    entry: Log.Entry,
    formatted: RenderedMessage
  ): Seq[(String, Json)] = {
    val eventInfo = Json.fromFields(
      Seq(
        "logger" -> Json.fromString(entry.context.static.id.id),
        "line" -> Json.fromInt(entry.context.static.position.line),
        "file" -> Json.fromString(entry.context.static.position.file),
        "thread" -> Json.fromFields(
          Seq(
            "id" -> Json.fromLong(entry.context.dynamic.threadData.threadId),
            "name" -> Json.fromString(entry.context.dynamic.threadData.threadName)
          )
        )
      )
    )

    val tail = Seq(
      MetaKey -> eventInfo,
      MessageKey -> Json.fromString(formatted.message)
    )

    tail
  }

  protected def parametersToJson[T](
    params: Seq[T],
    name: T => String,
    repr: T => Json
  ): Map[String, Json] = {
    val paramGroups = params.groupBy(name)
    val (unary, multiple) = paramGroups.partition(_._2.size == 1)
    val paramsMap = unary.map { kv =>
      kv._1 -> repr(kv._2.head)
    }
    val multiparamsMap = multiple.map { kv =>
      kv._1 -> Json.arr(kv._2.map(repr)*)
    }
    paramsMap ++ multiparamsMap
  }

  protected def repr(parameter: RenderedParameter): Json = {
    val mapStruct: PartialFunction[Any, Json] = {
      case a: Iterable[?] =>
        val params = a.map { v =>
          mapListElement.apply(v)
        }.toList
        Json.arr(params*)
      case _ =>
        Json.fromString(parameter.repr)
    }

    val mapParameter = mapScalar orElse mapStruct

    parameter.arg.codec match {
      case Some(codec) =>
        LogstageCirceWriter.write(codec.asInstanceOf[LogstageCodec[Any]], parameter.value)

      case None =>
        mapParameter(parameter.value)
    }
  }

  private val mapScalar: PartialFunction[Any, Json] = {
    case null =>
      Json.Null
    case a: Json =>
      a
    case a: Double =>
      Json.fromDoubleOrNull(a)
    case a: BigDecimal =>
      Json.fromBigDecimal(a)
    case a: Int =>
      Json.fromInt(a)
    case a: BigInt =>
      Json.fromBigInt(a)
    case a: Boolean =>
      Json.fromBoolean(a)
    case a: Long =>
      Json.fromLong(a)
    case a: Throwable =>
      LogstageCirceWriter.write(LogstageCodec.LogstageCodecThrowable, a)
  }

  private val mapToString: PartialFunction[Any, Json] = { case o =>
    Json.fromString(o.toString)
  }

  private val mapListElement: PartialFunction[Any, Json] = {
    mapScalar orElse mapToString
  }
}

object CirceRenderingPolicy:

  @inline def apply(
    prettyPrint: Boolean,
    info: AppInfo
  ): CirceRenderingPolicy = new CirceRenderingPolicy(prettyPrint, info)

  object Format extends LogFormat.LogFormatImpl {
    override protected def toString(argValue: Any): String = {
      argValue match {
        case j: Json =>
          j.noSpaces
        case o => o.toString
      }
    }
  }
