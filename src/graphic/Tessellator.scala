
package graphic

import javax.media.opengl.glu.{GLU, GLUtessellatorCallback}
import javax.media.opengl.GL
import java.awt.Shape
import java.awt.geom.PathIterator

class Tessellator(builder: GeometryBuilder) extends GLUtessellatorCallback {
  import builder._
  private val point = new Array[Float](6)
  protected var mode = 0
  private val tobj = javax.media.opengl.glu.GLU.gluNewTess  
  
  GLU.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_POSITIVE)
  GLU.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, this)
  GLU.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, this)
  GLU.gluTessCallback(tobj, GLU.GLU_TESS_END, this)
  GLU.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, this)
  GLU.gluTessCallback(tobj, GLU.GLU_TESS_COMBINE, this)
  
  protected def startTessPolygon() {
    GLU.gluTessBeginPolygon(tobj, null)
    tempCoords.rewind()
  }

  protected def endTessPolygon() {
    GLU.gluTessEndPolygon(tobj)
  }

  protected def startTessContour() {
    GLU.gluTessBeginContour(tobj)
    tempCoords.rewind()
  }

  protected def endTessContour() {
    GLU.gluTessEndContour(tobj)
  }

  protected def addVertexToTess(x: Float, y: Float) {
    val p = Array[Double](x.toDouble, y.toDouble, 0)
    GLU.gluTessVertex(tobj, p, 0, p)
  }

  protected def setWindRule(rule: Int) {
    rule match {
      case PathIterator.WIND_EVEN_ODD =>
        GLU.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD)
      case PathIterator.WIND_NON_ZERO =>
        GLU.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NONZERO)
    }    
  }

  override def begin(mode: Int) {
    this.mode = mode
  }
  
  // TODO: why don't we get rid of the temp array and propagate the primtive mode to the 
  // glDrawArray call in GLCanvas

  /**
   * My answer:
   * GLU tessellation seems to be designed for immidiate mode only. So as I have investigated it prodeuce
   * polygons which are triangle fan, triangle strip, triangles. I cant see better
   * solution to tessellate shape with glu (and use vbo) than capture verts (produced by glu
   * which are in sequence of one of above mode), then turn it into triangle strips sequences. To do
   * this there has to be the temp array which store tessellated verts in differents triangles' structures
   * for further conversion.
   * Get rid of the temp array and propagate the primtive mode would slow down rendering, as it would has to call
   * many times glDrawArray. So I think that better is to convert everything to triangle strips and render
   * by one glDrawArray.
   */
  override def end() {
    var st = 0
    var end = tempCoords.size
    def addCurrent() = coords += (tempCoords(st), tempCoords(st+1))
        
    mode match {
      case GL.GL_TRIANGLE_FAN =>
        st=2
        addCurrent()
        while(st < end) {
          addCurrent()
          st+=2
          coords += (tempCoords(0), tempCoords(1))
          if(st<end) {
            addCurrent()
            st+=2
          }
          if(st<end) {
            addCurrent()
            st+=2
            coords.repeatLast
          }
        }
        coords.repeatLast
      case GL.GL_TRIANGLE_STRIP =>
        addCurrent()
        while(st < end) {
          addCurrent()
          st+=2
        }
        coords.repeatLast
      case GL.GL_TRIANGLES =>
        var tri = 0
        while(st < end) {
          if(tri==0) addCurrent()
          addCurrent()
          st+=2
          tri+=1
          if(tri==3) {
            coords.repeatLast
            tri = 0
          }
        }
      case _ =>
        System.err.println("Tessellation mode error!")
    }
    tempCoords.rewind()//tempTessIndex = 0
  }
  
  override def vertex(vertexData: Any) {
    val data = vertexData.asInstanceOf[Array[Double]]
    tempCoords += (data(0).toFloat, data(1).toFloat)
    //coords += (data(0).toFloat, data(1).toFloat)
  }


  override def vertexData(vertexData: Any, polygonData: Any) {
    //println("vertex data")
  }

  override def combine(coords: Array[Double], data: Array[java.lang.Object], weight: Array[Float], outData: Array[java.lang.Object]){
    outData(0) = coords
  }

  override def combineData(coords: Array[Double], data: Array[java.lang.Object],
      weight: Array[Float], outData: Array[java.lang.Object], polygonData: Any) {
    //println("combine data")
  }

  override def error(errnum: Int) {
    System.err.println("Tessellation Error: " + new GLU().gluErrorString(errnum)+"\n"+
                       "Hint: Path's lineTo must follow moveTo, path must be closed.")
  }
  override def beginData(i: Int, o: Any) {
    //println("beginData")
  }

  override def edgeFlag(bln: Boolean) {
    //println("edgeFlag")
  }

  override def edgeFlagData(bln: Boolean, o: Any) {
    //println("edgeFlagData")
  }

  override def endData(o: Any) {
    //println("endData")
  }

  override def errorData(i: Int, o: Any) {
    //println("errorData")
  }
  
  def tessellate(shape: Shape)  {
    val path = shape.getPathIterator(null, flatnessFactor(shape))
    builder.rewind()
    this.setWindRule(path.getWindingRule)
    this.startTessPolygon
      var prevSeg = -1
      var closed = false
      while(!path.isDone){
        val seg = path.currentSegment(point)
        seg match {
          case java.awt.geom.PathIterator.SEG_MOVETO =>
            if(prevSeg != -1 && closed == false)
              this.endTessContour
            this.startTessContour
            closed = false
            this.addVertexToTess(point(0), point(1))
            prevSeg = seg
          case java.awt.geom.PathIterator.SEG_LINETO =>
            this.addVertexToTess(point(0), point(1))
            prevSeg = seg
          case java.awt.geom.PathIterator.SEG_CLOSE =>
            this.endTessContour
            closed = true
            prevSeg = seg
          case _ =>
            System.err.println("PathIterator contract violated")
        }
        path.next
      }
      if(prevSeg != -1 && closed == false)
        this.endTessContour
      this.endTessPolygon
  }

  /**
   * Tessellate a given shape which is assumed to be convex. The result is stored in the builder's
   * coordinate store.
   * 
   * The result of this method is undefined for non-convex shapes and may throw an exception.
   */

  /**
   * My answer:
   * It is not right name. Before it was: triangulateConvexPath(path: PathIterator),
   * where convex shape was triangulated, because as you wrote it is no sense to tessalate convex; Triangulation is
   * much faster that tessellation, that why I have used it to create triangle strips for
   * primitive shapes like Ellipse2D. Please look at previous fill(shape: Shape) method, there is simple test
   * for convex shapes, the rest ones are assumed to be a concave and are tessellated.
   * (In this case trianglulation means trianglultion to triangle strips)
   * Before I have also used it to get stroke by using BasicStroke's method createStrokedShape.
   */
  def tessellateConvex(shape: Shape) {
    val iter = shape.getPathIterator(null, flatnessFactor(shape))
    
    builder.rewind()
    val contiguous = builder.tempCoords.rewind
    while(!iter.isDone) {
      iter.currentSegment(point) match {
        case PathIterator.SEG_MOVETO =>
          contiguous.rewind += (point(0), point(1))
        case PathIterator.SEG_LINETO =>
          contiguous += (point(0), point(1))
        case PathIterator.SEG_CLOSE =>
          var st = 0
          var end = tempCoords.size
          while(st < end) {
            coords += (contiguous(st), contiguous(st+1))
            st+=2
            coords += (contiguous(end-2), contiguous(end-1))
            end-=2
          }
          // TODO @dariusz: I commented this out. Why would we need it?

          /**
           * My answer:
           * This is important, becouse it closes trianangle strips structure, if we would like to
           * add to buffer another shape's vertices strigt after above one without this additional vertex,
           * then opengl would treat two different triangle strips sequences as one sequence.
           * I have add this vertex as mark for opengl to prevent such situations. Anyway nothing will be
           * changed when this shape structure will not be closed, becouse only one triangle strip sequence
           * is draw per shape (exception it tessellation's triangle strips sequence), but I have add it to
           * for sure close structure to prevent possible rendering bugs, and for test when loading all (or group)
           * shapes's traingle strips sequence to buffer and display it at once.
           */
          //addVertex(coord(gvi-2), coord(gvi-1)) 
        case _ =>
          Predef.error("PathIterator contract violated")
      }
      iter.next
    }
  }
}
