package graphic
package test

import java.awt.{Canvas=>_, _}
import java.awt.geom._
import math._

object StrokeDemo extends FPSDemo {
  var animY1 = 0.01
  var dashOffset = 0f
  var dashWidth = 4f
  
  val outline = textOutline(new Font("Times New Roman", Font.BOLD, 108), "H e l l o", 100, 400)
  
  // counter clock wise
  val triangle1 = {
    val p = new Path2D.Float()
    p.moveTo(0, 0)
    p.lineTo(80, 0)
    p.lineTo(40, -60)
    p.closePath()
    p.transform(AffineTransform.getTranslateInstance(290, 180))
    p
  }
  // clock wise
  val triangle2 = {
    val p = new Path2D.Float()
    p.moveTo(0, 0)
    p.lineTo(40, -60)
    p.lineTo(80, 0)
    p.closePath()
    p.transform(AffineTransform.getTranslateInstance(390, 180))
    p
  }
  
  def draw(g: Canvas) {
    g.clear(Color.WHITE)

    val rect1 = new Rectangle2D.Float(290, 200, 80, 90)
    val rect2 = new Rectangle2D.Float(390, 200, 80, 90)
    val rrect1 = new RoundRectangle2D.Float(90, 200, 80, 90, 30, 30)
    val rrect2 = new RoundRectangle2D.Float(190, 200, 80, 90, 30, 30)
    
    g.color = Color.cyan
    g.fill(triangle1)
    g.fill(triangle2)
    g.fill(rect1)
    g.fill(rect2)
    
    g.color = new Color(0.0f, 0.0f, 0.01f)
    g.stroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 50, Array(8f, 10f), dashOffset)
    g.stroke(new Ellipse2D.Float(200, 30, 200, 10))
    g.stroke(new Ellipse2D.Float(200, 50, 200, 50))

    dashOffset += 0.2f
    dashOffset %= 18
   
    g.stroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 50, Array(8f, 10f), dashOffset)
    g.stroke(rrect1)
    g.stroke(rect1)
    g.stroke(triangle1)
    
    dashWidth += 0.05f
    dashWidth %= 10
    g.stroke = new BasicStroke(dashWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 50, Array(12f, 6f), dashOffset)
    g.stroke(rrect2)
    g.stroke(rect2)
    g.stroke(triangle2)

    g.stroke = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 50, Array(8f, 4f, 2f, 4f), dashOffset)
    g.stroke(outline)

    g.color = new Color(1.0f, 0.5f, 0.3f)
    //g.font = new Font("Times New Roman", Font.BOLD, dashWidth.toInt*5)
    //g.drawTextOnPath("scala java kawa", new Path2D.Float(new Ellipse2D.Float(100, 100, 150+(math.sin(animY1*5)*90.0).toInt, 100)))

    animY1 +=0.005f
    val p = new Path2D.Float
    p.moveTo(200 - 150*sin(animY1), 200 + 80*cos(animY1))
    p.curveTo(200 + 300*sin(animY1), 200 + 300*cos(animY1),
              300 - 300*sin(animY1), 300 - 300*cos(animY1),
              350 + 50*sin(animY1), 350 - 150*cos(animY1))
    p.quadTo(200 - 50*sin(animY1), 200 + 50*cos(animY1),
             300 + 150*sin(animY1*3.5), 300 + 180*cos(animY1*2))
    p.lineTo(200 - 150*sin(animY1), 200 + 80*cos(animY1))
    p.closePath()

    g.stroke = new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 50, Array(10f, 15f), 0)
    g.color = new Color(0.8f, 0.2f, 0.2f)
    g.fill(p)
    g.color = new Color(0.2f, 0.8f, 0.2f, 0.5f)
    g.stroke(p)
  }
}