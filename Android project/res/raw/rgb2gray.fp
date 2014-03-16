precision mediump float;

vec3 rgb2gray(vec3 irgb) {
	const vec3 W = vec3(0.2125, 0.7154, 0.0721);
	float luminance = dot(irgb,W);
	return vec3(luminance,luminance,luminance);
}

void main(){
}