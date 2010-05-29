
package graphic

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.media.opengl._
import java.awt.geom.QuadCurve2D

class Curve(gl2:GL2) {
  var bufferId : Array[Int] = Array(0)
  var floatSize : Int = 4  
  var bufferData : FloatBuffer = FloatBuffer.allocate(362)
  var bufferByte : ByteBuffer = null
  var verts : Array[Float] = new Array[Float](362)
  var gl : GL2 = gl2
  var vbo : VBuffer = new VBuffer
  var vertsNum:Int = 360
  var curve:QuadCurve2D = new QuadCurve2D.Float
  var stroke = 0x00FF
  var drawType:Int = GL.GL_LINE_STRIP

  vbo.init(gl, bufferId, verts, bufferData, floatSize)

  def draw(x1:Int, y1:Int, x2:Int, y2:Int, ctrx:Int, ctry:Int) = {

    calcCurve(x1, y1, x2, y2, ctrx, ctry)

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    vbo.drawBuffer(gl, GL2.GL_POLYGON, vertsNum)   
  }

  def drawOutline(x1:Int, y1:Int, x2:Int, y2:Int, ctrx:Int, ctry:Int, width:Float, loop:Boolean) = {

    calcCurve(x1, y1, x2, y2, ctrx, ctry)

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    if(stroke != 0xFFFF){
      gl.glEnable(GL2.GL_LINE_STIPPLE)
      gl.glLineStipple(1, stroke.shortValue )
    }
    gl.glLineWidth(width)

    if(loop) drawType = GL.GL_LINE_LOOP
    else drawType = GL.GL_LINE_STRIP

    vbo.drawBuffer(gl, drawType, vertsNum)

    gl.glDisable(GL2.GL_LINE_STIPPLE)
  }

  private def calcCurve(x1:Int, y1:Int, x2:Int, y2:Int, ctrx:Int, ctry:Int) = {
    curve.setCurve(x1, y1, ctrx, ctry, x2, y2)
    var path = curve.getPathIterator(null, 1.5)
    var point = new Array[Float](8)
    var i = 0
    while(!path.isDone){
      var t = path.currentSegment(point)
        verts(i) = point(0)
        verts(i+1) = point(1)
        i+=2        
      path.next
    }    
    vertsNum = i/2
  }

  def setStroke(s:Int){
    stroke = s
  }

  def deinit() = {
    gl.glDeleteBuffers(1, bufferId, 0)
  }
}

