import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;


public class BackupManager
{
	private File currentFile;
	private String defaultBrowDirectory;
	private static final String FILE_NAME = "defaults.conf";
	private String defaultDirectory;

	public BackupManager()
	{
		this.currentFile = new File(FILE_NAME);
		if (this.currentFile.exists())
		{
			try
			{
				DocumentBuilderFactory docBulderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = docBulderFactory.newDocumentBuilder();
				Document document = builder.parse(this.currentFile);
				Node defaultBrNode = document.getElementsByTagName("defaultBrowserDir").item(0);
				this.defaultBrowDirectory = (defaultBrNode.getTextContent().equals("")) ? null : defaultBrNode.getTextContent();
				Node defaultDirNode = document.getElementsByTagName("defaultOpenPlaylistDir").item(0);
				this.defaultDirectory = (defaultDirNode.getTextContent().equals("")) ? null : defaultDirNode.getTextContent();
			}
			catch (NullPointerException e)
			{
				return;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void saveDefaultsToFile()
	{
		try
		{
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();
			Element defElement = document.createElement("Defaults");
			document.appendChild(defElement);
			Element defElement1 = document.createElement("defaultBrowserDir");
			if (this.defaultBrowDirectory != null)
			{
				defElement1.appendChild(document.createTextNode(this.defaultBrowDirectory));
			}
			defElement.appendChild(defElement1);

			Element defDirEement = document.createElement("defaultOpenPlaylistDir");

			if (this.defaultDirectory != null)
			{
				defDirEement.appendChild(document.createTextNode(this.defaultDirectory));
			}
			defElement.appendChild(defDirEement);
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transObject = transFactory.newTransformer();
			transObject.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			DOMSource domSrc = new DOMSource(document);
			StringWriter strWriter = new StringWriter();
			StreamResult strResult = new StreamResult(strWriter);
			transObject.transform(domSrc, strResult);
			StringBuffer sbuilder = strWriter.getBuffer();
			PrintWriter pwriter = new PrintWriter(this.currentFile.getAbsolutePath());
			pwriter.println(sbuilder.toString());
			pwriter.close();
		}
		catch (ParserConfigurationException | TransformerException | FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public String getDefaultDirectory()
	{
		return this.defaultDirectory;
	}

	public void setDefaultDirectory(String directory)
	{
		this.defaultDirectory = directory;
	}

	public void setDefaultBrowserDir(String path)
	{
		this.defaultBrowDirectory = path;
	}

	public String getDefaultBrowseDirectory()
	{
		return this.defaultBrowDirectory;
	}

}