precision mediump float;
uniform sampler2D u_Texture;
uniform vec2 u_Mu1;
uniform mat2 u_Cinv1;
uniform vec2 u_Mu2;
uniform mat2 u_Cinv2;
uniform vec2 u_Mu3;
uniform mat2 u_Cinv3;
uniform vec2 u_Mu4;
uniform mat2 u_Cinv4;

varying vec4 v_Color;
varying vec2 v_TexCoordinate;
const float thresh = 0.015;

void main() {
	vec2 iab = texture2D(u_Texture, v_TexCoordinate).gb;
	vec4 dist;
	vec2 diff;
	vec2 dist_A;
	if (iab.r != 0.0 && iab.g != 0.0) {
		diff = iab - u_Mu1;
		dist_A = (diff * u_Cinv1);
		dist.r = dot(dist_A, diff);
		dist.r = sqrt(dist.r) * 50.0;
		diff = iab - u_Mu2;
		dist_A = (diff * u_Cinv2);
		dist.g = dot(dist_A, diff);
		dist.g = sqrt(dist.g) * 50.0;
		diff = iab - u_Mu3;
		dist_A = (diff * u_Cinv3);
		dist.b = dot(dist_A, diff);
		dist.b = sqrt(dist.b) * 50.0;
		diff = iab - u_Mu4;
		dist_A = (diff * u_Cinv4);
		dist.a = dot(dist_A, diff);
		dist.a = sqrt(dist.a) * 50.0;
		if (dist.r < thresh) dist.r = 1.0;
		else dist.r = 0.0;
		if (dist.g < thresh) dist.g = 1.0;
		else dist.g = 0.0;
		if (dist.b < thresh) dist.b = 1.0;
		else dist.b = 0.0;
		if (dist.a < thresh) dist.a = 1.0;
		else dist.a = 0.0;
	} else dist = vec4(0.0, 0.0, 0.0, 0.0);
	gl_FragColor = dist;//vec4(dist.r, dist.g, 0.0, 1.0);
}