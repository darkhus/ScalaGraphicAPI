
package graphic

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.media.opengl._


class Rectangle {
  var bufferId : Array[Int] = Array(0)
//  var bufferSize : Int = 0
  var floatSize : Int = 4
  var inited : Boolean = false
  var bufferData : FloatBuffer = FloatBuffer.allocate(8)
  var bufferByte : ByteBuffer = null
  var verts : Array[Float] = new Array[Float](8)
  var gl : GL2 = null
  var vbo : VBuffer = new VBuffer

  def draw(x : Int, y : Int, w : Int, h : Int) = {
    if(inited==false){
/*      verts = Array(x, y,
                    x+w, y,
                    x+w, y+h,
                    x, y+h);
      */
      vbo.init(gl, bufferId, verts, bufferData, floatSize)

      inited = true;
    }

    verts(0) = x; verts(1) = y;
    verts(2) = x+w; verts(3) = y;
    verts(4) = x+w; verts(5) = y+h;        
    verts(6) = x; verts(7) = y+h;

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    vbo.drawBuffer(gl, GL2.GL_QUADS, verts.size/2)

  }

  def deinit() = {
    gl.glDeleteBuffers(1, bufferId, 0)
  }
}
