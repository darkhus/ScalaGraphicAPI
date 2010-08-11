package graphic
package test

import javax.media.opengl.GL2
import scala.collection.mutable.ArrayBuffer

trait FPSDemo extends Demo {
  var t0, t1, frameCount = 0L
  var fpsCounter = 0.0
  lazy val fpsLog = new ArrayBuffer[Double](256) ++= Array.fill(5)(0d)
  
  private def lastFPS(n: Int) = fpsLog.view.drop(math.max(0, fpsLog.size-n))
  private def meanFPS(lastN: Int): Double = {
    val buf = lastFPS(lastN)
    val sum = buf reduceLeft (_+_)
    sum/buf.size
  }
  private def stdDeviationFPS(lastN: Int): Double = {
    val buf = lastFPS(lastN)
    val mean = meanFPS(lastN)
    val sqSum = buf reduceLeft { (a,b) => 
      val aa = (a - mean)
      val bb = (b - mean)
      aa*aa + bb*bb
    }
    math.sqrt(sqSum/buf.size)
  }
  
  def init() {
    t0 = System.nanoTime
  }
  
  def step(canvas: Canvas) {
    draw(canvas)
    
    frameCount +=1
    t1 = System.nanoTime
    val secs = (t1-t0)/1000000000.0
    if (secs >= 1.0) { // measure fps each 1/10th sec
      val fps = frameCount/secs
      fpsLog += fps
      val count = fpsLog.size
      fpsCounter += fps - (if(count > 10) fpsLog(count-11) else 0)
      val avg = fpsCounter / 10
      log("Fps: "+ fps.toFloat +", Avg(10s): "+ avg.toFloat)
      // reset:
      t0 = t1
      frameCount = 0
    }
  }
}