
package graphic

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.media.opengl._

class Line(gl2:GL2) {
  var bufferId : Array[Int] = Array(0)
  var floatSize : Int = 4  
  var bufferData : FloatBuffer = FloatBuffer.allocate(64)
  var bufferByte : ByteBuffer = null
  var verts : Array[Float] = new Array[Float](64)
  var gl : GL2 = gl2
  var vbo : VBuffer = new VBuffer
  var size : Int = 0
  var stroke = 0xFFFF
  var drawType:Int = GL.GL_LINE_STRIP

  vbo.init(gl, bufferId, verts, bufferData, floatSize)

  def draw(width:Float, line : Array[Float], loop:Boolean) = {
    gl.glLineWidth(width)

    assignLine(line)

    if(stroke != 0xFFFF){
      gl.glEnable(GL2.GL_LINE_STIPPLE)
      gl.glLineStipple(1, stroke.shortValue )
    }

    if(loop) drawType = GL.GL_LINE_LOOP
    else drawType = GL.GL_LINE_STRIP

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    vbo.drawBuffer(gl, drawType, size/2 )

    gl.glDisable(GL2.GL_LINE_STIPPLE)
  }
  private def assignLine(line : Array[Float]) = {
     var i = 0;
     while (i<line.size && line.size<=verts.size){
        verts(i) = line(i)
        verts(i+1) = line(i+1)
        i+=2
      }
      size = line.size
  }

  def deinit() = {
    gl.glDeleteBuffers(1, bufferId, 0)
  }
}

