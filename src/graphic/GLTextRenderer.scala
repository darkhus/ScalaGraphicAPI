package graphic

import java.awt.Font
//import com.sun.opengl.util.awt.{TextRenderer => JOGLTextRenderer}
import com.jogamp.opengl.util.awt.{TextRenderer => JOGLTextRenderer}
import java.awt.Shape
import java.awt.geom.PathIterator
import javax.media.opengl.GL2
import java.awt.geom.CubicCurve2D
import javax.media.opengl.fixedfunc.GLMatrixFunc

trait GLTextRenderer { self: GLCanvas =>
  val ANCH_LEFT = Int.MinValue
  val ANCH_RIGHT = Int.MinValue+1
  val ANCH_MID = Int.MinValue+2
  val ANCH_BOT = Int.MinValue+3
  val ANCH_TOP = Int.MinValue+4
  var anchorW = ANCH_LEFT
  var anchorH = ANCH_BOT
  def DefaultFont: Font = new Font("Times New Roman", Font.BOLD, 24)
  private var _font = DefaultFont
  def font: Font = _font
  //def font_=(f: Font) = _font = f
  def font_=(f: Font) = {
    if(renderer.getFont.equals(f) == false ||
       _useFractionalMetrics != useFractionalMetrics ||
       _antialiased != antialiasedFont) {
      renderer = new JOGLTextRenderer(f, antialiasedFont, useFractionalMetrics)
      _useFractionalMetrics = useFractionalMetrics
      _antialiased = antialiasedFont
    }
  }
  private var _antialiased = true
  private var _useFractionalMetrics = true
  def antialiasedFont = _antialiased
  def useFractionalMetrics = _useFractionalMetrics
  def antialiased_(a: Boolean) = {
    _antialiased = a
    font =_font
  }
  def useFractionalMetrics_(fm: Boolean) = {
    _useFractionalMetrics = fm
    font =_font
  }

  private var renderer = new JOGLTextRenderer(_font, true, false)

  def drawText(text: String, x: Int, y: Int): Unit = {
    var w = anchorW
    if(anchorW >= Int.MinValue && anchorW <= Int.MinValue+2) {
      anchorW match {
        case ANCH_LEFT => w = 0
        case ANCH_RIGHT =>
          w = (renderer.getBounds(text)).getWidth.toInt
        case ANCH_MID =>
          w = (renderer.getBounds(text)).getWidth.toInt / 2
      }
    }

    var h = anchorH
    if(anchorH >= Int.MinValue+2 && anchorH <= Int.MinValue+4) {
      anchorH match {
        case ANCH_BOT => h = 0
        case ANCH_TOP =>
          h = (renderer.getBounds(text)).getHeight.toInt
        case ANCH_MID =>
          h = (renderer.getBounds(text)).getHeight.toInt / 2
      }
    }
    gl.glPushMatrix
    renderer.beginRendering(gl.getContext.getGLDrawable.getWidth, gl.getContext.getGLDrawable.getHeight)
    gl.getGL2().glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl.getGL2().glLoadIdentity()
    gl.glTranslatef(x-w, y-h, 0)
    renderer.setColor(color)
    renderer.draw(text, 0, 0)
    renderer.endRendering
    gl.glPopMatrix
  }

  def setTextAnchors(anchW: Int, anchH: Int): Unit = {
    this.anchorW = anchW
    this.anchorH = anchH
  }

  def drawText(text: String, x: Int, y: Int, angle: Float): Unit = {
    var w = anchorW
    if(anchorW >= Int.MinValue && anchorW <= Int.MinValue+2) {
      anchorW match {
        case ANCH_LEFT => w = 0
        case ANCH_RIGHT =>
          w = (renderer.getBounds(text)).getWidth.intValue
        case ANCH_MID =>
          w = (renderer.getBounds(text)).getWidth.intValue / 2
      }
    }

    var h = anchorH
    if(anchorH >= Int.MinValue+2 && anchorH <= Int.MinValue+4) {
      anchorH match {
        case ANCH_BOT => h = 0
        case ANCH_TOP =>
          h = (renderer.getBounds(text)).getHeight.intValue
        case ANCH_MID =>
          h = (renderer.getBounds(text)).getHeight.intValue / 2
      }
    }

    gl.glPushMatrix
    renderer.beginRendering(gl.getContext.getGLDrawable.getWidth, gl.getContext.getGLDrawable.getHeight)
    gl.getGL2().glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl.getGL2().glLoadIdentity()
    gl.glTranslatef(x, y, 0)
    gl.glRotatef(angle, 0, 0, 1)
    gl.glTranslatef(-w, -h, 0)
    renderer.setColor(color)
    renderer.draw(text, 0, 0)
    renderer.endRendering
    gl.glPopMatrix
  }

  private def pathLength(figure: Shape): Float = {
    val path = figure.getPathIterator(null, 1.0)
    val point = new Array[Float](6)
    var prevX = 0.0f; var prevY = 0.0f
    var len = 0.0f

    while (!path.isDone()) {
      path.currentSegment(point) match {
        case PathIterator.SEG_MOVETO =>
            prevX = point(0)
            prevY = point(1)         
        case PathIterator.SEG_CLOSE =>
        case PathIterator.SEG_LINETO =>
          val dx = point(0)-prevX
          val dy = point(1)-prevY
          prevX = point(0)
          prevY = point(1)
          len += Math.sqrt(dx*dx + dy*dy).toFloat
        case _ =>
          System.err.println("FlatteningPathIterator contract violated")
      }
      path.next()
    }
    return len
  }

  def drawTextOnPath(text: String, shape: Shape): Unit = {
    val it = shape.getPathIterator(null, 1.0)
    val lenght = text.length
    var prevX = 0.0f; var prevY = 0.0f
    var currChar: Int = 0
    var point = new Array[Float](6)
    var first = false
    var next = 0.0f
    var nextAdv = 0.0f

    val factor = pathLength(shape).toFloat/(renderer.getBounds(text)).getWidth.toFloat

    while(currChar<lenght && !it.isDone) {

      it.currentSegment(point) match {
        case PathIterator.SEG_MOVETO =>
          prevX = point(0)
          prevY = point(1)
          first = true
          next = renderer.getCharWidth(text.charAt(currChar))*0.5f
          nextAdv = next
        
        case PathIterator.SEG_LINETO => 
          val dx = point(0)-prevX
          val dy = point(1)-prevY
          val dist:Float = scala.Math.sqrt(dx*dx + dy*dy).toFloat

          if(dist >= next) {
            val r:Float = 1.0f/dist
            val angle:Float = scala.Math.atan2(dy, dx).toFloat
            while(currChar<lenght && dist >= next) {
              val x:Float = prevX + next*dx*r
              val y:Float = prevY + next*dy*r
              val nextAdvTmp = nextAdv
              if(currChar+1<lenght)
                nextAdv = renderer.getCharWidth(text.charAt(currChar+1))*0.5f
              else
                nextAdv = 0
              gl.glPushMatrix
                renderer.beginRendering(gl.getContext.getGLDrawable.getWidth, gl.getContext.getGLDrawable.getHeight)
                renderer.setColor(color)
                gl.getGL2().glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
                gl.getGL2().glLoadIdentity()
                gl.glTranslatef(x, y, 0)
                gl.glRotatef(angle.toDegrees, 0, 0, 1)
                //gl.glTranslatef(-nextAdv, 0, 0)
                renderer.draw(text.charAt(currChar).toString, 0, 0)
                renderer.endRendering
              gl.glPopMatrix
              next += (nextAdvTmp+nextAdv) * factor
              currChar+=1
            }
            next -= dist
            first = false
            prevX = point(0)
            prevY = point(1)
          }        
        case PathIterator.SEG_CLOSE =>
        case _ =>
          System.err.println("FlatteningPathIterator contract violated")
      }
      it.next
    }
  }

}