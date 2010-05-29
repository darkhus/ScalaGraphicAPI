
package graphic

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.media.opengl._
import java.lang.Math

class Circle(gl2:GL2) {
  var bufferId : Array[Int] = Array(0)
  var floatSize : Int = 4  
  var bufferData : FloatBuffer = FloatBuffer.allocate(362)
  var bufferByte : ByteBuffer = null
  var verts : Array[Float] = new Array[Float](362)
  var gl : GL2 = gl2
  var vbo : VBuffer = new VBuffer
  var vertsNum:Int = 360
  var stroke = 0x00FF

  vbo.init(gl, bufferId, verts, bufferData, floatSize)

  def draw(x:Int, y:Int, r:Int) = {
    calcEllipse(x, y, r, r, 360)

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    vbo.drawBuffer(gl, GL2.GL_POLYGON, vertsNum)    
  }

  def drawOutline(x:Int, y:Int, r:Int, width:Float) = {
    calcEllipse(x, y, r, r, 360)

    bufferData = vbo.mapBuffer(gl, bufferId, verts)

    if(stroke != 0xFFFF){
      gl.glEnable(GL2.GL_LINE_STIPPLE)
      gl.glLineStipple(1, stroke.shortValue )
    }
    gl.glLineWidth(width)

//for pie
//    verts(vertsNum*2) = x;
//    verts(vertsNum*2 + 1) = y;

    vbo.drawBuffer(gl, GL.GL_LINE_LOOP, vertsNum)

    gl.glDisable(GL2.GL_LINE_STIPPLE)
  }

  private def calcEllipse(x:Int, y:Int, w:Int, h:Int, arcAngle:Int) = {
     var i = 0;
     var a:Double = 60

    vertsNum = /*a.intValue/2*/ a.intValue*arcAngle / 360 /2  // vertices per circle
     var z:Double = 360.0 / a // angle step
      while (i<a){
        verts(i) = w * Math.cos( Math.toRadians(i.doubleValue*z) ).floatValue + x ;
        verts(i+1) = h * Math.sin( Math.toRadians(i.doubleValue*z) ).floatValue + y;
        i+=2
      }
  }

  def setStroke(s:Int){
    stroke = s
  }

  def deinit() = {
    gl.glDeleteBuffers(1, bufferId, 0)
  }
}
