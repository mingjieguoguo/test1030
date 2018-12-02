package com.twitter.finatra.json.internal.caseclass.jackson

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.TreeTraversingParser
import com.fasterxml.jackson.databind.util.ClassUtil
import com.twitter.finatra.json.internal.caseclass.exceptions.CaseClassValidationException.PropertyPath
import com.twitter.finatra.json.internal.caseclass.exceptions.{
  CaseClassValidationException,
  FinatraJsonMappingException
}
import com.twitter.finatra.json.internal.caseclass.reflection.CaseClassSigParser
import com.twitter.finatra.json.internal.caseclass.reflection.DefaultMethodUtils.defaultFunction
import com.twitter.finatra.json.internal.caseclass.utils.AnnotationUtils._
import com.twitter.finatra.json.internal.caseclass.utils.FieldInjection
import com.twitter.finatra.request.{FormParam, Header, QueryParam}
import com.twitter.finatra.validation.ValidationResult._
import com.twitter.finatra.validation.{ErrorCode, Validation}
import com.twitter.inject.Logging
import com.twitter.inject.conversions.string._
import java.lang.annotation.Annotation
import scala.annotation.tailrec
import scala.language.existentials
import scala.reflect.NameTransformer

private[finatra] object CaseClassField {

  def createFields(
    clazz: Class[_],
    namingStrategy: PropertyNamingStrategy,
    typeFactory: TypeFactory
  ): Seq[CaseClassField] = {
    val allAnnotations = constructorAnnotations(clazz)
    val constructorParams = CaseClassSigParser.parseConstructorParams(clazz)
    assert(
      allAnnotations.size == constructorParams.size,
      "Non-static inner 'case classes' not supported"
    )

    val companionObject = Class.forName(clazz.getName + "$").getField("MODULE$").get(null)
    val companionObjectClass = companionObject.getClass

    for {
      (constructorParam, idx) <- constructorParams.zipWithIndex
      annotations = allAnnotations(idx)
      name = jsonNameForField(annotations, namingStrategy, constructorParam.name)
      deserializer = deserializerOrNone(annotations)
    } yield {
      CaseClassField(
        name = name,
        javaType = JacksonTypes.javaType(typeFactory, constructorParam.scalaType),
        parentClass = clazz,
        defaultFuncOpt = defaultFunction(companionObjectClass, companionObject, idx),
        annotations = annotations,
        deserializer = deserializer
      )
    }
  }

  private[finatra] def constructorAnnotations(clazz: Class[_]): Seq[Array[Annotation]] = {
    clazz.getConstructors.head.getParameterAnnotations.toSeq
  }

  private def jsonNameForField(
    annotations: Seq[Annotation],
    namingStrategy: PropertyNamingStrategy,
    name: String
  ): String = {
    findAnnotation[JsonProperty](annotations) match {
      case Some(jsonProperty) if jsonProperty.value.nonEmpty => jsonProperty.value
      case _ =>
        val decodedName = NameTransformer.decode(name) //decode unicode escaped field names
        namingStrategy.nameForField( //apply json naming strategy (e.g. snake_case)
          /* config = */ null,
          /* field = */ null,
          /* defaultName = */ decodedName
        )
    }
  }

  private def deserializerOrNone(
    annotations: Array[Annotation]
  ): Option[JsonDeserializer[Object]] = {
    for {
      jsonDeserializer <- findAnnotation[JsonDeserialize](annotations)
      if jsonDeserializer.using != classOf[JsonDeserializer.None]
    } yield
      ClassUtil.createInstance(jsonDeserializer.using, false).asInstanceOf[JsonDeserializer[Object]]
  }
}

private[finatra] case class CaseClassField(
  name: String,
  javaType: JavaType,
  parentClass: Class[_],
  defaultFuncOpt: Option[() => Object],
  annotations: Seq[Annotation],
  deserializer: Option[JsonDeserializer[Object]]
) extends Logging {

  private val isOption = javaType.getRawClass == classOf[Option[_]]
  private val isString = javaType.getRawClass == classOf[String]
  private val AttributeInfo(attributeType, attributeName) = findAttributeInfo(name, annotations)
  private val fieldInjection = new FieldInjection(name, javaType, parentClass, annotations)
  private lazy val firstTypeParam = javaType.containedType(0)
  private lazy val requiredFieldException = CaseClassValidationException(
    PropertyPath.leaf(attributeName),
    Invalid(s"$attributeType is required", ErrorCode.RequiredFieldMissing)
  )

  /* Public */

  lazy val missingValue: AnyRef = {
    if (javaType.isPrimitive)
      ClassUtil.defaultValue(javaType.getRawClass)
    else
      null
  }

  val validationAnnotations: Seq[Annotation] =
    filterIfAnnotationPresent[Validation](annotations)

  /**
   * Parse the field from a JsonNode representing a JSON object
   * NOTE: I'd normally return a Try[Object], but instead I'm using exceptions to optimize the non-failure case
   * NOTE: Option fields default to None even if no default is specified
   *
   * @param context DeserializationContext for deserialization
   * @param codec Codec for field
   * @param objectJsonNode The JSON object
   * @return The parsed object for this field
   * @throws CaseClassValidationException with reason for the parsing error
   */
  def parse(
    context: DeserializationContext,
    codec: ObjectCodec,
    objectJsonNode: JsonNode
  ): Object = {
    if (fieldInjection.isInjectable)
      fieldInjection
        .inject(context, codec).orElse(defaultValue).getOrElse(throwRequiredFieldException())
    else {
      val fieldJsonNode = objectJsonNode.get(name)
      if (fieldJsonNode != null && !fieldJsonNode.isNull)
        if (isOption)
          Option(parseFieldValue(codec, fieldJsonNode, firstTypeParam, context))
        else
          assertNotNull(fieldJsonNode, parseFieldValue(codec, fieldJsonNode, javaType, context))
      else if (defaultFuncOpt.isDefined)
        defaultFuncOpt.get.apply()
      else if (isOption)
        None
      else
        throwRequiredFieldException()
    }
  }

  /* Private */

  //optimized
  private[this] def parseFieldValue(
    fieldCodec: ObjectCodec,
    field: JsonNode,
    fieldType: JavaType,
    context: DeserializationContext
  ): Object = {
    if (isString) {
      field.asText()
    } else {
      val treeTraversingParser = new TreeTraversingParser(field, fieldCodec)
      if (deserializer.isDefined) {
        deserializer.get.deserialize(treeTraversingParser, context)
      } else {
        fieldCodec.readValue[Object](treeTraversingParser, fieldType)
      }
    }
  }

  //optimized
  private[this] def assertNotNull(field: JsonNode, value: Object): Object = {
    value match {
      case null => throw new FinatraJsonMappingException("error parsing '" + field.asText + "'")
      case traversable: Traversable[_] => assertNotNull(traversable)
      case array: Array[_] => assertNotNull(array)
      case _ => // no-op
    }
    value
  }

  private def assertNotNull(traversable: Traversable[_]): Unit = {
    if (traversable.exists(_ == null)) {
      throw new FinatraJsonMappingException(
        "Literal null values are not allowed as json array elements."
      )
    }
  }

  private def defaultValue: Option[Object] = {
    if (defaultFuncOpt.isDefined)
      defaultFuncOpt map { _() } else if (isOption)
      Some(None)
    else
      None
  }

  private def throwRequiredFieldException() = {
    throw requiredFieldException
  }

  private case class AttributeInfo(`type`: String, fieldName: String)

  @tailrec
  private def findAttributeInfo(
    fieldName: String,
    annotations: Seq[Annotation]
  ): AttributeInfo = {
    if (annotations.isEmpty) {
      AttributeInfo("field", fieldName)
    } else {
      val found = extractAttributeInfo(fieldName, annotations.head)
      if (found.isDefined) {
        found.get
      } else {
        findAttributeInfo(fieldName, annotations.tail)
      }
    }
  }

  private def extractAttributeInfo(
    fieldName: String,
    annotation: Annotation
  ): Option[AttributeInfo] = annotation match {
    case queryParam: QueryParam =>
      Some(AttributeInfo("queryParam", queryParam.value.getOrElse(fieldName)))
    case formParam: FormParam =>
      Some(AttributeInfo("formParam", formParam.value.getOrElse(fieldName)))
    case header: Header =>
      Some(AttributeInfo("header", header.value.getOrElse(fieldName)))
    case _ =>
      None
  }
}
