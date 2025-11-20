#version 330 core

in vec3 vWorldPos;
out vec4 FragColor;

uniform vec3 u_SkyTopColor;
uniform vec3 u_SkyHorizonColor;
uniform vec3 u_SkyBottomColor;
uniform vec3 u_SunDirection;
uniform vec3 u_SunColor;
uniform float u_SkyIntensity;

uniform float u_AtmosphereThickness;
uniform float u_TimeOfDay;

void main() {
    vec3 viewDir = normalize(vWorldPos);

    float height = max(viewDir.y, 0.0);

    vec3 skyColor = mix(u_SkyHorizonColor, u_SkyTopColor, height);

    float sunDot = max(dot(viewDir, u_SunDirection), 0.0);
    vec3 sunEffect = u_SunColor * pow(sunDot, 8.0) * 2.0;

    vec3 atmosphere = u_SunColor * pow(sunDot, u_AtmosphereThickness * 2.0) * 0.3;

    vec3 finalColor = (skyColor + sunEffect + atmosphere) * u_SkyIntensity;

    if (u_TimeOfDay < 0.25 || u_TimeOfDay > 0.75) {
        finalColor *= 0.2;
    }

    FragColor = vec4(finalColor, 1.0);
}