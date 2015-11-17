#pragma version(1)
#pragma rs java_package_name(com.kramarenko.illia.renderscriptdemo)
//#pragma rs_fp_relaxed

void root(const uchar4 *v_in, uchar4 *v_out, const void *usrData, uint32_t x, uint32_t y) {
    float3 pixel = convert_float4(v_in[0]).rgb;

    if (pixel.r < 60) {
        pixel.r = (int) (0.017 * (pixel.r * pixel.r));
    } else {
        pixel.r = (int) (((61.5 - pow((9.08 - 0.035 * pixel.r), 2.0f)) * 4) + 10);
        if (pixel.r > 255)
            pixel.r = 255;
    }

    if (pixel.g < 60) {
        pixel.g = (int) (0.017 * (pixel.g * pixel.g));
    } else {
        pixel.g = (int) (((61.5 - pow((9.08 - 0.035 * pixel.g), 2.0f)) * 4) + 10);
        if (pixel.g > 255)
            pixel.g = 255;
    }

    if (pixel.b < 60) {
        pixel.b = (int) (0.017 * (pixel.b * pixel.b));
    } else {
        pixel.b = (int) (((61.5 - pow((9.08 - 0.035 * pixel.b), 2.0f)) * 4) + 10);
        if (pixel.b > 255)
            pixel.b = 255;
    }

    v_out->rgb = convert_uchar3(pixel);
}