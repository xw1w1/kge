#version 330 core

layout(location = 0) in vec3 aPos;
out vec3 vWorldPos;
uniform mat4 u_ViewProj;

void main()
{
    vWorldPos = aPos;
    gl_Position = u_ViewProj * vec4(aPos, 1.0);
}
