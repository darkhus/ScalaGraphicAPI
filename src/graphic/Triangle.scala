
package graphic

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.media.opengl._

class Triangle(gl2:GL2) {
  var bufferId : Array[Int] = Array(0)
  var floatSize : Int = 4
  var bufferData : FloatBuffer = FloatBuffer.allocate(8)
  var bufferByte : ByteBuffer = null
  var verts : Array[Float] = new Array[Float](6)
  var gl : GL2 = gl2
  var vbo : VBuffer = new VBuffer
  var stroke = 0x00FF

  vbo.init(gl, bufferId, verts, bufferData, floatSize)

  def draw(coords : Array[Int]) = {

    assignTriangle(coords)

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    vbo.drawBuffer(gl, GL.GL_TRIANGLES, verts.size/2)

  }

  def drawOutline(coords : Array[Int], width:Int) = {

    assignTriangle(coords)

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    if(stroke != 0xFFFF){
      gl.glEnable(GL2.GL_LINE_STIPPLE)
      gl.glLineStipple(1, stroke.shortValue )
    }
    gl.glLineWidth(width)

    vbo.drawBuffer(gl, GL.GL_LINE_LOOP, 3)

    gl.glDisable(GL2.GL_LINE_STIPPLE)
  }

  private def assignTriangle(coords : Array[Int]){
          verts = Array(coords(0), coords(1),
                    coords(2), coords(3),
                    coords(4), coords(5));
  }

  def setStroke(s:Int){
    stroke = s
  }

  def deinit() = {
    gl.glDeleteBuffers(1, bufferId, 0)
  }
}
