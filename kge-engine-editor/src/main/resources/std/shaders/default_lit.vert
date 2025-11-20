#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;

uniform mat4 u_Model;
uniform mat4 u_ViewProj;

out vec3 vWorldPos;
out vec3 vNormal;

void main() {
    vec4 worldPos = u_Model * vec4(aPos, 1.0);
    vWorldPos = worldPos.xyz;

    // Правильно преобразуем нормали в мировое пространство
    mat3 normalMatrix = mat3(transpose(inverse(u_Model)));
    vNormal = normalize(normalMatrix * aNormal);

    gl_Position = u_ViewProj * worldPos;
}