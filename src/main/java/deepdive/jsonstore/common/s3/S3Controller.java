package deepdive.jsonstore.common.s3;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
@RestController
public class S3Controller {
	private final S3ImageService s3ImageService;

	@PostMapping("/upload")
	public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
		try {
			String fileUrl = s3ImageService.uploadImage(file);
			return ResponseEntity.ok().body("업로드 성공: " + fileUrl);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("업로드 실패: " + e.getMessage());
		}
	}
}
