package co.jp.snjp.x264demo.utils;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class ImageConvertTools {
    public static class YUV {
        byte Y, U, V;
    }

    public static byte[] convertI420(byte[] bmpData, int width, int height) {
        byte[] bgrData = new byte[bmpData.length - 54];
        System.arraycopy(bmpData, 54, bgrData, 0, bgrData.length);

        byte[] yuvData = new byte[width * height * 3 / 2];

        int numOfPixel = width * height;
        int positionOfU = numOfPixel;
        int positionOfV = numOfPixel * 5 / 4;

        for (int i = 0; i < height; i++) {
            int startY = i * width;
            int step = (i / 2) * (width / 2);
            int startU = positionOfU + step;
            int startV = positionOfV + step;
            for (int j = 0; j < width; j++) {
                int Y = startY + j;
                int U = startU + j / 2;
                int V = startV + j / 2;
                int index = (i * width + j) * 3;

                int b = bgrData[index] & 0xff;
                int g = bgrData[index + 1] & 0xff;
                int r = bgrData[index + 2] & 0xff;
                YUV yuv = Rgb2Yuv(r, g, b);
                yuvData[Y] = yuv.Y;
                yuvData[U] = yuv.U;
                yuvData[V] = yuv.V;
            }
        }
        //需要旋转图片
//        yuvData = rotateI420Degree180(yuvData, width, height);

        return yuvData;
    }

    public static byte[] convertI420FromRgb(byte[] rgbData, int width, int height) {

        byte[] yuvData = new byte[width * height * 3 / 2];

        int numOfPixel = width * height;
        int positionOfU = numOfPixel;
        int positionOfV = numOfPixel * 5 / 4;

        for (int i = 0; i < height; i++) {
            int startY = i * width;
            int step = (i / 2) * (width / 2);
            int startU = positionOfU + step;
            int startV = positionOfV + step;
            for (int j = 0; j < width; j++) {
                int Y = startY + j;
                int U = startU + j / 2;
                int V = startV + j / 2;
                int index = (i * width + j) * 3;

                int r = rgbData[index] & 0xff;
                int g = rgbData[index + 1] & 0xff;
                int b = rgbData[index + 2] & 0xff;
                YUV yuv = Rgb2Yuv(r, g, b);
                yuvData[Y] = yuv.Y;
                yuvData[U] = yuv.U;
                yuvData[V] = yuv.V;
            }
        }
        //需要旋转图片
        yuvData = rotateI420Degree180(yuvData, width, height);

        return yuvData;
    }

    public static byte[] convertNV12(byte[] bmpData, int width, int height) {
        byte[] bgrData = new byte[bmpData.length - 54];
        System.arraycopy(bmpData, 54, bgrData, 0, bgrData.length);

        byte[] yuvData = new byte[width * height * 3 / 2];

        int numOfPixel = width * height;
        int positionOfU = numOfPixel;
        int positionOfV = 1 + numOfPixel;

        for (int i = 0; i < height; i++) {
            int startY = i * width;
            int step = (i / 2) * (width);
            int startU = positionOfU + step;
            int startV = positionOfV + step;
            for (int j = 0; j < width; j++) {
                int jStep;
                if (j % 2 == 0)
                    jStep = j;
                else
                    jStep = j - 1;
                int Y = startY + j;
                int U = startU + jStep;
                int V = startV + jStep;
                int index = (i * width + j) * 3;

                int b = bgrData[index] & 0xff;
                int g = bgrData[index + 1] & 0xff;
                int r = bgrData[index + 2] & 0xff;
                YUV yuv = Rgb2Yuv(r, g, b);
                yuvData[Y] = yuv.Y;
                yuvData[U] = yuv.U;
                yuvData[V] = yuv.V;
            }
        }
        //需要旋转图片
//        yuvData = rotateNV12Degree180(yuvData, width, height);

        return yuvData;
    }

    public static byte[] convertNV12FromRgb(byte[] rgbData, int width, int height) {

        byte[] yuvData = new byte[width * height * 3 / 2];

        int numOfPixel = width * height;
        int positionOfU = numOfPixel;
        int positionOfV = 1 + numOfPixel;

        for (int i = 0; i < height; i++) {
            int startY = i * width;
            int step = (i / 2) * (width);
            int startU = positionOfU + step;
            int startV = positionOfV + step;
            for (int j = 0; j < width; j++) {
                int jStep;
                if (j % 2 == 0)
                    jStep = j;
                else
                    jStep = j - 1;
                int Y = startY + j;
                int U = startU + jStep;
                int V = startV + jStep;
                int index = (i * width + j) * 3;

                int r = rgbData[index] & 0xff;
                int g = rgbData[index + 1] & 0xff;
                int b = rgbData[index + 2] & 0xff;
                YUV yuv = Rgb2Yuv(r, g, b);
                yuvData[Y] = yuv.Y;
                yuvData[U] = yuv.U;
                yuvData[V] = yuv.V;
            }
        }
        //需要旋转图片
        yuvData = rotateNV12Degree180(yuvData, width, height);

        return yuvData;
    }

    /**
     * 将bmp格式图片转化成yuv_nv12格式，并旋转180度
     *
     * @param bmpData
     * @param width
     * @param height
     * @return
     */
    public static byte[] convertNV12Degree180(byte[] bmpData, int width, int height) {
        byte[] bgrData = new byte[bmpData.length - 54];
        System.arraycopy(bmpData, 54, bgrData, 0, bgrData.length);

        byte[] yuvData = new byte[width * height * 3 / 2];

        int numOfPixel = width * height;

        for (int i = 0; i < height; i++) {
            int startY = numOfPixel - i * width;
            int step = (i / 2 + 1) * (width);
            int startU = numOfPixel + numOfPixel / 2 - step;
            int startV = startU + 1;
            for (int j = 0; j < width; j++) {
                int jStep;
                if (j % 2 == 0)
                    jStep = j;
                else
                    jStep = j - 1;
                int Y = startY + j;
                int U = startU + jStep;
                int V = startV + jStep;
                int index = (i * width + j) * 3;

                int b = bgrData[index] & 0xff;
                int g = bgrData[index + 1] & 0xff;
                int r = bgrData[index + 2] & 0xff;
                YUV yuv = Rgb2Yuv(r, g, b);
                yuvData[Y] = yuv.Y;
                yuvData[U] = yuv.U;
                yuvData[V] = yuv.V;
            }
        }
        return yuvData;
    }

    /**
     * 将bmp格式图片转化成yuv_i420格式
     *
     * @param bmpData
     * @param width
     * @param height
     * @return
     */
    public static byte[] convertI420Degree180(byte[] bmpData, int width, int height) {
        byte[] bgrData = new byte[bmpData.length - 54];
        System.arraycopy(bmpData, 54, bgrData, 0, bgrData.length);

        byte[] yuvData = new byte[width * height * 3 / 2];

        int numOfPixel = width * height;
//        int positionOfU = numOfPixel;
//        int positionOfV = numOfPixel / 4 + numOfPixel;


        for (int i = 0; i < height; i++) {
//            int startY = i * width;
//            int step = (i / 2) * (width / 2);
//            int startU = positionOfU + step;
//            int startV = positionOfV + step;
            int startY = numOfPixel - i * width;
            int step = (i / 2 + 1) * width / 2;
            int startU = numOfPixel + numOfPixel / 4 - step;
            int startV = startU + numOfPixel / 4;
            for (int j = 0; j < width; j++) {
                int Y = startY + j;
                int U = startU + j / 2;
                int V = startV + j / 2;
                int index = (i * width + j) * 3;

                int b = bgrData[index] & 0xff;
                int g = bgrData[index + 1] & 0xff;
                int r = bgrData[index + 2] & 0xff;
                YUV yuv = Rgb2Yuv(r, g, b);
                yuvData[Y] = yuv.Y;
                yuvData[U] = yuv.U;
                yuvData[V] = yuv.V;
            }
        }
        //需要旋转图片
//        yuvData = rotateI420Degree180(yuvData, width, height);

        return yuvData;
    }

    private static byte[] rotateI420Degree180(byte[] srcdata, int imageWidth, int imageHeight) {

        byte[] dstyuv = new byte[srcdata.length];

        int index = 0;
        int tempindex = 0;

        int ustart = imageWidth * imageHeight;
        tempindex = ustart;
        for (int i = 0; i < imageHeight; i++) {

            tempindex -= imageWidth;
            for (int j = 0; j < imageWidth; j++) {

                dstyuv[index++] = srcdata[tempindex + j];
            }
        }

        int udiv = imageWidth * imageHeight / 4;

        int uWidth = imageWidth / 2;
        int uHeight = imageHeight / 2;
        index = ustart;
        tempindex = ustart + udiv;
        for (int i = 0; i < uHeight; i++) {

            tempindex -= uWidth;
            for (int j = 0; j < uWidth; j++) {

                dstyuv[index] = srcdata[tempindex + j];
                dstyuv[index + udiv] = srcdata[tempindex + j + udiv];
                index++;
            }
        }
        return dstyuv;
    }

    public static byte[] rotateNV12Degree180(byte[] srcdata, int imageWidth, int imageHeight) {

        byte[] dstyuv = new byte[srcdata.length];

        int index = 0;
        int yLength = imageWidth * imageHeight;
        int tempindex = yLength;

        for (int i = 0; i < imageHeight; i++) {
            tempindex -= imageWidth;
            for (int j = 0; j < imageWidth; j++) {
                dstyuv[index++] = srcdata[tempindex + j];
            }
        }

        int vdiv = 1;
        int uWidth = imageWidth / 2;
        int uHeight = imageHeight / 2;
        tempindex = yLength + yLength / 2;

        for (int i = 0; i < uHeight; i++) {
            tempindex -= imageWidth;
            index = yLength + i * imageWidth;
            for (int j = 0; j < imageWidth; j++) {
                int jStep;
                if (j % 2 == 0)
                    jStep = j;
                else
                    jStep = j - 1;
                dstyuv[index + jStep] = srcdata[tempindex + jStep];//填充u
                dstyuv[index + jStep + vdiv] = srcdata[tempindex + jStep + vdiv];//填充v
            }
        }
        return dstyuv;
    }


    private static YUV Rgb2Yuv(int r, int g, int b) {
        //Y =  0.299*R + 0.587*G + 0.114*B;
        //
        //U = -0.169*R - 0.331*G + 0.5  *B ;
        //
        //V =  0.5  *R - 0.419*G - 0.081*B;
        YUV yuv = new YUV();
//        yuv.Y = (byte) (0.299 * r + 0.587 * g + 0.114 * b);
//        yuv.U = (byte) (-0.169 * r - 0.331 * g + 0.5 * b + 128);
//        yuv.V = (byte) (0.5 * r - 0.419 * g - 0.081 * b + 128);
        yuv.Y = (byte) ((299 * r + 587 * g + 114 * b) / 1000);
        yuv.U = (byte) ((-169 * r - 331 * g + 500 * b + 128000) / 1000);
        yuv.V = (byte) ((500 * r - 419 * g - 81 * b + 128000) / 1000);
        return yuv;
    }

    public static byte[] pixels2bmp(int[] pixels, int width, int height) {
        byte[] rgb = addBMP_RGB_888(pixels, width, height);
        byte[] header = addBMPImageHeader(rgb.length);
        byte[] infos = addBMPImageInfosHeader(width, height);


        byte[] buffer = new byte[54 + rgb.length];
        System.arraycopy(header, 0, buffer, 0, header.length);
        System.arraycopy(infos, 0, buffer, 14, infos.length);
        System.arraycopy(rgb, 0, buffer, 54, rgb.length);
        return buffer;
    }
    public static byte[] pixels2rgb(int[] pixels, int width, int height) {
        return addBMP_RGB_888(pixels, width, height);
    }


    private static byte[] addBMP_RGB_888(int[] b, int w, int h) {
        int len = b.length;
        System.out.println(b.length);
        byte[] buffer = new byte[w * h * 3];
        int offset = 0;
        for (int i = len - 1; i >= w; i -= w) {
            //DIB文件格式最后一行为第一行，每行按从左到右顺序
            int end = i, start = i - w + 1;
            for (int j = start; j <= end; j++) {
                buffer[offset] = (byte) (b[j] >> 0);
                buffer[offset + 1] = (byte) (b[j] >> 8);
                buffer[offset + 2] = (byte) (b[j] >> 16);
                offset += 3;
            }
        }
        return buffer;
    }

    private static int R = 0;
    private static int G = 1;
    private static int B = 2;

    //I420是yuv420格式，是3个plane，排列方式为(Y)(U)(V)
    public static byte[] I420ToRGB(byte[] src, int width, int height) {
        int numOfPixel = width * height;
        int positionOfV = numOfPixel;
        int positionOfU = numOfPixel / 4 + numOfPixel;
        byte[] rgb = new byte[numOfPixel * 3];
        for (int i = 0; i < height; i++) {
            int startY = i * width;
            int step = (i / 2) * (width / 2);
            int startU = positionOfV + step;
            int startV = positionOfU + step;
            for (int j = 0; j < width; j++) {
                int Y = startY + j;
                int U = startU + j / 2;
                int V = startV + j / 2;
                int index = Y * 3;
                RGB tmp = yuvTorgb(src[Y], src[U], src[V]);
                rgb[index + R] = (byte) tmp.r;
                rgb[index + G] = (byte) tmp.g;
                rgb[index + B] = (byte) tmp.b;
            }
        }

        return rgb;
    }
    //I420是yuv420格式，是3个plane，排列方式为(Y)(U)(V)
    public static byte[] I420ToBGR(byte[] src, int width, int height) {
        int numOfPixel = width * height;
        int positionOfV = numOfPixel;
        int positionOfU = numOfPixel / 4 + numOfPixel;
        byte[] rgb = new byte[numOfPixel * 3];
        for (int i = 0; i < height; i++) {
            int startY = i * width;
            int step = (i / 2) * (width / 2);
            int startU = positionOfV + step;
            int startV = positionOfU + step;
            for (int j = 0; j < width; j++) {
                int Y = startY + j;
                int U = startU + j / 2;
                int V = startV + j / 2;
                int index = Y * 3;
                RGB tmp = yuvTorgb(src[Y], src[U], src[V]);
                rgb[index + R] = (byte) tmp.b;
                rgb[index + G] = (byte) tmp.g;
                rgb[index + B] = (byte) tmp.r;
            }
        }

        return rgb;
    }

    private static class RGB {
        public int r, g, b;
    }

    private static RGB yuvTorgb(byte Y, byte U, byte V) {
        RGB rgb = new RGB();
        rgb.r = (int) ((Y & 0xff) + 1.4075 * ((V & 0xff) - 128));
        rgb.g = (int) ((Y & 0xff) - 0.3455 * ((U & 0xff) - 128) - 0.7169 * ((V & 0xff) - 128));
        rgb.b = (int) ((Y & 0xff) + 1.779 * ((U & 0xff) - 128));
        rgb.r = (rgb.r < 0 ? 0 : rgb.r > 255 ? 255 : rgb.r);
        rgb.g = (rgb.g < 0 ? 0 : rgb.g > 255 ? 255 : rgb.g);
        rgb.b = (rgb.b < 0 ? 0 : rgb.b > 255 ? 255 : rgb.b);
        return rgb;
    }

    //YV16是yuv422格式，是三个plane，(Y)(U)(V)
    public static int[] YV16ToRGB(byte[] src, int width, int height) {
        int numOfPixel = width * height;
        int positionOfU = numOfPixel;
        int positionOfV = numOfPixel / 2 + numOfPixel;
        int[] rgb = new int[numOfPixel * 3];
        for (int i = 0; i < height; i++) {
            int startY = i * width;
            int step = i * width / 2;
            int startU = positionOfU + step;
            int startV = positionOfV + step;
            for (int j = 0; j < width; j++) {
                int Y = startY + j;
                int U = startU + j / 2;
                int V = startV + j / 2;
                int index = Y * 3;
                //rgb[index+R] = (int)((src[Y]&0xff) + 1.4075 * ((src[V]&0xff)-128));
                //rgb[index+G] = (int)((src[Y]&0xff) - 0.3455 * ((src[U]&0xff)-128) - 0.7169*((src[V]&0xff)-128));
                //rgb[index+B] = (int)((src[Y]&0xff) + 1.779 * ((src[U]&0xff)-128));
                RGB tmp = yuvTorgb(src[Y], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
            }
        }
        return rgb;
    }

    //YV12是yuv420格式，是3个plane，排列方式为(Y)(V)(U)
    public static int[] YV12ToRGB(byte[] src, int width, int height) {
        int numOfPixel = width * height;
        int positionOfV = numOfPixel;
        int positionOfU = numOfPixel / 4 + numOfPixel;
        int[] rgb = new int[numOfPixel * 3];

        for (int i = 0; i < height; i++) {
            int startY = i * width;
            int step = (i / 2) * (width / 2);
            int startV = positionOfV + step;
            int startU = positionOfU + step;
            for (int j = 0; j < width; j++) {
                int Y = startY + j;
                int V = startV + j / 2;
                int U = startU + j / 2;
                int index = Y * 3;

                //rgb[index+R] = (int)((src[Y]&0xff) + 1.4075 * ((src[V]&0xff)-128));
                //rgb[index+G] = (int)((src[Y]&0xff) - 0.3455 * ((src[U]&0xff)-128) - 0.7169*((src[V]&0xff)-128));
                //rgb[index+B] = (int)((src[Y]&0xff) + 1.779 * ((src[U]&0xff)-128));
                RGB tmp = yuvTorgb(src[Y], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
            }
        }
        return rgb;
    }

    //YUY2是YUV422格式，排列是(YUYV)，是1 plane
    public static int[] YUY2ToRGB(byte[] src, int width, int height) {
        int numOfPixel = width * height;
        int[] rgb = new int[numOfPixel * 3];
        int lineWidth = 2 * width;
        for (int i = 0; i < height; i++) {
            int startY = i * lineWidth;
            for (int j = 0; j < lineWidth; j += 4) {
                int Y1 = j + startY;
                int Y2 = Y1 + 2;
                int U = Y1 + 1;
                int V = Y1 + 3;
                int index = (Y1 >> 1) * 3;
                RGB tmp = yuvTorgb(src[Y1], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
                index += 3;
                tmp = yuvTorgb(src[Y2], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
            }
        }
        return rgb;
    }

    //UYVY是YUV422格式，排列是(UYVY)，是1 plane
    public static int[] UYVYToRGB(byte[] src, int width, int height) {
        int numOfPixel = width * height;
        int[] rgb = new int[numOfPixel * 3];
        int lineWidth = 2 * width;
        for (int i = 0; i < height; i++) {
            int startU = i * lineWidth;
            for (int j = 0; j < lineWidth; j += 4) {
                int U = j + startU;
                int Y1 = U + 1;
                int Y2 = U + 3;
                int V = U + 2;
                int index = (U >> 1) * 3;
                RGB tmp = yuvTorgb(src[Y1], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
                index += 3;
                tmp = yuvTorgb(src[Y2], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
            }
        }
        return rgb;
    }

    //NV21是YUV420格式，排列是(Y), (VU)，是2 plane
    public static int[] NV21ToRGB(byte[] src, int width, int height) {
        int numOfPixel = width * height;
        int positionOfV = numOfPixel;
        int[] rgb = new int[numOfPixel * 3];

        for (int i = 0; i < height; i++) {
            int startY = i * width;
            int step = i / 2 * width;
            int startV = positionOfV + step;
            for (int j = 0; j < width; j++) {
                int Y = startY + j;
                int V = startV + j / 2;
                int U = V + 1;
                int index = Y * 3;
                RGB tmp = yuvTorgb(src[Y], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
            }
        }
        return rgb;
    }

    //NV12是YUV420格式，排列是(Y), (UV)，是2 plane
    public static int[] NV12ToRGB(byte[] src, int width, int height) {
        int numOfPixel = width * height;
        int positionOfU = numOfPixel;
        int[] rgb = new int[numOfPixel * 3];

        for (int i = 0; i < height; i++) {
            int startY = i * width;
            int step = i / 2 * width;
            int startU = positionOfU + step;
            for (int j = 0; j < width; j++) {
                int Y = startY + j;
                int U = startU + j / 2;
                int V = U + 1;
                int index = Y * 3;
                RGB tmp = yuvTorgb(src[Y], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
            }
        }
        return rgb;
    }

    //NV16是YUV422格式，排列是(Y), (UV)，是2 plane
    public static int[] NV16ToRGB(byte[] src, int width, int height) {
        int numOfPixel = width * height;
        int positionOfU = numOfPixel;
        int[] rgb = new int[numOfPixel * 3];

        for (int i = 0; i < height; i++) {
            int startY = i * width;
            int step = i * width;
            int startU = positionOfU + step;
            for (int j = 0; j < width; j++) {
                int Y = startY + j;
                int U = startU + j / 2;
                int V = U + 1;
                int index = Y * 3;
                RGB tmp = yuvTorgb(src[Y], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
            }
        }
        return rgb;
    }

    //NV61是YUV422格式，排列是(Y), (VU)，是2 plane
    public static int[] NV61ToRGB(byte[] src, int width, int height) {
        int numOfPixel = width * height;
        int positionOfV = numOfPixel;
        int[] rgb = new int[numOfPixel * 3];

        for (int i = 0; i < height; i++) {
            int startY = i * width;
            int step = i * width;
            int startV = positionOfV + step;
            for (int j = 0; j < width; j++) {
                int Y = startY + j;
                int V = startV + j / 2;
                int U = V + 1;
                int index = Y * 3;
                RGB tmp = yuvTorgb(src[Y], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
            }
        }
        return rgb;
    }

    //YVYU是YUV422格式，排列是(YVYU)，是1 plane
    public static int[] YVYUToRGB(byte[] src, int width, int height) {
        int numOfPixel = width * height;
        int[] rgb = new int[numOfPixel * 3];
        int lineWidth = 2 * width;
        for (int i = 0; i < height; i++) {
            int startY = i * lineWidth;
            for (int j = 0; j < lineWidth; j += 4) {
                int Y1 = j + startY;
                int Y2 = Y1 + 2;
                int V = Y1 + 1;
                int U = Y1 + 3;
                int index = (Y1 >> 1) * 3;
                RGB tmp = yuvTorgb(src[Y1], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
                index += 3;
                tmp = yuvTorgb(src[Y2], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
            }
        }
        return rgb;
    }

    //VYUY是YUV422格式，排列是(VYUY)，是1 plane
    public static int[] VYUYToRGB(byte[] src, int width, int height) {
        int numOfPixel = width * height;
        int[] rgb = new int[numOfPixel * 3];
        int lineWidth = 2 * width;
        for (int i = 0; i < height; i++) {
            int startV = i * lineWidth;
            for (int j = 0; j < lineWidth; j += 4) {
                int V = j + startV;
                int Y1 = V + 1;
                int Y2 = V + 3;
                int U = V + 2;
                int index = (U >> 1) * 3;
                RGB tmp = yuvTorgb(src[Y1], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
                index += 3;
                tmp = yuvTorgb(src[Y2], src[U], src[V]);
                rgb[index + R] = tmp.r;
                rgb[index + G] = tmp.g;
                rgb[index + B] = tmp.b;
            }
        }
        return rgb;
    }

    /*
     * 将RGB数组转化为像素数组
     */
    public static int[] convertRgbToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }


        // 理论上data的长度应该是3的倍数，这里做个兼容
        int arg = 0;
        if (size % 3 != 0) {
            arg = 1;
        }

        int[] color = new int[size / 3 + arg];
        int red, green, blue;


        if (arg == 0) {                                    //  正好是3的倍数
            for (int i = 0; i < color.length; ++i) {

                color[i] = (data[i * 3] << 16 & 0x00FF0000) |
                        (data[i * 3 + 1] << 8 & 0x0000FF00) |
                        (data[i * 3 + 2] & 0x000000FF) |
                        0xFF000000;
            }
        } else {                                        // 不是3的倍数
            for (int i = 0; i < color.length - 1; ++i) {
                color[i] = (data[i * 3] << 16 & 0x00FF0000) |
                        (data[i * 3 + 1] << 8 & 0x0000FF00) |
                        (data[i * 3 + 2] & 0x000000FF) |
                        0xFF000000;
            }

            color[color.length - 1] = 0xFF000000;                    // 最后一个像素用黑色填充
        }

        return color;
    }

    /**
     * yuv420转jpeg格式
     *
     * @param src
     * @param width
     * @param height
     * @return
     */
    public static byte[] I420ToJpeg(byte[] src, int width, int height) {
        byte[] rgb = I420ToRGB(src, width, height);
        int[] colors = convertRgbToColor(rgb);
        Bitmap bmp = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] bmpData = null;

        try {
            bmp = Bitmap.createBitmap(colors, 0, width, width, height,
                    Bitmap.Config.ARGB_8888);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            bmpData = out.toByteArray();
        } catch (Exception ignored) {
        }
        return bmpData;
    }

    /**
     * yuv420转bmp格式
     *
     * @param src
     * @param width
     * @param height
     * @return
     */
    public static byte[] I420ToBmp(byte[] src, int width, int height) {
        byte[] bgr = I420ToBGR(src, width, Math.abs(height));

        byte[] header = addBMPImageHeader(bgr.length);
        byte[] infos = addBMPImageInfosHeader(width, height);

        byte[] buffer = new byte[54 + bgr.length];
        System.arraycopy(header, 0, buffer, 0, header.length);
        System.arraycopy(infos, 0, buffer, 14, infos.length);

        System.arraycopy(bgr, 0, buffer, 54, bgr.length);
        return buffer;
    }

    //BMP文件头
    private static byte[] addBMPImageHeader(int size) {
        byte[] buffer = new byte[14];
        buffer[0] = 0x42;
        buffer[1] = 0x4D;
        buffer[2] = (byte) (size >> 0);
        buffer[3] = (byte) (size >> 8);
        buffer[4] = (byte) (size >> 16);
        buffer[5] = (byte) (size >> 24);
        buffer[6] = 0x00;
        buffer[7] = 0x00;
        buffer[8] = 0x00;
        buffer[9] = 0x00;
        buffer[10] = 0x36;
        buffer[11] = 0x00;
        buffer[12] = 0x00;
        buffer[13] = 0x00;
        return buffer;
    }


    //BMP文件信息头
    private static byte[] addBMPImageInfosHeader(int w, int h) {
        byte[] buffer = new byte[40];
        buffer[0] = 0x28;
        buffer[1] = 0x00;
        buffer[2] = 0x00;
        buffer[3] = 0x00;
        buffer[4] = (byte) (w >> 0);
        buffer[5] = (byte) (w >> 8);
        buffer[6] = (byte) (w >> 16);
        buffer[7] = (byte) (w >> 24);
        buffer[8] = (byte) (h >> 0);
        buffer[9] = (byte) (h >> 8);
        buffer[10] = (byte) (h >> 16);
        buffer[11] = (byte) (h >> 24);
        buffer[12] = 0x01;
        buffer[13] = 0x00;
        buffer[14] = 0x18;
        buffer[15] = 0x00;
        buffer[16] = 0x00;
        buffer[17] = 0x00;
        buffer[18] = 0x00;
        buffer[19] = 0x00;
        buffer[20] = 0x00;
        buffer[21] = 0x00;
        buffer[22] = 0x00;
        buffer[23] = 0x00;
        buffer[24] = (byte) 0xE0;
        buffer[25] = 0x01;
        buffer[26] = 0x00;
        buffer[27] = 0x00;
        buffer[28] = 0x02;
        buffer[29] = 0x03;
        buffer[30] = 0x00;
        buffer[31] = 0x00;
        buffer[32] = 0x00;
        buffer[33] = 0x00;
        buffer[34] = 0x00;
        buffer[35] = 0x00;
        buffer[36] = 0x00;
        buffer[37] = 0x00;
        buffer[38] = 0x00;
        buffer[39] = 0x00;
        return buffer;
    }

    public static byte[] I420toNV12(byte[] i420Data) {
        byte[] nv12Data = new byte[i420Data.length];
        int yLength = i420Data.length * 2 / 3;
        int uvLength = yLength / 4;
        System.arraycopy(i420Data, 0, nv12Data, 0, yLength);

        for (int i = 0; i < uvLength; i++) {
            nv12Data[yLength + i * 2] = i420Data[yLength + i];
            nv12Data[yLength + i * 2 + 1] = i420Data[yLength + i + uvLength];
        }
        return nv12Data;
    }

    public static byte[] NV12toI420(byte[] nv12Data) {
        byte[] i420Data = new byte[nv12Data.length];
        int yLength = nv12Data.length * 2 / 3;
        int uvLength = yLength / 4;
        System.arraycopy(nv12Data, 0, i420Data, 0, yLength);

        for (int i = 0; i < uvLength; i++) {
            i420Data[yLength + i] = nv12Data[yLength + i * 2];
            i420Data[yLength + i + uvLength] = nv12Data[yLength + i * 2 + 1];
        }
        return i420Data;
    }

    public static byte[] YV12toI420(byte[] yv12Data) {
        byte[] i420Data = new byte[yv12Data.length];
        int yLength = yv12Data.length * 2 / 3;
        int uvLength = yLength / 4;
        System.arraycopy(yv12Data, 0, i420Data, 0, yLength);
        System.arraycopy(yv12Data, yLength + uvLength, i420Data, yLength, uvLength);
        System.arraycopy(yv12Data, yLength, i420Data, yLength + uvLength, uvLength);

        return i420Data;
    }

    public static byte[] NV21toI420(byte[] nv21Data) {
        byte[] i420Data = new byte[nv21Data.length];
        int yLength = nv21Data.length * 2 / 3;
        int uvLength = yLength / 4;
        System.arraycopy(nv21Data, 0, i420Data, 0, yLength);

        for (int i = 0; i < uvLength; i++) {
            i420Data[yLength + i + uvLength] = nv21Data[yLength + i * 2];
            i420Data[yLength + i] = nv21Data[yLength + i * 2 + 1];
        }
        return i420Data;
    }
}
