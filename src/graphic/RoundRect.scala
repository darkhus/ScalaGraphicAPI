
package graphic

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.media.opengl._
import java.awt.geom.RoundRectangle2D

class RoundRect(gl2:GL2) {
  var bufferId : Array[Int] = Array(0)
  var floatSize : Int = 4
  var bufferData : FloatBuffer = FloatBuffer.allocate(362)
  var bufferByte : ByteBuffer = null
  var verts : Array[Float] = new Array[Float](362)
  var gl : GL2 = gl2
  var vbo : VBuffer = new VBuffer
  var vertsNum:Int = 180
  var point = new Array[Float](8)
  var rect:RoundRectangle2D = new RoundRectangle2D.Float()
  var stroke = 0x00FF

  vbo.init(gl, bufferId, verts, bufferData, floatSize)

  def draw(x:Int, y:Int, w:Int, h:Int, arcw:Int, arch:Int) = {

    calcRect(x, y, w, h, arcw, arch)

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    vbo.drawBuffer(gl, GL2.GL_POLYGON, vertsNum)    
  }

  def drawOutline(x:Int, y:Int, w:Int, h:Int, arcw:Int, arch:Int, width:Float) = {

    calcRect(x, y, w, h, arcw, arch)

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    if(stroke != 0xFFFF){
      gl.glEnable(GL2.GL_LINE_STIPPLE)
      gl.glLineStipple(1, stroke.shortValue )
    }
    gl.glLineWidth(width)

    vbo.drawBuffer(gl, GL.GL_LINE_STRIP, vertsNum)

    gl.glDisable(GL2.GL_LINE_STIPPLE)
  }

  private def calcRect(x:Int, y:Int, w:Int, h:Int, arcw:Int, arch:Int) = {
    rect.setRoundRect(x, y, w, h, arcw, arch)
    var path = rect.getPathIterator(null, 1.0f)    
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

