static const AVBitStreamFilter * const bitstream_filters[] = {
    &ff_aac_adtstoasc_bsf,
    &ff_h264_mp4toannexb_bsf,
    &ff_null_bsf,
    NULL };
