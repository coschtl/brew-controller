package at.dcosta.brew.recipe;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class RecipeReaderWriterTest {

	@Test
	public void testReadWrite() throws IOException {
		Recipe original = RecipeReader.loadSampleRecipe();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		RecipeWriter writer = new RecipeWriter(original, false);
		writer.writeTo(bout);

		Recipe test = RecipeReader.read(new ByteArrayInputStream(bout.toByteArray()));
		assertEquals(original, test);

	}

}
