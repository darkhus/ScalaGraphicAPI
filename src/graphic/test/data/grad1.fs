
varying vec2 verts;
uniform vec2 middle;

vec4 col1 = vec4(0.2, 0.8, 0.2, 1.0);
vec4 col2 = vec4(0.9, 0.9, 0.0, 1.0);
vec4 col3 = vec4(0.8, 0.92, 0.92, 1.0);
vec4 col4 = vec4(0.2, 0.8, 0.92, 1.0);
float tres1 = 10.5;
float tres2 = 30.9;
float tres3 = 90.99;

void main(void)
{
	vec4 col;
 	vec2 pos2 = verts - middle;
	float l = length(pos2);

	col = mix(col1, col2, smoothstep(00.0, tres1, l));
	if(l > tres1 && l < tres2)
		col = mix(col2, col3, smoothstep(tres1, tres2, l));
	if(l >= tres2)
		col = mix(col3, col4, smoothstep(tres2, tres3, l));
	gl_FragColor = col;
}