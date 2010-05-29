
package graphic

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.media.opengl._


class Rectangle(gl2:GL2) {
  var bufferId : Array[Int] = Array(0)
  var floatSize : Int = 4
  var bufferData : FloatBuffer = FloatBuffer.allocate(8)
  var bufferByte : ByteBuffer = null
  var verts : Array[Float] = new Array[Float](8)
  var gl : GL2 = gl2
  var vbo : VBuffer = new VBuffer
  var stroke = 0xFFFF

  vbo.init(gl, bufferId, verts, bufferData, floatSize)

  def draw(x : Int, y : Int, w : Int, h : Int) = {

    calcRect(x, y, w, h)

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    vbo.drawBuffer(gl, GL2.GL_QUADS, verts.size/2)

  }

    def drawOutline(x:Int, y:Int, w:Int, h:Int, arcw:Int, arch:Int, width:Float) = {

    calcRect(x, y, w, h)

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    if(stroke != 0xFFFF){
      gl.glEnable(GL2.GL_LINE_STIPPLE)
      gl.glLineStipple(1, stroke.shortValue )
    }
    gl.glLineWidth(width)

    vbo.drawBuffer(gl, GL.GL_LINE_STRIP, verts.size/2)

    gl.glDisable(GL2.GL_LINE_STIPPLE)
  }

  private def calcRect(x:Int, y:Int, w:Int, h:Int) ={
    verts(0) = x; verts(1) = y;
    verts(2) = x+w; verts(3) = y;
    verts(4) = x+w; verts(5) = y+h;
    verts(6) = x; verts(7) = y+h;
  }

  def setStroke(s:Int){
    stroke = s
  }

  def deinit() = {
    gl.glDeleteBuffers(1, bufferId, 0)
  }
}
