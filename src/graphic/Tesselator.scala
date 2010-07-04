

package graphic

import javax.media.opengl.glu.GLU
import java.awt.geom.PathIterator
import javax.media.opengl.GL
import javax.media.opengl.glu.gl2.GLUgl2
import tesswarp.Tess

class Tesselator extends Tess(new GLUgl2) {
  var vertsData : Array[Float] = new Array[Float](8192)
  var tempVertsData : Array[Float] = new Array[Float](8192)
  var glu = new GLUgl2
  var mode = 0
  var f = 0 // temporary index
  var i = 0 // index for aray triangle strips output
  
  def startTessPolygon() = {
    this.startTesselationPoly
    i=0
    f=0
  }

  def endTessPolygon() = {
    this.endTesselationPoly
  }

  def startTessContour() = {
    this.startTesselationContour
  }

  def endTessContour() = {
    this.endTesselationContour
  }

  def addVertex(x:Float, y:Float) = {
    val p:Array[Double] = Array(x.doubleValue, y.doubleValue, 0 )
    this.addVert(p)
  }

  def setPathRule(rule:Int) = {
      rule match {
      case PathIterator.WIND_EVEN_ODD =>
        this.setWindingRule(GLU.GLU_TESS_WINDING_ODD)
      case PathIterator.WIND_NON_ZERO =>
        this.setWindingRule(GLU.GLU_TESS_WINDING_NONZERO)
    }
  }

  def setRule(rule:Int) = {
    this.setWindingRule(rule)
  }

  override
  def begin(mode:Int) = {
    this.mode = mode
  }
  
  override
  def end() = {
    // triangle fan - 6
    // triangle strip - 5
    // triangles - 4
    var st:Int = 0
    var end:Int = f
    // triangle fan -> triangle strip
    if(mode == GL.GL_TRIANGLE_FAN) {
      st+=2
      vertsData(i) = tempVertsData(st)
      vertsData(i+1) = tempVertsData(st+1)
      i+=2
      while(st < end){
        vertsData(i) = tempVertsData(st)
        vertsData(i+1) = tempVertsData(st+1)
        i+=2
        st+=2
        vertsData(i) = tempVertsData(0)
        vertsData(i+1) = tempVertsData(1)
        i+=2
        if(st<end){
          vertsData(i) = tempVertsData(st)
          vertsData(i+1) = tempVertsData(st+1)
          i+=2
          st+=2
        }
        if(st<end){
          vertsData(i) = tempVertsData(st)
          vertsData(i+1) = tempVertsData(st+1)
          i+=2
          st+=2
        }
      }
      vertsData(i) = vertsData(i-2)
      vertsData(i+1) = vertsData(i-1)
      i+=2
    }
    // triangle strip-> triangle strip
    if(mode == GL.GL_TRIANGLE_STRIP) {
      vertsData(i) = tempVertsData(st)
      vertsData(i+1) = tempVertsData(st+1)
      i+=2
      while(st < end) {
        vertsData(i) = tempVertsData(st)
        vertsData(i+1) = tempVertsData(st+1)
        st+=2
        i+=2
      }
      vertsData(i) = vertsData(i-2)
      vertsData(i+1) = vertsData(i-1)
      i+=2
    }

    // triangles -> triangle strip
    if(mode == GL.GL_TRIANGLES) {
      var tri = 0
      while(st < end) {
        if(tri==0) {
          vertsData(i) = tempVertsData(st)
          vertsData(i+1) = tempVertsData(st+1)
          i+=2
        }
        vertsData(i) = tempVertsData(st)
        vertsData(i+1) = tempVertsData(st+1)
        st+=2
        i+=2
        tri+=1
        if(tri==3) {
          vertsData(i) = vertsData(i-2)
          vertsData(i+1) = vertsData(i-1)
          i+=2
          tri = 0
        }        
      }
    }
    f = 0
  }
  
  override
  def vertex(vertexData:Any) = {
    var data:Array[Double] = vertexData.asInstanceOf[Array[Double]]
    tempVertsData(f) = data(0).floatValue
    tempVertsData(f+1) = data(1).floatValue
    f+=2
  }
}
