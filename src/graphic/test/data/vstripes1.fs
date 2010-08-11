
varying vec2 verts;

const float nsps = 0.2;
const vec3 color1 = vec3(0.2, 0.2, 0.8);
const vec3 color2 = vec3(0.2, 0.8, 0.2);

void main()
{
	vec2 NV = verts;
	vec2 onOrOff = (NV * nsps) / 2.0;
	onOrOff = step(1.0, mod(onOrOff, 2.0));	
        onOrOff.x = step(0.5, mod(onOrOff.y, 2.0));

	vec3 outColor = mix(color2, color1, onOrOff.x);        
	gl_FragColor = vec4(outColor, 1.0);
}
