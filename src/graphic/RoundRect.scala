
package graphic

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.media.opengl._
import java.awt.geom.PathIterator
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D

class RoundRect(gl2:GL2) {
  var bufferId : Array[Int] = Array(0)
  var floatSize : Int = 4
  var bufferData : FloatBuffer = FloatBuffer.allocate(3620)
  var bufferByte : ByteBuffer = null
  var verts : Array[Float] = new Array[Float](3620)
  var tmpverts : Array[Float] = new Array[Float](3620)
  var gl : GL2 = gl2
  var vbo : VBuffer = new VBuffer
  var vertsNumTmp:Int = 180
  var vertsNum:Int = 180
  var point = new Array[Float](6)
  var arcVerts = new Array[Float](360)
  var rect:RoundRectangle2D = new RoundRectangle2D.Float() 

  var width:Float = 5.0f
  var i:Int = 0;
  var ind:Int = 0;
  var nx:Float = 0.0f
  var ny:Float= 0.0f
  var curx:Float = 0.0f
  var cury:Float= 0.0f
  var startInd:Int = 0
  var endInd:Int = 0
  val CAP_FLAT = 0
  val CAP_SQUARE = 1
  val CAP_ROUND = 2
  val JOIN_BEVEL = 0
  val JOIN_MITER = 1
  val JOIN_ROUND = 2
  var cap_style = CAP_ROUND
  var join_style = JOIN_ROUND
  var miter_limit = 5
  var roundness = 10
  var arcInd:Int = 0;

  vbo.init(gl, bufferId, verts, bufferData, floatSize)

  def draw(x:Int, y:Int, w:Int, h:Int, arcw:Int, arch:Int) = {
    calcRect(x, y, w, h, arcw, arch)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_POINTS, vertsNum)
//    vbo.drawBuffer(gl, GL.GL_TRIANGLE_STRIP, vertsNum)
  }

  def drawOutline(x:Int, y:Int, w:Int, h:Int, arcw:Int, arch:Int, width:Float) = {
    calcRect(x, y, w, h, arcw, arch)
    bufferData = vbo.mapBuffer(gl, bufferId, verts)
    vbo.drawBuffer(gl, GL.GL_LINE_STRIP, vertsNum)
  }


  private def calcRect(x:Int, y:Int, w:Int, h:Int, arcw:Int, arch:Int) = {
    var r:Rectangle2D = new Rectangle2D.Float
//    rect.setRoundRect(x, y, w, h, 20, 20)
    r.setRect(x, y, w, h)
    var path = r.getPathIterator(null, 1.0f)

    while(!path.isDone){
      var t = path.currentSegment(point)
      t match {
      case java.awt.geom.PathIterator.SEG_CUBICTO =>
        {
          println("cubic to")
        }
      case java.awt.geom.PathIterator.SEG_QUADTO =>
        {
          println("quad to")
        }
      case java.awt.geom.PathIterator.SEG_MOVETO =>
        {
          tmpverts(i) = point(0)
          tmpverts(i+1) = point(1)
          i+=2
        }
      case java.awt.geom.PathIterator.SEG_LINETO =>
        {
          tmpverts(i) = point(0)
          tmpverts(i+1) = point(1)
          i+=2
        }
      case java.awt.geom.PathIterator.SEG_CLOSE =>
        {
        }
      }
      path.next
    }

    endInd = i
    vertsNumTmp = i/2
    i=0
    ind = 0

    var prevt = 0    
    path = r.getPathIterator(null, 1.0f)
    while(!path.isDone){
      var t = path.currentSegment(point)
      t match {
      case java.awt.geom.PathIterator.SEG_CUBICTO =>
        {
          println("cubic to")
        }
      case java.awt.geom.PathIterator.SEG_QUADTO =>
        {
          println("quad to")
        }
      case java.awt.geom.PathIterator.SEG_MOVETO =>
        {
          startInd = ind
          moveto(ind)
          ind+=2
          prevt = t          
        }
      case java.awt.geom.PathIterator.SEG_LINETO =>
        {
          if (prevt != PathIterator.SEG_MOVETO)
            join(ind)

          lineto(ind)
          ind+=2
          prevt = t          
        }
      case java.awt.geom.PathIterator.SEG_CLOSE =>
        {
          endCapOrCapClose(startInd, false, false)
        }
      }
      path.next
    }
    vertsNum = i/2
  }

  def lineto(ind:Int) = {
    emitLineSeg(tmpverts(ind), tmpverts(ind+1), nx, ny)
    curx = tmpverts(ind)
    cury = tmpverts(ind+1)
  }

  def emitLineSeg(x:Float, y:Float, nx:Float, ny:Float) = {
    verts(i) = x + nx
    verts(i+1) = y + ny
    i+=2
    verts(i) = x - nx
    verts(i+1) = y - ny
    i+=2
  }

  def moveto(ind:Int) = {
    // normal vector
    var x1:Float = tmpverts(ind)
    var y1:Float = tmpverts(ind+1)
    curx = tmpverts(ind)
    cury = tmpverts(ind+1)    
    var x2:Float = tmpverts(ind+2)
    var y2:Float = tmpverts(ind+3)

    var dx:Float = x2 - x1;
    var dy:Float = y2 - y1;

    var pw:Float = 0.0f;

    if (dx == 0.0)
      pw = width / scala.Math.abs(dy);
    else if (dy == 0.0)
      pw = width / scala.Math.abs(dx);
    else
      pw = width / scala.Math.sqrt(dx*dx + dy*dy).floatValue;

    nx = -dy * pw;
    ny = dx * pw;

    cap_style match {
      case CAP_FLAT => {
          verts(i) = curx + nx
          verts(i+1) = cury + ny
          i+=2
      }
      case CAP_SQUARE => {
          verts(i) = curx - ny + nx
          verts(i+1) = cury + nx +ny
          i+=2
          emitLineSeg(curx - ny, cury + nx, nx, ny)
      }
      case CAP_ROUND => {
          arcPoints(curx, cury, curx+nx, cury+ny, curx-nx, cury-ny)
          var count = i + arcInd + 2
          i = count
          var front = 0
          var end = arcInd / 2
          while(front != end) {
            count-=1
            verts(count) = arcVerts(2 * end - 1)
            count-=1
            verts(count) = arcVerts(2 * end - 2)
            
            end-=1
            if(front != end) {
              count-=1
              verts(count) = arcVerts(2 * front + 1)
              count-=1
              verts(count) = arcVerts(2 * front + 0)
            }
            front+=1
          }
          verts(count - 1) = verts(count + 1)
          verts(count - 2) = verts(count + 0)   
      }
    }
    emitLineSeg(curx, cury, nx, ny)
  }

  def join(ind:Int) = {
    // normal vector
    var x1:Float = curx
    var y1:Float = cury
    var x2:Float = tmpverts(ind)
    var y2:Float = tmpverts(ind+1)

    var dx:Float = x2 - x1
    var dy:Float = y2 - y1

    var pw:Float = 0.0f

    if (dx == 0)
        pw = width / scala.Math.abs(dy)
    else if (dy == 0)
        pw = width / scala.Math.abs(dx)
    else
      pw = width / scala.Math.sqrt(dx*dx + dy*dy).floatValue

    nx = -dy * pw
    ny = dx * pw

    join_style match {
      case JOIN_BEVEL => {
      }
      case JOIN_MITER => {
          val count = i
          val prevNvx = verts(count-2) - curx
          val prevNvy = verts(count-1) - cury
          val xprod = prevNvx * ny - prevNvy * nx
          var px, py, qx, qy = 0.0

          if(xprod <0 ) {
            px = verts(count-2)
            py = verts(count-1)
            qx = curx - nx
            qy = cury - ny
          } else {
            px = verts(count-4)
            py = verts(count-3)
            qx = curx + nx
            qy = cury + ny
          }

          var pu = px * prevNvx + py * prevNvy
          var qv = qx * nx + qy * ny
          var ix = (ny * pu - prevNvy * qv) / xprod
          var iy = (prevNvx * qv - nx * pu) / xprod

        if ((ix - px) * (ix - px) + (iy - py) * (iy - py) <= miter_limit * miter_limit) {
            verts(i) = ix.floatValue
            verts(i+1) = iy.floatValue
            i+=2
            verts(i) = ix.floatValue
            verts(i+1) = iy.floatValue
            i+=2            
        }
      }
      case JOIN_ROUND => {
          val prevNvx = verts(i - 2) - curx
          val prevNvy = verts(i - 1) - cury
          var ii:Int = 0
          if(nx * prevNvy - ny * prevNvx < 0) {
            arcPoints(0, 0, nx, ny, -prevNvx, -prevNvy)
            ii = arcInd / 2
            while( ii > 0 ) {
                emitLineSeg(curx, cury, arcVerts(2*ii - 2), arcVerts(2*ii - 1) )
                ii-=1
            }
          } else {
            arcPoints(0, 0, -prevNvx, -prevNvy, nx, ny)
            ii = 0
            while (ii < arcInd / 2) {
                emitLineSeg(curx, cury, arcVerts(2*ii + 0), arcVerts(2*ii + 1) )
                ii+=1
            }
        }
      }
    }
    emitLineSeg(curx, cury, nx, ny)
  }

  def arcPoints(cx:Float, cy:Float, fromX:Float, fromY:Float, toX:Float, toY:Float) = {
    var dx1 = fromX - cx
    var dy1 = fromY - cy
    var dx2 = toX - cx
    var dy2 = toY - cy

    val sin_theta = scala.Math.sin(scala.Math.Pi / roundness).floatValue
    val cos_theta = scala.Math.cos(scala.Math.Pi / roundness).floatValue

    arcInd = 0
    // > 180
    while (dx1 * dy2 - dx2 * dy1 < 0) {
      val tmpx = dx1 * cos_theta - dy1 * sin_theta
      val tmpy = dx1 * sin_theta + dy1 * cos_theta
      dx1 = tmpx
      dy1 = tmpy
      arcVerts(arcInd) = cx + dx1
      arcVerts(arcInd+1) = cy + dy1
      arcInd+=2
    }

    // > 90
    while (dx1 * dx2 + dy1 * dy2 < 0) {
      val tmpx = dx1 * cos_theta - dy1 * sin_theta
      val tmpy = dx1 * sin_theta + dy1 * cos_theta
      dx1 = tmpx
      dy1 = tmpy
      arcVerts(arcInd) = cx + dx1
      arcVerts(arcInd+1) = cy + dy1
      arcInd+=2
    }

    while (dx1 * dy2 - dx2 * dy1 > 0) {
      val tmpx = dx1 * cos_theta - dy1 * sin_theta
      val tmpy = dx1 * sin_theta + dy1 * cos_theta
      dx1 = tmpx
      dy1 = tmpy
      arcVerts(arcInd) = cx + dx1
      arcVerts(arcInd+1) = cy + dy1
      arcInd+=2
    }
    if(arcInd>0) arcInd -= 2
  }

  def endCapOrCapClose(startInd:Int, implicitClose:Boolean, endsAtStart:Boolean) = {

    if(endsAtStart){
      join(startInd+2)
    } else if(implicitClose) {
      join(startInd)
      lineto(startInd)
      join(startInd+2)
    } else {
      endCap()
    }

    verts(i) = verts(i-2)
    verts(i+1) = verts(i-1)
    i+=2
}

  def endCap() = {
    cap_style match {
      case CAP_FLAT => {
      }
      case CAP_SQUARE => {
          emitLineSeg(curx+ny, cury-nx, nx, ny)
      }
      case CAP_ROUND => {
          arcPoints(curx, cury, verts(i-2), verts(i-1), verts(i-4), verts(i-3) )
          var front:Int = 0
          var end:Int = arcInd / 2
          while (front != end) {
            verts(i) =  arcVerts(2*end-2)
            verts(i+1) = arcVerts(2*end-1)
            i+=2
            end-=1
            if (front != end) {
              verts(i) = arcVerts(2*front+0)
              verts(i+1) = arcVerts(2*front+1)
              i+=2
              front+=1
            }            
        }
      }
    }
  }

  def deinit() = {
    gl.glDeleteBuffers(1, bufferId, 0)
  }
}

