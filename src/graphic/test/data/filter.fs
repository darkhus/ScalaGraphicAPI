uniform sampler2D texture;
const int kernelSize = 9;
uniform float kernel[kernelSize];
const int width = 256;
const int height = 256;

float step_w = 1.0/width;
float step_h = 1.0/height;
vec2 offset[kernelSize];

void main()
{
    offset[0] = vec2(-step_w, -step_h);
    offset[1] = vec2(0.0, -step_h);
    offset[2] = vec2(step_w, -step_h);
    offset[3] = vec2(-step_w, 0.0);
    offset[4] = vec2(0.0, 0.0);
    offset[5] = vec2(step_w, 0.0);
    offset[6] = vec2(-step_w, step_h);
    offset[7] = vec2(0.0, step_h);
    offset[8] = vec2(step_w, step_h);

    int i;
    vec4 sum = vec4(0.0);

    for (i = 0; i < kernelSize; i++)
    {
        vec4 tmp = texture2D(texture, gl_TexCoord[0].st + offset[i]);
        sum += tmp * kernel[i];
    }
    gl_FragColor = sum;
}
