

package graphic

import com.jogamp.opengl.util.Animator
import java.awt.BorderLayout
import javax.media.opengl.GL
import javax.media.opengl.GL2
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.GLCapabilities
import javax.media.opengl.GLEventListener
import javax.media.opengl.GLProfile
import javax.media.opengl.awt.GLCanvas
import javax.media.opengl.fixedfunc.GLMatrixFunc
import javax.media.opengl.glu.GLU
import javax.swing.JFrame

object Main extends JFrame {

  var rectangle : Rectangle = null
  var triangle : Triangle = null
  var ellipse : Ellipse = null
  var circle : Circle = null
  var line : Line = null
  var arc : Arc = null
  var curve : Curve = null
  var rrect : RoundRect = null
  var gl : GL2 = null;

  def main(args: Array[String]): Unit = {
    val profile : GLProfile = GLProfile.getDefault()
    val caps : GLCapabilities = new GLCapabilities(profile)
    caps.setNumSamples(8)
    println(caps.toString)
    val canvas : GLCanvas = new GLCanvas(caps)
    val el : OGLEventListener = new OGLEventListener
    canvas.addGLEventListener(el)
    getContentPane.add(canvas, BorderLayout.CENTER)
    pack()
    setSize(500, 500)
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    canvas.requestFocusInWindow()
    setVisible(true)

    val anim : Animator = new Animator(canvas)
//    anim.start
  }

  class OGLEventListener extends GLEventListener{
      @Override
      def init(drawable : GLAutoDrawable) = {
      gl = drawable.getGL().getGL2;
      val glu:GLU = new GLU();
      gl.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT)
      gl.glViewport(0, 0, 500, 500);
      gl.getGL2().glMatrixMode(GLMatrixFunc.GL_PROJECTION);
      gl.getGL2().glLoadIdentity();
      glu.gluOrtho2D(0.0, 500.0, 0.0, 500.0);

      if( !gl.isExtensionAvailable("GL_ARB_vertex_buffer_object") )
        println("GL_ARB_vertex_buffer_object extension is not available")

      gl.glEnableClientState(javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY)

      rectangle = new Rectangle(gl)
      triangle = new Triangle(gl)
      ellipse = new Ellipse(gl)
      circle = new Circle(gl)
      line = new Line(gl)
      arc = new Arc(gl)
      rrect = new RoundRect(gl)
      curve = new Curve(gl)
    }

    @Override
    def dispose(drawable : GLAutoDrawable) = {

    }

    @Override
    def display(drawable : GLAutoDrawable) = {
      val gl:GL2 = drawable.getGL().getGL2()     
      gl.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT)

      setColor(0, 0, 1, 0)
      rrect.draw(100, 350, 200, 50, 20, 20)

      rrect.draw(100, 250, 200, 50, 20, 20)

      rrect.draw(100, 150, 200, 50, 20, 20)
    }
    
    @Override
    def reshape(drawable : GLAutoDrawable, x:Int, y:Int, width:Int, height:Int) = {
    }

    def setColor(r:Float, g:Float, b:Float, a:Float) = {
      gl.glColor4f(r, g, b, a)
    }
  }

}
