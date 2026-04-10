package com.king.pos.Interface;

public interface QrCodeService {
        byte[] generateQrCodePng(String text, int width, int height);

        byte[] generateBarcodePng(String text, int width, int height);

}
