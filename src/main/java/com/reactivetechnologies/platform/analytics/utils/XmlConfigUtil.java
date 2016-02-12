package com.reactivetechnologies.platform.analytics.utils;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XmlConfigUtil
{
	/**
	 * Formats a xml string
	 * @param input
	 * @param indent
	 * @return
	 */
	public static String prettyFormatXml(String input, int indent)
	{
		try
		{
			Source xmlInput = new StreamSource(new StringReader(input));
			StringWriter stringWriter = new StringWriter();
			StreamResult xmlOutput = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
			
			return xmlOutput.getWriter().toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return input;
	}

}
