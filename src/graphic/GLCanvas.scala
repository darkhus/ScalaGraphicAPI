package graphic

import javax.media.opengl._
import javax.media.opengl.fixedfunc.GLMatrixFunc
import javax.media.opengl.glu.GLU
import java.awt.Shape
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.{Color, BasicStroke}

class GLCanvas extends Canvas 
                  with GLTextRenderer with GLImageRenderer with Tessellator {
  private val bufferId: Array[Int] = Array(0)
  private val floatSize = 4
  private var fixedArraySize = 1024
  private val extendArraySize = 1024
  private var verts = new Array[Float](fixedArraySize)
  private var tmpVerts = new Array[Float](fixedArraySize)
  private[graphic] var gl: GL2 = null
  private val vbo = new VBuffer
  private var vertsNumTmp = 0
  private var vertsNum = 0
  private val point = new Array[Float](6)
  private val arcVerts = new Array[Float](360)

  private var i: Int = 0
  private var ind: Int = 0
  private var nx: Float = 0
  private var ny: Float = 0
  private var curx: Float = 0
  private var cury: Float = 0
  private var startInd: Int = 0
  private var endInd: Int = 0

  val miter_limit = 100  
  private var arcInd = 0
  private var endsAtStart = false
  private var implicitClose = false
  val WIDTH_MIN = 0.25
  val WIDTH_MAX = 20

  val uniqueStencilClipValue = 10
  val uniqueStencilValue1 = 5  

  //private val tess = new Tessellator

  private var _stroke = new BasicStroke
  def stroke: BasicStroke = _stroke
  def stroke_=(s: BasicStroke) {
    val w = s.getLineWidth
    _stroke = if(w > WIDTH_MIN && w < WIDTH_MAX) s
              else new BasicStroke(math.max(w, math.min(WIDTH_MAX, w)), s.getEndCap, s.getLineJoin,
                                   s.getMiterLimit, s.getDashArray, s.getDashPhase)
  }

  private def lineWidth = _stroke.getLineWidth / 2

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
      gl.glStencilFunc(GL.GL_ALWAYS, uniqueStencilClipValue, ~0)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE)
      gl.glColorMask(false, false, false, false)

      tessShape(shape)
      vbo.mapBuffer(gl, bufferId, verts)
      vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
      
      gl.glColorMask(true, true, true, true)
      gl.glStencilFunc(GL.GL_EQUAL, uniqueStencilClipValue, ~0)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_ZERO)
    } else {
      gl.glDisable(GL.GL_STENCIL_TEST)
    }
  }

  private[graphic] def init(gl2: GL2) {
    glImg = gl2
    gl = gl2   
    gl2.glEnable(GL.GL_MULTISAMPLE)
    gl2.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)
    gl2.glEnable(GL.GL_BLEND)

    gl2.glEnableClientState(javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY)
    vbo.init(gl2, bufferId, verts, null, floatSize)
    gl2.glColor3f(0, 0, 0)
  }

  private[graphic] def resize(width: Int, height: Int) {
    val glu = new GLU
    gl.glViewport(0, 0, width, height)
    gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    gl.glLoadIdentity()
    glu.gluOrtho2D(0.0, width, height, 0.0)
    gl.glClearStencil(0)
    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl.glLoadIdentity()
  }

  var fr: Long = 0
  var t1s: Long = 0
  var t2s: Long = 0
  def stroke(shape: Shape): Unit = {

    if(shape != null) {
      /*
    fr+=1
    var t = System.nanoTime      
      //strokeShape_1(shape)
      t2s += System.nanoTime - t
      //println("Basic Stroke: "+ (System.nanoTime - t) + ", avg: "+ t2s/fr)
      t = System.nanoTime
      if(stroke.getDashArray != null)
        shapeToTriangleStrip(strokeShape_2(shape), true)
      else
        shapeToTriangleStrip(shape, true)
      t1s += System.nanoTime - t
      //println("Stroke 2   : "+ (System.nanoTime - t) + ", avg: "+ t1s/fr); println
      */
    
    gl.glEnable(GL.GL_STENCIL_TEST)
    gl.glStencilFunc(GL.GL_EQUAL, uniqueStencilValue1, ~0)
    gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_ZERO)
    
    if(stroke.getDashArray != null) {// if dash      
      strokeShape_1(shape) // when dash      
      strokeShape(shape, false)
    } else {
      strokeShape(shape, true) // when no dash
    }
    
    vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)

    gl.glDisable(GL.GL_STENCIL_TEST)
    }
  }

  def fill(shape: Shape): Unit = {
    // simple test if shape is convex (no need to tessalate convex)
    if(shape.isInstanceOf[Rectangle2D] || shape.isInstanceOf[RoundRectangle2D] || shape.isInstanceOf[Ellipse2D])
         triangulateConvexPath(shape.getPathIterator(null, flatnessFactor(shape)))
    else {
      if(shape.isInstanceOf[Arc2D])
        if(shape.asInstanceOf[Arc2D].getArcType == Arc2D.OPEN)
          shape.asInstanceOf[Arc2D].setArcType(Arc2D.CHORD)
      tessShape(shape)
    }
    vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)    
  }

  def clipStroke(shape: Shape): Unit = {
    gl.glEnable(GL.GL_STENCIL_TEST)
    gl.glStencilFunc(GL.GL_ALWAYS, 1, 1)
    gl.glStencilOp(GL.GL_REPLACE, GL.GL_REPLACE, GL.GL_REPLACE)
    gl.glColorMask(false, false, false, false)

    if(stroke.getDashArray != null) // if dash
      strokeShape(shape, false)
    else
      strokeShape(shape, true)
    
    vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)

    gl.glColorMask(true, true, true, true)
    gl.glStencilFunc(GL.GL_EQUAL, 1, 1)
    gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP)
  }

  override // from tessellation
  def end() {
    // triangle fan - 6
    // triangle strip - 5
    // triangles - 4
    var st = 0
    var end = tempTessIndex
    // triangle fan -> triangle strip
    if(mode == GL.GL_TRIANGLE_FAN) {
      st+=2
      addVertex(tmpVerts(st), tmpVerts(st+1))
      while(st < end){
        addVertex(tmpVerts(st), tmpVerts(st+1))
        st+=2
        addVertex(tmpVerts(0), tmpVerts(1))
        if(st<end){
          addVertex(tmpVerts(st), tmpVerts(st+1))
          st+=2
        }
        if(st<end){
          addVertex(tmpVerts(st), tmpVerts(st+1))
          st+=2
        }
      }
      addVertex(verts(i-2), verts(i-1))
    }
    // triangle strip-> triangle strip
    if(mode == GL.GL_TRIANGLE_STRIP) {
      addVertex(tmpVerts(st), tmpVerts(st+1))
      while(st < end) {
        addVertex(tmpVerts(st), tmpVerts(st+1))
        st+=2
      }
      addVertex(verts(i-2), verts(i-1))
    }

    // triangles -> triangle strip
    if(mode == GL.GL_TRIANGLES) {
      var tri = 0
      while(st < end) {
        if(tri==0) {
          addVertex(tmpVerts(st), tmpVerts(st+1))
        }
        addVertex(tmpVerts(st), tmpVerts(st+1))
        st+=2        
        tri+=1
        if(tri==3) {
          addVertex(verts(i-2), verts(i-1))
          tri = 0
        }
      }
    }
    tempTessIndex = 0
  }

  override // from tessellation
  def vertex(vertexData: Any) {
    val data:Array[Double] = vertexData.asInstanceOf[Array[Double]]
    addTmpVertex(data(0).toFloat, data(1).toFloat, tempTessIndex)
    tempTessIndex+=2
  }

  private def tessShape(shape: Shape)  {
    val path = shape.getPathIterator(null, flatnessFactor(shape))
    this.setWindRule(path.getWindingRule)
    this.startTessPolygon
    //tess.startTessContour    
    i = 0    
    while(!path.isDone){
      var t = path.currentSegment(point)
      t match {
      case java.awt.geom.PathIterator.SEG_MOVETO =>
        this.startTessContour
        this.addVertexToTess(point(0), point(1))
      case java.awt.geom.PathIterator.SEG_LINETO =>
        this.addVertexToTess(point(0), point(1))
      case java.awt.geom.PathIterator.SEG_CLOSE =>
          this.endTessContour
      case _ =>
        System.err.println("PathIterator contract violated")
      }
      path.next
    }
    //tess.endTessContour
    this.endTessPolygon

    endInd = i
    vertsNumTmp = i/2
    vertsNum = vertsNumTmp
    i=0
    ind = 0
  }
  
  private def strokeShape_1(shape: Shape) {
    triangulateConvexPath(stroke.createStrokedShape(shape).getPathIterator(null, 1.0f))
  }
 
  private def triangulateConvexPath(path: PathIterator) {    
    var z = 0
    i=0
    while(!path.isDone) {
      path.currentSegment(point) match {
        case java.awt.geom.PathIterator.SEG_MOVETO =>
          z = 0
          addTmpVertex(point(0), point(1), z)
          z+=2
        case java.awt.geom.PathIterator.SEG_LINETO =>
          addTmpVertex(point(0), point(1), z)
          z+=2
        case java.awt.geom.PathIterator.SEG_CLOSE =>
          var st = 0
          var end = z
          while(st < end) {
            addVertex(tmpVerts(st), tmpVerts(st+1))           
            st+=2
            addVertex(tmpVerts(end-2), tmpVerts(end-1))
            end-=2
          }
          addVertex(verts(i-2), verts(i-1))
        case _ =>
          System.err.println("PathIterator contract violated")
      }
      path.next
    }
    vertsNum = i/2
    i=0
    ind = 0
  }
  
  private def strokeShape(shape: Shape, noDash: Boolean) {

    val strokePath = new Path2D.Float
    var len = 16.0f // length of stroke element
    var path = shape.getPathIterator(null, flatnessFactor(shape))
    
    if(shape.isInstanceOf[Arc2D] && !noDash){
      val path2: Path2D.Float = new Path2D.Float
      path2.append(shape.getPathIterator(null, 1.0), false)
      path.currentSegment(point)
      if(shape.asInstanceOf[Arc2D].getArcType != Arc2D.OPEN)
        path2.lineTo(point(0), point(1))
      path2.closePath
      path = path2.getPathIterator(null, 1.0)
    }
    
    var prevx = 0.0f
    var prevy = 0.0f
    var next = false // wheater to go to next point form path iterator
    var space = false // wheater is space or stroke
    var tDist= 0.0f // temporary distance from prev point, when dist < len
    var inter = 0 // determine when to start space or stroke
    val dash = stroke.getDashArray
    var f = false

    i = 0
if(!noDash) len = dash(0) + stroke.getDashPhase// stroke
    while(!path.isDone){
      path.currentSegment(point) match {
      case java.awt.geom.PathIterator.SEG_MOVETO =>
          if(noDash){
            addTmpVertex(point(0), point(1), i)
            i+=2
          } else {
            strokePath.moveTo(point(0), point(1))
            inter = 0
            prevx = point(0)
            prevy = point(1)
//            len = dash(0) + stroke.getDashPhase// stroke
            addTmpVertex(point(0), point(1), i)
            i+=2
          }
      case java.awt.geom.PathIterator.SEG_LINETO =>
        if(noDash){
          addTmpVertex(point(0), point(1), i)
          i+=2
        } else {
          while(next == false){
            val dist = scala.Math.sqrt((point(0)-prevx)*(point(0)-prevx) + (point(1)-prevy)*(point(1)-prevy)).toFloat
            if(dist+tDist > len) {
              val x1: Float = point(0) - prevx
              val y1: Float = point(1) - prevy
              val mtp1 = (x1/dist)*(len-tDist) + prevx
              val mtp2 = (y1/dist)*(len-tDist) + prevy
              tDist = 0
              prevx = mtp1
              prevy = mtp2
              // end point
              if(inter%2 == 0) {
                strokePath.lineTo(mtp1.toFloat, mtp2.toFloat)
                len = dash(1) // space
                addTmpVertex(mtp1.toFloat, mtp2.toFloat, i)
                i+=2
              }
              else {
                strokePath.moveTo(prevx, prevy)
                len = dash(0) // stroke
                addTmpVertex(prevx, prevy, i)
                i+=2
              }
              inter += 1
            } else {
              tDist += dist
              prevx = point(0)
              prevy = point(1)
              if(inter%2 == 0){ // dont add when break
                strokePath.lineTo(point(0), point(1)) // middle point
                addTmpVertex(point(0), point(1), i)
                i+=2
              }
              next = true
            }
          }
          if(tDist >= len) tDist= 0.0f
          next=false
        }
        case java.awt.geom.PathIterator.SEG_CLOSE =>
        case _ =>
          System.err.println("PathIterator contract violated")
      }
      path.next
    }
    if(noDash == false) strokePath.closePath

    /*
    var path = shape.getPathIterator(null, flatnessFactor(shape))
    i = 0
    while(!path.isDone){
      var t = path.currentSegment(point)
      t match {
      case java.awt.geom.PathIterator.SEG_MOVETO =>
        addTmpVertex(point(0), point(1), i)
        i+=2
      case java.awt.geom.PathIterator.SEG_LINETO =>
        addTmpVertex(point(0), point(1), i)
        i+=2
      case java.awt.geom.PathIterator.SEG_CLOSE =>
      case _ =>
        System.err.println("PathIterator contract violated")
      }
      path.next
    }
*/
    endInd = i
    vertsNumTmp = i/2
    i=0
    ind = 0
    
      var prevt = 0
      if(noDash)
        path = shape.getPathIterator(null, flatnessFactor(shape))
      else
        path = strokePath.getPathIterator(null, 1.0)
      while(!path.isDone){
        var t = path.currentSegment(point)
        t match {
        case java.awt.geom.PathIterator.SEG_MOVETO =>
          if(ind>0)
            endCapOrCapClose(startInd, false)
          startInd = ind
          moveto(ind)
          ind+=2
          prevt = t
        case java.awt.geom.PathIterator.SEG_LINETO =>
          if (prevt != PathIterator.SEG_MOVETO)
            join(ind)
          lineto(ind)
          ind+=2
          prevt = t
        case java.awt.geom.PathIterator.SEG_CLOSE =>
          endCapOrCapClose(startInd, implicitClose)
        case _ =>
          System.err.println("PathIterator contract violated")
        }
        path.next
      }      
      endCapOrCapClose(startInd, false)
      vertsNum = i/2    
  }

  private def flatnessFactor(shape: Shape):Double = {
    val flatTresh = 40.0 // treshold for decreasing flatness factor
    val size = math.min(shape.getBounds2D.getWidth, shape.getBounds2D.getHeight) - flatTresh
    val linearFactor = 500.0
    val flatMax = 1.0    
    val fac1 = 5.0
    if(size > 0f)
      return flatMax - math.log10(size)/fac1
    else
      return 1.0
  }

  private def addVertex(x: Float, y: Float) {
    // extends array, increase global index
    if(i+2 < fixedArraySize) {
      verts(i) = x
      verts(i+1) = y
      i+=2
    } else { // re-size the array
      val tempArray = new Array[Float](fixedArraySize)
      verts.copyToArray(tempArray)
      fixedArraySize = fixedArraySize + extendArraySize
      println("verts Array resized to: "+fixedArraySize)
      verts = new Array[Float](fixedArraySize)
      tempArray.copyToArray(verts)
      verts(i) = x
      verts(i+1) = y
      i+=2
    }
  }

  private def addTmpVertex(x: Float, y: Float, index: Int) {
    // extends array tmp, does not increase global index
    if(index+2 < fixedArraySize) {
      tmpVerts(index) = x
      tmpVerts(index+1) = y
    } else { // re-size the array
      val tempArray = new Array[Float](fixedArraySize)
      tmpVerts.copyToArray(tempArray)
      fixedArraySize = fixedArraySize + extendArraySize
      println("tmp and verts Array resized to: "+fixedArraySize)
      tmpVerts = new Array[Float](fixedArraySize)
      // most probably verts arry will be at lest same size as tmpVerts array
      verts = new Array[Float](fixedArraySize)
      tempArray.copyToArray(tmpVerts)
      tmpVerts(index) = x
      tmpVerts(index+1) = y
    }
  }
 
  private def lineto(ind: Int) {
    emitLineSeg(tmpVerts(ind), tmpVerts(ind+1), nx, ny)
    curx = tmpVerts(ind)
    cury = tmpVerts(ind+1)
  }

  private def emitLineSeg(x: Float, y: Float, nx: Float, ny: Float) {
    addVertex(x + nx, y + ny)
    addVertex(x - nx, y - ny)
  }

  private def moveto(ind: Int) {
    // normal vector
    val x1: Float = tmpVerts(ind)
    val y1: Float = tmpVerts(ind+1)
    curx = tmpVerts(ind)
    cury = tmpVerts(ind+1)
    val x2: Float = tmpVerts(ind+2)
    val y2: Float = tmpVerts(ind+3)
    val dx: Float = x2 - x1
    val dy: Float = y2 - y1
    var pw: Float = 0.0f

    if (dx == 0.0)
      pw = lineWidth / scala.Math.abs(dy)
    else if (dy == 0.0)
      pw = lineWidth / scala.Math.abs(dx)
    else
      pw = lineWidth / scala.Math.sqrt(dx*dx + dy*dy).toFloat

    nx = -dy * pw
    ny = dx * pw

    stroke.getEndCap match {
      case BasicStroke.CAP_SQUARE =>
        addVertex(curx + nx, cury + ny)
      case BasicStroke.CAP_BUTT =>
        addVertex(curx - ny + nx, cury + nx + ny)
        emitLineSeg(curx - ny, cury + nx, nx, ny)
      case BasicStroke.CAP_ROUND =>
        arcPoints(curx, cury, curx+nx, cury+ny, curx-nx, cury-ny)
        var st = 0
        var end = arcInd        
        addVertex(curx + nx, cury + ny)
        addVertex(curx + nx, cury + ny)
        while(end > st){
          addVertex(arcVerts(st), arcVerts(st+1))
          st += 2
          addVertex(arcVerts(end-2), arcVerts(end-1))
          end -= 2
        }
        addVertex(verts(i-2), verts(i-1))
        addVertex(curx + nx, cury + ny)
    }
    emitLineSeg(curx, cury, nx, ny)
  }

  private def join(ind: Int) {
    // normal vector
    val x1: Float = curx
    val y1: Float = cury
    val x2: Float = tmpVerts(ind)
    val y2: Float = tmpVerts(ind+1)
    val dx: Float = x2 - x1
    val dy: Float = y2 - y1
    var pw: Float = 0.0f

    if (dx == 0)
        pw = lineWidth / math.abs(dy)
    else if (dy == 0)
        pw = lineWidth / math.abs(dx)
    else
      pw = lineWidth / math.sqrt(dx*dx + dy*dy).toFloat

    nx = -dy * pw
    ny = dx * pw

    stroke.getLineJoin match {
      case BasicStroke.JOIN_BEVEL =>
      case BasicStroke.JOIN_MITER =>
        val count = i
        val prevNvx = verts(count-2) - curx
        val prevNvy = verts(count-1) - cury
        val xprod = prevNvx * ny - prevNvy * nx
        var px, py, qx, qy = 0.0

        if(xprod <0 ) {
          px = verts(count-2)
          py = verts(count-1)
          qx = curx - nx
          qy = cury - ny
        } else {
          px = verts(count-4)
          py = verts(count-3)
          qx = curx + nx
          qy = cury + ny
        }

        var pu = px * prevNvx + py * prevNvy
        var qv = qx * nx + qy * ny
        var ix = (ny * pu - prevNvy * qv) / xprod
        var iy = (prevNvx * qv - nx * pu) / xprod

        if ((ix - px) * (ix - px) + (iy - py) * (iy - py) <= miter_limit * miter_limit) {
          addVertex(ix.toFloat, iy.toFloat)
          addVertex(ix.toFloat, iy.toFloat)
        }    
    case BasicStroke.JOIN_ROUND =>
      val prevNvx = verts(i-2) - curx
      val prevNvy = verts(i-1) - cury
      var ii:Int = 0
      if(nx * prevNvy - ny * prevNvx < 0) {
        arcPoints(0, 0, nx, ny, -prevNvx, -prevNvy)
        ii = arcInd / 2
        while( ii > 0 ) {
          emitLineSeg(curx, cury, arcVerts(2*ii - 2), arcVerts(2*ii - 1) )
          ii-=1
        }
      } else {
        arcPoints(0, 0, -prevNvx, -prevNvy, nx, ny)
        ii = 0
        while (ii < arcInd / 2) {
          emitLineSeg(curx, cury, arcVerts(2*ii + 0), arcVerts(2*ii + 1) )
          ii+=1
        }
      }
  }
  emitLineSeg(curx, cury, nx, ny)
  }

  private def arcPoints(cx: Float, cy: Float, fromX: Float, fromY: Float, toX: Float, toY: Float) {
    var dx1: Float = fromX - cx
    var dy1: Float = fromY - cy
    var dx2: Float = toX - cx
    var dy2: Float = toY - cy
    val roundFactor = 15.0

    val sin_theta = math.sin(math.Pi/roundFactor).toFloat
    val cos_theta = math.cos(math.Pi/roundFactor).toFloat
    
    arcInd = 0    
    while (dx1 * dy2 - dx2 * dy1 < 0) {
      val tmpx = dx1 * cos_theta - dy1 * sin_theta
      val tmpy = dx1 * sin_theta + dy1 * cos_theta
      dx1 = tmpx
      dy1 = tmpy
      arcVerts(arcInd) = cx + dx1
      arcVerts(arcInd+1) = cy + dy1
      arcInd+=2      
    }

    while (dx1 * dx2 + dy1 * dy2 < 0) {
      val tmpx = dx1 * cos_theta - dy1 * sin_theta
      val tmpy = dx1 * sin_theta + dy1 * cos_theta
      dx1 = tmpx
      dy1 = tmpy
      arcVerts(arcInd) = cx + dx1
      arcVerts(arcInd+1) = cy + dy1
      arcInd+=2      
    }

    while (dx1 * dy2 - dx2 * dy1 >= 0) {
      val tmpx = dx1 * cos_theta - dy1 * sin_theta
      val tmpy = dx1 * sin_theta + dy1 * cos_theta
      dx1 = tmpx
      dy1 = tmpy
      arcVerts(arcInd) = cx + dx1
      arcVerts(arcInd+1) = cy + dy1
      arcInd+=2      
    }
    //if(arcInd>0) arcInd -= 2
  }

  private def endCapOrCapClose(startInd: Int, implicitClose: Boolean) {
    if(endsAtStart){
      join(startInd+2)
    } else if(implicitClose) {
      join(startInd)
      lineto(startInd)
      join(startInd+2)
    } else {
      endCap()
    }
    addVertex(verts(i-2), verts(i-1))
}

  private def endCap() {
    stroke.getEndCap match {
      case BasicStroke.CAP_SQUARE =>
      case BasicStroke.CAP_BUTT =>
        emitLineSeg(curx+ny, cury-nx, nx, ny)
      case BasicStroke.CAP_ROUND =>
        
        arcPoints(curx, cury, verts(i-2), verts(i-1), verts(i-4), verts(i-3) )
        var front:Int = 1
        var end:Int = (arcInd-2) / 2
        while (front < end) {          
          addVertex(arcVerts(2*end-2), arcVerts(2*end-1))
          end-=1
          if (front < end) {            
            addVertex(arcVerts(2*front), arcVerts(2*front+1))
            front+=1
          }
        }        
        addVertex(verts(i-2), verts(i-1))        
    }
  }

  def clear(c: Color): Unit = {
    gl.glClearColor(c.getRed/255f, c.getGreen/255f, c.getBlue/255f, c.getAlpha/255f)
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT)    
    gl.glEnable(GL.GL_STENCIL_TEST)
    gl.glStencilFunc(GL.GL_ALWAYS, uniqueStencilValue1, 0)
    gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE)
    gl.glColor4f(c.getRed/255f, c.getGreen/255f, c.getBlue/255f, c.getAlpha/255f)
    this.fill(new Rectangle2D.Float(0, 0, 500, 500))
    gl.glDisable(GL.GL_STENCIL_TEST)
  }
  def deinit(): Unit = {
    gl.glDeleteBuffers(1, bufferId, 0)
  }
}