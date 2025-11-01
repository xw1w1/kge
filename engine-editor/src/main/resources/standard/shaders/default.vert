#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;

uniform mat4 u_Model;
uniform mat4 u_ViewProj;
uniform vec3 u_Color;

out vec3 vColor;
out vec3 vNormal;

void main() {
    vec4 worldPos = u_Model * vec4(aPos, 1.0);
    vNormal = mat3(transpose(inverse(u_Model))) * aNormal;
    vColor = u_Color;
    gl_Position = u_ViewProj * worldPos;
}
