#version 330 core

in vec3 vWorldPos;
out vec4 FragColor;

uniform vec3 u_GridColor = vec3(0.35, 0.35, 0.35);
uniform vec3 u_HorizonColor = vec3(0.08, 0.09, 0.10);

void main()
{
    float dist = length(vWorldPos.xz);
    float fade = exp(-dist * 0.02);
    vec3 color = mix(u_HorizonColor, u_GridColor, fade);

    FragColor = vec4(color, fade);
}
