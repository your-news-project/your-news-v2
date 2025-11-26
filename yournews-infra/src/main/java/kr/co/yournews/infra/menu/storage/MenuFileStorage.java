package kr.co.yournews.infra.menu.storage;

import kr.co.yournews.domain.menu.type.MenuType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuFileStorage {

    @Value("${menu.storage.base-path}")
    private String basePath;

    /**
     * PDF or PNG 다운로드 → (필요 시 PDF → PNG 변환) → 로컬 저장
     */
    public void downloadAndStore(
            String originalFileName,
            String fileUrl,
            String refererUrl,
            MenuType menuType
    ) {
        MenuPlace place = resolvePlace(menuType, originalFileName);

        try {
            // 타입별 디렉터리 생성
            Path saveDir = Paths.get(basePath, menuType.name().toLowerCase());
            Files.createDirectories(saveDir);

            // 최종 파일명 (장소에 따른 생성)
            String targetFileName = place.toFileName();
            Path targetPath = saveDir.resolve(targetFileName);

            log.info("[MenuStorage] 다운로드 경로: {} → {}", fileUrl, targetPath);

            // 파일 다운로드
            byte[] bytes = downloadFileBytes(fileUrl, refererUrl);

            String lowerName = originalFileName.toLowerCase();
            boolean isPdf = lowerName.endsWith(".pdf");

            // pdf인 경우 png로 변환 후 저장
            if (isPdf) {
                convertPdfBytesToPngFile(bytes, targetPath);
                log.info("[MenuStorage] PDF → PNG 저장: {}", targetPath);
            } else {
                Files.write(targetPath, bytes);
                log.info("[MenuStorage] image 저장: {}", targetPath);
            }

        } catch (IOException e) {
            log.error("[MenuStorage] 파일 저장 중 오류 - menuType: {}, fileUrl: {}", menuType, fileUrl, e);
        }
    }

    /**
     * HTTP 다운로드 (referrer 포함 가능 - 이미지 접근)
     */
    private byte[] downloadFileBytes(String url, String referer) throws IOException {
        URL u = new URL(url);
        URLConnection conn = u.openConnection();

        if (referer != null && !referer.isBlank()) {
            conn.setRequestProperty("Referer", referer);
        }

        try (InputStream in = conn.getInputStream()) {
            return StreamUtils.copyToByteArray(in);
        }
    }

    /**
     * PDF 바이트 → PNG 1페이지 → targetPath에 저장
     */
    private void convertPdfBytesToPngFile(byte[] pdfBytes, Path targetPath) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            if (document.getNumberOfPages() == 0) {
                throw new IOException("PDF에 페이지가 없습니다.");
            }

            PDFRenderer renderer = new PDFRenderer(document);

            // 식단표는 1페이지라 0페이지만 사용
            BufferedImage image = renderer.renderImageWithDPI(0, 150);
            Files.createDirectories(targetPath.getParent());
            ImageIO.write(image, "png", targetPath.toFile());
        }
    }

    /**
     * 원본 파일명 + menuType으로 어떤 식당(위치)인지 판별
     * - MAIN_CAFETERIA:
     * - "인문계" 포함 → INMUN_STAFF
     * - "학생회관" 포함 → STUDENT_HALL
     * - "자연계" 포함 → NATURAL_SCIENCE
     * - DORMITORY_MENU:
     * - 무조건 DORMITORY
     * - 그 외 → ETC
     */
    private MenuPlace resolvePlace(MenuType menuType, String fileName) {
        if (menuType == MenuType.DORMITORY_MENU) {
            return MenuPlace.DORMITORY;
        }

        if (fileName.contains("인문계")) {
            return MenuPlace.INMUN_STAFF;
        }
        if (fileName.contains("학생회관")) {
            return MenuPlace.STUDENT_HALL;
        }
        if (fileName.contains("자연계")) {
            return MenuPlace.NATURAL_SCIENCE;
        }

        return MenuPlace.ETC;
    }
}
