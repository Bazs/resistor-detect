precision mediump float;
const int kernelSize = 81;
uniform sampler2D u_Texture;
uniform vec2 u_Offsets[kernelSize];
uniform float u_Kernel[kernelSize];

varying vec4 v_Color;
varying vec2 v_TexCoordinate;

void main() {
	vec2 pixelValue = vec2(0.0, 0.0);
	int i;
	vec2 readPixels, currentKernel;
	for (i = 0; i < kernelSize; i++) {
		readPixels = texture2D(u_Texture, v_TexCoordinate + u_Offsets[i]).rg;
		currentKernel = vec2(u_Kernel[i], u_Kernel[i]);
		pixelValue += readPixels * currentKernel;
	}
	if (pixelValue.r > 0.0) pixelValue.r = 1.0;
	if (pixelValue.g > 0.0) pixelValue.g = 1.0;
	
	gl_FragColor = vec4(pixelValue, 0., 1.);
}