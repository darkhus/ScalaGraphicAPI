
package tesswarp;

import javax.media.opengl.glu.*;


public class Tess implements GLUtessellatorCallback
{  
  private GLU glu; 
  GLUtessellator tobj;  

  public int WINDING_POSITIVE = GLU.GLU_TESS_WINDING_POSITIVE;

    public Tess(GLU glu) {
        this.glu = glu;        
        tobj = glu.gluNewTess();
        glu.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_POSITIVE);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, this);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, this);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_END, this);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, this);
        glu.gluTessCallback(tobj, GLU.GLU_TESS_COMBINE, this);
    }

    protected void setWindingRule(int rule){
        glu.gluTessProperty(tobj, GLU.GLU_TESS_WINDING_RULE, rule);
    }

    protected void startTesselationPoly(){
        glu.gluTessBeginPolygon(tobj, null);
    }

    protected void endTesselationPoly(){
        glu.gluTessEndPolygon(tobj);
    }

    protected void startTesselationContour(){
        glu.gluTessBeginContour(tobj);
    }

    protected void endTesselationContour(){
        glu.gluTessEndContour(tobj);
    }

    protected void addVert(double[] point) {
        glu.gluTessVertex(tobj, point, 0, point);
    }

    public void begin(int mode) {
    }

    public void end() {
    }

    public void vertex(Object vertexData) {
    }

    public void vertexData(Object vertexData, Object polygonData) {
        //System.out.println("vertex data");
    }

    public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
/*      double[] vertex = new double[6];
      vertex[0] = coords[0];
      vertex[1] = coords[1];
      vertex[2] = coords[2];
      for (ii = 3; ii < 6; ii++)
        vertex[ii] = weight[0] * ((double[]) data[0])[ii] + weight[1]
                    * ((double[]) data[1])[ii] + weight[2]
                    * ((double[]) data[2])[ii] + weight[3]
                    * ((double[]) data[3])[ii];
 */
      outData[0] = coords;//vertex;
    }

    public void combineData(double[] coords, Object[] data,
        float[] weight, Object[] outData, Object polygonData)
    {
        //System.out.println("combine data");
    }

    public void error(int errnum) {
        System.err.println("Tessellation Error: " + glu.gluErrorString(errnum));
    }

    public void beginData(int i, Object o) {
        //System.out.println("beginData");
    }

    public void edgeFlag(boolean bln) {
        //System.out.println("edgeFlag");
    }

    public void edgeFlagData(boolean bln, Object o) {
        //System.out.println("edgeFlagData");
    }

    public void endData(Object o) {
        //System.out.println("endData");
    }

    public void errorData(int i, Object o) {
        //System.out.println("errorData");
    }

}
