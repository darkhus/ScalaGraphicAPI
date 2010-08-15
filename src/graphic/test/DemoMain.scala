package graphic
package test

import com.jogamp.opengl.util.{Animator, FPSAnimator}
import javax.media.opengl.{GL, GL2, GLAutoDrawable, GLCapabilities, GLEventListener, GLProfile}
import javax.media.opengl.awt.{GLCanvas => JOGLCanvas}
import java.awt._
import javax.swing.JFrame

object DemoMain {
  val RendererPrefix = "-renderer:"
  val backends = Map("gl" -> GLAWTLauncher, 
                     "java2d" -> Java2DLauncher)

  val demos: Map[String, Demo] = Map("nil" -> NilDemo, 
      "stroke" -> StrokeDemo, 
      "font" -> FontTest,
      "linedash" -> LineDashDemo, 
      "simple" -> SimpleDemo, 
      "linecount" -> LineCountDemo,
      "rectcount" -> RectCountDemo,
      "ellipsecount" -> EllipseCountDemo)
  
  def main(args: Array[String]) {
    
    val (flags, demoIds) = args.partition(_.startsWith("-"))
    if (demoIds.isEmpty || !demos.contains(demoIds(0).toLowerCase)) {
      println("No test specified. Available tests are: "+ demos.keys.mkString(", "))
      return
    }
  
    val demoId = demoIds(0).toLowerCase
    val demo = demos(demoId)
    
    val backendFlag = flags.find(_.startsWith(RendererPrefix)).getOrElse(RendererPrefix + "gl")
    val backend = backendFlag.drop(RendererPrefix.length).toLowerCase
    backends.get(backend) match {
      case Some(b) => 
        val title = demoId +" with "+ backend +" backend"
        println("running demo "+ title)
        b.launch(demo, title)
      case _ => 
        println("Renderer "+ backend +" not found. Available renderers are: "+ backends.keys.mkString(", "))
    }
  }
}

trait Launcher {
  def launch(demo: Demo, title: String)
}

object GLAWTLauncher extends Launcher {
  def launch(demo: Demo, title: String) {
    val frame = new JFrame(title)
    val profile = GLProfile.getDefault
    val caps = new GLCapabilities(profile)
    
    caps.setSampleBuffers(true)
    caps.setDoubleBuffered(true)

    val joglCanvas = new JOGLCanvas(caps)
    joglCanvas.addGLEventListener(new OGLEventListener(demo))
    frame.add(joglCanvas)
    frame.setSize(500, 500)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    joglCanvas.requestFocusInWindow()
    frame.setVisible(true)

    //val anim = new FPSAnimator(joglCanvas, 50)
    val anim = new Animator(joglCanvas)
    anim.setRunAsFastAsPossible(true)
    anim.start
  }
  
  class OGLEventListener(demo: Demo) extends GLEventListener {
    val canvas = new GLCanvas
    
    def init(drawable: GLAutoDrawable) {
      val gl = drawable.getGL.getGL2
      demo.gl = gl
      demo.init()
      canvas.init(gl)   

      gl.setSwapInterval(0)
      drawable.setAutoSwapBufferMode(true)
      System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities())
      System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR))
      System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER))
      System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION))
      System.err.println(drawable.toString())
      drawable.setRealized(true)
    }

    def display(drawable: GLAutoDrawable) {
      //drawable.getContext.makeCurrent
      canvas.gl = drawable.getGL.getGL2      
      demo.step(canvas)
      drawable.swapBuffers
      //drawable.getContext.release
    }

    def reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
      demo.width = drawable.getWidth
      demo.height = drawable.getHeight
      canvas.resize(drawable.getWidth, drawable.getHeight)
      canvas.clear(Color.white)
    }
    def dispose(drawable: GLAutoDrawable) {
      //drawable.getContext.makeCurrent
      canvas.deinit
      //drawable.getContext.destroy
    }
  }
}

object Java2DLauncher extends Launcher {
  def launch(demo: Demo, title: String) {
    val frame = new JFrame(title)
    val comp = new CanvasComponent(demo)
    frame.add(comp)
    frame.setSize(500, 500)
    
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    comp.requestFocusInWindow()
    frame.setVisible(true)
  }
  
  class CanvasComponent(demo: Demo) extends javax.swing.JComponent {
    javax.swing.RepaintManager.currentManager(this).setDoubleBufferingEnabled(false)
    setOpaque(true)
    
    lazy val canvas = new Java2DCanvas(getGraphics.asInstanceOf[Graphics2D], 0, 0)
    lazy val initDemo = demo.init()
    
    override def paint(g: Graphics) {
      demo.width = getWidth
      demo.height = getHeight
      
      initDemo
      val g2d = g.asInstanceOf[Graphics2D]
      canvas.g = g2d
      canvas.width = getWidth
      canvas.height = getHeight
      demo.step(canvas)
      this.repaint()
    }
  }
}