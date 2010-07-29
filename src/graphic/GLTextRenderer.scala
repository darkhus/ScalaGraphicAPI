package graphic

import java.awt.{Font, Shape}
import java.awt.geom.{PathIterator, Path2D}
import java.util.ArrayList
import com.jogamp.opengl.util.awt.{TextRenderer => JOGLTextRenderer}
import javax.media.opengl.GL
import javax.media.opengl.fixedfunc.GLMatrixFunc

trait GLTextRenderer { canvas: GLCanvas =>
//  private val bufferId: Array[Int] = Array(0)
  val ANCH_LEFT = Int.MinValue
  val ANCH_RIGHT = Int.MinValue+1
  val ANCH_MID = Int.MinValue+2
  val ANCH_BOT = Int.MinValue+3
  val ANCH_TOP = Int.MinValue+4
  var anchorW = ANCH_LEFT
  var anchorH = ANCH_BOT
  private val REND_CACHE_LIMIT = 100
  
  private var _font = DefaultFont
  def font: Font = _font
  //def font_=(f: Font) = _font = f
  def font_=(f: Font) {
    _font = f
    if(renderer.getFont != f || useFractionalMetrics || antialiasedFont) { // TODO: why these tests?
      /**
       * apperance of font change as well according to those parameters;
       * use could change it somewher in loop, those parameters can be ste by user
       */
      cacheRenderer(renderer, f)
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

  private var renderer = new JOGLTextRenderer(_font, true, true)

  private val fontStore = new ArrayList[Font]
  private val rendStore = new ArrayList[JOGLTextRenderer]
  private def cacheRenderer(r: JOGLTextRenderer, f: Font) {
    // ceches renderer object or recall form cache
    val index = fontStore.indexOf(f)
    if(index != -1){
      renderer = rendStore.get(index)
    } else {
      renderer = new JOGLTextRenderer(f, true, true)
      if(fontStore.size < REND_CACHE_LIMIT){
        fontStore.add(f)
        rendStore.add(renderer)        
      }
    }
  }

  def drawText(text: String, x: Int, y: Int): Unit = {
    // TODO: what is the anchoring code doing here?
    /**
     * sets text in correct position, but ther doesnt need to be anchoring at all
     */
    val w = setAnchorW(text)
    val h = setAnchorH(text)

    gl.glPushMatrix
    renderer.beginRendering(canvas.width, canvas.height)
    gl.getGL2().glMatrixMode(GLMatrixFunc.GL_MODELVIEW)    
    gl.glTranslatef(x, canvas.height-y, 0)
    renderer.setColor(color)
    renderer.draw(text, 0, 0)
    renderer.endRendering
    gl.glPopMatrix

    /* binds again main graphic buffer */
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferId(0))
  }

  def setTextAnchors(anchW: Int, anchH: Int): Unit = {
    this.anchorW = anchW
    this.anchorH = anchH
  }

  // TODO: major code duplication with the other drawText method

  /**
   * done
   */
  def drawText(text: String, x: Int, y: Int, angle: Float): Unit = {
    val w = setAnchorW(text)
    val h = setAnchorH(text)

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

     /* binds again main graphic buffer */
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferId(0))
  }

  private def setAnchorW(text: String): Int = {
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
    return w
  }

  private def setAnchorH(text: String): Int =  {
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
    return h
  }

  private def pathLength(path: Path2D): Float = {
    val iter = path.getPathIterator(null, 1.0) // TODO: flatness 1.0 okay?
    /**
     * for sure, it is not visible, only sets character in corect position
     */
    val point = new Array[Float](6)
    var prevX = 0.0f; var prevY = 0.0f
    var len = 0.0f

    while (!iter.isDone()) {
      iter.currentSegment(point) match {
        case PathIterator.SEG_MOVETO =>
            prevX = point(0)
            prevY = point(1)         
        case PathIterator.SEG_CLOSE =>
        case PathIterator.SEG_LINETO =>
          val dx = point(0)-prevX
          val dy = point(1)-prevY
          prevX = point(0)
          prevY = point(1)
          len += math.sqrt(dx*dx + dy*dy).toFloat
        case _ =>
          System.err.println("PathIterator contract violated")
      }
      iter.next()
    }
    return len
  }

  def drawTextOnPath(text: String, path: Path2D): Unit = {
    val it = path.getPathIterator(null, 1.0) // TODO: flatness 1.0 is okay?
    val lenght = text.length
    var prevX = 0.0f; var prevY = 0.0f
    var currChar: Int = 0
    var point = new Array[Float](6)
    var first = false
    var next = 0.0f
    var nextAdv = 0.0f

    val factor = pathLength(path).toFloat/(renderer.getBounds(text)).getWidth.toFloat    

    renderer.beginRendering(gl.getContext.getGLDrawable.getWidth, gl.getContext.getGLDrawable.getHeight)
    renderer.setColor(color)
    
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
          val dist = math.sqrt(dx*dx + dy*dy).toFloat

          if(dist >= next) {
            val r = 1.0f/dist
            val angle = math.atan2(dy, dx).toFloat
            while(currChar<lenght && dist >= next) {
              val x = prevX + next*dx*r
              val y = prevY + next*dy*r
              val nextAdvTmp = nextAdv
              if(currChar+1<lenght)
                nextAdv = renderer.getCharWidth(text.charAt(currChar+1))*0.5f
              else
                nextAdv = 0
              gl.glPushMatrix                
                gl.getGL2().glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
                gl.getGL2().glLoadIdentity()
                gl.glRotatef(angle.toDegrees, 0, 0, 1)                
                renderer.draw(text.charAt(currChar).toString, x.toInt, y.toInt)
              gl.glPopMatrix
              next += (nextAdvTmp+nextAdv) * factor
              currChar+=1
            }
            next -= dist
            first = false
            prevX = point(0)
            prevY = point(1)
          }        
        case _ =>
          System.err.println("PathIterator contract violated")
      }
      it.next
    }
    renderer.endRendering

    /* binds again main graphic buffer */
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferId(0))
  }
}
