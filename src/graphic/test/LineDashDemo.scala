package graphic
package test

import java.awt.{Canvas=>_, _}
import java.awt.geom._
import math._

object LineDashDemo extends FPSDemo {
  def dashButt(d: Float*)(w: Float) = new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, d.toArray, 0)
  def dashSquare(d: Float*)(w: Float) = new BasicStroke(w, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, d.toArray, 0)
  def dashRound(d: Float*)(w: Float) = new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, d.toArray, 0)
  
  def hline(y: Int) = new Line2D.Float(5, y, width-10, y)
  
  def draw(g: Canvas) {
    def drawDashSet(y0: Int)(stroke: (Float*)=>(Float)=>BasicStroke): Int = {
      var y = drawWidthSet(y0)(stroke(2, 2))
      y = drawWidthSet(y)(stroke(10, 10))
      y = drawWidthSet(y)(stroke(20, 15))
      y = drawWidthSet(y)(stroke(10, 13, 4, 8))
      y
    }
    
    def drawWidthSet(y: Int)(stroke: Float=>BasicStroke): Int = {
      g.stroke = stroke(0.5f)
      g.stroke(hline(y))
      g.stroke = stroke(1)
      g.stroke(hline(y+5))
      g.stroke = stroke(2)
      g.stroke(hline(y+10))
      g.stroke = stroke(5)
      g.stroke(hline(y+16))
      g.stroke = stroke(10)
      g.stroke(hline(y+25))
      y+40
    }
    
    g.clear(Color.white)
    g.color = Color.black
    
    var y = drawDashSet(5)(dashButt)
    y = drawDashSet(y)(dashSquare)
    drawDashSet(y)(dashRound)
  }
}