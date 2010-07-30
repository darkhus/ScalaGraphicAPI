
float size = 28.0;
vec4 col1 = vec4(0.99, 0.99, 0.4, 1.0);
vec4 col2 = vec4(0.2, 0.2, 0.4, 1.0);
vec4 col3 = vec4(0.8, 0.92, 0.92, 1.0);

void main(void)
{    
    vec2 pos = mod(gl_FragCoord.xy, vec2(size)) - vec2(size/2);
    float dist2 = dot(pos, pos);

    vec4 col = mix(col1, col2, smoothstep(150.0, 200.0, dist2));

    if(dist2 > 200)
        col = mix(col2, col3, smoothstep(200.0, 250.0, dist2));
    gl_FragColor = col;
}