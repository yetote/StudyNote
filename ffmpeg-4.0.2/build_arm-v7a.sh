#!/bin/bash
export TMPDIR=E:/ffmpeg-4.0.2/ffmpeg-4.0.2/ffmpegtemp
NDK_HOME=E:/ndk/android-ndk-r12b
PROJECT_PATH=E:/ffmpeg-4.0.2/ffmpeg-4.0.2/ffmpegtemp
echo $PROJECT_PATH
ANDROID_NDK_ROOT=$NDK_HOME
PREBUILT=$ANDROID_NDK_ROOT/toolchains/arm-linux-androideabi-4.9/prebuilt/windows-x86_64
PLATFORM=$ANDROID_NDK_ROOT/platforms/android-21/arch-arm
PREFIX=$PROJECT_PATH/build/armv7a

./configure \
--target-os=linux \
--arch=arm \
--enable-gpl \
--disable-shared \
--disable-stripping \
--disable-ffmpeg \
--disable-ffplay \
--disable-ffprobe \
--disable-avdevice \
--disable-indevs \
--disable-devices \
--disable-debug \
--disable-asm \
--disable-yasm \
--disable-doc \
--disable-bsfs \
--disable-ffplay \
--disable-ffprobe \
--disable-indevs \
--disable-outdevs \
--enable-cross-compile \
--cross-prefix=$PREBUILT/bin/arm-linux-androideabi- \
--enable-filter=aresample \
--enable-bsf=aac_adtstoasc \
--enable-bsf=h264_mp4toannexb \
--enable-small \
--enable-dct \
--enable-dwt \
--enable-lsp \
--enable-mdct \
--enable-rdft \
--enable-fft \
--enable-static \
--enable-version3 \
--enable-nonfree \
--disable-encoders \
--enable-encoder=pcm_s16le \
--enable-encoder=aac \
--enable-encoder=libmp3lame \
--enable-encoder=libvo_aacenc \
--enable-encoder=libfdk_aac \
--enable-encoder=libx264 \
--enable-encoder=mp2 \
--disable-decoders \
--enable-decoder=aac \
--enable-decoder=mjpeg \
--enable-decoder=png \
--enable-decoder=gif \
--enable-decoder=mp3 \
--enable-decoder=h264 \
--enable-decoder=pcm_s16le \
--disable-parsers \
--enable-parser=mjpeg  \
--enable-parser=png  \
--enable-parser=aac  \
--enable-parser=h264  \
--enable-parser=mpeg4video  \
--enable-parser=mpegvideo  \
--enable-parser=mpegaudio  \
--disable-muxers \
--enable-muxer=avi \
--enable-muxer=flv \
--enable-muxer=mp4 \
--enable-muxer=m4v \
--enable-muxer=mp3 \
--enable-muxer=mov \
--enable-muxer=h264 \
--enable-muxer=wav \
--enable-muxer=adts \
--disable-demuxers \
--enable-demuxer=mjpeg \
--enable-demuxer=m4v \
--enable-demuxer=gif \
--enable-demuxer=mov \
--enable-demuxer=avi \
--enable-demuxer=flv \
--enable-demuxer=h264 \
--enable-demuxer=aac \
--enable-demuxer=mp3 \
--enable-demuxer=wav \
--disable-protocols \
--enable-protocol=rtmp \
--enable-protocol=file \
--enable-protocol=http \
--disable-filters \
--enable-filter=transpose \
--enable-filter=crop \
--enable-filter=vflip \
--enable-filter=hflip \
--sysroot=$PLATFORM \
--prefix='E:/ffmpeg-4.0.2/ffmpeg-4.0.2/android/build/lib'


make clean
make -j8
make install