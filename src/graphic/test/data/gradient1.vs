
varying vec2 verts;

void main()
{
	verts = gl_Vertex.xy;
	gl_Position = ftransform();
} 