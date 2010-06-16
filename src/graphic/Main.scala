

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
  var gl:GL2 = null
  val graphic:Scala2DGraphic = new Scala2DGraphic

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
      gl.glClearColor(0.7f, 0.7f, 0.7f, 1.0f);
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT)
      gl.glViewport(0, 0, 500, 500);
      gl.getGL2().glMatrixMode(GLMatrixFunc.GL_PROJECTION);
      gl.getGL2().glLoadIdentity();
      glu.gluOrtho2D(0.0, 500.0, 0.0, 500.0);

      if( !gl.isExtensionAvailable("GL_ARB_vertex_buffer_object") )
        println("GL_ARB_vertex_buffer_object extension is not available")

      gl.glEnableClientState(javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY)

      graphic.init(gl)
    }

    @Override
    def dispose(drawable : GLAutoDrawable) = {

    }

    @Override
    def display(drawable : GLAutoDrawable) = {

      //gl.glEnable(javax.media.opengl.GL2GL3.GL_POLYGON_SMOOTH)
      graphic.clearCanvas(1, 1, 1)
/*
      graphic.setColor(0, 0, 1)
      graphic.outlineRectangle(100, 350, 200, 50, graphic.JOIN_MITER, 10)
      
      graphic.outlineTriangle(50, 100, 150, 100, 100, 200, graphic.JOIN_BEVEL, 5)
      graphic.outlineTriangle(180, 100, 280, 100, 230, 200, graphic.JOIN_ROUND, 5)
      graphic.outlineTriangle(310, 100, 410, 100, 360, 200, graphic.JOIN_MITER, 5)

      graphic.setColor(0, 0, 1)
      graphic.line(50, 400, 300, 400, graphic.CAP_ROUND, 10)
      graphic.setColor(0, 0, 0)
      graphic.line(50, 400, 300, 400, graphic.CAP_FLAT, 0.5f)
      graphic.setColor(0, 0, 1)
      graphic.line(50, 360, 300, 360, graphic.CAP_SQUARE, 10)
      graphic.setColor(0, 0, 0)
      graphic.line(50, 360, 300, 360, graphic.CAP_FLAT, 0.5f)
      graphic.setColor(0, 0, 1)
      graphic.line(50, 320, 300, 320, graphic.CAP_FLAT, 10)
      graphic.setColor(0, 0, 0)
      graphic.line(50, 320, 300, 320, graphic.CAP_FLAT, 0.5f)
*/
      graphic.setColor(0.3f, 0.2f, 0.7f)
      graphic.pathMoveTo(50, 50)
      graphic.pathLineTo(150, 250)
      graphic.pathLineTo(200, 50)
      graphic.pathLineTo(300, 100)
      graphic.pathLineTo(400, 50)
      
      graphic.pathMoveTo(50, 400)
      graphic.pathLineTo(100, 350)
      graphic.pathLineTo(200, 450)
      graphic.pathLineTo(350, 350)

      graphic.pathDraw(graphic.CAP_ROUND, graphic.JOIN_BEVEL, 5)

      graphic.setColor(0.7f, 0.5f, 0.3f)
      graphic.pathMoveTo(0, 0)
      graphic.pathLineTo(10, 100)
      graphic.pathCurveTo(300, 100, 200, 300, 250, 00)
      graphic.pathLineTo(400, 300)
      graphic.pathQuadTo(100, 400, 200, 500)
      graphic.pathDrawStroke(4, 4)

      graphic.setColor(0.4f, 0.3f, 0.9f)
      graphic.fillEllipse(400, 300, 100, 50)
      graphic.setColor(0.6f, 0.5f, 0.99f)
      graphic.outlineEllipse(400, 300, 100, 50, 3)

/*
      graphic.setColor(0, 0, 1)
      graphic.outlineRectangle(100, 350, 200, 50, graphic.JOIN_ROUND, 10)
      
      graphic.setColor(0.9f, 0.01f, 0.01f)
      graphic.arc(150, 380, 100, 100, 90, 270,
                         graphic.CAP_ROUND, graphic.JOIN_ROUND, 8, graphic.ARC_PIE)

      graphic.setColor(0.9f, 0.9f, 0.1f)
      graphic.fillRoundRectangle(300, 300, 100, 130, 14, 14)
      graphic.setColor(0.9f, 0.9f, 0.1f)
      graphic.outlineRoundRectangle(290, 290, 120, 150, 30, 30, 5)
*/
      
      graphic.setColor(0.0f, 0.0f, 0.01f)
      graphic.strokeEllipse(200, 200, 100, 200, 4, 4)
      graphic.strokeRoundRectangle(290, 290, 120, 150, 30, 30, 5, 5)
      graphic.strokeRectangle(100, 350, 200, 50, 5, 5)
                  
    }

    @Override
    def reshape(drawable : GLAutoDrawable, x:Int, y:Int, width:Int, height:Int) = {
    }

  }
}
