package graphic

import java.awt._
import java.awt.geom._
import java.awt.image._
import java.awt.font._

/**
 * Abstract low-level immediate mode 2D graphics API.
 */
abstract class Canvas {
  def DefaultStroke = new BasicStroke
  def stroke: BasicStroke
  def stroke_=(s: BasicStroke)
  
  def DefaultColor = Color.BLACK
  def color: Color
  def color_=(c: Color)

  def DefaultFont: Font = new Font("Times New Roman", Font.PLAIN, 12)
  def font: Font
  def font_=(f: Font)

  def clear(c: Color)
  def stroke(shape: Shape)
  def drawText(text: String, x: Int, y: Int)
  def drawTextOnPath(text: String, path: Path2D)
  //def drawImage(image: BufferedImage, x: Int, y: Int, width: Int, height: Int)
  
  def fill(shape: Shape)
  def clip_=(shape: Shape)
  def clip: Shape
}