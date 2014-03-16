precision mediump float;
uniform sampler2D u_Texture;

varying vec2 v_TexCoordinate;

vec3 convertRGB2HSV(vec3 RGBColor) {
	float h, s, v;
	float r = RGBColor.r;
	float g = RGBColor.g;
	float b = RGBColor.b;
	v = max(max(r,g),b);
	float maxval = v;
	float minval = min(min(r,g),b);
	if (maxval == 0.0) s = 0.0;
	else s = (maxval-minval)/maxval;
	if (s == 0.0) h = 0.0;
	else {
		float delta = maxval-minval;
		if(r == maxval) h = (g-b)/delta;
		else if (g == maxval) h = 2.0 + (b-r)/delta;
		else h = 4.0 + (r-g)/delta;
		if (h < 0.0) h += 360.0;
	}
	return vec3(h, s, v);
}

void main() {
	vec3 irgb = texture2D(u_Texture, v_TexCoordinate).rgb;
	vec3 ihsv = convertRGB2HSV(irgb);
	gl_FragColor = vec4(ihsv.g, ihsv.g, ihsv.g, 1.);
}