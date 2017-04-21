package at.dcosta.brew.recipe;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import at.dcosta.brew.util.IOUtils;

public class RecipeReader {

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
				case "yeast":
					addYeast(recipe, child);
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
			Element hop = (Element) childs.item(i);
			recipe.addHop(new Hop(hop.getAttribute("name"), getIntAttribute("amount", hop),
					getFloatAttribute("alpha", hop), getIntAttribute("boilingTime", hop)));
		}
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
			Element rest = (Element) childs.item(i);
			recipe.addRest((new Rest(getIntAttribute("temperature", rest), getIntAttribute("time", rest))));
		}
	}

	private static void addWhirlpool(Recipe recipe, Element whirlpool) {
		recipe.setWhirlpoolTime(getIntAttribute("time", whirlpool));
	}

	private static void addYeast(Recipe recipe, Element yeast) {
		recipe.setYeast(new Ingredient(yeast.getAttribute("name"), getIntAttribute("amount", yeast)));
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
		String stringValue = element.getAttribute(name);
		String err = null;
		if (stringValue == null) {
			err = "is not present";
		} else {
			try {
				return Integer.parseInt(stringValue);
			} catch (Exception e) {
				err = "can't does not have a integer value: " + e.toString();
			}
		}
		throw new RecipeException("Error in element '" + element.getTagName() + "': attribute '" + name + "': " + err);
	}

	private static Recipe getRecipe(Element root) {
		if ("infusion".equals(root.getAttribute("type"))) {
			return new InfusionRecipe(root.getAttribute("name"), getFloatAttribute("wort", root));
		}
		throw new RecipeException("unknown type: " + root.getAttribute("type"));
	}
}