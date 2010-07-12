package graphic

import java.awt._
import java.awt.geom._
import math._

object SimpleDemo extends Demo {
  var animY1 = 0.01
  var dashOffset = 0f
  var dashWidth = 4f

  def draw(g: GLCanvas) {
    g.clear(Color.WHITE)
    g.color = new Color(0.0f, 0.0f, 0.01f)
    g.stroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 50, Array(8f, 10f), dashOffset)
    g.strokeEllipse(200, 200, 200, 50)

    dashOffset += 0.2f
    dashOffset %= 18

    g.stroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 50, Array(8f, 10f), dashOffset)
    g.strokeRoundRectangle(290, 290, 80, 90, 30, 30)

    dashWidth += 0.05f
    dashWidth %= 10
    g.stroke = new BasicStroke(dashWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 50, Array(8f, 1f, 6f), dashOffset)
    g.strokeRoundRectangle(390, 290, 80, 90, 30, 30)

    //g.strokeRectangle(100, 350, 200, 50)

    g.font = g.font.deriveFont(64f)
    g.stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 50, Array(4f, 6f), dashOffset)
    g.stroke(textOutline(g, "H   e   l   l   o", 100, 400))
    g.color = new Color(1.0f, 0.5f, 0.3f, 0.9f)
    
    g.setTextFont(new Font("Times New Roman", Font.BOLD, 48), true, true)
    g.drawShapeText("scala java kawa", new Ellipse2D.Float(100, 100, 150+(math.sin(animY1*5)*90.0).toInt, 100))

    animY1 +=0.01f
    val p = new Path2D.Float
    p.moveTo(200 - 150*sin(animY1), 200 + 80*cos(animY1))
    p.curveTo(200 + 300*sin(animY1), 200 + 300*cos(animY1),
              300 - 300*sin(animY1), 300 - 300*cos(animY1),
              350 + 50*sin(animY1), 350 - 150*cos(animY1))
    p.quadTo(200 - 50*sin(animY1), 200 + 50*cos(animY1),
             200 - 150*sin(animY1), 200 + 80*cos(animY1))
    p.closePath()
    g.color = new Color(0.2f, 0.8f, 0.2f)
    g.stroke = new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 50, Array(8f, 12f), 0)    
    g.stroke(p)
    //g.fill(new Arc2D.Float(100, 300, 20, 200, 0, 270, Arc2D.CHORD))
    g.color = new Color(0.2f, 0.2f, 0.8f)
    g.fill(p)

    g.stroke = new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)//, 50, Array(4f, 6f), dashOffset)
    g.setTextFont(new Font("Times New Roman", Font.BOLD, 68), true, true)
    //g.clipStroke(p)
    g.clip = textOutline(g, "H   e   l   l   o", 100, 300)
    image.drawImage(00, 00, 500, 500)
    g.deactiveClip

    shader.applyShader
    g.fillEllipse(50, 50, 90, 30)
    shader.deactiveShader
  }
}