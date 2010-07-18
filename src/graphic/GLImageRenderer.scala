
package graphic

import com.jogamp.opengl.util.texture.Texture
import javax.media.opengl._

trait GLImageRenderer {

  val MODE_BLEND = GL.GL_BLEND
  val MODE_MODULATE  = GL2ES1.GL_MODULATE
  val MODE_DECAL = GL2ES1.GL_DECAL
  val FILTER_NN = GL.GL_NEAREST
  val FILTER_LINEAR = GL.GL_LINEAR
  val FILTER_MIPMAP = GL.GL_LINEAR_MIPMAP_LINEAR
  val FILTER_ANISOTROPIC = -1
  private var texFilter = FILTER_LINEAR
  private var env_mode = MODE_MODULATE
  private var gl: GL2 = null
  private var _img: Texture = null
  def image: Texture = _img
  def image_=(img: Texture) = _img = img
  protected def glImg_=(gl2: GL2) = gl = gl2
  protected def glImg: GL2 = gl

  def setImageEnv(env_mode:Int, texFilter:Int): Unit = {
    this.env_mode = env_mode
    this.texFilter = texFilter
  }

  def drawImage(x: Int, y: Int, h: Int, w: Int): Unit = {
    if(image != null){
    image.enable
    image.bind
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
    gl.glTexCoord2f(image.getImageTexCoords.left, image.getImageTexCoords.top)
    gl.glVertex2f(x, y)
    gl.glTexCoord2f(image.getImageTexCoords.right, image.getImageTexCoords.top)
    gl.glVertex2f(x+h, y)
    gl.glTexCoord2f(image.getImageTexCoords.right, image.getImageTexCoords.bottom)
    gl.glVertex2f(x+h, y+w)
    gl.glTexCoord2f(image.getImageTexCoords.left, image.getImageTexCoords.bottom)
    gl.glVertex2f(x, y+w)
    gl.glEnd    
    image.disable
    }
  }
}
