

package graphic

import java.awt.Font
import com.jogamp.opengl.util.awt.TextRenderer
import java.awt.Shape
import java.awt.geom.PathIterator
import javax.media.opengl.GL2
import java.awt.geom.CubicCurve2D
import javax.media.opengl.fixedfunc.GLMatrixFunc

class FontText {

  val BOLD:Int = Font.BOLD
  val ITALIC:Int = Font.ITALIC
  val PLAIN:Int = Font.PLAIN
  val ROMAN_BAELINE = Font.ROMAN_BASELINE
  val TRUETYPE_FONT = Font.TRUETYPE_FONT
  val ANCH_LEFT = Int.MinValue
  val ANCH_RIGHT = Int.MinValue+1
  val ANCH_MID = Int.MinValue+2
  val ANCH_BOT = Int.MinValue+3
  val ANCH_TOP = Int.MinValue+4
  var anchorW = ANCH_LEFT
  var anchorH = ANCH_BOT
  var tr:Float=0 // text colors
  var tg:Float=0
  var tb:Float=0
  var ta:Float=1.0f
  var gl:GL2 = null
  var x1 = 0; var y1=0; var x2=0; var y2=0;
  var ctrlx1=0; var ctrly1=0; var ctrlx2=0; var ctrly2=0;
  private var curve:CubicCurve2D = new CubicCurve2D.Float
  private var font:Font = new Font("Times New Roman", Font.BOLD, 14)
  private var renderer = new TextRenderer(font, false, false)

  def createFontText(gl:GL2, fontName:String, fontType:Int, fontSize:Int, antialiased:Boolean) = {
    this.gl = gl
    font = new Font(fontName, fontType, fontSize)
    renderer = new TextRenderer(font, antialiased, false)
  }

  def setTextColor(r:Float, g:Float, b:Float, a:Float) = {
    tr = r; tg = g; tb = b; ta = a;
  }

  def setTextColor(r:Float, g:Float, b:Float) = {
    tr = r; tg = g; tb = b; ta = 1.0f;
  }

  def drawText(text:String, x:Int, y:Int) = {
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
    gl.glTranslatef(x-w, y-h, 0)
    renderer.setColor(tr, tg, tb, ta)
    renderer.draw(text, 0, 0)
    renderer.endRendering
    gl.glPopMatrix
  }

  def setTextAnchors(anchW:Int, anchH:Int) = {
    this.anchorW = anchW
    this.anchorH = anchH
  }

  def drawText(text:String, x:Int, y:Int, angle:Float) = {
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
    renderer.setColor(tr, tg, tb, ta)
    renderer.draw(text, 0, 0)
    renderer.endRendering
    gl.glPopMatrix
  }

  private def pathLength(figure:Shape) = {
    val path = figure.getPathIterator(null, 1.0)
    val point = new Array[Float](6)
    var prevX = 0.0f; var prevY = 0.0f
    var len:Float = 0.0f

    while (!path.isDone()) {
      path.currentSegment(point) match {
        case PathIterator.SEG_MOVETO => {
            prevX = point(0)
            prevY = point(1)
          }
        case PathIterator.SEG_CLOSE => {}
        case PathIterator.SEG_LINETO => {
          val dx = point(0)-prevX
          val dy = point(1)-prevY
          prevX = point(0)
          prevY = point(1)
          len += Math.sqrt(dx*dx + dy*dy).floatValue
        }
      }
      path.next()
    }
    len
  }

  def setTextCurveParam(x1:Int, y1:Int, x2:Int, y2:Int, ctrlx1:Int, ctrly1:Int, ctrlx2:Int, ctrly2:Int) = {
    this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
    this.ctrlx1 = ctrlx1; this.ctrly1 = ctrly1; this.ctrlx2 = ctrlx2; this.ctrly2 = ctrly2;
  }

  def drawShapeText(text:String) = {
    curve = new CubicCurve2D.Float(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2)
    val it = curve.getPathIterator(null, 1.0)
    val lenght = text.length    
    var prevX = 0.0f; var prevY = 0.0f
    var currChar:Int = 0
    var point = new Array[Float](6)
    var first = false
    var next = 0.0f
    var nextAdv = 0.0f

    val factor:Float = pathLength(curve).floatValue /(renderer.getBounds(text)).getWidth.floatValue

    while(currChar<lenght && !it.isDone) {

      it.currentSegment(point) match {
        case PathIterator.SEG_MOVETO => {
            prevX = point(0)
            prevY = point(1)
            first = true
            next = renderer.getCharWidth(text.charAt(currChar))*0.5f
            nextAdv = next
        }
        case PathIterator.SEG_LINETO => {
          val dx = point(0)-prevX
          val dy = point(1)-prevY
          val dist:Float = Math.sqrt(dx*dx + dy*dy).floatValue

          if(dist >= next) {
            val r:Float = 1.0f/dist
            val angle:Float = Math.atan2(dy, dx).floatValue
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
                renderer.setColor(tr, tg, tb, ta)
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
        }
        case PathIterator.SEG_CLOSE => {
        }
      }
      it.next
    }       
  }

}
