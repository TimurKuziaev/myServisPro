
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Cursor;
import java.awt.event.KeyEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;


/*
 * @author Timur
 */
public class NewJFrameMainMyservisProg extends javax.swing.JFrame {

    private Statement stmt;
    private ResultSet rs;
    private Connection conn;
    private String progDir;
    private long starTime1, starTime2, deltaTime;

    private int[] masArtikul, masSort, masUsluga, masPNom;
    
    private String sIspolnitel, sPostavchik;
    private String[] masPas;      // Массив с паролями пользователей
    private String[] masNameIsp;  // Массив с исполнительями
    private String[] masNameKKT;  // Массив с именем кассира для ККТ
    private String[] masTowarKKT; // Массив json с позициями товаров для ККТ
    private String aktPolName, aktPolLogin;
    private int kolTowZak;

    private String sTire70, sTire99, sTire180, sVersionProg, sVersionServer;

    private String textJSON, sessionKey;

    private String sDumpDirLocal, sDumpDirHost, sDumpFileExe, sWixod, sDirNewVersion;
    private String sSession;   // Признак сессии: open - сессия открыта close - сессия закрыта none - сессия неопределена в результате операции закрытия сессии
    private String sSmena;     // Признак смены: Открыта - смена открыта Закрыта - смена закрыта

    public static String userNameBD, passwordBD, urlHostBD;
    public static String httpKKM, sKKMmercComPort, sKKMmercModel, sKKMkassirINN, aktPolKKT;    
    public static int IdZak;

    public NewJFrameMainMyservisProg() {
        initComponents();

        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  //  Это было до вставки подтверждения закрытия программы
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);    
        
        addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowClosing(WindowEvent we) {
                
                if ("yes".equals(sWixod)) {
                String ObjButtons[] = {"Yes", "No"};
                int PromptResult = JOptionPane.showOptionDialog(null,
                        "Закрыть программу?", "Подтверждение закрытия программы",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                        ObjButtons, ObjButtons[1]);
                if (PromptResult == 0) {

                String sTime,sDate,sTextFileBat1,sFileLog,sFileLogHost,sNameFileBat1;
                String sTextFileBat2,sNameFileBat2;
                
                String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
                sTime = sDateTimeTek.substring(11, 13)+"_"+sDateTimeTek.substring(14, 16)+"_";
                sDate = sDateTimeTek.substring(0, 10)+"_";
                                    
                sNameFileBat1 = "temp\\copylogkkt.bat";
                sNameFileBat2 = "temp\\copydump.bat";
                
                sFileLog=progDir+"temp\\log_kkt_merkuriy_tot.txt";
                sFileLogHost=sDumpDirHost+sDate+sTime+"log_kkt_merkuriy_tot.txt";
                                
                sTextFileBat1="copy "+sFileLog+" "+sFileLogHost+"\n";  // копируем на внешний диск файл лога ККТ 
                // Организуется копирование на внешний диск предыдущего дампа, т.к. новый дамп создается некоторое время, поэтому копируем предыдущий дамп 
                sTextFileBat1+="copy "+sDumpDirLocal+"dumpdate.sql "+sDumpDirLocal+sDate+"dumpdate_pred.sql"+"\n";              // копируем файл дампа если есть в предыдущий файл дампа
                sTextFileBat1+="copy "+sDumpDirLocal+sDate+"dumpdate_pred.sql "+sDumpDirHost+sDate+sTime+"dumpdat.sql"+"\n";    // копируем файл предыдущего дампа БД на внешний диск
                                                
                sTextFileBat2="cd "+sDumpFileExe+"\nmysqldump -uroot -padmin myservis -h localhost > "+sDumpDirLocal+"dumpdate.sql"+"\n";   // создаем дамп
                                
                // Создаем bat1 файл
                try {
                    Path fileName = Paths.get(sNameFileBat1);
                    Files.write(fileName, sTextFileBat1.getBytes(), StandardOpenOption.CREATE);
                } catch (IOException e) {e.printStackTrace();}
                
                // Запускаем bat1 файл
                try {Process child = Runtime.getRuntime().exec(sNameFileBat1);}
                catch (IOException ex) {Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);}
                
                // Задержка 1 сек, между копированием файлов и созданием дампа
                // почему-то зависает программа - поэтому убрал
                //try {TimeUnit.SECONDS.sleep(1000);}
                //catch (InterruptedException ex) {Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);}                
                                
                // Создаем bat2 файл
                try {
                    Path fileName = Paths.get(sNameFileBat2);
                    Files.write(fileName, sTextFileBat2.getBytes(), StandardOpenOption.CREATE);
                } catch (IOException e) {e.printStackTrace();}
                                                  
                // Запускаем bat2 файл
                try {Process child = Runtime.getRuntime().exec(sNameFileBat2);}
                catch (IOException ex) {Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);}
                  
                System.exit(0);
                }
            }else{System.exit(0);}
        }
        });
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
        
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ppZakazItem = new javax.swing.JPopupMenu();
        imOpenZakaz = new javax.swing.JMenuItem();
        ppPrihodItem = new javax.swing.JPopupMenu();
        imOpenPrihod = new javax.swing.JMenuItem();
        tabPanMain = new javax.swing.JTabbedPane();
        panPolzovatel = new javax.swing.JPanel();
        panPolzovatelTop = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        cbLogin = new javax.swing.JComboBox<>();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        bbLogin = new javax.swing.JButton();
        tfpPas = new javax.swing.JPasswordField();
        lbPolInfo = new javax.swing.JLabel();
        tfIsp = new javax.swing.JTextField();
        lbNameKKT = new javax.swing.JLabel();
        lbIsp = new javax.swing.JLabel();
        lbMainDateTime = new javax.swing.JLabel();
        lbKKTsmenaInfo = new javax.swing.JLabel();
        lbKKTsmena = new javax.swing.JLabel();
        bbVerProg = new javax.swing.JButton();
        jLabel29 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        panPolTopInfo = new javax.swing.JPanel();
        bbKKTopenSmenaTot = new javax.swing.JButton();
        bbKKTcloseSmenaTot = new javax.swing.JButton();
        bbKKTprovStatus = new javax.swing.JButton();
        bbKKTprintText = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        taInfoMain = new javax.swing.JTextArea();
        panKlient = new javax.swing.JPanel();
        panKlientLeft = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tfKlientTel = new javax.swing.JTextField();
        tfKlientOtch = new javax.swing.JTextField();
        tfKlientName = new javax.swing.JTextField();
        tfKlientEmail = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        tfKlientFam = new javax.swing.JTextField();
        bbKlientZakaz = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        tfKlientGorod = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        tfKlientIndex = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        tfKlientAdres = new javax.swing.JTextField();
        tfKlientSCod = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        tfKlientLCod = new javax.swing.JTextField();
        bbUchetUslug = new javax.swing.JButton();
        panKlientCenter = new javax.swing.JPanel();
        ScrollPanKlientTab = new javax.swing.JScrollPane();
        tabFIO = new javax.swing.JTable();
        panKlientTop = new javax.swing.JPanel();
        tfKlientFind = new javax.swing.JTextField();
        bbFindLCod = new javax.swing.JButton();
        bbFindFIO = new javax.swing.JButton();
        jtFindFIO = new javax.swing.JTextField();
        chKlientTot = new javax.swing.JCheckBox();
        panKlientBottom = new javax.swing.JPanel();
        bbKlientRedakt = new javax.swing.JButton();
        bbKlientSave = new javax.swing.JButton();
        lbDateRog = new javax.swing.JLabel();
        pbKlientPrim = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        tfKlientGod = new javax.swing.JTextField();
        tfKlientMon = new javax.swing.JTextField();
        tfKlientDay = new javax.swing.JTextField();
        bbKlientCSVtot = new javax.swing.JButton();
        bbKlientExcelTot = new javax.swing.JButton();
        bbKlientCSV = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        taKlientPrim = new javax.swing.JTextArea();
        panNewKlient = new javax.swing.JPanel();
        panNewKlientForma = new javax.swing.JPanel();
        panNewLeft = new javax.swing.JPanel();
        tfOthNew = new javax.swing.JTextField();
        tfNamNew = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        tfFamNew = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        tfSCodNew = new javax.swing.JTextField();
        tfLCodNew = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        taPrimNew = new javax.swing.JTextArea();
        jLabel8 = new javax.swing.JLabel();
        tfTelNew = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        tfEmailNew = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        tfAdresNew = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        tfGorodNew = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        tfIndexNew = new javax.swing.JTextField();
        bbNewKlientReg = new javax.swing.JButton();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        tfGod = new javax.swing.JTextField();
        tfMon = new javax.swing.JTextField();
        tfDay = new javax.swing.JTextField();
        bbNewKlient = new javax.swing.JButton();
        panFormZakaz = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        bbZagruz = new javax.swing.JButton();
        bbObsl = new javax.swing.JButton();
        tfWibZakSumma = new javax.swing.JTextField();
        bbRaschet = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        tfKlientPriseLCod = new javax.swing.JTextField();
        tfKlientPriseFIO = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        cbSkidka = new javax.swing.JComboBox<>();
        tfKlientPriseSCod = new javax.swing.JTextField();
        cbZakazGroup = new javax.swing.JComboBox<>();
        jLabel33 = new javax.swing.JLabel();
        chZakazWib = new javax.swing.JCheckBox();
        jScrollPane8 = new javax.swing.JScrollPane();
        tabTowar = new javax.swing.JTable();
        panZakaz = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jfEmail = new javax.swing.JTextField();
        chKKT = new javax.swing.JCheckBox();
        chOFD = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        chEmail = new javax.swing.JCheckBox();
        tfSummaZak = new javax.swing.JTextField();
        lbZakazPrSk = new javax.swing.JLabel();
        tfKlientZakazSkidPr = new javax.swing.JTextField();
        chNal = new javax.swing.JCheckBox();
        chQR = new javax.swing.JCheckBox();
        lbZakazPrSk1 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        bbZakaz = new javax.swing.JButton();
        tfKlientZakazID_zak = new javax.swing.JTextField();
        tfKlientZakazSC = new javax.swing.JTextField();
        tfKlientZakazLCod = new javax.swing.JTextField();
        tfKlientZakazSCod = new javax.swing.JTextField();
        tfKlientZakazFIO = new javax.swing.JTextField();
        jScrollPane9 = new javax.swing.JScrollPane();
        tabZakaz = new javax.swing.JTable();
        bbZakazZak = new javax.swing.JButton();
        pznPrise = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        bbPriseZagruz = new javax.swing.JButton();
        bbNewTowar = new javax.swing.JButton();
        bbPriseSave = new javax.swing.JButton();
        tfTowarNewCena = new javax.swing.JTextField();
        tfTowarNewSort = new javax.swing.JTextField();
        lbInfoNewTowar = new javax.swing.JLabel();
        bbPrisePrihod = new javax.swing.JButton();
        chPriseRedakt = new javax.swing.JCheckBox();
        cbPriseGroup = new javax.swing.JComboBox<>();
        tfTowarNewArtikul = new javax.swing.JTextField();
        tfTowarNewName = new javax.swing.JTextField();
        tfPrihodID_prih = new javax.swing.JTextField();
        chUsluga = new javax.swing.JCheckBox();
        tfPrihodPrim = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        tabPrise = new javax.swing.JTable();
        panBD = new javax.swing.JPanel();
        tpBD = new javax.swing.JTabbedPane();
        panBDzakaz = new javax.swing.JPanel();
        panBDzakazTop = new javax.swing.JPanel();
        bbBDzakaz = new javax.swing.JButton();
        bbBDzakazCSV = new javax.swing.JButton();
        bbBDzakazXLS = new javax.swing.JButton();
        panTabZakaz = new javax.swing.JPanel();
        jScrollPane11 = new javax.swing.JScrollPane();
        tabBDzakaz = new javax.swing.JTable();
        panBDzakazBottom = new javax.swing.JPanel();
        tfBDZakazSumma = new javax.swing.JTextField();
        lbBDzakazSumma = new javax.swing.JLabel();
        tfBDZakazNal = new javax.swing.JTextField();
        lbBDzakazNal = new javax.swing.JLabel();
        lbBDzakazQR = new javax.swing.JLabel();
        tfBDZakazQR = new javax.swing.JTextField();
        panBDprihod = new javax.swing.JPanel();
        panBDprihodBottom = new javax.swing.JPanel();
        tfBDprihodSumma = new javax.swing.JTextField();
        lbBDzakazSumma4 = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        tabBDprihod = new javax.swing.JTable();
        panBDprihodTop = new javax.swing.JPanel();
        bbBDprihod = new javax.swing.JButton();
        bbBDprihodCSV = new javax.swing.JButton();
        panBDtop = new javax.swing.JPanel();
        lbBDzakazDate = new javax.swing.JLabel();
        tfBDZakazDat = new javax.swing.JTextField();
        lbBDzakazMon = new javax.swing.JLabel();
        cbBDzakazMon = new javax.swing.JComboBox<>();
        lbBDzakazGod = new javax.swing.JLabel();
        tfBDZakazGod = new javax.swing.JTextField();
        tfBDZakazLcod = new javax.swing.JTextField();
        chBDzakazLCod = new javax.swing.JCheckBox();
        lbBDzakazGod1 = new javax.swing.JLabel();
        panKassa = new javax.swing.JPanel();
        panKasTop = new javax.swing.JPanel();
        bbKassaOtchet = new javax.swing.JButton();
        bbKassaRashod = new javax.swing.JButton();
        lbBDzakazSumma5 = new javax.swing.JLabel();
        tfKassaDat = new javax.swing.JTextField();
        lbBDzakazSumma6 = new javax.swing.JLabel();
        cbKassaMon = new javax.swing.JComboBox<>();
        lbBDzakazSumma7 = new javax.swing.JLabel();
        tfKassaGod = new javax.swing.JTextField();
        bbKassaPrihod = new javax.swing.JButton();
        bbKassaCSV = new javax.swing.JButton();
        chKassaOtchetMon = new javax.swing.JCheckBox();
        bbKassaExcel = new javax.swing.JButton();
        panKasPrihod = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tabKasPrihod = new javax.swing.JTable();
        panKasBottom = new javax.swing.JPanel();
        tfKasPrihodQR = new javax.swing.JTextField();
        tfKasRashod = new javax.swing.JTextField();
        tfKassaTek = new javax.swing.JTextField();
        tfKasPrihodNal = new javax.swing.JTextField();
        tfKassaOstatok = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        panKasRashod = new javax.swing.JPanel();
        jScrollPane14 = new javax.swing.JScrollPane();
        tabKasRashod = new javax.swing.JTable();
        panPosechenie = new javax.swing.JPanel();
        panUslugiTop = new javax.swing.JPanel();
        tfUslugaLCod = new javax.swing.JTextField();
        tfUslugaFIO = new javax.swing.JTextField();
        tfUslugaID = new javax.swing.JTextField();
        tfUslugaArtikul = new javax.swing.JTextField();
        tfUslugaName = new javax.swing.JTextField();
        panUslugiTop2 = new javax.swing.JPanel();
        bbUslugaZak = new javax.swing.JButton();
        bbUslugaOtchet = new javax.swing.JButton();
        lbOstatok = new javax.swing.JLabel();
        tfUslugaOstatok = new javax.swing.JTextField();
        tfUslugaIspTot = new javax.swing.JTextField();
        lbOstatok1 = new javax.swing.JLabel();
        bbUslugaCSV = new javax.swing.JButton();
        bbSaveXLS = new javax.swing.JButton();
        panUslugi = new javax.swing.JPanel();
        jScrollPane12 = new javax.swing.JScrollPane();
        tabUslugi = new javax.swing.JTable();
        panKKT = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        bbKKTdraiverTest = new javax.swing.JButton();
        tfKKTComPort = new javax.swing.JTextField();
        lbComPort = new javax.swing.JLabel();
        lbComPort1 = new javax.swing.JLabel();
        tfKKTmodel = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        bbKKTsessionOpen = new javax.swing.JButton();
        bbKKTsessionClose = new javax.swing.JButton();
        tfKKTotvet = new javax.swing.JTextField();
        bbKKTdobriyTest = new javax.swing.JButton();
        tfKeySession = new javax.swing.JTextField();
        tfRezultKKT = new javax.swing.JTextField();
        tfKKTzapros = new javax.swing.JTextField();
        tfKKTtext = new javax.swing.JTextField();
        bbKKTpostTest = new javax.swing.JButton();
        bbKKTzapusk = new javax.swing.JButton();
        tfDumpFile = new javax.swing.JTextField();
        bbKKTpythonTest = new javax.swing.JButton();
        bbKKTpreobrazovanieTest = new javax.swing.JButton();
        bbKKTgetStatus = new javax.swing.JButton();
        bbKKTdobriyDen = new javax.swing.JButton();
        bbKKTsmenaOpen = new javax.swing.JButton();
        bbKKTsmenaClose = new javax.swing.JButton();
        bbKKTopenCheck = new javax.swing.JButton();
        bbKKTaddGoods = new javax.swing.JButton();
        bbKKTcloseCheck = new javax.swing.JButton();
        bbKKTtowar = new javax.swing.JButton();
        tabPanKKT = new javax.swing.JTabbedPane();
        tpanKKTlog = new javax.swing.JPanel();
        jScrollPane17 = new javax.swing.JScrollPane();
        taKKTlog = new javax.swing.JTextArea();
        tpanKKTtowar = new javax.swing.JPanel();
        jScrollPane15 = new javax.swing.JScrollPane();
        taKKTtowar = new javax.swing.JTextArea();
        panLog = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        taLog = new javax.swing.JTextArea();
        panLogDop = new javax.swing.JPanel();
        bbNewFrame = new javax.swing.JButton();

        imOpenZakaz.setText("Открыть заказ");
        imOpenZakaz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imOpenZakazActionPerformed(evt);
            }
        });
        ppZakazItem.add(imOpenZakaz);

        imOpenPrihod.setText("Открыть приход");
        imOpenPrihod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imOpenPrihodActionPerformed(evt);
            }
        });
        ppPrihodItem.add(imOpenPrihod);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setIconImage(Toolkit.getDefaultToolkit().getImage(NewJFrameMainMyservisProg.class.getResource("48.png")));
        addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                formComponentAdded(evt);
            }
        });

        tabPanMain.setForeground(new java.awt.Color(0, 0, 153));
        tabPanMain.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPanMain.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tabPanMain.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tabPanMain.setMaximumSize(new java.awt.Dimension(1600, 900));
        tabPanMain.setMinimumSize(new java.awt.Dimension(900, 600));
        tabPanMain.setPreferredSize(new java.awt.Dimension(1200, 900));
        tabPanMain.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                tabPanMainAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
                tabPanMainAncestorRemoved(evt);
            }
        });

        panPolzovatel.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                panPolzovatelAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        cbLogin.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        cbLogin.setForeground(new java.awt.Color(102, 0, 102));
        cbLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbLoginActionPerformed(evt);
            }
        });

        jLabel27.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(0, 0, 153));
        jLabel27.setText("Логин");

        jLabel28.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(0, 0, 153));
        jLabel28.setText("Пароль:");

        bbLogin.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        bbLogin.setForeground(new java.awt.Color(0, 0, 153));
        bbLogin.setText("Ок");
        bbLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbLoginActionPerformed(evt);
            }
        });

        tfpPas.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        tfpPas.setForeground(new java.awt.Color(0, 0, 153));
        tfpPas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfpPasActionPerformed(evt);
            }
        });

        lbPolInfo.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        lbPolInfo.setForeground(new java.awt.Color(0, 0, 153));
        lbPolInfo.setText("Пользователь:");

        tfIsp.setEditable(false);
        tfIsp.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfIsp.setForeground(new java.awt.Color(0, 0, 153));
        tfIsp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfIspActionPerformed(evt);
            }
        });

        lbNameKKT.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbNameKKT.setForeground(new java.awt.Color(102, 0, 102));
        lbNameKKT.setText("Кассир ККТ: ");

        lbIsp.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbIsp.setForeground(new java.awt.Color(0, 102, 0));
        lbIsp.setText("Исполнитель док: ");

        lbMainDateTime.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lbMainDateTime.setForeground(new java.awt.Color(0, 102, 51));
        lbMainDateTime.setText("Текущая дата");

        lbKKTsmenaInfo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lbKKTsmenaInfo.setForeground(new java.awt.Color(102, 0, 102));
        lbKKTsmenaInfo.setText("Смена:");

        lbKKTsmena.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lbKKTsmena.setForeground(new java.awt.Color(102, 102, 0));
        lbKKTsmena.setText("--------");

        bbVerProg.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        bbVerProg.setForeground(new java.awt.Color(0, 102, 102));
        bbVerProg.setText("2024.01.18:1");
        bbVerProg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbVerProgActionPerformed(evt);
            }
        });

        jLabel29.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(0, 102, 102));
        jLabel29.setText("Версия:");

        javax.swing.GroupLayout panPolzovatelTopLayout = new javax.swing.GroupLayout(panPolzovatelTop);
        panPolzovatelTop.setLayout(panPolzovatelTopLayout);
        panPolzovatelTopLayout.setHorizontalGroup(
            panPolzovatelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panPolzovatelTopLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panPolzovatelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panPolzovatelTopLayout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addGap(12, 12, 12)
                        .addComponent(cbLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfpPas, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lbMainDateTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panPolzovatelTopLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bbVerProg)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panPolzovatelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panPolzovatelTopLayout.createSequentialGroup()
                        .addComponent(bbLogin)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lbPolInfo))
                    .addGroup(panPolzovatelTopLayout.createSequentialGroup()
                        .addComponent(lbKKTsmenaInfo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lbKKTsmena)))
                .addGroup(panPolzovatelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panPolzovatelTopLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(lbNameKKT, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panPolzovatelTopLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(lbIsp, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panPolzovatelTopLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tfIsp)))
                .addContainerGap(261, Short.MAX_VALUE))
        );
        panPolzovatelTopLayout.setVerticalGroup(
            panPolzovatelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panPolzovatelTopLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panPolzovatelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panPolzovatelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel27)
                        .addComponent(cbLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel28)
                        .addComponent(bbLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tfpPas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tfIsp, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lbPolInfo))
                    .addComponent(jLabel11))
                .addGap(18, 18, 18)
                .addGroup(panPolzovatelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panPolzovatelTopLayout.createSequentialGroup()
                        .addComponent(lbNameKKT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panPolzovatelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lbIsp)
                            .addComponent(lbKKTsmenaInfo)
                            .addComponent(lbKKTsmena)))
                    .addGroup(panPolzovatelTopLayout.createSequentialGroup()
                        .addComponent(lbMainDateTime, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panPolzovatelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel29)
                            .addComponent(bbVerProg, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jLabel32.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logo400.png"))); // NOI18N

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 508, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 411, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bbKKTopenSmenaTot.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKKTopenSmenaTot.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTopenSmenaTot.setText("Открыть смену");
        bbKKTopenSmenaTot.setEnabled(false);
        bbKKTopenSmenaTot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTopenSmenaTotActionPerformed(evt);
            }
        });

        bbKKTcloseSmenaTot.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKKTcloseSmenaTot.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTcloseSmenaTot.setText("Закрыть смену");
        bbKKTcloseSmenaTot.setEnabled(false);
        bbKKTcloseSmenaTot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTcloseSmenaTotActionPerformed(evt);
            }
        });

        bbKKTprovStatus.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKKTprovStatus.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTprovStatus.setText("Проверка статуса");
        bbKKTprovStatus.setEnabled(false);
        bbKKTprovStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTprovStatusActionPerformed(evt);
            }
        });

        bbKKTprintText.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKKTprintText.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTprintText.setText("Добрый день!");
        bbKKTprintText.setEnabled(false);
        bbKKTprintText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTprintTextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panPolTopInfoLayout = new javax.swing.GroupLayout(panPolTopInfo);
        panPolTopInfo.setLayout(panPolTopInfoLayout);
        panPolTopInfoLayout.setHorizontalGroup(
            panPolTopInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panPolTopInfoLayout.createSequentialGroup()
                .addGroup(panPolTopInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panPolTopInfoLayout.createSequentialGroup()
                        .addComponent(bbKKTopenSmenaTot, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bbKKTcloseSmenaTot, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panPolTopInfoLayout.createSequentialGroup()
                        .addComponent(bbKKTprovStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bbKKTprintText, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panPolTopInfoLayout.setVerticalGroup(
            panPolTopInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panPolTopInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panPolTopInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bbKKTopenSmenaTot, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bbKKTcloseSmenaTot, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panPolTopInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bbKKTprovStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bbKKTprintText, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        taInfoMain.setColumns(20);
        taInfoMain.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        taInfoMain.setForeground(new java.awt.Color(0, 0, 153));
        taInfoMain.setRows(5);
        jScrollPane6.setViewportView(taInfoMain);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panPolTopInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 519, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panPolTopInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6)
                .addContainerGap())
        );

        javax.swing.GroupLayout panPolzovatelLayout = new javax.swing.GroupLayout(panPolzovatel);
        panPolzovatel.setLayout(panPolzovatelLayout);
        panPolzovatelLayout.setHorizontalGroup(
            panPolzovatelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panPolzovatelTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panPolzovatelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(238, Short.MAX_VALUE))
        );
        panPolzovatelLayout.setVerticalGroup(
            panPolzovatelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panPolzovatelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panPolzovatelTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panPolzovatelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(130, Short.MAX_VALUE))
        );

        tabPanMain.addTab("Пользователи", panPolzovatel);

        panKlient.setPreferredSize(new java.awt.Dimension(1200, 600));
        panKlient.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                panKlientAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(102, 0, 102));
        jLabel1.setText("Телефон:");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(102, 0, 102));
        jLabel2.setText("E-mail:");

        tfKlientTel.setEditable(false);
        tfKlientTel.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tfKlientTel.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientTel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientTelActionPerformed(evt);
            }
        });
        tfKlientTel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfKlientTelKeyTyped(evt);
            }
        });

        tfKlientOtch.setEditable(false);
        tfKlientOtch.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfKlientOtch.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientOtch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientOtchActionPerformed(evt);
            }
        });

        tfKlientName.setEditable(false);
        tfKlientName.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfKlientName.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientNameActionPerformed(evt);
            }
        });

        tfKlientEmail.setEditable(false);
        tfKlientEmail.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tfKlientEmail.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientEmailActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(102, 0, 102));
        jLabel3.setText("Фамилия");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(102, 0, 102));
        jLabel4.setText("Имя");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(102, 0, 102));
        jLabel5.setText("Отчество");

        tfKlientFam.setEditable(false);
        tfKlientFam.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfKlientFam.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientFam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientFamActionPerformed(evt);
            }
        });

        bbKlientZakaz.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKlientZakaz.setForeground(new java.awt.Color(0, 0, 153));
        bbKlientZakaz.setText("Сформировать заказ");
        bbKlientZakaz.setEnabled(false);
        bbKlientZakaz.setHideActionText(true);
        bbKlientZakaz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKlientZakazActionPerformed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(102, 0, 102));
        jLabel17.setText("Город:");

        tfKlientGorod.setEditable(false);
        tfKlientGorod.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tfKlientGorod.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientGorod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientGorodActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(102, 0, 102));
        jLabel19.setText("Индекс:");

        tfKlientIndex.setEditable(false);
        tfKlientIndex.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tfKlientIndex.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientIndexActionPerformed(evt);
            }
        });

        jLabel22.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(102, 0, 102));
        jLabel22.setText("Адрес (улица, дом, квартира):");

        tfKlientAdres.setEditable(false);
        tfKlientAdres.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tfKlientAdres.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientAdres.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientAdresActionPerformed(evt);
            }
        });

        tfKlientSCod.setEditable(false);
        tfKlientSCod.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfKlientSCod.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientSCod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientSCodActionPerformed(evt);
            }
        });
        tfKlientSCod.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfKlientSCodKeyTyped(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(102, 0, 102));
        jLabel25.setText("№ Учёта");

        jLabel26.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(102, 0, 102));
        jLabel26.setText("Id Клиента");

        tfKlientLCod.setEditable(false);
        tfKlientLCod.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfKlientLCod.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientLCod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientLCodActionPerformed(evt);
            }
        });

        bbUchetUslug.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbUchetUslug.setForeground(new java.awt.Color(0, 0, 153));
        bbUchetUslug.setText("Учёт посещений ");
        bbUchetUslug.setEnabled(false);
        bbUchetUslug.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbUchetUslugActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panKlientLeftLayout = new javax.swing.GroupLayout(panKlientLeft);
        panKlientLeft.setLayout(panKlientLeftLayout);
        panKlientLeftLayout.setHorizontalGroup(
            panKlientLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKlientLeftLayout.createSequentialGroup()
                .addGroup(panKlientLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(tfKlientName, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(tfKlientTel, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfKlientEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfKlientOtch, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfKlientFam, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(tfKlientGorod, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bbKlientZakaz, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(tfKlientIndex, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfKlientAdres, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panKlientLeftLayout.createSequentialGroup()
                        .addGroup(panKlientLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel26)
                            .addComponent(tfKlientLCod, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panKlientLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addComponent(tfKlientSCod, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(bbUchetUslug, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        panKlientLeftLayout.setVerticalGroup(
            panKlientLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKlientLeftLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(panKlientLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(panKlientLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfKlientSCod, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfKlientLCod, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tfKlientFam, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tfKlientName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tfKlientOtch, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tfKlientTel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tfKlientEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tfKlientGorod, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tfKlientIndex, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tfKlientAdres, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bbKlientZakaz, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bbUchetUslug, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabFIO.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tabFIO.setForeground(new java.awt.Color(0, 0, 153));
        tabFIO.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null}
            },
            new String [] {
                "Фамилия", "Имя", "Отчество", "Телефон", "Id Клиента", "Город"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabFIO.setRowHeight(30);
        tabFIO.setShowGrid(true);
        tabFIO.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabFIOMouseClicked(evt);
            }
        });
        ScrollPanKlientTab.setViewportView(tabFIO);
        if (tabFIO.getColumnModel().getColumnCount() > 0) {
            tabFIO.getColumnModel().getColumn(0).setResizable(false);
            tabFIO.getColumnModel().getColumn(0).setPreferredWidth(170);
            tabFIO.getColumnModel().getColumn(1).setResizable(false);
            tabFIO.getColumnModel().getColumn(1).setPreferredWidth(170);
            tabFIO.getColumnModel().getColumn(2).setResizable(false);
            tabFIO.getColumnModel().getColumn(2).setPreferredWidth(170);
            tabFIO.getColumnModel().getColumn(3).setResizable(false);
            tabFIO.getColumnModel().getColumn(3).setPreferredWidth(170);
            tabFIO.getColumnModel().getColumn(4).setResizable(false);
            tabFIO.getColumnModel().getColumn(4).setPreferredWidth(100);
            tabFIO.getColumnModel().getColumn(5).setResizable(false);
        }

        javax.swing.GroupLayout panKlientCenterLayout = new javax.swing.GroupLayout(panKlientCenter);
        panKlientCenter.setLayout(panKlientCenterLayout);
        panKlientCenterLayout.setHorizontalGroup(
            panKlientCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKlientCenterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ScrollPanKlientTab)
                .addGap(7, 7, 7))
        );
        panKlientCenterLayout.setVerticalGroup(
            panKlientCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKlientCenterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ScrollPanKlientTab, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tfKlientFind.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfKlientFind.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientFind.setText("2000");
        tfKlientFind.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                tfKlientFindCaretUpdate(evt);
            }
        });
        tfKlientFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientFindActionPerformed(evt);
            }
        });
        tfKlientFind.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tfKlientFindKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfKlientFindKeyTyped(evt);
            }
        });

        bbFindLCod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbFindLCod.setForeground(new java.awt.Color(0, 0, 153));
        bbFindLCod.setText("Поиск по номеру Клиента");
        bbFindLCod.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bbFindLCod.setEnabled(false);
        bbFindLCod.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        bbFindLCod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbFindLCodActionPerformed(evt);
            }
        });

        bbFindFIO.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbFindFIO.setForeground(new java.awt.Color(0, 0, 153));
        bbFindFIO.setText("Поиск по Фамилии");
        bbFindFIO.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bbFindFIO.setEnabled(false);
        bbFindFIO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbFindFIOActionPerformed(evt);
            }
        });

        jtFindFIO.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jtFindFIO.setForeground(new java.awt.Color(0, 0, 153));
        jtFindFIO.setText("Хисматуллина");
        jtFindFIO.setMinimumSize(new java.awt.Dimension(70, 3130));
        jtFindFIO.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jtFindFIOFocusGained(evt);
            }
        });
        jtFindFIO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtFindFIOActionPerformed(evt);
            }
        });

        chKlientTot.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        chKlientTot.setForeground(new java.awt.Color(0, 0, 153));
        chKlientTot.setText("Все клиенты");
        chKlientTot.setBorderPaintedFlat(true);
        chKlientTot.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        chKlientTot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chKlientTotActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panKlientTopLayout = new javax.swing.GroupLayout(panKlientTop);
        panKlientTop.setLayout(panKlientTopLayout);
        panKlientTopLayout.setHorizontalGroup(
            panKlientTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKlientTopLayout.createSequentialGroup()
                .addComponent(tfKlientFind, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(bbFindLCod)
                .addGap(48, 48, 48)
                .addComponent(jtFindFIO, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(bbFindFIO)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chKlientTot, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panKlientTopLayout.setVerticalGroup(
            panKlientTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKlientTopLayout.createSequentialGroup()
                .addGroup(panKlientTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfKlientFind, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panKlientTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(bbFindLCod, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(bbFindFIO, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(chKlientTot, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jtFindFIO, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        bbKlientRedakt.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKlientRedakt.setForeground(new java.awt.Color(0, 0, 153));
        bbKlientRedakt.setText("Редактировать Клиента");
        bbKlientRedakt.setEnabled(false);
        bbKlientRedakt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKlientRedaktActionPerformed(evt);
            }
        });

        bbKlientSave.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKlientSave.setForeground(new java.awt.Color(0, 0, 153));
        bbKlientSave.setText("Сохранить справочник");
        bbKlientSave.setEnabled(false);
        bbKlientSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKlientSaveActionPerformed(evt);
            }
        });

        lbDateRog.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbDateRog.setForeground(new java.awt.Color(102, 0, 102));
        lbDateRog.setText("Дата рождения:");

        pbKlientPrim.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        pbKlientPrim.setForeground(new java.awt.Color(102, 0, 102));
        pbKlientPrim.setText("Примечание:");

        jLabel38.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(0, 0, 153));
        jLabel38.setText("  День     Месяц          Год");

        tfKlientGod.setEditable(false);
        tfKlientGod.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        tfKlientGod.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientGod.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfKlientGod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientGodActionPerformed(evt);
            }
        });
        tfKlientGod.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfKlientGodKeyTyped(evt);
            }
        });

        tfKlientMon.setEditable(false);
        tfKlientMon.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        tfKlientMon.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientMon.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfKlientMon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientMonActionPerformed(evt);
            }
        });
        tfKlientMon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfKlientMonKeyTyped(evt);
            }
        });

        tfKlientDay.setEditable(false);
        tfKlientDay.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        tfKlientDay.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientDay.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfKlientDay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientDayActionPerformed(evt);
            }
        });
        tfKlientDay.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfKlientDayKeyTyped(evt);
            }
        });

        bbKlientCSVtot.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKlientCSVtot.setForeground(new java.awt.Color(0, 0, 153));
        bbKlientCSVtot.setText("Csv (полный)");
        bbKlientCSVtot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKlientCSVtotActionPerformed(evt);
            }
        });

        bbKlientExcelTot.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKlientExcelTot.setForeground(new java.awt.Color(0, 0, 153));
        bbKlientExcelTot.setText("Excel (полный)");
        bbKlientExcelTot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKlientExcelTotActionPerformed(evt);
            }
        });

        bbKlientCSV.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKlientCSV.setForeground(new java.awt.Color(0, 0, 153));
        bbKlientCSV.setText("Csv (выборка)");
        bbKlientCSV.setEnabled(false);
        bbKlientCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKlientCSVActionPerformed(evt);
            }
        });

        taKlientPrim.setColumns(20);
        taKlientPrim.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        taKlientPrim.setForeground(new java.awt.Color(0, 102, 0));
        taKlientPrim.setRows(5);
        taKlientPrim.setWrapStyleWord(true);
        taKlientPrim.setPreferredSize(new java.awt.Dimension(300, 114));
        jScrollPane3.setViewportView(taKlientPrim);

        javax.swing.GroupLayout panKlientBottomLayout = new javax.swing.GroupLayout(panKlientBottom);
        panKlientBottom.setLayout(panKlientBottomLayout);
        panKlientBottomLayout.setHorizontalGroup(
            panKlientBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKlientBottomLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panKlientBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panKlientBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panKlientBottomLayout.createSequentialGroup()
                            .addComponent(tfKlientDay, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(tfKlientMon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(tfKlientGod, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panKlientBottomLayout.createSequentialGroup()
                            .addComponent(lbDateRog)
                            .addGap(127, 127, 127)))
                    .addGroup(panKlientBottomLayout.createSequentialGroup()
                        .addGroup(panKlientBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(bbKlientRedakt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bbKlientSave, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addComponent(jLabel38, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)))
                .addGroup(panKlientBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 555, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panKlientBottomLayout.createSequentialGroup()
                        .addComponent(pbKlientPrim)
                        .addGap(445, 445, 445)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panKlientBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bbKlientCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bbKlientCSVtot, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bbKlientExcelTot, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        panKlientBottomLayout.setVerticalGroup(
            panKlientBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panKlientBottomLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panKlientBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbDateRog, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pbKlientPrim, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bbKlientExcelTot, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panKlientBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panKlientBottomLayout.createSequentialGroup()
                        .addComponent(bbKlientCSVtot, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bbKlientCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(64, 64, 64))
                    .addGroup(panKlientBottomLayout.createSequentialGroup()
                        .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panKlientBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfKlientGod, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfKlientMon, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfKlientDay, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bbKlientRedakt, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bbKlientSave, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panKlientLayout = new javax.swing.GroupLayout(panKlient);
        panKlient.setLayout(panKlientLayout);
        panKlientLayout.setHorizontalGroup(
            panKlientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKlientLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(panKlientLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panKlientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panKlientCenter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panKlientLayout.createSequentialGroup()
                        .addGroup(panKlientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panKlientTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(panKlientBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panKlientLayout.setVerticalGroup(
            panKlientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKlientLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(panKlientTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panKlientCenter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panKlientBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(panKlientLayout.createSequentialGroup()
                .addComponent(panKlientLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPanMain.addTab("Клиент", panKlient);

        panNewKlient.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                panNewKlientAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        panNewKlientForma.setEnabled(false);

        panNewLeft.setVerifyInputWhenFocusTarget(false);

        tfOthNew.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfOthNew.setForeground(new java.awt.Color(0, 0, 153));
        tfOthNew.setEnabled(false);
        tfOthNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfOthNewActionPerformed(evt);
            }
        });

        tfNamNew.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfNamNew.setForeground(new java.awt.Color(0, 0, 153));
        tfNamNew.setEnabled(false);
        tfNamNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfNamNewActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(102, 0, 102));
        jLabel12.setText("Фамилия");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(102, 0, 102));
        jLabel13.setText("Имя");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(102, 0, 102));
        jLabel14.setText("Отчество");

        tfFamNew.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfFamNew.setForeground(new java.awt.Color(0, 0, 153));
        tfFamNew.setEnabled(false);
        tfFamNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfFamNewActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(102, 0, 102));
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText("Id Клиента");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(102, 0, 102));
        jLabel15.setText("№ Учёта");

        tfSCodNew.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfSCodNew.setForeground(new java.awt.Color(0, 0, 153));
        tfSCodNew.setText("1");
        tfSCodNew.setEnabled(false);
        tfSCodNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfSCodNewActionPerformed(evt);
            }
        });
        tfSCodNew.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfSCodNewKeyTyped(evt);
            }
        });

        tfLCodNew.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfLCodNew.setForeground(new java.awt.Color(0, 0, 153));
        tfLCodNew.setEnabled(false);
        tfLCodNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfLCodNewActionPerformed(evt);
            }
        });
        tfLCodNew.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfLCodNewKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout panNewLeftLayout = new javax.swing.GroupLayout(panNewLeft);
        panNewLeft.setLayout(panNewLeftLayout);
        panNewLeftLayout.setHorizontalGroup(
            panNewLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tfNamNew, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jLabel12)
            .addComponent(tfOthNew, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jLabel13)
            .addComponent(jLabel14)
            .addComponent(tfFamNew, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(panNewLeftLayout.createSequentialGroup()
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(panNewLeftLayout.createSequentialGroup()
                .addComponent(tfLCodNew, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tfSCodNew, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panNewLeftLayout.setVerticalGroup(
            panNewLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panNewLeftLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(panNewLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(panNewLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfLCodNew, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfSCodNew, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tfFamNew, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tfNamNew, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tfOthNew, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(60, Short.MAX_VALUE))
        );

        taPrimNew.setColumns(20);
        taPrimNew.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        taPrimNew.setForeground(new java.awt.Color(0, 102, 0));
        taPrimNew.setRows(5);
        taPrimNew.setText("Это пример заполнения поля примечание для нового клиента Оздоровительного Центра Наиля");
        taPrimNew.setWrapStyleWord(true);
        taPrimNew.setEnabled(false);
        jScrollPane4.setViewportView(taPrimNew);

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(102, 0, 102));
        jLabel8.setText("Телефон:");

        tfTelNew.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tfTelNew.setForeground(new java.awt.Color(0, 0, 153));
        tfTelNew.setEnabled(false);
        tfTelNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfTelNewActionPerformed(evt);
            }
        });
        tfTelNew.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfTelNewKeyTyped(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(102, 0, 102));
        jLabel9.setText("E-mail:");

        tfEmailNew.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tfEmailNew.setForeground(new java.awt.Color(0, 0, 153));
        tfEmailNew.setEnabled(false);
        tfEmailNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfEmailNewActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(102, 0, 102));
        jLabel21.setText("Адрес (улица, дом, квартира):");

        tfAdresNew.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tfAdresNew.setForeground(new java.awt.Color(0, 0, 153));
        tfAdresNew.setEnabled(false);
        tfAdresNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfAdresNewActionPerformed(evt);
            }
        });

        jLabel23.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(102, 0, 102));
        jLabel23.setText("Примечание:");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(102, 0, 102));
        jLabel18.setText("Город:");

        tfGorodNew.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tfGorodNew.setForeground(new java.awt.Color(0, 0, 153));
        tfGorodNew.setEnabled(false);
        tfGorodNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfGorodNewActionPerformed(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(102, 0, 102));
        jLabel20.setText("Индекс:");

        tfIndexNew.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tfIndexNew.setForeground(new java.awt.Color(0, 0, 153));
        tfIndexNew.setEnabled(false);
        tfIndexNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfIndexNewActionPerformed(evt);
            }
        });
        tfIndexNew.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfIndexNewKeyTyped(evt);
            }
        });

        bbNewKlientReg.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbNewKlientReg.setForeground(new java.awt.Color(0, 0, 153));
        bbNewKlientReg.setText("Зарегистрировать нового Клиента");
        bbNewKlientReg.setEnabled(false);
        bbNewKlientReg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbNewKlientRegActionPerformed(evt);
            }
        });

        jLabel35.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(102, 0, 102));
        jLabel35.setText("Дата рождения:");

        jLabel36.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(0, 0, 153));
        jLabel36.setText(" День      Месяц         Год");

        tfGod.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        tfGod.setForeground(new java.awt.Color(0, 0, 153));
        tfGod.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfGod.setEnabled(false);
        tfGod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfGodActionPerformed(evt);
            }
        });
        tfGod.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfGodKeyTyped(evt);
            }
        });

        tfMon.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        tfMon.setForeground(new java.awt.Color(0, 0, 153));
        tfMon.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfMon.setEnabled(false);
        tfMon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfMonActionPerformed(evt);
            }
        });
        tfMon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfMonKeyTyped(evt);
            }
        });

        tfDay.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        tfDay.setForeground(new java.awt.Color(0, 0, 153));
        tfDay.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfDay.setEnabled(false);
        tfDay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfDayActionPerformed(evt);
            }
        });
        tfDay.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfDayKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout panNewKlientFormaLayout = new javax.swing.GroupLayout(panNewKlientForma);
        panNewKlientForma.setLayout(panNewKlientFormaLayout);
        panNewKlientFormaLayout.setHorizontalGroup(
            panNewKlientFormaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panNewKlientFormaLayout.createSequentialGroup()
                .addGroup(panNewKlientFormaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfAdresNew, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panNewKlientFormaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel21)
                        .addGroup(panNewKlientFormaLayout.createSequentialGroup()
                            .addComponent(panNewLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(40, 40, 40)
                            .addGroup(panNewKlientFormaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(tfTelNew)
                                .addComponent(jLabel8)
                                .addComponent(jLabel9)
                                .addComponent(tfEmailNew)
                                .addComponent(jLabel18)
                                .addComponent(tfGorodNew)
                                .addComponent(jLabel20)
                                .addComponent(tfIndexNew)
                                .addComponent(jLabel35)
                                .addComponent(jLabel36, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                                .addGroup(panNewKlientFormaLayout.createSequentialGroup()
                                    .addComponent(tfDay, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(tfMon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(tfGod, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addComponent(jLabel23)
                        .addGroup(panNewKlientFormaLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(bbNewKlientReg))))
                .addGap(20, 20, 20))
        );
        panNewKlientFormaLayout.setVerticalGroup(
            panNewKlientFormaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panNewKlientFormaLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(panNewKlientFormaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panNewLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panNewKlientFormaLayout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(tfTelNew, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(tfEmailNew, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(tfGorodNew, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(tfIndexNew, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panNewKlientFormaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfGod, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfMon, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfDay, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(10, 10, 10)
                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(tfAdresNew, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jLabel23)
                .addGap(5, 5, 5)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bbNewKlientReg, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(58, Short.MAX_VALUE))
        );

        bbNewKlient.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbNewKlient.setForeground(new java.awt.Color(0, 0, 153));
        bbNewKlient.setText("Начать регистрацию нового Клиента");
        bbNewKlient.setEnabled(false);
        bbNewKlient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbNewKlientActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panNewKlientLayout = new javax.swing.GroupLayout(panNewKlient);
        panNewKlient.setLayout(panNewKlientLayout);
        panNewKlientLayout.setHorizontalGroup(
            panNewKlientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panNewKlientLayout.createSequentialGroup()
                .addGroup(panNewKlientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panNewKlientLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(panNewKlientForma, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panNewKlientLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(bbNewKlient)))
                .addContainerGap(697, Short.MAX_VALUE))
        );
        panNewKlientLayout.setVerticalGroup(
            panNewKlientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panNewKlientLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bbNewKlient, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panNewKlientForma, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabPanMain.addTab("Новый Клиент", panNewKlient);

        panFormZakaz.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                panFormZakazAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        bbZagruz.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbZagruz.setForeground(new java.awt.Color(0, 0, 153));
        bbZagruz.setText("Загрузить");
        bbZagruz.setEnabled(false);
        bbZagruz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbZagruzActionPerformed(evt);
            }
        });

        bbObsl.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbObsl.setForeground(new java.awt.Color(0, 0, 153));
        bbObsl.setText("Обслужить");
        bbObsl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbObslActionPerformed(evt);
            }
        });

        tfWibZakSumma.setEditable(false);
        tfWibZakSumma.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        tfWibZakSumma.setForeground(new java.awt.Color(0, 0, 153));
        tfWibZakSumma.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfWibZakSumma.setText("0");
        tfWibZakSumma.setToolTipText("");
        tfWibZakSumma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfWibZakSummaActionPerformed(evt);
            }
        });

        bbRaschet.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbRaschet.setForeground(new java.awt.Color(0, 0, 153));
        bbRaschet.setText("Посчитать");
        bbRaschet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbRaschetActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(102, 0, 102));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Сумма заказа");

        tfKlientPriseLCod.setEditable(false);
        tfKlientPriseLCod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfKlientPriseLCod.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientPriseLCod.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfKlientPriseLCod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientPriseLCodActionPerformed(evt);
            }
        });

        tfKlientPriseFIO.setEditable(false);
        tfKlientPriseFIO.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfKlientPriseFIO.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientPriseFIO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientPriseFIOActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 153));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Скидка-%");

        cbSkidka.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        cbSkidka.setForeground(new java.awt.Color(102, 0, 102));
        cbSkidka.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
        cbSkidka.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSkidkaActionPerformed(evt);
            }
        });

        tfKlientPriseSCod.setEditable(false);
        tfKlientPriseSCod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfKlientPriseSCod.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientPriseSCod.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfKlientPriseSCod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientPriseSCodActionPerformed(evt);
            }
        });

        cbZakazGroup.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        cbZakazGroup.setForeground(new java.awt.Color(0, 0, 153));
        cbZakazGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbZakazGroupActionPerformed(evt);
            }
        });

        jLabel33.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(102, 0, 102));
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel33.setText("Группа:");

        chZakazWib.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        chZakazWib.setForeground(new java.awt.Color(0, 0, 153));
        chZakazWib.setText("Выбрать");
        chZakazWib.setBorderPaintedFlat(true);
        chZakazWib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chZakazWibActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfKlientPriseLCod, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfKlientPriseSCod, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(tfKlientPriseFIO, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(chZakazWib)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel33)
                        .addGap(18, 18, 18)
                        .addComponent(cbZakazGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfWibZakSumma, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cbSkidka, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(bbRaschet, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bbObsl, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bbZagruz, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(155, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbSkidka, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bbZagruz))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tfWibZakSumma, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfKlientPriseFIO, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfKlientPriseLCod, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(tfKlientPriseSCod, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(cbZakazGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(chZakazWib, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bbRaschet, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bbObsl, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        tabTowar.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        tabTowar.setForeground(new java.awt.Color(0, 0, 153));
        tabTowar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "№ пп", "Артикул", "Наименование", "Цена", "Остаток", "Кол", "Кол.Услуг"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Byte.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabTowar.setGridColor(new java.awt.Color(0, 0, 153));
        tabTowar.setRowHeight(34);
        tabTowar.setShowGrid(true);
        tabTowar.addHierarchyListener(new java.awt.event.HierarchyListener() {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
                tabTowarHierarchyChanged(evt);
            }
        });
        tabTowar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                tabTowarMouseDragged(evt);
            }
        });
        tabTowar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabTowarMouseClicked(evt);
            }
        });
        tabTowar.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                tabTowarInputMethodTextChanged(evt);
            }
        });
        tabTowar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tabTowarKeyPressed(evt);
            }
        });
        jScrollPane8.setViewportView(tabTowar);
        tabTowar.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tabTowar.getColumnModel().getColumnCount() > 0) {
            tabTowar.getColumnModel().getColumn(0).setResizable(false);
            tabTowar.getColumnModel().getColumn(0).setPreferredWidth(15);
            tabTowar.getColumnModel().getColumn(1).setResizable(false);
            tabTowar.getColumnModel().getColumn(1).setPreferredWidth(40);
            tabTowar.getColumnModel().getColumn(2).setResizable(false);
            tabTowar.getColumnModel().getColumn(2).setPreferredWidth(400);
            tabTowar.getColumnModel().getColumn(3).setResizable(false);
            tabTowar.getColumnModel().getColumn(3).setPreferredWidth(60);
            tabTowar.getColumnModel().getColumn(4).setResizable(false);
            tabTowar.getColumnModel().getColumn(4).setPreferredWidth(60);
            tabTowar.getColumnModel().getColumn(5).setResizable(false);
            tabTowar.getColumnModel().getColumn(5).setPreferredWidth(60);
            tabTowar.getColumnModel().getColumn(6).setPreferredWidth(20);
        }

        javax.swing.GroupLayout panFormZakazLayout = new javax.swing.GroupLayout(panFormZakaz);
        panFormZakaz.setLayout(panFormZakazLayout);
        panFormZakazLayout.setHorizontalGroup(
            panFormZakazLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panFormZakazLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane8))
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panFormZakazLayout.setVerticalGroup(
            panFormZakazLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panFormZakazLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPanMain.addTab("Формирование заказа", panFormZakaz);

        jfEmail.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jfEmail.setForeground(new java.awt.Color(0, 0, 153));
        jfEmail.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jfEmail.setText("email@mail.ru");
        jfEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jfEmailActionPerformed(evt);
            }
        });

        chKKT.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        chKKT.setForeground(new java.awt.Color(0, 0, 153));
        chKKT.setText("Печатать Чек на ККТ");
        chKKT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chKKTActionPerformed(evt);
            }
        });

        chOFD.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        chOFD.setForeground(new java.awt.Color(0, 0, 153));
        chOFD.setText("Оформить в ОФД");
        chOFD.setBorderPaintedFlat(true);
        chOFD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chOFDActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 0, 153));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Сумма заказа");

        chEmail.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        chEmail.setForeground(new java.awt.Color(0, 0, 153));
        chEmail.setText("Отправить Чек на e-mail");
        chEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chEmailActionPerformed(evt);
            }
        });

        tfSummaZak.setEditable(false);
        tfSummaZak.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        tfSummaZak.setForeground(new java.awt.Color(0, 0, 153));
        tfSummaZak.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfSummaZak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfSummaZakActionPerformed(evt);
            }
        });

        lbZakazPrSk.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lbZakazPrSk.setForeground(new java.awt.Color(153, 0, 0));
        lbZakazPrSk.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbZakazPrSk.setText("Скидка-%");

        tfKlientZakazSkidPr.setEditable(false);
        tfKlientZakazSkidPr.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        tfKlientZakazSkidPr.setForeground(new java.awt.Color(153, 0, 0));
        tfKlientZakazSkidPr.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfKlientZakazSkidPr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientZakazSkidPrActionPerformed(evt);
            }
        });

        chNal.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        chNal.setForeground(new java.awt.Color(0, 0, 153));
        chNal.setText("Наличными");
        chNal.setBorderPaintedFlat(true);
        chNal.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        chNal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chNalActionPerformed(evt);
            }
        });

        chQR.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        chQR.setForeground(new java.awt.Color(0, 0, 153));
        chQR.setText("QR-код");
        chQR.setBorderPaintedFlat(true);
        chQR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chQRActionPerformed(evt);
            }
        });

        lbZakazPrSk1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lbZakazPrSk1.setForeground(new java.awt.Color(153, 0, 0));
        lbZakazPrSk1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbZakazPrSk1.setText("Id Клиента               № Учёта                  ФИО");
        lbZakazPrSk1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tfSummaZak, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbZakazPrSk, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfKlientZakazSkidPr, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(chNal, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(lbZakazPrSk1, javax.swing.GroupLayout.PREFERRED_SIZE, 439, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(chQR, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(17, 17, 17)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chKKT)
                    .addComponent(chOFD, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chOFD, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chNal, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfSummaZak, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbZakazPrSk, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfKlientZakazSkidPr, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(chQR)
                        .addComponent(jfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(chKKT))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(lbZakazPrSk1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        tfSummaZak.getAccessibleContext().setAccessibleName("");
        lbZakazPrSk1.getAccessibleContext().setAccessibleName("");

        bbZakaz.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbZakaz.setForeground(new java.awt.Color(0, 0, 153));
        bbZakaz.setText("Оформить заказ");
        bbZakaz.setEnabled(false);
        bbZakaz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbZakazActionPerformed(evt);
            }
        });

        tfKlientZakazID_zak.setEditable(false);
        tfKlientZakazID_zak.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfKlientZakazID_zak.setForeground(new java.awt.Color(153, 0, 0));
        tfKlientZakazID_zak.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfKlientZakazID_zak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientZakazID_zakActionPerformed(evt);
            }
        });

        tfKlientZakazSC.setEditable(false);
        tfKlientZakazSC.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfKlientZakazSC.setForeground(new java.awt.Color(153, 0, 0));
        tfKlientZakazSC.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfKlientZakazSC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientZakazSCActionPerformed(evt);
            }
        });

        tfKlientZakazLCod.setEditable(false);
        tfKlientZakazLCod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfKlientZakazLCod.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientZakazLCod.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfKlientZakazLCod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientZakazLCodActionPerformed(evt);
            }
        });

        tfKlientZakazSCod.setEditable(false);
        tfKlientZakazSCod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfKlientZakazSCod.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientZakazSCod.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfKlientZakazSCod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientZakazSCodActionPerformed(evt);
            }
        });

        tfKlientZakazFIO.setEditable(false);
        tfKlientZakazFIO.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfKlientZakazFIO.setForeground(new java.awt.Color(0, 0, 153));
        tfKlientZakazFIO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKlientZakazFIOActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tfKlientZakazLCod, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(tfKlientZakazSCod, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(tfKlientZakazFIO, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(tfKlientZakazSC, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(tfKlientZakazID_zak, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(bbZakaz, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tfKlientZakazLCod, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tfKlientZakazSCod, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tfKlientZakazFIO, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(bbZakaz)
                        .addComponent(tfKlientZakazID_zak, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tfKlientZakazSC, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 5, Short.MAX_VALUE))
        );

        tabZakaz.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        tabZakaz.setForeground(new java.awt.Color(0, 0, 153));
        tabZakaz.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "№ пп", "Артикул", "Наименование", "Цена", "Кол", "Сумма", "Кол.Услуг"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabZakaz.setColumnSelectionAllowed(true);
        tabZakaz.setRowHeight(34);
        tabZakaz.setShowGrid(true);
        tabZakaz.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabZakazMouseClicked(evt);
            }
        });
        jScrollPane9.setViewportView(tabZakaz);
        tabZakaz.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tabZakaz.getColumnModel().getColumnCount() > 0) {
            tabZakaz.getColumnModel().getColumn(0).setResizable(false);
            tabZakaz.getColumnModel().getColumn(0).setPreferredWidth(30);
            tabZakaz.getColumnModel().getColumn(1).setResizable(false);
            tabZakaz.getColumnModel().getColumn(1).setPreferredWidth(20);
            tabZakaz.getColumnModel().getColumn(2).setResizable(false);
            tabZakaz.getColumnModel().getColumn(2).setPreferredWidth(340);
            tabZakaz.getColumnModel().getColumn(3).setResizable(false);
            tabZakaz.getColumnModel().getColumn(3).setPreferredWidth(50);
            tabZakaz.getColumnModel().getColumn(3).setHeaderValue("Цена");
            tabZakaz.getColumnModel().getColumn(4).setResizable(false);
            tabZakaz.getColumnModel().getColumn(4).setPreferredWidth(50);
            tabZakaz.getColumnModel().getColumn(4).setHeaderValue("Кол");
            tabZakaz.getColumnModel().getColumn(5).setResizable(false);
            tabZakaz.getColumnModel().getColumn(5).setPreferredWidth(80);
            tabZakaz.getColumnModel().getColumn(5).setHeaderValue("Сумма");
            tabZakaz.getColumnModel().getColumn(6).setResizable(false);
            tabZakaz.getColumnModel().getColumn(6).setPreferredWidth(40);
        }

        bbZakazZak.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbZakazZak.setForeground(new java.awt.Color(0, 0, 153));
        bbZakazZak.setText("Закрыть");
        bbZakazZak.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bbZakazZak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbZakazZakActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panZakazLayout = new javax.swing.GroupLayout(panZakaz);
        panZakaz.setLayout(panZakazLayout);
        panZakazLayout.setHorizontalGroup(
            panZakazLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panZakazLayout.createSequentialGroup()
                .addGroup(panZakazLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panZakazLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bbZakazZak, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane9)
        );
        panZakazLayout.setVerticalGroup(
            panZakazLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panZakazLayout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bbZakazZak)
                .addContainerGap())
        );

        tabPanMain.addTab("Заказ", panZakaz);

        pznPrise.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                pznPriseAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        bbPriseZagruz.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbPriseZagruz.setForeground(new java.awt.Color(0, 0, 153));
        bbPriseZagruz.setText("Загрузить Прайс-Лист");
        bbPriseZagruz.setEnabled(false);
        bbPriseZagruz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbPriseZagruzActionPerformed(evt);
            }
        });

        bbNewTowar.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbNewTowar.setForeground(new java.awt.Color(0, 0, 153));
        bbNewTowar.setText("Новый товар");
        bbNewTowar.setEnabled(false);
        bbNewTowar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbNewTowarActionPerformed(evt);
            }
        });

        bbPriseSave.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbPriseSave.setForeground(new java.awt.Color(0, 0, 153));
        bbPriseSave.setText("Сохранить");
        bbPriseSave.setEnabled(false);
        bbPriseSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbPriseSaveActionPerformed(evt);
            }
        });

        tfTowarNewCena.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfTowarNewCena.setForeground(new java.awt.Color(0, 0, 153));
        tfTowarNewCena.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfTowarNewCenaActionPerformed(evt);
            }
        });
        tfTowarNewCena.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfTowarNewCenaKeyTyped(evt);
            }
        });

        tfTowarNewSort.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfTowarNewSort.setForeground(new java.awt.Color(0, 0, 153));
        tfTowarNewSort.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfTowarNewSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfTowarNewSortActionPerformed(evt);
            }
        });
        tfTowarNewSort.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfTowarNewSortKeyTyped(evt);
            }
        });

        lbInfoNewTowar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lbInfoNewTowar.setForeground(new java.awt.Color(0, 0, 153));
        lbInfoNewTowar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbInfoNewTowar.setText("Группа                                                         Артикул                               Наименование нового товара                                          Цена         Сорт.    Услуга");

        bbPrisePrihod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbPrisePrihod.setForeground(new java.awt.Color(0, 0, 153));
        bbPrisePrihod.setText("Приходовать Товар");
        bbPrisePrihod.setEnabled(false);
        bbPrisePrihod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbPrisePrihodActionPerformed(evt);
            }
        });

        chPriseRedakt.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        chPriseRedakt.setForeground(new java.awt.Color(102, 0, 102));
        chPriseRedakt.setText("Редактировать Цену, Название, Сортировка");
        chPriseRedakt.setEnabled(false);
        chPriseRedakt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chPriseRedaktActionPerformed(evt);
            }
        });

        cbPriseGroup.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        cbPriseGroup.setForeground(new java.awt.Color(0, 0, 153));
        cbPriseGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbPriseGroupActionPerformed(evt);
            }
        });

        tfTowarNewArtikul.setEditable(false);
        tfTowarNewArtikul.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfTowarNewArtikul.setForeground(new java.awt.Color(0, 0, 153));
        tfTowarNewArtikul.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfTowarNewArtikul.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfTowarNewArtikulActionPerformed(evt);
            }
        });

        tfTowarNewName.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfTowarNewName.setForeground(new java.awt.Color(0, 0, 153));
        tfTowarNewName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfTowarNewNameActionPerformed(evt);
            }
        });

        tfPrihodID_prih.setEditable(false);
        tfPrihodID_prih.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfPrihodID_prih.setForeground(new java.awt.Color(153, 0, 0));
        tfPrihodID_prih.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfPrihodID_prih.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfPrihodID_prihActionPerformed(evt);
            }
        });

        chUsluga.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        chUsluga.setForeground(new java.awt.Color(0, 0, 153));
        chUsluga.setBorderPaintedFlat(true);
        chUsluga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chUslugaActionPerformed(evt);
            }
        });

        tfPrihodPrim.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tfPrihodPrim.setForeground(new java.awt.Color(0, 0, 153));
        tfPrihodPrim.setText("Примечание");
        tfPrihodPrim.setEnabled(false);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(bbPriseZagruz)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chPriseRedakt, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bbPriseSave, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tfPrihodID_prih, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bbPrisePrihod, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfPrihodPrim))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(cbPriseGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfTowarNewArtikul, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfTowarNewName, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfTowarNewCena, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfTowarNewSort, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(chUsluga, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(bbNewTowar, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lbInfoNewTowar, javax.swing.GroupLayout.PREFERRED_SIZE, 1033, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(tfPrihodPrim, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(bbPriseZagruz)
                        .addComponent(bbPriseSave)
                        .addComponent(bbPrisePrihod)
                        .addComponent(chPriseRedakt, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tfPrihodID_prih, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(9, 9, 9)
                .addComponent(lbInfoNewTowar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(cbPriseGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tfTowarNewArtikul, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfTowarNewCena, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfTowarNewName, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(tfTowarNewSort, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addComponent(bbNewTowar)
                        .addContainerGap())
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chUsluga)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        tabPrise.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        tabPrise.setForeground(new java.awt.Color(0, 0, 153));
        tabPrise.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "№ пп", "Артикул", "Наименование", "Цена", "Приход", "Остаток", "Приход М", "Реализация", "Сортировка"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, true, true, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabPrise.setColumnSelectionAllowed(true);
        tabPrise.setRowHeight(34);
        tabPrise.setShowGrid(true);
        tabPrise.addHierarchyListener(new java.awt.event.HierarchyListener() {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
                tabPriseHierarchyChanged(evt);
            }
        });
        tabPrise.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                tabPriseMouseDragged(evt);
            }
        });
        tabPrise.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabPriseMouseClicked(evt);
            }
        });
        tabPrise.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                tabPriseInputMethodTextChanged(evt);
            }
        });
        tabPrise.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tabPriseKeyPressed(evt);
            }
        });
        jScrollPane10.setViewportView(tabPrise);
        tabPrise.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tabPrise.getColumnModel().getColumnCount() > 0) {
            tabPrise.getColumnModel().getColumn(0).setResizable(false);
            tabPrise.getColumnModel().getColumn(0).setPreferredWidth(15);
            tabPrise.getColumnModel().getColumn(1).setResizable(false);
            tabPrise.getColumnModel().getColumn(1).setPreferredWidth(60);
            tabPrise.getColumnModel().getColumn(2).setResizable(false);
            tabPrise.getColumnModel().getColumn(2).setPreferredWidth(300);
            tabPrise.getColumnModel().getColumn(3).setResizable(false);
            tabPrise.getColumnModel().getColumn(3).setPreferredWidth(60);
            tabPrise.getColumnModel().getColumn(4).setResizable(false);
            tabPrise.getColumnModel().getColumn(4).setPreferredWidth(60);
            tabPrise.getColumnModel().getColumn(5).setResizable(false);
            tabPrise.getColumnModel().getColumn(5).setPreferredWidth(20);
            tabPrise.getColumnModel().getColumn(6).setResizable(false);
            tabPrise.getColumnModel().getColumn(6).setPreferredWidth(20);
            tabPrise.getColumnModel().getColumn(7).setResizable(false);
            tabPrise.getColumnModel().getColumn(7).setPreferredWidth(90);
            tabPrise.getColumnModel().getColumn(8).setResizable(false);
            tabPrise.getColumnModel().getColumn(8).setPreferredWidth(30);
        }

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane10)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
                .addGap(4, 4, 4))
        );

        javax.swing.GroupLayout pznPriseLayout = new javax.swing.GroupLayout(pznPrise);
        pznPrise.setLayout(pznPriseLayout);
        pznPriseLayout.setHorizontalGroup(
            pznPriseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pznPriseLayout.createSequentialGroup()
                .addGroup(pznPriseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pznPriseLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pznPriseLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pznPriseLayout.setVerticalGroup(
            pznPriseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pznPriseLayout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(4, 4, 4))
        );

        tabPanMain.addTab("Прайс-Листа", pznPrise);

        panBD.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                panBDAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        tpBD.setForeground(new java.awt.Color(0, 102, 51));
        tpBD.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tpBD.setToolTipText("");
        tpBD.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tpBD.setVerifyInputWhenFocusTarget(false);
        tpBD.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                tpBDAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        panBDzakazTop.setForeground(new java.awt.Color(0, 0, 153));
        panBDzakazTop.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        panBDzakazTop.setVerifyInputWhenFocusTarget(false);

        bbBDzakaz.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbBDzakaz.setForeground(new java.awt.Color(0, 0, 153));
        bbBDzakaz.setText("Сформировать отчёт по заказам");
        bbBDzakaz.setEnabled(false);
        bbBDzakaz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbBDzakazActionPerformed(evt);
            }
        });

        bbBDzakazCSV.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbBDzakazCSV.setForeground(new java.awt.Color(0, 0, 153));
        bbBDzakazCSV.setText("Сохранить в csv");
        bbBDzakazCSV.setEnabled(false);
        bbBDzakazCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbBDzakazCSVActionPerformed(evt);
            }
        });

        bbBDzakazXLS.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbBDzakazXLS.setForeground(new java.awt.Color(0, 0, 153));
        bbBDzakazXLS.setText("Сохранить в xls");
        bbBDzakazXLS.setEnabled(false);
        bbBDzakazXLS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbBDzakazXLSActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panBDzakazTopLayout = new javax.swing.GroupLayout(panBDzakazTop);
        panBDzakazTop.setLayout(panBDzakazTopLayout);
        panBDzakazTopLayout.setHorizontalGroup(
            panBDzakazTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDzakazTopLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(bbBDzakaz)
                .addGap(500, 500, 500)
                .addComponent(bbBDzakazCSV)
                .addGap(18, 18, 18)
                .addComponent(bbBDzakazXLS)
                .addContainerGap(148, Short.MAX_VALUE))
        );
        panBDzakazTopLayout.setVerticalGroup(
            panBDzakazTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDzakazTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(bbBDzakaz)
                .addComponent(bbBDzakazCSV)
                .addComponent(bbBDzakazXLS))
        );

        tabBDzakaz.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tabBDzakaz.setForeground(new java.awt.Color(0, 0, 153));
        tabBDzakaz.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "№ пп", "Дата", "№ заказа", "Id Клиента", "ФИО Клиента", "Сумма", "Скидка %", "Нал/QR", "Чек", "Исполнитель"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabBDzakaz.setColumnSelectionAllowed(true);
        tabBDzakaz.setComponentPopupMenu(ppZakazItem);
        tabBDzakaz.setRowHeight(34);
        tabBDzakaz.setRowMargin(3);
        tabBDzakaz.setShowGrid(true);
        tabBDzakaz.addHierarchyListener(new java.awt.event.HierarchyListener() {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
                tabBDzakazHierarchyChanged(evt);
            }
        });
        tabBDzakaz.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                tabBDzakazMouseDragged(evt);
            }
        });
        tabBDzakaz.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabBDzakazMouseClicked(evt);
            }
        });
        tabBDzakaz.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                tabBDzakazInputMethodTextChanged(evt);
            }
        });
        tabBDzakaz.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tabBDzakazKeyPressed(evt);
            }
        });
        jScrollPane11.setViewportView(tabBDzakaz);
        tabBDzakaz.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tabBDzakaz.getColumnModel().getColumnCount() > 0) {
            tabBDzakaz.getColumnModel().getColumn(0).setResizable(false);
            tabBDzakaz.getColumnModel().getColumn(0).setPreferredWidth(15);
            tabBDzakaz.getColumnModel().getColumn(1).setPreferredWidth(150);
            tabBDzakaz.getColumnModel().getColumn(2).setResizable(false);
            tabBDzakaz.getColumnModel().getColumn(2).setPreferredWidth(60);
            tabBDzakaz.getColumnModel().getColumn(3).setResizable(false);
            tabBDzakaz.getColumnModel().getColumn(3).setPreferredWidth(60);
            tabBDzakaz.getColumnModel().getColumn(4).setResizable(false);
            tabBDzakaz.getColumnModel().getColumn(4).setPreferredWidth(300);
            tabBDzakaz.getColumnModel().getColumn(5).setResizable(false);
            tabBDzakaz.getColumnModel().getColumn(5).setPreferredWidth(60);
            tabBDzakaz.getColumnModel().getColumn(6).setResizable(false);
            tabBDzakaz.getColumnModel().getColumn(6).setPreferredWidth(30);
            tabBDzakaz.getColumnModel().getColumn(7).setResizable(false);
            tabBDzakaz.getColumnModel().getColumn(7).setPreferredWidth(20);
            tabBDzakaz.getColumnModel().getColumn(8).setResizable(false);
            tabBDzakaz.getColumnModel().getColumn(8).setPreferredWidth(20);
            tabBDzakaz.getColumnModel().getColumn(9).setResizable(false);
            tabBDzakaz.getColumnModel().getColumn(9).setPreferredWidth(90);
        }

        tfBDZakazSumma.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfBDZakazSumma.setForeground(new java.awt.Color(0, 102, 0));
        tfBDZakazSumma.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfBDZakazSumma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfBDZakazSummaActionPerformed(evt);
            }
        });

        lbBDzakazSumma.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lbBDzakazSumma.setForeground(new java.awt.Color(0, 102, 0));
        lbBDzakazSumma.setText("Сумма Итого:");

        tfBDZakazNal.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfBDZakazNal.setForeground(new java.awt.Color(0, 0, 153));
        tfBDZakazNal.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfBDZakazNal.setCaretColor(new java.awt.Color(0, 0, 153));
        tfBDZakazNal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfBDZakazNalActionPerformed(evt);
            }
        });

        lbBDzakazNal.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbBDzakazNal.setForeground(new java.awt.Color(0, 0, 153));
        lbBDzakazNal.setText("Наличные");

        lbBDzakazQR.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbBDzakazQR.setForeground(new java.awt.Color(102, 0, 102));
        lbBDzakazQR.setText("QR-код");

        tfBDZakazQR.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfBDZakazQR.setForeground(new java.awt.Color(102, 0, 102));
        tfBDZakazQR.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfBDZakazQR.setCaretColor(new java.awt.Color(0, 0, 153));
        tfBDZakazQR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfBDZakazQRActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panBDzakazBottomLayout = new javax.swing.GroupLayout(panBDzakazBottom);
        panBDzakazBottom.setLayout(panBDzakazBottomLayout);
        panBDzakazBottomLayout.setHorizontalGroup(
            panBDzakazBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panBDzakazBottomLayout.createSequentialGroup()
                .addGap(550, 550, 550)
                .addComponent(lbBDzakazSumma)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfBDZakazSumma, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(lbBDzakazNal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfBDZakazNal, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbBDzakazQR)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfBDZakazQR, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panBDzakazBottomLayout.setVerticalGroup(
            panBDzakazBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDzakazBottomLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(panBDzakazBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfBDZakazSumma, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbBDzakazSumma)
                    .addComponent(tfBDZakazNal, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbBDzakazNal)
                    .addComponent(lbBDzakazQR)
                    .addComponent(tfBDZakazQR, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7))
        );

        javax.swing.GroupLayout panTabZakazLayout = new javax.swing.GroupLayout(panTabZakaz);
        panTabZakaz.setLayout(panTabZakazLayout);
        panTabZakazLayout.setHorizontalGroup(
            panTabZakazLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panTabZakazLayout.createSequentialGroup()
                .addComponent(panBDzakazBottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jScrollPane11)
        );
        panTabZakazLayout.setVerticalGroup(
            panTabZakazLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panTabZakazLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panBDzakazBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );

        javax.swing.GroupLayout panBDzakazLayout = new javax.swing.GroupLayout(panBDzakaz);
        panBDzakaz.setLayout(panBDzakazLayout);
        panBDzakazLayout.setHorizontalGroup(
            panBDzakazLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDzakazLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(panBDzakazLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panBDzakazLayout.createSequentialGroup()
                        .addComponent(panTabZakaz, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(11, 11, 11))
                    .addGroup(panBDzakazLayout.createSequentialGroup()
                        .addComponent(panBDzakazTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        panBDzakazLayout.setVerticalGroup(
            panBDzakazLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panBDzakazLayout.createSequentialGroup()
                .addComponent(panBDzakazTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panTabZakaz, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );

        tpBD.addTab("Заказы клиентов", panBDzakaz);

        tfBDprihodSumma.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfBDprihodSumma.setForeground(new java.awt.Color(102, 0, 102));
        tfBDprihodSumma.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfBDprihodSumma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfBDprihodSummaActionPerformed(evt);
            }
        });

        lbBDzakazSumma4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lbBDzakazSumma4.setForeground(new java.awt.Color(0, 0, 102));
        lbBDzakazSumma4.setText("Сумма приходов:");

        javax.swing.GroupLayout panBDprihodBottomLayout = new javax.swing.GroupLayout(panBDprihodBottom);
        panBDprihodBottom.setLayout(panBDprihodBottomLayout);
        panBDprihodBottomLayout.setHorizontalGroup(
            panBDprihodBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panBDprihodBottomLayout.createSequentialGroup()
                .addGap(600, 600, 600)
                .addComponent(lbBDzakazSumma4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfBDprihodSumma, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(427, Short.MAX_VALUE))
        );
        panBDprihodBottomLayout.setVerticalGroup(
            panBDprihodBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDprihodBottomLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(panBDprihodBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfBDprihodSumma, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbBDzakazSumma4))
                .addGap(7, 7, 7))
        );

        tabBDprihod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tabBDprihod.setForeground(new java.awt.Color(0, 0, 153));
        tabBDprihod.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "№ пп", "Дата", "№ прихода", "№ поставщика", "Поставщик", "Сумма", "Исполнитель", "Примечание"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabBDprihod.setColumnSelectionAllowed(true);
        tabBDprihod.setComponentPopupMenu(ppPrihodItem);
        tabBDprihod.setRowHeight(34);
        tabBDprihod.setRowMargin(3);
        tabBDprihod.setShowGrid(true);
        tabBDprihod.addHierarchyListener(new java.awt.event.HierarchyListener() {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
                tabBDprihodHierarchyChanged(evt);
            }
        });
        tabBDprihod.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                tabBDprihodMouseDragged(evt);
            }
        });
        tabBDprihod.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabBDprihodMouseClicked(evt);
            }
        });
        tabBDprihod.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                tabBDprihodInputMethodTextChanged(evt);
            }
        });
        tabBDprihod.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tabBDprihodKeyPressed(evt);
            }
        });
        jScrollPane13.setViewportView(tabBDprihod);
        tabBDprihod.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tabBDprihod.getColumnModel().getColumnCount() > 0) {
            tabBDprihod.getColumnModel().getColumn(0).setResizable(false);
            tabBDprihod.getColumnModel().getColumn(0).setPreferredWidth(15);
            tabBDprihod.getColumnModel().getColumn(1).setResizable(false);
            tabBDprihod.getColumnModel().getColumn(1).setPreferredWidth(150);
            tabBDprihod.getColumnModel().getColumn(2).setResizable(false);
            tabBDprihod.getColumnModel().getColumn(2).setPreferredWidth(50);
            tabBDprihod.getColumnModel().getColumn(3).setResizable(false);
            tabBDprihod.getColumnModel().getColumn(3).setPreferredWidth(50);
            tabBDprihod.getColumnModel().getColumn(4).setResizable(false);
            tabBDprihod.getColumnModel().getColumn(4).setPreferredWidth(300);
            tabBDprihod.getColumnModel().getColumn(5).setResizable(false);
            tabBDprihod.getColumnModel().getColumn(5).setPreferredWidth(50);
            tabBDprihod.getColumnModel().getColumn(6).setResizable(false);
            tabBDprihod.getColumnModel().getColumn(6).setPreferredWidth(80);
            tabBDprihod.getColumnModel().getColumn(7).setPreferredWidth(190);
        }

        bbBDprihod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbBDprihod.setForeground(new java.awt.Color(0, 0, 153));
        bbBDprihod.setText("Сформировать отчёт по приходам");
        bbBDprihod.setEnabled(false);
        bbBDprihod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbBDprihodActionPerformed(evt);
            }
        });

        bbBDprihodCSV.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbBDprihodCSV.setForeground(new java.awt.Color(0, 0, 153));
        bbBDprihodCSV.setText("Сохранить в csv");
        bbBDprihodCSV.setEnabled(false);
        bbBDprihodCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbBDprihodCSVActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panBDprihodTopLayout = new javax.swing.GroupLayout(panBDprihodTop);
        panBDprihodTop.setLayout(panBDprihodTopLayout);
        panBDprihodTopLayout.setHorizontalGroup(
            panBDprihodTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDprihodTopLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(bbBDprihod)
                .addGap(500, 500, 500)
                .addComponent(bbBDprihodCSV)
                .addContainerGap(311, Short.MAX_VALUE))
        );
        panBDprihodTopLayout.setVerticalGroup(
            panBDprihodTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDprihodTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(bbBDprihod)
                .addComponent(bbBDprihodCSV))
        );

        javax.swing.GroupLayout panBDprihodLayout = new javax.swing.GroupLayout(panBDprihod);
        panBDprihod.setLayout(panBDprihodLayout);
        panBDprihodLayout.setHorizontalGroup(
            panBDprihodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDprihodLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(panBDprihodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panBDprihodBottom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panBDprihodTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        panBDprihodLayout.setVerticalGroup(
            panBDprihodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDprihodLayout.createSequentialGroup()
                .addComponent(panBDprihodTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panBDprihodBottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );

        tpBD.addTab("Оприходование товаров", panBDprihod);

        lbBDzakazDate.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbBDzakazDate.setForeground(new java.awt.Color(0, 102, 0));
        lbBDzakazDate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbBDzakazDate.setText("Дата");

        tfBDZakazDat.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfBDZakazDat.setForeground(new java.awt.Color(102, 0, 102));
        tfBDZakazDat.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfBDZakazDat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfBDZakazDatActionPerformed(evt);
            }
        });
        tfBDZakazDat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfBDZakazDatKeyTyped(evt);
            }
        });

        lbBDzakazMon.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbBDzakazMon.setForeground(new java.awt.Color(0, 102, 0));
        lbBDzakazMon.setText("Месяц");

        cbBDzakazMon.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        cbBDzakazMon.setForeground(new java.awt.Color(0, 0, 153));
        cbBDzakazMon.setVerifyInputWhenFocusTarget(false);
        cbBDzakazMon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbBDzakazMonActionPerformed(evt);
            }
        });

        lbBDzakazGod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbBDzakazGod.setForeground(new java.awt.Color(0, 102, 0));
        lbBDzakazGod.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbBDzakazGod.setText("Год");

        tfBDZakazGod.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfBDZakazGod.setForeground(new java.awt.Color(102, 0, 102));
        tfBDZakazGod.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfBDZakazGod.setText("2023");
        tfBDZakazGod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfBDZakazGodActionPerformed(evt);
            }
        });
        tfBDZakazGod.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfBDZakazGodKeyTyped(evt);
            }
        });

        tfBDZakazLcod.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfBDZakazLcod.setForeground(new java.awt.Color(102, 0, 102));
        tfBDZakazLcod.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfBDZakazLcod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfBDZakazLcodActionPerformed(evt);
            }
        });
        tfBDZakazLcod.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfBDZakazLcodKeyTyped(evt);
            }
        });

        chBDzakazLCod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        chBDzakazLCod.setForeground(new java.awt.Color(0, 0, 153));
        chBDzakazLCod.setText("С учётом Клиента");
        chBDzakazLCod.setBorderPaintedFlat(true);
        chBDzakazLCod.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        chBDzakazLCod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chBDzakazLCodActionPerformed(evt);
            }
        });

        lbBDzakazGod1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbBDzakazGod1.setForeground(new java.awt.Color(0, 102, 0));
        lbBDzakazGod1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbBDzakazGod1.setText("Id Клиента");

        javax.swing.GroupLayout panBDtopLayout = new javax.swing.GroupLayout(panBDtop);
        panBDtop.setLayout(panBDtopLayout);
        panBDtopLayout.setHorizontalGroup(
            panBDtopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDtopLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lbBDzakazDate, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfBDZakazDat, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(lbBDzakazMon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbBDzakazMon, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbBDzakazGod, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfBDZakazGod, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(chBDzakazLCod)
                .addGap(10, 10, 10)
                .addComponent(tfBDZakazLcod, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbBDzakazGod1, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panBDtopLayout.setVerticalGroup(
            panBDtopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDtopLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panBDtopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbBDzakazDate)
                    .addComponent(tfBDZakazDat, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbBDzakazMon)
                    .addComponent(cbBDzakazMon, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbBDzakazGod)
                    .addComponent(tfBDZakazGod, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfBDZakazLcod, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chBDzakazLCod, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbBDzakazGod1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panBDLayout = new javax.swing.GroupLayout(panBD);
        panBD.setLayout(panBDLayout);
        panBDLayout.setHorizontalGroup(
            panBDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDLayout.createSequentialGroup()
                .addGroup(panBDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panBDLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panBDtop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(tpBD))
                .addContainerGap())
        );
        panBDLayout.setVerticalGroup(
            panBDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBDLayout.createSequentialGroup()
                .addComponent(panBDtop, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tpBD)
                .addContainerGap())
        );

        tpBD.getAccessibleContext().setAccessibleName("Просмотр БД");

        tabPanMain.addTab("Просмотр операций", panBD);

        panKassa.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                panKassaAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        bbKassaOtchet.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKassaOtchet.setForeground(new java.awt.Color(0, 0, 153));
        bbKassaOtchet.setText("Отчёт");
        bbKassaOtchet.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bbKassaOtchet.setEnabled(false);
        bbKassaOtchet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKassaOtchetActionPerformed(evt);
            }
        });

        bbKassaRashod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKassaRashod.setForeground(new java.awt.Color(0, 0, 153));
        bbKassaRashod.setText("Расход");
        bbKassaRashod.setEnabled(false);
        bbKassaRashod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKassaRashodActionPerformed(evt);
            }
        });

        lbBDzakazSumma5.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbBDzakazSumma5.setForeground(new java.awt.Color(0, 102, 0));
        lbBDzakazSumma5.setText("Дата");

        tfKassaDat.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfKassaDat.setForeground(new java.awt.Color(102, 0, 102));
        tfKassaDat.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfKassaDat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKassaDatActionPerformed(evt);
            }
        });
        tfKassaDat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfKassaDatKeyTyped(evt);
            }
        });

        lbBDzakazSumma6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbBDzakazSumma6.setForeground(new java.awt.Color(0, 102, 0));
        lbBDzakazSumma6.setText("Месяц");

        cbKassaMon.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        cbKassaMon.setForeground(new java.awt.Color(0, 0, 153));
        cbKassaMon.setVerifyInputWhenFocusTarget(false);
        cbKassaMon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbKassaMonActionPerformed(evt);
            }
        });

        lbBDzakazSumma7.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbBDzakazSumma7.setForeground(new java.awt.Color(0, 102, 0));
        lbBDzakazSumma7.setText("Год");

        tfKassaGod.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        tfKassaGod.setForeground(new java.awt.Color(102, 0, 102));
        tfKassaGod.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfKassaGod.setText("2023");
        tfKassaGod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKassaGodActionPerformed(evt);
            }
        });
        tfKassaGod.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfKassaGodKeyTyped(evt);
            }
        });

        bbKassaPrihod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKassaPrihod.setForeground(new java.awt.Color(0, 0, 153));
        bbKassaPrihod.setText("Приход");
        bbKassaPrihod.setEnabled(false);
        bbKassaPrihod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKassaPrihodActionPerformed(evt);
            }
        });

        bbKassaCSV.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKassaCSV.setForeground(new java.awt.Color(0, 0, 153));
        bbKassaCSV.setText("save .csv");
        bbKassaCSV.setEnabled(false);
        bbKassaCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKassaCSVActionPerformed(evt);
            }
        });

        chKassaOtchetMon.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        chKassaOtchetMon.setForeground(new java.awt.Color(0, 0, 153));
        chKassaOtchetMon.setText("Отчёт за месяц");
        chKassaOtchetMon.setBorderPaintedFlat(true);
        chKassaOtchetMon.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        chKassaOtchetMon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chKassaOtchetMonActionPerformed(evt);
            }
        });

        bbKassaExcel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKassaExcel.setForeground(new java.awt.Color(0, 0, 153));
        bbKassaExcel.setText("save .xls");
        bbKassaExcel.setEnabled(false);
        bbKassaExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKassaExcelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panKasTopLayout = new javax.swing.GroupLayout(panKasTop);
        panKasTop.setLayout(panKasTopLayout);
        panKasTopLayout.setHorizontalGroup(
            panKasTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKasTopLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbBDzakazSumma5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfKassaDat, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbBDzakazSumma6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panKasTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(chKassaOtchetMon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbKassaMon, 0, 180, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbBDzakazSumma7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfKassaGod, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(bbKassaOtchet, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(bbKassaPrihod, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(bbKassaCSV)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bbKassaExcel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bbKassaRashod, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panKasTopLayout.setVerticalGroup(
            panKasTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKasTopLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panKasTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bbKassaRashod)
                    .addComponent(lbBDzakazSumma5)
                    .addComponent(tfKassaDat, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbBDzakazSumma6)
                    .addComponent(cbKassaMon, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbBDzakazSumma7)
                    .addComponent(tfKassaGod, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bbKassaOtchet)
                    .addComponent(bbKassaPrihod)
                    .addComponent(bbKassaCSV)
                    .addComponent(bbKassaExcel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chKassaOtchetMon, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        tabKasPrihod.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tabKasPrihod.setForeground(new java.awt.Color(0, 0, 153));
        tabKasPrihod.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "№", "Дата", "Id Клиента", "ФИО Клиента", "Сумма Чек", "QR-код", "Чек", " № нак.", "Исполнитель", "Прим"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabKasPrihod.setRowHeight(34);
        tabKasPrihod.setRowMargin(3);
        tabKasPrihod.setShowGrid(true);
        tabKasPrihod.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                tabKasPrihodComponentShown(evt);
            }
        });
        jScrollPane7.setViewportView(tabKasPrihod);
        if (tabKasPrihod.getColumnModel().getColumnCount() > 0) {
            tabKasPrihod.getColumnModel().getColumn(0).setResizable(false);
            tabKasPrihod.getColumnModel().getColumn(0).setPreferredWidth(20);
            tabKasPrihod.getColumnModel().getColumn(1).setResizable(false);
            tabKasPrihod.getColumnModel().getColumn(1).setPreferredWidth(150);
            tabKasPrihod.getColumnModel().getColumn(2).setResizable(false);
            tabKasPrihod.getColumnModel().getColumn(2).setPreferredWidth(60);
            tabKasPrihod.getColumnModel().getColumn(3).setPreferredWidth(150);
            tabKasPrihod.getColumnModel().getColumn(4).setResizable(false);
            tabKasPrihod.getColumnModel().getColumn(5).setResizable(false);
            tabKasPrihod.getColumnModel().getColumn(6).setResizable(false);
            tabKasPrihod.getColumnModel().getColumn(6).setPreferredWidth(10);
            tabKasPrihod.getColumnModel().getColumn(7).setResizable(false);
            tabKasPrihod.getColumnModel().getColumn(8).setResizable(false);
            tabKasPrihod.getColumnModel().getColumn(9).setResizable(false);
            tabKasPrihod.getColumnModel().getColumn(9).setPreferredWidth(20);
        }

        javax.swing.GroupLayout panKasPrihodLayout = new javax.swing.GroupLayout(panKasPrihod);
        panKasPrihod.setLayout(panKasPrihodLayout);
        panKasPrihodLayout.setHorizontalGroup(
            panKasPrihodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panKasPrihodLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE))
        );
        panKasPrihodLayout.setVerticalGroup(
            panKasPrihodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKasPrihodLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
                .addContainerGap())
        );

        tfKasPrihodQR.setEditable(false);
        tfKasPrihodQR.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        tfKasPrihodQR.setForeground(new java.awt.Color(51, 51, 0));
        tfKasPrihodQR.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        tfKasRashod.setEditable(false);
        tfKasRashod.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        tfKasRashod.setForeground(new java.awt.Color(102, 0, 102));
        tfKasRashod.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        tfKassaTek.setEditable(false);
        tfKassaTek.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        tfKassaTek.setForeground(new java.awt.Color(0, 102, 51));
        tfKassaTek.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        tfKasPrihodNal.setEditable(false);
        tfKasPrihodNal.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        tfKasPrihodNal.setForeground(new java.awt.Color(0, 0, 153));
        tfKasPrihodNal.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        tfKassaOstatok.setEditable(false);
        tfKassaOstatok.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        tfKassaOstatok.setForeground(new java.awt.Color(0, 0, 102));
        tfKassaOstatok.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel30.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(0, 0, 102));
        jLabel30.setText("Сумма Расход");

        jLabel31.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(0, 0, 102));
        jLabel31.setText("Сумма на Начало          Сумма в Кассе                                              Сумма приход            Сумма QR-код    ");

        javax.swing.GroupLayout panKasBottomLayout = new javax.swing.GroupLayout(panKasBottom);
        panKasBottom.setLayout(panKasBottomLayout);
        panKasBottomLayout.setHorizontalGroup(
            panKasBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKasBottomLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panKasBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panKasBottomLayout.createSequentialGroup()
                        .addComponent(tfKassaOstatok, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(tfKassaTek, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(166, 166, 166)
                        .addComponent(tfKasPrihodNal, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addComponent(tfKasPrihodQR, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel31))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panKasBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfKasRashod, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31))
        );
        panKasBottomLayout.setVerticalGroup(
            panKasBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKasBottomLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(panKasBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(jLabel30))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panKasBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfKasPrihodQR, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfKasRashod, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfKassaTek, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfKassaOstatok, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfKasPrihodNal, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        tabKasRashod.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tabKasRashod.setForeground(new java.awt.Color(102, 0, 102));
        tabKasRashod.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "№ пп", "Дата", "Id Клиента", "ФИО", "Сумма", "Исполнитель", "Примечание"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabKasRashod.setRowHeight(34);
        tabKasRashod.setRowMargin(3);
        tabKasRashod.setShowGrid(true);
        tabKasRashod.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                tabKasRashodComponentShown(evt);
            }
        });
        jScrollPane14.setViewportView(tabKasRashod);
        if (tabKasRashod.getColumnModel().getColumnCount() > 0) {
            tabKasRashod.getColumnModel().getColumn(0).setResizable(false);
            tabKasRashod.getColumnModel().getColumn(0).setPreferredWidth(10);
            tabKasRashod.getColumnModel().getColumn(1).setResizable(false);
            tabKasRashod.getColumnModel().getColumn(1).setPreferredWidth(150);
            tabKasRashod.getColumnModel().getColumn(2).setResizable(false);
            tabKasRashod.getColumnModel().getColumn(2).setPreferredWidth(50);
            tabKasRashod.getColumnModel().getColumn(3).setPreferredWidth(150);
            tabKasRashod.getColumnModel().getColumn(4).setResizable(false);
            tabKasRashod.getColumnModel().getColumn(4).setPreferredWidth(50);
            tabKasRashod.getColumnModel().getColumn(5).setResizable(false);
            tabKasRashod.getColumnModel().getColumn(5).setPreferredWidth(90);
            tabKasRashod.getColumnModel().getColumn(6).setPreferredWidth(150);
        }

        javax.swing.GroupLayout panKasRashodLayout = new javax.swing.GroupLayout(panKasRashod);
        panKasRashod.setLayout(panKasRashodLayout);
        panKasRashodLayout.setHorizontalGroup(
            panKasRashodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKasRashodLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        panKasRashodLayout.setVerticalGroup(
            panKasRashodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panKasRashodLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane14)
                .addContainerGap())
        );

        javax.swing.GroupLayout panKassaLayout = new javax.swing.GroupLayout(panKassa);
        panKassa.setLayout(panKassaLayout);
        panKassaLayout.setHorizontalGroup(
            panKassaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKassaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panKasTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panKassaLayout.createSequentialGroup()
                .addComponent(panKasPrihod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panKasRashod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(panKassaLayout.createSequentialGroup()
                .addComponent(panKasBottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panKassaLayout.setVerticalGroup(
            panKassaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKassaLayout.createSequentialGroup()
                .addComponent(panKasTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panKassaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panKasPrihod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panKasRashod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panKasBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabPanMain.addTab("Касса", panKassa);

        tfUslugaLCod.setEditable(false);
        tfUslugaLCod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfUslugaLCod.setForeground(new java.awt.Color(0, 0, 153));
        tfUslugaLCod.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfUslugaLCod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUslugaLCodActionPerformed(evt);
            }
        });

        tfUslugaFIO.setEditable(false);
        tfUslugaFIO.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfUslugaFIO.setForeground(new java.awt.Color(0, 0, 153));
        tfUslugaFIO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUslugaFIOActionPerformed(evt);
            }
        });

        tfUslugaID.setEditable(false);
        tfUslugaID.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfUslugaID.setForeground(new java.awt.Color(102, 0, 102));
        tfUslugaID.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfUslugaID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUslugaIDActionPerformed(evt);
            }
        });

        tfUslugaArtikul.setEditable(false);
        tfUslugaArtikul.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfUslugaArtikul.setForeground(new java.awt.Color(102, 0, 102));
        tfUslugaArtikul.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfUslugaArtikul.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUslugaArtikulActionPerformed(evt);
            }
        });

        tfUslugaName.setEditable(false);
        tfUslugaName.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfUslugaName.setForeground(new java.awt.Color(102, 0, 102));
        tfUslugaName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        tfUslugaName.setInheritsPopupMenu(true);
        tfUslugaName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUslugaNameActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panUslugiTopLayout = new javax.swing.GroupLayout(panUslugiTop);
        panUslugiTop.setLayout(panUslugiTopLayout);
        panUslugiTopLayout.setHorizontalGroup(
            panUslugiTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panUslugiTopLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tfUslugaLCod, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfUslugaFIO, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfUslugaID, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfUslugaArtikul, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfUslugaName, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panUslugiTopLayout.setVerticalGroup(
            panUslugiTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panUslugiTopLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panUslugiTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfUslugaLCod, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfUslugaFIO, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfUslugaID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfUslugaArtikul, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfUslugaName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        panUslugiTop2.setForeground(new java.awt.Color(0, 0, 153));
        panUslugiTop2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        panUslugiTop2.setVerifyInputWhenFocusTarget(false);

        bbUslugaZak.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbUslugaZak.setForeground(new java.awt.Color(0, 0, 153));
        bbUslugaZak.setText("Учесть посещение Клиента");
        bbUslugaZak.setEnabled(false);
        bbUslugaZak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbUslugaZakActionPerformed(evt);
            }
        });

        bbUslugaOtchet.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbUslugaOtchet.setForeground(new java.awt.Color(0, 0, 153));
        bbUslugaOtchet.setText("Отчёт по посещениям");
        bbUslugaOtchet.setEnabled(false);
        bbUslugaOtchet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbUslugaOtchetActionPerformed(evt);
            }
        });

        lbOstatok.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lbOstatok.setForeground(new java.awt.Color(0, 0, 153));
        lbOstatok.setText("Остаток:");

        tfUslugaOstatok.setEditable(false);
        tfUslugaOstatok.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        tfUslugaOstatok.setForeground(new java.awt.Color(0, 102, 0));
        tfUslugaOstatok.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfUslugaOstatok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUslugaOstatokActionPerformed(evt);
            }
        });

        tfUslugaIspTot.setEditable(false);
        tfUslugaIspTot.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        tfUslugaIspTot.setForeground(new java.awt.Color(102, 0, 102));
        tfUslugaIspTot.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfUslugaIspTot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUslugaIspTotActionPerformed(evt);
            }
        });

        lbOstatok1.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        lbOstatok1.setForeground(new java.awt.Color(0, 0, 153));
        lbOstatok1.setText("Всего использовано:");

        bbUslugaCSV.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbUslugaCSV.setForeground(new java.awt.Color(0, 0, 153));
        bbUslugaCSV.setText("save .csv");
        bbUslugaCSV.setEnabled(false);
        bbUslugaCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbUslugaCSVActionPerformed(evt);
            }
        });

        bbSaveXLS.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbSaveXLS.setForeground(new java.awt.Color(0, 0, 153));
        bbSaveXLS.setText("save .xls");
        bbSaveXLS.setEnabled(false);
        bbSaveXLS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbSaveXLSActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panUslugiTop2Layout = new javax.swing.GroupLayout(panUslugiTop2);
        panUslugiTop2.setLayout(panUslugiTop2Layout);
        panUslugiTop2Layout.setHorizontalGroup(
            panUslugiTop2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panUslugiTop2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(bbUslugaOtchet)
                .addGap(18, 18, 18)
                .addComponent(bbUslugaZak)
                .addGap(10, 10, 10)
                .addComponent(lbOstatok)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfUslugaOstatok, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lbOstatok1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfUslugaIspTot, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bbUslugaCSV)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bbSaveXLS)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panUslugiTop2Layout.setVerticalGroup(
            panUslugiTop2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panUslugiTop2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(bbUslugaZak)
                .addComponent(bbUslugaOtchet)
                .addComponent(lbOstatok)
                .addComponent(tfUslugaOstatok, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(tfUslugaIspTot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lbOstatok1)
                .addComponent(bbUslugaCSV)
                .addComponent(bbSaveXLS))
        );

        tabUslugi.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        tabUslugi.setForeground(new java.awt.Color(0, 0, 153));
        tabUslugi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "№ заказа", "Дата оплаты", "Артикул", "Наименование", "Опл.Услуг", "Использ.", "Использ..Всего", "Дата использования", "Остаток", "Исп."
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabUslugi.setColumnSelectionAllowed(true);
        tabUslugi.setRowHeight(34);
        tabUslugi.setShowGrid(true);
        tabUslugi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabUslugiMouseClicked(evt);
            }
        });
        jScrollPane12.setViewportView(tabUslugi);
        tabUslugi.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tabUslugi.getColumnModel().getColumnCount() > 0) {
            tabUslugi.getColumnModel().getColumn(0).setResizable(false);
            tabUslugi.getColumnModel().getColumn(0).setPreferredWidth(30);
            tabUslugi.getColumnModel().getColumn(1).setResizable(false);
            tabUslugi.getColumnModel().getColumn(1).setPreferredWidth(150);
            tabUslugi.getColumnModel().getColumn(2).setResizable(false);
            tabUslugi.getColumnModel().getColumn(2).setPreferredWidth(20);
            tabUslugi.getColumnModel().getColumn(3).setResizable(false);
            tabUslugi.getColumnModel().getColumn(3).setPreferredWidth(340);
            tabUslugi.getColumnModel().getColumn(4).setResizable(false);
            tabUslugi.getColumnModel().getColumn(4).setPreferredWidth(40);
            tabUslugi.getColumnModel().getColumn(5).setResizable(false);
            tabUslugi.getColumnModel().getColumn(5).setPreferredWidth(40);
            tabUslugi.getColumnModel().getColumn(6).setResizable(false);
            tabUslugi.getColumnModel().getColumn(6).setPreferredWidth(40);
            tabUslugi.getColumnModel().getColumn(7).setResizable(false);
            tabUslugi.getColumnModel().getColumn(7).setPreferredWidth(150);
            tabUslugi.getColumnModel().getColumn(8).setResizable(false);
            tabUslugi.getColumnModel().getColumn(8).setPreferredWidth(40);
            tabUslugi.getColumnModel().getColumn(9).setResizable(false);
            tabUslugi.getColumnModel().getColumn(9).setPreferredWidth(80);
        }

        javax.swing.GroupLayout panUslugiLayout = new javax.swing.GroupLayout(panUslugi);
        panUslugi.setLayout(panUslugiLayout);
        panUslugiLayout.setHorizontalGroup(
            panUslugiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panUslugiLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jScrollPane12)
                .addGap(0, 0, 0))
        );
        panUslugiLayout.setVerticalGroup(
            panUslugiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panUslugiLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panPosechenieLayout = new javax.swing.GroupLayout(panPosechenie);
        panPosechenie.setLayout(panPosechenieLayout);
        panPosechenieLayout.setHorizontalGroup(
            panPosechenieLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panUslugiTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panPosechenieLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panUslugi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(panUslugiTop2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panPosechenieLayout.setVerticalGroup(
            panPosechenieLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panPosechenieLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panUslugiTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panUslugiTop2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panUslugi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabPanMain.addTab("Посещение", panPosechenie);

        jPanel9.setForeground(new java.awt.Color(0, 0, 153));
        jPanel9.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        bbKKTdraiverTest.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbKKTdraiverTest.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTdraiverTest.setText("Соединение с драйвером");
        bbKKTdraiverTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTdraiverTestActionPerformed(evt);
            }
        });

        tfKKTComPort.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfKKTComPort.setForeground(new java.awt.Color(0, 0, 153));
        tfKKTComPort.setCaretColor(new java.awt.Color(0, 0, 153));

        lbComPort.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbComPort.setForeground(new java.awt.Color(0, 0, 153));
        lbComPort.setText("Сом порт:");

        lbComPort1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbComPort1.setForeground(new java.awt.Color(0, 0, 153));
        lbComPort1.setText("Модель:");

        tfKKTmodel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfKKTmodel.setForeground(new java.awt.Color(0, 0, 153));
        tfKKTmodel.setCaretColor(new java.awt.Color(0, 0, 153));
        tfKKTmodel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKKTmodelActionPerformed(evt);
            }
        });

        jLabel34.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(102, 0, 0));
        jLabel34.setText("На данной вкаладке ничего не нажимаем");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbComPort)
                .addGap(13, 13, 13)
                .addComponent(tfKKTComPort, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbComPort1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfKKTmodel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(bbKKTdraiverTest)
                .addGap(18, 18, 18)
                .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bbKKTdraiverTest)
                    .addComponent(tfKKTComPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbComPort)
                    .addComponent(lbComPort1)
                    .addComponent(tfKKTmodel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel34))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        bbKKTsessionOpen.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTsessionOpen.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTsessionOpen.setText("OpenSession");
        bbKKTsessionOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTsessionOpenActionPerformed(evt);
            }
        });

        bbKKTsessionClose.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTsessionClose.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTsessionClose.setText("CloseSession");
        bbKKTsessionClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTsessionCloseActionPerformed(evt);
            }
        });

        tfKKTotvet.setEditable(false);
        tfKKTotvet.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tfKKTotvet.setForeground(new java.awt.Color(0, 0, 153));
        tfKKTotvet.setText("KKTotvet");

        bbKKTdobriyTest.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTdobriyTest.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTdobriyTest.setText("Добрый день - Тест");
        bbKKTdobriyTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTdobriyTestActionPerformed(evt);
            }
        });

        tfKeySession.setEditable(false);
        tfKeySession.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tfKeySession.setForeground(new java.awt.Color(0, 0, 153));
        tfKeySession.setText("sessionKey");

        tfRezultKKT.setEditable(false);
        tfRezultKKT.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tfRezultKKT.setForeground(new java.awt.Color(0, 0, 153));
        tfRezultKKT.setText("rezult");

        tfKKTzapros.setEditable(false);
        tfKKTzapros.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tfKKTzapros.setForeground(new java.awt.Color(0, 0, 153));
        tfKKTzapros.setText("KKTzapros");

        tfKKTtext.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfKKTtext.setForeground(new java.awt.Color(0, 0, 153));
        tfKKTtext.setText("Добрый день!");
        tfKKTtext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfKKTtextActionPerformed(evt);
            }
        });

        bbKKTpostTest.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTpostTest.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTpostTest.setText("POST-запрос");
        bbKKTpostTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTpostTestActionPerformed(evt);
            }
        });

        bbKKTzapusk.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTzapusk.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTzapusk.setText("Запуск внешний");
        bbKKTzapusk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTzapuskActionPerformed(evt);
            }
        });

        tfDumpFile.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tfDumpFile.setForeground(new java.awt.Color(0, 0, 153));
        tfDumpFile.setText("c:\\java\\dampbat.bat");
        tfDumpFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfDumpFileActionPerformed(evt);
            }
        });

        bbKKTpythonTest.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTpythonTest.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTpythonTest.setText("Запуск Python");
        bbKKTpythonTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTpythonTestActionPerformed(evt);
            }
        });

        bbKKTpreobrazovanieTest.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTpreobrazovanieTest.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTpreobrazovanieTest.setText("Преобразование-Тестирование");
        bbKKTpreobrazovanieTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTpreobrazovanieTestActionPerformed(evt);
            }
        });

        bbKKTgetStatus.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTgetStatus.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTgetStatus.setText("GetStatus");
        bbKKTgetStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTgetStatusActionPerformed(evt);
            }
        });

        bbKKTdobriyDen.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTdobriyDen.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTdobriyDen.setText("Добрый день!");
        bbKKTdobriyDen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTdobriyDenActionPerformed(evt);
            }
        });

        bbKKTsmenaOpen.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTsmenaOpen.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTsmenaOpen.setText("OpenShift");
        bbKKTsmenaOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTsmenaOpenActionPerformed(evt);
            }
        });

        bbKKTsmenaClose.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTsmenaClose.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTsmenaClose.setText("CloseShift");
        bbKKTsmenaClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTsmenaCloseActionPerformed(evt);
            }
        });

        bbKKTopenCheck.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTopenCheck.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTopenCheck.setText("OpenCheck");
        bbKKTopenCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTopenCheckActionPerformed(evt);
            }
        });

        bbKKTaddGoods.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTaddGoods.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTaddGoods.setText("AddGoods");
        bbKKTaddGoods.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTaddGoodsActionPerformed(evt);
            }
        });

        bbKKTcloseCheck.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTcloseCheck.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTcloseCheck.setText("CloseCheck");
        bbKKTcloseCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTcloseCheckActionPerformed(evt);
            }
        });

        bbKKTtowar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        bbKKTtowar.setForeground(new java.awt.Color(0, 0, 153));
        bbKKTtowar.setText("Обслуживание товара");
        bbKKTtowar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbKKTtowarActionPerformed(evt);
            }
        });

        tabPanKKT.setForeground(new java.awt.Color(0, 0, 153));
        tabPanKKT.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        taKKTlog.setColumns(20);
        taKKTlog.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        taKKTlog.setForeground(new java.awt.Color(0, 0, 153));
        taKKTlog.setRows(5);
        jScrollPane17.setViewportView(taKKTlog);

        javax.swing.GroupLayout tpanKKTlogLayout = new javax.swing.GroupLayout(tpanKKTlog);
        tpanKKTlog.setLayout(tpanKKTlogLayout);
        tpanKKTlogLayout.setHorizontalGroup(
            tpanKKTlogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tpanKKTlogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 1256, Short.MAX_VALUE)
                .addContainerGap())
        );
        tpanKKTlogLayout.setVerticalGroup(
            tpanKKTlogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tpanKKTlogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPanKKT.addTab("Логи", tpanKKTlog);

        tpanKKTtowar.setForeground(new java.awt.Color(0, 0, 153));
        tpanKKTtowar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        taKKTtowar.setColumns(20);
        taKKTtowar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        taKKTtowar.setForeground(new java.awt.Color(0, 0, 153));
        taKKTtowar.setRows(5);
        jScrollPane15.setViewportView(taKKTtowar);

        javax.swing.GroupLayout tpanKKTtowarLayout = new javax.swing.GroupLayout(tpanKKTtowar);
        tpanKKTtowar.setLayout(tpanKKTtowarLayout);
        tpanKKTtowarLayout.setHorizontalGroup(
            tpanKKTtowarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tpanKKTtowarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 1246, Short.MAX_VALUE)
                .addContainerGap())
        );
        tpanKKTtowarLayout.setVerticalGroup(
            tpanKKTtowarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tpanKKTtowarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPanKKT.addTab("Товар", tpanKKTtowar);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfKKTotvet)
                    .addComponent(tfKKTzapros)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tabPanKKT, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(jPanel13Layout.createSequentialGroup()
                                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel13Layout.createSequentialGroup()
                                        .addComponent(bbKKTpythonTest)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(bbKKTpreobrazovanieTest)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfKKTtext, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(bbKKTdobriyTest)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(bbKKTpostTest))
                                    .addGroup(jPanel13Layout.createSequentialGroup()
                                        .addComponent(bbKKTsessionOpen)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(bbKKTsessionClose)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(bbKKTgetStatus)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bbKKTdobriyDen)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bbKKTsmenaOpen)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bbKKTsmenaClose)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(bbKKTopenCheck)
                                        .addGap(15, 15, 15)
                                        .addComponent(bbKKTaddGoods)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bbKKTcloseCheck))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                                        .addComponent(tfKeySession, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(tfRezultKKT, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(bbKKTzapusk)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfDumpFile, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(175, 175, 175)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bbKKTtowar)))
                        .addContainerGap())))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bbKKTsessionOpen)
                    .addComponent(bbKKTsessionClose)
                    .addComponent(bbKKTgetStatus)
                    .addComponent(bbKKTdobriyDen)
                    .addComponent(bbKKTsmenaOpen)
                    .addComponent(bbKKTsmenaClose)
                    .addComponent(bbKKTopenCheck)
                    .addComponent(bbKKTaddGoods)
                    .addComponent(bbKKTcloseCheck)
                    .addComponent(bbKKTtowar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfKeySession, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfRezultKKT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bbKKTzapusk, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfDumpFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfKKTzapros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfKKTotvet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bbKKTpythonTest, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bbKKTpreobrazovanieTest, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfKKTtext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bbKKTdobriyTest, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bbKKTpostTest, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabPanKKT)
                .addGap(38, 38, 38))
        );

        javax.swing.GroupLayout panKKTLayout = new javax.swing.GroupLayout(panKKT);
        panKKT.setLayout(panKKTLayout);
        panKKTLayout.setHorizontalGroup(
            panKKTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKKTLayout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(panKKTLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(70, 70, 70))
        );
        panKKTLayout.setVerticalGroup(
            panKKTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panKKTLayout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabPanMain.addTab("Параметры ККТ", panKKT);

        taLog.setColumns(20);
        taLog.setRows(5);
        jScrollPane5.setViewportView(taLog);

        bbNewFrame.setForeground(new java.awt.Color(0, 0, 153));
        bbNewFrame.setText("Сформировать отдельный фрейм программно");
        bbNewFrame.setEnabled(false);
        bbNewFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbNewFrameActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panLogDopLayout = new javax.swing.GroupLayout(panLogDop);
        panLogDop.setLayout(panLogDopLayout);
        panLogDopLayout.setHorizontalGroup(
            panLogDopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panLogDopLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bbNewFrame)
                .addContainerGap(13, Short.MAX_VALUE))
        );
        panLogDopLayout.setVerticalGroup(
            panLogDopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panLogDopLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bbNewFrame)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panLogLayout = new javax.swing.GroupLayout(panLog);
        panLog.setLayout(panLogLayout);
        panLogLayout.setHorizontalGroup(
            panLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panLogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 693, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panLogDop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(299, Short.MAX_VALUE))
        );
        panLogLayout.setVerticalGroup(
            panLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panLogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panLogLayout.createSequentialGroup()
                        .addComponent(panLogDop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(panLogLayout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 694, Short.MAX_VALUE)
                        .addGap(10, 10, 10))))
        );

        tabPanMain.addTab("Логи", panLog);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabPanMain, javax.swing.GroupLayout.DEFAULT_SIZE, 1315, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabPanMain, javax.swing.GroupLayout.DEFAULT_SIZE, 741, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void tabPanMainAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_tabPanMainAncestorAdded
        /*
        sVerProg = "2024.12.15";
        bbVerProg.setText("Версия: "+sVerProg);
        
        masArtikul = new int[50];
        masSort = new int[50];
        masUsluga = new int[50];
        masPNom = new int[1000];
        sPostavchik = "Поставщик";
                
        httpKKM = "http://localhost:50010/api.json";        
        sKKMkassirINN = "00000000000000000000";

        sTire70 = "";
        sTire180 = "";
        sTire99 = "";

        for (int i = 0; i < 180; i++) {
            if (i < 70) sTire70 += "-";
            if (i < 120) sTire99 += "*";
            if (i < 180) sTire180 += "-";
        }
        
        sTire70 += "\n";
        sTire180 += "\n";
        sTire99 += "\n";
        
        taKKTlog.setText(null);
        taKKTlog.append(sTire99);
        String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        taKKTlog.append("Дата: " + sDateTimeTek + "    Настройки программы.:\n");
        taKKTlog.append(sTire99);
        
        lbMainDateTime.setText("");
        progDir = System.getProperty("user.dir");
        progDir = progDir + "\\";
        sKKMmercComPort = "COM4";
        sKKMmercModel = "185F";
        
        userNameBD = "root";
        passwordBD = "admin";
        urlHostBD = "jdbc:MySQL://localhost";            
        String redHostBD = "";
        
        // Читаем файл с настройками
        try {
            
            try (BufferedReader reader = new BufferedReader(new FileReader("myServisProg.ini"))) {
                String line = reader.readLine();
                int ik = 0;
                while (line != null) {
                    ik++;
                    if (ik == 1) sKKMmercComPort = line;
                    if (ik == 2) sKKMmercModel = line;
                    if (ik == 3) sDumpDirLocal = line;
                    if (ik == 4) sDumpDirHost = line;
                    if (ik == 5) sDumpFileExe = line;
                    if (ik == 6) sWixod = line;
                    if (ik == 7) redHostBD = line;
                    line = reader.readLine();
                }
                urlHostBD = "jdbc:MySQL://"+redHostBD;
            }
            
        } catch (IOException e) {
        }
        taKKTlog.append("COM порт: "+sKKMmercComPort+"\n");
        taKKTlog.append("Тип ККТ: "+sKKMmercModel+"\n");
        taKKTlog.append("Папка для ежденевных дампов: "+sDumpDirLocal+"\n");
        taKKTlog.append("Папка для копирования ежденевных дампов: "+sDumpDirHost+"\n");
        taKKTlog.append("Папка с командой mysqldump (папка установки mySQL): "+sDumpFileExe+"\n");
        taKKTlog.append("Запрашивать подтверждение выхода из программы: "+sWixod+"\n");
        taKKTlog.append("Host БД : "+urlHostBD+"\n");
                    
        tfKKTComPort.setText(sKKMmercComPort);
        tfKKTmodel.setText(sKKMmercModel);
        tfDumpFile.setText(sDumpDirLocal);
        starTime1 = System.currentTimeMillis();
        */
    }//GEN-LAST:event_tabPanMainAncestorAdded

    private void tfBDZakazGodKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfBDZakazGodKeyTyped
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfBDZakazGodKeyTyped

    private void tfBDZakazGodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfBDZakazGodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfBDZakazGodActionPerformed

    private void cbBDzakazMonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbBDzakazMonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbBDzakazMonActionPerformed

    private void tfBDZakazDatKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfBDZakazDatKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfBDZakazDatKeyTyped

    private void tfBDZakazDatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfBDZakazDatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfBDZakazDatActionPerformed

    private void tpBDAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_tpBDAncestorAdded
        // TODO add your handling code here:
        cbBDzakazMon.removeAllItems();
        cbBDzakazMon.addItem("Январь");
        cbBDzakazMon.addItem("Февраль");
        cbBDzakazMon.addItem("Март");
        cbBDzakazMon.addItem("Апрель");
        cbBDzakazMon.addItem("Май");
        cbBDzakazMon.addItem("Июнь");
        cbBDzakazMon.addItem("Июль");
        cbBDzakazMon.addItem("Август");
        cbBDzakazMon.addItem("Сентябрь");
        cbBDzakazMon.addItem("Октябрь");
        cbBDzakazMon.addItem("Ноябрь");
        cbBDzakazMon.addItem("Декабрь");
        cbBDzakazMon.setMaximumRowCount(12);

        String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        String sMonTek = sDateTimeTek.substring(5, 7);
        String sGodTek = sDateTimeTek.substring(0, 4);
        String sDatTek = sDateTimeTek.substring(8, 10);
        int mont = Integer.parseInt(sMonTek);
        cbBDzakazMon.setSelectedIndex(mont - 1);
        tfBDZakazGod.setText(sGodTek);
        tfBDZakazDat.setText(sDatTek);

    }//GEN-LAST:event_tpBDAncestorAdded

    private void tfBDZakazSummaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfBDZakazSummaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfBDZakazSummaActionPerformed

    private void bbBDzakazActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbBDzakazActionPerformed
        // TODO add your handling code here:
        //Загружаем заказы
        bbBDzakaz.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
            taLog.append("Соединение с БД для zakaz прошло успешно ...\n");

            DefaultTableModel dtm = (DefaultTableModel) tabBDzakaz.getModel();

            //dtm.removeRow(0); //  Удаляем первую  строчку
            dtm.getDataVector().removeAllElements(); // удаляем все строки

            String sGod = tfBDZakazGod.getText();
            String sDat = tfBDZakazDat.getText();
            String sMon = Integer.toString(cbBDzakazMon.getSelectedIndex() + 1);

            String sDate1 = sGod + "-01-01";
            String sDate2 = sGod + "-12-31";

            if (cbBDzakazMon.getSelectedIndex() > -1) {
                sDate1 = sGod + "-" + sMon + "-01";
                sDate2 = sGod + "-" + sMon + "-31";
            }

            if (!"".equals(sDat)) {
                sDate1 = sGod + "-" + sMon + "-" + sDat;
                int mDat2 = Integer.parseInt(sDat);
                String sDat2 = Integer.toString(mDat2 + 1);
                sDate2 = sGod + "-" + sMon + "-" + sDat2;

                taLog.append("(Дата) ...\n");
                taLog.append("sDate1:" + sDate1 + " ...\n");
                taLog.append("sDate2:" + sDate2 + " ...\n");

            }
            String query;
            stmt = conn.createStatement();
            if (chBDzakazLCod.isSelected()) {
                query = "select date_zak,id_zak,lcod,fio,sum_zak,proc_skid,tip_nal,tip_chek,isp from myservis.zakaz where date_zak BETWEEN \"" + sDate1 + "\" AND \"" + sDate2 + "\" AND lcod="+tfBDZakazLcod.getText()+" order by date_zak";
            }else {
                query = "select date_zak,id_zak,lcod,fio,sum_zak,proc_skid,tip_nal,tip_chek,isp from myservis.zakaz where date_zak BETWEEN \"" + sDate1 + "\" AND \"" + sDate2 + "\" order by date_zak";
            }
            rs = stmt.executeQuery(query);
            int i = 0;
            int sumZakTot = 0;
            int sumNal = 0;
            int sumQR = 0;
            while (rs.next()) {
                i = i + 1;
                int idZak = rs.getInt("id_zak");
                int lcod = rs.getInt("lcod");
                String date_zak = rs.getString("date_zak");
                String fio = rs.getString("fio");
                int sumZak = rs.getInt("sum_zak");
                int procSkid = rs.getInt("proc_skid");
                int tipNal = rs.getInt("tip_nal");
                int tipChek = rs.getInt("tip_chek");
                String isp = rs.getString("isp");
                String sTipNal,sTipChek;
                if (tipNal==1) sTipNal="";
                else sTipNal="QR";
                
                if (tipChek==1) sTipChek="+";
                else sTipChek="";
                
                dtm.addRow(new Object[]{i, date_zak, idZak, lcod, fio, sumZak, procSkid, sTipNal, tipChek, isp});
                sumZakTot += sumZak;
                
                if (tipNal==1) sumNal += sumZak;
                else sumQR +=sumZak;
            }
            rs.close();
            stmt.close();
            
            tfBDZakazSumma.setText(Integer.toString(sumZakTot));
            tfBDZakazNal.setText(Integer.toString(sumNal));
            tfBDZakazQR.setText(Integer.toString(sumQR));
            
            taLog.append("Запрос к zakaz - выполнен успешно \n");
            if (i == 0) {
                dtm.getDataVector().removeAllElements(); // удаляем все строки
                dtm.addRow(new Object[]{"", "", "", "", "", "", "", "", "", ""});
                JFrame jfInfo = new JFrame();
                JOptionPane.showMessageDialog(jfInfo, "Заказов не обнаружено!");
                bbBDzakazCSV.setEnabled(false);
                bbBDzakazXLS.setEnabled(false);
            } else {
                bbBDzakazCSV.setEnabled(true);
                bbBDzakazXLS.setEnabled(true);
            }

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД zakaz \n");
                    taLog.append("-------------------------------------------------\n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        bbBDzakaz.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bbBDzakazActionPerformed

    private void tabBDzakazKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tabBDzakazKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tabBDzakazKeyPressed

    private void tabBDzakazInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tabBDzakazInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabBDzakazInputMethodTextChanged

    private void tabBDzakazMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabBDzakazMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tabBDzakazMouseClicked

    private void tabBDzakazMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabBDzakazMouseDragged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabBDzakazMouseDragged

    private void tabBDzakazHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_tabBDzakazHierarchyChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabBDzakazHierarchyChanged

    private void tabPriseKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tabPriseKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tabPriseKeyPressed

    private void tabPriseInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tabPriseInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabPriseInputMethodTextChanged

    private void tabPriseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabPriseMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tabPriseMouseClicked

    private void tabPriseMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabPriseMouseDragged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabPriseMouseDragged

    private void tabPriseHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_tabPriseHierarchyChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabPriseHierarchyChanged

    private void tfKlientZakazFIOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientZakazFIOActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientZakazFIOActionPerformed

    private void tfKlientZakazSCodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientZakazSCodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientZakazSCodActionPerformed

    private void tfKlientZakazLCodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientZakazLCodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientZakazLCodActionPerformed

    private void tabZakazMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabZakazMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tabZakazMouseClicked

    private void tfKlientZakazSCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientZakazSCActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientZakazSCActionPerformed

    private void tfKlientZakazID_zakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientZakazID_zakActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientZakazID_zakActionPerformed

    private void bbZakazActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbZakazActionPerformed
        // Оформление заказа
        bbZakaz.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        sDateTimeTek = "\"" + sDateTimeTek + "\"";
        sIspolnitel = "\"" + aktPolLogin + "\"";

        int LCod;
        if ("".equals(tfKlientZakazLCod.getText())) {
            LCod = 0;
        } else {
            LCod = Integer.parseInt(tfKlientZakazLCod.getText());
        }

        if (LCod <= 0) {
            JFrame jfInfo = new JFrame();
            JOptionPane.showMessageDialog(jfInfo, "Необходимо обязательно выбрать покупателя!");
        } else {

            try {
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

                conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);

                taLog.append("Запуск соединения с БД для insert в zakaz и zakaz_item ... \n");

                stmt = conn.createStatement();

                String query = "select sc,id_zak from myservis.nastroi";
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    tfKlientZakazID_zak.setText(rs.getString("id_zak"));
                    tfKlientZakazSC.setText(rs.getString("sc"));
                }
                rs.close();
                taLog.append("Запрос select для nastroi - выполнен успешно \n");

                String query2 = "update myservis.nastroi set id_zak=id_zak+1";
                stmt.executeUpdate(query2);

                taLog.append("Запрос update для nastroi - выполнен успешно \n");

                int kod_sc = Integer.parseInt(tfKlientZakazSC.getText());
                int id_zak = Integer.parseInt(tfKlientZakazID_zak.getText());
                //id_zak = id_zak + 1;
                id_zak++;

                String sFIO = "\"" + tfKlientZakazFIO.getText() + "\"";
                int tip_zak = 1; // Обычный заказ
                int tip_nal;
                int tip_chek;
                int tip_kas = 1;
                int sum_nal;
                int sum_gr;
                int sum_chek;
                int sum_bchek;

                if (chNal.isSelected()) {
                    sum_nal = Integer.parseInt(tfSummaZak.getText());
                    sum_gr = 0;
                    tip_nal = 1;

                } else {
                    sum_nal = 0;
                    sum_gr = Integer.parseInt(tfSummaZak.getText());
                    tip_nal = 2;  // QR-код
                }

                if (chOFD.isSelected()) {
                    sum_chek = Integer.parseInt(tfSummaZak.getText());
                    sum_bchek = 0;
                    tip_chek = 1;

                } else {
                    sum_chek = 0;
                    sum_bchek = Integer.parseInt(tfSummaZak.getText());
                    tip_chek = 2;
                }

                String query3 = "insert ignore into myservis.zakaz (sc,id_zak,nom_zak,lcod,scod,fio,sum_zak,sum_opl,tip_zak,tip_nal,tip_chek,proc_skid,date_zak,isp) values ("
                        + kod_sc + "," + id_zak + "," + id_zak + "," + tfKlientZakazLCod.getText() + "," + tfKlientZakazSCod.getText() + "," + sFIO + "," + tfSummaZak.getText() + "," + tfSummaZak.getText() + "," + tip_zak + "," + tip_nal + "," + tip_chek
                        + "," + tfKlientZakazSkidPr.getText() + "," + sDateTimeTek + "," + sIspolnitel
                        + ");";
                stmt.executeUpdate(query3);
                taLog.append("Запрос inset для zakaz - выполнен успешно \n");

                String query6 = "insert ignore into myservis.kassa (id_zak,date_kas,tip_kas,lcod,sum_nal,sum_gr,sum_chek,sum_bchek,tip_nal,tip_chek,fio,isp) values ("
                        + id_zak + "," + sDateTimeTek + "," + tip_kas + "," + tfKlientZakazLCod.getText() + "," + sum_nal + "," + sum_gr + "," + sum_chek + "," + sum_bchek + "," + tip_nal + "," + tip_chek
                        + "," + sFIO + "," + sIspolnitel
                        + ");";

                stmt.executeUpdate(query6);
                taLog.append("Запрос inset для kassa - выполнен успешно \n");

                taKKTtowar.setText(null);

                int wibUsluga=0;
                                
                for (int i = 0; i < tabZakaz.getRowCount(); i++) {
                    int artikul = Integer.parseInt(tabZakaz.getValueAt(i, 1).toString());
                    int cena = Integer.parseInt(tabZakaz.getValueAt(i, 3).toString());
                    int kol = Integer.parseInt(tabZakaz.getValueAt(i, 4).toString());
                    String sTowar = "\"" + tabZakaz.getValueAt(i, 2).toString() + "\"";

                    String sKolUslug = tabZakaz.getValueAt(i, 6).toString();
                    
                    int kolUslug;
                    if (("0".equals(sKolUslug)) | (sKolUslug=="")) {
                        kolUslug = 0;
                    } else {
                        kolUslug = Integer.parseInt(sKolUslug);
                        wibUsluga=1;                        
                    }
                    
                    String query4 = "insert ignore into myservis.zakaz_item (sc,id_zak,nom_zak,lcod,tip_zak,artikul,kol,cen,towar,date_zak,kol_uslug) values ("
                            + kod_sc + "," + id_zak + "," + id_zak + "," + tfKlientZakazLCod.getText() + "," + tip_zak + "," + artikul + "," + kol + "," + cena + "," + sTowar + "," + sDateTimeTek + "," + kolUslug
                            + ");";

                    if (kolUslug > 0) {
                        String query7 = "insert ignore into myservis.uslugi (id_zak,date_zak,lcod,artikul,usluga_name,isp,usluga_kol,usluga_ostat) values ("
                                + id_zak + "," + sDateTimeTek + "," + tfKlientZakazLCod.getText() + "," + artikul + "," + sTowar + "," + sIspolnitel + ","
                                + kolUslug + "," + kolUslug + ");";
                        stmt.executeUpdate(query7);
                    }
                    int ssSekciya;
                    int ssPredmet;
                    if (((artikul > 5000) & (artikul < 6000)) | ((artikul > 9000) & (artikul < 10000))) {
                        // Это товары
                        ssSekciya = 2;
                        ssPredmet = 1;
                    } else {
                        // Это услуги
                        ssSekciya = 1;
                        ssPredmet = 4;
                    }

                    String ssTowar = "\"productName\": \"" + strPreobraz(tabZakaz.getValueAt(i, 2).toString()) + "\", \"qty\": " + kol * 10000 + " , \"taxCode\": 6, \"price\":" + cena * 100 + ", \"section\":" + ssSekciya + ", \"productTypeCode\":" + ssPredmet;
                    masTowarKKT[i] = ssTowar;
                    //textJSON = "{\"sessionKey\": null, \"command\": \"OpenSession\", \"portName\": \"" + sKKMmercComPort + "\", \"model\": \"" + sKKMmercModel + "\"}";
                    taKKTtowar.append(ssTowar + '\n');

                    stmt.executeUpdate(query4);
                }

                taLog.append("Запрос inset для zakaz_item - выполнен успешно \n");

                for (int i = 0; i < tabZakaz.getRowCount(); i++) {
                    int artikul = Integer.parseInt(tabZakaz.getValueAt(i, 1).toString());
                    int kol = Integer.parseInt(tabZakaz.getValueAt(i, 4).toString());

                    String query5 = "update myservis.prise set ostat=ostat-" + kol + ",realiz=realiz+" + kol
                            + " where artikul=" + artikul;

                    stmt.executeUpdate(query5);
                }

                taLog.append("Запрос update для prise - выполнен успешно \n");

                stmt.close();

                if (chOFD.isSelected()) {
                    bbKKTtowarActionPerformed(evt);  // Отправка кассового чека в ОФД
                }
                
                if ( (wibUsluga == 0) | (LCod == 1000) | (LCod == 1001) | (LCod == 2000) | (LCod == 2001)  ){
                    JOptionPane.showMessageDialog(new JFrame(), "Заказ обслужен успешно!");                                            
                }else{
                    int showConfirmDialog;
                
                    showConfirmDialog = JOptionPane.showConfirmDialog(new JFrame(),
                    "В заказе использова услуга с Абонементом на несколько посещений.\n\nУчесть использование услуги в Абонементе?",
                    "В заказе использова услуга с Абонементом на несколько посещений.",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                    
                    if (showConfirmDialog == 0) {
                        bbUchetUslugActionPerformed(evt);
                    }                    
                }

            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
                System.err.println("Cannot connect to database server");
            } finally {
                if (conn != null) {
                    try {
                        taLog.append("Завершение запроса к БД после оформления заказа \n");
                        taLog.append("------------------------------------------------- \n");
                        System.out.println("n***** Let terminate the Connection *****");
                        conn.close();
                        System.out.println("Database connection terminated... ");
                    } catch (SQLException ex) {
                        System.out.println("Error in connection termination!");
                    }
                }
            }
        }
        bbZakaz.setEnabled(false);
        bbZakaz.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bbZakazActionPerformed

    private void chQRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chQRActionPerformed
        // TODO add your handling code here:
        if (chQR.isSelected()) {
            chNal.setSelected(false);
        } else {
            chNal.setSelected(true);
        }
    }//GEN-LAST:event_chQRActionPerformed

    private void chNalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chNalActionPerformed
        // TODO add your handling code here:
        if (chNal.isSelected()) {
            chQR.setSelected(false);
        } else {
            chQR.setSelected(true);
        }
    }//GEN-LAST:event_chNalActionPerformed

    private void tfKlientZakazSkidPrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientZakazSkidPrActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientZakazSkidPrActionPerformed

    private void tfSummaZakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfSummaZakActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfSummaZakActionPerformed

    private void chEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chEmailActionPerformed

    private void chOFDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chOFDActionPerformed
        // TODO add your handling code here:
        if (chOFD.isSelected()) {
        } else {
            chKKT.setSelected(false);
            chEmail.setSelected(false);
        }
    }//GEN-LAST:event_chOFDActionPerformed

    private void chKKTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chKKTActionPerformed
        // TODO add your handling code here:
        if (chKKT.isSelected()) {
            chOFD.setSelected(true);
        } else {
        }
    }//GEN-LAST:event_chKKTActionPerformed

    private void jfEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jfEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jfEmailActionPerformed

    private void tabTowarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tabTowarKeyPressed

    }//GEN-LAST:event_tabTowarKeyPressed

    private void tabTowarInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tabTowarInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabTowarInputMethodTextChanged

    private void tabTowarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabTowarMouseClicked

    }//GEN-LAST:event_tabTowarMouseClicked

    private void tabTowarMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabTowarMouseDragged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabTowarMouseDragged

    private void tabTowarHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_tabTowarHierarchyChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabTowarHierarchyChanged

    private void tfKlientPriseSCodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientPriseSCodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientPriseSCodActionPerformed

    private void cbSkidkaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSkidkaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbSkidkaActionPerformed

    private void tfKlientPriseFIOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientPriseFIOActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientPriseFIOActionPerformed

    private void tfKlientPriseLCodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientPriseLCodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientPriseLCodActionPerformed

    private void bbRaschetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbRaschetActionPerformed
        // TODO add your handling code here:
        String sProc = (String) cbSkidka.getSelectedItem();
        int SkidkaPr = Integer.parseInt(sProc);
        //int SummaSkid=Integer.parseInt(tfSummaZak.getText())/100*SkidkaPr;

        Integer kolTow = tabTowar.getRowCount();

        int SummaZak = 0;
        for (int i = 0; i < kolTow; i++) {
            String sCena = tabTowar.getValueAt(i, 3).toString();
            String sKol = tabTowar.getValueAt(i, 5).toString();
            //jTextArea1.append("Цена: " + sCena + "\n");
            if (!"".equals(sCena)) {
                if (!"".equals(sKol)) {
                    if (!"0".equals(sKol)) {
                        //int Cena = Integer.parseInt(sCena) / 100 * (100 - SkidkaPr);
                        int Skidka = Integer.parseInt(sCena)*SkidkaPr/100;
                        int Cena = Integer.parseInt(sCena) - Skidka;
                        int Kol = Integer.parseInt(sKol);
                        SummaZak = SummaZak + Cena * Kol;
                    }
                }
            }
        }
        tfWibZakSumma.setText(Integer.toString(SummaZak));
    }//GEN-LAST:event_bbRaschetActionPerformed

    private void tfWibZakSummaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfWibZakSummaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfWibZakSummaActionPerformed

    private void bbObslActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbObslActionPerformed
        // Обслуживание заказа
        if ("".equals(tfKlientPriseLCod.getText())) {
            JFrame jfInfo = new JFrame();
            JOptionPane.showMessageDialog(jfInfo, "Необходимо обязательно выбрать Клиента");
        } else {

            chNal.setSelected(true);
            chQR.setSelected(false);

            int LCod = Integer.parseInt(tfKlientPriseLCod.getText());

            if ((LCod == 1000) | (LCod == 2000)) {
                chNal.setSelected(true);
                chQR.setSelected(false);
            }
            if ((LCod == 1001) | (LCod == 2001)) {
                chNal.setSelected(false);
                chQR.setSelected(true);
            }
            
            if ("Открыта".equals(sSmena)) {
                chOFD.setSelected(true);
                chKKT.setSelected(true);                
            }else{
                chOFD.setSelected(false);
                chKKT.setSelected(false);                                
            }

            String sProc = (String) cbSkidka.getSelectedItem();
            int SkidkaPr = Integer.parseInt(sProc);

            tfKlientZakazLCod.setText(tfKlientPriseLCod.getText());
            tfKlientZakazSCod.setText(tfKlientPriseSCod.getText());
            tfKlientZakazFIO.setText(tfKlientPriseFIO.getText());

            DefaultTableModel dtm2 = (DefaultTableModel) tabZakaz.getModel();
            dtm2.getDataVector().removeAllElements(); // удаляем все строки

            int SummaZak = 0;
            int SummaTow;
            int ik = 0;
            kolTowZak = 0;

            for (int i = 0; i < tabTowar.getRowCount(); i++) {
                String sCena = tabTowar.getValueAt(i, 3).toString();
                String sKol = tabTowar.getValueAt(i, 5).toString();

                //jTextArea1.append("Цена: " + sCena + "\n");
                if (!"".equals(sCena)) {
                    if (!"".equals(sKol)) {
                        if (!"0".equals(sKol)) {
                            ik++;
                            String sKolUslug = tabTowar.getValueAt(i, 6).toString();
                            kolTowZak = ik;
                            //int Cena = Integer.parseInt(sCena);
                            int Skidka = Integer.parseInt(sCena)*SkidkaPr/100;
                            int Cena = Integer.parseInt(sCena) - Skidka;
                            
                            int Kol = Integer.parseInt(sKol);
                            int kolUslug = Integer.parseInt(sKolUslug);
                            SummaTow = Cena * Kol;
                            SummaZak = SummaZak + SummaTow;
                            kolUslug = kolUslug * Kol;
                            String TowarArtikul = tabTowar.getValueAt(i, 1).toString();
                            String TowarName = tabTowar.getValueAt(i, 2).toString();

                            dtm2.addRow(new Object[]{ik, TowarArtikul, TowarName, Cena, Kol, SummaTow, kolUslug});

                        }
                    }
                }
            }
            if (kolTowZak > 0) {
                tfKlientZakazSkidPr.setText(sProc);
                tfSummaZak.setText(Integer.toString(SummaZak));        // TODO add your handling code here:
                tabPanMain.setSelectedIndex(4);
                bbZakaz.setEnabled(true);
            } else {
                JFrame jfInfo = new JFrame();
                JOptionPane.showMessageDialog(jfInfo, "Не выбран товар для оформления заказа!");
            }
        }
    }//GEN-LAST:event_bbObslActionPerformed

    private void bbZagruzActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbZagruzActionPerformed
        // Загрузка Прайс-Листа для формирования заказа
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        bbZagruz.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        cbZakazGroup.setMaximumRowCount(16);
        cbZakazGroup.removeAllItems();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
            System.out.println("Database Connection Established...");
            taLog.append("Соединение с БД для prise прошло успешно ...\n");

            DefaultTableModel dtm = (DefaultTableModel) tabTowar.getModel();
            //dtm.removeRow(0); //  Удаляем первую  строчку
            dtm.getDataVector().removeAllElements(); // удаляем все строки

            stmt = conn.createStatement();

            String query = "select artikul,id_group,name,cenaklient,ostat,tipgroup,kol_uslug from myservis.prise order by id_group,sort;";
            rs = stmt.executeQuery(query);

            int i = 0, g = 0, tot = 0;

            while (rs.next()) {
                tot++;
                int tipGroup = rs.getInt("tipgroup");
                int towarArtikul = rs.getInt("artikul");
                //int TowarId_group=rs.getInt("id_group");
                String towarName = rs.getString("name");
                int towarCena = rs.getInt("cenaklient");
                int towarOstat = rs.getInt("ostat");
                int kolUslug = rs.getInt("kol_uslug");

                if (tipGroup == 1) {
                    dtm.addRow(new Object[]{"", "", towarName, "", "", "", ""});
                    cbZakazGroup.addItem(towarName);
                    masPNom[g] = tot;
                    g++;
                } else {
                    i++;
                    dtm.addRow(new Object[]{i, towarArtikul, towarName, towarCena, towarOstat, "", kolUslug});
                }
            }
            rs.close();
            stmt.close();

            taLog.append("Запрос к prise - выполнен успешно \n");

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД prise \n");
                    taLog.append("------------------------------------------------- \n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        setCursor(Cursor.getDefaultCursor());
        bbZagruz.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bbZagruzActionPerformed

    private void panNewKlientAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_panNewKlientAncestorAdded
        taPrimNew.setWrapStyleWord(true);        // TODO add your handling code here:
        taPrimNew.setLineWrap(true);
    }//GEN-LAST:event_panNewKlientAncestorAdded

    private void bbNewKlientRegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbNewKlientRegActionPerformed
        // Регистрация нового покупателя
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);

            taLog.append("Запуск соединения с БД client для insert ... \n");

            String sFam = "\"" + tfFamNew.getText() + "\"";
            String sNam = "\"" + tfNamNew.getText() + "\"";
            String sOth = "\"" + tfOthNew.getText() + "\"";
            String sGorod = "\"" + tfGorodNew.getText() + "\"";
            String sIndex = "\"" + tfIndexNew.getText() + "\"";
            String sTel = "\"" + tfTelNew.getText() + "\"";
            String sEmail = "\"" + tfEmailNew.getText() + "\"";
            String sAdres = "\"" + tfAdresNew.getText() + "\"";
            String sPrim = "\"" + taPrimNew.getText() + "\"";
                        
            String ss,sGod,sMon,sDay;
            int kDlin;          
            
            ss=tfGod.getText();                            
            if (ss==null) kDlin=0; else kDlin = ss.length();
            if (kDlin==4) sGod=ss;else sGod="1910";
            
            ss=tfMon.getText();                            
            if (ss==null) kDlin=0; else kDlin = ss.length();
            if (kDlin==2) sMon=ss;else sMon="01";
            
            ss=tfDay.getText();                            
            if (ss==null) kDlin=0; else kDlin = ss.length();
            if (kDlin==2) sDay=ss;else sDay="01";
            
            String sDateRog = "\"" +sGod+"-"+sMon+"-"+sDay+ "\"";

            String sDateReg = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());
            sDateReg = "\"" + sDateReg + "\"";
            lbMainDateTime.setText(sDateReg);

            stmt = conn.createStatement();
            String query = "insert ignore into myservis.client (lcod,scod,f,i,o,gorod_name,poch_index,tel,email,adres,date_reg,prim,date_rog) values (" 
            + tfLCodNew.getText() + "," + tfSCodNew.getText() + "," + sFam + "," + sNam + "," + sOth + "," + sGorod + "," + sIndex + "," 
            + sTel + "," + sEmail + "," + sAdres + "," + sDateReg+ "," + sPrim+ "," + sDateRog + ");";
            stmt.executeUpdate(query);
            stmt.close();

            taLog.append("Запрос insert для client - выполнен успешно \n");

            JFrame jfInfo = new JFrame();
            JOptionPane.showMessageDialog(jfInfo, "Новый покупатель зарегистрирован успешно!");

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД для inser for client \n");
                    taLog.append("-------------------------------------------------\n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
    }//GEN-LAST:event_bbNewKlientRegActionPerformed

    private void tfIndexNewKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfIndexNewKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfIndexNewKeyTyped

    private void tfIndexNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfIndexNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfIndexNewActionPerformed

    private void tfGorodNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfGorodNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfGorodNewActionPerformed

    private void tfAdresNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfAdresNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfAdresNewActionPerformed

    private void tfEmailNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfEmailNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfEmailNewActionPerformed

    private void tfTelNewKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfTelNewKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfTelNewKeyTyped

    private void tfTelNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfTelNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfTelNewActionPerformed

    private void tfLCodNewKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfLCodNewKeyTyped
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfLCodNewKeyTyped

    private void tfLCodNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfLCodNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfLCodNewActionPerformed

    private void tfSCodNewKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfSCodNewKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfSCodNewKeyTyped

    private void tfSCodNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfSCodNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfSCodNewActionPerformed

    private void tfFamNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfFamNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfFamNewActionPerformed

    private void tfNamNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfNamNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfNamNewActionPerformed

    private void tfOthNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfOthNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfOthNewActionPerformed

    private void panKlientAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_panKlientAncestorAdded

    }//GEN-LAST:event_panKlientAncestorAdded

    private void bbKlientSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKlientSaveActionPerformed
        // TODO add your handling code here:
        // Сохранение данных после редактирвоания покупателя
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);

            taLog.append("Запуск соединения с БД для update client ... \n");

            stmt = conn.createStatement();

            String sFam = "\"" + tfKlientFam.getText() + "\"";
            String sNam = "\"" + tfKlientName.getText() + "\"";
            String sOth = "\"" + tfKlientOtch.getText() + "\"";
            String sGorod = "\"" + tfKlientGorod.getText() + "\"";
            String sIndex = "\"" + tfKlientIndex.getText() + "\"";
            String sTel = "\"" + tfKlientTel.getText() + "\"";
            String sEmail = "\"" + tfKlientEmail.getText() + "\"";
            String sAdres = "\"" + tfKlientAdres.getText() + "\"";
            String sPrim = "\"" + taKlientPrim.getText() + "\"";
            
            String ss,sGod,sMon,sDay;
            int kDlin;            
            ss=tfKlientGod.getText();                            
            if (ss==null) kDlin=0; else kDlin = ss.length();
            if (kDlin==4) sGod=ss;else sGod="1910";
            
            ss=tfKlientMon.getText();                            
            if (ss==null) kDlin=0; else kDlin = ss.length();
            if (kDlin==2) sMon=ss;else sMon="01";
            
            ss=tfKlientDay.getText();                            
            if (ss==null) kDlin=0; else kDlin = ss.length();
            if (kDlin==2) sDay=ss;else sDay="01";
            
            String sDateRog = "\"" +sGod+"-"+sMon+"-"+sDay+ "\"";            
            
            String query2 = "update myservis.client set scod=" + tfKlientSCod.getText()
                    + ",f=" + sFam + ",i=" + sNam + ",o=" + sOth + ",gorod_name=" + sGorod + ",poch_index=" + sIndex + ",tel=" + sTel 
                    + ",email=" + sEmail + ",adres=" + sAdres+ ",prim=" + sPrim+ ",date_rog=" + sDateRog
                    + "where lcod=" + tfKlientLCod.getText();
            stmt.executeUpdate(query2);
            stmt.close();

            taLog.append("Запрос update для client - выполнен успешно \n");

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД update для client \n");
                    taLog.append("------------------------------------------------- \n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }

        tfKlientLCod.setEditable(false);
        tfKlientSCod.setEditable(false);
        tfKlientFam.setEditable(false);
        tfKlientName.setEditable(false);
        tfKlientOtch.setEditable(false);
        tfKlientGorod.setEditable(false);
        tfKlientIndex.setEditable(false);
        tfKlientAdres.setEditable(false);
        tfKlientTel.setEditable(false);
        tfKlientEmail.setEditable(false);
        taKlientPrim.setEditable(false);

        setCursor(Cursor.getDefaultCursor());
        //jbLCod.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JFrame jfInfo = new JFrame();
        JOptionPane.showMessageDialog(jfInfo, "Сохранение произведено успешно!");
    }//GEN-LAST:event_bbKlientSaveActionPerformed

    private void bbKlientRedaktActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKlientRedaktActionPerformed
        // TODO add your handling code here:
        JFrame jfInfo = new JFrame();
        JOptionPane.showMessageDialog(jfInfo, "Включен режим редактирования Клиента!");
        tfKlientSCod.setEditable(true);
        tfKlientFam.setEditable(true);
        tfKlientName.setEditable(true);
        tfKlientOtch.setEditable(true);
        tfKlientGorod.setEditable(true);
        tfKlientIndex.setEditable(true);
        tfKlientAdres.setEditable(true);
        tfKlientTel.setEditable(true);
        tfKlientEmail.setEditable(true);
        taKlientPrim.setEditable(true);
        
        tfKlientGod.setEditable(true);
        tfKlientMon.setEditable(true);
        tfKlientDay.setEditable(true);
        
        bbKlientSave.setEnabled(true);
    }//GEN-LAST:event_bbKlientRedaktActionPerformed

    private void tabFIOMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabFIOMouseClicked
        // TODO add your handling code here:
        if (tabFIO.getSelectedRow() >= 0) {
            String sLCod = tabFIO.getValueAt(tabFIO.getSelectedRow(), 4).toString();
            int lCod = Integer.parseInt(sLCod);
            if ((lCod > 0) && (lCod < 1000000)) {
                tfKlientFind.setText(sLCod);
                //bbFindLCodActionPerformed(evt);                
            }
        }
    }//GEN-LAST:event_tabFIOMouseClicked

    private void jtFindFIOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtFindFIOActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtFindFIOActionPerformed

    private void jtFindFIOFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jtFindFIOFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jtFindFIOFocusGained

    private void bbFindFIOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbFindFIOActionPerformed
        // Поиск покупателя по ФИО
        int kDlin = jtFindFIO.getText().length();
        
        if ((kDlin > 1) | chKlientTot.isSelected()) {
            bbFindFIO.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            DefaultTableModel dtm = (DefaultTableModel) tabFIO.getModel();
            dtm.removeRow(0); //  Удаляем первую  строчку
            dtm.getDataVector().removeAllElements(); // удаляем все строки

            conn = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

                conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
                System.out.println("Database Connection Established...");
                taLog.append("Соединение прошло с БД для client успешно ... \n");

                String sFam = jtFindFIO.getText();

                taLog.append("Запуск запроса к БД client ... \n");
                stmt = conn.createStatement();
                //String query = "select lcod,scod,f,i,o,tel,email from myservis.client where LOWER(f)=\"" + sFam + "\"";
                String query;
                if (chKlientTot.isSelected()) { query = "select lcod,scod,f,i,o,tel,gorod_name from myservis.client order by f,i,i";}
                else {query = "select lcod,scod,f,i,o,tel,gorod_name from myservis.client where LOWER(f) LIKE \"" + "%" + sFam + "%" + "\"";}
                //LIKE '%Sales%'
                rs = stmt.executeQuery(query);

                int i = 0;
                while (rs.next()) {
                    i++;
                    String LCod = rs.getString("lcod");
                    String fam = rs.getString("f");
                    String name = rs.getString("i");
                    String otch = rs.getString("o");
                    String sTel = rs.getString("tel");
                    String sGorod = rs.getString("gorod_name");

                    dtm.addRow(new Object[]{fam, name, otch, sTel, LCod, sGorod});
                }
                rs.close();
                stmt.close();

            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
                System.err.println("Cannot connect to database server");
            } finally {
                if (conn != null) {
                    try {
                        taLog.append("Завершение запроса к БД cleent ... \n");
                        taLog.append("------------------------------------------------- \n");
                        System.out.println("n***** Let terminate the Connection *****");
                        conn.close();
                        System.out.println("Database connection terminated... ");
                    } catch (SQLException ex) {
                        System.out.println("Error in connection termination!");
                    }
                }
            }
            bbKlientCSV.setEnabled(true);
            bbFindFIO.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            JOptionPane.showMessageDialog(new JFrame(), "Фамилия не может быть меньше 2 символов!");
        }
    }//GEN-LAST:event_bbFindFIOActionPerformed

    private void bbFindLCodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbFindLCodActionPerformed
        // поиск покупателя по номеру
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        bbFindLCod.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        //String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        //lbInfoMain.setText(sDateTimeTek);

        conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
            taLog.append("Соединение с БД для client успешно ... \n");

            tfKlientLCod.setText("");
            tfKlientSCod.setText("");
            tfKlientFam.setText("");
            tfKlientName.setText("");
            tfKlientOtch.setText("");
            tfKlientTel.setText("");
            tfKlientEmail.setText("");
            tfKlientGorod.setText("");
            tfKlientIndex.setText("");
            tfKlientAdres.setText("");
            taKlientPrim.setText("");
            tfKlientGod.setText("");
            tfKlientMon.setText("");
            tfKlientDay.setText("");
            
            String sLcod = tfKlientFind.getText();

            taLog.append("Запуск запроса к БД для client ... \n");
            stmt = conn.createStatement();
            String query = "select lcod,scod,f,i,o,tel,email,gorod_name,poch_index,adres,prim,date_rog from myservis.client where lcod=" + sLcod;
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                String sLCod = rs.getString("lcod");
                String sSCod = rs.getString("scod");
                String fam = rs.getString("f");
                String name = rs.getString("i");
                String otch = rs.getString("o");
                String sTel = rs.getString("tel");
                String sEmail = rs.getString("email");
                String sGorod = rs.getString("gorod_name");
                String sIndex = rs.getString("poch_index");
                String sAdres = rs.getString("adres");
                String sPrim = rs.getString("prim");
                String sDateRog = rs.getString("date_rog");
                
                String sGod;
                String sMon;
                String sDay;
                int kDlin;
                
                if (sDateRog==null){
                    kDlin=0;
                }else{    
                    kDlin = sDateRog.length();
                }
                
                if (kDlin==10){
                    sGod = sDateRog.substring(0,4);
                    sMon = sDateRog.substring(5,7);
                    sDay = sDateRog.substring(8,10);
                }else{
                    sGod="";
                    sMon="";
                    sDay="";
                }

                tfKlientLCod.setText(sLCod);
                tfKlientSCod.setText(sSCod);
                tfKlientFam.setText(fam);
                tfKlientName.setText(name);
                tfKlientOtch.setText(otch);
                tfKlientTel.setText(sTel);
                tfKlientEmail.setText(sEmail);
                tfKlientGorod.setText(sGorod);
                tfKlientIndex.setText(sIndex);
                tfKlientAdres.setText(sAdres);
                tfKlientGod.setText(sGod);
                tfKlientMon.setText(sMon);
                tfKlientDay.setText(sDay);
                
                taKlientPrim.setText(sPrim);
                taKlientPrim.setLineWrap(true);
                taKlientPrim.setWrapStyleWord(true);

                tfUslugaLCod.setText(sLCod);
                tfUslugaFIO.setText(fam + name + otch);
            }
            rs.close();
            stmt.close();

            if ("".equals(tfKlientLCod.getText())) {
                JFrame jfInfo = new JFrame();
                JOptionPane.showMessageDialog(jfInfo, "Клиент не найден!");
                bbKlientZakaz.setEnabled(false);
                bbKlientRedakt.setEnabled(false);
                bbUchetUslug.setEnabled(false);
            } else {
                bbKlientZakaz.setEnabled(true);
                bbKlientRedakt.setEnabled(true);
                bbUchetUslug.setEnabled(true);
                tfBDZakazLcod.setText(tfKlientLCod.getText());
            }

            bbKlientSave.setEnabled(false);

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server для client");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД для client\n");
                    taLog.append("-------------------------------------------------\n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated для client... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination для client!");
                }
            }
        }
        setCursor(Cursor.getDefaultCursor());
        bbFindLCod.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bbFindLCodActionPerformed

    private void tfKlientFindKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfKlientFindKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfKlientFindKeyTyped

    private void tfKlientFindKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfKlientFindKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientFindKeyPressed

    private void tfKlientFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientFindActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientFindActionPerformed

    private void tfKlientFindCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_tfKlientFindCaretUpdate
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientFindCaretUpdate

    private void tfKlientLCodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientLCodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientLCodActionPerformed

    private void tfKlientSCodKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfKlientSCodKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfKlientSCodKeyTyped

    private void tfKlientSCodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientSCodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientSCodActionPerformed

    private void tfKlientAdresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientAdresActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientAdresActionPerformed

    private void tfKlientIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientIndexActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientIndexActionPerformed

    private void tfKlientGorodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientGorodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientGorodActionPerformed

    private void bbKlientZakazActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKlientZakazActionPerformed
        int showConfirmDialog;
        if ("Открыта".equals(sSmena)) {
            showConfirmDialog =0;
        } else {
            showConfirmDialog = JOptionPane.showConfirmDialog(new JFrame(),
                "На Кассовом аппарате Смена не открыта!\nПродолжить с Закрытой сменой?",
                "Проверка Смены на Кассовом аппарате.",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        }
        
        if (showConfirmDialog == 0) {
            String sLCod = tfKlientLCod.getText();

            if ("".equals(sLCod)) {
                JFrame jfInfo = new JFrame();
                JOptionPane.showMessageDialog(jfInfo, "Необходимо обязательно выбрать клиента");
            } else {
                tfKlientPriseLCod.setText(tfKlientLCod.getText());
                tfKlientPriseSCod.setText(tfKlientSCod.getText());
                tfKlientPriseFIO.setText(tfKlientFam.getText() + ' ' + tfKlientName.getText() + ' ' + tfKlientOtch.getText());
                tfWibZakSumma.setText("");
                bbZagruzActionPerformed(evt);

                tabPanMain.setSelectedIndex(3);
            }
            cbSkidka.setSelectedIndex(0);
        }
    }//GEN-LAST:event_bbKlientZakazActionPerformed

    private void tfKlientFamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientFamActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientFamActionPerformed

    private void tfKlientEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientEmailActionPerformed

    private void tfKlientNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientNameActionPerformed

    private void tfKlientOtchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientOtchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientOtchActionPerformed

    private void tfKlientTelKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfKlientTelKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfKlientTelKeyTyped

    private void tfKlientTelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientTelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientTelActionPerformed

    private void bbBDprihodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbBDprihodActionPerformed
        // TODO add your handling code here:
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        bbBDprihod.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
            taLog.append("Соединение с БД для prihod прошло успешно ...\n");
            stmt = conn.createStatement();

            int SumZak = 0;
            DefaultTableModel dtm = (DefaultTableModel) tabBDprihod.getModel();

            //dtm.removeRow(0); //  Удаляем первую  строчку
            dtm.getDataVector().removeAllElements(); // удаляем все строки

            String sGod = tfBDZakazGod.getText();
            String sDat = tfBDZakazDat.getText();
            String sMon = Integer.toString(cbBDzakazMon.getSelectedIndex() + 1);

            String sDate1 = sGod + "-01-01";
            String sDate2 = sGod + "-12-31";

            if (cbBDzakazMon.getSelectedIndex() > -1) {
                sDate1 = sGod + "-" + sMon + "-01";
                sDate2 = sGod + "-" + sMon + "-31";
            }

            if (!"".equals(sDat)) {
                sDate1 = sGod + "-" + sMon + "-" + sDat;
                int mDat2 = Integer.parseInt(sDat);
                String sDat2 = Integer.toString(mDat2 + 1);
                sDate2 = sGod + "-" + sMon + "-" + sDat2;

                taLog.append("(Дата) ...\n");
                taLog.append("sDate1:" + sDate1 + " ...\n");
                taLog.append("sDate2:" + sDate2 + " ...\n");
            }

            String query = "select date_zak,id_zak,lcod,fio,sum_zak,isp,prim from myservis.prihod where date_zak BETWEEN \"" + sDate1 + "\" AND \"" + sDate2 + "\" order by date_zak";
            rs = stmt.executeQuery(query);

            int i = 0;

            while (rs.next()) {
                i = i + 1;
                int id_zak = rs.getInt("id_zak");
                int lcod = rs.getInt("lcod");
                String date_zak = rs.getString("date_zak");
                String fio = rs.getString("fio");
                int sum_zak = rs.getInt("sum_zak");
                String isp = rs.getString("isp");
                String prim = rs.getString("prim");

                dtm.addRow(new Object[]{i, date_zak, id_zak, lcod, fio, sum_zak, isp, prim});
                SumZak = SumZak + sum_zak;
            }

            rs.close();
            stmt.close();

            String sZak = Integer.toString(SumZak);
            tfBDprihodSumma.setText(sZak);
            taLog.append("Запрос к prihod - выполнен успешно \n");
            if (i == 0) {
                dtm.getDataVector().removeAllElements(); // удаляем все строки
                dtm.addRow(new Object[]{"", "", "", "", "", "", "", "", "", "", ""});
                JFrame jfInfo = new JFrame();
                JOptionPane.showMessageDialog(jfInfo, "Приходов не обнаружено!");
                bbBDprihodCSV.setEnabled(false);
            } else {
                bbBDprihodCSV.setEnabled(true);
            }

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД prihod \n");
                    taLog.append("-------------------------------------------------\n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        setCursor(Cursor.getDefaultCursor());
        bbBDprihod.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bbBDprihodActionPerformed

    private void tfBDprihodSummaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfBDprihodSummaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfBDprihodSummaActionPerformed

    private void tabBDprihodHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_tabBDprihodHierarchyChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabBDprihodHierarchyChanged

    private void tabBDprihodMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabBDprihodMouseDragged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabBDprihodMouseDragged

    private void tabBDprihodMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabBDprihodMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tabBDprihodMouseClicked

    private void tabBDprihodInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_tabBDprihodInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabBDprihodInputMethodTextChanged

    private void tabBDprihodKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tabBDprihodKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tabBDprihodKeyPressed

    private void bbBDzakazCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbBDzakazCSVActionPerformed
        String nameFiles = "temp\\Заказы-Csv.csv";
        String fText = "№ пп;Дата;№ заказа;Личный Id;ФИО;Сумма;Скидка-%;QR/Нал;Чек;Исполнитель;\n";

        for (int i = 0; i < tabBDzakaz.getRowCount(); i++) {
            //fText = tabBDzakaz.getValueAt(i, 0).toString()+";";
            //fText = fText + tabBDzakaz.getValueAt(i, 1).toString()+";";
            //fText = fText + tabBDzakaz.getValueAt(i, 2).toString()+";";            
            for (int k = 0; k < 10; k++) {
                fText +=tabBDzakaz.getValueAt(i, k).toString() + ";";
            }
            fText +="\n";
        }
        fText += ";;;;Сумма всего;" + tfBDZakazSumma.getText() + ";\n";
        fText += ";;;;Наличными;" + tfBDZakazNal.getText() + ";\n";
        fText += ";;;;QR-код;" + tfBDZakazQR.getText() + ";\n";

        try {
            File file = new File(nameFiles);
            file.delete();
            file.createNewFile();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(fText);
                writer.close();

            } catch (IOException e) {
                System.out.println("Ошибка-1 при записи в файл");
                taLog.append("Ошибка-1 при записи в файл \n");
            }

            System.out.println("Текст успешно записан в файл.");
        } catch (IOException e) {
            System.out.println("Ошибка-2 при записи в файл");
            taLog.append("Ошибка-2 при записи в файл \n");
        }

        JOptionPane.showMessageDialog(new JFrame(), "Файл:\n\n" + progDir + nameFiles + "\n\nCоздан успешно!");
    }//GEN-LAST:event_bbBDzakazCSVActionPerformed

    private void bbBDprihodCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbBDprihodCSVActionPerformed
        // TODO add your handling code here:
        String nameFiles = "temp\\Prihod.csv";
        String fText = "№ пп;Дата;№ прихода;№ поставщика;Поставщик;Сумма;Исполнитель;\n";

        for (int i = 0; i < tabBDprihod.getRowCount(); i++) {
            for (int k = 0; k < 7; k++) {
                fText +=tabBDprihod.getValueAt(i, k).toString() + ";";
            }
            fText +="\n";
        }

        fText += ";;;;;" + tfBDprihodSumma.getText() + ";\n";

        try {
            File file = new File(nameFiles);

            file.delete();
            file.createNewFile();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(fText);
                writer.close();

            } catch (IOException e) {
                System.out.println("Ошибка-1 при записи в файл");
                taLog.append("Ошибка-1 при записи в файл \n");
            }

            System.out.println("Текст успешно записан в файл.");
        } catch (IOException e) {
            System.out.println("Ошибка-2 при записи в файл");
            taLog.append("Ошибка-2 при записи в файл \n");
        }

        JOptionPane.showMessageDialog(new JFrame(), "Файл:\n\n" + progDir + nameFiles + "\n\nCоздан успешно!");
    }//GEN-LAST:event_bbBDprihodCSVActionPerformed

    private void panBDAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_panBDAncestorAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_panBDAncestorAdded

    private void panFormZakazAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_panFormZakazAncestorAdded
        // TODO add your handling code here:
        cbSkidka.setMaximumRowCount(16);
    }//GEN-LAST:event_panFormZakazAncestorAdded

    private void cbLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbLoginActionPerformed
        int wib = cbLogin.getSelectedIndex();
    }//GEN-LAST:event_cbLoginActionPerformed

    private void bbLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbLoginActionPerformed
        int wib = cbLogin.getSelectedIndex();

        String sPas = tfpPas.getText();
        String sPasBaz = masPas[wib];
        //jTextArea1.append("sPasBaz:"+sPasBaz+"\n");

        if (sPas.equals(sPasBaz)) {
            aktPolName = masNameIsp[wib];
            aktPolKKT = masNameKKT[wib];
            aktPolLogin = cbLogin.getItemAt(wib);
            lbNameKKT.setText("Кассир ККТ: " + aktPolKKT);
            lbIsp.setText("Исполнитель документов: " + aktPolLogin);
            tfIsp.setText(aktPolName);

            bbFindLCod.setEnabled(true);
            bbFindFIO.setEnabled(true);
            bbZagruz.setEnabled(true);
            bbPriseZagruz.setEnabled(true);
            bbBDzakaz.setEnabled(true);
            bbBDprihod.setEnabled(true);
            bbKassaOtchet.setEnabled(true);
            bbUslugaOtchet.setEnabled(true);
            bbNewKlient.setEnabled(true);

            bbKKTopenSmenaTot.setEnabled(true);
            bbKKTcloseSmenaTot.setEnabled(true);
            bbKKTprovStatus.setEnabled(true);
            bbKKTprintText.setEnabled(true);

            String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
            lbMainDateTime.setText("Вход в программу: " + sDateTimeTek);

        } else {

            tfIsp.setText("");
            bbFindLCod.setEnabled(false);
            bbFindFIO.setEnabled(false);
            bbZagruz.setEnabled(false);
            bbPriseZagruz.setEnabled(false);
            bbBDzakaz.setEnabled(false);
            bbBDprihod.setEnabled(false);
            bbKassaOtchet.setEnabled(false);
            bbUslugaOtchet.setEnabled(false);
            bbNewKlient.setEnabled(false);

            bbKKTopenSmenaTot.setEnabled(false);
            bbKKTcloseSmenaTot.setEnabled(false);
            bbKKTprovStatus.setEnabled(false);
            bbKKTprintText.setEnabled(false);

            JFrame jfInfo = new JFrame();
            JOptionPane.showMessageDialog(jfInfo, "Пароль введен неверно!");
        }
    }//GEN-LAST:event_bbLoginActionPerformed

    private void panPolzovatelAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_panPolzovatelAncestorAdded
        //userNameBD = "root";
        //passwordBD = "admin";
        //urlHostBD = "jdbc:MySQL://localhost";
        
        sVersionProg = bbVerProg.getText();
        
        // Создаем файл с номером версии для чтения программой: myServisUpdateNew.jar 
        try {
            Path fileName = Paths.get("myVersionProgLocal.ver");
            Files.write(fileName, sVersionProg.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {e.printStackTrace();}  
                        
        masArtikul = new int[50];
        masSort = new int[50];
        masUsluga = new int[50];
        masPNom = new int[1000];
        sPostavchik = "Поставщик";
                
        httpKKM = "http://localhost:50010/api.json";        
        sKKMkassirINN = "00000000000000000000";

        sTire70 = "";
        sTire180 = "";
        sTire99 = "";

        for (int i = 0; i < 180; i++) {
            if (i < 70) sTire70 += "-";
            if (i < 120) sTire99 += "*";
            if (i < 180) sTire180 += "-";
        }
        
        sTire70 += "\n";
        sTire180 += "\n";
        sTire99 += "\n";
        
        taLog.setText(null);
        taLog.append(sTire99);
        String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        taLog.append("Дата: " + sDateTimeTek + "    Настройки программы:\n");
        taLog.append(sTire99);
        
        lbMainDateTime.setText("");
        progDir = System.getProperty("user.dir");
        progDir = progDir + "\\";
        sKKMmercComPort = "COM4";
        sKKMmercModel = "185F";
        
        userNameBD = "root";
        passwordBD = "admin";
        urlHostBD = "jdbc:MySQL://localhost";            
        String redHostBD = "";
        
        // Читаем файл с настройками
        try {            
            try (BufferedReader reader = new BufferedReader(new FileReader("myServisProg.ini"))) {
                String line = reader.readLine();
                int ik = 0;
                while (line != null) {
                    ik++;
                    if (ik == 1) sKKMmercComPort = line;
                    if (ik == 2) sKKMmercModel = line;
                    if (ik == 3) sDumpDirLocal = line;
                    if (ik == 4) sDumpDirHost = line;
                    if (ik == 5) sDumpFileExe = line;
                    if (ik == 6) sWixod = line;
                    if (ik == 7) redHostBD = line;
                    if (ik == 8) sDirNewVersion = line;
                    line = reader.readLine();
                }
                urlHostBD = "jdbc:MySQL://"+redHostBD;
            }
            
        } catch (IOException e) {
        }
        taLog.append("COM порт: "+sKKMmercComPort+"\n");
        taLog.append("Тип ККТ: "+sKKMmercModel+"\n");
        taLog.append("Папка для ежденевных дампов: "+sDumpDirLocal+"\n");
        taLog.append("Папка для копирования ежденевных дампов: "+sDumpDirHost+"\n");
        taLog.append("Папка с командой mysqldump (папка установки mySQL): "+sDumpFileExe+"\n");
        taLog.append("Запрашивать подтверждение выхода из программы: "+sWixod+"\n");
        taLog.append("Host БД : "+urlHostBD+"\n");
        taLog.append("Каталог для обновления новой версии : "+sDirNewVersion+"\n");
                    
        tfKKTComPort.setText(sKKMmercComPort);
        tfKKTmodel.setText(sKKMmercModel);
        tfDumpFile.setText(sDumpDirLocal);
        starTime1 = System.currentTimeMillis();
        
        cbLogin.removeAllItems();
        masPas = new String[50];
        masNameIsp = new String[50];
        masNameKKT = new String[50];
        masTowarKKT = new String[100];
        
        sVersionServer = "null";                
        try {            
            try (BufferedReader reader = new BufferedReader(new FileReader(sDirNewVersion+"myVersionProgServer.ver"))) {
                String line = reader.readLine();
                sVersionServer = line;
            }            
        } catch (IOException e) {}     
        
        taLog.append(sTire99);
        taLog.append("Версия программы Текущая : "+sVersionProg+"\n");
        taLog.append("Версия программы на Cервере : "+sVersionServer+"\n");
        taLog.append(sTire99);
        
        // Создаем файл на Сервере с логом загрузки настроек и с номером версии программы для контроля
        try {
            Path fileName = Paths.get(sDumpDirHost+"myServisProSetupVersion.ver");
            Files.write(fileName, taLog.getText().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            taLog.append(e.toString());
            taLog.append(sTire99);
        }
        
        // Копируем лог программы обновления на сервер, для контроля
        Path sourcePath = Paths.get("temp\\myServisUpdate_log.txt");
        Path destPath = Paths.get(sDumpDirHost+"myServisUpdate_log.txt");        
        try {
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {e.printStackTrace();}        
                        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
            taLog.append("Соединение с БД для polzovatel прошло успешно ...\n");

            stmt = conn.createStatement();

            String query = "select login,name,uroven,password,nameKKT from myservis.polzovatel";
            rs = stmt.executeQuery(query);
            int i = 0;
            while (rs.next()) {                
                String login = rs.getString("login");
                String name = rs.getString("name");
                String nameKKT = rs.getString("nameKKT");
                String password = rs.getString("password");

                cbLogin.addItem(login);
                masPas[i] = password;
                masNameIsp[i] = name;
                masNameKKT[i] = nameKKT;
                i = i + 1;
            }
            rs.close();
            stmt.close();
                        
            taLog.append("Запрос к polzovatel - выполнен успешно \n");
            

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД polzovatel \n");
                    taLog.append(sTire99);
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
    }//GEN-LAST:event_panPolzovatelAncestorAdded

    private void tfIspActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfIspActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfIspActionPerformed

    private void bbNewFrameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbNewFrameActionPerformed
        // TODO add your handling code here:
        JFrame jfInfo = new JFrame();
        jfInfo.setBounds(120, 80, 800, 480);

        //JOptionPane.showMessageDialog(jfInfo, "Успешно!");
        JPanel p = new JPanel();
        p.setBounds(0, 360, 750, 40);
        p.setBackground(Color.GRAY);
        JButton b = new JButton("Сохранить в csv");
        b.setBounds(0, 0, 80, 34);
        b.setFont(new java.awt.Font("Arial", 0, 18));

        JTable t = new JTable(4, 6);
        t.setBounds(10, 10, 780, 360);
        t.setRowHeight(30);
        t.setRowHeight(0, 30);
        t.setRowHeight(1, 30);
        t.setRowHeight(2, 30);
        t.setRowHeight(3, 30);
        //t.setIntercellSpacing(new Dimension(10, 10));
        t.setGridColor(Color.blue);
        t.setShowVerticalLines(true);
        t.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        t.setForeground(new java.awt.Color(0, 0, 153));
        t.setBackground(Color.LIGHT_GRAY);
        //t.setPreferredWidth(50);

        TableColumn column = null;
        for (int i = 0; i < 6; i++) {
            column = t.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(30);
            }
            if (i == 1) {
                column.setPreferredWidth(60);
            }
            if (i == 2) {
                column.setPreferredWidth(380);
            }
            if (i == 3) {
                column.setPreferredWidth(50);
            }
            if (i == 4) {
                column.setPreferredWidth(50);
            }
        }
        t.setShowGrid(true);

        p.add(b);
        //panLogDop.add(p);
        jfInfo.add(p);
        jfInfo.add(t);
        jfInfo.setVisible(true);
        p.setVisible(true);
        b.setVisible(true);
        t.setVisible(true);

        DefaultTableModel dtm = (DefaultTableModel) t.getModel();
        dtm.getDataVector().removeAllElements(); // удаляем все строки        

        dtm.addRow(new Object[]{"№пп", "Артикул", "Наименование", "Цена", "Кол-во", "Сумма"});
        dtm.addRow(new Object[]{"1", "3001", "Пудра из серии самых пушистых пдр в РФ", "1000", "2", "2000"});
        dtm.addRow(new Object[]{"2", "3002", "Пудра", "1500", "3", "4500"});
        dtm.addRow(new Object[]{"3", "3003", "Пудра", "2500", "3", "4500"});

    }//GEN-LAST:event_bbNewFrameActionPerformed

    private void imOpenZakazActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imOpenZakazActionPerformed

        IdZak = Integer.parseInt(tabBDzakaz.getValueAt(tabBDzakaz.getSelectedRow(), 2).toString());
        //new FrameZakazItem().setVisible(true);
        taLog.append("Id_zak:" + IdZak + "\n");
        new FrameZak().setVisible(true);
    }//GEN-LAST:event_imOpenZakazActionPerformed

    private void imOpenPrihodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imOpenPrihodActionPerformed

        IdZak = Integer.parseInt(tabBDprihod.getValueAt(tabBDprihod.getSelectedRow(), 2).toString());
        new FramePrihodItem().setVisible(true);

        taLog.append("Id_zak:" + IdZak + "\n");


    }//GEN-LAST:event_imOpenPrihodActionPerformed

    private void formComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_formComponentAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_formComponentAdded

    private void pznPriseAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_pznPriseAncestorAdded
        // TODO add your handling code here:
        cbPriseGroup.setMaximumRowCount(16);
    }//GEN-LAST:event_pznPriseAncestorAdded

    private void chUslugaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chUslugaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chUslugaActionPerformed

    private void tfPrihodID_prihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfPrihodID_prihActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfPrihodID_prihActionPerformed

    private void tfTowarNewNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfTowarNewNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfTowarNewNameActionPerformed

    private void tfTowarNewArtikulActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfTowarNewArtikulActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfTowarNewArtikulActionPerformed

    private void cbPriseGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbPriseGroupActionPerformed
        // TODO add your handling code here:

        int wib = cbPriseGroup.getSelectedIndex();
        //cbPriseArt.setSelectedIndex(0);

        if (wib > -1) {
            tfTowarNewArtikul.setText(Integer.toString(masArtikul[wib]));
            tfTowarNewSort.setText(Integer.toString(masSort[wib]));

            if (masUsluga[wib] == 1) {
                chUsluga.setSelected(true);
            } else {
                chUsluga.setSelected(false);
            }
        }

    }//GEN-LAST:event_cbPriseGroupActionPerformed

    private void chPriseRedaktActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chPriseRedaktActionPerformed
        // TODO add your handling code here:
        if (chPriseRedakt.isSelected()) {
            bbPriseSave.setEnabled(true);
        } else {
            bbPriseSave.setEnabled(false);
        }
    }//GEN-LAST:event_chPriseRedaktActionPerformed

    private void bbPrisePrihodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbPrisePrihodActionPerformed
        // TODO add your handling code here:
        // Приходование товара
        bbPrisePrihod.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        sDateTimeTek = "\"" + sDateTimeTek + "\"";
        sIspolnitel = "\"" + aktPolLogin + "\"";
        sPostavchik = "\"" + sPostavchik + "\"";
        String sPrim = "\"" + tfPrihodPrim.getText() + "\"";

        int SumZakaz = 0;

        for (int i = 0; i < tabPrise.getRowCount(); i++) {
            String sKol = tabPrise.getValueAt(i, 4).toString();
            if (!"".equals(sKol)) {
                int kol = Integer.parseInt(tabPrise.getValueAt(i, 4).toString());
                int cena = Integer.parseInt(tabPrise.getValueAt(i, 3).toString());
                SumZakaz = SumZakaz + kol * cena;
            }
        }

        if (SumZakaz <= 0) {
            JFrame jfInfo = new JFrame();
            JOptionPane.showMessageDialog(jfInfo, "Необходимо сформировать приход с суммой больше 0!");
        } else {

            try {
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

                conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
                taLog.append("Запуск соединения с БД для insert в prihod и prihod_item ... \n");

                stmt = conn.createStatement();

                String query = "select sc,id_prih from myservis.nastroi";
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    tfPrihodID_prih.setText(rs.getString("id_prih"));
                    //id_prih=rs.getInt("id_prih");
                    //tfKlientZakazSC.setText(rs.getString("sc"));
                }
                rs.close();
                taLog.append("Запрос select для nastroi - выполнен успешно \n");

                String query2 = "update myservis.nastroi set id_prih=id_prih+1";
                stmt.executeUpdate(query2);

                taLog.append("Запрос update для nastroi - выполнен успешно \n");

                //int kod_sc=Integer.parseInt(tfKlientZakazSC.getText());
                int id_prih = Integer.parseInt(tfPrihodID_prih.getText());
                int kod_sc = 1001;

                id_prih++;

                int tip_zak = 1; // Обычный приход

                String query3 = "insert ignore into myservis.prihod (sc,id_zak,nom_zak,sum_zak,tip_zak,date_zak,fio,isp,prim) values ("
                        + kod_sc + "," + id_prih + "," + id_prih + "," + SumZakaz + "," + tip_zak + "," + sDateTimeTek + "," + sPostavchik + "," + sIspolnitel + "," + sPrim
                        + ");";
                stmt.executeUpdate(query3);
                taLog.append("Запрос inset для prihod - выполнен успешно \n");

                for (int i = 0; i < tabPrise.getRowCount(); i++) {
                    String sKol = tabPrise.getValueAt(i, 4).toString();
                    taLog.append("Количество прихода: " + sKol + "\n");

                    if (!"".equals(sKol)) {
                        int artikul = Integer.parseInt(tabPrise.getValueAt(i, 1).toString());
                        taLog.append("артикул: " + artikul + "\n");
                        String sTowar = "\"" + tabPrise.getValueAt(i, 2).toString() + "\"";
                        taLog.append("товар: " + sTowar + "\n");
                        int cena = Integer.parseInt(tabPrise.getValueAt(i, 3).toString());
                        taLog.append("Цена: " + cena + "\n");
                        String query4 = "insert ignore into myservis.prihod_item (sc,id_zak,nom_zak,tip_zak,artikul,kol,cen,towar,date_zak) values ("
                                + kod_sc + "," + id_prih + "," + id_prih + "," + tip_zak + "," + artikul + "," + sKol + "," + cena + "," + sTowar + "," + sDateTimeTek
                                + ");";
                        stmt.executeUpdate(query4);
                    }
                }

                taLog.append("Запрос inset для prihod_item - выполнен успешно \n");

                for (int i = 0; i < tabPrise.getRowCount(); i++) {
                    String sKol = tabPrise.getValueAt(i, 4).toString();
                    if (!"".equals(sKol)) {
                        int artikul = Integer.parseInt(tabPrise.getValueAt(i, 1).toString());

                        String query5 = "update myservis.prise set ostat=ostat+" + sKol + ",prihod=prihod+" + sKol
                                + " where artikul=" + artikul;
                        stmt.executeUpdate(query5);
                    }
                }

                taLog.append("Запрос update для prise - выполнен успешно \n");

                stmt.close();

                JFrame jfInfo = new JFrame();
                JOptionPane.showMessageDialog(jfInfo, "Приход проведен успешно!");

            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
                System.err.println("Cannot connect to database server");
            } finally {
                if (conn != null) {
                    try {
                        taLog.append("Завершение запроса к БД после оформления прихода \n");
                        taLog.append("------------------------------------------------- \n");
                        System.out.println("n***** Let terminate the Connection *****");
                        conn.close();
                        System.out.println("Database connection terminated... ");
                    } catch (SQLException ex) {
                        System.out.println("Error in connection termination!");
                    }
                }
            }
        }
        setCursor(Cursor.getDefaultCursor());
        bbPrisePrihod.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bbPrisePrihodActionPerformed

    private void tfTowarNewSortKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfTowarNewSortKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfTowarNewSortKeyTyped

    private void tfTowarNewSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfTowarNewSortActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfTowarNewSortActionPerformed

    private void tfTowarNewCenaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfTowarNewCenaKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfTowarNewCenaKeyTyped

    private void tfTowarNewCenaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfTowarNewCenaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfTowarNewCenaActionPerformed

    private void bbPriseSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbPriseSaveActionPerformed
        // TODO add your handling code here:
        // Сохранение Прайс-Листа
        bbPriseSave.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
            taLog.append("Запуск соединения с БД для update prise ... \n");

            stmt = conn.createStatement();
            int rowIndex = tabPrise.getSelectedRow();  // Выделенный ряд

            int artikul = Integer.parseInt(tabPrise.getValueAt(rowIndex, 1).toString());
            String sTowarNew = "\"" + tabPrise.getValueAt(rowIndex, 2).toString() + "\"";
            int CenaNew = Integer.parseInt(tabPrise.getValueAt(rowIndex, 3).toString());
            int TowarSort = Integer.parseInt(tabPrise.getValueAt(rowIndex, 8).toString());

            String query = "update myservis.prise set cenaklient=" + CenaNew + ", name=" + sTowarNew + ", sort=" + TowarSort + " where artikul=" + artikul;
            stmt.executeUpdate(query);
            stmt.close();

            taLog.append("Запрос update для Prise - выполнен успешно \n");

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД update для Prise \n");
                    taLog.append("------------------------------------------------- \n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated ... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        setCursor(Cursor.getDefaultCursor());
        bbPriseSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JFrame jfInfo = new JFrame();
        JOptionPane.showMessageDialog(jfInfo, "Сохранение Прайс-Листа прошло успешно!");
    }//GEN-LAST:event_bbPriseSaveActionPerformed

    private void bbNewTowarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbNewTowarActionPerformed
        // Новый товар
        bbNewTowar.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
            taLog.append("Запуск соединения с БД prise для insert ...\n");

            String sTowarNew = "\"" + tfTowarNewName.getText() + "\"";
            int id_group = cbPriseGroup.getSelectedIndex() + 1;

            String sDateReg = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());
            sDateReg = "\"" + sDateReg + "\"";
            lbMainDateTime.setText(sDateReg);

            int tipUsluga;
            if (chUsluga.isSelected()) {
                tipUsluga = 1;
            } else {
                tipUsluga = 0;
            }

            stmt = conn.createStatement();
            String query = "insert ignore into myservis.prise (nomsc,artikul,tipgroup,id_group,name,cenaklient,sort,usluga) values (1001,"
                    + tfTowarNewArtikul.getText() + ",0," + id_group + "," + sTowarNew + "," + tfTowarNewCena.getText() + "," + tfTowarNewSort.getText() + "," + tipUsluga + ");";
            stmt.executeUpdate(query);
            stmt.close();

            taLog.append("Запрос insert для prise - выполнен успешно \n");

            JFrame jfInfo = new JFrame();
            JOptionPane.showMessageDialog(jfInfo, "Новый товар добавлен успешно!");

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД для inser for client \n");
                    taLog.append("------------------------------------------------- \n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        tfTowarNewName.setText(null);
        tfTowarNewCena.setText(null);
        tfTowarNewSort.setText(null);

        bbPriseZagruzActionPerformed(evt);
        bbNewTowar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bbNewTowarActionPerformed

    private void bbPriseZagruzActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbPriseZagruzActionPerformed
        //Загружаем Прайс-лист для редактирования
        bbPriseZagruz.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        taLog.append("Позиция-00 ...\n");

        cbPriseGroup.removeAllItems();

        taLog.append("Позиция-01 ...\n");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
            taLog.append("Соединение с БД для prise прошло успешно ...\n");
            taLog.append("Позиция-02 ...\n");

            stmt = conn.createStatement();

            DefaultTableModel dtm = (DefaultTableModel) tabPrise.getModel();
            //dtm.removeRow(0); //  Удаляем первую  строчку
            dtm.getDataVector().removeAllElements(); // удаляем все строки

            String query = "select artikul,id_group,name,cenaklient,ostat,realiz,prihod,tipgroup,sort,usluga from myservis.prise order by id_group,sort;";
            rs = stmt.executeQuery(query);

            int i = 0;
            int towarArtikulMax = 0;
            int sortMax = 0;
            int km = 0;
            //TabTowar.setRowHeight(TabTowar.getRowHeight() + 40);

            while (rs.next()) {
                int tipGroup = rs.getInt("tipgroup");
                int towarArtikul = rs.getInt("artikul");
                int towarId_group = rs.getInt("id_group");
                String towarName = rs.getString("name");
                int towarCena = rs.getInt("cenaklient");
                int towarOstat = rs.getInt("ostat");
                int towarRealiz = rs.getInt("realiz");
                int towarPrihod = rs.getInt("prihod");
                int towarSort = rs.getInt("sort");
                int towarUsluga = rs.getInt("usluga");

                if (tipGroup == 1) {
                    dtm.addRow(new Object[]{"", "", towarName + "  (" + towarId_group + ")", "", "", "", "", "", ""});
                    cbPriseGroup.addItem(towarName);
                    if (km > 0) {
                        masArtikul[km - 1] = towarArtikulMax + 1;
                        masSort[km - 1] = sortMax + 1;
                    }
                    masUsluga[km] = towarUsluga;
                    km = km + 1;
                    sortMax = 0;
                } else {
                    i = i + 1;
                    if (towarArtikulMax < towarArtikul) {
                        towarArtikulMax = towarArtikul;
                    }
                    if (sortMax < towarSort) {
                        sortMax = towarSort;
                    }
                    dtm.addRow(new Object[]{i, towarArtikul, towarName, towarCena, "", towarOstat, towarPrihod, towarRealiz, towarSort});
                }
            }
            if (km > 0) {
                masArtikul[km - 1] = towarArtikulMax + 1;
                masSort[km - 1] = sortMax + 1;
            }

            rs.close();
            stmt.close();

            cbPriseGroupActionPerformed(evt);
            taLog.append("Запрос к prise - выполнен успешно \n");

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД prise \n");
                    taLog.append("-------------------------------------------------\n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        bbPrisePrihod.setEnabled(true);
        tfPrihodPrim.setEnabled(true);
        bbNewTowar.setEnabled(true);
        chPriseRedakt.setEnabled(true);

        bbPriseZagruz.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bbPriseZagruzActionPerformed

    private void bbKKTsessionOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTsessionOpenActionPerformed
        //taKKTlog.setText(null);
        sessionKey = "";
        try {
            URL urlKKT = new URL(httpKKM);
            textJSON = "{\"sessionKey\": null, \"command\": \"OpenSession\", \"portName\": \"" + sKKMmercComPort + "\", \"model\": \"" + sKKMmercModel + "\"}";
            tfKKTzapros.setText(textJSON);

            //taKKTlog.append(sTire70);
            taKKTlog.append("Запрос:");
            taKKTlog.append(textJSON + "\n");
            taKKTlog.append(sTire70);

            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setConnectTimeout(5000);
            connKKT.setDoOutput(true);
            connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
            connKKT.setUseCaches(false);

            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                dos.writeBytes(textJSON);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    taKKTlog.append(line + "\n");
                    System.out.println(line);

                    tfKKTotvet.setText(line);
                    String sOtwet = tfKKTotvet.getText();
                    System.out.println("----------------------------------------------");

                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(sOtwet);
                    // получение строки из объекта
                    String sSessionKey = (String) jsonObject.get("sessionKey");
                    long sLong = (long) jsonObject.get("result");
                    String sResult = Long.toString(sLong);

                    sessionKey = sSessionKey;
                    tfKeySession.setText(sessionKey);
                    tfRezultKKT.setText(sResult);

                    if (sLong == 0) {
                        sSession = "open";
                    } else {
                        sSession = "close";
                    }
                    String sDescription = (String) jsonObject.get("description");
                    taKKTlog.append("Description:" + sDescription + "\n");
                    taKKTlog.append("Сессия:" + sSession + "\n");

                    System.out.println("     key     /rezult/Description");
                    System.out.println(sSessionKey + "/" + sResult + "/" + sDescription);
                }
            } catch (ParseException ex) {
                Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {
        }        
        taKKTlog.append("Запрос на открытие сессии с ККМ - выполнен успешно!\n" + sTire70);
        
    }//GEN-LAST:event_bbKKTsessionOpenActionPerformed

    private void bbKKTdraiverTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTdraiverTestActionPerformed
        // TODO add your handling code here:
        taKKTlog.append("Запрос связи с драйвером ККМ Incerman ...\n");
        try {
            URL urlKKT = new URL(httpKKM);
            textJSON = "{\"command\": \"GetDriverInfo\"}";

            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setDoOutput(true);
            connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
            connKKT.setUseCaches(false);

            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                dos.writeBytes(textJSON);
                //dos.writeUTF(textJSON);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    taKKTlog.append(line + "\n");
                    tfKKTotvet.setText(line);
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
        }
        taKKTlog.append("Запрос связи с драйвером ККМ Incerman - выполнен успешно!" + sTire70);
    }//GEN-LAST:event_bbKKTdraiverTestActionPerformed

    private void bbKKTsessionCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTsessionCloseActionPerformed
        try {
            URL urlKKT = new URL(httpKKM);
            textJSON = "{\"sessionKey\": \"" + sessionKey + "\",\"command\": \"CloseSession\"}";
            tfKKTzapros.setText(textJSON);

            taKKTlog.append("Запрос:");
            taKKTlog.append(textJSON + "\n");
            taKKTlog.append(sTire70);

            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setConnectTimeout(5000);
            connKKT.setDoOutput(true);
            connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
            connKKT.setUseCaches(false);

            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                dos.writeBytes(textJSON);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    taKKTlog.append(line + "\n");
                    tfKKTotvet.setText(line);
                    System.out.println(line);
                    System.out.println("----------------------------------------------");
                    String sOtwet = tfKKTotvet.getText();

                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(sOtwet);
                    // получение строки из объекта
                    long sLong = (long) jsonObject.get("result");
                    String sResult = Long.toString(sLong);
                    String sDescription = (String) jsonObject.get("description");
                    tfRezultKKT.setText(sResult);

                    taKKTlog.append("Description:" + sDescription + "\n");

                    System.out.println("rezult/Description");
                    System.out.println(sResult + "/" + sDescription);

                    if (sLong == 0) {
                        sSession = "close";
                    } else {
                        sSession = "none";
                    }

                }
            } catch (ParseException ex) {
                Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {}
        taKKTlog.append("Запрос на закрытие сессии с ККМ - выполнен успешно!\n");
    }//GEN-LAST:event_bbKKTsessionCloseActionPerformed

    private void bbKKTopenSmenaTotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTopenSmenaTotActionPerformed
        // TODO add your handling code here:
        starTime2 = System.currentTimeMillis();
        deltaTime = (starTime2 - starTime1) / 1000;
        if (deltaTime > 5) {
            bbKKTopenSmenaTot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            taKKTlog.setText(null);
            taKKTlog.append(sTire99);
            String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
            taKKTlog.append("Версия программы: "+sVersionProg+" Прошло времени, сек: " + deltaTime + "   Дата: " + sDateTimeTek + " Запрос на Отрытие смены:\n");
            taKKTlog.append(sTire99);

            bbKKTsessionOpenActionPerformed(evt);
            if ("open".equals(sSession)) {
                bbKKTsmenaOpenActionPerformed(evt);
                bbKKTsessionCloseActionPerformed(evt);

                starTime1 = System.currentTimeMillis();

                try {
                    Path fileName = Paths.get("temp\\log_kkt_merkuriy_tot.txt");
                    Files.write(fileName, taKKTlog.getText().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else {
            taInfoMain.append("Между запросами должно пройти не менее 5 сек. Прошло: " + deltaTime + " сек.\n");
        }
        bbKKTopenSmenaTot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bbKKTopenSmenaTotActionPerformed

    private void bbKKTcloseSmenaTotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTcloseSmenaTotActionPerformed
        // TODO add your handling code here:
        starTime2 = System.currentTimeMillis();
        deltaTime = (starTime2 - starTime1) / 1000;
        if (deltaTime > 5) {
            bbKKTcloseSmenaTot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            taKKTlog.setText(null);
            taKKTlog.append(sTire99);
            String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
            taKKTlog.append("Версия программы: "+sVersionProg+" Прошло времени, сек: " + deltaTime + "   Дата: " + sDateTimeTek + "Запрос на Закрытие смены:\n");
            taKKTlog.append(sTire99);

            bbKKTsessionOpenActionPerformed(evt);
            if ("open".equals(sSession)) {
                bbKKTsmenaCloseActionPerformed(evt);
                bbKKTsessionCloseActionPerformed(evt);

                starTime1 = System.currentTimeMillis();

                try {
                    Path fileName = Paths.get("temp\\log_kkt_merkuriy_tot.txt");
                    Files.write(fileName, taKKTlog.getText().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            taInfoMain.append("Между запросами должно пройти не менее 5 сек. Прошло: " + deltaTime + " сек.\n");
        }
        bbKKTcloseSmenaTot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bbKKTcloseSmenaTotActionPerformed

    private void bbKKTprovStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTprovStatusActionPerformed
        starTime2 = System.currentTimeMillis();
        deltaTime = (starTime2 - starTime1) / 1000;
        if (deltaTime > 5) {
            bbKKTprintText.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            taKKTlog.setText(null);
            taKKTlog.append(sTire99);
            String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
            taKKTlog.append("Прошло времени, сек: " + deltaTime + "   Дата: " + sDateTimeTek + " Запрос на проверку статуса:\n");
            taKKTlog.append(sTire99);

            bbKKTsessionOpenActionPerformed(evt);

            if ("open".equals(sSession)) {
                bbKKTgetStatusActionPerformed(evt);
                bbKKTsessionCloseActionPerformed(evt);

                starTime1 = System.currentTimeMillis();

                try {
                    Path fileName = Paths.get("temp\\log_kkt_merkuriy_tot.txt");
                    Files.write(fileName, taKKTlog.getText().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            taInfoMain.append("Между запросами должно пройти не менее 5 сек. Прошло: " + deltaTime + " сек.\n");
        }
        bbKKTprintText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
    }//GEN-LAST:event_bbKKTprovStatusActionPerformed

    private void bbKKTprintTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTprintTextActionPerformed
        // TODO add your handling code here:
        starTime2 = System.currentTimeMillis();
        deltaTime = (starTime2 - starTime1) / 1000;
        if (deltaTime > 5) {
            bbKKTprintText.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            taKKTlog.setText(null);
            taKKTlog.append(sTire99);
            String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
            taKKTlog.append("Прошло времени, сек: " + deltaTime + "   Дата: " + sDateTimeTek + " Запрос на Добрый день:\n");
            taKKTlog.append(sTire99);

            bbKKTsessionOpenActionPerformed(evt);

            if ("open".equals(sSession)) {
                bbKKTgetStatusActionPerformed(evt);
                bbKKTdobriyDenActionPerformed(evt);
                bbKKTsessionCloseActionPerformed(evt);

                starTime1 = System.currentTimeMillis();

                try {
                    Path fileName = Paths.get("temp\\log_kkt_merkuriy_tot.txt");
                    Files.write(fileName, taKKTlog.getText().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            taInfoMain.append("Между запросами должно пройти не менее 5 сек. Прошло: " + deltaTime + " сек.\n");
        }
        bbKKTprintText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bbKKTprintTextActionPerformed

    private void bbKKTdobriyTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTdobriyTestActionPerformed
        // TODO add your handling code here:        
        try {
            URL urlKKT = new URL(httpKKM);
            //textJSON="{\"sessionKey\": \""+sessionKey+"\",\"command\": \"CloseSession\"}";
            textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"PrintText\", \"text\": \"" + tfKKTtext.getText() + "\"}";
            //textJSON="{\"sessionKey\": null, \"command\": \"OpenSession\", \"portName\": \""+sKKMmercComPort+"\", \"model\": \""+ssKKM_Merc_Model+"\"}";

            //String serbianString = textJSON; // What are you doing? 
            byte[] bytes = textJSON.getBytes(StandardCharsets.UTF_8);

            ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(textJSON);
            byte[] ptext = textJSON.getBytes(UTF_8);
            String value = new String(ptext, UTF_8);

            //String utf8String = new String(bytes); 
            String textJSON2 = new String(bytes);
            System.out.println("----------------------------------");
            System.out.println(textJSON);
            System.out.println("----------------------------------");
            // for (byte b : bytes) { 
            //System.out.print(String.format("%s ", b)); 
            //} 
            //System.out.println("----------------------------------");             
            //System.out.println(value); 
            //System.out.println("----------------------------------"); 

            //String utf8String = new String(bytes, StandardCharsets.UTF_8); 
            System.out.println("----------------------------------");
            System.out.println(URLEncoder.encode(textJSON, "UTF8"));
            System.out.println("----------------------------------");

            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setDoOutput(true);
            connKKT.setUseCaches(false);
            //connKKT.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connKKT.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connKKT.setRequestProperty("Content-Type", "application/json");
            connKKT.setRequestProperty("charset", "UTF-8");
            //connKKT.setRequestProperty("Content-Length", Integer.toString(postData.length()));
            connKKT.setRequestProperty("Accept-Charset", "UTF-8");
            //connKKT.setRequestProperty("Content-Language", "ru-RU");

            tfKKTzapros.setText(textJSON);
            taKKTlog.append(sTire70);
            taKKTlog.append("Запрос:");
            taKKTlog.append(textJSON);
            taKKTlog.append(sTire70);

            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                //dos.writeBytes(textJSON2); - не работает
                //dos.writeBytes(value);  // Без кирилиицы работает
                //dos.writeUTF(value);
                //dos.writeChars(textJSON);
                //dos.writeUTF("nom_sc=1069000000&lcod=5159&text="+new String(tfKKTtext.getText().getBytes("UTF-8"), StandardCharsets.UTF_8));
                //dos.writeUTF("{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"PrintText\", \"text\": \"" +new String("Добрый день!".getBytes("UTF-8"), StandardCharsets.UTF_8)+ "\"}");

                //dos.writeUTF(value);      // - не работает
                //dos.writeChars(value);    // - не работает                
                //dos.writeUTF(textJSON);  // - не работает
                //String str = encodeURI(JSON.stringify(json));
                //encodeURI
                //URLDecoder.decode(title, "UTF-8")
                //URLEncoder.encode(URL, "UTF-8");
                //dos.writeBytes(URLEncoder.encode(textJSON,"UTF8"));  // не  работает
                //dos.writeUTF(URLEncoder.encode(textJSON,"UTF8"));  // не  работает
                //String str = "Privet";
                //String str = "\\u0436\\u0430\\u0440\\u043A\\u043E";                                
                String str2 = "";
                String str22 = "";
                String inString = "ПриветМир";
                for (int i = 0; i < inString.length(); i++) {
                    Integer charCode = (int) inString.charAt(i);
                    str22 = Integer.toHexString(charCode);
                    str2 += "\\u" + strlenFix(str22);
                }

                //str2="\\u041f\\u0440\\u0438\\u0432\\u0435\\u0442  \\u041c\\u0438\\u0440";
                System.out.println(str2);
                dos.writeBytes("{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"PrintText\", \"text\": \"" + str2 + "\"}");  // Без кирилиицы работает

                //encode string into URI format
                //String transportString = URLEncoder.encode("Добрый день", "UTF-8");
                //String decodedString = URLDecoder.decode(transportString, "UTF-8");                
                //dos.writeBytes("{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"PrintText\", \"text\": \"" +decodedString+ "\"}");  // Без кирилиицы работает
                //dos.writeByte("{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"PrintText\", \"text\": \"" +URLDecoder.decode(tfKKTtext.getText(), "UTF-8")+ "\"}");  // Без кирилиицы работает                                
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    taKKTlog.append(line + "\n");
                    tfKKTotvet.setText(line);
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
        }
        taKKTlog.append("Запрос Добрый день - выполнен успешно!" + sTire70);

    }//GEN-LAST:event_bbKKTdobriyTestActionPerformed

    private void bbZakazZakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbZakazZakActionPerformed
        // TODO add your handling code here:
        //DefaultTableModel dtm2 = (DefaultTableModel) tabZakaz.getModel();
        //dtm2.getDataVector().removeAllElements(); // удаляем все строки
        tfKlientZakazLCod.setText("");
        tfKlientZakazSCod.setText("");
        tfKlientZakazFIO.setText("");
        tfKlientZakazSkidPr.setText("");
        tfSummaZak.setText("");

        for (int i = 0; i < tabZakaz.getRowCount(); i++) {
            tabZakaz.setValueAt("", i, 0);
            tabZakaz.setValueAt("", i, 1);
            tabZakaz.setValueAt("", i, 2);
            tabZakaz.setValueAt("", i, 3);
            tabZakaz.setValueAt("", i, 4);
            tabZakaz.setValueAt("", i, 5);
        }

        //DefaultTableModel dtm1 = (DefaultTableModel) tabTowar.getModel();
        //dtm1.getDataVector().removeAllElements(); // удаляем все строки
        tfKlientPriseFIO.setText("");
        tfKlientPriseLCod.setText("");
        tfKlientPriseSCod.setText("");
        tfWibZakSumma.setText("");
        cbSkidka.setSelectedIndex(0);

    }//GEN-LAST:event_bbZakazZakActionPerformed

    private void panKassaAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_panKassaAncestorAdded
        // TODO add your handling code here:
        cbKassaMon.removeAllItems();
        cbKassaMon.addItem("Январь");
        cbKassaMon.addItem("Февраль");
        cbKassaMon.addItem("Март");
        cbKassaMon.addItem("Апрель");
        cbKassaMon.addItem("Май");
        cbKassaMon.addItem("Июнь");
        cbKassaMon.addItem("Июль");
        cbKassaMon.addItem("Август");
        cbKassaMon.addItem("Сентябрь");
        cbKassaMon.addItem("Октябрь");
        cbKassaMon.addItem("Ноябрь");
        cbKassaMon.addItem("Декабрь");
        cbKassaMon.setMaximumRowCount(12);

        String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        String sMonTek = sDateTimeTek.substring(5, 7);
        String sGodTek = sDateTimeTek.substring(0, 4);
        String sDatTek = sDateTimeTek.substring(8, 10);
        int mont = Integer.parseInt(sMonTek);

        tfKassaDat.setText(sDatTek);
        cbKassaMon.setSelectedIndex(mont - 1);
        tfKassaGod.setText(sGodTek);
    }//GEN-LAST:event_panKassaAncestorAdded

    private void tfKassaGodKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfKassaGodKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfKassaGodKeyTyped

    private void tfKassaGodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKassaGodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKassaGodActionPerformed

    private void cbKassaMonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKassaMonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKassaMonActionPerformed

    private void tfKassaDatKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfKassaDatKeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfKassaDatKeyTyped

    private void tfKassaDatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKassaDatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKassaDatActionPerformed

    private void tabKasPrihodComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_tabKasPrihodComponentShown
        // TODO add your handling code here:
    }//GEN-LAST:event_tabKasPrihodComponentShown

    private void tabKasRashodComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_tabKasRashodComponentShown
        // TODO add your handling code here:
    }//GEN-LAST:event_tabKasRashodComponentShown

    private void bbKassaOtchetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKassaOtchetActionPerformed
        int God = Integer.parseInt(tfKassaGod.getText());
        int Dat = Integer.parseInt(tfKassaDat.getText());

        if (God < 2023) {
            JFrame jfInfo = new JFrame();
            JOptionPane.showMessageDialog(jfInfo, "Год менее 2023!");
        } else {
            if (God > 2033) {
                JFrame jfInfo = new JFrame();
                JOptionPane.showMessageDialog(jfInfo, "Год более 2033!");
            } else {
                if (Dat < 1) {
                    JFrame jfInfo = new JFrame();
                    JOptionPane.showMessageDialog(jfInfo, "Не правильно задана дата!");
                } else {
                    if (Dat > 31) {
                        JFrame jfInfo = new JFrame();
                        JOptionPane.showMessageDialog(jfInfo, "Дата более, чем 31!");
                    } else {

                        bbKassaOtchet.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        try {
                            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

                            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
                            taLog.append("Соединение с БД для касса прошло успешно ...\n");

                            int sumNalTotNal = 0;
                            int sumNalTotQr = 0;

                            DefaultTableModel dtm = (DefaultTableModel) tabKasPrihod.getModel();
                            dtm.getDataVector().removeAllElements(); // удаляем все строки

                            DefaultTableModel dtm2 = (DefaultTableModel) tabKasRashod.getModel();
                            dtm2.getDataVector().removeAllElements(); // удаляем все строки

                            String sGod = tfKassaGod.getText();
                            String sDat;
                            String sMon = Integer.toString(cbKassaMon.getSelectedIndex() + 1);

                            String sDateTek;
                            String query1;
                            String query2;
                            String query3;

                            if (chKassaOtchetMon.isSelected()) {
                                String sDatePred = sGod + "-" + sMon + "-01";
                                // Приход
                                //query1 = "select date_kas,lcod,fio,sum_nal,sum_gr,tip_chek,isp,id_zak,prim from myservis.kassa where MONTH(date_kas) = \"" + sMon + "\"     AND tip_kas=1 order by date_kas";
                                query1 = "select date_kas,lcod,fio,sum_nal,sum_gr,tip_chek,isp,id_zak,prim from myservis.kassa where MONTH(date_kas) = \"" + sMon + "\" AND  YEAR(date_kas) = \"" + sGod + "\" AND tip_kas=1 order by date_kas";
                                // Расход
                                query2 = "select date_kas,lcod,fio,sum_nal,sum_gr,tip_chek,isp,id_zak,prim from myservis.kassa where MONTH(date_kas) = \"" + sMon + "\" AND  YEAR(date_kas) = \"" + sGod + "\" AND tip_kas=2 order by date_kas";
                                // Расчет сумм
                                query3 = "select sum_nal,tip_kas from myservis.kassa where (date_kas < \"" + sDatePred + "\") order by date_kas";
                            } else {
                                sDat = tfKassaDat.getText();
                                sDateTek = sGod + "-" + sMon + "-" + sDat;
                                // Приход
                                query1 = "select date_kas,lcod,fio,sum_nal,sum_gr,tip_chek,isp,id_zak,prim from myservis.kassa where  date(date_kas) = \"" + sDateTek + "\" AND tip_kas=1 order by date_kas";
                                // Расход
                                query2 = "select date_kas,lcod,fio,sum_nal,sum_gr,tip_chek,isp,prim from myservis.kassa where  date(date_kas) = \"" + sDateTek + "\"  AND tip_kas=2 order by date_kas";
                                // Расчет сумм
                                query3 = "select sum_nal,tip_kas from myservis.kassa where (date_kas < \"" + sDateTek + "\") order by date_kas";
                            }

                            //String query = "select date_kas,lcod,fio,sum_nal,sum_gr,tip_chek,isp,id_zak from myservis.kassa where (date_kas BETWEEN \"" + sDate1 + "\" AND \"" + sDate2 + "\") " 
                            stmt = conn.createStatement();
                            rs = stmt.executeQuery(query1);    // Выбираем приход
                            int i = 0;
                            while (rs.next()) {
                                i = i + 1;
                                String date_kas = rs.getString("date_kas");
                                int lcod = rs.getInt("lcod");
                                String fio = rs.getString("fio");
                                int sum_nal = rs.getInt("sum_nal");
                                int sum_gr = rs.getInt("sum_gr");
                                int tip_chek = rs.getInt("tip_chek");
                                int id_zak = rs.getInt("id_zak");
                                String isp = rs.getString("isp");
                                String sPrim = rs.getString("prim");

                                String stip_chek = "";
                                if (tip_chek == 1) {
                                    stip_chek = "+";
                                }
                                if (tip_chek == 2) {
                                    stip_chek = "-";
                                }
                                if (tip_chek == 0) {
                                    stip_chek = "в";
                                }

                                dtm.addRow(new Object[]{i, date_kas, lcod, fio, sum_nal, sum_gr, stip_chek, id_zak, isp, sPrim});

                                sumNalTotNal = sumNalTotNal + sum_nal;
                                sumNalTotQr = sumNalTotQr + sum_gr;
                            }

                            String ssumNalTotNal = Integer.toString(sumNalTotNal);

                            tfKasPrihodNal.setText(ssumNalTotNal);
                            tfKasPrihodQR.setText(Integer.toString(sumNalTotQr));

                            int sumNalTotRashod = 0;
                            //query = "select date_kas,lcod,fio,sum_nal,sum_gr,tip_chek,isp from myservis.kassa where (date_kas BETWEEN \"" + sDate1 + "\" AND \"" + sDate2 + "\") "  
                            //+" AND tip_kas=2 order by date_kas";
                            
                            rs = stmt.executeQuery(query2);  // Выбираем расход
                            int k = 0;
                            while (rs.next()) {
                                k = k + 1;
                                String date_kas = rs.getString("date_kas");
                                int lcod = rs.getInt("lcod");
                                String fio = rs.getString("fio");
                                int sum_nal = rs.getInt("sum_nal");
                                String isp = rs.getString("isp");
                                String sPrim = rs.getString("prim");

                                dtm2.addRow(new Object[]{k, date_kas, lcod, fio, sum_nal, isp, sPrim});
                                sumNalTotRashod = sumNalTotRashod + sum_nal;
                            }

                            tfKasRashod.setText(Integer.toString(sumNalTotRashod));

                            int sumNalTotPrihod2 = 0;
                            int sumNalTotRashod2 = 0;

                            rs = stmt.executeQuery(query3);  // Считае суммы

                            while (rs.next()) {
                                int tip_kas = rs.getInt("tip_kas");
                                int sum_nal = rs.getInt("sum_nal");

                                if (tip_kas == 1) {
                                    sumNalTotPrihod2 = sumNalTotPrihod2 + sum_nal;
                                }
                                if (tip_kas == 2) {
                                    sumNalTotRashod2 = sumNalTotRashod2 + sum_nal;
                                }
                            }

                            rs.close();
                            stmt.close();

                            tfKassaOstatok.setText(Integer.toString(sumNalTotPrihod2 - sumNalTotRashod2));

                            tfKassaTek.setText(Integer.toString(sumNalTotPrihod2 - sumNalTotRashod2 + sumNalTotNal - sumNalTotRashod));

                            taLog.append("Запрос к kassa - выполнен успешно \n");

                            if (i == 0) {
                                dtm.getDataVector().removeAllElements(); // удаляем все строки
                                dtm.addRow(new Object[]{"", "", "", "", "", "", "", "", ""});
                                bbKassaCSV.setEnabled(false);
                                bbKassaExcel.setEnabled(false);
                            } else {
                                bbKassaCSV.setEnabled(true);
                                bbKassaExcel.setEnabled(true);
                            }

                            if (k == 0) {
                                dtm2.getDataVector().removeAllElements(); // удаляем все строки
                                dtm2.addRow(new Object[]{"", "", "", "", "", "", ""});
                                //bbBDzakazSave.setEnabled(false);
                            } else {
                                bbKassaCSV.setEnabled(true);
                            }

                            if (i == 0) {
                                JOptionPane.showMessageDialog(new JFrame(), "Заказов за выбранный день не обнаружено!");
                            }

                            if (k == 0) {
                                JOptionPane.showMessageDialog(new JFrame(), "Расходов за выбранный день не обнаружено!");
                            }

                        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
                            System.err.println("Cannot connect to database server");
                        } finally {
                            if (conn != null) {
                                try {
                                    taLog.append("Завершение запроса к БД kassa \n");
                                    taLog.append("-------------------------------------------------\n");
                                    System.out.println("n***** Let terminate the Connection *****");
                                    conn.close();
                                    System.out.println("Database connection terminated... ");
                                } catch (SQLException ex) {
                                    System.out.println("Error in connection termination!");
                                }
                            }
                        }
                        bbKassaOtchet.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                }
            }
        }
        bbKassaPrihod.setEnabled(true);
        bbKassaRashod.setEnabled(true);
    }//GEN-LAST:event_bbKassaOtchetActionPerformed

    private void bbKassaRashodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKassaRashodActionPerformed
        // TODO add your handling code here:
        JFrame frameRashod = new JFrame("Расход из Кассы");
        frameRashod.setBounds(100, 170, 860, 300);

        //JOptionPane.showMessageDialog(jfInfo, "Успешно!");
        JPanel panRashod = new JPanel();
        panRashod.setLayout(new BorderLayout());
        panRashod.setLayout(null);
        panRashod.setBounds(0, 0, 860, 220);
        //panR.setBackground(Color.GRAY);

        JTextField tfRarhodSumma = new JTextField("0");
        tfRarhodSumma.setBounds(20, 50, 140, 38);
        tfRarhodSumma.setFont(new java.awt.Font("Segoe UI", 0, 22));
        tfRarhodSumma.setForeground(new java.awt.Color(0, 0, 153));
        JLabel lbRarhodSumma = new JLabel("Сумма расхода");
        lbRarhodSumma.setBounds(25, 20, 140, 34);
        lbRarhodSumma.setFont(new java.awt.Font("Segoe UI", 0, 18));
        lbRarhodSumma.setForeground(new java.awt.Color(0, 0, 153));

        JTextField tfRashodFIO = new JTextField("");
        tfRashodFIO.setBounds(180, 50, 380, 38);
        tfRashodFIO.setFont(new java.awt.Font("Segoe UI", 0, 22));
        tfRashodFIO.setForeground(new java.awt.Color(0, 0, 153));
        JLabel lbRarhodFIO = new JLabel("ФИО на кого оформить расход");
        lbRarhodFIO.setBounds(185, 20, 380, 34);
        lbRarhodFIO.setFont(new java.awt.Font("Segoe UI", 0, 18));
        lbRarhodFIO.setForeground(new java.awt.Color(0, 0, 153));

        JButton bbRashod = new JButton("Произвести расход");
        bbRashod.setBounds(580, 50, 220, 38);
        bbRashod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbRashod.setForeground(new java.awt.Color(0, 0, 153));
        bbRashod.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbRarhodPrim = new JLabel("Примечание:");
        lbRarhodPrim.setBounds(25, 100, 140, 34);
        lbRarhodPrim.setFont(new java.awt.Font("Segoe UI", 0, 18));
        lbRarhodPrim.setForeground(new java.awt.Color(0, 0, 153));
        JTextField tfRashodPrim = new JTextField("");
        tfRashodPrim.setBounds(180, 100, 620, 38);
        tfRashodPrim.setFont(new java.awt.Font("Segoe UI", 0, 22));
        tfRashodPrim.setForeground(new java.awt.Color(0, 0, 153));

        bbRashod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbRashodActionPerformed(evt);
                bbKassaOtchetActionPerformed(evt);
                frameRashod.setVisible(false);
            }

            private void bbRashodActionPerformed(java.awt.event.ActionEvent evt) {
                int tip_kas = 2;
                String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
                sDateTimeTek = "\"" + sDateTimeTek + "\"";
                sIspolnitel = "\"" + aktPolLogin + "\"";

                String sPrim = "\"" + tfRashodPrim.getText() + "\"";
                String ssFIO = "\"" + tfRashodFIO.getText() + "\"";

                try {
                    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

                    conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);

                    taLog.append("Запуск соединения с БД для insert в kassa ... \n");

                    stmt = conn.createStatement();

                    String query6 = "insert ignore into myservis.kassa (tip_kas,date_kas,sum_nal,fio,isp,prim) values ("
                            + tip_kas + "," + sDateTimeTek + "," + tfRarhodSumma.getText() + "," + ssFIO + ","
                            + sIspolnitel + "," + sPrim
                            + ");";

                    stmt.executeUpdate(query6);

                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
                    System.err.println("Cannot connect to database server");
                } finally {
                    if (conn != null) {
                        try {
                            taLog.append("Завершение запроса к БД после добавление расхода в кассу \n");
                            taLog.append("------------------------------------------------- \n");
                            System.out.println("n***** Let terminate the Connection *****");
                            conn.close();
                            System.out.println("Database connection terminated... ");
                        } catch (SQLException ex) {
                            System.out.println("Error in connection termination!");
                        }
                    }
                }
                taLog.append("Запрос update для kassa - выполнен успешно \n");
                JFrame jfInfo = new JFrame();
                JOptionPane.showMessageDialog(jfInfo, "Расход по Кассе добавлен успешно!");
            }

        });

        panRashod.add(lbRarhodSumma);
        panRashod.add(lbRarhodFIO);
        panRashod.add(tfRarhodSumma);
        panRashod.add(tfRashodFIO);
        panRashod.add(bbRashod);
        panRashod.add(lbRarhodPrim);
        panRashod.add(tfRashodPrim);

        frameRashod.add(panRashod);
        frameRashod.setVisible(true);
    }//GEN-LAST:event_bbKassaRashodActionPerformed

    private void bbKassaPrihodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKassaPrihodActionPerformed
        // TODO add your handling code here:
        JFrame framePrihod = new JFrame("Приход в Кассу");
        framePrihod.setBounds(100, 170, 860, 300);

        //JOptionPane.showMessageDialog(jfInfo, "Успешно!");
        JPanel panPrihod = new JPanel();
        panPrihod.setLayout(new BorderLayout());
        panPrihod.setLayout(null);
        panPrihod.setBounds(0, 0, 860, 220);
        //panR.setBackground(Color.GRAY);

        JTextField tfPrihodSumma = new JTextField("0");
        tfPrihodSumma.setBounds(20, 50, 140, 38);
        tfPrihodSumma.setFont(new java.awt.Font("Segoe UI", 0, 22));
        tfPrihodSumma.setForeground(new java.awt.Color(0, 0, 153));
        JLabel lbPrihodSumma = new JLabel("Сумма прихода");
        lbPrihodSumma.setBounds(25, 20, 140, 34);
        lbPrihodSumma.setFont(new java.awt.Font("Segoe UI", 0, 18));
        lbPrihodSumma.setForeground(new java.awt.Color(0, 0, 153));

        JTextField tfPrihodFIO = new JTextField("");
        tfPrihodFIO.setBounds(180, 50, 380, 38);
        tfPrihodFIO.setFont(new java.awt.Font("Segoe UI", 0, 22));
        tfPrihodFIO.setForeground(new java.awt.Color(0, 0, 153));
        JLabel lbPrihodFIO = new JLabel("ФИО от кого оформить приход");
        lbPrihodFIO.setBounds(185, 20, 380, 34);
        lbPrihodFIO.setFont(new java.awt.Font("Segoe UI", 0, 18));
        lbPrihodFIO.setForeground(new java.awt.Color(0, 0, 153));

        JButton bbPrihod = new JButton("Произвести приход");
        bbPrihod.setBounds(580, 50, 220, 38);
        bbPrihod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbPrihod.setForeground(new java.awt.Color(0, 0, 153));
        bbPrihod.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbPrihodPrim = new JLabel("Примечание:");
        lbPrihodPrim.setBounds(25, 100, 140, 34);
        lbPrihodPrim.setFont(new java.awt.Font("Segoe UI", 0, 18));
        lbPrihodPrim.setForeground(new java.awt.Color(0, 0, 153));
        JTextField tfPrihodPrim = new JTextField("");
        tfPrihodPrim.setBounds(180, 100, 620, 38);
        tfPrihodPrim.setFont(new java.awt.Font("Segoe UI", 0, 22));
        tfPrihodPrim.setForeground(new java.awt.Color(0, 0, 153));

        bbPrihod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbPrihodActionPerformed(evt);
                bbKassaOtchetActionPerformed(evt);
                framePrihod.setVisible(false);
            }

            private void bbPrihodActionPerformed(java.awt.event.ActionEvent evt) {
                int tip_kas = 1;
                String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
                sDateTimeTek = "\"" + sDateTimeTek + "\"";
                sIspolnitel = "\"" + aktPolLogin + "\"";

                String ssFIO = "\"" + tfPrihodFIO.getText() + "\"";
                String sPrim = "\"" + tfPrihodPrim.getText() + "\"";

                try {
                    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

                    conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);

                    taLog.append("Запуск соединения с БД для insert в kassa ... \n");

                    stmt = conn.createStatement();

                    String query6 = "insert ignore into myservis.kassa (tip_kas,date_kas,sum_nal,fio,isp,prim) values ("
                            + tip_kas + "," + sDateTimeTek + "," + tfPrihodSumma.getText() + "," + ssFIO + ","
                            + sIspolnitel + "," + sPrim
                            + ");";

                    stmt.executeUpdate(query6);

                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
                    System.err.println("Cannot connect to database server");
                } finally {
                    if (conn != null) {
                        try {
                            taLog.append("Завершение запроса к БД после добавление прихода в кассу \n");
                            taLog.append("------------------------------------------------- \n");
                            System.out.println("n***** Let terminate the Connection *****");
                            conn.close();
                            System.out.println("Database connection terminated... ");
                        } catch (SQLException ex) {
                            System.out.println("Error in connection termination!");
                        }
                    }
                }
                taLog.append("Запрос insert для kassa - выполнен успешно \n");
                JOptionPane.showMessageDialog(new JFrame(), "Приход по Кассе добавлен успешно!");
            }

        });

        panPrihod.add(lbPrihodSumma);
        panPrihod.add(lbPrihodFIO);
        panPrihod.add(tfPrihodSumma);
        panPrihod.add(tfPrihodFIO);
        panPrihod.add(bbPrihod);
        panPrihod.add(lbPrihodPrim);
        panPrihod.add(tfPrihodPrim);

        framePrihod.add(panPrihod);
        framePrihod.setVisible(true);
    }//GEN-LAST:event_bbKassaPrihodActionPerformed

    private void bbKassaCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKassaCSVActionPerformed
        String sDat;
        if (chKassaOtchetMon.isSelected()) {
            sDat = tfKassaGod.getText() + "_" + cbKassaMon.getItemAt(cbKassaMon.getSelectedIndex());
        } else {
            sDat = tfKassaGod.getText() + "_" + Integer.toString(cbKassaMon.getSelectedIndex() + 1) + "_" + tfKassaDat.getText();
        }

        String nameFiles = "temp\\Кassa_" + sDat + ".csv";

        String fText = "Дата;" + sDat + ";;Сумма на начало дня:;" + tfKassaOstatok.getText() + ";;;;;\n\n";
        fText +="Приход:;;;;;;;;;\n";
        fText +="№ пп;Дата;Личный Id;ФИО;Сумма Нал;Сумма QR-код;Чек;№ нак.;Исполнитель;Примечание;\n";

        for (int i = 0; i < tabKasPrihod.getRowCount(); i++) {
            for (int k = 0; k < 10; k++) {
                fText +=tabKasPrihod.getValueAt(i, k).toString() + ";";
            }
            fText +="\n";
        }
        fText +=";;;;" + tfKasPrihodNal.getText() + ";" + tfKasPrihodQR.getText() + ";;;;;\n\n";
        fText +="Расход:;;;;;;;;;;\n";

        for (int i = 0; i < tabKasRashod.getRowCount(); i++) {
            for (int k = 0; k < 5; k++) {
                fText +=tabKasRashod.getValueAt(i, k).toString() + ";";
            }
            fText +=";;;" + tabKasRashod.getValueAt(i, 5).toString() + ";" + tabKasRashod.getValueAt(i, 6).toString() + ";";
            fText +="\n";
        }
        fText +=";;;;" + tfKasRashod.getText() + ";;;;;;\n\n";

        fText +=";;;Сумма на конец дня:;" + tfKassaTek.getText() + ";;;;;;\n";

        try {
            File file = new File(nameFiles);
            file.delete();
            file.createNewFile();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(fText);
                writer.close();

            } catch (IOException e) {
                System.out.println("Ошибка-1 при записи в файл");
                taLog.append("Ошибка-1 при записи в файл \n");
            }

            System.out.println("Текст успешно записан в файл.");
        } catch (IOException e) {
            System.out.println("Ошибка-2 при записи в файл");
            taLog.append("Ошибка-2 при записи в файл \n");
        }

        JOptionPane.showMessageDialog(new JFrame(), "Файл:\n\n" + progDir + nameFiles + "\n\nCоздан успешно!");        
    }//GEN-LAST:event_bbKassaCSVActionPerformed

    private void chKassaOtchetMonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chKassaOtchetMonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chKassaOtchetMonActionPerformed

    private void bbUslugaOtchetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbUslugaOtchetActionPerformed
        //Загружаем услуги
        if ("".equals(tfUslugaLCod.getText())) {
            JFrame jfInfo = new JFrame();
            JOptionPane.showMessageDialog(jfInfo, "Необходимо обязательно выбрать Клиента");
        } else {

            bbUslugaOtchet.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            tfUslugaID.setText("");
            tfUslugaArtikul.setText("");
            tfUslugaName.setText("");
            tfUslugaOstatok.setText("");
            tfUslugaIspTot.setText("");

            try {
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

                conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
                taLog.append("Соединение с БД для таблицы uslugi прошло успешно ...\n");

                DefaultTableModel dtm = (DefaultTableModel) tabUslugi.getModel();
                dtm.getDataVector().removeAllElements(); // удаляем все строки

                /*
            String sGod = tfBDZakazGod.getText();
            String sDat = tfBDZakazDat.getText();
            String sMon = Integer.toString(cbBDzakazMon.getSelectedIndex() + 1);

            String sDate1 = sGod + "-01-01";
            String sDate2 = sGod + "-12-31";

            if (cbBDzakazMon.getSelectedIndex() > -1) {
                sDate1 = sGod + "-" + sMon + "-01";
                sDate2 = sGod + "-" + sMon + "-31";
            }

            if (!"".equals(sDat)) {
                sDate1 = sGod + "-" + sMon + "-" + sDat;
                int mDat2 = Integer.parseInt(sDat);
                String sDat2 = Integer.toString(mDat2 + 1);
                sDate2 = sGod + "-" + sMon + "-" + sDat2;

                jTextArea1.append("(Дата) ...\n");
                jTextArea1.append("sDate1:" + sDate1 + " ...\n");
                jTextArea1.append("sDate2:" + sDate2 + " ...\n");

            }
                 */
                stmt = conn.createStatement();
                String query = "select date_zak,id_zak,artikul,usluga_name,usluga_kol,usluga_ispol_tot,usluga_ispol,date_ispol,usluga_ostat,isp from myservis.uslugi where lcod=" + tfUslugaLCod.getText() + " order by id";
                rs = stmt.executeQuery(query);
                int i = 0;
                while (rs.next()) {
                    i++;
                    int id_zak = rs.getInt("id_zak");
                    int artikul = rs.getInt("artikul");

                    String date_zak = rs.getString("date_zak");
                    date_zak = date_zak + " ";
                    String date_ispol = rs.getString("date_ispol");
                    date_ispol = date_ispol + " ";
                    String usluga_name = rs.getString("usluga_name");
                    usluga_name = usluga_name + " ";
                    String isp = rs.getString("isp");
                    isp = isp + " ";

                    int usluga_kol = rs.getInt("usluga_kol");
                    int usluga_ispol_tot = rs.getInt("usluga_ispol_tot");
                    int usluga_ispol = rs.getInt("usluga_ispol");
                    int usluga_ostat = rs.getInt("usluga_ostat");

                    tfUslugaOstatok.setText(Integer.toString(usluga_ostat));
                    tfUslugaID.setText(Integer.toString(id_zak));
                    tfUslugaArtikul.setText(Integer.toString(artikul));
                    tfUslugaIspTot.setText(Integer.toString(usluga_ispol_tot));
                    tfUslugaName.setText(usluga_name);

                    dtm.addRow(new Object[]{id_zak, date_zak, artikul, usluga_name, usluga_kol, usluga_ispol, usluga_ispol_tot, date_ispol, usluga_ostat, isp});
                }
                rs.close();
                stmt.close();

                taLog.append("Запрос к usligi - выполнен успешно \n");
                if (i == 0) {
                    dtm.getDataVector().removeAllElements(); // удаляем все строки
                    dtm.addRow(new Object[]{"", "", "", "", "", "", "", "", "", ""});
                    JFrame jfInfo = new JFrame();
                    JOptionPane.showMessageDialog(jfInfo, "Услуг не обнаружено!");
                    bbUslugaCSV.setEnabled(false);
                    bbUslugaZak.setEnabled(false);
                    bbSaveXLS.setEnabled(false);
                } else {
                    bbUslugaCSV.setEnabled(true);
                    bbUslugaZak.setEnabled(true);
                    bbSaveXLS.setEnabled(true);
                }

            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
                System.err.println("Cannot connect to database server");
            } finally {
                if (conn != null) {
                    try {
                        taLog.append("Завершение запроса к таблице uslugi \n");
                        taLog.append("-------------------------------------------------\n");
                        System.out.println("n***** Let terminate the Connection *****");
                        conn.close();
                        System.out.println("Database connection terminated... ");
                    } catch (SQLException ex) {
                        System.out.println("Error in connection termination!");
                    }
                }
            }
            bbUslugaOtchet.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }//GEN-LAST:event_bbUslugaOtchetActionPerformed

    private void bbUslugaCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbUslugaCSVActionPerformed
        String nameFiles = "temp\\Uslugi.csv";
        String fText = ";Личный №;" + tfUslugaLCod.getText() + ";" + tfUslugaFIO.getText() + "\n\n";
        fText += "№ пп;Дата оплаты;Артикул;Услуга;Оплачено за услугу;Использовано;Использовано всего;Дата использования;Остаток;Исполнитель;\n";

        for (int i = 0; i < tabUslugi.getRowCount(); i++) {
            for (int k = 0; k < tabUslugi.getColumnCount(); k++) {
                fText += tabUslugi.getValueAt(i, k).toString() + ";";
            }
            fText += "\n";
        }

        try {
            File file = new File(nameFiles);
            file.delete();
            file.createNewFile();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(fText);
                writer.close();

            } catch (IOException e) {
                System.out.println("Ошибка-1 при записи в файл");
                taLog.append("Ошибка-1 при записи в файл \n");
            }

            System.out.println("Текст успешно записан в файл.");
        } catch (IOException e) {
            System.out.println("Ошибка-2 при записи в файл");
            taLog.append("Ошибка-2 при записи в файл \n");
        }
        JOptionPane.showMessageDialog(new JFrame(), "Файл:\n\n" + progDir + nameFiles + "\n\nCоздан успешно!");
    }//GEN-LAST:event_bbUslugaCSVActionPerformed

    private void tabUslugiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabUslugiMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tabUslugiMouseClicked

    private void tfUslugaLCodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUslugaLCodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfUslugaLCodActionPerformed

    private void tfUslugaFIOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUslugaFIOActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfUslugaFIOActionPerformed

    private void bbUchetUslugActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbUchetUslugActionPerformed
        // TODO add your handling code here:
        String sLCod = tfKlientLCod.getText();

        if ("".equals(sLCod)) {
            JFrame jfInfo = new JFrame();
            JOptionPane.showMessageDialog(jfInfo, "Необходимо обязательно выбрать Клиента");
        } else {
            tfUslugaLCod.setText(tfKlientLCod.getText());
            tfUslugaFIO.setText(tfKlientFam.getText() + ' ' + tfKlientName.getText() + ' ' + tfKlientOtch.getText());
            bbUslugaOtchet.setEnabled(true);
            tfUslugaID.setText("");
            tfUslugaArtikul.setText("");
            tfUslugaName.setText("");
            tfUslugaOstatok.setText("");
            tfUslugaIspTot.setText("");
            DefaultTableModel dtm1 = (DefaultTableModel) tabUslugi.getModel();
            dtm1.getDataVector().removeAllElements(); // удаляем все строки

            tabPanMain.setSelectedIndex(8);
            bbUslugaOtchetActionPerformed(evt);
        }
    }//GEN-LAST:event_bbUchetUslugActionPerformed

    private void bbNewKlientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbNewKlientActionPerformed
        // TODO add your handling code here:

        conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
            taLog.append("Соединение с БД для client успешно ... \n");

            taLog.append("Запуск запроса к БД для client ... \n");
            stmt = conn.createStatement();
            String query = "select max(lcod) from myservis.client";
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                String sLCod = rs.getString("max(lcod)");
                int LCod = Integer.parseInt(sLCod);
                LCod++;
                sLCod = Integer.toString(LCod);
                tfLCodNew.setText(sLCod);
            }
            rs.close();
            stmt.close();

            if ("".equals(tfKlientLCod.getText())) {
            } else {
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server для client");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД для client\n");
                    taLog.append("-------------------------------------------------\n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated для client... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination для client!");
                }
            }
        }

        //tfLCodNew.setText(null);
        tfSCodNew.setText("1");
        tfFamNew.setText(null);
        tfNamNew.setText(null);
        tfOthNew.setText(null);
        tfAdresNew.setText(null);
        tfTelNew.setText(null);
        tfEmailNew.setText(null);
        tfGorodNew.setText(null);
        tfIndexNew.setText(null);
        taPrimNew.setText(null);
        tfGod.setText("1910");
        tfMon.setText("01");
        tfDay.setText("01");
        
        tfLCodNew.setEnabled(true);
        tfSCodNew.setEnabled(true);
        tfFamNew.setEnabled(true);
        tfNamNew.setEnabled(true);
        tfOthNew.setEnabled(true);
        tfAdresNew.setEnabled(true);
        tfTelNew.setEnabled(true);
        tfEmailNew.setEnabled(true);
        tfGorodNew.setEnabled(true);
        tfIndexNew.setEnabled(true);
        taPrimNew.setEnabled(true);
        tfGod.setEnabled(true);
        tfMon.setEnabled(true);
        tfDay.setEnabled(true);
                
        bbNewKlientReg.setEnabled(true);
    }//GEN-LAST:event_bbNewKlientActionPerformed

    private void tfKKTmodelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKKTmodelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKKTmodelActionPerformed

    private void bbKKTpostTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTpostTestActionPerformed
        // TODO add your handling code here:
        try {
            URL url = new URL("http://support.tentorium.ru/script/test/test_post.php");
            //String postData = "nom_sc=1069000000&lcod=5159&text=Пример";

            String postData = "nom_sc=1069000000&lcod=5159&text=" + tfKKTtext.getText();

            //String serbianString = postData; // What are you doing? 
            //byte[] bytes = serbianString.getBytes(StandardCharsets.UTF_8); 
            //String utf8String = new String(bytes); 
            //String postData2 = new String(bytes);             
            //byte[] converttoBytes = postData.getBytes("UTF-16");
            //postData = new String(converttoBytes, "UTF-8");
            System.out.println("Без преобразования:" + postData);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "UTF-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length()));
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("Content-Language", "ru-RU");
            conn.setUseCaches(false);
            conn.setDoInput(true);

            String postDateNew = "nom_sc=1069000000&lcod=5159&text=" + new String(tfKKTtext.getText().getBytes("UTF-8"), StandardCharsets.UTF_8);
            System.out.println("------------------------------------------------------------------");
            System.out.println("После преобразования:" + postDateNew);
            System.out.println("------------------------------------------------------------------");

            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                //dos.writeBytes(bytes);
                //dos.writeUTF(postData);   // Работает
                dos.writeUTF("nom_sc=1069000000&lcod=5159&text=" + new String(tfKKTtext.getText().getBytes("UTF-8"), StandardCharsets.UTF_8));
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    taKKTlog.append(line + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_bbKKTpostTestActionPerformed

    private void tfKKTtextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKKTtextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKKTtextActionPerformed

    private void bbKKTzapuskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTzapuskActionPerformed
        //String sFile = tfDumpFile.getText();
        String nameFiles = "temp\\dump.bat";
        String sTime,sDate,sTextFileBat;
        String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        sTime = sDateTimeTek.substring(11, 13)+"_"+sDateTimeTek.substring(14, 16)+"_";
        sDate = sDateTimeTek.substring(0, 10)+"_";
        sTextFileBat="cd "+sDumpFileExe+"\nmysqldump -uroot -padmin myservis -h localhost > "+sDumpDirLocal+sDate+sTime+"dumpdate.sql\n";
        sTextFileBat+="copy "+sDumpDirLocal+sDate+sTime+"dumpdate.sql "+sDumpDirHost+sDate+sTime+"dumpdat.sql";
        
                try {
                    Path fileName = Paths.get(nameFiles);
                    Files.write(fileName, sTextFileBat.getBytes(), StandardOpenOption.CREATE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
        try {         
            Process child = Runtime.getRuntime().exec(nameFiles);
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_bbKKTzapuskActionPerformed

    private void tfDumpFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfDumpFileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfDumpFileActionPerformed

    private void bbUslugaZakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbUslugaZakActionPerformed
        // TODO add your handling code here:
        int kolOstatok = Integer.parseInt(tfUslugaOstatok.getText());
        
        if (kolOstatok>0) {
        String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        sDateTimeTek = "\"" + sDateTimeTek + "\"";
        sIspolnitel = "\"" + aktPolLogin + "\"";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);

            taLog.append("Запуск соединения с БД для update в uslugi ... \n");

            stmt = conn.createStatement();

            //usluga_ispol,date_ispol,usluga_ostat
            //tfUslugaID.getText() date_ispol
            int kolOstat = Integer.parseInt(tfUslugaOstatok.getText());
            kolOstat = kolOstat - 1;
            int uspolTot = Integer.parseInt(tfUslugaIspTot.getText());
            uspolTot = uspolTot + 1;
            String sName = "\"" + tfUslugaName.getText() + "\"";

            String query = "insert ignore into myservis.uslugi (id_zak,usluga_ispol,usluga_ostat,date_ispol,isp,lcod,artikul,usluga_ispol_tot,usluga_name) values ("
                    + tfUslugaID.getText() + ",1," + kolOstat + "," + sDateTimeTek + "," + sIspolnitel + "," + tfUslugaLCod.getText() + ","
                    + tfUslugaArtikul.getText() + "," + uspolTot + "," + sName
                    + ");";

            stmt.executeUpdate(query);
            stmt.close();

            taLog.append("Запрос update для uslugi - выполнен успешно \n");

            JOptionPane.showMessageDialog(new JFrame(), "Посещение оформлено успешно!");

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД после погашения услуги \n");
                    taLog.append("------------------------------------------------- \n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        bbUslugaOtchetActionPerformed(evt);
        }else{
            JOptionPane.showMessageDialog(new JFrame(), "Все посещения были учтены ранее!\n\nОстатка оплаченных услуг нет!");
        }
            
    }//GEN-LAST:event_bbUslugaZakActionPerformed

    private void tfUslugaOstatokActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUslugaOstatokActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfUslugaOstatokActionPerformed

    private void tfUslugaIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUslugaIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfUslugaIDActionPerformed

    private void tfUslugaArtikulActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUslugaArtikulActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfUslugaArtikulActionPerformed

    private void tfUslugaNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUslugaNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfUslugaNameActionPerformed

    private void tfUslugaIspTotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUslugaIspTotActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfUslugaIspTotActionPerformed

    private void bbSaveXLSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbSaveXLSActionPerformed
        String nameFiles = "temp\\Учёт_услуг.xls";

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Учёт услуг");

        // создаем шрифт
        HSSFFont font = workbook.createFont();
        HSSFFont font2 = workbook.createFont();

        font.setFontHeightInPoints((short) 16);
        //font.setFontName("Courier New");
        font.setFontName("Arial");
        font.setBold(true);

        font2.setFontHeightInPoints((short) 11);
        font2.setFontName("Arial");
        font2.setBold(true);

        // создаем стиль для ячеек основной таблицы и для ячеек в заголовке таблицы
        HSSFCellStyle style = workbook.createCellStyle();
        HSSFCellStyle style2 = workbook.createCellStyle();
        HSSFCellStyle style3 = workbook.createCellStyle();
        // и применяем к этому стилю жирный шрифт
        style3.setFont(font);  // Для Шапки 
        style2.setFont(font2);  // Для заголовка таблицы

        // Настройка выравнивания стиля
        // Для основной таблицы
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Для заголовка таблицы
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setVerticalAlignment(VerticalAlignment.CENTER);

        // Для шапки
        style3.setAlignment(HorizontalAlignment.LEFT);
        style3.setVerticalAlignment(VerticalAlignment.CENTER);

        HSSFRow row = sheet.createRow((short) 0);

        // прописываем шапку документа
        row.createCell(0).setCellValue("");
        row.createCell(1).setCellValue("Личный №");
        row.createCell(2).setCellValue(tfUslugaLCod.getText());
        row.createCell(3).setCellValue(tfUslugaFIO.getText());

        row.getCell(1).setCellStyle(style3);
        row.getCell(2).setCellStyle(style3);
        row.getCell(3).setCellStyle(style3);
        row.setHeightInPoints(30);

        // стиль для выделения ячейки со всех сторон
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        // стиль-2 для выделения ячейки со всех сторон
        style2.setBorderTop(BorderStyle.THIN);
        style2.setBorderRight(BorderStyle.THIN);
        style2.setBorderBottom(BorderStyle.THIN);
        style2.setBorderLeft(BorderStyle.THIN);

        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style2.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());  // серый цвет фона для заголовка таблицы

        // создаем строку с заголовком
        row = sheet.createRow((short) 2);
        row.createCell(0).setCellValue("№ пп");
        row.createCell(1).setCellValue("Дата оплаты");
        row.createCell(2).setCellValue("Артикул");
        row.createCell(3).setCellValue("Услуга");
        row.createCell(4).setCellValue("Оплачено за услугу");
        row.createCell(5).setCellValue("Использовано");
        row.createCell(6).setCellValue("Использовано всего");
        row.createCell(7).setCellValue("Дата использования");
        row.createCell(8).setCellValue("Остаток");
        row.createCell(9).setCellValue("Исполнитель");

        // Устанавливаем высоту и стили для заголовка таблицы 
        for (int k = 0; k < tabUslugi.getColumnCount(); k++) {
            row.getCell(k).setCellStyle(style2);
            row.setHeightInPoints(22);
        }

        // прописываем основную таблицу
        for (int i = 0; i < tabUslugi.getRowCount(); i++) {
            row = sheet.createRow((short) i + 3);
            for (int k = 0; k < tabUslugi.getColumnCount(); k++) {
                //tabUslugi.getValueAt(i, k).toString() + ";";
                row.createCell(k).setCellValue(tabUslugi.getValueAt(i, k).toString());
                if (k == 8) {
                    row.getCell(k).setCellStyle(style2);
                } else {
                    row.getCell(k).setCellStyle(style);
                }
                row.setHeightInPoints(18);
            }
        }

        // Устанавливаем автоширину для всех колонок
        for (int k = 0; k < tabUslugi.getColumnCount(); k++) {
            sheet.autoSizeColumn(k);
        }

        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(nameFiles);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            workbook.write(fileOut);
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fileOut.close();
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            workbook.close();
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Your excel file has been generated!");

        JOptionPane.showMessageDialog(new JFrame(), "Файл:\n\n" + progDir + nameFiles + "\n\nCоздан успешно!");
    }//GEN-LAST:event_bbSaveXLSActionPerformed

    private void tfpPasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfpPasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfpPasActionPerformed

    private void bbKassaExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKassaExcelActionPerformed
        String sDat;
        if (chKassaOtchetMon.isSelected()) {
            sDat = tfKassaGod.getText() + "_" + cbKassaMon.getItemAt(cbKassaMon.getSelectedIndex());
        } else {
            sDat = tfKassaGod.getText() + "_" + Integer.toString(cbKassaMon.getSelectedIndex() + 1) + "_" + tfKassaDat.getText();
        }

        String nameFiles = "temp\\Касса_" + sDat + ".xls";

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Касса");

        // создаем шрифт
        HSSFFont font = workbook.createFont();
        HSSFFont font2 = workbook.createFont();

        font.setFontHeightInPoints((short) 14);
        //font.setFontName("Courier New");
        font.setFontName("Arial");
        font.setBold(true);

        font2.setFontHeightInPoints((short) 11);
        font2.setFontName("Arial");
        font2.setBold(true);

        // создаем стиль для ячеек основной таблицы и для ячеек в заголовке таблицы
        HSSFCellStyle style = workbook.createCellStyle();
        HSSFCellStyle style2 = workbook.createCellStyle();
        HSSFCellStyle style3 = workbook.createCellStyle();
        // и применяем к этому стилю жирный шрифт 
        style3.setFont(font);  // Для Шапки 
        style2.setFont(font2);  // Для заголовка таблицы

        // Настройка выравнивания стиля
        // Для основной таблицы
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Для заголовка таблицы
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setVerticalAlignment(VerticalAlignment.CENTER);

        // Для шапки
        style3.setAlignment(HorizontalAlignment.LEFT);
        style3.setVerticalAlignment(VerticalAlignment.CENTER);

        HSSFRow row = sheet.createRow((short) 0);

        // прописываем шапку документа
        row.createCell(0).setCellValue("Дата:");
        row.createCell(1).setCellValue(sDat);
        row.createCell(3).setCellValue("Сумма на начало дня:");
        row.createCell(4).setCellValue(tfKassaOstatok.getText());

        row.getCell(0).setCellStyle(style3);
        row.getCell(1).setCellStyle(style3);
        row.getCell(3).setCellStyle(style3);
        row.getCell(4).setCellStyle(style3);
        row.setHeightInPoints(30);

        // стиль для выделения ячейки со всех сторон
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        // стиль-2 для выделения ячейки со всех сторон
        style2.setBorderTop(BorderStyle.THIN);
        style2.setBorderRight(BorderStyle.THIN);
        style2.setBorderBottom(BorderStyle.THIN);
        style2.setBorderLeft(BorderStyle.THIN);

        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style2.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());  // серый цвет фона для заголовка таблицы

        row = sheet.createRow((short) 2);
        row.createCell(0).setCellValue("Приход:");
        row.getCell(0).setCellStyle(style2);

        // создаем строку с заголовком
        row = sheet.createRow((short) 3);
        row.createCell(0).setCellValue("№ пп");
        row.createCell(1).setCellValue("Дата");
        row.createCell(2).setCellValue("Id Клиента");
        row.createCell(3).setCellValue("ФИО Клиента");
        row.createCell(4).setCellValue("Сумма Нал");
        row.createCell(5).setCellValue("Сумма QR-код");
        row.createCell(6).setCellValue("Чек");
        row.createCell(7).setCellValue("№ нак.");
        row.createCell(8).setCellValue("Исполнитель");
        row.createCell(9).setCellValue("Примечание");

        // Устанавливаем высоту и стили для заголовка таблицы 
        for (int k = 0; k < tabKasPrihod.getColumnCount(); k++) {
            row.getCell(k).setCellStyle(style2);
            row.setHeightInPoints(22);
        }
        // прописываем основную таблицу по приходу
        for (int i = 0; i < tabKasPrihod.getRowCount(); i++) {
            row = sheet.createRow((short) i + 4);
            for (int k = 0; k < tabKasPrihod.getColumnCount(); k++) {
                row.createCell(k).setCellValue(tabKasPrihod.getValueAt(i, k).toString());
                row.getCell(k).setCellStyle(style);
                row.setHeightInPoints(18);
            }
        }

        row = sheet.createRow((short) 5 + tabKasPrihod.getRowCount());
        row.createCell(0).setCellValue("Расход:");
        row.getCell(0).setCellStyle(style2);

        row = sheet.createRow((short) 6 + tabKasPrihod.getRowCount());
        row.createCell(0).setCellValue("№ пп");
        row.createCell(1).setCellValue("Дата");
        row.createCell(2).setCellValue("Личный №");
        row.createCell(3).setCellValue("ФИО");
        row.createCell(4).setCellValue("Сумма Нал");
        row.createCell(5).setCellValue("Сумма QR-код");
        row.createCell(6).setCellValue("Чек");
        row.createCell(7).setCellValue("№ нак.");
        row.createCell(8).setCellValue("Исполнитель");
        row.createCell(9).setCellValue("Примечание");

        // Устанавливаем высоту и стили для заголовка таблицы 
        for (int k = 0; k < tabKasPrihod.getColumnCount(); k++) {
            row.getCell(k).setCellStyle(style2);
            row.setHeightInPoints(22);
        }

        // прописываем основную таблицу по расходу
        for (int i = 0; i < tabKasRashod.getRowCount(); i++) {
            row = sheet.createRow((short) i + 7 + tabKasPrihod.getRowCount());
            for (int k = 0; k < tabKasRashod.getColumnCount(); k++) {
                if (k < 5) {
                    row.createCell(k).setCellValue(tabKasRashod.getValueAt(i, k).toString());
                    row.getCell(k).setCellStyle(style);
                    row.setHeightInPoints(18);
                } else {
                    row.createCell(k + 3).setCellValue(tabKasRashod.getValueAt(i, k).toString());
                    row.getCell(k + 3).setCellStyle(style);
                    row.setHeightInPoints(18);
                }
            }
        }

        row = sheet.createRow((short) tabKasRashod.getRowCount() + 8 + tabKasPrihod.getRowCount());
        row.createCell(3).setCellValue("Остаток:");
        row.createCell(4).setCellValue(tfKassaTek.getText());
        row.getCell(3).setCellStyle(style3);
        row.getCell(4).setCellStyle(style3);
        row.setHeightInPoints(30);

        // Устанавливаем автоширину для всех колонок
        for (int k = 0; k < tabKasPrihod.getColumnCount(); k++) {
            sheet.autoSizeColumn(k);
        }

        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(nameFiles);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            workbook.write(fileOut);
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fileOut.close();
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            workbook.close();
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Your excel file has been generated!");

        JOptionPane.showMessageDialog(new JFrame(), "Файл:\n\n" + progDir + nameFiles + "\n\nCоздан успешно!");
    }//GEN-LAST:event_bbKassaExcelActionPerformed

    private void bbBDzakazXLSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbBDzakazXLSActionPerformed
        String nameFiles = "temp\\Заказы-Excel.xls";

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Заказы");

        // создаем шрифт
        HSSFFont font = workbook.createFont();
        HSSFFont font2 = workbook.createFont();

        font.setFontHeightInPoints((short) 14);
        //font.setFontName("Courier New");
        font.setFontName("Arial");
        font.setBold(true);

        font2.setFontHeightInPoints((short) 11);
        font2.setFontName("Arial");
        font2.setBold(true);

        // создаем стиль для ячеек основной таблицы и для ячеек в заголовке таблицы
        HSSFCellStyle style = workbook.createCellStyle();
        HSSFCellStyle style2 = workbook.createCellStyle();
        HSSFCellStyle style3 = workbook.createCellStyle();
        // и применяем к этому стилю жирный шрифт
        style3.setFont(font);  // Для Шапки 
        style2.setFont(font2);  // Для заголовка таблицы

        // Настройка выравнивания стиля
        // Для основной таблицы
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Для заголовка таблицы
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setVerticalAlignment(VerticalAlignment.CENTER);

        // Для шапки
        style3.setAlignment(HorizontalAlignment.LEFT);
        style3.setVerticalAlignment(VerticalAlignment.CENTER);

        HSSFRow row = sheet.createRow((short) 0);

        //прописываем шапку документа
        //row.createCell(0).setCellValue("");
        //row.createCell(1).setCellValue("Личный №");
        //row.createCell(2).setCellValue(tfUslugaLCod.getText());
        //row.createCell(3).setCellValue(tfUslugaFIO.getText());
        //row.getCell(1).setCellStyle(style3);
        //row.getCell(2).setCellStyle(style3);
        //row.getCell(3).setCellStyle(style3);
        //row.setHeightInPoints(30);
        // стиль для выделения ячейки со всех сторон
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        // стиль-2 для выделения ячейки со всех сторон
        style2.setBorderTop(BorderStyle.THIN);
        style2.setBorderRight(BorderStyle.THIN);
        style2.setBorderBottom(BorderStyle.THIN);
        style2.setBorderLeft(BorderStyle.THIN);

        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style2.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());  // серый цвет фона для заголовка таблицы

        // создаем строку с заголовком
        row = sheet.createRow((short) 2);
        row.createCell(0).setCellValue("№ пп");
        row.createCell(1).setCellValue("Дата");
        row.createCell(2).setCellValue("№ заказа");
        row.createCell(3).setCellValue("Id Клиента");
        row.createCell(4).setCellValue("ФИО Клиента");
        row.createCell(5).setCellValue("Сумма");
        row.createCell(6).setCellValue("Скидка-%");
        row.createCell(7).setCellValue("Нал/QR");
        row.createCell(8).setCellValue("Чек");
        row.createCell(9).setCellValue("Исполнитель");

        // Устанавливаем высоту и стили для заголовка таблицы 
        for (int k = 0; k < tabBDzakaz.getColumnCount(); k++) {
            row.getCell(k).setCellStyle(style2);
            row.setHeightInPoints(22);
        }

        // прописываем основную таблицу
        for (int i = 0; i < tabBDzakaz.getRowCount(); i++) {
            row = sheet.createRow((short) i + 3);
            for (int k = 0; k < tabBDzakaz.getColumnCount(); k++) {
                row.createCell(k).setCellValue(tabBDzakaz.getValueAt(i, k).toString());
                row.getCell(k).setCellStyle(style);
                row.setHeightInPoints(18);
            }
        }

        row = sheet.createRow((short) tabBDzakaz.getRowCount() + 4);
        row.createCell(4).setCellValue("Итого сумма:");
        row.createCell(5).setCellValue(tfBDZakazSumma.getText());
        row.getCell(4).setCellStyle(style3);
        row.getCell(5).setCellStyle(style3);
        row.setHeightInPoints(30);
        
        row = sheet.createRow((short) tabBDzakaz.getRowCount() + 5);
        row.createCell(4).setCellValue("Сумма наличными:");
        row.createCell(5).setCellValue(tfBDZakazNal.getText());
        row.getCell(4).setCellStyle(style3);
        row.getCell(5).setCellStyle(style3);
        row.setHeightInPoints(30);

        row = sheet.createRow((short) tabBDzakaz.getRowCount() + 6);
        row.createCell(4).setCellValue("Сумма QR-код:");
        row.createCell(5).setCellValue(tfBDZakazQR.getText());
        row.getCell(4).setCellStyle(style3);
        row.getCell(5).setCellStyle(style3);
        row.setHeightInPoints(30);

        // Устанавливаем автоширину для всех колонок
        for (int k = 0; k < tabBDzakaz.getColumnCount(); k++) {
            sheet.autoSizeColumn(k);
        }

        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(nameFiles);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            workbook.write(fileOut);
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fileOut.close();
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            workbook.close();
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Your excel file has been generated!");

        JOptionPane.showMessageDialog(new JFrame(), "Файл:\n\n" + progDir + nameFiles + "\n\nCоздан успешно!");
    }//GEN-LAST:event_bbBDzakazXLSActionPerformed

    private void bbKKTpythonTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTpythonTestActionPerformed
        // TODO add your handling code here:
        try (FileWriter writer = new FileWriter("python\\command.ini", false)) //try(FileWriter writer = new FileWriter("command.ini", false))
        {
            // запись всей строки
            String text = "OpenSession";
            writer.write(text);
            // запись по символам
            //writer.append('\n');
            //writer.append('E');             
            //writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        /*
        String sFile = "python\\KKTpyton.py"; 
        String param =""; 
        try {         
            Process child = Runtime.getRuntime().exec(sFile+param);
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        } 
         */
        String Script_Path = "python\\KKTpyton.py";
        ProcessBuilder Process_Builder = new ProcessBuilder("python", Script_Path)
                .inheritIO();

        Process Demo_Process = null;
        try {
            Demo_Process = Process_Builder.start();
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Demo_Process.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }

        BufferedReader Buffered_Reader = new BufferedReader(
                new InputStreamReader(
                        Demo_Process.getInputStream()
                ));
        String Output_line = "";

        try {
            while ((Output_line = Buffered_Reader.readLine()) != null) {
                System.out.println(Output_line);
            }
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_bbKKTpythonTestActionPerformed

    private void bbKKTpreobrazovanieTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTpreobrazovanieTestActionPerformed
        // TODO add your handling code here:
        /*   
      try {

      String priwet = new String(
      "\u041F"+"\u0440"+"\u0438"+
      "\u0432"+"\u0435"+"\u0442"+"!");

      byte[] utf8Bytes = priwet.getBytes("UTF8");
      System.out.println(utf8Bytes);
      String priwet2 = new String(utf8Bytes,"UTF8");      
      System.out.println(priwet2);
      
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }        
        String result = "";
        String inString = "жарко";
        for (int i = 0; i < inString.length(); i++) {
            Integer charCode = (int)inString.charAt(i);
            result += "\\u" + Integer.toHexString(charCode) ;
        }
    System.out.println(result);

                String str2 = "";
                String inString2 = "Привет! Мир!";
                for (int i = 0; i < inString2.length(); i++) {
                    Integer charCode = (int)inString2.charAt(i);
                    str2 += "\\u0" + Integer.toHexString(charCode) ;
                }
                
                System.out.println(str2);    
         */
        String result = null;
        String inString = "Привет Мир!";
        for (int i = 0; i < inString.length(); i++) {
            char charCode = inString.charAt(i);
            result += unicodeEscaped(charCode);
        }
        System.out.println(result);
        System.out.println("----------------------------------------");

        String inText = "Привет Мир!";
        String result2;
        result2 = strPreobraz(inText);
        /*
        result = "";
        String rez;
        for (int i = 0; i < inText.length(); i++) {
            //char charCode = inString.charAt(i);
            char c = inText.charAt(i);
            //System.out.println(char2);
            rez=strKiril(c);
            if ("1".equals(rez)) {            
            result += String.format("\\u%04x", (int) inText.charAt(i));
            }else{
            result += c;    
            }
        }
         */
        System.out.println(result2);

    }//GEN-LAST:event_bbKKTpreobrazovanieTestActionPerformed

    private void bbKKTgetStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTgetStatusActionPerformed
        taKKTlog.append("Запрос на проверку статуса ККТ ...\n");
        try {
            URL urlKKT = new URL(httpKKM);
            textJSON = "{\"sessionKey\": \"" + sessionKey + "\",\"command\": \"GetStatus\"}";

            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setConnectTimeout(5000);
            connKKT.setDoOutput(true);
            connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
            connKKT.setUseCaches(false);

            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                dos.writeBytes(textJSON);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    taKKTlog.append(line + "\n");
                    taInfoMain.append(line + "\n");
                    tfKKTotvet.setText(line);
                    System.out.println(line);
                    System.out.println("----------------------------------------------");

                    String sOtwet = tfKKTotvet.getText();
                    
                    String sResult,sDescription;
                    
                    int index1,index2;
                    index1 = line.indexOf("result");
                    index1 = index1 + 8;

                    String sRezultNew1 = line.substring(index1, index1 + 4);
                    sRezultNew1 = sParsingDescription(sRezultNew1);
                    taKKTlog.setText("sRezultNew: "+sRezultNew1+"\n");
                    sResult=sRezultNew1;
                    

                    if (Integer.parseInt(sRezultNew1) == 0){
                        sDescription="";
                        index1 = line.indexOf("isOpen");
                        index1 = index1 + 8;
                    
                        String sRezultNew2 = line.substring(index1, index1 + 4);
                        taKKTlog.setText("isOpen: "+sRezultNew2+ "\n");
                     
                        if ("fals".equals(sRezultNew2)) {
                            sSmena = "Закрыта";
                        } else {
                            sSmena = "Открыта";
                        }
                        
                    }else{
                        sSmena="------";
                        index1 = line.indexOf("description");
                        taKKTlog.append("Начало description index1: "+index1+ "\n");
                        int kDlin=line.length();
                        taKKTlog.append("Общая длина kDlin: "+kDlin+ "\n");
                        index1 = index1 + 14;
                        taKKTlog.append("Начало значения description (+14) index1: "+index1+ "\n");                        
                        taKKTlog.append("Предполагаемый финишь description (kDlin): "+kDlin+ "\n");
                        
                        String sPredDescription = line.substring(index1, kDlin);
                        sDescription = sParsingDescription(sPredDescription);
                        
                        System.out.println("sPredDescription: "+sPredDescription);
                        System.out.println("sDescription: "+sDescription);
                        
                        taKKTlog.append("sPredDescription: "+sPredDescription+ " ("+kDlin+")"+"\n");
                        taKKTlog.append("sDescription: "+sDescription + "\n");
                        
                    }
                    
                    /*
                    //---------------------------------------------
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(sOtwet);

                    long sLong = (long) jsonObject.get("result");                    
                    sResult = Long.toString(sLong);
                     
                    String sDescription = (String) jsonObject.get("description");
                    
                    JSONObject jsonObject2 = (JSONObject) jsonObject.get("shiftInfo");
                    java.lang.Boolean sIsOpen = (Boolean) jsonObject2.get("isOpen");
                    //---------------------------------------------------------    
                    
                    if (false == sIsOpen) {
                        sSmena = "Закрыта";
                    } else {
                        sSmena = "Открыта";
                    }
                     */
                    
                    tfRezultKKT.setText(sResult);
                    //tfRezultKKT.setText("Description:"+sDescription);
                    //System.out.println(sIsOpen + "/" + sResult + "/" + sDescription);
                   
                    lbKKTsmena.setText(sSmena);
                    System.out.println("Смена: " + sSmena);
                    taKKTlog.append("Смена: " + sSmena + "\n");
                    //taInfoMain.append("Смена: " + sSmena + "\n");
                    taInfoMain.append("Смена: " + sSmena + "\n");
                }
           } 
            //catch (ParseException ex) 
           //{
           //     Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
           // }
        } catch (IOException e) {
        }
        taKKTlog.append("Проверка статуса ККТ произведено успешно!\n" + sTire70);
    }//GEN-LAST:event_bbKKTgetStatusActionPerformed

    private void bbKKTdobriyDenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTdobriyDenActionPerformed
        // TODO add your handling code here:
        try {
            URL urlKKT = new URL(httpKKM);
            textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"PrintText\", \"text\": \"" + strPreobraz("Добрый день!  Смена: " + sSmena) + "\"}";

            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setDoOutput(true);
            connKKT.setUseCaches(false);
            //connKKT.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connKKT.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connKKT.setRequestProperty("Content-Type", "application/json");
            connKKT.setRequestProperty("charset", "UTF-8");
            //connKKT.setRequestProperty("Content-Length", Integer.toString(postData.length()));
            connKKT.setRequestProperty("Accept-Charset", "UTF-8");
            //connKKT.setRequestProperty("Content-Language", "ru-RU");

            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                dos.writeBytes(textJSON);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    taKKTlog.append(line + "\n");
                    tfKKTotvet.setText(line);
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
        }
        taKKTlog.append("Запрос Добрый день - выполнен успешно!\n" + sTire70);
    }//GEN-LAST:event_bbKKTdobriyDenActionPerformed

    private void bbKKTsmenaOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTsmenaOpenActionPerformed
        // TODO add your handling code here:
        taKKTlog.append("Запрос на открытие смены ...\n");
        if ("open".equals(sSession)) {
            try {
                URL urlKKT = new URL(httpKKM);
                textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"OpenShift\", \"printDoc\": true, \"cashierInfo\": {\"+cashierName+\": \"" + strPreobraz(aktPolKKT) + "\", \"cashierINN\": \"" + sKKMkassirINN + "\"} }";

                HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
                connKKT.setRequestMethod("POST");
                connKKT.setDoOutput(true);
                connKKT.setConnectTimeout(5000);
                connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
                connKKT.setUseCaches(false);

                try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                    dos.writeBytes(textJSON);
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                    String line;
                    while ((line = br.readLine()) != null) {
                        taKKTlog.append(line + "\n");
                        tfKKTotvet.setText(line);
                        System.out.println(line);
                    }

                    String sOtwet = tfKKTotvet.getText();

                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = (JSONObject) jsonParser.parse(sOtwet);
                    } catch (ParseException ex) {
                        Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // получение строки из объекта
                    long sLong = (long) jsonObject.get("result");
                    System.out.println("rezult:" + sLong);

                    if (sLong == 0) {
                        sSmena = "Открыта";
                    } else {
                        sSmena = "Закрыта";
                    }
                    lbKKTsmena.setText(sSmena);
                    System.out.println("Смена: " + sSmena);
                    taKKTlog.append("Смена: " + sSmena + "\n");
                    taInfoMain.append("Смена: " + sSmena + "\n");

                    String sDescription = (String) jsonObject.get("description");
                    taKKTlog.append("Description:" + sDescription + "\n");
                }
            } catch (IOException e) {
            }
            taKKTlog.append("Открытие смены произведено успешно!\n" + sTire70);
        } else {
            taKKTlog.append("Открытие смены не произведено успешно! Т.к. не открыта сессия.\n" + sTire70);
        }

    }//GEN-LAST:event_bbKKTsmenaOpenActionPerformed

    private void bbKKTsmenaCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTsmenaCloseActionPerformed
        // TODO add your handling code here:
        try {
            URL urlKKT = new URL(httpKKM);
            textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"CloseShift\", \"printDoc\": true, \"cashierInfo\": {\"+cashierName+\": \"" + strPreobraz(aktPolKKT) + "\", \"cashierINN\": \"" + sKKMkassirINN + "\"} }";

            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setDoOutput(true);
            connKKT.setConnectTimeout(5000);
            connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
            connKKT.setUseCaches(false);

            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                dos.writeBytes(textJSON);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    taKKTlog.append(line + "\n");
                    tfKKTotvet.setText(line);
                    String sOtwet = tfKKTotvet.getText();
                    System.out.println(line);

                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = (JSONObject) jsonParser.parse(sOtwet);
                    } catch (ParseException ex) {
                        Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // получение строки из объекта
                    long sLong = (long) jsonObject.get("result");
                    System.out.println("rezult:" + sLong);

                    if (sLong == 0) {
                        sSmena = "Закрыта";
                    } else {
                        sSmena = "Открыта";
                    }
                    lbKKTsmena.setText(sSmena);
                    System.out.println("Смена: " + sSmena);
                    taKKTlog.append("Смена: " + sSmena + "\n");
                    taInfoMain.append("Смена: " + sSmena + "\n");

                    String sDescription = (String) jsonObject.get("description");
                    taKKTlog.append("Description:" + sDescription + "\n");
                }
            }
        } catch (IOException e) {
        }
        taKKTlog.append("Закрытие смены произведено успешно!\n" + sTire70);

    }//GEN-LAST:event_bbKKTsmenaCloseActionPerformed

    private void bbKKTopenCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTopenCheckActionPerformed
        // TODO add your handling code here:
        try {
            URL urlKKT = new URL(httpKKM);
            // checkType: 0-Приход, 1-Возврат прихода
            // taxSystem: 0-Общая, 1- Упрощенная доходы
            textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"OpenCheck\", \"checkType\": 0, \"printDoc\": true, \"taxSystem\": 1 ,\"cashierInfo\": {\"+cashierName+\": \"" + strPreobraz(aktPolKKT) + "\", \"cashierINN\": \"" + sKKMkassirINN + "\"} }";

            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setDoOutput(true);
            connKKT.setConnectTimeout(5000);
            connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
            connKKT.setUseCaches(false);

            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                dos.writeBytes(textJSON);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    taKKTlog.append(line + "\n");
                    tfKKTotvet.setText(line);
                    String sOtwet = tfKKTotvet.getText();
                    System.out.println(line);

                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = (JSONObject) jsonParser.parse(sOtwet);
                    } catch (ParseException ex) {
                        Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    long lRezult = (long) jsonObject.get("result");
                    if (lRezult == 0) {
                        taKKTlog.append("Открытие Чека произведено успешно!\n");
                    } else {
                        taKKTlog.append("Открытие Чека не произведено.\n");
                    }

                    String sDescription = (String) jsonObject.get("description");
                    taKKTlog.append("Description:" + sDescription + "\n");
                }
            }
        } catch (IOException e) {
        }
        taKKTlog.append("Операция Открытие Чека завершена!\n" + sTire70);
    }//GEN-LAST:event_bbKKTopenCheckActionPerformed

    private void bbKKTaddGoodsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTaddGoodsActionPerformed
        // Выводим для контроля в ТекстАрия(textArie)
        taKKTtowar.setText(null);
        taKKTtowar.append(sTire180);
        taKKTtowar.append("Итоговый JSON, подготовленный для команды AddGoods:\n");
        taKKTtowar.append(sTire180);
        for (int i = 0; i < tabZakaz.getRowCount(); i++) {
            textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"AddGoods\"," + masTowarKKT[i] + "}";
            taKKTtowar.append(textJSON + "\n");
        }
        // Создаем файл лога:
        try {
            Path fileName = Paths.get("temp\\log_kkt_merkuriy_towar.txt");
            Files.write(fileName, taKKTtowar.getText().getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Основная часть:
        try {            
            for (int i = 0; i < tabZakaz.getRowCount(); i++) {
                URL urlKKT = new URL(httpKKM);            
                HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
                connKKT.setRequestMethod("POST");
                connKKT.setDoOutput(true);
                connKKT.setConnectTimeout(10000);
                connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
                connKKT.setUseCaches(false);
                
                textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"AddGoods\"," + masTowarKKT[i] + "}";

                try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                    dos.writeBytes(textJSON);
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                    String line;
                    while ((line = br.readLine()) != null) {
                        taKKTlog.append(line + "\n");
                        tfKKTotvet.setText(line);
                        String sOtwet = tfKKTotvet.getText();
                        System.out.println(line);

                        JSONParser jsonParser = new JSONParser();
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = (JSONObject) jsonParser.parse(sOtwet);
                        } catch (ParseException ex) {
                            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        long lRezult = (long) jsonObject.get("result");
                        if (lRezult == 0) {
                            taKKTlog.append("Добавления товара произведено успешно.\n");
                        } else {
                            taKKTlog.append("Добавления товара не произведено!!!\n");
                        }

                        String sDescription = (String) jsonObject.get("description");
                        taKKTlog.append("Description:" + sDescription + "\n");
                    }
                }
            }
        } catch (IOException e) {
        }
        taKKTlog.append("Операция добавления товара завершена!\n" + sTire70);
    }//GEN-LAST:event_bbKKTaddGoodsActionPerformed

    private void bbKKTcloseCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTcloseCheckActionPerformed
        // TODO add your handling code here:
        try {
            URL urlKKT = new URL(httpKKM);
            //String ssCash;
            //ssCash="";
            //String ssSummaKKM;

            int summaKKM = Integer.parseInt(tfSummaZak.getText());
            summaKKM = summaKKM * 100;

            //ssSummaKKM="100";
            //TextJSON:='{"sessionKey": "'+sessionKey+'", "command": "CloseCheck"'+ssEmailKKT+', "payment": { "'+ssCash+'": '+ssSummaKKM+' } }';
            if (chNal.isSelected()) {
                textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"CloseCheck\", \"payment\": { \"cash\": " + summaKKM + " } }";
            } else {
                textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"CloseCheck\", \"payment\": { \"ecash\": " + summaKKM + " } }";
            }

            taKKTlog.append(textJSON + "\n");

            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setConnectTimeout(5000);
            connKKT.setDoOutput(true);
            connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
            connKKT.setUseCaches(false);

            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                dos.writeBytes(textJSON);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    taKKTlog.append(line + "\n");
                    tfKKTotvet.setText(line);
                    String sOtwet = tfKKTotvet.getText();
                    System.out.println(line);

                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = (JSONObject) jsonParser.parse(sOtwet);
                    } catch (ParseException ex) {
                        Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    long lRezult = (long) jsonObject.get("result");
                    if (lRezult == 0) {
                        taKKTlog.append("Закрытие Чека произведено успешно!\n");
                    } else {
                        taKKTlog.append("Закрытие Чека не произведено.\n");
                    }

                    String sDescription = (String) jsonObject.get("description");
                    taKKTlog.append("Description:" + sDescription + "\n");
                }
            }
        } catch (IOException e) {
        }
        taKKTlog.append("Операция Закрытие Чека произведена!\n" + sTire70);
    }//GEN-LAST:event_bbKKTcloseCheckActionPerformed

    private void bbKKTtowarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKKTtowarActionPerformed
        // TODO add your handling code here:
        bbKKTtowar.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        taKKTlog.setText(null);
        taKKTlog.append(sTire99);
        String sDateTimeTek = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        taKKTlog.append("Дата: " + sDateTimeTek + " Запрос на Обслуживание товара:\n");
        taKKTlog.append(sTire99);

        // тестируем Добавляем товар  
        //bbKKTaddGoodsActionPerformed(evt);
        bbKKTsessionOpenActionPerformed(evt);     // Открываем Сессию

        if ("open".equals(sSession)) {

            bbKKTopenCheckActionPerformed(evt);   // Открываем Чек
            bbKKTaddGoodsActionPerformed(evt);    // Добавляем товар
            bbKKTcloseCheckActionPerformed(evt);  // Закрываем Чек

            bbKKTsessionCloseActionPerformed(evt); // Закрываем Сессию

            starTime1 = System.currentTimeMillis();

            try {
                Path fileName = Paths.get("temp\\log_kkt_merkuriy_tot.txt");
                Files.write(fileName, taKKTlog.getText().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bbKKTtowar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_bbKKTtowarActionPerformed

    private void cbZakazGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbZakazGroupActionPerformed
        // TODO add your handling code here:
        int wib = cbZakazGroup.getSelectedIndex();
        //masPNom[g];
        //tabTowar.rowAtPoint(masUsluga[wib]);

        //tabTowar.setLocation(2,3);
        //tabTowar.setRowSelectionInterval(3, 4);
        //table.setRowSelectionInterval(row1,row2) 
        //masUsluga[wib]
    }//GEN-LAST:event_cbZakazGroupActionPerformed

    private void chZakazWibActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chZakazWibActionPerformed
        // TODO add your handling code here:
        int wib = cbZakazGroup.getSelectedIndex();
        //masPNom[g];
        //tabTowar.rowAtPoint(masUsluga[wib]);

        //tabTowar.setLocation(2,3);
        tabTowar.setRowSelectionInterval(masPNom[wib], masPNom[wib]);
        //table.setRowSelectionInterval(row1,row2) 

        //masUsluga[wib]

    }//GEN-LAST:event_chZakazWibActionPerformed

    private void tfGodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfGodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfGodActionPerformed

    private void tfGodKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfGodKeyTyped
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfGodKeyTyped

    private void tfMonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfMonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfMonActionPerformed

    private void tfMonKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfMonKeyTyped
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfMonKeyTyped

    private void tfDayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfDayActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfDayActionPerformed

    private void tfDayKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfDayKeyTyped
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfDayKeyTyped

    private void tfKlientGodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientGodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientGodActionPerformed

    private void tfKlientGodKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfKlientGodKeyTyped
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfKlientGodKeyTyped

    private void tfKlientMonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientMonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientMonActionPerformed

    private void tfKlientMonKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfKlientMonKeyTyped
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfKlientMonKeyTyped

    private void tfKlientDayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfKlientDayActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfKlientDayActionPerformed

    private void tfKlientDayKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfKlientDayKeyTyped
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_tfKlientDayKeyTyped

    private void tabPanMainAncestorRemoved(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_tabPanMainAncestorRemoved
        // TODO add your handling code here:
    }//GEN-LAST:event_tabPanMainAncestorRemoved

    private void chKlientTotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chKlientTotActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chKlientTotActionPerformed

    private void bbKlientCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKlientCSVActionPerformed
        String nameFiles = "temp\\Клиенты.csv";
        String fText;
        fText = "Фамилия;Имя;Отчество;Телефон;Номер id;Город;\n";

        for (int i = 0; i < tabFIO.getRowCount(); i++) {
            for (int k = 0; k < tabFIO.getColumnCount(); k++) {
                fText +=tabFIO.getValueAt(i, k).toString() + ";";
            }
            fText +="\n";
        }

        try {
            File file = new File(nameFiles);
            file.delete();
            file.createNewFile();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(fText);
                writer.close();

            } catch (IOException e) {
                System.out.println("Ошибка-1 при записи в файл");
                taLog.append("Ошибка-1 при записи в файл \n");
                //e.printStackTrace();
            }

            System.out.println("Текст успешно записан в файл.");
        } catch (IOException e) {
            System.out.println("Ошибка-2 при записи в файл");
            taLog.append("Ошибка-2 при записи в файл \n");
            //e.printStackTrace();
        }

        JOptionPane.showMessageDialog(new JFrame(), "Файл:\n\n" + progDir + nameFiles + "\n\nCоздан успешно!");
    }//GEN-LAST:event_bbKlientCSVActionPerformed

    private void bbKlientCSVtotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKlientCSVtotActionPerformed
                bbKlientCSVtot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String nameFiles = "temp\\Клиенты csv.csv";
        String fText;
        fText = "Номер id;id учета;Фамилия;Имя;Отчество;Телефон;E-mail;Индекс;Город;Адрес;Дата рождения;\n";
        
        conn = null;
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
            stmt = conn.createStatement();

            String query;
            query = "select lcod,scod,f,i,o,tel,email,poch_index,gorod_name,adres,date_rog from myservis.client order by f,i,i";
            rs = stmt.executeQuery(query);

            int i = 0;
            while (rs.next()) {
                i++;
                String lCod = rs.getString("lcod");
                String sCod = rs.getString("scod");
                String fam = rs.getString("f");
                String name = rs.getString("i");
                String otch = rs.getString("o");
                String sTel = rs.getString("tel");
                String sEmail = rs.getString("email");
                
                String sIndex = rs.getString("poch_index");
                String sGorod = rs.getString("gorod_name");
                String sAdres = rs.getString("adres");
                String sDate_rog = rs.getString("date_rog");                
                
                fText += lCod+";"+sCod+";"+fam+";"+name+";"+otch+";"+sTel+";"+sEmail+";"+sIndex+";"+sGorod+";"+sAdres+";"+sDate_rog+";\n";
            }
            rs.close();
            stmt.close();            
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД cleent ... \n");
                    taLog.append("------------------------------------------------- \n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        
        try {
            File file = new File(nameFiles);
            file.delete();
            file.createNewFile();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(fText);
                writer.close();

            } catch (IOException e) {
                System.out.println("Ошибка-1 при записи в файл");
            }

            System.out.println("Текст успешно записан в файл.");
        } catch (IOException e) {
            System.out.println("Ошибка-2 при записи в файл");
        }
        
        bbKlientCSVtot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JOptionPane.showMessageDialog(new JFrame(), "Файл:\n\n" + progDir + nameFiles + "\n\nCоздан успешно!");
    }//GEN-LAST:event_bbKlientCSVtotActionPerformed

    private void bbKlientExcelTotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbKlientExcelTotActionPerformed
        String nameFiles = "temp\\Клиенты Excel.xls";

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Клиенты");

        // создаем шрифт
        HSSFFont font = workbook.createFont();
        HSSFFont font2 = workbook.createFont();

        font.setFontHeightInPoints((short) 16);
        //font.setFontName("Courier New");
        font.setFontName("Arial");
        font.setBold(true);

        font2.setFontHeightInPoints((short) 11);
        font2.setFontName("Arial");
        font2.setBold(true);

        // создаем стиль для ячеек основной таблицы и для ячеек в заголовке таблицы
        HSSFCellStyle style = workbook.createCellStyle();
        HSSFCellStyle style2 = workbook.createCellStyle();
        HSSFCellStyle style3 = workbook.createCellStyle();
        // и применяем к этому стилю жирный шрифт
        style3.setFont(font);  // Для Шапки 
        style2.setFont(font2);  // Для заголовка таблицы

        // Настройка выравнивания стиля
        // Для основной таблицы
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Для заголовка таблицы
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setVerticalAlignment(VerticalAlignment.CENTER);

        // Для шапки
        style3.setAlignment(HorizontalAlignment.LEFT);
        style3.setVerticalAlignment(VerticalAlignment.CENTER);

        HSSFRow row = sheet.createRow((short) 0);

        // прописываем шапку документа
        //row.createCell(0).setCellValue("");
        //row.createCell(1).setCellValue("Личный №");
        //row.createCell(2).setCellValue(tfUslugaLCod.getText());
        //row.createCell(3).setCellValue(tfUslugaFIO.getText());

        //row.getCell(1).setCellStyle(style3);
        //row.getCell(2).setCellStyle(style3);
        //row.getCell(3).setCellStyle(style3);
        //row.setHeightInPoints(30);

        // стиль для выделения ячейки со всех сторон
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        // стиль-2 для выделения ячейки со всех сторон
        style2.setBorderTop(BorderStyle.THIN);
        style2.setBorderRight(BorderStyle.THIN);
        style2.setBorderBottom(BorderStyle.THIN);
        style2.setBorderLeft(BorderStyle.THIN);

        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style2.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());  // серый цвет фона для заголовка таблицы

        // создаем строку с заголовком
        row = sheet.createRow((short) 1);
        row.createCell(0).setCellValue("Номер id");
        row.createCell(1).setCellValue("id учета");
        row.createCell(2).setCellValue("Фамилия");
        row.createCell(3).setCellValue("Имя");
        row.createCell(4).setCellValue("Отчество");
        row.createCell(5).setCellValue("Телефон");
        row.createCell(6).setCellValue("E-mail");
        row.createCell(7).setCellValue("Индекс");
        row.createCell(8).setCellValue("Город");
        row.createCell(9).setCellValue("Адрес");
        row.createCell(10).setCellValue("Дата рождения");

        // Устанавливаем высоту и стили для заголовка таблицы 
        for (int k = 0; k < 11; k++) {
            row.getCell(k).setCellStyle(style2);
            row.setHeightInPoints(22);
        }

        // прописываем основную таблицу
        /*
        for (int i = 0; i < tabUslugi.getRowCount(); i++) {
            row = sheet.createRow((short) i + 3);
            for (int k = 0; k < tabUslugi.getColumnCount(); k++) {
                //tabUslugi.getValueAt(i, k).toString() + ";";
                row.createCell(k).setCellValue(tabUslugi.getValueAt(i, k).toString());
                if (k == 8) {
                    row.getCell(k).setCellStyle(style2);
                } else {
                    row.getCell(k).setCellStyle(style);
                }
                row.setHeightInPoints(18);
            }
        }
        */
        
        conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(urlHostBD, userNameBD, passwordBD);
            stmt = conn.createStatement();

            String query;
            query = "select lcod,scod,f,i,o,tel,email,poch_index,gorod_name,adres,date_rog from myservis.client order by f,i,i";
            rs = stmt.executeQuery(query);

            int i = 0;
            while (rs.next()) {
                i++;
                String lCod = rs.getString("lcod");
                String sCod = rs.getString("scod");
                String fam = rs.getString("f");
                String name = rs.getString("i");
                String otch = rs.getString("o");
                String sTel = rs.getString("tel");
                String sEmail = rs.getString("email");
                
                String sIndex = rs.getString("poch_index");
                String sGorod = rs.getString("gorod_name");
                String sAdres = rs.getString("adres");
                String sDate_rog = rs.getString("date_rog");  
                
                row = sheet.createRow((short) i + 1);
                
                row.createCell(0).setCellValue(lCod); row.getCell(0).setCellStyle(style);
                row.createCell(1).setCellValue(sCod); row.getCell(1).setCellStyle(style);
                row.createCell(2).setCellValue(fam); row.getCell(2).setCellStyle(style);
                row.createCell(3).setCellValue(name); row.getCell(3).setCellStyle(style);
                row.createCell(4).setCellValue(otch); row.getCell(4).setCellStyle(style);
                row.createCell(5).setCellValue(sTel); row.getCell(5).setCellStyle(style);
                row.createCell(6).setCellValue(sEmail); row.getCell(6).setCellStyle(style);
                row.createCell(7).setCellValue(sIndex); row.getCell(7).setCellStyle(style);
                row.createCell(8).setCellValue(sGorod); row.getCell(8).setCellStyle(style);
                row.createCell(9).setCellValue(sAdres); row.getCell(9).setCellStyle(style);
                row.createCell(10).setCellValue(sDate_rog); row.getCell(10).setCellStyle(style);
                /*
                if (k == 8) {
                    row.getCell(k).setCellStyle(style2);
                } else {
                    row.getCell(k).setCellStyle(style);
                }
                */
                row.setHeightInPoints(18);                
                
                //fText += lCod+";"+sCod+";"+fam+";"+name+";"+otch+";"+sTel+";"+sEmail+";"+sIndex+";"+sGorod+";"+sAdres+";"+sDate_rog+";\n";
            }
            rs.close();
            stmt.close();            
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    taLog.append("Завершение запроса к БД cleent ... \n");
                    taLog.append("------------------------------------------------- \n");
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
                
        // Устанавливаем автоширину для всех колонок
        for (int k = 0; k < 11; k++) {
            sheet.autoSizeColumn(k);
        }

        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(nameFiles);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            workbook.write(fileOut);
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fileOut.close();
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            workbook.close();
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Your excel file has been generated!");

        JOptionPane.showMessageDialog(new JFrame(), "Файл:\n\n" + progDir + nameFiles + "\n\nCоздан успешно!");
    }//GEN-LAST:event_bbKlientExcelTotActionPerformed

    private void tfBDZakazNalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfBDZakazNalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfBDZakazNalActionPerformed

    private void tfBDZakazQRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfBDZakazQRActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfBDZakazQRActionPerformed

    private void bbVerProgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbVerProgActionPerformed
        String ObjButtons[] = {"Yes", "No"};
        int PromptResult = JOptionPane.showOptionDialog(null,
                "Текущая версия программы: "+sVersionProg+"\nВерсия для обновления на Сервере: "+sVersionServer+"\n\nОбновить версию программу?", "Информация о версии программы",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                ObjButtons, ObjButtons[1]);
        if (PromptResult == 0) {
         
            // копируем новую версию программы    
            Path sourcePath = Paths.get(sDirNewVersion+"myServisProg.jar");
            Path destPath = Paths.get("myServisProg_New.jar");         
        
            try {
                Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {e.printStackTrace();}
            
            // создаем ват фал для обновления:
            
            String sNameFileBat1 = "temp\\update_prog.bat";
            String sTextFileBat1;
            sTextFileBat1 = "cd "+progDir+"\n";
            sTextFileBat1 += "TIMEOUT /T 4 /NOBREAK\n";                                     // Устанавливаем временную задержку
            sTextFileBat1 += "del "+progDir+"myServisProg_Old.jar\n";                       // Удаляем предыдущий временный файл старой версии
            sTextFileBat1 += "TIMEOUT /T 2 /NOBREAK\n";                                     // Устанавливаем временную задержку
            sTextFileBat1 += "rename "+progDir+"myServisProg.jar myServisProg_Old.jar\n";   // Переименовываем текущую программу 
            sTextFileBat1 += "TIMEOUT /T 2 /NOBREAK\n";                                     // Устанавливаем временную задержку
            sTextFileBat1 += "copy "+progDir+"myServisProg_New.jar "+progDir+"myServisProg.jar\n";  // Копируем новую программу
            sTextFileBat1 += "TIMEOUT /T 2 /NOBREAK\n";                                     // Устанавливаем временную задержку
            sTextFileBat1 += "@echo off \n";                                                // 
            sTextFileBat1 += "Start "+progDir+"myServisProg.jar\n";                         // Запускаем новую программу
            
            try {
                Path fileName = Paths.get(sNameFileBat1);
                Files.write(fileName, sTextFileBat1.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException e) {e.printStackTrace();}
                        
            JFrame jfInfo = new JFrame();
            jfInfo.setLocation(0,0);            // Пока не работает
            JOptionPane.showMessageDialog(jfInfo, "Сейчас произойдет запуск обновления программы!\n\nДля перезапуска программы нажмите Ок!");
            
            // Запускаем ват файл:
            try {Process child = Runtime.getRuntime().exec(sNameFileBat1);}
            catch (IOException ex) {Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);}
            
            // Закрываем программу:
            System.exit(0);
        }
    }//GEN-LAST:event_bbVerProgActionPerformed

    private void tfBDZakazLcodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfBDZakazLcodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfBDZakazLcodActionPerformed

    private void tfBDZakazLcodKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfBDZakazLcodKeyTyped
        char c = evt.getKeyChar();
        if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE)
                || c == KeyEvent.VK_DELETE)) {
            getToolkit().beep();
            evt.consume();
        }
        
    }//GEN-LAST:event_tfBDZakazLcodKeyTyped

    private void chBDzakazLCodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chBDzakazLCodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chBDzakazLCodActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            //   java.util.logging.Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            new NewJFrameMainMyservisProg().setVisible(true);
        });

    }

    // Оставил для примера не использую
    static public String strlenFix(String str) {
        while (str.length() < 4) {
            str = "0" + str;
        }
        return str;
    }

    // Оставил для примера не использую
    static String unicodeEscaped(char ch) {
        if (ch < 0x10) {
            return "\\u000" + Integer.toHexString(ch);
        } else if (ch < 0x100) {
            return "\\u00" + Integer.toHexString(ch);
        } else if (ch < 0x1000) {
            return "\\u0" + Integer.toHexString(ch);
        }
        return "\\u" + Integer.toHexString(ch);
    }

    static public String strKiril(char ch) {
        if ((ch == 'А') || (ch == 'Б') || (ch == 'В') || (ch == 'Г') || (ch == 'Д') || (ch == 'Е') || (ch == 'Ё') || (ch == 'Ж') || (ch == 'З') || (ch == 'И') || (ch == 'К') || (ch == 'Л') || (ch == 'М') || (ch == 'Н') || (ch == 'О') || (ch == 'П') || (ch == 'Р') || (ch == 'С') || (ch == 'Т') || (ch == 'У') || (ch == 'Ф') || (ch == 'Х') || (ch == 'Ц') || (ch == 'Ч') || (ch == 'Ш') || (ch == 'Щ') || (ch == 'Ь') || (ch == 'Ы') || (ch == 'Ъ') || (ch == 'Э') || (ch == 'Ю') || (ch == 'Я') || (ch == 'Й')
                || (ch == 'а') || (ch == 'б') || (ch == 'в') || (ch == 'г') || (ch == 'д') || (ch == 'е') || (ch == 'ё') || (ch == 'ж') || (ch == 'з') || (ch == 'и') || (ch == 'к') || (ch == 'л') || (ch == 'м') || (ch == 'н') || (ch == 'о') || (ch == 'п') || (ch == 'р') || (ch == 'с') || (ch == 'т') || (ch == 'у') || (ch == 'ф') || (ch == 'х') || (ch == 'ц') || (ch == 'ч') || (ch == 'ш') || (ch == 'щ') || (ch == 'ь') || (ch == 'ы') || (ch == 'ъ') || (ch == 'э') || (ch == 'ю') || (ch == 'я') || (ch == 'й')) {
            return "1";
        } else {
            return "";
        }
    }

    static public String strPreobraz(String str) {
        String resKir;
        String resTek = "";
        for (int i = 0; i < str.length(); i++) {
            //char charCode = inString.charAt(i);
            char c = str.charAt(i);
            //System.out.println(char2);
            resKir = strKiril(c);
            if ("1".equals(resKir)) {
                resTek += String.format("\\u%04x", (int) str.charAt(i));
            } else {
                resTek += c;
            }
        }
        return resTek;
    }

    static public String sParsingDescription(String str) {
        String resTek = "";
        for (int i = 0; i < str.length(); i++) {
            //char charCode = inString.charAt(i);
            char ch = str.charAt(i);
            if ((ch == ',') || (ch == '"') || (ch == '}') )
                    {            
                break;
            }else{
                resTek += ch;
            }
        }
        return resTek;
    }
    
// простой и удобный метод копирования файла в Java 7

    
        
    //private javax.swing.JTextField tfPrihodSumma;
    //private javax.swing.JTextField tfRashodSumma;
    //private javax.swing.JTextField tfPrihodFIO;
    //private javax.swing.JTextField tfRashodFIO;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane ScrollPanKlientTab;
    private javax.swing.JButton bbBDprihod;
    private javax.swing.JButton bbBDprihodCSV;
    private javax.swing.JButton bbBDzakaz;
    private javax.swing.JButton bbBDzakazCSV;
    private javax.swing.JButton bbBDzakazXLS;
    private javax.swing.JButton bbFindFIO;
    private javax.swing.JButton bbFindLCod;
    private javax.swing.JButton bbKKTaddGoods;
    private javax.swing.JButton bbKKTcloseCheck;
    private javax.swing.JButton bbKKTcloseSmenaTot;
    private javax.swing.JButton bbKKTdobriyDen;
    private javax.swing.JButton bbKKTdobriyTest;
    private javax.swing.JButton bbKKTdraiverTest;
    private javax.swing.JButton bbKKTgetStatus;
    private javax.swing.JButton bbKKTopenCheck;
    private javax.swing.JButton bbKKTopenSmenaTot;
    private javax.swing.JButton bbKKTpostTest;
    private javax.swing.JButton bbKKTpreobrazovanieTest;
    private javax.swing.JButton bbKKTprintText;
    private javax.swing.JButton bbKKTprovStatus;
    private javax.swing.JButton bbKKTpythonTest;
    private javax.swing.JButton bbKKTsessionClose;
    private javax.swing.JButton bbKKTsessionOpen;
    private javax.swing.JButton bbKKTsmenaClose;
    private javax.swing.JButton bbKKTsmenaOpen;
    private javax.swing.JButton bbKKTtowar;
    private javax.swing.JButton bbKKTzapusk;
    private javax.swing.JButton bbKassaCSV;
    private javax.swing.JButton bbKassaExcel;
    private javax.swing.JButton bbKassaOtchet;
    private javax.swing.JButton bbKassaPrihod;
    private javax.swing.JButton bbKassaRashod;
    private javax.swing.JButton bbKlientCSV;
    private javax.swing.JButton bbKlientCSVtot;
    private javax.swing.JButton bbKlientExcelTot;
    private javax.swing.JButton bbKlientRedakt;
    private javax.swing.JButton bbKlientSave;
    private javax.swing.JButton bbKlientZakaz;
    private javax.swing.JButton bbLogin;
    private javax.swing.JButton bbNewFrame;
    private javax.swing.JButton bbNewKlient;
    private javax.swing.JButton bbNewKlientReg;
    private javax.swing.JButton bbNewTowar;
    private javax.swing.JButton bbObsl;
    private javax.swing.JButton bbPrisePrihod;
    private javax.swing.JButton bbPriseSave;
    private javax.swing.JButton bbPriseZagruz;
    private javax.swing.JButton bbRaschet;
    private javax.swing.JButton bbSaveXLS;
    private javax.swing.JButton bbUchetUslug;
    private javax.swing.JButton bbUslugaCSV;
    private javax.swing.JButton bbUslugaOtchet;
    private javax.swing.JButton bbUslugaZak;
    private javax.swing.JButton bbVerProg;
    private javax.swing.JButton bbZagruz;
    private javax.swing.JButton bbZakaz;
    private javax.swing.JButton bbZakazZak;
    private javax.swing.JComboBox<String> cbBDzakazMon;
    private javax.swing.JComboBox<String> cbKassaMon;
    private javax.swing.JComboBox<String> cbLogin;
    private javax.swing.JComboBox<String> cbPriseGroup;
    private javax.swing.JComboBox<String> cbSkidka;
    private javax.swing.JComboBox<String> cbZakazGroup;
    private javax.swing.JCheckBox chBDzakazLCod;
    private javax.swing.JCheckBox chEmail;
    private javax.swing.JCheckBox chKKT;
    private javax.swing.JCheckBox chKassaOtchetMon;
    private javax.swing.JCheckBox chKlientTot;
    private javax.swing.JCheckBox chNal;
    private javax.swing.JCheckBox chOFD;
    private javax.swing.JCheckBox chPriseRedakt;
    private javax.swing.JCheckBox chQR;
    private javax.swing.JCheckBox chUsluga;
    private javax.swing.JCheckBox chZakazWib;
    private javax.swing.JMenuItem imOpenPrihod;
    private javax.swing.JMenuItem imOpenZakaz;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTextField jfEmail;
    private javax.swing.JTextField jtFindFIO;
    private javax.swing.JLabel lbBDzakazDate;
    private javax.swing.JLabel lbBDzakazGod;
    private javax.swing.JLabel lbBDzakazGod1;
    private javax.swing.JLabel lbBDzakazMon;
    private javax.swing.JLabel lbBDzakazNal;
    private javax.swing.JLabel lbBDzakazQR;
    private javax.swing.JLabel lbBDzakazSumma;
    private javax.swing.JLabel lbBDzakazSumma4;
    private javax.swing.JLabel lbBDzakazSumma5;
    private javax.swing.JLabel lbBDzakazSumma6;
    private javax.swing.JLabel lbBDzakazSumma7;
    private javax.swing.JLabel lbComPort;
    private javax.swing.JLabel lbComPort1;
    private javax.swing.JLabel lbDateRog;
    private javax.swing.JLabel lbInfoNewTowar;
    private javax.swing.JLabel lbIsp;
    private javax.swing.JLabel lbKKTsmena;
    private javax.swing.JLabel lbKKTsmenaInfo;
    private javax.swing.JLabel lbMainDateTime;
    private javax.swing.JLabel lbNameKKT;
    private javax.swing.JLabel lbOstatok;
    private javax.swing.JLabel lbOstatok1;
    private javax.swing.JLabel lbPolInfo;
    private javax.swing.JLabel lbZakazPrSk;
    private javax.swing.JLabel lbZakazPrSk1;
    private javax.swing.JPanel panBD;
    private javax.swing.JPanel panBDprihod;
    private javax.swing.JPanel panBDprihodBottom;
    private javax.swing.JPanel panBDprihodTop;
    private javax.swing.JPanel panBDtop;
    private javax.swing.JPanel panBDzakaz;
    private javax.swing.JPanel panBDzakazBottom;
    private javax.swing.JPanel panBDzakazTop;
    private javax.swing.JPanel panFormZakaz;
    private javax.swing.JPanel panKKT;
    private javax.swing.JPanel panKasBottom;
    private javax.swing.JPanel panKasPrihod;
    private javax.swing.JPanel panKasRashod;
    private javax.swing.JPanel panKasTop;
    private javax.swing.JPanel panKassa;
    private javax.swing.JPanel panKlient;
    private javax.swing.JPanel panKlientBottom;
    private javax.swing.JPanel panKlientCenter;
    private javax.swing.JPanel panKlientLeft;
    private javax.swing.JPanel panKlientTop;
    private javax.swing.JPanel panLog;
    private javax.swing.JPanel panLogDop;
    private javax.swing.JPanel panNewKlient;
    private javax.swing.JPanel panNewKlientForma;
    private javax.swing.JPanel panNewLeft;
    private javax.swing.JPanel panPolTopInfo;
    private javax.swing.JPanel panPolzovatel;
    private javax.swing.JPanel panPolzovatelTop;
    private javax.swing.JPanel panPosechenie;
    private javax.swing.JPanel panTabZakaz;
    private javax.swing.JPanel panUslugi;
    private javax.swing.JPanel panUslugiTop;
    private javax.swing.JPanel panUslugiTop2;
    private javax.swing.JPanel panZakaz;
    private javax.swing.JLabel pbKlientPrim;
    private javax.swing.JPopupMenu ppPrihodItem;
    private javax.swing.JPopupMenu ppZakazItem;
    private javax.swing.JPanel pznPrise;
    private javax.swing.JTextArea taInfoMain;
    private javax.swing.JTextArea taKKTlog;
    private javax.swing.JTextArea taKKTtowar;
    private javax.swing.JTextArea taKlientPrim;
    private javax.swing.JTextArea taLog;
    private javax.swing.JTextArea taPrimNew;
    private javax.swing.JTable tabBDprihod;
    private javax.swing.JTable tabBDzakaz;
    private javax.swing.JTable tabFIO;
    private javax.swing.JTable tabKasPrihod;
    private javax.swing.JTable tabKasRashod;
    private javax.swing.JTabbedPane tabPanKKT;
    private javax.swing.JTabbedPane tabPanMain;
    private javax.swing.JTable tabPrise;
    private javax.swing.JTable tabTowar;
    private javax.swing.JTable tabUslugi;
    private javax.swing.JTable tabZakaz;
    private javax.swing.JTextField tfAdresNew;
    private javax.swing.JTextField tfBDZakazDat;
    private javax.swing.JTextField tfBDZakazGod;
    private javax.swing.JTextField tfBDZakazLcod;
    private javax.swing.JTextField tfBDZakazNal;
    private javax.swing.JTextField tfBDZakazQR;
    private javax.swing.JTextField tfBDZakazSumma;
    private javax.swing.JTextField tfBDprihodSumma;
    private javax.swing.JTextField tfDay;
    private javax.swing.JTextField tfDumpFile;
    private javax.swing.JTextField tfEmailNew;
    private javax.swing.JTextField tfFamNew;
    private javax.swing.JTextField tfGod;
    private javax.swing.JTextField tfGorodNew;
    private javax.swing.JTextField tfIndexNew;
    private javax.swing.JTextField tfIsp;
    private javax.swing.JTextField tfKKTComPort;
    private javax.swing.JTextField tfKKTmodel;
    private javax.swing.JTextField tfKKTotvet;
    private javax.swing.JTextField tfKKTtext;
    private javax.swing.JTextField tfKKTzapros;
    private javax.swing.JTextField tfKasPrihodNal;
    private javax.swing.JTextField tfKasPrihodQR;
    private javax.swing.JTextField tfKasRashod;
    private javax.swing.JTextField tfKassaDat;
    private javax.swing.JTextField tfKassaGod;
    private javax.swing.JTextField tfKassaOstatok;
    private javax.swing.JTextField tfKassaTek;
    private javax.swing.JTextField tfKeySession;
    private javax.swing.JTextField tfKlientAdres;
    private javax.swing.JTextField tfKlientDay;
    private javax.swing.JTextField tfKlientEmail;
    private javax.swing.JTextField tfKlientFam;
    private javax.swing.JTextField tfKlientFind;
    private javax.swing.JTextField tfKlientGod;
    private javax.swing.JTextField tfKlientGorod;
    private javax.swing.JTextField tfKlientIndex;
    private javax.swing.JTextField tfKlientLCod;
    private javax.swing.JTextField tfKlientMon;
    private javax.swing.JTextField tfKlientName;
    private javax.swing.JTextField tfKlientOtch;
    private javax.swing.JTextField tfKlientPriseFIO;
    private javax.swing.JTextField tfKlientPriseLCod;
    private javax.swing.JTextField tfKlientPriseSCod;
    private javax.swing.JTextField tfKlientSCod;
    private javax.swing.JTextField tfKlientTel;
    private javax.swing.JTextField tfKlientZakazFIO;
    private javax.swing.JTextField tfKlientZakazID_zak;
    private javax.swing.JTextField tfKlientZakazLCod;
    private javax.swing.JTextField tfKlientZakazSC;
    private javax.swing.JTextField tfKlientZakazSCod;
    private javax.swing.JTextField tfKlientZakazSkidPr;
    private javax.swing.JTextField tfLCodNew;
    private javax.swing.JTextField tfMon;
    private javax.swing.JTextField tfNamNew;
    private javax.swing.JTextField tfOthNew;
    private javax.swing.JTextField tfPrihodID_prih;
    private javax.swing.JTextField tfPrihodPrim;
    private javax.swing.JTextField tfRezultKKT;
    private javax.swing.JTextField tfSCodNew;
    private javax.swing.JTextField tfSummaZak;
    private javax.swing.JTextField tfTelNew;
    private javax.swing.JTextField tfTowarNewArtikul;
    private javax.swing.JTextField tfTowarNewCena;
    private javax.swing.JTextField tfTowarNewName;
    private javax.swing.JTextField tfTowarNewSort;
    private javax.swing.JTextField tfUslugaArtikul;
    private javax.swing.JTextField tfUslugaFIO;
    private javax.swing.JTextField tfUslugaID;
    private javax.swing.JTextField tfUslugaIspTot;
    private javax.swing.JTextField tfUslugaLCod;
    private javax.swing.JTextField tfUslugaName;
    private javax.swing.JTextField tfUslugaOstatok;
    private javax.swing.JTextField tfWibZakSumma;
    private javax.swing.JPasswordField tfpPas;
    private javax.swing.JTabbedPane tpBD;
    private javax.swing.JPanel tpanKKTlog;
    private javax.swing.JPanel tpanKKTtowar;
    // End of variables declaration//GEN-END:variables

    //private void or(boolean equals) {
    //    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    //}
}
/*
Integer.toString() - преобразование числа в строку
int result = Integer.parseInt(givenString); - преобразование строки в Число

Работа с таблицей
TabTowar.setRowHeight(TabTowar.getRowHeight() + 40);   // Высота строки в таблице
int rowIndex = table.getSelectedRow();                 // Выделенный ряд
Integer rowCount = TabTowar.getRowCount();             // Количество Строк 
Integer colCount = tabTowar.getColumnCount();          // Количество Рядов
String strValue = model.getValueAt(0, 1).toString();   // Получение значения ячейки

Работа с comboBox
jPB1.setValue((jCB1.getSelectedIndex()+1)*10);         // прогресс Бар Index из combobox
String b=jCB1.getItemAt(jCB1.getSelectedIndex());      // Значение из combobox


&& - AND
|| - OR

try {
    // Возьмите файл
    File f = new File("D:\\example.txt");
    //Создайте новый файл
    // Убедитесь, что он не существует 
    if (f.createNewFile())
        System.out.println("File created");
    else
        System.out.println("File already exists");
    }
    catch (Exception e) {
    System.err.println(e);
        System.exit(0);  // Выход из программы
}

// кракозябры в post ответе
1)outputStream.writeBytes(URLEncoder.encode(text2, "UTF-8"));
2)// вместо строки DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
// пишем нижеприведенные две.
// OutputStreamWriter будет преобразовывать строку в UTF-8.

DataOutputStream tmpStream = new DataOutputStream(connection.getOutputStream());
BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(tmpStream, "UTF-8"));
3)
httppost.setEntity(new StringEntity(str,"utf-8"));

4)
BufferedReader reader =  new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
String str = reader.readLine();


 */
