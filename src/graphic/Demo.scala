package graphic

import com.jogamp.opengl.util.FPSAnimator
import com.jogamp.opengl.util.Animator
import java.awt._
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.GLCapabilities
import javax.media.opengl.GLEventListener
import javax.media.opengl.GLProfile
import javax.media.opengl.awt.{GLCanvas => JOGLCanvas}
import javax.swing.JFrame
import com.jogamp.opengl.util.texture.TextureIO
import java.awt.font.FontRenderContext
import java.io.IOException
import java.io.InputStream

abstract class Demo extends JFrame {
  val canvas = new GLCanvas  
  var image1: GLImage = null
  var image2: GLImage = null
  /*
  val solidShader: Shader = new Shader
  val hstripesShader: Shader = new Shader
  val vstripesShader: Shader = new Shader
  val chessShader: Shader = new Shader
  val circlesShader: Shader = new Shader
  val grad1Shader: Shader = new Shader
  */
  var t0, lastFPSUpdate = System.nanoTime
  var t = 0
  var t1 = 0L
  var framesCounter = 0L
  var fpsCounter = 0.0

  def main(args: Array[String]) {
    val profile = GLProfile.getDefault
    val caps = new GLCapabilities(profile)
    caps.setHardwareAccelerated(true)
    caps.setSampleBuffers(true)
    caps.setNumSamples(4)
    caps.setStencilBits(8)
    caps.setDoubleBuffered(true)    
    println(caps.toString)
    val joglCanvas = new JOGLCanvas(caps)
    joglCanvas.addGLEventListener(OGLEventListener)
    getContentPane.add(joglCanvas, BorderLayout.CENTER)
    pack()
    setSize(500, 500)
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    joglCanvas.requestFocusInWindow()
    setVisible(true)

//    val anim = new FPSAnimator(joglCanvas, 80, true)
    val anim = new Animator(joglCanvas)
    anim.setRunAsFastAsPossible(true)
    anim.start
  }

  object OGLEventListener extends GLEventListener {
    def init(drawable: GLAutoDrawable) {
      val gl = drawable.getGL.getGL2
      canvas.init(gl)            
/*      solidShader.compileShadersFromFile(gl, "data/solid.fs", "data/solid.vs")
      hstripesShader.compileShadersFromFile(gl, "data/hstripes1.fs", "data/hstripes1.vs")
      vstripesShader.compileShadersFromFile(gl, "data/vstripes1.fs", "data/vstripes1.vs")
      chessShader.compileShadersFromFile(gl, "data/chessboard1.fs", "data/chessboard1.vs")
      circlesShader.compileShadersFromFile(gl, "data/circles1.fs", "data/circles1.vs")
      grad1Shader.compileShadersFromFile(gl, "data/grad1.fs", "data/grad1.vs")
      */
      image1 = loadImage("data/CoffeeBean.bmp", "bmp")
      image2 = loadImage("data/Island.jpg", "jpg")
      gl.setSwapInterval(0)
      drawable.setRealized(true)
    }

    def display(drawable: GLAutoDrawable) {
//      drawable.getContext.makeCurrent
      canvas.gl = drawable.getGL.getGL2
      draw(canvas)      
      drawable.getContext.release
    }

    def reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
      canvas.gl = drawable.getGL.getGL2
      canvas.resize(drawable.getWidth, drawable.getHeight)
    }

    def dispose(drawable: GLAutoDrawable) {
      canvas.gl = drawable.getGL.getGL2
      image1.tex.destroy(canvas.gl)
      image2.tex.destroy(canvas.gl)      
      canvas.deinit
    }
  }

  def textOutline(f: Font, str: String, x: Int, y: Int): Shape =
    f.createGlyphVector(new FontRenderContext(null, false, false), str).getOutline(x,y)

  def loadImage(name: String, sufix: String): GLImage = {
    val f: InputStream = getClass.getResourceAsStream(name)
    try {
      val img = TextureIO.newTexture(f, true, sufix)
      return new GLImage(img)
    } catch {
      case ioe: IOException => {
          error("Image loading: can't find file "+name+"\n"+ioe.toString)
        }
      case e: Exception => { error("Image loading: " + e.toString) }
    }
    return null
  }

  def countFPS(){
    framesCounter +=1
    t1 = System.nanoTime
    var fps = 1000000000.0/(t1 - t0)
    fpsCounter += fps
    val avg = fpsCounter / framesCounter
    if(t1 - lastFPSUpdate > 1000000000){
      t += 1
      println("Fps: " + fps.toFloat +", Avg: " + avg.toFloat +", Sec: "+t)
      lastFPSUpdate = t1      
    }
    t0 = t1
  }

  def draw(canvas: GLCanvas)
}