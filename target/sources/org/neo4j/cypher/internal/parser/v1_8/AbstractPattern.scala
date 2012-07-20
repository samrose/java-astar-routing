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
package org.neo4j.cypher.internal.parser.v1_8

import org.neo4j.graphdb.Direction
import org.neo4j.cypher.internal.commands.{Predicate, Expression}
import collection.Map
import org.neo4j.cypher.SyntaxException

abstract sealed class AbstractPattern

object PatternWithEnds {
  def unapply(p: AbstractPattern): Option[(ParsedEntity, ParsedEntity, Seq[String], Direction, Boolean, Option[Int], Option[String], Predicate)] = p match {
    case ParsedVarLengthRelation(name, _, start, end, typ, dir, optional, predicate, None, maxHops, relIterator) => Some((start, end, typ, dir, optional, maxHops, relIterator, predicate))
    case ParsedVarLengthRelation(_, _, _, _, _, _, _, _, Some(x), _, _) => throw new SyntaxException("Shortest path does not support a minimal length")
    case ParsedRelation(name, _, start, end, typ, dir, optional, predicate) => Some((start, end, typ, dir, optional, Some(1), Some(name), predicate))
  }
}

abstract class PatternWithPathName(val pathName: String) extends AbstractPattern {
  def rename(newName: String): PatternWithPathName
}


case class ParsedEntity(expression: Expression,
                        props: Map[String, Expression],
                        predicate: Predicate) extends AbstractPattern

case class ParsedRelation(name: String,
                          props: Map[String, Expression],
                          start: ParsedEntity,
                          end: ParsedEntity,
                          typ: Seq[String],
                          dir: Direction,
                          optional: Boolean,
                          predicate: Predicate) extends PatternWithPathName(name) {
  def rename(newName: String): PatternWithPathName = copy(name = newName)
}

case class ParsedVarLengthRelation(name: String,
                                   props: Map[String, Expression],
                                   start: ParsedEntity,
                                   end: ParsedEntity,
                                   typ: Seq[String],
                                   dir: Direction,
                                   optional: Boolean,
                                   predicate: Predicate,
                                   minHops: Option[Int],
                                   maxHops: Option[Int],
                                   relIterator: Option[String]) extends PatternWithPathName(name) {
  def rename(newName: String): PatternWithPathName = copy(name = newName)
}

case class ParsedShortestPath(name: String,
                              props: Map[String, Expression],
                              start: ParsedEntity,
                              end: ParsedEntity,
                              typ: Seq[String],
                              dir: Direction,
                              optional: Boolean,
                              predicate: Predicate,
                              maxDepth: Option[Int],
                              single: Boolean,
                              relIterator: Option[String]) extends PatternWithPathName(name) {
def rename(newName: String): PatternWithPathName = copy(name = newName)
}

case class ParsedNamedPath(name: String, pieces: Seq[AbstractPattern]) extends PatternWithPathName(name) {
  def rename(newName: String): PatternWithPathName = copy(name = newName)
}