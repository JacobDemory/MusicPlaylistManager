import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Queue;


public class MyPlaylist
{
	private String description;
	private String name;
	private File playlistFile;
	//private ArrayList<Track> tracks;
	private Queue<Track> tracks ;

	private static final String WPL_PREFIX = "<?wpl version=\"1.0\"?>";
	public static final String AUDIO_M3U = "m3u";
	public static final String WPL_STR = "wpl";


	public MyPlaylist()
	{
		this.description = new String();
		this.name = new String();
		this.tracks = new ArrayDeque<Track>();
	}

	public MyPlaylist(File pFile) throws FileNotFoundException
	{
		this.playlistFile = pFile;
		this.tracks = new ArrayDeque<Track>();
		if (pFile.getName().endsWith(".wpl"))
		{
			try
			{
				DocumentBuilderFactory dBuildFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dBuildFactory.newDocumentBuilder();
				Document document = dBuilder.parse(pFile);
				Node titleItem = document.getElementsByTagName("title").item(0);
				this.name = titleItem.getTextContent();
				Node descItem = document.getElementsByTagName("author").item(0);
				this.description = descItem.getTextContent();
				NodeList mediaItem = document.getElementsByTagName("media");

				for (int i = 0; i < mediaItem.getLength(); i++)
				{
					Track track = new Track(mediaItem.item(i).getAttributes().getNamedItem("src").getTextContent());

					this.tracks.add(track);
				}
			}
			catch (FileNotFoundException e)
			{
				throw new FileNotFoundException();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("File format not supported.");
		}
	}

	public void savePlayList(String file)
	{
		String result = "";
		switch(file)
		{
			case WPL_STR:
			{
				result = this.wplParser();
				break;
			}
			case AUDIO_M3U:
			{
				break;
			}
			default:
			{
				System.out.println("File format not supported: " + file);
				return;
			}
		}

		try
		{
			PrintWriter writer = new PrintWriter(this.getPlayListFile().getAbsolutePath());
			writer.println(result);
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public String wplParser()
	{
		String result = WPL_PREFIX;
		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document document = docBuilder.newDocument();

			Element smilEle = document.createElement("smil");
			document.appendChild(smilEle);
			Element headEle = document.createElement("head");
			smilEle.appendChild(headEle);
			Element titleEle = document.createElement("title");
			titleEle.appendChild(document.createTextNode(this.getName()));
			headEle.appendChild(titleEle);
			Element authorEle = document.createElement("author");
			authorEle.appendChild(document.createTextNode(this.getDescription()));
			headEle.appendChild(authorEle);
			Element bodyEle = document.createElement("body");
			smilEle.appendChild(bodyEle);
			Element seqEle = document.createElement("seq");
			bodyEle.appendChild(seqEle);

			for (Track track : this.tracks)
			{
				Element mediaElement = document.createElement("media");
				mediaElement.setAttribute("src", track.getTrackFile().getAbsolutePath());
				mediaElement.setAttribute("albumTitle", track.getAlbum());
				mediaElement.setAttribute("albumArtist", track.getArtist());
				mediaElement.setAttribute("trackTitle", track.getTitle());
				mediaElement.setAttribute("trackArtist", track.getArtist());
				mediaElement.setAttribute("duration", Long.toString(track.getDuration()));
				seqEle.appendChild(mediaElement);
			}

			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformerObj = transFactory.newTransformer();
			transformerObj.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			DOMSource domSrc = new DOMSource(document);
			StringWriter strWriter = new StringWriter();
			StreamResult strResult = new StreamResult(strWriter);
			transformerObj.transform(domSrc, strResult);
			StringBuffer sbuilder = strWriter.getBuffer();
			result += sbuilder.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public boolean checkSaved()
	{
		if (this.playlistFile == null && this.description.equals("") && this.name.equals("") && this.tracks.size() == 0)
			return true;

		if (this.playlistFile == null)
			return false;
		String wplOutput = this.wplParser();
		String fileResult = new String();
		try
		{
			fileResult = Files.readAllLines(this.playlistFile.toPath()).get(0);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		if (wplOutput.equals(fileResult))
		{
			return true;
		}
		return false;
	}

	public String getName()
	{
		return this.name;
	}


	public String getDescription()
	{
		return this.description;
	}


	public void setName(String name)
	{
		this.name = name;
	}


	public void setDescription(String description)
	{
		this.description = description;
	}

	public Queue<Track> getTracks()
	{
		return this.tracks;
	}



	public int getIndexOf(Track track)
	{
		Queue<Track> queueCopy = new ArrayDeque<Track>(tracks);
		for(int i=0; i < tracks.size(); i++){
			Track c = queueCopy.remove();
			if (c.equals(track)) {
				return i;
			}
		}
		return -1;
	}


	public Track getTrackById(int id)
	{
		if (id <= (this.tracks.size() - 1) && id >= 0)
		{
			return getById(id);
		}
		return null;
	}


	public Track getById(int index) {
		Queue<Track> queueCopy = new ArrayDeque<Track>(tracks);
		for(int i=0; i<index; i++){
			queueCopy.remove();
		}
		return queueCopy.peek();
	}

	public Track getLast()
	{
		if (this.tracks.size() != 0)
		{
			return getById(this.tracks.size() - 1);
		}
		return null;
	}

	public File getPlayListFile()
	{
		return this.playlistFile;
	}

	public void setPlayListFile(File file)
	{
		this.playlistFile = file;
	}

	public int getCount()
	{
		return this.tracks.size();
	}

	public String getDuration()
	{
		long duration = 0;

		for (Track track : this.tracks)
		{
			duration += track.getDuration();
		}

		int hrs = (int) duration / 3600;
		int rem = (int) duration - hrs * 3600;
		int min = rem / 60;
		rem = rem - min * 60;
		int sec = rem;

		return (((hrs == 0) ? "" : hrs + "h ") + ((hrs == 0 && min == 0) ? "" : min + "m ") + sec + "s");
	}


	public void add(Track track)
	{
		this.tracks.add(track);
	}

	public void remove(int id)
	{

		this.tracks.remove(getById(id));
	}

	public Track[] getTrackArray() {
		Queue<Track> queueCopy = new ArrayDeque<Track>(tracks);
		Track array[] = new Track[tracks.size()];
		for(int i=0; i < tracks.size(); i++){
			array[i] = queueCopy.remove();
		}
		return array;
	}

	public void updateTracks(Track[] ts) {
		Queue<Track> queueCopy = new ArrayDeque<Track>();;
		for (Track t : ts) {
			queueCopy.add(t);
		}
		tracks = queueCopy;
	}

	/*public void swap(int index1, int index2)
	{
		Collections.swap(this.tracks, index1, index2);
	}*/

}