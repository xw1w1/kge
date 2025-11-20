#version 330 core
#define MAX_LIGHTS 32

struct Light {
    int type; // LightType constant ordinal
    vec3 position;
    vec3 direction;
    vec3 color;
    float range;
    float intensity;
    float innerCutoff;
    float outerCutoff;
};

uniform int u_LightCount;
uniform Light u_Lights[MAX_LIGHTS];

uniform vec3 u_Color;
uniform vec3 u_CameraPosition;
uniform vec3 u_CameraForward;

in vec3 vNormal;
in vec3 vWorldPos;

out vec4 FragColor;

vec3 calculatePointLight(Light light, vec3 normal, vec3 fragPos, vec3 materialColor) {
    vec3 lightDir = normalize(light.position - fragPos);
    float diff = max(dot(normal, lightDir), 0.0);

    float distance = length(light.position - fragPos);
    if (distance > light.range) return vec3(0.0);

    float attenuation = 1.0 / (1.0 + 0.1 * distance + 0.01 * distance * distance);

    return materialColor * light.color * light.intensity * diff * attenuation;
}

vec3 calculateSpotLight(Light light, vec3 normal, vec3 fragPos, vec3 materialColor) {
    vec3 lightDir = normalize(light.position - fragPos);
    float diff = max(dot(normal, lightDir), 0.0);

    float theta = dot(lightDir, normalize(-light.direction));
    float epsilon = light.innerCutoff - light.outerCutoff;
    float intensity = clamp((theta - light.outerCutoff) / epsilon, 0.0, 1.0);

    float distance = length(light.position - fragPos);
    if(distance > light.range) return vec3(0.0);

    float attenuation = 1.0 / (1.0 + 0.1 * distance + 0.01 * distance * distance);

    return materialColor * light.color * light.intensity * diff * attenuation * intensity;
}

vec3 calculateDirectionalLight(Light light, vec3 normal, vec3 materialColor) {
    vec3 lightDir = normalize(-light.direction);
    float diff = max(dot(normal, lightDir), 0.0);
    return materialColor * light.color * light.intensity * diff;
}

vec3 calculateCameraLight(vec3 normal, vec3 fragPos, vec3 materialColor, vec3 cameraPos, vec3 cameraForward) {
    vec3 result = vec3(0.0);

    vec3 cameraDir = normalize(fragPos - cameraPos);
    float frontLight = max(dot(normal, -cameraDir), 0.0);
    result += materialColor * frontLight * 0.6;

    float topLight = max(dot(normal, vec3(0.0, 1.0, 0.0)), 0.0);
    result += materialColor * topLight * 0.4;

    vec3 cameraRight = normalize(cross(cameraForward, vec3(0.0, 1.0, 0.0)));
    float sideLight = max(dot(normal, cameraRight), 0.0);
    result += materialColor * sideLight * 0.3;

    float backLight = max(dot(normal, cameraDir), 0.0);
    result += materialColor * backLight * 0.2;

    return result;
}

void main() {
    vec3 N = normalize(vNormal);
    vec3 totalLight = vec3(0.0);

    vec3 ambient = u_Color * 0.15;

    for (int i = 0; i < u_LightCount; i++) {
        Light L = u_Lights[i];

        if (L.type == 0) { // LightType.Directional
            totalLight += calculateDirectionalLight(L, N, u_Color);
        }
        else if (L.type == 1) { // LightType.Point
            totalLight += calculatePointLight(L, N, vWorldPos, u_Color);
        }
        else if (L.type == 2) { // LightType.Spot
            totalLight += calculateSpotLight(L, N, vWorldPos, u_Color);
        }
    }

    if (u_LightCount == 0) {
        totalLight = calculateCameraLight(N, vWorldPos, u_Color, u_CameraPosition, u_CameraForward);
    } else {
        totalLight += ambient;
    }

    totalLight = max(totalLight, u_Color * 0.1);

    FragColor = vec4(totalLight, 1.0);
}