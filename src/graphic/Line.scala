
package graphic

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.media.opengl._

class Line {
  var bufferId : Array[Int] = Array(0)
//  var bufferSize : Int = 0
  var floatSize : Int = 4
  var inited : Boolean = false
  var bufferData : FloatBuffer = FloatBuffer.allocate(64)
  var bufferByte : ByteBuffer = null
  var verts : Array[Float] = new Array[Float](64)
  var gl : GL2 = null
  var vbo : VBuffer = new VBuffer
  var size : Int = 0

  def draw(width:Float, line : Array[Float]) = {
    if(inited==false){
//      assignLine(line)
      vbo.init(gl, bufferId, verts, bufferData, floatSize)
      inited = true;
    }

    gl.glLineWidth(width)

    assignLine(line)

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    vbo.drawBuffer(gl, GL.GL_LINE_STRIP, size/2 )

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

