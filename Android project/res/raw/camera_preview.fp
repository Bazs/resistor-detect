#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 v_TexCoordinate; 
varying vec4 v_Color;
uniform samplerExternalOES u_Texture;
uniform vec3 u_WhiteBalanceRatios;

void main() {
	vec3 irgb = texture2D(u_Texture, v_TexCoordinate).rgb;
	gl_FragColor = vec4(irgb * u_WhiteBalanceRatios, 1.0);
}