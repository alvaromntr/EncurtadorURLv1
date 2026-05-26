package com.tcc.url_cutter_api.service;

import com.tcc.url_cutter_api.controller.QRCodeController;
import com.tcc.url_cutter_api.utils.QRCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QRCodeController")
class QRCodeControllerTest {

    private QRCodeController qrCodeController;

    private static final String BASE_URL   = "https://short.ly/";
    private static final String SHORT_CODE = "abc123";
    private static final String FULL_URL   = BASE_URL + SHORT_CODE;

    @BeforeEach
    void setUp() {
        qrCodeController = new QRCodeController();
        // injeta o @Value manualmente, pois não há contexto Spring
        ReflectionTestUtils.setField(qrCodeController, "baseUrl", BASE_URL);
    }

    // =======================================================================
    // GET /qr/qrgenerator/{shortCode}
    // =======================================================================

    @Nested
    @DisplayName("GET /qr/qrgenerator/{shortCode}")
    class Generate {

        @Test
        @DisplayName("deve retornar 200 com bytes do QR Code e Content-Type image/png")
        void shouldReturn200WithQrCodeBytes() throws Exception {
            byte[] fakeQr = new byte[]{0x1, 0x2, 0x3, 0x4};

            try (MockedStatic<QRCodeGenerator> mocked = mockStatic(QRCodeGenerator.class)) {
                mocked.when(() -> QRCodeGenerator.generateQRCode(FULL_URL, 300, 300))
                        .thenReturn(fakeQr);

                ResponseEntity<byte[]> response = qrCodeController.generate(SHORT_CODE);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);
                assertThat(response.getBody()).isEqualTo(fakeQr);

                mocked.verify(() -> QRCodeGenerator.generateQRCode(FULL_URL, 300, 300));
            }
        }

        @Test
        @DisplayName("deve montar a URL completa concatenando baseUrl + shortCode")
        void shouldConcatenateBaseUrlAndShortCode() throws Exception {
            byte[] fakeQr = new byte[]{0x0};

            try (MockedStatic<QRCodeGenerator> mocked = mockStatic(QRCodeGenerator.class)) {
                mocked.when(() -> QRCodeGenerator.generateQRCode(FULL_URL, 300, 300))
                        .thenReturn(fakeQr);

                qrCodeController.generate(SHORT_CODE);

                // garante que a URL passada ao gerador é exatamente baseUrl + shortCode
                mocked.verify(() -> QRCodeGenerator.generateQRCode(
                        eq(BASE_URL + SHORT_CODE), eq(300), eq(300)
                ));
            }
        }

        @Test
        @DisplayName("deve gerar QR Code com dimensões 300x300")
        void shouldGenerateQrCodeWith300x300Dimensions() throws Exception {
            byte[] fakeQr = new byte[]{0x0};

            try (MockedStatic<QRCodeGenerator> mocked = mockStatic(QRCodeGenerator.class)) {
                mocked.when(() -> QRCodeGenerator.generateQRCode(FULL_URL, 300, 300))
                        .thenReturn(fakeQr);

                qrCodeController.generate(SHORT_CODE);

                mocked.verify(() -> QRCodeGenerator.generateQRCode(anyString(), eq(300), eq(300)));
            }
        }

        @Test
        @DisplayName("deve retornar 500 quando QRCodeGenerator lança exceção")
        void shouldReturn500WhenGeneratorThrows() throws Exception {
            try (MockedStatic<QRCodeGenerator> mocked = mockStatic(QRCodeGenerator.class)) {
                mocked.when(() -> QRCodeGenerator.generateQRCode(FULL_URL, 300, 300))
                        .thenThrow(new RuntimeException("Falha ao gerar QR"));

                ResponseEntity<byte[]> response = qrCodeController.generate(SHORT_CODE);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                assertThat(response.getBody()).isNull();
            }
        }

        @Test
        @DisplayName("deve retornar 500 quando QRCodeGenerator lança WriterException (checked)")
        void shouldReturn500WhenGeneratorThrowsWriterException() throws Exception {
            try (MockedStatic<QRCodeGenerator> mocked = mockStatic(QRCodeGenerator.class)) {
                mocked.when(() -> QRCodeGenerator.generateQRCode(FULL_URL, 300, 300))
                        .thenThrow(new com.google.zxing.WriterException("Erro de codificação"));

                ResponseEntity<byte[]> response = qrCodeController.generate(SHORT_CODE);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        @Test
        @DisplayName("deve funcionar com shortCodes de diferentes formatos")
        void shouldHandleDifferentShortCodeFormats() throws Exception {
            String[] shortCodes = {"x1", "ABCDEF", "123456", "aB3-xZ"};
            byte[] fakeQr = new byte[]{0x0};

            for (String code : shortCodes) {
                try (MockedStatic<QRCodeGenerator> mocked = mockStatic(QRCodeGenerator.class)) {
                    mocked.when(() -> QRCodeGenerator.generateQRCode(BASE_URL + code, 300, 300))
                            .thenReturn(fakeQr);

                    ResponseEntity<byte[]> response = qrCodeController.generate(code);

                    assertThat(response.getStatusCode())
                            .as("shortCode '%s' deveria retornar 200", code)
                            .isEqualTo(HttpStatus.OK);
                }
            }
        }
    }
}