package graphic

import java.nio.FloatBuffer
import java.util.ArrayList
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
  protected val bufferId: Array[Int] = Array(0)
  private val floatSize = 4
  private var fixedArraySize = 4096//12210
  private val extendArraySize = 4096 // 4096 = 2048 two coord's verts = 8192 bytes
  private var verts = FloatBuffer.allocate(fixedArraySize)//new Array[Float](fixedArraySize)
  private var tmpVerts = new Array[Float](fixedArraySize)
  protected[graphic] var gl: GL2 = null
  //private val vbo = new VBuffer
  private var vertsNum = 0
  private var totalCountNumber = 0
  private val point = new Array[Float](6)
  private val arcVerts = new Array[Float](180)

  private var gvi: Int = 0 // global vertex index
  private var ind: Int = 0 // index for path outline
  private var nx = 0.0f
  private var ny = 0.0f
  private var curx = 0.0f
  private var cury = 0.0f
  
  private var arcInd = 0
  private var endsAtStart = false
  private var implicitClose = false
  val WIDTH_MIN = 0.25
  val WIDTH_MAX = 20
  private var thinLine = false

  private val uniqueStencilClipValue = 10
  private val uniqueStencilValue1 = 5

  private val shapeStore = new ArrayList[Shape]
  private val vertsStore = new ArrayList[Array[Float]]
  private val TESS_STORE_LIMIT = 6

  private var _stroke = new BasicStroke

  def stroke: BasicStroke = _stroke
  def stroke_=(s: BasicStroke) {
    val w = s.getLineWidth
    var cap = s.getEndCap
    var join = s.getLineJoin
    if(w <= 3) {
      cap = BasicStroke.CAP_SQUARE
      join = BasicStroke.JOIN_BEVEL
      if(w <= 0.75) thinLine = true
      else thinLine = false
    }
    else thinLine = false
    _stroke = if(w > WIDTH_MIN && w < WIDTH_MAX) s
              else new BasicStroke(math.max(w, math.min(WIDTH_MAX, w)), cap, join,
                                   s.getMiterLimit, s.getDashArray, s.getDashPhase)
  }

  private def lineWidth = _stroke.getLineWidth / 2.0f

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
      fillAndDrawBuffer(vertsNum)

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

    // VBO initialization
    gl.glGenBuffers(1, bufferId, 0)
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferId(0))    
    gl.glBufferData(GL.GL_ARRAY_BUFFER, floatSize*fixedArraySize, null, GL.GL_DYNAMIC_DRAW)
    gl.glVertexPointer(2, GL.GL_FLOAT, 0, 0)

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

  def stroke(shape: Shape): Unit = {
    if(shape != null) {
      if(color.getAlpha == 1.0f)
        gl.glDisable(GL.GL_BLEND)
      gl.glEnable(GL.GL_STENCIL_TEST)
      gl.glStencilFunc(GL.GL_EQUAL, uniqueStencilValue1, ~0)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_ZERO)

      if(stroke.getDashArray != null) {
        //strokeShape_1(shape)
        strokeShape(shape, false) // when dash
      } else {
        strokeShape(shape, true) // when no dash
      }

      fillAndDrawBuffer(vertsNum)
      gl.glDisable(GL.GL_STENCIL_TEST)
      if(color.getAlpha == 1.0f)
        gl.glEnable(GL.GL_BLEND)
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
      tessShape(shape, false)
    }
    if(color.getAlpha == 1.0f)
      gl.glDisable(GL.GL_BLEND)
    fillAndDrawBuffer(vertsNum)
    if(color.getAlpha == 1.0f)
      gl.glEnable(GL.GL_BLEND)
  }

  def clipStroke(shape: Shape): Unit = {
    if(shape != null){
      gl.glEnable(GL.GL_STENCIL_TEST)
      gl.glDisable(GL.GL_BLEND)
      gl.glStencilFunc(GL.GL_ALWAYS, uniqueStencilClipValue, ~0)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE)
      gl.glColorMask(false, false, false, false)

    if(stroke.getDashArray != null) // if dash
      strokeShape(shape, false)
    else
      strokeShape(shape, true)

    fillAndDrawBuffer(vertsNum)

      gl.glColorMask(true, true, true, true)
      gl.glStencilFunc(GL.GL_EQUAL, uniqueStencilClipValue, ~0)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_ZERO)
    } else {
      gl.glDisable(GL.GL_STENCIL_TEST)
    }
  }

  override // from tessellation
  def end() {
    var st = 0
    var end = tempTessIndex    
    
    mode match {
      case GL.GL_TRIANGLE_FAN =>
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
            addVertex(verts.get(gvi-2), verts.get(gvi-1))
          }
        }
        addVertex(verts.get(gvi-2), verts.get(gvi-1))

      case GL.GL_TRIANGLE_STRIP =>
        addVertex(tmpVerts(st), tmpVerts(st+1))
        while(st < end) {
          addVertex(tmpVerts(st), tmpVerts(st+1))
          st+=2
        }
        addVertex(verts.get(gvi-2), verts.get(gvi-1))

      case GL.GL_TRIANGLES =>
        var tri = 0
        while(st < end) {
          if(tri==0) {
            addVertex(tmpVerts(st), tmpVerts(st+1))
          }
          addVertex(tmpVerts(st), tmpVerts(st+1))
          st+=2
          tri+=1
          if(tri==3) {
            addVertex(verts.get(gvi-2), verts.get(gvi-1))
            tri = 0
          }
        }
      
      case _ =>
        System.err.println("Tessellation mode error!")
    }
    tempTessIndex = 0
  }

  override // from tessellation
  def vertex(vertexData: Any) {
    val data:Array[Double] = vertexData.asInstanceOf[Array[Double]]
    addTmpVertex(data(0).toFloat, data(1).toFloat, tempTessIndex)
    tempTessIndex+=2
  }

  private def compareShapes(s1: Shape, s2: Shape): Boolean = {
        val point1 = new Array[Float](6)
        val point2 = new Array[Float](6)
        val p1 = s1.getPathIterator(null, 1.0)
        val p2 = s2.getPathIterator(null, 1.0)
        while(!p1.isDone && !p2.isDone){
          p1.currentSegment(point1)
          p2.currentSegment(point2)
          if( !(point1(0) == point2(0) && point1(1) == point2(1)))
            return false
          p1.next
          p2.next
        }
        if(!(p1.isDone && p2.isDone))
          return false
        return true
  }

  private def tessShape(shape: Shape, cache: Boolean)  {
    var toRestore = false
    var shapeInd = -1
    while(toRestore == false && shapeInd<TESS_STORE_LIMIT && cache==true){
      shapeInd+=1
      if(shapeStore.size > shapeInd)
        toRestore = compareShapes(shapeStore.get(shapeInd), shape)
    }

    if(toRestore && cache==true){
      verts.position(0)
        verts.put(vertsStore.get(shapeInd))
        gvi = vertsStore.get(shapeInd).size
        tempTessIndex = 0
    } else {
    val path = shape.getPathIterator(null, flatnessFactor(shape))
    this.setWindRule(path.getWindingRule)
    this.startTessPolygon
    //tess.startTessContour
    gvi = 0
    while(!path.isDone){
      path.currentSegment(point) match {
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

      if(shapeStore.size <= TESS_STORE_LIMIT && cache==true){
        shapeStore.add(shape)
        val tessArray = new Array[Float](gvi)
        verts.array.copyToArray(tessArray, 0, gvi)
        vertsStore.add(tessArray)
      }
      //val z = new Array[Float](i)
      //verts.copyToArray(z, 0, i)
      //vertsStore.add(z)

    }

    vertsNum = gvi/2
    gvi = 0
    ind = 0
  }

  private def strokeShape_1(shape: Shape) {
    triangulateConvexPath(stroke.createStrokedShape(shape).getPathIterator(null, 1.0f))
  }

  private def triangulateConvexPath(path: PathIterator) {
    var z = 0
    gvi = 0
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
          //addVertex(verts(i-2), verts(i-1))
          addVertex(verts.get(gvi-2), verts.get(gvi-1))
        case _ =>
          System.err.println("PathIterator contract violated")
      }
      path.next
    }
    vertsNum = gvi/2
    gvi = 0
    ind = 0
  }

  private def strokeShape(shape: Shape, noDash: Boolean) {

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

    val strokePath = new Path2D.Float
    var len = 10.0f // lenght of stroke segment
    var prevx = 0.0f
    var prevy = 0.0f
    var next = false // wheater to go to next point form path iterator
    var space = false // wheater is space or stroke
    var tDist= 0.0f // temporary distance from prev point, when dist < len
    var inter = 0 // determine when to start space or stroke
    val dash = stroke.getDashArray
    var f = false

    gvi=0

    if(!noDash) {
      //len = dash(0) + stroke.getDashPhase// stroke
      len = stroke.getDashPhase % (dash(0)+dash(1))
      if(len <= dash(0)){
        inter = 0 // stroke
      } else {
        inter = 1
        len = len - dash(0) // break
      }
    }
    while(!path.isDone){
      path.currentSegment(point) match {
      case java.awt.geom.PathIterator.SEG_MOVETO =>
          if(noDash){
            addTmpVertex(point(0), point(1), gvi)
            gvi += 2
          } else {
            if(inter%2 == 0){
              strokePath.moveTo(point(0), point(1))
//            inter = 0
              addTmpVertex(point(0), point(1), gvi)
              gvi += 2
            }
            prevx = point(0)
            prevy = point(1)
          }
      case java.awt.geom.PathIterator.SEG_LINETO =>
        if(noDash){
          addTmpVertex(point(0), point(1), gvi)
          gvi += 2
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
                //if(first == true)
                  strokePath.lineTo(mtp1.toFloat, mtp2.toFloat)
                len = dash(1) // space
                addTmpVertex(mtp1.toFloat, mtp2.toFloat, gvi)
                gvi += 2
              }
              else {
                //first = true
                strokePath.moveTo(prevx, prevy)
                len = dash(0) // stroke
                addTmpVertex(prevx, prevy, gvi)
                gvi += 2
              }
              inter += 1
            } else {
              tDist += dist
              prevx = point(0)
              prevy = point(1)
              if(inter%2 == 0){ // dont add when break
                strokePath.lineTo(point(0), point(1)) // middle point
                addTmpVertex(point(0), point(1), gvi)
                gvi += 2
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

    gvi = 0
    ind = 0

    var prevt = 0
    var startInd = 0
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
          if (prevt != PathIterator.SEG_MOVETO && thinLine == false)
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
      vertsNum = gvi/2
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
    if(gvi+2 < fixedArraySize) {      
      verts.put(x)
      verts.put(y)
      gvi += 2
    } else { // re-size the array      
      fixedArraySize = fixedArraySize + extendArraySize
      val tmpVertsBuffer = FloatBuffer.allocate(fixedArraySize)
      tmpVertsBuffer.put(verts.array, 0, gvi)
      verts = tmpVertsBuffer
      verts.position(gvi)
      verts.put(x)
      verts.put(y)
      gvi += 2
      resizeVBO
      println("Verts Array resized to: "+fixedArraySize+" elements")
      println("VBO resized to: "+fixedArraySize*4+" bytes")
    }
  }

  private def addTmpVertex(x: Float, y: Float, index: Int) {    
    if(index+2 < fixedArraySize) {
      tmpVerts(index) = x
      tmpVerts(index+1) = y
    } else { // extends array tmp, does not increase global index
      val tempArray = new Array[Float](fixedArraySize)
      tmpVerts.copyToArray(tempArray)
      fixedArraySize = fixedArraySize + extendArraySize
      println("tmp verts Array resized to: "+fixedArraySize)
      tmpVerts = new Array[Float](fixedArraySize)
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
        //addVertex(verts(i-2), verts(i-1))
        addVertex(verts.get(gvi-2), verts.get(gvi-1))
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
        val count = gvi
        val prevNvx = verts.get(count-2) - curx
        val prevNvy = verts.get(count-1) - cury
        val xprod = prevNvx * ny - prevNvy * nx
        var px, py, qx, qy = 0.0

        if(xprod <0 ) {
          px = verts.get(count-2)
          py = verts.get(count-1)
          qx = curx - nx
          qy = cury - ny
        } else {
          px = verts.get(count-4)
          py = verts.get(count-3)
          qx = curx + nx
          qy = cury + ny
        }

        var pu = px * prevNvx + py * prevNvy
        var qv = qx * nx + qy * ny
        var ix = (ny * pu - prevNvy * qv) / xprod
        var iy = (prevNvx * qv - nx * pu) / xprod

        if ((ix - px) * (ix - px) + (iy - py) * (iy - py) <= stroke.getMiterLimit * stroke.getMiterLimit) {
          addVertex(ix.toFloat, iy.toFloat)
          addVertex(ix.toFloat, iy.toFloat)
        }
    case BasicStroke.JOIN_ROUND =>
      val prevNvx = verts.get(gvi-2) - curx
      val prevNvy = verts.get(gvi-1) - cury
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
    //addVertex(verts(i-2), verts(i-1))
    addVertex(verts.get(gvi-2), verts.get(gvi-1))
}

  private def endCap() {
    stroke.getEndCap match {
      case BasicStroke.CAP_SQUARE =>
      case BasicStroke.CAP_BUTT =>
        emitLineSeg(curx+ny, cury-nx, nx, ny)
      case BasicStroke.CAP_ROUND =>

        arcPoints(curx, cury, verts.get(gvi-2), verts.get(gvi-1), verts.get(gvi-4), verts.get(gvi-3) )
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
        addVertex(verts.get(gvi-2), verts.get(gvi-1))
    }
  }

  def clear(c: Color): Unit = {
    verts.position(0)
    totalCountNumber = 0
    gvi = 0

    gl.glClearColor(c.getRed/255f, c.getGreen/255f, c.getBlue/255f, c.getAlpha/255f)
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT)
    gl.glEnable(GL.GL_STENCIL_TEST)
    gl.glDisable(GL.GL_BLEND)
    gl.glStencilFunc(GL.GL_ALWAYS, uniqueStencilValue1, 0)
    gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE)
    gl.glColor4f(c.getRed/255f, c.getGreen/255f, c.getBlue/255f, c.getAlpha/255f)
    this.fill(new Rectangle2D.Float(0, 0, 500, 500))
    gl.glDisable(GL.GL_STENCIL_TEST)
    gl.glEnable(GL.GL_BLEND)
  }

  def deinit(): Unit = {
    gl.glDeleteBuffers(1, bufferId, 0)
  }

  private def resizeVBO() {
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferId(0))
    gl.glVertexPointer(2, GL.GL_FLOAT, 0, 0)
    gl.glBufferData(GL.GL_ARRAY_BUFFER, floatSize*fixedArraySize, null, GL.GL_DYNAMIC_DRAW)
  }

  private def fillAndDrawBuffer(count: Int) {
/*
// buffer mapping
//    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferId(0))
//    gl.glBufferData(GL.GL_ARRAY_BUFFER, 4*5210, null, GL.GL_STATIC_DRAW)
    val byteBuffer = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, javax.media.opengl.GL.GL_WRITE_ONLY)
    if(byteBuffer != null) {
      bufferData = (byteBuffer.order(ByteOrder.nativeOrder())).asFloatBuffer
      bufferData.put(verts.array, 0, count*2)
      //bufferData.rewind()
    }
    verts.position(0)
    if( gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER) == false ) {
      System.err.println("Error mapping VBO, error code: "+gl.glGetError)
    }
    gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, count)
*/
    verts.position(0)
//    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferId(0))
/*
    gl.glBufferData(GL.GL_ARRAY_BUFFER, 8*count, verts, GL.GL_DYNAMIC_DRAW)
    gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, count)
*/
    gl.glVertexPointer(2, GL.GL_FLOAT, 0, 0)
    gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 4*verts.position, count*8, verts)
//    gl.glVertexPointer(2, GL.GL_FLOAT, 0, 0)
    gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, count)
/*
    gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 8*totalCountNumber, count*8, verts)
//    gl.glVertexPointer(2, GL.GL_FLOAT, 0, 0)
    gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, totalCountNumber, count)
    totalCountNumber += count
*/
  }
}