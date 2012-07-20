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
package org.neo4j.cypher.internal.executionplan.builders

import org.neo4j.cypher.internal.pipes.{ExtractPipe, EagerAggregationPipe}
import org.neo4j.cypher.internal.executionplan.{ExecutionPlanInProgress, PartiallySolvedQuery, PlanBuilder}
import org.neo4j.cypher.internal.commands.{CachedExpression, Entity, Expression, AggregationExpression}

class AggregationBuilder extends PlanBuilder with ExpressionExtractor {
  def apply(plan: ExecutionPlanInProgress) = {

    val (keyExpressionsToExtract,_) = getExpressions(plan)

    val newPlan = ExtractBuilder.extractIfNecessary(plan, keyExpressionsToExtract)

    val (
      keyExpressions:Seq[Expression],
      aggregationExpressions: Seq[AggregationExpression]
      ) = getExpressions(newPlan)

    val pipe = new EagerAggregationPipe(newPlan.pipe, keyExpressions, aggregationExpressions)

    val query = newPlan.query

    val notKeyAndNotAggregate = query.returns.flatMap(_.token.expressions(pipe.symbols)).filterNot(keyExpressions.contains)

    val resultPipe = if (notKeyAndNotAggregate.isEmpty) {
      pipe
    } else {

      val rewritten = notKeyAndNotAggregate.map(e => {
        e.rewrite(removeAggregates)
      })

      new ExtractPipe(pipe, rewritten)
    }

    val resultQ = query.copy(
      aggregation = query.aggregation.map(_.solve),
      aggregateQuery = query.aggregateQuery.solve,
      extracted = true
    ).rewrite(removeAggregates)

    newPlan.copy(query = resultQ, pipe = resultPipe)
  }

  private def removeAggregates(e: Expression) = e match {
    case e: AggregationExpression => CachedExpression(e.identifier.name, e.identifier)
    case x => x
  }

  def canWorkWith(plan: ExecutionPlanInProgress) = {
    val q = plan.query

    q.aggregateQuery.token &&
      q.aggregateQuery.unsolved &&
      q.readyToAggregate

  }

  def priority: Int = PlanBuilder.Aggregation
}

trait ExpressionExtractor {
  def getExpressions(plan: ExecutionPlanInProgress): (Seq[Expression], Seq[AggregationExpression]) = {
    val keys = plan.query.returns.flatMap(_.token.expressions(plan.pipe.symbols)).filterNot(_.containsAggregate)
    val returnAggregates = plan.query.aggregation.map(_.token)

    val eventualSortAggregation = plan.query.sort.filter(_.token.expression.isInstanceOf[AggregationExpression]).map(_.token.expression.asInstanceOf[AggregationExpression])

    val aggregates = eventualSortAggregation ++ returnAggregates.map(_.asInstanceOf[AggregationExpression])

    (keys, aggregates)
  }
}