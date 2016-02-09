#pragma version(1)
#pragma rs java_package_name(com.kramarenko.illia.renderscriptdemo)
//#pragma rs_fp_relaxed

rs_allocation gIn;
rs_allocation gOut;
rs_script gScript;

int width;
int height;

float a = 150.3f;
float b = 10.5f;

void root(const uchar4 *v_in, uchar4 *v_out, const void *usrData, uint32_t x, uint32_t y) {
    //a = amount of rotation
    //b = size of effect

    //float angle = a * exp(-(x*x+y*y)/(b*b));
    //float angle = a*(x*x+y*y);
    //uint32_t u = cos(angle) * x + sin(angle) * y;
    //if (u > width) { u = width; }
    //if (u < 0) { u = 0; }
    //uint32_t u = width - x;
    //uint32_t v = -sin(angle) * x + cos(angle) * y;
    //if (v > height) { v = height; }
    //if (v < 0) { v = 0; }
    //uint32_t v = y;

//    const uchar4 *element = rsGetElementAt(gIn, u, v);
  //  float4 color = rsUnpackColor8888(*element);
    //float4 output = {color.r, color.g, color.b};
    //*v_out = rsPackColorTo8888(output);

}

//void filter() {
 //  rsForEach(gScript, gIn, gOut);
//}