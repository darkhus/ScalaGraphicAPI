
varying vec2 verts;

vec4 col1 = vec4(0.99, 0.99, 0.0, 1.0);
vec4 col2 = vec4(0.4, 0.4, 0.8, 1.0);

vec2 mid = vec2(150.0, 150.0);
float start = 0.0;
float stop = 60.0;

void main(void)
{ 	        
 	vec2 pos2 = verts - mid;
	float dist2 = dot(pos2, pos2);
	dist2 = sqrt(dist2);

        vec4 col = mix(col1, col2, smoothstep(start, stop, dist2));
	
	gl_FragColor = col;
}
