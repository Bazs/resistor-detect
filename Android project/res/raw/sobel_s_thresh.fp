precision mediump float;
uniform sampler2D u_Texture;
uniform vec2 u_PixelSize;

varying vec4 v_Color;
varying vec2 v_TexCoordinate;

float rgb2gray(vec3 irgb) {
	const vec3 W = vec3(0.2125, 0.7154, 0.0721);
	float luminance = dot(irgb,W);
	return luminance;
}

float rgb2hsv_s(vec3 RGBColor) {
	float s, v;
	float r = RGBColor.r;
	float g = RGBColor.g;
	float b = RGBColor.b;
	v = max(max(r,g),b);
	float maxval = v;
	float minval = min(min(r,g),b);
	if (maxval == 0.0) s = 0.0;
	else s = (maxval-minval)/maxval;
	return s;
}

void main() {
	vec3 irgb = texture2D(u_Texture, v_TexCoordinate).rgb; 
	float s = rgb2hsv_s(irgb);
	const vec3 sobel = vec3(1.0, 2.0, 1.0);
	vec3 column1, column3, row1, row3;

	vec2 stp0 = vec2(u_PixelSize.x, 0.0);
	vec2 st0p = vec2(0.0, u_PixelSize.y);
	vec2 stpp = vec2(u_PixelSize.x, u_PixelSize.y);
	vec2 stpm = vec2(u_PixelSize.x, -u_PixelSize.y);
	
	column1.r = rgb2gray(texture2D(u_Texture, v_TexCoordinate - stpm).rgb);
	column1.g = rgb2gray(texture2D(u_Texture, v_TexCoordinate + st0p).rgb);
	column1.b = rgb2gray(texture2D(u_Texture, v_TexCoordinate + stpp).rgb);
	
	column3.r = rgb2gray(texture2D(u_Texture, v_TexCoordinate - stpp).rgb);
	column3.g = rgb2gray(texture2D(u_Texture, v_TexCoordinate - st0p).rgb);
	column3.b = rgb2gray(texture2D(u_Texture, v_TexCoordinate + stpm).rgb);
	
	row1.r = rgb2gray(texture2D(u_Texture, v_TexCoordinate - stpm).rgb);
	row1.g = rgb2gray(texture2D(u_Texture, v_TexCoordinate - stp0).rgb);
	row1.b = rgb2gray(texture2D(u_Texture, v_TexCoordinate - stpp).rgb);
	
	row3.r = rgb2gray(texture2D(u_Texture, v_TexCoordinate + stpp).rgb);
	row3.g = rgb2gray(texture2D(u_Texture, v_TexCoordinate + stp0).rgb);
	row3.b = rgb2gray(texture2D(u_Texture, v_TexCoordinate + stpm).rgb);
	
	float h = dot(column1, sobel) - dot(column3, sobel);
	float v = dot(row1, sobel) - dot(row3, sobel);
	float mag = length(vec2(h, v));
	float edge_val, s_val;
	if (mag > 0.2) edge_val = 1.0;
	else edge_val = 0.0;
	if (s > 0.3) s_val = 1.0;
	else s_val = 0.0;
	if (edge_val == 1.0 && s_val == 1.0) s_val = 1.0;
	else s_val = 0.0;
	
	gl_FragColor = vec4(s_val, s_val, 0., 1.);
}