
package graphic

import javax.media.opengl._
import javax.media.opengl.fixedfunc.GLMatrixFunc
import javax.media.opengl.glu.GLU
import java.nio.{FloatBuffer}
import java.awt.Shape
import java.awt.geom._
import java.awt.{Color, BasicStroke}
import collection.mutable.HashMap

class GLCanvas extends Canvas with GLTextRenderer with GLImageRenderer {
  protected[graphic] var gl: GL2 = null
  protected var width, height = 0
  protected val bufferId = Array(0)

  val WIDTH_MIN = 0.25
  val WIDTH_MAX = 20

  private val uniqueStencilClipValue = 10
  private val uniqueStencilValue1 = 5

  private val builder = new GeometryBuilder
  private val tessellator = new Tessellator(builder)
  private val stroker = new Stroker(builder)
  
  private val tessellationCache = new HashMap[Shape, FloatBuffer]()      

  private var _shader: Shader = null
  def shader: Shader = _shader
  def shader_=(s: Shader) {
    if(s != null){
      _shader = s
      _shader.applyShader
    } else {
      gl.glUseProgram(0)
    }
  }

  private var _stroke = new BasicStroke
  def stroke: BasicStroke = _stroke
  def stroke_=(s: BasicStroke) {
    val w = s.getLineWidth
    val cap = if(w <= 3) BasicStroke.CAP_SQUARE else s.getEndCap
    val join = if(w <= 3) BasicStroke.JOIN_BEVEL else s.getLineJoin
    
    _stroke = if(w > WIDTH_MIN && w < WIDTH_MAX) s
              else new BasicStroke(math.max(w, math.min(WIDTH_MAX, w)), cap, join,
                                   s.getMiterLimit, s.getDashArray, s.getDashPhase)
  }

  private var _color = Color.BLACK
  def color: Color = _color
  def color_=(c: Color) = {
    _color = c
    gl.glColor4ub(c.getRed.toByte, c.getGreen.toByte, c.getBlue.toByte, c.getAlpha.toByte)
  }

  private var _clip = null
  def clip: Shape = _clip
  def clip_=(shape: Shape) = {
    if(shape != null){
      gl.glEnable(GL.GL_STENCIL_TEST)
      gl.glDisable(GL.GL_BLEND)
      gl.glStencilFunc(GL.GL_ALWAYS, uniqueStencilClipValue, ~0)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE)
      gl.glColorMask(false, false, false, false)

      tessShape(shape, false)
      fillAndDrawBuffer()

      gl.glColorMask(true, true, true, true)
      gl.glStencilFunc(GL.GL_EQUAL, uniqueStencilClipValue, ~0)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_ZERO)      
    } else {
      gl.glDisable(GL.GL_STENCIL_TEST)
    }
  }

  private[graphic] def init(gl2: GL2) {
    gl = gl2
    gl2.glEnable(GL.GL_MULTISAMPLE)
    gl2.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)
    gl2.glEnable(GL.GL_BLEND)

    gl2.glEnableClientState(javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY)
    
    initVBO()

    gl2.glColor3f(0, 0, 0)
  }

  private[graphic] def resize(width: Int, height: Int) {
    this.width = width
    this.height = height
    val glu = new GLU
    gl.glViewport(0, 0, width, height)
    gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    gl.glLoadIdentity()
    glu.gluOrtho2D(0.0, width, height, 0.0)
    gl.glClearStencil(0)
    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl.glLoadIdentity()
    gl.glTranslated(0.375, 0.375, 0)
  }

  def stroke(shape: Shape): Unit = {
    if(shape != null) {
      if(color.getAlpha == 1.0f)
        gl.glDisable(GL.GL_BLEND)
      gl.glEnable(GL.GL_STENCIL_TEST)
      gl.glStencilFunc(GL.GL_EQUAL, uniqueStencilValue1, ~0)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_ZERO)

      if(stroke.getDashArray != null) {        
        stroker.stroke(shape, stroke, false) // when dash
      } else {
        stroker.stroke(shape, stroke, true) // when no dash
      }

      fillAndDrawBuffer()
      gl.glDisable(GL.GL_STENCIL_TEST)
      if(color.getAlpha == 1.0f)
        gl.glEnable(GL.GL_BLEND)
    }
  }
  
  def fill(shape: Shape): Unit = {
    // simple test if shape is convex (no need to tessalate convex)
    shape match {
      case _ : Rectangle2D | _ : RoundRectangle2D | _ : Ellipse2D =>
        tessellator.tessellateConvex(shape)
      case _ => 
        tessShape(shape, false)
    }
    if(color.getAlpha == 1.0f) gl.glDisable(GL.GL_BLEND)
    fillAndDrawBuffer()
    if(color.getAlpha == 1.0f) gl.glEnable(GL.GL_BLEND)
  }

  def clipStroke(shape: Shape): Unit = {
    if(shape != null) {
      gl.glEnable(GL.GL_STENCIL_TEST)
      gl.glDisable(GL.GL_BLEND)
      gl.glStencilFunc(GL.GL_ALWAYS, uniqueStencilClipValue, ~0)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE)
      gl.glColorMask(false, false, false, false)

      val noDash = stroke.getDashArray == null
      stroker.stroke(shape, stroke, noDash)

      fillAndDrawBuffer()

      gl.glColorMask(true, true, true, true)
      gl.glStencilFunc(GL.GL_EQUAL, uniqueStencilClipValue, ~0)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_ZERO)
    } else {
      gl.glDisable(GL.GL_STENCIL_TEST)
    }
  }

  private def tessShape(shape: Shape, doCache: Boolean)  {
    tessellator.tessellate(shape, doCache)
    /*
    if (tessellationCache contains shape) {
      val cachedCoords = tessellationCache(shape)
      builder.rewind()
      builder.fill(cachedCoords)
    } else {
      tessellator.tessellate(shape)

      if(doCache && tessellationCache.size <= tessellator.TESS_STORE_LIMIT) {
        tessellationCache(shape) = builder.coordData
        builder.newCoordData
      }
    }
    */
  }

  def clear(c: Color): Unit = {
    gl.glClearColor(c.getRed/255f, c.getGreen/255f, c.getBlue/255f, c.getAlpha/255f)
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT)
    gl.glEnable(GL.GL_STENCIL_TEST)
    gl.glDisable(GL.GL_BLEND)
    gl.glStencilFunc(GL.GL_ALWAYS, uniqueStencilValue1, 0)
    gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE)
    gl.glColor4f(c.getRed/255f, c.getGreen/255f, c.getBlue/255f, c.getAlpha/255f)
    this.fill(new Rectangle2D.Float(0, 0, width, height))
    gl.glDisable(GL.GL_STENCIL_TEST)
    gl.glEnable(GL.GL_BLEND)
    gl.glColor4f(_color.getRed/255f, _color.getGreen/255f, _color.getBlue/255f, _color.getAlpha/255f)
    tessellator.clearSkipArray()
  }

  def deinit() {
    gl.glDeleteBuffers(1, bufferId, 0)
  }


  private def initVBO() {
    gl.glGenBuffers(1, bufferId, 0)
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferId(0))
    gl.glVertexPointer(2, GL.GL_FLOAT, 0, 0)
    gl.glBufferData(GL.GL_ARRAY_BUFFER, 4*builder.size, null, GL.GL_DYNAMIC_DRAW)
  }


  private def fillAndDrawBuffer() {
    val count = builder.vertexCount
    builder.rewind()
    // avoid sychronization in glBufferSubData by emptying the buffer
    // tremendous speedup on Ingo's MBP
    // no effect on performance on Dariusz's WinXP/ATI box or Ingo's Win7/ION netbook
    //gl.glBufferData(GL.GL_ARRAY_BUFFER, 8*count, null, GL.GL_DYNAMIC_DRAW)

    /**
     * on XP it is speed up only when contex is released in each frame,
     * otherwise glBufferSubData is faster
     */
    
    gl.glVertexPointer(2, GL.GL_FLOAT, 0, 0)
    gl.glBufferData(GL.GL_ARRAY_BUFFER, 8*count, builder.coordData, GL.GL_DYNAMIC_DRAW)
    //gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, count*8, verts)
    gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, count)
  }
}