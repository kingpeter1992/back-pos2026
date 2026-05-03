package com.king.pos.ImplementServices;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.king.pos.Interface.QrCodeService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
@Service
public class BarcodeServiceImpl implements QrCodeService {

@Override
public byte[] generateBarcodePng(String text, int width, int height) {
    try {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code-barres est vide.");
        }

        BitMatrix matrix = new Code128Writer().encode(
                text.trim(),
                BarcodeFormat.CODE_128,
                width,
                height
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);

        return outputStream.toByteArray();

    } catch (Exception e) {
        throw new RuntimeException("Erreur génération code-barres : " + e.getMessage(), e);
    }
}

    @Override
    public byte[] generateQrCodePng(String text, int width, int height) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateQrCodePng'");
    }
}