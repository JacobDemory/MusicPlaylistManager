import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class AudioPlayer extends JPanel implements ActionListener
{
	private final Image SHUFFLE_IMAGE = (new ImageIcon(getClass().getResource("/images/shuffle.png"))).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
	private final Image LOOP_IMAGE = (new ImageIcon(getClass().getResource("/images/loop.png"))).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
	private final Image PLAY_IMAGE = (new ImageIcon(getClass().getResource("/images/play_button.png"))).getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
	private final Image PAUSE_IMAGE = (new ImageIcon(getClass().getResource("/images/pause_button.png"))).getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
	private final Image DEFAULT_ALBUM_ART_ICON = (new ImageIcon(getClass().getResource("/images/album.png"))).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);

	private FileInputStream fileInpstream;
	private long totalLength;
	private volatile boolean isPausedFlag = false;
	private Player playerInstance;
	private Thread playerThread;
	private JSlider timeSlider;
	private Track currentTrack;
	private MyPlaylist myPlaylist;
	private JTextField fieldTrackTitle;
	private JButton skipBackTrack;
	private JButton skipForwardTrack;
	private JButton playPauseButton;
	private JLabel albumLabel;
	private JButton loopButton;
	private boolean isLooping;
	private JButton searchButton;
	private JTextField searchField;
	private JButton shuffleButton;

	public AudioPlayer(MyPlaylist myPlaylist, JLabel albumLabel)
	{
		super();
		this.myPlaylist = myPlaylist;
		this.albumLabel = albumLabel;
		this.setLayout(null);
		this.albumLabel.setIcon(new ImageIcon(DEFAULT_ALBUM_ART_ICON));
		Image icon1 = (new ImageIcon(getClass().getResource("/images/back.png"))).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
		skipBackTrack = new JButton(new ImageIcon(icon1));
		skipBackTrack.addActionListener(this);
		skipBackTrack.setBounds(10, 50, 80, 25);
		add(skipBackTrack);

		playPauseButton = new JButton(new ImageIcon(PLAY_IMAGE));
		playPauseButton.addActionListener(this);
		playPauseButton.setBounds(135, 50, 60, 25);
		add(playPauseButton);

		shuffleButton = new JButton(new ImageIcon(SHUFFLE_IMAGE));
		shuffleButton.addActionListener(this);
		shuffleButton.setBounds(10, 125, 60, 25);
		add(shuffleButton);

		loopButton = new JButton(new ImageIcon(LOOP_IMAGE));
		loopButton.addActionListener(this);
		loopButton.setBounds(80, 125, 60, 25);
		add(loopButton);

		searchField = new JTextField();

		searchField.setBounds(150, 125, 80, 25);
		add(searchField);

		searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		searchButton.setBounds(240, 125, 90, 25);
		add(searchButton);

		Image icon2 = (new ImageIcon(getClass().getResource("/images/forward.png"))).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
		skipForwardTrack = new JButton(new ImageIcon(icon2));
		skipForwardTrack.addActionListener(this);
		skipForwardTrack.setBounds(240, 50, 80, 25);
		add(skipForwardTrack);

		fieldTrackTitle = new JTextField();
		fieldTrackTitle.setBorder(BorderFactory.createEmptyBorder());
		fieldTrackTitle.setHorizontalAlignment(SwingConstants.CENTER);
		fieldTrackTitle.setEditable(false);
		fieldTrackTitle.setBackground(SystemColor.menu);
		fieldTrackTitle.setBounds(10, 15, 310, 20);
		add(fieldTrackTitle);

		timeSlider = new JSlider() {
			{
				MouseListener[] mouseListeners = getMouseListeners();
				for (MouseListener l : mouseListeners)
					removeMouseListener(l);
				final BasicSliderUI baseUI = (BasicSliderUI) getUI();
				BasicSliderUI.TrackListener tlistener = baseUI.new TrackListener()
				{
					@Override
					public void mouseReleased(MouseEvent e)
					{
						Point point = e.getPoint();
						int val = baseUI.valueForXPosition(point.x);
						setValue(val);
						if (!isPausedFlag)
						{
							pauseTrack();
							setValue(val);
							resume();
						}
					}

					@Override public boolean shouldScroll(int dir) {
						return false;
					}
				};
				addMouseListener(tlistener);
			}
		};

		timeSlider.setValue(0);
		timeSlider.setBounds(10, 95, 310, 25);
		add(timeSlider);
	}


	private void playTrack(long initialValue)
	{
		if (this.currentTrack != null)
		{
			this.isPausedFlag = false;
			if (playerInstance != null)
			{
				playerInstance.close();
				playerInstance = null;
			}
			try
			{
				this.fileInpstream = new FileInputStream(this.currentTrack.getTrackFile());
				this.totalLength = this.fileInpstream.available();
				this.timeSlider.setMaximum((int) this.totalLength);
				this.fileInpstream.skip(initialValue);
				playerInstance = new Player(this.fileInpstream);
				this.playerThread = new Thread("Audio Player")
				{
					public void run()
					{
						try
						{
							refreshGUI();
							playPauseButton.setIcon(new ImageIcon(PAUSE_IMAGE));
							playerInstance.play();
							if (playerInstance != null && playerInstance.isComplete())
							{
								skipForwardTrack();
							}
						}
						catch (JavaLayerException e)
						{
							e.printStackTrace();
						}
					}
				};

				this.playerThread.start();
				Set<Thread> threads = Thread.getAllStackTraces().keySet();
				for (Thread th : threads)
				{
					if (th.getName().equals("Track Timer Thread"))
					{
						return;
					}
				}

				Thread progressThread = new Thread("Track Timer Thread") {
					public void run()
					{
						while (!this.isInterrupted())
						{
							try
							{
								if (playerInstance.isComplete() || isPausedFlag)
								{
									break;
								}
								updateTimerProgress();
								Thread.sleep(100);
							}
							catch (NullPointerException | InterruptedException e)
							{
								break;
							}
						}
					}
				};
				progressThread.start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object currentSource = e.getSource();

		if (currentSource == skipBackTrack)
		{
			skipBackTrack();
		}
		else if (currentSource == playPauseButton)
		{
			playOrPause();
		}
		else if (currentSource == skipForwardTrack)
		{
			skipForwardTrack();
		}
		else if (currentSource == loopButton)
		{
			loopTrack();
		}
		else if (currentSource == searchButton)
		{
			searchTracks();
		}

		else if (currentSource == shuffleButton)
		{
			shuffleTracks();
		}
	}



	private void shuffleTracks() {
		MyPlaylist currentPlayList = getPlayList();
		if (currentPlayList != null) {
			Track[] currentTracks = currentPlayList.getTrackArray();
			Track[] shuffled = shuffle(currentTracks);
			currentPlayList.updateTracks(shuffled);
			updateList(currentPlayList);
		}

	}


	private Track[] shuffle(Track[] currentTracks) {
		Track[] shuffled = new Track[currentTracks.length];
		List<Track> tList = Arrays.asList(currentTracks);
		Collections.shuffle(tList);
		tList.toArray(shuffled);
		return shuffled;
	}


	private void searchTracks() {
		String title = searchField.getText();
		if (title.isEmpty() || title.isBlank()) {
			return;
		}

		BinaryTrackTree bTree = new BinaryTrackTree();
		Track tracks[] = myPlaylist.getTrackArray();
		for (int i=0; i<tracks.length;i++) {
			bTree.insert(tracks[i]);
		}

		TreeNode node = bTree.find(bTree.root, title);
		if (node !=null) {
			this.currentTrack = node.key;
			this.playTrack(0);
		}
	}


	private void loopTrack() {
		if (isLooping) {
			isLooping = false;
		}
		else {
			isLooping = true;
		}
	}


	public void playOrPause()
	{
		if (this.playerInstance != null)
		{
			if (this.isPausedFlag)
			{
				this.resume();
			}
			else
			{
				this.pauseTrack();
			}
		}
		else
		{
			if (this.myPlaylist.getTrackById(0) != null)
			{
				this.currentTrack = this.myPlaylist.getById(0);
				this.playTrack(0);
			}
		}
	}

	private void playTrack()
	{
		this.playTrack(this.timeSlider.getValue());
	}

	private void resume()
	{
		playTrack(this.timeSlider.getValue());
	}


	private void pauseTrack()
	{
		this.playPauseButton.setIcon(new ImageIcon(PLAY_IMAGE));
		if (playerInstance != null)
		{
			this.playerInstance.close();
		}
		this.isPausedFlag = true;
	}

	public void skipForwardTrack()
	{
		if (this.myPlaylist.getLast() != null)
		{
			if (this.currentTrack != this.myPlaylist.getLast())
			{
				this.currentTrack = this.myPlaylist.getTrackById(this.myPlaylist.getIndexOf(this.currentTrack) + 1);
				this.playTrack(0);
			}
			else
			{
				if (isLooping) {
					Track first = this.myPlaylist.getById(0);
					this.currentTrack = first;
					this.playTrack(0);
				}
				else {
					this.pauseTrack();
					this.timeSlider.setValue(0);
				}
			}
		}
	}


	private void skipBackTrack()
	{
		if (this.currentTrack != this.myPlaylist.getTrackById(0) && this.myPlaylist.getCount() > 0)
		{
			this.currentTrack = this.myPlaylist.getTrackById(this.myPlaylist.getIndexOf(this.currentTrack) - 1);
		}
		this.playTrack(0);
	}


	public void splitCurrentTrackAndWriteToFile(File outputFile, boolean isToSplit)
	{
		if (this.currentTrack != null)
		{
			try
			{
				FileInputStream currentInpStream = new FileInputStream(this.currentTrack.getTrackFile());
				int point1, point2;
				if (isToSplit)
				{
					point1 = 0;
					point2 = this.timeSlider.getValue();
				}
				else
				{
					point1 = this.timeSlider.getValue();
					point2 = (int) this.totalLength;
				}
				currentInpStream.skip((long) point1);
				FileOutputStream fops = new FileOutputStream(outputFile);
				int remaining = point2 - point1;
				byte[] dataBuffer = new byte[remaining];
				currentInpStream.read(dataBuffer);
				fops.write(dataBuffer);
				currentInpStream.close();
				fops.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return;
			}
		}
	}

	public void updateList(MyPlaylist mplaylist)
	{
		this.myPlaylist = mplaylist;
		if (mplaylist.getCount() > 0)
		{
			Track firstTrack = mplaylist.getById(0);
			this.currentTrack = firstTrack;
		}
		this.refreshGUI();
	}

	public void refreshGUI()
	{
		try
		{
			this.albumLabel.setIcon(new ImageIcon(new ImageIcon(this.currentTrack.getAlbumArt()).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH)));
		}
		catch (NullPointerException e)
		{
			this.albumLabel.setIcon(new ImageIcon(DEFAULT_ALBUM_ART_ICON));
		}

		if (this.currentTrack != null)
			this.fieldTrackTitle.setText(this.currentTrack.getTitle() + " - " + this.currentTrack.getArtist());
	}


	public void updateTimerProgress()
	{
		int result;
		try
		{
			result = (int) (this.totalLength - this.fileInpstream.available());
		}
		catch (IOException e)
		{
			return;
		}
		this.timeSlider.setValue(result);
	}


	public Track getCurrentTrack()
	{
		return this.currentTrack;
	}

	public void setCurrentTrack(Track track)
	{
		this.currentTrack = track;
		this.pauseTrack();
		this.timeSlider.setValue(0);
		try
		{
			FileInputStream fips = new FileInputStream(this.currentTrack.getTrackFile());
			this.timeSlider.setMaximum((int) fips.available());
			fips.close();
		}
		catch (IOException e)
		{
			return;
		}
	}


	public MyPlaylist getPlayList() {
		return myPlaylist;
	}

}