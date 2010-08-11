package graphic
package test

import java.awt.{Shape, Color, BasicStroke}
import java.awt.geom._

abstract class PrimitiveCountDemo extends FPSDemo {
  val colors = Array(Color.magenta, Color.black, Color.blue, Color.red, Color.cyan, Color.green, Color.yellow)
  val strokes = Array(new BasicStroke(1), new BasicStroke(1.5f), new BasicStroke(2), 
      new BasicStroke(2.5f), new BasicStroke(3), new BasicStroke(3.5f), new BasicStroke(4))
  
  def r = math.random.toFloat
  def chooseRandomly[T](arr: Array[T]) = arr((r*arr.length).toInt)
  
  def size = 500
  
  def draw(g: Canvas) {
    var i = 0
    // draw a bunch at once to reduce the effect of event queue latency
    while(i < 1000) {
      g.color = chooseRandomly(colors)
      g.stroke = chooseRandomly(strokes)
      g.stroke(primitive)
      i += 1
    }
  }
  
  def primitive: Shape 
}

object LineCountDemo extends PrimitiveCountDemo {
  def primitive = new Line2D.Float(r*size, r*size, r*size, r*size)
}

object RectCountDemo extends PrimitiveCountDemo {
  def primitive = new Rectangle2D.Float(r*size, r*size, r*size, r*size)
}

object EllipseCountDemo extends PrimitiveCountDemo {
  def primitive = new Ellipse2D.Float(r*size, r*size, r*size, r*size)
}