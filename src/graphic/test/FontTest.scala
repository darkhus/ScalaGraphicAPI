package graphic
package test

import java.awt.{Canvas=>_, _}
import java.awt.geom._

object FontTest extends FPSDemo {
  val text = "The quick brown fox jumps over the lazy dog"

  def draw(g: Canvas) {
    def drawSizeSet(y: Int, font: Font): Int = {
      g.font = font.deriveFont(9f)
      g.drawText(text, 5, y+5)
      g.font = font.deriveFont(12f)
      g.drawText(text, 5, y+17)
      g.font = font.deriveFont(16f)
      g.drawText(text, 5, y+32)
      g.font = font.deriveFont(32f)
      g.drawText(text, 5, y+58)
      g.fill(textOutline(g.font, text + " (outline)", 5, y+86))
      y+95
    }
    
    g.clear(Color.white)
    var y = 0
    y = drawSizeSet(y+5, new Font("Times New Roman", Font.PLAIN, 6))
    y = drawSizeSet(y+5, new Font("Times New Roman", Font.ITALIC, 6))
    y = drawSizeSet(y+5, new Font("Helvetica", Font.PLAIN, 6))
    y = drawSizeSet(y+5, new Font("Helvetica", Font.ITALIC, 6))
    y = drawSizeSet(y+5, new Font("Courier", Font.PLAIN, 6))
    y = drawSizeSet(y+5, new Font("Courier", Font.ITALIC, 6))
  }
}