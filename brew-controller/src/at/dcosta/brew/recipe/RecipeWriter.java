package at.dcosta.brew.recipe;

public class RecipeWriter {

	private final StringBuilder xml;

	private boolean tagIsOpen;
	private boolean includeNewlines;

	public RecipeWriter(Recipe recipe, boolean includeNewlines) {
		this.xml = new StringBuilder();
		this.includeNewlines = includeNewlines;
		toXml(recipe);
	}

	public String getRecipeAsXmlString() {
		return xml.toString();
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
		closeTagIfNecessary();
		xml.append("</").append(elementName).append(">");
		newline();
		return this;
	}

	private RecipeWriter endEmptyElement() {
		xml.append("/>");
		newline();
		tagIsOpen = false;
		return this;
	}

	private void newline() {
		if (includeNewlines) {
			xml.append('\n');
		}
	}

	private RecipeWriter startElement(String elementName) {
		closeTagIfNecessary();
		xml.append('<').append(elementName);
		tagIsOpen = true;
		return this;
	}

	private void toXml(Recipe recipe) {
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		startElement("recipe");
		attribute("version", "1.0");
		attribute("name", recipe.getName());
		attribute("type", recipe.getBrewType());
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
		Ingredient yeast = recipe.getYeast();
		startElement("yeast").attribute("name", yeast.getName()).attribute("amount", yeast.getAmount())
				.endEmptyElement();
		endElement("recipe");
	}

}
