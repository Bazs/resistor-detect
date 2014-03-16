precision mediump float;
uniform sampler2D u_Texture;
uniform vec3 u_Mu1;
uniform mat3 u_Cinv1;
uniform float u_Thresh1;
uniform vec3 u_Mu2;
uniform mat3 u_Cinv2;
uniform float u_Thresh2;
uniform vec3 u_Mu3;
uniform mat3 u_Cinv3;
uniform float u_Thresh3;
uniform vec3 u_Mu4;
uniform mat3 u_Cinv4;
uniform float u_Thresh4;

uniform vec3 u_WhiteBalanceRatios;

varying vec4 v_Color;
varying vec2 v_TexCoordinate;


void main() {
	vec3 irgb = texture2D(u_Texture, v_TexCoordinate).rgb;
	vec4 dist;
	vec3 diff;
	vec3 dist_A;
	if ((irgb.r != 0.0 && irgb.g != 0.0) && irgb.b != 0.0) {
		//irgb = irgb * u_WhiteBalanceRatios;
		diff = irgb - u_Mu1;
		dist_A = (diff * u_Cinv1);
		dist.r = dot(dist_A, diff);
		dist.r = sqrt(dist.r) * 50.0;
		diff = irgb - u_Mu2;
		dist_A = (diff * u_Cinv2);
		dist.g = dot(dist_A, diff);
		dist.g = sqrt(dist.g) * 50.0;
		diff = irgb - u_Mu3;
		dist_A = (diff * u_Cinv3);
		dist.b = dot(dist_A, diff);
		dist.b = sqrt(dist.b) * 50.0;
		diff = irgb - u_Mu4;
		dist_A = (diff * u_Cinv4);
		dist.a = dot(dist_A, diff);
		dist.a = sqrt(dist.a) * 50.0;
		if (dist.r < u_Thresh1) dist.r = 1.0;
		else dist.r = 0.0;
		if (dist.g < u_Thresh2) dist.g = 1.0;
		else dist.g = 0.0;
		if (dist.b < u_Thresh3) dist.b = 1.0;
		else dist.b = 0.0;
		if (dist.a < u_Thresh4) dist.a = 1.0;
		else dist.a = 0.0;
	} else dist = vec4(0.0, 0.0, 0.0, 0.0);
	gl_FragColor = dist;//vec4(dist.r, dist.g, 0.0, 1.0);
}