package org.home.syncBox;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.classic.methods.HttpPost;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.impl.classic.HttpClients;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpEntity;
import org.home.syncBox.dto.AuthRequestDto;
import org.home.syncBox.dto.BoxFileDto;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Ignore
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SyncBoxApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	private static final String HOST = "http://localhost";
	private static final int PORT = 5500;
	private static final String IMAGE = "openjdk:8";
	private static GenericContainer<?> APP = new GenericContainer<>(IMAGE).withExposedPorts(PORT).waitingFor(Wait.forHttp("/h2-console"));

	@BeforeAll
	public static void setUp() {
		APP.start();
	}

	@AfterAll
	public static void setDown() {
		APP.stop();
	}

	private final static String ENDPOINT_LOGIN = "/cloud/login";
	private final static String ENDPOINT_LOGOUT = "/cloud/login";
	private final static String ENDPOINT_FILE = "/cloud/file";
	private final static String ENDPOINT_LIST = "/cloud/list";

	private final static String PATH_TO_TEST_FILE = "/test/license.txt";

	private String token = "Bearer ";
	private File testFile;

	@Test
	@Order(0)
	void successLogin() {
		AuthRequestDto authRequestDto = new AuthRequestDto();
		authRequestDto.setLogin("admin");
		authRequestDto.setPassword("admin");

		ResponseEntity<String> responseEntity = restTemplate.postForEntity(
				HOST + ":" + APP.getMappedPort(PORT) + ENDPOINT_LOGIN,
				authRequestDto, String.class);
		List<String> tokenVal = responseEntity.getHeaders().getOrEmpty("auth-token");
		this.token += tokenVal.get(0);

		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		Assertions.assertEquals(tokenVal.size(), 1);
		try {
			this.testFile = createSimpleFile();
		} catch (Exception e) {}

	}

	// add file
	@Test
	@Order(1)
	void successLoadFile() throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost uploadFile = new HttpPost(HOST + ":" + APP.getMappedPort(PORT) + ENDPOINT_FILE);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("filename", this.testFile.getName(), ContentType.TEXT_PLAIN);

		builder.addBinaryBody(
				"file",
				new FileInputStream(this.testFile),
				ContentType.APPLICATION_OCTET_STREAM,
				this.testFile.getName()
		);

		HttpEntity multipart = builder.build();
		uploadFile.setEntity(multipart);
		CloseableHttpResponse response = httpClient.execute(uploadFile);
		//HttpEntity responseEntity = response.getEntity();
		Assertions.assertEquals(HttpStatus.OK, response.getCode());
	}

	// list files
	@Test
	@Order(2)
	void successListFiles() {

		//HOST + ":" + APP.getMappedPort(PORT) + ENDPOINT_LOGIN,
		BoxFileDto[] files = getListFiles();

		Assertions.assertEquals(1, files.length);
		Assertions.assertEquals(testFile.getName(), files[0].getFilename());
	}

	// change file
	@Test
	@Order(3)
	void successChangeFile() throws IOException {
//		ResponseEntity<BoxFileDto[]> responseEntity = restTemplate.(
//				HOST + ":" + APP.getMappedPort(PORT) + ENDPOINT_LIST + "?limit=3", BoxFileDto[].class);
		String NewFileName = "license_changedFileName.txt";

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		Map<String, String> param = new HashMap<>();
		param.put("filename", NewFileName);
		HttpHeaders headers = new HttpHeaders();
		headers.set("auth-token", token);
		org.springframework.http.HttpEntity<Map<String, String>> requestEntity =
				new org.springframework.http.HttpEntity<>(param, headers);
		ResponseEntity<? extends Map> response =
				restTemplate.exchange(HOST + ":" + APP.getMappedPort(PORT) + ENDPOINT_FILE,
						HttpMethod.PUT, requestEntity, param.getClass(), param);
		Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);

		BoxFileDto[] files = getListFiles();

		Assertions.assertEquals(1, files.length);
		Assertions.assertEquals(NewFileName, files[0].getFilename());

		File cngFile = new File(testFile.getParent(), NewFileName);
		Files.move(testFile.toPath(), cngFile.toPath());
		testFile = cngFile;
	}

	// remove file and list file
	@Test
	@Order(4)
	void successRemoveFileAndListForCheck() {

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		Map<String, String> param = new HashMap<>();
		param.put("filename", testFile.getName());
		HttpHeaders headers = new HttpHeaders();
		headers.set("auth-token", token);
		org.springframework.http.HttpEntity<Map<String, String>> requestEntity =
				new org.springframework.http.HttpEntity<>(param, headers);
		ResponseEntity<? extends Map> response =
				restTemplate.exchange(HOST + ":" + APP.getMappedPort(PORT) + ENDPOINT_FILE,
						HttpMethod.DELETE, requestEntity, param.getClass(), param);
		Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
		BoxFileDto[] files = getListFiles();

		Assertions.assertEquals(0, files.length);

	}

	// logout
	@Test
	@Order(5)
	void successLogout() {

		ResponseEntity<String> responseEntity = restTemplate.postForEntity(
				HOST + ":" + APP.getMappedPort(PORT) + ENDPOINT_LOGOUT,
				"", String.class);
		Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	@Order(6)
	void failedListFiles() {
		ResponseEntity<BoxFileDto[]> responseEntity = getResponseEntityForFiles();
		Assertions.assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	File createSimpleFile() throws IOException {
		Path newFile = Paths.get(PATH_TO_TEST_FILE);
		Files.createDirectories(newFile.getParent());
		Files.write(newFile, "Some license info".getBytes(StandardCharsets.UTF_8));
		return newFile.toFile();
	}

	private BoxFileDto[] getListFiles() {
		ResponseEntity<BoxFileDto[]> responseEntity = getResponseEntityForFiles();
		BoxFileDto[] files = responseEntity.getBody();
		return files;
	}

	private ResponseEntity<BoxFileDto[]> getResponseEntityForFiles() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("auth-token", token);

		ResponseEntity<BoxFileDto[]> responseEntity = new TestRestTemplate().exchange(
				HOST + ":" + APP.getMappedPort(PORT) + ENDPOINT_LIST + "?limit=3",
				HttpMethod.GET,
				new org.springframework.http.HttpEntity<Object>(headers),
				BoxFileDto[].class);
		return responseEntity;
	}

}
