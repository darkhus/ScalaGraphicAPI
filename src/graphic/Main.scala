

package graphic

import com.jogamp.opengl.util.Animator
import com.jogamp.opengl.util.FPSAnimator
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

  var text:FontText = new FontText
  var image:ImageRender = new ImageRender
  var shader:Shader = new Shader

  var animY1:Double = 0.01  

  def main(args: Array[String]): Unit = {
    val profile : GLProfile = GLProfile.getDefault()
    val caps : GLCapabilities = new GLCapabilities(profile)
    caps.setHardwareAccelerated(true)
    caps.setSampleBuffers(true)
    caps.setNumSamples(4)
    caps.setStencilBits(8)
    caps.setDoubleBuffered(true)
    println(caps.getSampleBuffers)
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
    
    val anim : FPSAnimator = new FPSAnimator(canvas, 80)
    anim.start
  }

  class OGLEventListener extends GLEventListener{
      @Override
    def init(drawable : GLAutoDrawable) = {
      gl = drawable.getGL().getGL2
      val glu:GLU = new GLU()
      gl.glClearColor(0.7f, 0.7f, 0.7f, 0.0f)
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT)
      gl.glViewport(0, 0, 500, 500)
      gl.getGL2().glMatrixMode(GLMatrixFunc.GL_PROJECTION)
      gl.getGL2().glLoadIdentity()
      glu.gluOrtho2D(0.0, 500.0, 0.0, 500.0)
      gl.glClearStencil(0)
      gl.getGL2().glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
      gl.getGL2().glLoadIdentity()

      gl.glEnable(GL.GL_MULTISAMPLE)
      //gl.glDisable(GL.GL_DEPTH_TEST)
      //gl.glBlendFunc(GL.GL_SRC_ALPHA_SATURATE, GL.GL_ONE)
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)      
      gl.glEnable(GL.GL_BLEND)
      //gl.glEnable(javax.media.opengl.GL2GL3.GL_POLYGON_SMOOTH)
      //gl.glHint(javax.media.opengl.GL2GL3.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST)

      gl.glEnableClientState(javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY)

      graphic.init(gl)
      text.createFontText(gl, "Times New Roman", text.BOLD, 48, true)
      image.loadImage(gl, "CoffeeBean.bmp", "bmp")
      shader.buildShader(gl)
      shader.compileShaders(null, null)
    }

    @Override
    def dispose(drawable : GLAutoDrawable) = {}

    @Override
    def display(drawable : GLAutoDrawable) = {      
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
/*
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
      graphic.pathDrawStroke(10, graphic.CAP_FLAT, graphic.JOIN_BEVEL)

      graphic.setColor(0.4f, 0.3f, 0.9f)
      graphic.fillEllipse(400, 300, 100, 50)
      graphic.setColor(0.6f, 0.5f, 0.99f)
      graphic.outlineEllipse(400, 300, 100, 50, 3)

      
      text.setTextColor(1.0f, 0.5f, 0.3f, 0.9f)
//      graphic.setClipRect(250, 200, 100, 100)
      text.setTextAnchors(text.ANCH_MID, text.ANCH_MID)      
      text.drawText("I love Scala", 200, 200, 90)
//      graphic.deactiveClipArea


      graphic.setColor(0, 0, 1)
      graphic.outlineRectangle(100, 350, 200, 50, graphic.JOIN_ROUND, 10)
*/
/*
      graphic.setClipRect(50, 60, 40, 200)
      graphic.setColor(0.9f, 0.01f, 0.01f)
      graphic.outlineArc(150, 80, 100, 100, 90, 270,
                  graphic.CAP_ROUND, graphic.JOIN_ROUND, 8, graphic.ARC_CHORD)


      graphic.setColor(0.1f, 0.1f, 0.1f)
      graphic.strokeArc(150, 80, 100, 100, 90, 270,
                        graphic.CAP_FLAT, graphic.JOIN_ROUND, 8, graphic.ARC_CHORD)

      graphic.setColor(0.8f, 0.8f, 0.8f)
      graphic.fillArc(150, 80, 85, 85, 90, 270,
                      graphic.CAP_FLAT, graphic.JOIN_ROUND, 8, graphic.ARC_OPEN)
      graphic.deactiveClipArea
*/
/*
      graphic.setColor(0.9f, 0.9f, 0.1f)
      graphic.fillRoundRectangle(300, 300, 100, 130, 14, 14)
      graphic.setColor(0.9f, 0.9f, 0.1f)
      graphic.outlineRoundRectangle(290, 290, 120, 150, 30, 30, 5)
*/
  
      graphic.setColor(0.0f, 0.0f, 0.01f)
//      graphic.strokeEllipse(200, 200, 100, 200, 10, graphic.CAP_FLAT, graphic.JOIN_BEVEL)
      graphic.strokeRoundRectangle(290, 290, 120, 150, 30, 30, 4, graphic.CAP_FLAT, graphic.JOIN_BEVEL)
//      graphic.strokeRectangle(100, 350, 200, 50, 10, graphic.CAP_FLAT, graphic.JOIN_BEVEL)

      graphic.charOutline("Times New Roman", 64, 'A', 100, 400)
      text.setTextColor(1.0f, 0.5f, 0.3f, 0.9f)
      
      text.setTextCurveParam(00, 100, 500, 100, 150, 
                             200+(Math.sin(animY1*5)*90.0).intValue,
                             300,
                             -50+(Math.sin(animY1*5)* -90.0).intValue)   
      text.drawShapeText(" I  LOVE  SCALA  2.8 ")

      // 3.7, 5.16
      animY1 +=0.01f            
      
        graphic.pathMoveTo(200+(Math.sin(animY1)* -150).intValue, 200+(Math.cos(animY1)* 80).intValue)
        graphic.pathCurveTo(350+(Math.sin(animY1)*50).intValue, 350+(Math.cos(animY1)* -150).intValue,
                            200+(Math.sin(animY1)* 300).intValue, 200+(Math.cos(animY1)* 300).intValue,
                            300+(Math.sin(animY1)* -300).intValue, 300+(Math.cos(animY1)* -300).intValue)
        graphic.pathQuadTo(200+(Math.sin(animY1)* -150).intValue, 200+(Math.cos(animY1)* 80).intValue,
                           200+(Math.sin(animY1)* -50).intValue, 200+(Math.cos(animY1)*50).intValue)

//      graphic.setColor(0.8f, 0.2f, 0.2f)
//      graphic.pathDrawFill
      graphic.setColor(0.2f, 0.8f, 0.2f)
      graphic.pathDraw(graphic.CAP_ROUND, graphic.JOIN_ROUND, 3.0f)
//      graphic.pathReset
      //graphic.setColor(0,0,0)      
      //graphic.setClipRect(150, 150, 100, 100)

      graphic.pathClipArea
      graphic.pathReset      
      image.drawImage(00, 00, 500, 500)
      graphic.deactiveClipArea

      shader.applyShader
      graphic.fillEllipse(50, 50, 90, 30)
      shader.deactiveShader
    }

    @Override
    def reshape(drawable : GLAutoDrawable, x:Int, y:Int, width:Int, height:Int) = {}
  }
}
