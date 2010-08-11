package graphic
package test

import com.jogamp.opengl.util.texture.{Texture, TextureIO}
import java.io.IOException
import java.io.InputStream

trait GLDemo extends Demo {
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
}