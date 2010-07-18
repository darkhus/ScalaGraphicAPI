package graphic

import java.awt._
import java.awt.geom._
import math._

object SimpleDemo extends Demo {
  var animY1 = 0.01
  var dashOffset = 0f
  var dashWidth = 4f

  var startTime: Long = 0L
  var startTime2: Long = 0L
  var framesCounter: Long = 0L
  var fpsCounter = 0.0

  def draw(g: GLCanvas) {
    g.clear(Color.WHITE)
    g.color = new Color(0.0f, 0.0f, 0.01f)
    g.stroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 50, Array(8f, 10f), dashOffset)
    g.stroke(new Ellipse2D.Float(200, 200, 200, 50))

    dashOffset += 0.2f
    dashOffset %= 18

    g.stroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 50, Array(8f, 10f), dashOffset)
    g.stroke(new RoundRectangle2D.Float(290, 290, 80, 90, 30, 30))

    dashWidth += 0.05f
    dashWidth %= 10
    g.stroke = new BasicStroke(dashWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 50, Array(8f, 3f, 6f), dashOffset)
    g.stroke(new RoundRectangle2D.Float(390, 290, 80, 90, 30, 30))

    g.stroke = new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 50, Array(4f, 8f), dashOffset)
    g.stroke(textOutline(new Font("Times New Roman", Font.BOLD, 108), g, "H e l l o", 100, 400))
    g.color = new Color(1.0f, 0.5f, 0.3f)
    
    g.font = new Font("Times New Roman", Font.BOLD, 48)
    g.drawTextOnPath("scala java kawa", new Ellipse2D.Float(100, 100, 150+(math.sin(animY1*5)*90.0).toInt, 100))
/*
    g.color = new Color(0.2f, 0.2f, 0.8f, 0.5f)
    g.fill(new Arc2D.Float(100, 300, 80, 80, 0, 300, Arc2D.CHORD))
    g.fill(new Arc2D.Float(200, 300, 80, 80, 0, 300, Arc2D.OPEN))
    g.fill(new Arc2D.Float(300, 300, 80, 80, 0, 300, Arc2D.PIE))

    g.stroke = new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)//, 50, Array(8f, 12f), 0)
    g.stroke(new Arc2D.Float(100, 200, 80, 80, 0, 300, Arc2D.CHORD))
    g.stroke(new Arc2D.Float(200, 200, 80, 80, 0, 300, Arc2D.OPEN))
    g.stroke(new Arc2D.Float(300, 200, 80, 80, 0, 300, Arc2D.PIE))

    g.stroke = new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 50, Array(8f, 12f), 0)
    g.stroke(new Arc2D.Float(100, 100, 80, 80, 0, 300, Arc2D.CHORD))
    g.stroke(new Arc2D.Float(200, 100, 80, 80, 0, 300, Arc2D.OPEN))
    g.stroke(new Arc2D.Float(300, 100, 80, 180, 0, 300, Arc2D.PIE))
*/
    animY1 +=0.01f
    //animY1 = 6.00    
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
           
    //g.fill(textOutline(new Font("Times New Roman", Font.BOLD, 18), g, animY1.toString, 30, 200))
    //g.clipStroke(p)
    //g.clip = textOutline(new Font("Times New Roman", Font.BOLD, 18), g, "H   e   l   l   o", 100, 300)

    g.image = image1
    g.drawImage(20, 20, 40, 40)
//    g.deactiveClip
/*
    g.fill(new Ellipse2D.Float(0, 0, 50, 100))
    g.fill(new Rectangle2D.Float(120, 20, 50, 70))
    g.fill(new RoundRectangle2D.Float(300, 200, 100, 80, 50, 50))
*/

    framesCounter +=1
    var fps = 1000000000.0/((System.nanoTime - startTime))    
    fpsCounter += fps
    val avg = fpsCounter / framesCounter
    if(System.nanoTime - startTime2 > 1000000000){  // display fps in each sec
      println("Fps: " + fps.toFloat +", Avg: " + avg.toFloat)
      startTime2 = System.nanoTime
    }
    startTime = System.nanoTime    
    
  }
}