package graphic

import java.awt._
import java.awt.font._

/**
* Abstract low-level immediate mode 2D graphics API.
*/
abstract class Canvas {
  def stroke: BasicStroke
  def stroke_=(s: BasicStroke)
  def color: Color
  def color_=(c: Color)
  
  def DefaultFont: Font
  def font: Font
  def font_=(f: Font)

  def clear(c: Color)
  def stroke(shape: Shape)
  def drawText(text: String, x: Int, y: Int)
  
  def fill(shape: Shape)
  def clip_=(shape: Shape)
  def clip: Shape
}