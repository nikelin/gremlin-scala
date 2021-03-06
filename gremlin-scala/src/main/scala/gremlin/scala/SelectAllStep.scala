package gremlin.scala

import org.apache.tinkerpop.gremlin.process.traversal.{Path, Traversal, Traverser}
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent
import org.apache.tinkerpop.gremlin.process.traversal.step.map.MapStep
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement
import scala.collection.JavaConversions._
import scala.language.postfixOps
import shapeless._
import shapeless.ops.hlist._

class SelectAllStep[S, Labels <: HList, LabelsTuple](traversal: Traversal[_, _])(implicit tupler: Tupler.Aux[Labels, LabelsTuple]) extends MapStep[S, LabelsTuple](traversal.asAdmin) with TraversalParent {

  override def getRequirements = Set(TraverserRequirement.PATH)

  override def map(traverser: Traverser.Admin[S]): LabelsTuple = {
    val labels: Labels = toHList(toList(traverser.path))
    tupler(labels)
  }

  def toList(path: Path): List[Any] = {
    val labels = path.labels
    def hasUserLabel(i: Int) = !labels(i).isEmpty

    (0 until path.size) filter hasUserLabel map path.get[Any] toList
  }

  private def toHList[T <: HList](path: List[_]): T =
    if (path.length == 0)
      HNil.asInstanceOf[T]
    else
      (path.head :: toHList[IsHCons[T]#T](path.tail)).asInstanceOf[T]
}
