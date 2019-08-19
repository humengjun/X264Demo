package co.jp.snjp.x264demo.utils;

public class Bmp2YuvTools {

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
        yuvData = rotateI420Degree180(yuvData, width, height);

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
        yuvData = rotateNV12Degree180(yuvData, width, height);

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

}
