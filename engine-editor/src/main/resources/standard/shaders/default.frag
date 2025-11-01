#version 330 core

in vec3 vColor;
in vec3 vNormal;
out vec4 FragColor;

void main() {
    vec3 lightDir = normalize(vec3(0.5, 1.0, 0.3));
    float diff = max(dot(normalize(vNormal), lightDir), 0.0);
    vec3 color = vColor * (0.3 + diff * 0.7);
    FragColor = vec4(color, 1.0);
}
