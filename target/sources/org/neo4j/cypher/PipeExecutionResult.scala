/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher

import internal.commands.{IterableSupport, IsIterable}
import internal.StringExtras
import scala.collection.JavaConverters._
import org.neo4j.graphdb.{PropertyContainer, Relationship, Node}
import java.io.{StringWriter, PrintWriter}
import java.lang.String
import internal.symbols.SymbolTable
import collection.Map
import collection.immutable.{Map => ImmutableMap}

class PipeExecutionResult(r: => Traversable[Map[String, Any]], val symbols: SymbolTable, val columns: List[String])
  extends ExecutionResult
  with StringExtras
  with IterableSupport {

  lazy val immutableResult = r.map(m => m.toMap)

  def javaColumns: java.util.List[String] = columns.asJava

  def javaColumnAs[T](column: String): java.util.Iterator[T] = columnAs[T](column).map(x => makeValueJavaCompatible(x).asInstanceOf[T]).asJava

  def columnAs[T](column: String): Iterator[T] = {
    this.map(m => {
      val item: Any = m.getOrElse(column, throw new EntityNotFoundException("No column named '" + column + "' was found. Found: " + m.keys.mkString("(\"", "\", \"", "\")")))
      item.asInstanceOf[T]
    }).toIterator
  }

  private def makeValueJavaCompatible(value: Any): Any = value match {
    case iter: Seq[_] => iter.asJava
    case x => x
  }

  def javaIterator: java.util.Iterator[java.util.Map[String, Any]] = this.map(m => {
    m.map(kv => kv._1 -> makeValueJavaCompatible(kv._2)).asJava
  }).toIterator.asJava

  private def calculateColumnSizes(result: Seq[Map[String, Any]]): Map[String, Int] = {
    val columnSizes = new scala.collection.mutable.HashMap[String, Int] ++ columns.map(name => name -> name.size)

    result.foreach((m) => {
      m.foreach((kv) => {
        val length = text(kv._2).size
        if (!columnSizes.contains(kv._1) || columnSizes.get(kv._1).get < length) {
          columnSizes.put(kv._1, length)
        }
      })
    })
    columnSizes.toMap
  }

  protected def createTimedResults = {
    val start = System.currentTimeMillis()
    val eagerResult = immutableResult.toList
    val ms = System.currentTimeMillis() - start

    (eagerResult, ms.toString)
  }

  def dumpToString(writer: PrintWriter) {
    val (eagerResult, timeTaken) = createTimedResults

    val columnSizes = calculateColumnSizes(eagerResult)

    if (columns.nonEmpty) {
      val headers = columns.map((c) => Map[String, Any](c -> Some(c))).reduceLeft(_ ++ _)
      val headerLine: String = createString(columns, columnSizes, headers)
      val lineWidth: Int = headerLine.length - 2
      val --- = "+" + repeat("-", lineWidth) + "+"

      val row = if (eagerResult.size > 1) "rows" else "row"
      val footer = "%d %s".format(eagerResult.size, row)

      writer.println(---)
      writer.println(headerLine)
      writer.println(---)

      eagerResult.foreach(resultLine => writer.println(createString(columns, columnSizes, resultLine)))

      writer.println(---)
      writer.println(footer)
      if (queryStatistics.containsUpdates) {
        writer.print(queryStatistics.toString)
      }
    } else {
      if (queryStatistics.containsUpdates) {
        writer.println("+-------------------+")
        writer.println("| No data returned. |")
        writer.println("+-------------------+")
        writer.print(queryStatistics.toString)
      } else {
        writer.println("+--------------------------------------------+")
        writer.println("| No data returned, and nothing was changed. |")
        writer.println("+--------------------------------------------+")
      }
    }



    writer.println("%s ms".format(timeTaken))
  }

  def dumpToString(): String = {
    val stringWriter = new StringWriter()
    val writer = new PrintWriter(stringWriter)
    dumpToString(writer)
    writer.close()
    stringWriter.getBuffer.toString
  }

  private def props(x: PropertyContainer): String = x.getPropertyKeys.asScala.map(key => key + ":" + text(x.getProperty(key))).mkString("{", ",", "}")

  private def text(obj: Any): String = obj match {
    case x: Node => x.toString + props(x)
    case x: Relationship => ":" + x.getType.toString + "[" + x.getId + "] " + props(x)
    case IsIterable(coll) => coll.map(text).mkString("[", ",", "]")
    case x: String => "\"" + x + "\""
    case Some(x) => x.toString
    case null => "<null>"
    case x => x.toString
  }

  private def createString(columns: List[String], columnSizes: Map[String, Int], m: Map[String, Any]): String = {
    columns.map(c => {
      val length = columnSizes.get(c).get
      val txt = text(m.get(c).get)
      val value = makeSize(txt, length)
      value
    }).mkString("| ", " | ", " |")
  }

  lazy val iterator = immutableResult.toIterator

  def hasNext: Boolean = iterator.hasNext

  def next(): ImmutableMap[String, Any] = iterator.next()

  lazy val queryStatistics = QueryStatistics.empty
}

