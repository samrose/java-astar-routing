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
package org.neo4j.cypher.internal.pipes

import aggregation.AggregationFunction
import collection.Seq
import java.lang.String
import org.neo4j.helpers.ThisShouldNotHappenError
import org.neo4j.cypher.internal.symbols.{AnyType, Identifier, SymbolTable}
import org.neo4j.cypher.internal.commands.{Expression, AggregationExpression}
import collection.mutable.Map
import collection.immutable.{Map => ImmutableMap}

// This class can be used to aggregate if the values sub graphs come in the order that they are keyed on
class OrderedAggregationPipe(source: Pipe, val keyExpressions: Seq[Expression], aggregations: Seq[AggregationExpression]) extends PipeWithSource(source) {

  if (keyExpressions.isEmpty)
    throw new ThisShouldNotHappenError("Andres Taylor", "The ordered aggregation pipe should never be used without aggregation keys")

  val symbols: SymbolTable = createSymbols()

  def dependencies: Seq[Identifier] = keyExpressions.flatMap(_.dependencies(AnyType())) ++ aggregations.flatMap(_.dependencies(AnyType()))

  def createSymbols() = {
    val keySymbols = source.symbols.filter(keyExpressions.map(_.identifier.name): _*)
    val aggregateIdentifiers = aggregations.map(_.identifier)

    keySymbols.add(aggregateIdentifiers: _*)
  }

  def createResults(state: QueryState): Traversable[ExecutionContext] = new OrderedAggregator(source.createResults(state), keyExpressions, aggregations)

  override def executionPlan(): String = source.executionPlan() + "\r\n" + "EagerAggregation( keys: [" + keyExpressions.map(_.identifier.name).mkString(", ") + "], aggregates: [" + aggregations.mkString(", ") + "])"
}

private class OrderedAggregator(source: Traversable[ExecutionContext],
                                returnItems: Seq[Expression],
                                aggregations: Seq[AggregationExpression]) extends Traversable[ExecutionContext] {
  var currentKey: Option[Seq[Any]] = None
  var aggregationSpool: Seq[AggregationFunction] = null
  var currentCtx: Option[ExecutionContext] = null
  val keyColumns = returnItems.map(_.identifier.name)
  val aggregateColumns = aggregations.map(_.identifier.name)

  def getIntermediateResults[U](ctx: ExecutionContext) = {
    val newMap = MutableMaps.create

    //add key values
    keyColumns.zip(currentKey.get).foreach( newMap += _)

    //add aggregated values
    aggregateColumns.zip(aggregationSpool.map(_.result)).foreach( newMap += _ )

    ctx.newFrom(newMap)
  }

  def foreach[U](f: ExecutionContext => U) {
    source.foreach(ctx => {
      val key = Some(returnItems.map(_.apply(ctx)))
      if (currentKey.isEmpty) {
        aggregationSpool = aggregations.map(_.createAggregationFunction)
        currentKey = key
        currentCtx = Some(ctx)
      } else if (key != currentKey) {
        f(getIntermediateResults(currentCtx.get))

        aggregationSpool = aggregations.map(_.createAggregationFunction)
        currentKey = key
        currentCtx = Some(ctx)
      }

      aggregationSpool.foreach(func => func(ctx))
    })

    if (currentKey.nonEmpty) {
      f(getIntermediateResults(currentCtx.get))
    }
  }
}