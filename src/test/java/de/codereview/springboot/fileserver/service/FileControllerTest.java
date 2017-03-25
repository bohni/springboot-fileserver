package de.codereview.springboot.fileserver.service;

import de.codereview.springboot.fileserver.service.converter.ConverterService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc                   // full spring application context...
//@WebMvcTest(FileController.class)     // just the web layer...
public class FileControllerTest
{
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FileService fileService;

	@Mock
	private Stream<Path> pathStream;

	@MockBean
	private FileTypeService fileTypeService;

	@MockBean
	private ConverterService converterService;

	@Captor
	ArgumentCaptor<String> stringCaptor;

	@Captor
	ArgumentCaptor<byte[]> byteArrayCaptor;

	private Map<String, Path> boxes = new HashMap<>();

	private static final String BOX_NAME = "some-random-string";
	private static final Path BOX_PATH = Paths.get("some/random/path");
	private static final String DIR_PATH = "some/sub/dir";
	private static final String FILE_PATH = "dummy-file.txt";

	@Before
	public void setUp() {
		boxes = new HashMap<>();
		boxes.put(BOX_NAME, BOX_PATH);
	}

	@Test
	public void shouldServeFileContentUsingFileService() throws Exception {
		when(fileService.getBoxList()).thenReturn(boxes.keySet());
		when(fileService.getBoxPath(BOX_NAME)).thenReturn(BOX_PATH);
		when(fileService.getFilePath(anyString(), anyString())).thenReturn(
				BOX_PATH.resolve(DIR_PATH + "/" + FILE_PATH));
		when(fileService.listDir(BOX_PATH)).thenReturn(pathStream);
		byte[] content = {42, 43, 44};
		when(fileService.readFile(any())).thenReturn(content);
		when(fileTypeService.detectMimeType(any())).thenReturn("text/plain");

		MvcResult result = mockMvc.perform(get("/fs/" + BOX_NAME + "/" + DIR_PATH + "/" + FILE_PATH))
				.andDo(print()).andExpect(status().isOk()).andReturn();

		org.junit.Assert.assertThat(result.getResponse().getContentAsByteArray(),
				org.hamcrest.Matchers.equalTo(content));
	}
}
