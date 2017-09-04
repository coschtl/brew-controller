package at.dcosta.brew.recipe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import at.dcosta.brew.util.IOUtils;

public class RecipeWriter {

	private final StringBuilder xml;

	private boolean tagIsOpen;
	private boolean prettyPrint;
	private int depth;

	public RecipeWriter(Recipe recipe, boolean prettyPrint) {
		this.xml = new StringBuilder();
		this.prettyPrint = prettyPrint;
		toXml(recipe);
	}

	public String getRecipeAsXmlString() {
		return xml.toString();
	}

	public void writeTo(File file) throws IOException {
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			writeTo(out);
			out.flush();
		} finally {
			out.close();
		}
	}

	public void writeTo(OutputStream out) throws IOException {
		try {
			OutputStreamWriter writer = null;
			try {
				writer = new OutputStreamWriter(out, "utf-8");
				writer.write(getRecipeAsXmlString());
				writer.flush();
			} finally {
				IOUtils.close(writer);
			}
		} catch (UnsupportedEncodingException e) {
			// utf-8 must be present!
			e.printStackTrace();
		}
	}

	private RecipeWriter attribute(String name, float value) {
		attribute(name, Float.toString(value));
		return this;
	}

	private RecipeWriter attribute(String name, int value) {
		attribute(name, Integer.toString(value));
		return this;
	}

	private RecipeWriter attribute(String name, String value) {
		xml.append(' ').append(name).append("=\"").append(value).append('"');
		return this;
	}

	private void closeTagIfNecessary() {
		if (tagIsOpen) {
			xml.append('>');
			newline();
			tagIsOpen = false;
		}
	}

	private RecipeWriter endElement(String elementName) {
		depth--;
		closeTagIfNecessary();
		indent();
		xml.append("</").append(elementName).append(">");
		newline();
		return this;
	}

	private RecipeWriter endEmptyElement() {
		xml.append("/>");
		newline();
		depth--;
		tagIsOpen = false;
		return this;
	}

	private void indent() {
		if (prettyPrint) {
			for (int i = 0; i < depth; i++) {
				xml.append("  ");
			}
		}
	}

	private void newline() {
		if (prettyPrint) {
			xml.append('\n');
		}
	}

	private RecipeWriter startElement(String elementName) {
		closeTagIfNecessary();
		indent();
		xml.append('<').append(elementName);
		tagIsOpen = true;
		depth++;
		return this;
	}

	private void toXml(Recipe recipe) {
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		newline();
		startElement("recipe");
		attribute("version", "1.0");
		attribute("name", recipe.getName());
		attribute("type", recipe.getBrewType());
		attribute("fermentationType", recipe.getFermentationType().toString());
		attribute("wort", recipe.getWort());
		attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		attribute("xsi:noNamespaceSchemaLocation", "InfusionRecipe.xsd");
		startElement("malts");
		for (Ingredient malt : recipe.getMalts()) {
			startElement("malt").attribute("name", malt.getName()).attribute("amount", malt.getAmount())
					.endEmptyElement();
		}
		endElement("malts");
		if (recipe instanceof InfusionRecipe) {
			InfusionRecipe ir = (InfusionRecipe) recipe;
			startElement("mashing").attribute("water", recipe.getPrimaryWater()).attribute("temperature",
					recipe.getMashingTemperature());
			for (Rest rest : ir.getRests()) {
				startElement("rest").attribute("temperature", rest.getTemperature())
						.attribute("time", rest.getMinutes()).endEmptyElement();
			}
			endElement("mashing");
		}

		startElement("lautering").attribute("rest", recipe.getLauteringRest())
				.attribute("water", recipe.getSecondaryWater()).endEmptyElement();

		startElement("boiling").attribute("time", recipe.getBoilingTime());
		for (Hop hop : recipe.getHops()) {
			startElement("hop").attribute("name", hop.getName()).attribute("alpha", hop.getAlpha())
					.attribute("amount", hop.getAmount()).attribute("boilingTime", hop.getBoilingTime())
					.endEmptyElement();
		}
		endElement("boiling");
		startElement("whirlpool").attribute("time", recipe.getWhirlpoolTime()).endEmptyElement();
		startElement("fermentation");
		Ingredient yeast = recipe.getYeast();
		startElement("yeast").attribute("name", yeast.getName()).attribute("amount", yeast.getAmount())
				.endEmptyElement();
		for (Hop hop : recipe.getColdHops()) {
			startElement("hop").attribute("name", hop.getName()).attribute("alpha", hop.getAlpha())
					.attribute("amount", hop.getAmount()).endEmptyElement();
		}
		endElement("fermentation");
		endElement("recipe");
	}

}
