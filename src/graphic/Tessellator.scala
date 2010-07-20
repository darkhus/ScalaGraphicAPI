
package graphic

import javax.media.opengl.glu.GLU
import java.awt.geom.PathIterator
import javax.media.opengl.glu.GLUtessellatorCallback


trait Tessellator extends GLUtessellatorCallback {  
  protected var mode = 0
  protected var tempTessIndex = 0 // temporary index
  private var tobj =  javax.media.opengl.glu.GLU.gluNewTess  
  
  javax.media.opengl.glu.GLU.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_POSITIVE)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, this)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, this)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_END, this)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, this)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_COMBINE, this)
  
  protected def startTessPolygon() {
    GLU.gluTessBeginPolygon(tobj, null)    
    tempTessIndex = 0
  }

  protected def endTessPolygon() {
    GLU.gluTessEndPolygon(tobj)
  }

  protected def startTessContour() {
    GLU.gluTessBeginContour(tobj)
  }

  protected def endTessContour() {
    GLU.gluTessEndContour(tobj)
  }

  protected def addVertexToTess(x: Float, y: Float) {
    val p: Array[Double] = Array(x.toDouble, y.toDouble, 0)
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

  override
  def begin(mode: Int) {
    this.mode = mode
  }
  
  override
  def end()  
  
  override
  def vertex(vertexData: Any)

  override
  def vertexData(vertexData: Any, polygonData: Any) {
        //println("vertex data")
    }

  override
  def combine(coords: Array[Double], data: Array[java.lang.Object], weight: Array[Float], outData: Array[java.lang.Object]){
      outData(0) = coords
    }

  override
  def combineData(coords: Array[Double], data: Array[java.lang.Object],
        weight: Array[Float], outData: Array[java.lang.Object], polygonData: Any) {
        //println("combine data")
    }

  override
  def error(errnum: Int) {
        //error("Tessellation Error: ")// + GLU.gluErrorString(errnum))
        System.err.println("Tessellation Error")
    }
  override
  def beginData(i: Int, o: Any) {
        //println("beginData")
    }

  override
  def edgeFlag(bln: Boolean) {
        //println("edgeFlag")
    }

  override
  def edgeFlagData(bln: Boolean, o: Any) {
        //println("edgeFlagData")
    }

  override
  def endData(o: Any) {
        //println("endData")
    }

  override
  def errorData(i: Int, o: Any) {
        //println("errorData")
    }
}
