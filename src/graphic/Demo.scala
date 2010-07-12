package graphic

//import com.sun.opengl.util.Animator
//import com.sun.opengl.util.FPSAnimator
import com.jogamp.opengl.util.FPSAnimator
import com.jogamp.opengl.util.Animator
import java.awt._
import javax.media.opengl.GL
import javax.media.opengl.GL2
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.GLCapabilities
import javax.media.opengl.GLEventListener
import javax.media.opengl.GLProfile
import javax.media.opengl.awt.{GLCanvas => JOGLCanvas}
import javax.swing.JFrame

abstract class Demo extends JFrame {
  val canvas = new GLCanvas

  //var text:FontText = new FontText
  var image:ImageRender = new ImageRender
  var shader:Shader = new Shader

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

    val anim = new FPSAnimator(joglCanvas, 50)
    anim.start
  }

  object OGLEventListener extends GLEventListener {
    def init(drawable: GLAutoDrawable) {
      val gl = drawable.getGL.getGL2
      canvas.init(gl)
      image.loadImage(gl, "data/CoffeeBean.bmp", "bmp")
      shader.buildShader(gl)
      shader.compileShadersFromFile("data/solid.fs", "data/solid.vs")
    }

    def display(drawable: GLAutoDrawable) {
      canvas.gl = drawable.getGL.getGL2
      canvas.resize(drawable.getWidth, drawable.getHeight)
      draw(canvas)
    }

    def reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {}
    def dispose(drawable: GLAutoDrawable) {}
  }

  def textOutline(g: Canvas, str: String, x: Int, y: Int): Shape =
    g.font.createGlyphVector(g.fontRenderContext, str).getOutline(x,y)

  def draw(canvas: GLCanvas)
}