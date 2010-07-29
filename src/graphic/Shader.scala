
package graphic

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import javax.media.opengl._

class Shader {
  
  private var v = 0
  private var f = 0
  private var program: Int = 0
  private var gl: GL2 = null
  
  private val vs =
    "void main(void){"+
    "gl_Position = ftransform();}"
  
  private val fs =
    "void main(void){"+
    "gl_FragColor = vec4(0.0,0.0,0.0,1.0);}"
  
  private def buildShader(gl: GL2): Unit = {
    this.gl = gl
    v = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER)
    f = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER)
    program = gl.glCreateProgram
  }

  def compileShadersFromFile(gl: GL2, fragName: String, vertName: String): Unit = {
    buildShader(gl)
      try{
        val fragString = inputStreamToString(getClass.getResourceAsStream(fragName))
        val vertString = inputStreamToString(getClass.getResourceAsStream(vertName))
        this.compileShadersFromString(gl, fragString, vertString)
      } catch {
        case e: Exception => { System.err.println("can't find shader file") } 
      }
  }

  def compileShadersFromString(gl: GL2, fragSrc: String, vertSrc: String): Unit = {
    buildShader(gl)
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
  
  def applyShader(): Unit = {
    gl.glUseProgram(program)
  }
  
  def deactiveShader(): Unit = {
    gl.glUseProgram(0)
  }

  def setUniformParameter1(name: String, value: Any): Unit = {
    val loc = gl.glGetUniformLocation(this.program, name)
    if(loc == -1) println("No such uniform parameter: "+name)
    else {
      if(value.isInstanceOf[Float])
        gl.glUniform1f(loc, value.asInstanceOf[Float])
      else if(value.isInstanceOf[Int])
        gl.glUniform1i(loc, value.asInstanceOf[Int])
    }
  }

  def setUniformParameter2(name: String, value1: Any, value2: Any): Unit = {
    val loc = gl.glGetUniformLocation(this.program, name)
    if(loc == -1) println("No such uniform parameter: "+name)
    else {
      if(value1.isInstanceOf[Float] && value2.isInstanceOf[Float])
        gl.glUniform2f(loc, value1.asInstanceOf[Float], value2.asInstanceOf[Float])
      else if(value1.isInstanceOf[Int] && value2.isInstanceOf[Int])
        gl.glUniform2i(loc, value1.asInstanceOf[Int], value2.asInstanceOf[Int])
    }
  }

  def setUniformParameter3(name: String, value1: Any, value2: Any, value3: Any): Unit = {
    val loc = gl.glGetUniformLocation(this.program, name)
    if(loc == -1) println("No such uniform parameter: "+name)
    else {
      if(value1.isInstanceOf[Float] && value2.isInstanceOf[Float] && value3.isInstanceOf[Float])
        gl.glUniform3f(loc, value1.asInstanceOf[Float], value2.asInstanceOf[Float], value3.asInstanceOf[Float])
      else if(value1.isInstanceOf[Int] && value2.isInstanceOf[Int] && value3.isInstanceOf[Int])
        gl.glUniform3i(loc, value1.asInstanceOf[Int], value2.asInstanceOf[Int], value3.asInstanceOf[Int])
    }
  }

  def setUniformParameter4(name: String, value1: Any, value2: Any, value3: Any, value4: Any): Unit = {
    val loc = gl.glGetUniformLocation(this.program, name)
    if(loc == -1) println("No such uniform parameter: "+name)
    else {
      if(value1.isInstanceOf[Float] && value2.isInstanceOf[Float] && value3.isInstanceOf[Float] && value4.isInstanceOf[Float])
        gl.glUniform4f(loc, value1.asInstanceOf[Float], value2.asInstanceOf[Float], value3.asInstanceOf[Float], value4.asInstanceOf[Float])
      else if(value1.isInstanceOf[Int] && value2.isInstanceOf[Int] && value3.isInstanceOf[Int] && value4.isInstanceOf[Int])
        gl.glUniform4i(loc, value1.asInstanceOf[Int], value2.asInstanceOf[Int], value3.asInstanceOf[Int], value4.asInstanceOf[Int])
    }
  }

  def destroyShader():Unit = {
    gl.glDetachShader(program, v)
    gl.glDeleteShader(v)
    gl.glDetachShader(program, f)
    gl.glDeleteShader(f)
    gl.glDeleteProgram(program)
  }

  private def getLog(shader: Int) {
    val infoLogLength:Array[Int] = new Array(1)
    val length:Array[Int] = new Array(1)
    gl.glGetObjectParameterivARB(shader, GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB, infoLogLength, 0)
    if (infoLogLength(0) > 1) {
      val b:Array[Byte] = new Array(1024)
      gl.glGetInfoLogARB(shader, 1024, length, 0, b, 0)
      println(new String(b, 0, length(0)))
    }
  }

  private def inputStreamToString(stream: InputStream): String = {
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
