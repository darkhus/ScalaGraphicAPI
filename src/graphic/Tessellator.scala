

package graphic

import javax.media.opengl.glu.GLU
import java.awt.geom.PathIterator
import javax.media.opengl.GL
import javax.media.opengl.glu.GLUtessellatorCallback
//import javax.media.opengl.glu.gl2.GLUgl2


trait Tessellator extends GLUtessellatorCallback {
  var vertsData = new Array[Float](8192)
  var tempVertsData = new Array[Float](8192)
  private var glu = new javax.media.opengl.glu.GLU
  var mode = 0
  var tempTessIndex = 0 // temporary index
  private var tobj =  javax.media.opengl.glu.GLU.gluNewTess
  var tessIndex = 0 // index for aray triangle strips output
  //private GLU glu;  

  var WINDING_POSITIVE = GLU.GLU_TESS_WINDING_POSITIVE
  
  javax.media.opengl.glu.GLU.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_POSITIVE)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, this)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, this)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_END, this)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, this)
  javax.media.opengl.glu.GLU.gluTessCallback(tobj, GLU.GLU_TESS_COMBINE, this)
  
  protected def startTessPolygon() {
    //this.startTesselationPoly
    GLU.gluTessBeginPolygon(tobj, null);
    tessIndex = 0
    tempTessIndex = 0
  }

  protected def endTessPolygon() {
    //this.endTesselationPoly
    GLU.gluTessEndPolygon(tobj);
  }

  protected def startTessContour() {
    //this.startTesselationContour
    GLU.gluTessBeginContour(tobj);
  }

  protected def endTessContour() {
    //this.endTesselationContour
    GLU.gluTessEndContour(tobj);
  }

  protected def addVertexToTess(x: Float, y: Float) {
    val p: Array[Double] = Array(x.toDouble, y.toDouble, 0)
    GLU.gluTessVertex(tobj, p, 0, p);
    //this.addVert(p)
  }

  protected def setWindRule(rule: Int) {
      rule match {
      case PathIterator.WIND_EVEN_ODD =>
        GLU.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD)
      case PathIterator.WIND_NON_ZERO =>
        GLU.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NONZERO)
    }    
  }

  def setRule(rule: Int) {
    GLU.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, rule)
  }

  override
  def begin(mode: Int) {
    this.mode = mode
  }
  
  override
  def end() {
    /*
    // triangle fan - 6
    // triangle strip - 5
    // triangles - 4
    var st = 0
    var end = tempTessIndex
    // triangle fan -> triangle strip
    if(mode == GL.GL_TRIANGLE_FAN) {
      st+=2
      vertsData(tessIndex) = tempVertsData(st)
      vertsData(tessIndex+1) = tempVertsData(st+1)
      tessIndex+=2
      while(st < end){
        vertsData(tessIndex) = tempVertsData(st)
        vertsData(tessIndex+1) = tempVertsData(st+1)
        tessIndex+=2
        st+=2
        vertsData(tessIndex) = tempVertsData(0)
        vertsData(tessIndex+1) = tempVertsData(1)
        tessIndex+=2
        if(st<end){
          vertsData(tessIndex) = tempVertsData(st)
          vertsData(tessIndex+1) = tempVertsData(st+1)
          tessIndex+=2
          st+=2
        }
        if(st<end){
          vertsData(tessIndex) = tempVertsData(st)
          vertsData(tessIndex+1) = tempVertsData(st+1)
          tessIndex+=2
          st+=2
        }
      }
      vertsData(tessIndex) = vertsData(tessIndex-2)
      vertsData(tessIndex+1) = vertsData(tessIndex-1)
      tessIndex+=2
    }
    // triangle strip-> triangle strip
    if(mode == GL.GL_TRIANGLE_STRIP) {
      vertsData(tessIndex) = tempVertsData(st)
      vertsData(tessIndex+1) = tempVertsData(st+1)
      tessIndex+=2
      while(st < end) {
        vertsData(tessIndex) = tempVertsData(st)
        vertsData(tessIndex+1) = tempVertsData(st+1)
        st+=2
        tessIndex+=2
      }
      vertsData(tessIndex) = vertsData(tessIndex-2)
      vertsData(tessIndex+1) = vertsData(tessIndex-1)
      tessIndex+=2
    }

    // triangles -> triangle strip
    if(mode == GL.GL_TRIANGLES) {
      var tri = 0
      while(st < end) {
        if(tri==0) {
          vertsData(tessIndex) = tempVertsData(st)
          vertsData(tessIndex+1) = tempVertsData(st+1)
          tessIndex+=2
        }
        vertsData(tessIndex) = tempVertsData(st)
        vertsData(tessIndex+1) = tempVertsData(st+1)
        st+=2
        tessIndex+=2
        tri+=1
        if(tri==3) {
          vertsData(tessIndex) = vertsData(tessIndex-2)
          vertsData(tessIndex+1) = vertsData(tessIndex-1)
          tessIndex+=2
          tri = 0
        }        
      }
    }
    tempTessIndex = 0
    */
  }
  
  override
  def vertex(vertexData: Any) {
    /*
    var data:Array[Double] = vertexData.asInstanceOf[Array[Double]]
    tempVertsData(tempTessIndex) = data(0).toFloat
    tempVertsData(tempTessIndex+1) = data(1).toFloat
    tempTessIndex+=2
    */
  }

  override
  def vertexData(vertexData: Any, polygonData: Any) {
        //System.out.println("vertex data");
    }

  override
  def combine(coords: Array[Double], data: Array[java.lang.Object], weight: Array[Float], outData: Array[java.lang.Object]){
      outData(0) = coords
    }

  override
  def combineData(coords: Array[Double], data: Array[java.lang.Object],
        weight: Array[Float], outData: Array[java.lang.Object], polygonData: Any) {
        //System.out.println("combine data");
    }

  override
  def error(errnum: Int) {
        //error("Tessellation Error: ")// + GLU.gluErrorString(errnum))
        System.err.println("Tessellation Error")
    }
  override
  def beginData(i: Int, o: Any) {
        //System.out.println("beginData");
    }

  override
  def edgeFlag(bln: Boolean) {
        //System.out.println("edgeFlag");
    }

  override
  def edgeFlagData(bln: Boolean, o: Any) {
        //System.out.println("edgeFlagData");
    }

  override
  def endData(o: Any) {
        //System.out.println("endData");
    }

  override
  def errorData(i: Int, o: Any) {
        //System.out.println("errorData");
    }
}
