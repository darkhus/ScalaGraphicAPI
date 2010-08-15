package graphic

import java.nio.{ByteBuffer, ByteOrder, FloatBuffer}
import java.util.ArrayList
import java.awt.Shape
import java.awt.geom._

sealed abstract class CoordBuffer {
  protected def newSizeHint(old: Int) = old * 2
  def size: Int 
  protected def ensureSize(n: Int)
  protected def addAtEnd(x: Float, y: Float)
  
  def +=(x: Float, y: Float): this.type = {
    ensureSize(size + 2)
    addAtEnd(x, y)
    this
  }
  
  def repeatLast(): this.type = this += (this(size-2), this(size-1))
  
  def apply(idx: Int): Float
  
  /**
   * Sets the size to zero, does not clear any elements.
   */
  def rewind(): this.type
}

final class ArrayCoordBuffer(initialSize: Int) extends CoordBuffer {
  protected def newArray(size: Int) = new Array[Float](size)
  protected var array = newArray(initialSize)
  protected var size0 = 0
  
  def size = size0
  
  protected def ensureSize(n: Int) {
    if (n > array.length) {
      var newsize = newSizeHint(array.length)
      while (n > newsize)
        newsize = newSizeHint(newsize)
        val newar = newArray(newsize)
        Array.copy(array, 0, newar, 0, size0)
        array = newar
    }
  }
  
  protected def addAtEnd(x: Float, y: Float) = {
    array(size0) = x
    array(size0 + 1) = y
    size0 += 2
  }
  
  def apply(idx: Int) = array(idx)
  
  /**
   * Sets the size to zero, does not clear any elements.
   */
  def rewind(): this.type = { size0 = 0; this }
}

final class NIOCoordBuffer(initialSize: Int) extends CoordBuffer {
  protected def newBuffer(size: Int) = 
    ByteBuffer.allocateDirect(4*size).order(ByteOrder.nativeOrder).asFloatBuffer
    //FloatBuffer.allocate(n)//new Array[Float](fixedArraySize)
  protected var buffer = newBuffer(initialSize)
  def nioBuffer: FloatBuffer = buffer
  def size = buffer.position
  
  protected def ensureSize(n: Int) {
    if (n > buffer.capacity) {
      var newsize = newSizeHint(buffer.capacity)
      while (n > newsize)
        newsize = newSizeHint(newsize)
        buffer = newBuffer(newsize).put(buffer)
    }
  }
  
  protected def addAtEnd(x: Float, y: Float) = {
    buffer.put(x)
    buffer.put(y)
  }
  
  def apply(idx: Int) = buffer.get(idx)
  
  /**
   * Sets the size to zero, does not clear any elements.
   */
  def rewind(): this.type = { buffer.rewind(); this }
}


class GeometryBuilder {
  private val floatSize = 4
  private var fixedArraySize = 4096
  private val extendArraySize = 4096
  private var _coords = new NIOCoordBuffer(fixedArraySize)//allocateCoordData(fixedArraySize)
  private var _tempCoords = new ArrayCoordBuffer(fixedArraySize)
  
  def vertexCount = _coords.size/2//gvi/2
  def size = fixedArraySize

  val arcVerts = new Array[Float](180)
  var arcInd = 0
  var endsAtStart = false
  var implicitClose = false
  
  def tempCoords: CoordBuffer = _tempCoords
  def coords: CoordBuffer = _coords
  
  def coordData: FloatBuffer = _coords.nioBuffer
  private def allocateCoordData(n: Int) = 
    ByteBuffer.allocateDirect(4*n).order(ByteOrder.nativeOrder).asFloatBuffer
    //FloatBuffer.allocate(n)//new Array[Float](fixedArraySize)
  def newCoordData() = allocateCoordData(fixedArraySize)
  
  def rewind() { 
    coordData.rewind() 
  }
  
  def fill(b: FloatBuffer) {
    _coords.nioBuffer.put(b)
  }
  
  def emitLineSeg(x: Float, y: Float, nx: Float, ny: Float) {
    _coords += (x + nx, y + ny)
    _coords += (x - nx, y - ny)
  }
 
  // TODO: where to move this?
  def flatnessFactor(shape: Shape): Double = 0.5 /*{
    val flatTresh = 40.0 // treshold for decreasing flatness factor
    val size = math.min(shape.getBounds2D.getWidth, shape.getBounds2D.getHeight) - flatTresh
    val linearFactor = 500.0
    val flatMax = 1.0
    val fac1 = 5.0
    if(size > 0f) flatMax - math.log10(size)/fac1
    else 1.0
  }*/
}