
package graphic

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.media.opengl._

class Triangle {
  var bufferId : Array[Int] = Array(0)
//  var bufferSize : Int = 0
  var floatSize : Int = 4
  var inited : Boolean = false
  var bufferData : FloatBuffer = FloatBuffer.allocate(8)
  var bufferByte : ByteBuffer = null
  var verts : Array[Float] = new Array[Float](6)
  var gl : GL2 = null
  var vbo : VBuffer = new VBuffer

  def draw(coords : Array[Int]) = {
    if(inited==false){
/*      verts = Array(coords(0), coords(1),
                    coords(2), coords(3),
                    coords(4), coords(5));
*/
      vbo.init(gl, bufferId, verts, bufferData, floatSize)
      inited = true;
    }
          verts = Array(coords(0), coords(1),
                    coords(2), coords(3),
                    coords(4), coords(5));

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    vbo.drawBuffer(gl, GL.GL_TRIANGLES, verts.size/2)

  }

  def deinit() = {
    gl.glDeleteBuffers(1, bufferId, 0)
  }
}
