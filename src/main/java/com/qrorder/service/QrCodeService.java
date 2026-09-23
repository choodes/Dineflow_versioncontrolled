package com.qrorder.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QrCodeService {

    /**
     * Encodes the given URL (a table's full customer ordering link, e.g.
     * "http://192.168.1.5:8080/customer.html?table=table-token-1") as a PNG
     * QR code image. This is what would be printed and stuck to the table,
     * or shown on the e-ink display discussed earlier - either way, a real
     * phone camera app can scan it directly with no extra software.
     */
    public byte[] generatePng(String content, int sizePx) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }
}
