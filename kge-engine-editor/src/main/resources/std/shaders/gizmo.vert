#version 330 core

layout(location = 0) in vec3 aPos;

uniform mat4 u_Model;
uniform mat4 u_ViewProj;
uniform vec3 u_Color;
out vec3 vColor;

void main() {
    vColor = u_Color;
    gl_Position = u_ViewProj * u_Model * vec4(aPos, 1.0);
}