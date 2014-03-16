precision mediump float;
uniform sampler2D u_Texture;
uniform vec2 u_PixelSize;

varying vec4 v_Color;
varying vec2 v_TexCoordinate;

void main() {

	const vec3 disk0 = vec3(0.0, 1.0, 0.0);
	const vec3 disk1 = vec3(1.0, 1.0, 1.0);
	
	vec3 column1, column3, row1, row3;

	vec2 stp0 = vec2(u_PixelSize.x, 0.0);
	vec2 st0p = vec2(0.0, u_PixelSize.y);
	
	
	vec2 pix_up = texture2D(u_Texture, v_TexCoordinate + st0p).rg;
	vec2 pix_left = texture2D(u_Texture, v_TexCoordinate - stp0).rg;
	vec2 pix_middle = texture2D(u_Texture, v_TexCoordinate).rg;
	vec2 pix_right = texture2D(u_Texture, v_TexCoordinate + stp0).rg;
	vec2 pix_down = texture2D(u_Texture, v_TexCoordinate - st0p).rg;
	
	vec2 values;
	vec2 output;
	values.r = pix_up.r + pix_left.r + pix_middle.r + pix_right.r + pix_down.r;
	values.g = pix_up.g + pix_left.g + pix_middle.g + pix_right.g + pix_down.g;
	
	if (values.r != 0.0) output.r = 1.0;
	else output.r = 0.0;
	if (values.g != 0.0) output.g = 1.0;
	else output.g = 0.0;
	
	gl_FragColor = vec4(output.rg, 0., 1.);
}