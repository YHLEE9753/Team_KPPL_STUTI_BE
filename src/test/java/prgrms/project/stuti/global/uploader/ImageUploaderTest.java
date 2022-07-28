package prgrms.project.stuti.global.uploader;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
class ImageUploaderTest {

	@Autowired
	private ImageUploader imageUploader;

	@Autowired
	private ResourceLoader resourceLoader;

	private String rootPath;
	private File testImageFile;
	private File imageFile;

	@BeforeEach
	void setup() throws IOException {
		String testFilePath = Paths.get("src", "test", "resources").toString();
		rootPath = resourceLoader.getResource("classpath:static").getURL().getPath();
		testImageFile = new File(testFilePath + File.separator + "test.png");

		assertThat(testImageFile).isFile();
	}

	@AfterEach
	void cleanup() throws IOException {
		Files.deleteIfExists(Paths.get(imageFile.getAbsolutePath()));
	}

	@Test
	@DisplayName("이미지 파일을 입력된 사이즈에 맞게 리사이징하고 업로드한다.")
	void testUpload() throws IOException {
		MultipartFile testMultipartFile = getMockMultipartFile(testImageFile);

		String imageFileUrl = imageUploader.upload(testMultipartFile, ImageDirectory.STUDY_GROUP);

		assertThat(imageFileUrl).isNotNull();

		String fullPath = rootPath + File.separator + imageFileUrl;
		imageFile = new File(fullPath);

		assertThat(imageFile).isFile();
	}

	@Test
	@DisplayName("여러개의 이미지 파일을 업로드한다.")
	void testUploadAll() throws IOException {
		MultipartFile testMultipartFile = getMockMultipartFile(testImageFile);
		MultipartFile testMultipartFile2 = getMockMultipartFile(testImageFile);

		List<String> imageFileUrls = imageUploader
			.uploadAll(List.of(testMultipartFile, testMultipartFile2), ImageDirectory.STUDY_GROUP);

		assertThat(imageFileUrls).isNotNull();
		assertThat(imageFileUrls.size()).isNotZero();

		List<String> fullPaths = imageFileUrls.stream().map(i -> rootPath + File.separator + i).toList();

		for (String fullPath : fullPaths) {
			imageFile = new File(fullPath);

			assertThat(imageFile).isFile();

			Files.deleteIfExists(Paths.get(imageFile.getAbsolutePath()));
		}
	}

	@Test
	@DisplayName("업로드된 이미지파일 중 첫 번째 이미지 파일을 썸네일로 만든다.")
	void testCreateThumbnail() throws IOException {
		MultipartFile testMultipartFile = getMockMultipartFile(testImageFile);

		String imageFileUrl = imageUploader.upload(testMultipartFile, ImageDirectory.STUDY_GROUP);

		assertThat(imageFileUrl).isNotNull();

		String fullPath = rootPath + File.separator + imageFileUrl;
		imageFile = new File(fullPath);

		assertThat(imageFile).isFile();

		String thumbnailFileUrl = imageUploader.createThumbnail(imageFileUrl);

		String thumbnailFullPath = rootPath + File.separator + thumbnailFileUrl;
		File thumbnailImageFile = new File(thumbnailFullPath);

		assertThat(thumbnailImageFile).isFile();

		Files.deleteIfExists(Paths.get(rootPath, thumbnailFileUrl));
	}

	@Test
	@DisplayName("업로드된 이미지 파일을 삭제한다.")
	void testDelete() throws IOException {
		MultipartFile testMultipartFile = getMockMultipartFile(testImageFile);

		String imageFileUrl = imageUploader.upload(testMultipartFile, ImageDirectory.STUDY_GROUP);

		assertThat(imageFileUrl).isNotNull();

		String fullPath = rootPath + File.separator + imageFileUrl;
		imageFile = new File(fullPath);

		assertThat(imageFile).isFile();

		imageUploader.delete(imageFileUrl);

		assertThat(imageFile.isFile()).isFalse();
	}

	private MultipartFile getMockMultipartFile(File testFile) throws IOException {
		FileInputStream inputStream = new FileInputStream(testFile);
		String[] split = testFile.getName().split("\\.");

		return new MockMultipartFile(split[0], testFile.getName(), "image/" + split[1], inputStream);
	}
}
