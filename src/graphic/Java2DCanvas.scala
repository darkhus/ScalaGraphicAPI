package graphic

import java.awt._
import java.awt.geom._
import java.awt.font._
import java.awt.image.BufferedImage

class Java2DCanvas(private[graphic] var g: Graphics2D, var width: Int, var height: Int) extends Canvas {
  stroke = DefaultStroke
  color = DefaultColor
  font = DefaultFont
  
  def stroke: BasicStroke = g.getStroke.asInstanceOf[BasicStroke]
  def stroke_=(s: BasicStroke) = g.setStroke(s) 
  def color: Color = g.getColor
  def color_=(c: Color) = g.setColor(c)

  def font: Font = g.getFont
  def font_=(f: Font) = g.setFont(f)
  def drawText(text: String, x: Int, y: Int) = g.drawString(text, x, y)
  def drawTextOnPath(text: String, shape: Shape) = throw new IllegalArgumentException

  def shader: Shader = null
  def shader_=(s: Shader) = null
  def drawImage(image: GLImage, x: Int, y: Int, width: Int, height: Int) = null
  def drawImage(image: BufferedImage, x: Int, y: Int, width: Int, height: Int) = null

  def clear(c: Color) = {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setBackground(c)
    val oldTransform = g.getTransform
    g.setTransform(new AffineTransform)
    g.clearRect(0, 0, width, height)
    g.setTransform(oldTransform)
  }
  def stroke(shape: Shape) = g.draw(shape)
 
  def fill(shape: Shape) = g.fill(shape)
  def clip_=(shape: Shape) = g.setClip(shape)
  def clip: Shape = g.getClip
}