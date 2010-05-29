

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
    anim.start
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

      setColor(1, 0, 0, 0)
      arc.drawOutline(250, 250, 100, 100, 1.0f, false)

      circle.draw(250, 250, 5)
      setColor(1, 1, 1, 0)
      curve.draw(100, 100, 400, 100, 100, 300)
      setColor(1, 0, 0, 0)
      curve.drawOutline(100, 100, 400, 100, 100, 300, 1.0f, false)

      setColor(1, 1, 1, 0)
      rrect.draw(100, 350, 200, 50, 0, 0)

      setColor(1, 0, 0, 0)
      rrect.drawOutline(100, 350, 200, 50, 20, 20, 2.0f)
            
      
      /*
      rectangle.draw(100, 100, 100, 100)
      rectangle.draw(210, 210, 110, 110)

      setColor(0, 1, 0, 0)
      triangle.draw(Array(0, 0, 100, 0, 50, 100))

      setColor(0, 0, 1, 0)
      ellipse.draw(300, 300, 80, 40)

      setColor(0, 0.5f, 1, 0)
      ellipse.draw(310, 310, 80, 40)

      setColor(0.0f, 0.0f, 0.0f, 0)
      circle.draw(130, 410, 75)
      setColor(0.9f, 0.9f, 0.9f, 0)
      circle.draw(330, 410, 75)

      setColor(0.0f, 0.0f, 0.0f, 0)
      circle.drawOutline(250, 250, 100, 5.0f, 0x00FF)

      setColor(0.5f, 0.5f, 0.5f, 0)
      line.draw(5.0f, Array(200,200, 200,0, 250,350))

      setColor(0.9f, 0.5f, 0.5f, 0)
      line.draw(1.0f, Array(20,300, 200,300, 250,50, 400,400))
      */
    }
    
    @Override
    def reshape(drawable : GLAutoDrawable, x:Int, y:Int, width:Int, height:Int) = {
    }

    def setColor(r:Float, g:Float, b:Float, a:Float) = {
      gl.glColor4f(r, g, b, a)
    }
  }

}
