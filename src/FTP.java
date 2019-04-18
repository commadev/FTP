import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import org.w3c.dom.stylesheets.StyleSheet;



public class FTP extends JFrame implements DropTargetListener {
	public static final int DEFAULT_BUFFER_SIZE = 10000;

	
    Pattern ipAddr = Pattern.compile("^"
            + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}" // Domain name
            + "|"
            + "localhost" // localhost
            + "|"
            + "([01]?\\d?\\d|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d?\\d|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d?\\d|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d?\\d|2[0-4]\\d|25[0-5]))"
            // Ip
            + ":"
            + "[0-9]{1,5}$"); // Port
	
	
    ServerSocket server = null;
    String ExternalIP;
    String filename;

    JTextField tf;
    JLabel FileLinkNAddr;
    JLabel State;
    JLabel dragDrop;
    JLabel dragOver;
    JLabel downloadByte;
    
    DropTarget dt;
    ImageIcon dropPic;
    ImageIcon overPic;
    ImageIcon shareIcon;
    JButton btn;
    JProgressBar pb;

    public static String jFileChooserUtil() {
        String folderPath = "";
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory()); // ���丮 ����
        chooser.setCurrentDirectory(new File("/home"));
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setDialogTitle("File Select");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        int returnVal = chooser.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            folderPath = chooser.getSelectedFile().toString();
        } else if (returnVal == JFileChooser.CANCEL_OPTION) {
            System.out.println("cancel");
            folderPath = "";
        }
        return folderPath;
    }

    public FTP() {
        /*
         * ���̾ƿ� ¥�� �κ�
         */
        setTitle("FTP");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setSize(500, 440);
        setResizable(false);
        setIconImage(new ImageIcon("images/icon.png").getImage());
        getContentPane().setBackground(new java.awt.Color(255, 255, 255));
        
        dropPic = new ImageIcon("images/drag_file_stop.jpg");
        overPic = new ImageIcon("images/drag_file.gif");
        shareIcon = new ImageIcon("images/file_share.gif");

        
        tf = new JTextField();
        tf.setLocation(10, 10);
        tf.setSize(370, 30);
        tf.setVisible(false);
        add(tf);
		
        
        FileLinkNAddr = new JLabel("");
        FileLinkNAddr.setSize(300, 30);
        FileLinkNAddr.setForeground(Color.WHITE);
        FileLinkNAddr.setVisible(false);
        add(FileLinkNAddr);
        
        
        downloadByte = new JLabel("");
        downloadByte.setSize(300, 30);
        downloadByte.setLocation(50,210);
        downloadByte.setVisible(false);
        add(downloadByte);

        
        btn = new JButton("SELECT");
        btn.setLocation(390, 10);
        btn.setSize(90, 30);
        btn.setVisible(false);
        //btn.addActionListener(new BtnEvent());
        add(btn);
        
        
        
        State = new JLabel(shareIcon);
        State.setLocation(0,0);
        State.setSize(500,500);
        State.setVisible(false);
        State.addMouseListener(new MouseListener__());
        add(State);

        dragDrop = new JLabel(dropPic);
        dragDrop.setLocation(0,0);
        dragDrop.setSize(500,400);
        dt = new DropTarget(dragDrop, DnDConstants.ACTION_COPY_OR_MOVE, this, true, null);
        dragDrop.addMouseListener(new MouseListener_());
        dragDrop.setVisible(true);
        add(dragDrop);

        


        pb = new JProgressBar(0, 100);
        pb.setLocation(50, 180);
        pb.setSize(380,30);
        pb.setVisible(false);
        add(pb);

        setVisible(true);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    	dragDrop.setIcon(overPic);
    }
    @Override
    public void dragOver(DropTargetDragEvent dtde) {}
    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {}
    @Override
    public void dragExit(DropTargetEvent dte) {
    	dragDrop.setIcon(dropPic);
    }
    @Override
    public void drop(DropTargetDropEvent dtde)
    {
        if ((dtde.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
            dtde.acceptDrop(dtde.getDropAction());
            Transferable tr = dtde.getTransferable();
            try {
                //���ϸ� ������
                java.util.List list = (java.util.List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                //���ϸ� ���
                for(int i=0;i < list.size();i++) {
                    tf.setText(list.get(i) + "");
                    filename = tf.getText();
                }
                Serving();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    class MouseListener__ implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
	    	dragDrop.setIcon(dropPic);
	    	dragDrop.setVisible(true);
			State.setVisible(false);
			FileLinkNAddr.setText("");
		}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
    	
    }
    
    class MouseListener_ implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {

			String name = JOptionPane.showInputDialog("Input IP & Port");
			System.out.println(name);
			
			String str[] = name.split(":");
			
            if(!ipAddr.matcher(name).matches()){
                JOptionPane.showMessageDialog(null, name + "�� ��ȿ�� �ּҰ� �ƴմϴ�.", "Error!", JOptionPane.ERROR_MESSAGE);
                return;
            }
			
            
            dragDrop.setVisible(false);
            
            

            try {
            	Socket socket = new Socket(/*str[0]*/"localhost", Integer.parseInt(str[1]));
                if (!socket.isConnected()) {
                    System.out.println("Socket Connect Error.");
                    System.exit(0);
                }

                // ����Ǵ� �����̸�
                // todo : �������� ���� �̸� �޾Ƽ� �� �̸����� ������ ��
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                String fileInfo[] = dis.readUTF().split("::");
                FileOutputStream fos = new FileOutputStream(fileInfo[0]);
                InputStream is = socket.getInputStream();
                
                FileLinkNAddr.setVisible(true);
                FileLinkNAddr.setText(fileInfo[0]);
                FileLinkNAddr.setLocation(50,145);
                FileLinkNAddr.setForeground(Color.BLACK);
                pb.setVisible(true);
                downloadByte.setVisible(true);

                new Thread(() -> {
                    try {
                        double startTime = System.currentTimeMillis();
                        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                        int readBytes;
                        int persentage;
                        long totalReadBytes = 0;
                        while ((readBytes = is.read(buffer)) != -1) {
                            // �����κ��� ���� �޾ƿ�
                            fos.write(buffer, 0, readBytes);

                            // ���� �󸶳� �޾Ҵ��� Ȯ��
                            // todo : ���κ��� ����ũ�� ������ �޾ƿͼ� ���α׷����� ����� ��
                            totalReadBytes += readBytes;
                            State.setText(""+totalReadBytes);
                            persentage = (int) (totalReadBytes * 100 / Integer.parseInt(fileInfo[1]));
                            downloadByte.setText(""+totalReadBytes+"/"+fileInfo[1]+" ("+persentage+"%)");
                            pb.setValue(persentage);
                        }
                        double endTime = System.currentTimeMillis();
                        double diffTime = (endTime - startTime) / 1000;

                        JOptionPane.showMessageDialog(null, "Successful download. time: " + diffTime + " second(s)");

                        is.close();
                        fos.close();
                        socket.close();
                        
                        FileLinkNAddr.setVisible(false);
                        pb.setVisible(false);
                        downloadByte.setVisible(false);
                        dragDrop.setVisible(true);
                        tf.setText("");
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			
		}
    	
    }

    
    /*
    class BtnEvent implements ActionListener {
        public void actionPerformed(ActionEvent arg0) {
 			//�ؽ�Ʈ �ʵ尡 �����̸� ������ ���� ��

            if (tf.getText().length() == 0) {               // ���̾�α� ��� ���� ��ũ ������
                filename = jFileChooserUtil();
                if(filename=="") {
                return;
                }
                else {
                tf.setText(filename);
                Serving();
                }
                
            } else {
                 //�ؽ�Ʈ �ʵ尡 ������� ������ Ŭ���̾�Ʈ�� ���� ��
                // ':'���� �յ� �߶� IP(str[0]),Port(str[1])�� ����
                
            }
        }
    }
    
    */
    
    
    
    
    public void Serving() {
    try {
                    URL url_name = new URL("http://bot.whatismyipaddress.com");
                    BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
                    ExternalIP = sc.readLine().trim();
                } catch (IOException e) {
                    ExternalIP = "Network Error";
                }
                try {
                    // �̿� ������ �ƹ� ��Ʈ�� ���� ����
                    server = new ServerSocket(0);
                    FileLinkNAddr.setVisible(true);
                    FileLinkNAddr.setText("<html><p style=\"text-align:center;\">���濡�� �� �ּҸ� �ҷ��ּ���!<br/>" + ExternalIP + ":" + server.getLocalPort()+"</p></html>");
                    FileLinkNAddr.setLocation(160,this.getHeight()/2-15);
                    dragDrop.setVisible(false);
                    State.setVisible(true);

                    new Thread(() -> {
                        try {
                        	while(true) {
                        	Socket socket = server.accept();
                            File file = new File(filename);
                            FileInputStream fis = new FileInputStream(filename);
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                            dos.writeUTF(file.getName() + "::" + file.length());
                            	// �����̸��̶� ���� ũ�� Ŭ���̾�Ʈ�� ������
                            // todo : ����� ���� �������� �ʾƼ� �����ؾ� ��
                            dos.flush();

                            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                            int readBytes;
                            long totalReadBytes = 0;
                            long fileSize = file.length();

                            	// �󸶳� �ɷȴ��� Ȯ���ϱ� ���� ����
                            double startTime = 0;
                            startTime = System.currentTimeMillis();
                            OutputStream os = socket.getOutputStream();
                            while ((readBytes = fis.read(buffer)) > 0) {
                                // Ŭ���̾�Ʈ���� ���۸�ŭ ����
                                os.write(buffer, 0, readBytes);
                            	}

                            os.close();
                            fis.close();
                            dos.close();
                            socket.close();
                        	}
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                server.close();
                            } catch(IOException ignored) {}
                        }
                    }).start();
                } catch (IOException e) {
                    State.setText("Not Serving.");
                }
    }

	public static void main(String[] args) {
		new FTP();
	}
}