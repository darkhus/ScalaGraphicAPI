
package graphic

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import javax.media.opengl._

class Shader {
  
  var v = 0
  var f = 0
  var program:Int = 0
  var gl:GL2 = null
  
  val vs =
    "void main(void){"+
    "gl_Position = ftransform();}"
  
  val fs =
    "void main(void){"+
    "gl_FragColor = vec4(0.0,0.0,0.0,1.0);}"
  
  def buildShader(gl:GL2) = {
    this.gl = gl
    v = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER)
    f = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER)
    program = gl.glCreateProgram
  }

  def compileShadersFromFile(fragName:String, vertName:String) = {
      try{
        val fragString = inputStreamToString(getClass.getResourceAsStream(fragName))
        val vertString = inputStreamToString(getClass.getResourceAsStream(vertName))
        this.compileShaders(fragString, vertString)
      } catch {
        case e: Exception => { System.err.println("can't find shader file") } 
      }
  }

  def compileShaders(fragSrc:String, vertSrc:String) = {    
    var status:Array[Int] = new Array(1)

    if(fragSrc != null) {
      gl.glShaderSource(f, 1, Array(fragSrc), null)
    } else
      gl.glShaderSource(f, 1, Array(fs), null)
    gl.glCompileShader(f)
    gl.glGetShaderiv(f, GL2ES2.GL_COMPILE_STATUS, status, 0)
    if(status(0) == GL.GL_FALSE) {
      System.err.println("Fragment shader compile error!")
      getLog(f)
    }

    if(vertSrc != null) {
      gl.glShaderSource(v, 1, Array(vertSrc), null)
    } else 
      gl.glShaderSource(v, 1, Array(vs), null)
    gl.glCompileShader(v)
    gl.glGetShaderiv(v, GL2ES2.GL_COMPILE_STATUS, status, 0)
    if(status(0) == GL.GL_FALSE) {
      System.err.println("Vertex shader compile error!")
      getLog(v)
    }
    
    gl.glAttachShader(program, v)
    gl.glAttachShader(program, f)
    gl.glLinkProgram(program)
    gl.glValidateProgram(program)
    //gl.glUseProgram(program)
  }
  
  def applyShader() = {
    gl.glUseProgram(program)
  }
  
  def deactiveShader() = {
    gl.glUseProgram(0)
  }

  def destroyShader() = {
    gl.glDetachShader(program, v)
    gl.glDeleteShader(v)
    gl.glDetachShader(program, f)
    gl.glDeleteShader(f)
    gl.glDeleteProgram(program)
  }

  private def getLog(shader:Int) = {
    val infoLogLength:Array[Int] = new Array(1)
    val length:Array[Int] = new Array(1)
    gl.glGetObjectParameterivARB(shader, GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB, infoLogLength, 0)
    if (infoLogLength(0) > 1) {
      val b:Array[Byte] = new Array(1024)
      gl.glGetInfoLogARB(shader, 1024, length, 0, b, 0)
      println(new String(b, 0, length(0)))
    }
  }

  private def inputStreamToString(stream:InputStream) : String = {
    val br = new BufferedReader(new InputStreamReader(stream))
    val sb = new StringBuilder
    var line:String  = null
    line = br.readLine
    while (line != null) {
      sb.append(line+"\n")
      line = br.readLine
    }
    br.close
    return sb.toString
  }

}
