
package graphic

import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.TextureIO
import java.io.IOException
import java.io.InputStream
import javax.media.opengl._

class ImageRender {

  val MODE_BLEND = GL.GL_BLEND
  val MODE_MODULATE  = GL2ES1.GL_MODULATE
  val MODE_DECAL = GL2ES1.GL_DECAL
  val FILTER_NN = GL.GL_NEAREST // NEAREST NEIGHBOUR
  val FILTER_LINEAR = GL.GL_LINEAR
  val FILTER_MIPMAP = GL.GL_LINEAR_MIPMAP_LINEAR  //trilinear
  val FILTER_ANISOTROPIC = -1
  private var texFilter = FILTER_LINEAR
  private var env_mode = MODE_MODULATE
  private var gl:GL2 = null
  private var img:Texture = null  

  def loadImage(gl:GL2, name:String, sufix:String) = {
    this.gl = gl
    if(gl==null) {
      System.err.print("gl object is null\n")
    } else {
      val f:InputStream = getClass.getResourceAsStream(name)
      try{
        img = TextureIO.newTexture(f, true, sufix)
      } catch {
        case ioe: IOException => {
            System.err.println("can't find file "+name+"\n"+ioe.toString)
        }
        case e: Exception => { println(e) }
      }
    }
  }

  def setImageEnv(env_mode:Int, texFilter:Int) = {
    this.env_mode = env_mode
    this.texFilter = texFilter
  }

  def drawImage(x:Int, y:Int, h:Int, w:Int) = {
    img.enable
    img.bind

    gl.glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE)//this.env_mode)

    this.texFilter match {
      case FILTER_NN => {
          gl.glTexEnvi(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, texFilter)
          gl.glTexEnvi(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, texFilter)
      }
      case FILTER_LINEAR => {
          gl.glTexEnvi(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, texFilter)
          gl.glTexEnvi(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, texFilter)
      }
      case FILTER_MIPMAP => {
          gl.glTexEnvi(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, texFilter)
          gl.glTexEnvi(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, texFilter)
      }
      case FILTER_ANISOTROPIC => {
          if(gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) {
            var max:Array[Float] = new Array(1)
            gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0)
            gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, max(0))
          } else {
            gl.glTexEnvi(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR)
            gl.glTexEnvi(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR)
          }
      }
    }
    
    gl.glBegin(GL2.GL_QUADS)
    gl.glTexCoord2f(img.getImageTexCoords.left, img.getImageTexCoords.bottom)
    gl.glVertex2f(x, y)
    gl.glTexCoord2f(img.getImageTexCoords.right, img.getImageTexCoords.bottom)
    gl.glVertex2f(x+h, y)
    gl.glTexCoord2f(img.getImageTexCoords.right, img.getImageTexCoords.top)
    gl.glVertex2f(x+h, y+w)
    gl.glTexCoord2f(img.getImageTexCoords.left, img.getImageTexCoords.top)
    gl.glVertex2f(x, y+w)
    gl.glEnd
    img.disable    
  }
}
