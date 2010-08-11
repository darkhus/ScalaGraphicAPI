package graphic
package test

import java.awt.{Canvas => _, _}
import javax.media.opengl.GL2

trait Demo {
  def log(s: String) = println(s)
  
  def textOutline(f: Font, str: String, x: Int, y: Int): Shape =
    f.createGlyphVector(new font.FontRenderContext(null, true, true), str).getOutline(x,y)
      
  var width = 0
  var height = 0

  var gl: GL2 = null

  def init()
  def step(canvas: Canvas)
  def draw(canvas: Canvas)
}