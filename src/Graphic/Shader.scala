
package graphic

import javax.media.opengl._

class Shader {
  
  var v = 0
  var f = 0
  var program:Int = 0
  var gl:GL2 = null
  
  val vs =
  "varying vec3 MCPosition;"+
  "void main(void){"+
  "MCPosition = vec3(gl_Vertex);"+
  "gl_TexCoord[0] = gl_MultiTexCoord0;"+
  "gl_Position = ftransform();}"
  
  val fs = 
    "varying vec3 MCPosition;"+
    "void main(void){"+
    "gl_FragColor = vec4(0.2,0.2,0.8,1.0);"+
  "}"
  
  def buildShader(gl:GL2) = {
    this.gl = gl
    v = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER)
    f = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER)
    program = gl.glCreateProgram
  }
  
  def compileShaders(fragSrc:String, vertSrc:String) = {    
    var status:Array[Int] = new Array(1)
    //gl.glShaderSource(f, 1, fragSrc.asInstanceOf, null)
    val afs:Array[String] = Array(fs)
    val avs:Array[String] = Array(vs)
    gl.glShaderSource(f, 1, afs, null)
    gl.glCompileShader(f)
    gl.glGetShaderiv(f, GL2ES2.GL_COMPILE_STATUS, status, 0)

    if(status(0) == GL.GL_FALSE) {
      System.err.println("Fragment shader compile error!")
      getLog(f)
    }

    if(vertSrc != null) {
      gl.glShaderSource(v, 1, avs, null)
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
}
