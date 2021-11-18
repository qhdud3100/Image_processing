package 영상처리;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.SystemColor;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.Color;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

public class Frame extends JFrame implements ActionListener{

	private JPanel contentPane;
	private BufferedImage image = null;
	private Image resizedImage;
	private BufferedImage afterImage;
	private BufferedImage objectImage;
	private String fileName;
	private String path;
	private ImagePanel imagePanel;
	private JFileChooser fileChooser;
	private int brightness;
	private int width;
	private int height;
	private boolean grayScale;
	private int startX;
	private int startY;
	private BufferedImage tmpImage;
	private float brightFactor;
	private int beforeWidth;
	private int beforeHeight;
	private boolean brightOption=false;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private int filterOption;
	private final int BLUE=1;
	private final int SEPIA=2;
	private final int PRETTY=3;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame frame = new Frame();
					frame.setBackground(Color.black);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/*
	 * 
	 * 버튼 리스너
	 * 
	 */


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String button=e.getActionCommand();
		
		
		switch(button) {
		case "Upload":
			fileUpload();
			break;
		case "Save":
			if(afterImage==null) JOptionPane.showMessageDialog(null, "이미지가 존재하지 않습니다 ", "경고", JOptionPane.WARNING_MESSAGE);
			else save();
			break;
		case "Gray Scale":

			if(afterImage==null) JOptionPane.showMessageDialog(null, "이미지가 존재하지 않습니다 ", "경고", JOptionPane.WARNING_MESSAGE);
			else {
					
				if(brightOption==true) {
					//System.out.println("밝기가 최근에 변경된 상태");
					afterImage=deepCopy(tmpImage);
				}

				grayScale();

				brightOption=false;
				break;
			}
		case "Up":
			brightOption=true;
			if(afterImage==null) JOptionPane.showMessageDialog(null, "이미지가 존재하지 않습니다 ", "경고", JOptionPane.WARNING_MESSAGE);
			else upbright();
			break;
			
		case "Down":
			brightOption=true; 
			if(afterImage==null) JOptionPane.showMessageDialog(null, "이미지가 존재하지 않습니다 ", "경고", JOptionPane.WARNING_MESSAGE);
			else downbright();
			break;
			
		case "manipulation":

			if(afterImage==null) JOptionPane.showMessageDialog(null, "이미지가 존재하지 않습니다 ", "경고", JOptionPane.WARNING_MESSAGE);
			else if(brightOption) manipulation(tmpImage);
			else manipulation(afterImage);
			brightOption=false; 
			break;
			
		case "Reset":
			if(afterImage==null) JOptionPane.showMessageDialog(null, "이미지가 존재하지 않습니다 ", "경고", JOptionPane.WARNING_MESSAGE);
			else{
				afterImage=deepCopy(image);
				brightFactor=1.0F; //밝기 초기화
				repaint();
				brightOption=false;
			}break;	
			
		case "Edge detect":
			if(afterImage==null) JOptionPane.showMessageDialog(null, "이미지가 존재하지 않습니다 ", "경고", JOptionPane.WARNING_MESSAGE);
			else{
				edgeDetect();
			}
			brightOption=false;
			break;
			
		case "Blue filter":
			if(afterImage==null) JOptionPane.showMessageDialog(null, "이미지가 존재하지 않습니다 ", "경고", JOptionPane.WARNING_MESSAGE);
			else{
				filterOption=BLUE;
				filter();
			}
			brightOption=false;
			break;
			
		case "Sepia":
			if(afterImage==null) JOptionPane.showMessageDialog(null, "이미지가 존재하지 않습니다 ", "경고", JOptionPane.WARNING_MESSAGE);
			else{
				filterOption=SEPIA;
				filter();
			}
			brightOption=false;
			break;
			
		case "Pretty":
			if(afterImage==null) JOptionPane.showMessageDialog(null, "이미지가 존재하지 않습니다 ", "경고", JOptionPane.WARNING_MESSAGE);
			else{
				filterOption=PRETTY;
				filter();
			}
			brightOption=false;
			break;
			
		}
	}

	
	// 원래대로 돌릴때/초기출력
	class ImagePanel extends JPanel{
		public void paintComponent(Graphics g0) {
			
//			System.out.println("resized Image");
			
			super.paintComponent(g0);
			Graphics2D g = (Graphics2D)g0;
			
			
			adjustSize();
			
			g.drawImage(resizedImage,startX,startY,null);
		}
	}
	
	public void adjustSize() {
//		width=image.getWidth();
		beforeWidth=image.getWidth();
		beforeHeight=image.getHeight();
		
	
		try {
			
			//가로 세로 길이 조절 
			if(beforeWidth>beforeHeight) {
				width=800-160;
				height=(int)(beforeHeight*(780/(float)beforeWidth)); //사이즈 큰 사진의 높이가 0이 안되도록
			}else {
				width=(int)(beforeWidth*(600/(float)beforeHeight)); //사이즈 큰 사진의 너비가 0이 안되도록
				height=600-40;
				
			}
			
			
			resizedImage=afterImage.getScaledInstance(width,height,Image.SCALE_SMOOTH);

			//가로 세로 시작점 조절
			startX=Math.max(480-width/2, 0);
			startY=Math.max(300-height/2, 0);
			
//			if(beforeWidth>beforeHeight) {
//				startY=Math.max(300-height/2, 0)+20;
////				System.out.println("가로가 더 김");
//			}
//			else {
//				startY=Math.max(300-height/2, 0);
////				System.out.println("세로가 더 김, "+width+", "+height);
//			}
				
			
		}catch(Exception e) {
			System.out.println("사이즈 조정에서 예외 발생");
			System.out.println("width: "+width+", Height: "+ height);
		}
//		System.out.println("width is "+ width + ", height is " + height);
		


	}
	
	/*
	 * 
	 * 파일 업로드
	 * 
	 */
	
	public void fileUpload() {
		
		fileChooser= new JFileChooser();
		int result= fileChooser.showOpenDialog(null);

		if(result!= JFileChooser.APPROVE_OPTION) {
			JOptionPane.showMessageDialog(null, "파일을 선택하지 않았습니다.", "경고",JOptionPane.WARNING_MESSAGE);
		}else {

			path=fileChooser.getSelectedFile().toString();
			File inputFile = new File(path);

			fileName=fileChooser.getName(inputFile);
//			System.out.println(fileName);
			
			try {
				image=ImageIO.read(inputFile);
				repaint();
	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			brightFactor=1.0F; //밝기 초기화
			
			imagePanel = new ImagePanel();
			imagePanel.setVisible(true);
			imagePanel.setBounds(0, 0, 800, 600);
			
			getContentPane().add(imagePanel);
			
//			afterImage= new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
			afterImage=deepCopy(image);
			//변화를 위해 미리 만들어놓음

			tmpImage=deepCopy(image);
			
		}
	}
	
	
	/*
	 * 
	 * 파일 저장
	 * 
	 */
	
	public void save(){
		JFileChooser fileChooser = new JFileChooser();
		int result = fileChooser.showSaveDialog(this);
		
		if(result!=JFileChooser.APPROVE_OPTION) {
			JOptionPane.showMessageDialog(null, "저장 경로를 선택해주세요 ", "경고", JOptionPane.WARNING_MESSAGE);
		}else {

			path=fileChooser.getSelectedFile().toString();
//			System.out.println(path);
		
//			fileName=fileChooser.getName()+".jpg";
			File outFile=new File(path);
			
			if(brightOption==true) {
				afterImage=tmpImage;
//				System.out.println("저장하기 전, 최근에 바뀐 것은 밝기");
			}
			
			Graphics2D gbright = (Graphics2D)imagePanel.getGraphics();
			resizedImage=afterImage.getScaledInstance(width,height,Image.SCALE_SMOOTH);



			
			try {
				ImageIO.write(afterImage,"JPG",outFile);
			} catch (IOException e) {

				System.out.println("파일이 없습니다. ");
			}
	
			JOptionPane.showMessageDialog(null, "저장되었습니다. ", "경고",JOptionPane.WARNING_MESSAGE);
//			gbright.drawImage(afterImage, startX, startY, null); //저장확인
		}
		
	}
	
	/*
	 * 
	 * 흑백
	 * 
	 */

	
	public void grayScale() {
		
		if(brightOption) afterImage=tmpImage;

		for(int i=0;i<afterImage.getHeight();i++) {
			for(int j=0;j<afterImage.getWidth();j++) {
				
				Color color = new Color(afterImage.getRGB(j, i));
				
				int red = (int)(color.getRed() * 0.299);
				int green = (int)(color.getGreen() * 0.587);
				int blue = (int)(color.getBlue() * 0.114);
//					System.out.println(red+","+green+","+blue);
				
				Color afterColor = new Color(red+green+blue, red+green+blue, red+green+blue);
				
				if(afterImage!=null) afterImage.setRGB(j, i, afterColor.getRGB());	
				
			}

		}
		resizedImage=afterImage.getScaledInstance(width,height,Image.SCALE_SMOOTH);
		Graphics2D gscale = (Graphics2D)imagePanel.getGraphics();
		gscale.drawImage(resizedImage, startX, startY, null);
		
	}

	
	/*
	 * 
	 * 밝기 조절 
	 * 
	 */
	
	public void upbright() {
		brightFactor+=0.2F;
		changeBrightness();
	}
	
	public void downbright() {
		brightFactor-=0.2F;
		changeBrightness();
	}

	
	public void changeBrightness(){
//		System.out.println(brightFactor);
		RescaleOp op = new RescaleOp(brightFactor, 0, null);
		tmpImage= op.filter(afterImage, null);
		
		
		Graphics2D gbright = (Graphics2D)imagePanel.getGraphics();

		resizedImage=tmpImage.getScaledInstance(width,height,Image.SCALE_SMOOTH);
		gbright.drawImage(resizedImage, startX, startY, null);
	}
	
	/*
	 * 
	 * 합성
	 * 
	 */
	public void manipulation(BufferedImage backgroundImage) {
		
		
		fileChooser= new JFileChooser();
		int result= fileChooser.showOpenDialog(null);

		if(result!= JFileChooser.APPROVE_OPTION) {
			JOptionPane.showMessageDialog(null, "파일을 선택하지 않았습니다 ", "경고", JOptionPane.WARNING_MESSAGE);
		}else {

			path=fileChooser.getSelectedFile().toString();
			File inputFile = new File(path);

			fileName=fileChooser.getName(inputFile);
//			System.out.println(fileName);
			
			
			try {
				objectImage=ImageIO.read(inputFile);
			}catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("오브젝트를 불러오는데에서 예외 발생");
			}
			
//			System.out.println("width: "+objectImage.getWidth() +", height: "+objectImage.getHeight());
			
			// objectImage 를 backgroundImage에 삽입하기 
			
//			System.out.println(objectImage.getHeight());
//			System.out.println(objectImage.getWidth());
//			Image tmp = resizedImage=objectImage.getScaledInstance(width,height,Image.SCALE_SMOOTH);
//			objectImage= (BufferedImage) tmp;
			
			
			
			if(brightOption==true) afterImage=tmpImage;
			
			
			Color objectbackgroundColor = new Color(objectImage.getRGB(0,0));//오브젝트 배경색 인지
			
			System.out.println(objectbackgroundColor);
//			System.out.println(objectImage.getHeight());
			
			int objWidth;
			int objHeight;
			
			if(objectImage.getWidth()>objectImage.getHeight()) {
				//System.out.println("오브젝트 가로가 더 김");
				objWidth=800-160;
				objHeight=(int)(objectImage.getHeight()*(780/(float)objectImage.getWidth())); //사이즈 큰 사진의 높이가 0이 안되도록
			}else {
				//System.out.println("오브젝트 세로가 더 김");
				objWidth=(int)(objectImage.getWidth()*(600/(float)objectImage.getHeight())); //사이즈 큰 사진의 너비가 0이 안되도록
				objHeight=600-40;
				
			}
			
			//System.out.println(objWidth+","+objHeight);
			
			int selectedRed = 0;
			int selectedGreen = 0;
			int selectedBlue = 0;
			
			
			
			for(int i=0;i<objHeight;i++) {
				for(int j=0;j<objWidth;j++) {

//					System.out.println(backgroundColor.getRed());
//					System.out.println(objectColor);
//					if(backgroundColor==null) {
//					}
					try {
						Color objectColor = new Color(objectImage.getRGB(j, i));
						Color backgroundColor = new Color(backgroundImage.getRGB(j,i));

						
						if(objectbackgroundColor.getRed()-40<objectColor.getRed() && objectColor.getRed() <objectbackgroundColor.getRed()+40 &&
							objectbackgroundColor.getGreen()-40<objectColor.getGreen() && objectColor.getGreen() <objectbackgroundColor.getGreen()+40 &&
							objectbackgroundColor.getBlue()-40<objectColor.getBlue() && objectColor.getBlue() <objectbackgroundColor.getBlue()+40) {
	
//							System.out.println("true");
							
							selectedRed = backgroundColor.getRed();
							selectedGreen = backgroundColor.getGreen();
							selectedBlue = backgroundColor.getBlue();
							

							
						}else {
//							System.out.println("false");
							selectedRed = (int)objectColor.getRed();
							selectedGreen = (int)objectColor.getGreen();
							selectedBlue = (int)objectColor.getBlue();
						}
						
						Color afterColor = new Color(selectedRed,selectedGreen,selectedBlue);
						
						if(afterImage!=null) afterImage.setRGB(j, i, afterColor.getRGB());	
						
					}catch(Exception e) {
						System.out.println("합 성 중 ...");
					}

					
				}

			}
			
			resizedImage=afterImage.getScaledInstance(width,height,Image.SCALE_SMOOTH);
			
			Graphics2D gmanipulation = (Graphics2D)imagePanel.getGraphics();
			gmanipulation.drawImage(resizedImage, startX, startY, null);
				
			
			
			
			
			
		}
	}
	
	public void edgeDetect() {
		if(brightOption) afterImage=tmpImage;
		
		
		float[] sharpen = new float[] {
			     0.0f, -1.0f, 0.0f,
			    -1.0f, 5.0f, -1.0f,
			     0.0f, -1.0f, 0.0f
			};
		Kernel kernel = new Kernel(3, 3, sharpen);
		ConvolveOp op = new ConvolveOp(kernel);
		afterImage = op.filter(afterImage, null);
		
		double[][] filterBlur = 
			{{0.088 , 0.107 , 0.088}, 
			 {0.107 , 0.214 , 0.107},
	   	   	 {0.088 , 0.107 , 0.088}}; 
		
		double[][] filterEdge = 
			{ { -1, -1, -1 }, 
			  { -1,  8, -1  }, 
			  { -1, -1, -1  }}; 
		
		//픽셀단위 2차원 배열로 배꿔주기
//		double [][] output = new double[afterImage.getHeight()][afterImage.getWidth()];
//		for(int y = 0; y < afterImage.getHeight(); y++) {
//			for(int x = 0; x < afterImage.getWidth(); x++) {
//				Color c = new Color(afterImage.getRGB(x, y));
//				output [y][x] = (c.getRed() + c.getBlue() + c.getGreen())/3;
//			}
//		}
		
		int [][]beforeArray = new int [afterImage.getHeight()][afterImage.getWidth()];
		int [][]afterArray = new int [afterImage.getHeight()][afterImage.getWidth()];
		
		for(int y=0;y<afterImage.getHeight();y++) {
			for(int x=0;x<afterImage.getWidth();x++) {
				Color c = new Color(afterImage.getRGB(x, y));
				beforeArray[y][x] = (c.getRed() + c.getBlue() + c.getGreen())/3;
				
				for (int i = 0; i < filterBlur.length; i++) { 
					for (int j = 0; j < filterBlur[i].length; j++) { 
						
						
						try { 
							afterArray[y][x] += beforeArray[y - i + 1][x - j + 1] * filterBlur[i][j];
							afterArray[y][x] += beforeArray[y - i + 1][x - j + 1] * filterEdge[i][j];
						} catch (ArrayIndexOutOfBoundsException e) { 
							System.out.println("윤 곽 선 추 출 중. . . ");
						}

					}
				}
				//색 범위 설정
				afterArray[y][x] = Math.max(0, afterArray[y][x]); 
				afterArray[y][x] = Math.min(225, afterArray[y][x]); 
				
				//다시 이미지화
				afterImage.setRGB(x, y, new Color((int) afterArray[y][x], (int) afterArray[y][x], (int) afterArray[y][x]).getRGB()); 
		
				
			}
		}
		resizedImage=afterImage.getScaledInstance(width,height,Image.SCALE_SMOOTH);
		
		Graphics2D gedge = (Graphics2D)imagePanel.getGraphics();
		gedge.drawImage(resizedImage, startX, startY, null);
		
		
		
	}
	
	public void filter() {
		int redPlus=0;
		int bluePlus=0;
		int greenPlus=0;
		
		if(brightOption) afterImage=tmpImage;

		
		if(filterOption==BLUE) {
//			redPlus-=50;
//			greenPlus-=50;
			bluePlus+=50;
			
		}else if(filterOption==SEPIA) {
			redPlus+=50;
			greenPlus+=50;
//			bluePlus-=50;
		}else if(filterOption==PRETTY) {
			redPlus+=50;
			greenPlus+=50;
			bluePlus+=50;
		}
		
		
		for(int i=0;i<afterImage.getHeight();i++) {
			for(int j=0;j<afterImage.getWidth();j++) {
				
				Color color = new Color(afterImage.getRGB(j, i));
				
				int red = Math.min((int)(color.getRed() +redPlus),255);
				int green = Math.min((int)(color.getGreen() +greenPlus),255);
				int blue = Math.min((int)(color.getBlue() +bluePlus),255);
//					System.out.println(red+","+green+","+blue);
				
				Color afterColor = new Color(red, green, blue);
				
				if(afterImage!=null) afterImage.setRGB(j, i, afterColor.getRGB());	
				
			}

		}
		resizedImage=afterImage.getScaledInstance(width,height,Image.SCALE_SMOOTH);
		Graphics2D gscale = (Graphics2D)imagePanel.getGraphics();
		gscale.drawImage(resizedImage, startX, startY, null);
		
		
	}
	
	
	public static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	

	
	
	
	/*
	 * 
	 * 프레임
	 * 
	 */
	public Frame() {

		
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);

		getContentPane().setBackground(Color.WHITE);
		setContentPane(contentPane);
		setBounds(100, 100, 800, 600);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel selectPanel = new JPanel();
		selectPanel.setBackground(Color.LIGHT_GRAY);
		selectPanel.setBounds(0, 20, 160, 560);
		contentPane.add(selectPanel);
		selectPanel.setLayout(null);
		
		JButton blackwhiteButton = new JButton("Gray Scale");
		buttonGroup.add(blackwhiteButton);
		blackwhiteButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		blackwhiteButton.setBounds(25, 16, 105, 39);
		selectPanel.add(blackwhiteButton);
		blackwhiteButton.addActionListener(this);
		
		JButton btnUp = new JButton("Up");
		buttonGroup.add(btnUp);
		btnUp.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		btnUp.setBounds(25, 234, 49, 39);
		selectPanel.add(btnUp);
		btnUp.addActionListener(this);
		
		JButton btnDown = new JButton("Down");
		buttonGroup.add(btnDown);
		btnDown.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		btnDown.setBounds(73, 234, 57, 39);
		selectPanel.add(btnDown);
		btnDown.addActionListener(this);
		
		JLabel brightnessLabel = new JLabel("Brightness");
		brightnessLabel.setHorizontalAlignment(SwingConstants.CENTER);
		brightnessLabel.setBounds(25, 213, 105, 16);
		selectPanel.add(brightnessLabel);
		
		JButton btnManipulation = new JButton("manipulation");
		buttonGroup.add(btnManipulation);
		btnManipulation.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		btnManipulation.setBounds(18, 423, 124, 39);
		selectPanel.add(btnManipulation);
		btnManipulation.addActionListener(this);
		
		JButton resetButton = new JButton("Reset");
		buttonGroup.add(resetButton);
		resetButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		resetButton.setBounds(18, 515, 124, 39);
		selectPanel.add(resetButton);
		resetButton.addActionListener(this);
		
		JButton EdgedetectButton = new JButton("Edge detect");
		buttonGroup.add(EdgedetectButton);
		EdgedetectButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		EdgedetectButton.setBounds(18, 329, 124, 39);
		selectPanel.add(EdgedetectButton);
		EdgedetectButton.addActionListener(this);
		
		JRadioButton SepiaButton = new JRadioButton("Sepia");
		buttonGroup.add(SepiaButton);
		SepiaButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		SepiaButton.setBounds(18, 121, 124, 23);
		selectPanel.add(SepiaButton);
		SepiaButton.addActionListener(this);
		
		JRadioButton blueButton = new JRadioButton("Blue filter");
		buttonGroup.add(blueButton);
		blueButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		blueButton.setBounds(18, 97, 124, 23);
		selectPanel.add(blueButton);
		blueButton.addActionListener(this);
		
		JRadioButton PrettyFilter = new JRadioButton("Pretty");
		buttonGroup.add(PrettyFilter);
		PrettyFilter.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		PrettyFilter.setBounds(18, 145, 124, 23);
		selectPanel.add(PrettyFilter);
		PrettyFilter.addActionListener(this);

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 800, 20);
		contentPane.add(menuBar);

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem uploadItem = new JMenuItem("Upload");
		uploadItem.addActionListener(this);
		fileMenu.add(uploadItem);
		
		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.addActionListener(this);
		fileMenu.add(saveItem);


		setVisible(true);



	}
}
