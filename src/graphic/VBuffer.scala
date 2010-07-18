package graphic

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.media.opengl._

class VBuffer {
  def init(gl: GL2, buffId: Array[Int], verts: Array[Float],
           buffData: FloatBuffer, floatSize: Int) {
    gl.glGenBuffers(1, buffId, 0)
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffId(0))
// buffData = FloatBuffer.allocate(4*2)
    //buffData.put(verts)
    //buffData.rewind()
    gl.glBufferData(GL.GL_ARRAY_BUFFER, 
                    /*floatSize*verts.size*/floatSize*5210, /*buffData*/null, GL.GL_DYNAMIC_DRAW) // GL.GL_STATIC_DRAW)
  }

  def drawBuffer(gl: GL2, drawType: Int, count: Int) {
// gl.glEnableClientState(javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY)
    gl.glVertexPointer(2, GL.GL_FLOAT, 0, 0)
    gl.glDrawArrays(drawType, 0, count)
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0)
// gl.getGL2GL3().glDisableClientState(javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY)
  }

  def mapBuffer(gl: GL2, bufferId: Array[Int], verts: Array[Float]) {
// gl.glEnableClientState(javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY)
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferId(0))
    val byteBuffer = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, javax.media.opengl.GL.GL_WRITE_ONLY)
    var bufferData: FloatBuffer = null
    if(byteBuffer != null) {
      bufferData = (byteBuffer.order(ByteOrder.nativeOrder())).asFloatBuffer
      bufferData.put(verts)
      bufferData.rewind()
    }
    
    if( gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER) == false ) {
      System.err.println("Error mapping VBO, error code: "+gl.glGetError)
    }
// gl.getGL2GL3().glDisableClientState(javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY)
    //bufferData
  }

  def deinitBuffer(): Unit = {
  }
}