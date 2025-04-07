package deepdive.jsonstore.common.s3;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageService {
	private final S3Client s3Client;

	// application.yml 또는 properties에 설정한 버킷명
	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucketName;
	@Value("${spring.cloud.aws.region.static}")
	private String region;


	/**
	 * MultipartFile로 받은 이미지를 S3에 업로드하고 파일 URL을 반환합니다.
	 */
	public String uploadImage(MultipartFile multipartFile) {
		// 고유 파일명 생성 (uuid + 기존 파일명)
		String key = "images/" + UUID.randomUUID()+"_"+multipartFile.getOriginalFilename();

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.contentType(multipartFile.getContentType())
				.build();

			//파일 업로드
			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
		} catch (IOException e) {
			throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
		}
		return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
	}
}
