
package graphic

import com.jogamp.opengl.util.texture.Texture
import javax.media.opengl._

trait GLImageRenderer { self: GLCanvas =>
  val MODE_BLEND = GL.GL_BLEND
  val MODE_MODULATE  = GL2ES1.GL_MODULATE
  val MODE_DECAL = GL2ES1.GL_DECAL
  val FILTER_NN = GL.GL_NEAREST
  val FILTER_LINEAR = GL.GL_LINEAR
  val FILTER_MIPMAP = GL.GL_LINEAR_MIPMAP_LINEAR
  val FILTER_ANISOTROPIC = -1
  private var texFilter = FILTER_LINEAR
  private var env_mode = MODE_MODULATE  
  private var _img: GLImage = null

  def setImageEnv(env_mode:Int, texFilter:Int): Unit = {
    this.env_mode = env_mode
    this.texFilter = texFilter
  }

  def drawImage(image: GLImage, x: Int, y: Int, h: Int, w: Int): Unit = {
    if(image != null){
    image.tex.enable
    image.tex.bind
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
    gl.glTexCoord2f(image.tex.getImageTexCoords.left, image.tex.getImageTexCoords.top)
    gl.glVertex2f(x, y)
    gl.glTexCoord2f(image.tex.getImageTexCoords.right, image.tex.getImageTexCoords.top)
    gl.glVertex2f(x+h, y)
    gl.glTexCoord2f(image.tex.getImageTexCoords.right, image.tex.getImageTexCoords.bottom)
    gl.glVertex2f(x+h, y+w)
    gl.glTexCoord2f(image.tex.getImageTexCoords.left, image.tex.getImageTexCoords.bottom)
    gl.glVertex2f(x, y+w)
    gl.glEnd    
    image.tex.disable
    }
  }
}
