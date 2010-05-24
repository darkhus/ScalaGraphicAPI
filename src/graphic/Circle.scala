
package graphic

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.media.opengl._
import java.lang.Math

class Circle {
  var bufferId : Array[Int] = Array(0)
//  var bufferSize : Int = 0
  var floatSize : Int = 4
  var inited : Boolean = false
  var bufferData : FloatBuffer = FloatBuffer.allocate(360)
  var bufferByte : ByteBuffer = null
  var verts : Array[Float] = new Array[Float](360)
  var gl : GL2 = null
  var vbo : VBuffer = new VBuffer

  def draw(x:Int, y:Int, r:Int) = {
    if(inited==false){
//     calcEllipse(x, y, r, r)
      vbo.init(gl, bufferId, verts, bufferData, floatSize)
      inited = true;
    }

    calcEllipse(x, y, r, r)

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    vbo.drawBuffer(gl, GL2.GL_POLYGON, verts.size/2)
  }

  private def calcEllipse(x:Int, y:Int, w:Int, h:Int) = {
     var i = 0;
      while (i<360){
        verts(i) = w * Math.cos( Math.toRadians(i.doubleValue) ).floatValue + x ;
        verts(i+1) = h * Math.sin( Math.toRadians(i.doubleValue) ).floatValue + y;
        i+=2
      }
  }

  def deinit() = {
    gl.glDeleteBuffers(1, bufferId, 0)
  }
}

