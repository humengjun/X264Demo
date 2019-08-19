package co.jp.snjp.x264demo.hardware;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import co.jp.snjp.x264demo.utils.FileUtils;
import co.jp.snjp.x264demo.utils.ImageConvertTools;

public class ImageEncoder {

    private byte[] configByte;

    private MediaCodec mMediaCodec;

    private MediaFormat mMediaFormat;

    private boolean isI420;

    private int generateIndex = 0;

    private final int DEFAULT_FRAMERATE = 1;

    private final int DEFAULT_I_FRAME_INTERVAL = 1;

    private final int DEFAULT_WIDTH = 1280;

    private final int DEFAULT_HEIGHT = 720;

    private final int DEFAULT_MAX_BIT_RATE = DEFAULT_HEIGHT * DEFAULT_WIDTH * 3 / 2;

//    private final int DEFAULT_KEY_BIT_RATE = 2500 * 100;//1280*720推荐码率

    private int cacheFrameCount;

    private boolean isFirstFrame = true;

    private boolean isNew = false;

    private String mimeType;

    private int compressRatio;

    private int width = DEFAULT_WIDTH;

    private int height = DEFAULT_HEIGHT;

    private int max_bit_rate = DEFAULT_MAX_BIT_RATE;

    //需要编码的图像类型
    public static final int TYPE_YUV = 0;
    public static final int TYPE_JPG = 1;
    public static final int TYPE_BMP = 2;

    public ImageEncoder(String mimeType, int compressRatio) {
        this.mimeType = mimeType;
//        this.isNew = isNew;
//        this.width = width;
//        this.height = height;
        this.compressRatio = compressRatio;

        try {
            mMediaCodec = MediaCodec.createEncoderByType(mimeType);
        } catch (IOException e) {
            mMediaCodec = null;
            return;
        }

        mMediaFormat = MediaFormat.createVideoFormat(mimeType, width, height);
        //码率越低，图片越模糊
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, max_bit_rate * compressRatio / 100);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, DEFAULT_FRAMERATE);
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, DEFAULT_I_FRAME_INTERVAL);
        int color_format = getSupportColorFormat();
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, color_format);

        mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        isFirstFrame = true;
        cacheFrameCount = 0;
        generateIndex = 0;
    }

    /**
     * 获取支持的颜色格式
     *
     * @return
     */
    private int getSupportColorFormat() {
        int numCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo codecInfo = null;
        for (int i = 0; i < numCodecs && codecInfo == null; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder()) {
                continue;
            }
            String[] types = info.getSupportedTypes();
            boolean found = false;
            for (int j = 0; j < types.length && !found; j++) {
                if (types[j].equals("video/avc")) {
                    System.out.println("found");
                    found = true;
                }
            }
            if (!found)
                continue;
            codecInfo = info;
        }

        // Find a color profile that the codec supports
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");

        if (isNew) {
            //优先使用新版本的API，兼容性差，解码效果更好
            for (int i = 0; i < capabilities.colorFormats.length; i++) {

                switch (capabilities.colorFormats[i]) {
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible:
                        isI420 = true;
                        return capabilities.colorFormats[i];
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                        return capabilities.colorFormats[i];
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                        isI420 = true;
                        return capabilities.colorFormats[i];
                    default:
                        Log.d("AvcEncoder", "other color format " + capabilities.colorFormats[i]);
                        break;
                }
            }
        } else {
            //优先使用旧版本API，兼容性强，解码效果较差(背景是灰色)
            for (int i = capabilities.colorFormats.length - 1; i >= 0; i--) {

                switch (capabilities.colorFormats[i]) {
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible:
                        isI420 = true;
                        return capabilities.colorFormats[i];
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                        return capabilities.colorFormats[i];
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                        isI420 = true;
                        return capabilities.colorFormats[i];
                    default:
                        Log.d("AvcEncoder", "other color format " + capabilities.colorFormats[i]);
                        break;
                }
            }
        }
        return 0;
    }

    /**
     * 编码一张图片
     *
     * @param file
     * @param isReset 是否重置编码器
     * @return
     */
    public byte[] encoderFile(File file, boolean isReset) throws Exception {
        byte[] yuvData;
        byte[] fileData = FileUtils.readFile4Bytes(file);
        if (fileData == null)
            return null;

        if (file.getName().endsWith(".bmp")) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;//这个参数设置为true才有效，
            BitmapFactory.decodeByteArray(fileData, 0, fileData.length, options);
            //获取图片的宽高
            int height = options.outHeight;
            int width = options.outWidth;
            if (this.width != width || this.height != height)
                resetEncoder(width, height);
            else if (isReset)
                resetEncoder(width, height);

            if (isI420)
                yuvData = ImageConvertTools.convertI420(fileData, width, height);
            else
                yuvData = ImageConvertTools.convertNV12(fileData, width, height);
        } else if (file.getName().endsWith(".jpg")) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            //       options.inSampleSize = 1;   //width，hight设为原来的十分一
            Bitmap bitmap = BitmapFactory.decodeByteArray(fileData, 0, fileData.length, options);
            //获取图片的宽高
            int height = options.outHeight;
            int width = options.outWidth;

            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            byte[] bmpData = ImageConvertTools.pixels2bmp(pixels, width, height);

            if (this.width != width || this.height != height)
                resetEncoder(width, height);
            else if (isReset)
                resetEncoder(width, height);
            if (isI420)
                yuvData = ImageConvertTools.convertI420(bmpData, width, height);
            else
                yuvData = ImageConvertTools.convertNV12(bmpData, width, height);
        } else if (file.getName().endsWith(".yuv")) {
            if (isReset)
                resetEncoder(width, height);

            if (!isI420)
                yuvData = ImageConvertTools.I420toNV12(fileData);
            else
                yuvData = fileData;
        } else {
            return null;
        }
        int size = width * height * 3 / 2;
        if (yuvData.length > size) {
            byte[] src = yuvData;
            yuvData = new byte[size];
            System.arraycopy(src, 0, yuvData, 0, size);
        }

        byte[] h264Data = null;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        //针对某些设备会缓存几帧再编码，因此需要判断是否是第一帧并记录缓存的帧数。
        if (isFirstFrame) {
            while (true) {
                if (mMediaCodec != null) {
                    int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                    int flag = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                        } else {
                            inputBuffer = mMediaCodec.getInputBuffers()[inputBufferIndex];
                        }
                        if (inputBuffer != null && yuvData != null) {
                            inputBuffer.clear();
                            inputBuffer.put(yuvData);
                            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, yuvData.length, computePresentationTime(generateIndex), flag);
                            generateIndex++;
                            cacheFrameCount++;
                        }
                    }

                    int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                    while (outputBufferIndex >= 0) {
                        ByteBuffer outputBuffer = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                        } else {
                            outputBuffer = mMediaCodec.getOutputBuffers()[outputBufferIndex];
                        }
                        if (outputBuffer != null && bufferInfo.size > 0) {
                            byte[] buffer = new byte[bufferInfo.size];
                            outputBuffer.get(buffer, 0, buffer.length);

                            if (bufferInfo.flags == 2) {
                                configByte = new byte[bufferInfo.size];
                                configByte = buffer;
                            } else {// if (bufferInfo.flags == 1)
                                h264Data = new byte[buffer.length + configByte.length];
                                System.arraycopy(configByte, 0, h264Data, 0, configByte.length);
                                System.arraycopy(buffer, 0, h264Data, configByte.length, buffer.length);
                                cacheFrameCount--;
                            }
                        }

                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                    }
                    if (h264Data != null) {
                        break;
                    }
                }
            }
        } else {
            for (int i = 0; i < cacheFrameCount + 1; i++) {
                if (mMediaCodec != null) {
                    int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                    int flag = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                        } else {
                            inputBuffer = mMediaCodec.getInputBuffers()[inputBufferIndex];
                        }
                        if (inputBuffer != null && yuvData != null) {
                            inputBuffer.clear();
                            inputBuffer.put(yuvData);
                            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, yuvData.length, computePresentationTime(generateIndex), flag);
                            generateIndex++;
                            cacheFrameCount++;
                        }
                    }

                    int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                    while (outputBufferIndex >= 0) {
                        ByteBuffer outputBuffer = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                        } else {
                            outputBuffer = mMediaCodec.getOutputBuffers()[outputBufferIndex];
                        }
                        if (outputBuffer != null && bufferInfo.size > 0) {
                            byte[] buffer = new byte[bufferInfo.size];
                            outputBuffer.get(buffer, 0, buffer.length);

                            if (bufferInfo.flags == 2) {
                                configByte = new byte[bufferInfo.size];
                                configByte = buffer;
                            } else {// if (bufferInfo.flags == 1)
                                h264Data = new byte[buffer.length + configByte.length];
                                System.arraycopy(configByte, 0, h264Data, 0, configByte.length);
                                System.arraycopy(buffer, 0, h264Data, configByte.length, buffer.length);
                                cacheFrameCount--;
                            }
                        }

                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                    }
                }
            }
        }
        isFirstFrame = false;
        return h264Data;
    }

    /**
     * 编码一张图片
     *
     * @param yuvData
     * @return
     */
    private byte[] encoderData(byte[] yuvData) throws Exception {
        byte[] h264Data = null;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        //针对某些设备会缓存几帧再编码，因此需要判断是否是第一帧并记录缓存的帧数。
        if (isFirstFrame) {
            while (true) {
                if (mMediaCodec != null) {
                    int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                    int flag = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                        } else {
                            inputBuffer = mMediaCodec.getInputBuffers()[inputBufferIndex];
                        }
                        if (inputBuffer != null && yuvData != null) {
                            inputBuffer.clear();
                            inputBuffer.put(yuvData);
                            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, yuvData.length, computePresentationTime(generateIndex), flag);
                            generateIndex++;
                            cacheFrameCount++;
                        }
                    }

                    int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                    while (outputBufferIndex >= 0) {
                        ByteBuffer outputBuffer = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                        } else {
                            outputBuffer = mMediaCodec.getOutputBuffers()[outputBufferIndex];
                        }
                        if (outputBuffer != null && bufferInfo.size > 0) {
                            byte[] buffer = new byte[bufferInfo.size];
                            outputBuffer.get(buffer, 0, buffer.length);

                            if (bufferInfo.flags == 2) {
                                configByte = new byte[bufferInfo.size];
                                configByte = buffer;
                            } else {// if (bufferInfo.flags == 1)
                                h264Data = new byte[buffer.length + configByte.length];
                                System.arraycopy(configByte, 0, h264Data, 0, configByte.length);
                                System.arraycopy(buffer, 0, h264Data, configByte.length, buffer.length);
                                cacheFrameCount--;
                            }
                        }

                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                    }
                    if (h264Data != null) {
                        break;
                    }
                }
            }
        } else {
            for (int i = 0; i < cacheFrameCount + 1; i++) {
                if (mMediaCodec != null) {
                    int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                    int flag = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                        } else {
                            inputBuffer = mMediaCodec.getInputBuffers()[inputBufferIndex];
                        }
                        if (inputBuffer != null && yuvData != null) {
                            inputBuffer.clear();
                            inputBuffer.put(yuvData);
                            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, yuvData.length, computePresentationTime(generateIndex), flag);
                            generateIndex++;
                            cacheFrameCount++;
                        }
                    }

                    int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                    while (outputBufferIndex >= 0) {
                        ByteBuffer outputBuffer = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                        } else {
                            outputBuffer = mMediaCodec.getOutputBuffers()[outputBufferIndex];
                        }
                        if (outputBuffer != null && bufferInfo.size > 0) {
                            byte[] buffer = new byte[bufferInfo.size];
                            outputBuffer.get(buffer, 0, buffer.length);

                            if (bufferInfo.flags == 2) {
                                configByte = new byte[bufferInfo.size];
                                configByte = buffer;
                            } else {// if (bufferInfo.flags == 1)
                                h264Data = new byte[buffer.length + configByte.length];
                                System.arraycopy(configByte, 0, h264Data, 0, configByte.length);
                                System.arraycopy(buffer, 0, h264Data, configByte.length, buffer.length);
                                cacheFrameCount--;
                            }
                        }

                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                    }
                }
            }
        }
        isFirstFrame = false;
        return h264Data;
    }

    public byte[] encodeImage(File file, int type) throws Exception {
        byte[] fileData = FileUtils.readFile4Bytes(file);
        return encodeImage(fileData, type);
    }

    public byte[] encodeImage(byte[] fileData, int type) throws Exception {
        byte[] yuvData = null;
        if (fileData == null)
            return null;

        BitmapFactory.Options options;
        int height;
        int width;
        int size;

        switch (type) {
            case TYPE_YUV:
                if (!isI420)
                    yuvData = ImageConvertTools.I420toNV12(fileData);
                else
                    yuvData = fileData;
                break;
            case TYPE_JPG:
                options = new BitmapFactory.Options();
                options.inScaled = false;
                //       options.inSampleSize = 1;   //width，hight设为原来的十分一
                Bitmap bitmap = BitmapFactory.decodeByteArray(fileData, 0, fileData.length, options);
                //获取图片的宽高
                height = options.outHeight;
                width = options.outWidth;

                if (this.width != width || this.height != height)
                    resetEncoder(width, height);

                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                byte[] bmpData = ImageConvertTools.pixels2bmp(pixels, width, height);

                if (isI420)
                    yuvData = ImageConvertTools.convertI420(bmpData, width, height);
                else
                    yuvData = ImageConvertTools.convertNV12(bmpData, width, height);

                size = width * height * 3 / 2;
                if (yuvData.length > size) {
                    byte[] src = yuvData;
                    yuvData = new byte[size];
                    System.arraycopy(src, 0, yuvData, 0, size);
                }
                break;
            case TYPE_BMP:
                options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;//这个参数设置为true才有效，
                BitmapFactory.decodeByteArray(fileData, 0, fileData.length, options);
                //获取图片的宽高
                height = options.outHeight;
                width = options.outWidth;
                if (this.width != width || this.height != height)
                    resetEncoder(width, height);

                if (isI420)
                    yuvData = ImageConvertTools.convertI420(fileData, width, height);
                else
                    yuvData = ImageConvertTools.convertNV12(fileData, width, height);

                size = width * height * 3 / 2;
                if (yuvData.length > size) {
                    byte[] src = yuvData;
                    yuvData = new byte[size];
                    System.arraycopy(src, 0, yuvData, 0, size);
                }
                break;
        }
        return encoderData(yuvData);
    }

    /**
     * 图像宽高变化，需要重新设置编码器
     *
     * @param width
     * @param height
     */
    private void resetEncoder(int width, int height) {
        release();
        this.width = width;
        this.height = height;
        this.max_bit_rate = width * height * 3 / 2;
        try {
            mMediaCodec = MediaCodec.createEncoderByType(mimeType);
        } catch (IOException e) {
            mMediaCodec = null;
            return;
        }
        if (mMediaFormat != null) {
            mMediaFormat.setInteger(MediaFormat.KEY_WIDTH, width);
            mMediaFormat.setInteger(MediaFormat.KEY_HEIGHT, height);
            mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, max_bit_rate * compressRatio / 100);

            mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
            isFirstFrame = true;
            cacheFrameCount = 0;
            generateIndex = 0;
        } else {
            mMediaFormat = MediaFormat.createVideoFormat(mimeType, width, height);
            //码率越低，图片越模糊
            //2.56x10
            mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 3 / 2 * compressRatio / 100);
            mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, DEFAULT_FRAMERATE);
            mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, DEFAULT_I_FRAME_INTERVAL);
            int color_format = getSupportColorFormat();
            mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, color_format);

            mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
            isFirstFrame = true;
            cacheFrameCount = 0;
            generateIndex = 0;
        }
    }

    /**
     * 停止硬件编码
     */
    public void stopEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
        }
    }

    /**
     * release all resource that used in Encoder
     */
    public void release() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / DEFAULT_FRAMERATE;
    }


}
