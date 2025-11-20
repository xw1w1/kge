#version 330 core

in vec4 vFragPosLightSpace;

uniform sampler2D u_ShadowMap;

out vec4 FragColor;

float calcShadow(vec4 fragPosLightSpace) {
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;

    if(projCoords.x <0.0 || projCoords.x>1.0 || projCoords.y<0.0 || projCoords.y>1.0)
    return 0.0;

    float currentDepth = projCoords.z;
    float bias = 0.005;
    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(u_ShadowMap,0);
    for(int x=-1;x<=1;x++){
        for(int y=-1;y<=1;y++){
            float pcfDepth = texture(u_ShadowMap,projCoords.xy + vec2(x,y)*texelSize).r;
            if(currentDepth - bias > pcfDepth) shadow += 1.0;
        }
    }
    shadow /= 9.0;
    return clamp(shadow,0.0,1.0);
}

void main() {
    float s = calcShadow(vFragPosLightSpace);
    FragColor = vec4(vec3(1.0-s),1.0); // тени = темнее
}
