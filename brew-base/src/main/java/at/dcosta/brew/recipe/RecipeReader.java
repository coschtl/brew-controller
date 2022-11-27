package at.dcosta.brew.recipe;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import at.dcosta.brew.recipe.Recipe.FermentationType;
import at.dcosta.brew.util.IOUtils;

public class RecipeReader {

	private static final Integer NOT_PRESENT = Integer.valueOf(-1);
	private static final List<Hop> NO_COLD_HOPS = Collections.emptyList();

	public static Recipe loadSampleRecipe() {
		ClassLoader c = RecipeReader.class.getClassLoader();
		return read(c.getResourceAsStream("at/dcosta/brew/recipe/SampleRecipe.xml"));
	}

	public static Recipe read(File recipeFile) {
		InputStream in = null;
		try {
			in = new FileInputStream(recipeFile);
			return read(in);
		} catch (FileNotFoundException e) {
			throw new RecipeException("Can not read recipe: " + e.toString());
		} finally {
			IOUtils.close(in);
		}
	}

	public static Recipe read(InputStream in) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(in);
			Element root = doc.getDocumentElement();

			Recipe recipe = getRecipe(root);
			NodeList childs = root.getElementsByTagName("*");
			for (int i = 0; i < childs.getLength(); i++) {
				Element child = (Element) childs.item(i);
				switch (child.getTagName()) {
				case "malts":
					addMalts(recipe, child);
					break;
				case "mashing":
					addMashing((InfusionRecipe) recipe, child);
					break;
				case "lautering":
					addLautering(recipe, child);
					break;
				case "boiling":
					addBoiling(recipe, child);
					break;
				case "whirlpool":
					addWhirlpool(recipe, child);
					break;
				case "fermentation":
					addFermentation(recipe, child);
					break;
				case "yeast": // to be able to read old recipes without
								// coldHopping capabilities
					recipe.setFermentation(new Ingredient(child.getAttribute("name"), getIntAttribute("amount", child)),
							NO_COLD_HOPS);
					break;
				}
			}
			return recipe;
		} catch (RecipeException e) {
			throw e;
		} catch (Exception e) {
			throw new RecipeException("Can not read recipe: " + e.toString());
		}

	}

	public static Recipe read(String recipeAsSting) {
		InputStream in = null;
		try {
			in = new ByteArrayInputStream(recipeAsSting.getBytes("utf-8"));
			return read(in);
		} catch (UnsupportedEncodingException e) {
			throw new RecipeException("Can not read recipe: " + e.toString());
		} finally {
			IOUtils.close(in);
		}
	}

	private static void addBoiling(Recipe recipe, Element boiling) {
		recipe.setBoilingTime(getIntAttribute("time", boiling));
		NodeList childs = boiling.getElementsByTagName("hop");
		for (int i = 0; i < childs.getLength(); i++) {
			recipe.addHop(createHop((Element) childs.item(i)));
		}
	}

	private static void addFermentation(Recipe recipe, Element fermentation) {
		Element yeastElement = (Element) fermentation.getElementsByTagName("yeast").item(0);
		Ingredient yeast = new Ingredient(yeastElement.getAttribute("name"), getIntAttribute("amount", yeastElement));

		NodeList childs = fermentation.getElementsByTagName("hop");
		List<Hop> coldHops = new ArrayList<>();
		for (int i = 0; i < childs.getLength(); i++) {
			coldHops.add(createHop((Element) childs.item(i)));
		}
		recipe.setFermentation(yeast, coldHops);
	}

	private static void addLautering(Recipe recipe, Element lautering) {
		recipe.setLauteringRest(getIntAttribute("rest", lautering));
		recipe.setSecondaryWater(getIntAttribute("water", lautering));
	}

	private static void addMalts(Recipe recipe, Element malts) {
		NodeList childs = malts.getElementsByTagName("malt");
		for (int i = 0; i < childs.getLength(); i++) {
			Element malt = (Element) childs.item(i);
			recipe.addMalt(new Ingredient(malt.getAttribute("name"), getIntAttribute("amount", malt)));
		}
	}

	private static void addMashing(InfusionRecipe recipe, Element mashing) {
		recipe.setMashingTemperature(getIntAttribute("temperature", mashing));
		recipe.setPrimaryWater(getIntAttribute("water", mashing));
		NodeList childs = mashing.getElementsByTagName("rest");
		for (int i = 0; i < childs.getLength(); i++) {
			Element restElm = (Element) childs.item(i);
			Rest rest = new Rest(i, getIntAttribute("temperature", restElm), getIntAttribute("time", restElm));
			rest.setKeepTemperature(getBooleanAttribute("keepTemperature", restElm, true));
			recipe.addRest(rest);
		}
	}

	private static void addWhirlpool(Recipe recipe, Element whirlpool) {
		recipe.setWhirlpoolTime(getIntAttribute("time", whirlpool));
	}

	private static Hop createHop(Element hopElement) {
		return new Hop(hopElement.getAttribute("name"), getIntAttribute("amount", hopElement),
				getFloatAttribute("alpha", hopElement), getIntAttribute("boilingTime", hopElement, NOT_PRESENT));
	}

	private static boolean getBooleanAttribute(String name, Element element, boolean defaultValue) {
		String stringValue = getStringAttribute(name, element, null);
		if (stringValue == null || stringValue.isEmpty()) {
			return defaultValue;
		}
		try {
			return Boolean.parseBoolean(stringValue);
		} catch (Exception e) {
			throw new RecipeException("Error in element '" + element.getTagName() + "': attribute '" + name
					+ "': does not have a integer value: " + e.toString());
		}
	}

	private static float getFloatAttribute(String name, Element element) {
		String stringValue = element.getAttribute(name);
		String err = null;
		if (stringValue == null) {
			err = "is not present";
		} else {
			try {
				return Float.parseFloat(stringValue);
			} catch (Exception e) {
				err = "can't does not have a float value: " + e.toString();
			}
		}
		throw new RecipeException("Error in element '" + element.getTagName() + "': attribute '" + name + "': " + err);
	}

	private static int getIntAttribute(String name, Element element) {
		return getIntAttribute(name, element, null);
	}

	private static int getIntAttribute(String name, Element element, Integer defaultValue) {
		String stringValue = getStringAttribute(name, element, null);
		String err;
		if (stringValue == null || stringValue.isEmpty()) {
			if (defaultValue != null) {
				return defaultValue.intValue();
			}
			err = "is not present";
		} else {
			try {
				return Integer.parseInt(stringValue);
			} catch (Exception e) {
				err = "does not have a integer value: " + e.toString();
			}
		}
		throw new RecipeException("Error in element '" + element.getTagName() + "': attribute '" + name + "': " + err);
	}

	private static Recipe getRecipe(Element root) {
		if ("infusion".equals(root.getAttribute("type"))) {
			return new InfusionRecipe(root.getAttribute("name"), root.getAttribute("source"),
					FermentationType.valueOf(root.getAttribute("fermentationType")), getFloatAttribute("wort", root));
		}
		throw new RecipeException("unknown type: " + root.getAttribute("type"));
	}

	private static String getStringAttribute(String name, Element element, String defaultValue) {
		String stringValue = element.getAttribute(name);
		if (stringValue == null) {
			return defaultValue;
		}
		return stringValue;
	}
}
